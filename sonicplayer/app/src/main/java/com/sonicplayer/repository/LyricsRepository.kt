package com.sonicplayer.repository

import android.content.Context
import android.net.Uri
import com.sonicplayer.data.model.Lyrics
import com.sonicplayer.data.model.Song
import com.sonicplayer.util.LyricsParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads lyrics from a sidecar .lrc/.txt file or from the audio file tags.
 * Results (including "no lyrics") are cached so scrolling the player stays cheap.
 */
@Singleton
class LyricsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cache = object : LinkedHashMap<Long, Lyrics?>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Lyrics?>?): Boolean {
            return size > MAX_CACHED_TRACKS
        }
    }

    suspend fun getLyrics(song: Song): Lyrics? = withContext(Dispatchers.IO) {
        synchronized(cache) {
            if (cache.containsKey(song.id)) return@withContext cache[song.id]
        }

        val loaded = runCatching { loadLyrics(song) }.getOrNull()
        synchronized(cache) { cache[song.id] = loaded }
        loaded
    }

    private fun loadLyrics(song: Song): Lyrics? {
        readSidecar(song)?.let { text ->
            LyricsParser.parse(text, source = "file")?.let { return it }
        }

        val tagBytes = readTagBytes(song) ?: return null
        val embedded = LyricsParser.extractEmbedded(tagBytes, song.path) ?: return null
        return LyricsParser.parse(embedded, source = "tag")
    }

    private fun readSidecar(song: Song): String? {
        if (song.path.isBlank()) return null
        val audioFile = File(song.path)
        val basePath = audioFile.path.substringBeforeLast('.', audioFile.path)

        for (extension in SIDECAR_EXTENSIONS) {
            val candidate = File("$basePath.$extension")
            if (!candidate.isFile) continue
            val text = runCatching { candidate.readText() }.getOrNull()
            if (!text.isNullOrBlank()) return text
        }
        return null
    }

    private fun readTagBytes(song: Song): ByteArray? {
        val uri: Uri = runCatching { song.contentUri }.getOrNull() ?: return null
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(64 * 1024)
                var total = 0
                while (total < MAX_TAG_BYTES) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    output.write(buffer, 0, read)
                    total += read
                }
                output.toByteArray()
            }
        }.getOrNull()
    }

    companion object {
        private const val MAX_TAG_BYTES = 8 * 1024 * 1024
        private const val MAX_CACHED_TRACKS = 60

        private val SIDECAR_EXTENSIONS = listOf("lrc", "LRC", "txt")
    }
}
