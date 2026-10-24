package com.sonicplayer.data.model

/** A single line of lyrics. When [timeMs] is negative the lyrics are unsynchronised. */
data class LyricLine(
    val timeMs: Long,
    val text: String
)

/**
 * Lyrics for one track, either parsed from an LRC file or extracted from the audio tags.
 */
data class Lyrics(
    val lines: List<LyricLine>,
    val synced: Boolean,
    val source: String = ""
) {
    fun activeLineIndex(positionMs: Long): Int {
        if (!synced || lines.isEmpty()) return -1
        var low = 0
        var high = lines.lastIndex
        var result = -1
        while (low <= high) {
            val middle = (low + high) ushr 1
            if (lines[middle].timeMs <= positionMs) {
                result = middle
                low = middle + 1
            } else {
                high = middle - 1
            }
        }
        return result
    }
}
