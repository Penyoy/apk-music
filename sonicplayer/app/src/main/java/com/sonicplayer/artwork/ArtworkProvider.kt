package com.sonicplayer.artwork

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import com.sonicplayer.R
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.concurrent.thread

/**
 * Serves album/embedded artwork through a stable content:// URI.
 *
 * Both Coil (UI thumbnails) and Media3 (notification large icon) can read this provider,
 * so the same bounded-size image is used everywhere. When a track has no artwork the
 * bundled [R.drawable.placeholder] image is returned instead of an error.
 */
class ArtworkProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val pipe = ParcelFileDescriptor.createPipe()
        val readSide = pipe[0]
        val writeSide = pipe[1]

        // Resolve and write on a background thread so openFile never blocks the binder thread.
        thread(name = "sonic-artwork", isDaemon = true) {
            try {
                ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { output ->
                    output.write(loadArtworkBytes(uri))
                }
            } catch (_: IOException) {
                // Reader may close early when an image request is cancelled.
            } catch (_: Throwable) {
                // A failed decode must never crash the provider process.
            } finally {
                runCatching { writeSide.close() }
            }
        }
        return readSide
    }

    override fun getType(uri: Uri): String = "image/jpeg"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    private fun loadArtworkBytes(uri: Uri): ByteArray {
        val key = uri.toString()
        cache.get(key)?.let { return it }

        val appContext = context ?: return ByteArray(0)
        val bitmap = runCatching { resolveBitmap(appContext, uri) }.getOrNull()
        val bytes = if (bitmap != null) compress(bitmap) else placeholderBytes(appContext)
        if (bytes.isNotEmpty()) cache.put(key, bytes)
        return bytes
    }

    private fun placeholderBytes(context: Context): ByteArray {
        cache.get(PLACEHOLDER_KEY)?.let { return it }
        val bytes = runCatching {
            BitmapFactory.decodeResource(context.resources, R.drawable.placeholder)
                ?.let { compress(it) }
        }.getOrNull() ?: ByteArray(0)
        if (bytes.isNotEmpty()) cache.put(PLACEHOLDER_KEY, bytes)
        return bytes
    }

    private fun resolveBitmap(context: Context, uri: Uri): Bitmap? {
        val segments = uri.pathSegments
        if (segments.size < 2) return null
        val id = segments[1].toLongOrNull() ?: return null
        return when (segments[0]) {
            "song" -> loadSongBitmap(context, id)
            "album" -> loadAlbumBitmap(context, id)
            else -> null
        }
    }

    private fun loadSongBitmap(context: Context, songId: Long): Bitmap? {
        val song = findSong(context, songId)

        // MediaStore can generate a correctly oriented thumbnail for us on Android 10+.
        if (song != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                context.contentResolver.loadThumbnail(
                    song.uri,
                    Size(MAX_ART_SIZE, MAX_ART_SIZE),
                    null
                )
            }.getOrNull()?.let { return scaleDown(it) }
        }

        // Most music files store the cover as an embedded picture (ID3 APIC / FLAC PICTURE).
        if (song != null) {
            loadEmbeddedBitmap(context, song.uri)?.let { return scaleDown(it) }
        }

        // Last resort: the legacy album-art provider used by older Android versions.
        if (song != null && song.albumId > 0) {
            loadLegacyAlbumArt(context, song.albumId)?.let { return scaleDown(it) }
        }
        return null
    }

    private fun loadAlbumBitmap(context: Context, albumId: Long): Bitmap? {
        val song = findSongForAlbum(context, albumId)
        if (song != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                runCatching {
                    context.contentResolver.loadThumbnail(
                        song.uri,
                        Size(MAX_ART_SIZE, MAX_ART_SIZE),
                        null
                    )
                }.getOrNull()?.let { return scaleDown(it) }
            }
            loadEmbeddedBitmap(context, song.uri)?.let { return scaleDown(it) }
        }
        return loadLegacyAlbumArt(context, albumId)?.let { scaleDown(it) }
    }

    private fun findSong(context: Context, songId: Long): SongRef? {
        val idColumn = MediaStore.Audio.Media._ID
        val albumColumn = MediaStore.Audio.Media.ALBUM_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val projection = arrayOf(idColumn, albumColumn, MediaStore.MediaColumns.VOLUME_NAME)
            runCatching {
                context.contentResolver.query(
                    collection,
                    projection,
                    "$idColumn = ?",
                    arrayOf(songId.toString()),
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(albumColumn))
                        val volume = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.VOLUME_NAME)
                        )
                        val itemUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.getContentUri(volume),
                            songId
                        )
                        return SongRef(itemUri, albumId)
                    }
                }
            }
        }

        return runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(idColumn, albumColumn),
                "$idColumn = ?",
                arrayOf(songId.toString()),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    SongRef(
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            songId
                        ),
                        cursor.getLong(cursor.getColumnIndexOrThrow(albumColumn))
                    )
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    private fun findSongForAlbum(context: Context, albumId: Long): SongRef? {
        val idColumn = MediaStore.Audio.Media._ID
        val albumColumn = MediaStore.Audio.Media.ALBUM_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val projection = arrayOf(idColumn, albumColumn, MediaStore.MediaColumns.VOLUME_NAME)
            runCatching {
                context.contentResolver.query(
                    collection,
                    projection,
                    "$albumColumn = ?",
                    arrayOf(albumId.toString()),
                    "$idColumn ASC"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val songId = cursor.getLong(cursor.getColumnIndexOrThrow(idColumn))
                        val volume = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.VOLUME_NAME)
                        )
                        val itemUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.getContentUri(volume),
                            songId
                        )
                        return SongRef(itemUri, albumId)
                    }
                }
            }
        }

        return runCatching {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(idColumn, albumColumn),
                "$albumColumn = ?",
                arrayOf(albumId.toString()),
                "$idColumn ASC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val songId = cursor.getLong(cursor.getColumnIndexOrThrow(idColumn))
                    SongRef(
                        ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            songId
                        ),
                        albumId
                    )
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    private fun loadEmbeddedBitmap(context: Context, uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture?.let { decodeSampled(it) }
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun loadLegacyAlbumArt(context: Context, albumId: Long): Bitmap? {
        val legacyUri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
        return runCatching {
            context.contentResolver.openInputStream(legacyUri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()
    }

    private fun decodeSampled(data: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / sample > MAX_ART_SIZE * 2 ||
            bounds.outHeight / sample > MAX_ART_SIZE * 2
        ) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= MAX_ART_SIZE) return bitmap
        val ratio = MAX_ART_SIZE.toFloat() / largest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun compress(bitmap: Bitmap): ByteArray {
        val scaled = scaleDown(bitmap)
        return ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 88, output)
            output.toByteArray()
        }
    }

    private data class SongRef(val uri: Uri, val albumId: Long)

    companion object {
        private const val AUTHORITY = "com.sonicplayer.artwork"
        private const val MAX_ART_SIZE = 640
        private const val CACHE_BYTES = 12 * 1024 * 1024
        private const val PLACEHOLDER_KEY = "asset://placeholder"

        fun songUri(songId: Long): Uri = Uri.parse("content://$AUTHORITY/song/$songId")

        fun albumUri(albumId: Long): Uri = Uri.parse("content://$AUTHORITY/album/$albumId")

        private val cache = object : LruCache<String, ByteArray>(CACHE_BYTES) {
            override fun sizeOf(key: String, value: ByteArray): Int = value.size
        }
    }
}
