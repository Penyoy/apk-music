package com.sonicplayer.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.sonicplayer.artwork.ArtworkProvider
import com.sonicplayer.data.dao.FavoriteDao
import com.sonicplayer.data.dao.RecentlyPlayedDao
import com.sonicplayer.data.dao.SongDao
import com.sonicplayer.data.model.Album
import com.sonicplayer.data.model.Artist
import com.sonicplayer.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val favoriteDao: FavoriteDao,
    private val recentlyPlayedDao: RecentlyPlayedDao
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> = songDao.getSongsByAlbum(albumId)
    fun getSongsByArtist(artist: String): Flow<List<Song>> = songDao.getSongsByArtist(artist)
    fun getSongsInFolder(folderPath: String): Flow<List<Song>> = songDao.getSongsInFolder(folderPath)
    fun getFavoriteSongs(): Flow<List<Song>> = songDao.getFavoriteSongs()
    fun getRecentlyPlayedSongs(): Flow<List<Song>> = songDao.getRecentlyPlayedSongs()
    fun getRecentlyAddedSongs(): Flow<List<Song>> = songDao.getRecentlyAddedSongs()
    fun getMostPlayedSongs(): Flow<List<Song>> = songDao.getMostPlayedSongs()
    fun getSongCount(): Flow<Int> = songDao.getSongCount()

    fun getAllAlbums(): Flow<List<Album>> = songDao.getAllAlbums().map { albums ->
        albums.map { albumInfo ->
            Album(
                id = albumInfo.id,
                title = albumInfo.title,
                artist = albumInfo.artist,
                artistId = albumInfo.artistId,
                songCount = albumInfo.songCount,
                year = albumInfo.year,
                duration = albumInfo.duration,
                coverUri = getAlbumArtUri(albumInfo.id)
            )
        }
    }

    fun getAllArtists(): Flow<List<Artist>> = songDao.getAllArtists().map { artists ->
        artists.map { artistInfo ->
            Artist(
                id = artistInfo.id,
                name = artistInfo.artist,
                songCount = artistInfo.songCount,
                albumCount = artistInfo.albumCount,
                duration = artistInfo.duration
            )
        }
    }

    suspend fun toggleFavorite(songId: Long) {
        val isFav = favoriteDao.isFavorite(songId)
        songDao.updateFavoriteStatus(songId, !isFav)
        favoriteDao.toggleFavorite(songId)
    }

    suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)

    suspend fun recordPlay(songId: Long) {
        songDao.incrementPlayCount(songId)
        recentlyPlayedDao.recordPlay(songId)
    }

    /**
     * Scans the device media library and stores a fresh copy of every track while
     * preserving the user's favorites and play statistics.
     */
    suspend fun refreshLibrary() = withContext(Dispatchers.IO) {
        val scanned = runCatching { scanAudioFiles() }.getOrElse { emptyList() }
        if (scanned.isEmpty()) return@withContext

        val previous = runCatching { songDao.getAllSongsOnce() }
            .getOrElse { emptyList() }
            .associateBy { it.id }

        val merged = scanned.map { song ->
            previous[song.id]?.let { old ->
                song.copy(
                    isFavorite = old.isFavorite,
                    playCount = old.playCount,
                    lastPlayed = old.lastPlayed,
                    datePlayed = old.datePlayed
                )
            } ?: song
        }
        songDao.replaceAll(merged)
    }

    private fun scanAudioFiles(): List<Song> {
        val songs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        // Only use columns that are available on every supported Android version.
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        runCatching {
            context.contentResolver.query(collection, projection, selection, null, sortOrder)
                ?.use { cursor ->
                    val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    if (idColumn < 0) return@use

                    val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val trackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                    val dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                    val dateModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                    val pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        if (id <= 0L) continue

                        val uri = ContentUris.withAppendedId(collection, id).toString()
                        songs.add(
                            Song(
                                id = id,
                                title = cursor.stringAt(titleColumn),
                                artist = cursor.stringAt(artistColumn),
                                album = cursor.stringAt(albumColumn),
                                albumId = cursor.longAt(albumIdColumn),
                                duration = cursor.longAt(durationColumn).coerceAtLeast(0L),
                                trackNumber = cursor.intAt(trackColumn),
                                dateAdded = cursor.longAt(dateAddedColumn) * 1000L,
                                dateModified = cursor.longAt(dateModifiedColumn) * 1000L,
                                path = cursor.stringAt(pathColumn),
                                uri = uri,
                                year = cursor.intAt(yearColumn),
                                genre = "",
                                bitrate = 0,
                                sampleRate = 0
                            )
                        )
                    }
                }
        }
        return songs
    }

    companion object {
        fun getAlbumArtUri(albumId: Long): Uri = ArtworkProvider.albumUri(albumId)

        fun getSongUri(songId: Long): Uri {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
        }
    }
}

private fun Cursor.stringAt(column: Int): String {
    if (column < 0 || isNull(column)) return ""
    return runCatching { getString(column) }.getOrNull().orEmpty()
}

private fun Cursor.longAt(column: Int): Long {
    if (column < 0 || isNull(column)) return 0L
    return runCatching { getLong(column) }.getOrDefault(0L)
}

private fun Cursor.intAt(column: Int): Int {
    if (column < 0 || isNull(column)) return 0
    return runCatching { getInt(column) }.getOrDefault(0)
}
