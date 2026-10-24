package com.sonicplayer.util

import com.sonicplayer.data.model.LyricLine
import com.sonicplayer.data.model.Lyrics
import java.nio.charset.Charset
import java.util.Locale

/**
 * Parses LRC text and lyrics stored inside common audio containers.
 *
 * Supported containers:
 *  - ID3v2.2 / v2.3 / v2.4 `ULT` / `USLT` frames (MP3, some M4A files)
 *  - FLAC / Ogg Vorbis comments (`LYRICS`, `UNSYNCEDLYRICS`, `SYNCEDLYRICS`)
 *  - MP4 `©lyr` atom
 */
object LyricsParser {

    private val timeTag = Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")
    private val metadataTags = listOf(
        "[ar:", "[ti:", "[al:", "[by:", "[offset:", "[re:", "[ve:", "[length:", "[tool:"
    )

    fun parse(raw: String, source: String = "text"): Lyrics? {
        val text = raw.removePrefix("\uFEFF").replace("\r\n", "\n").replace('\r', '\n')
        if (text.isBlank()) return null

        val timed = mutableListOf<LyricLine>()
        val plain = mutableListOf<String>()
        var offset = 0L

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach

            if (metadataTags.any { line.startsWith(it, ignoreCase = true) }) {
                if (line.startsWith("[offset:", ignoreCase = true)) {
                    offset = line.substringAfter(':', "").substringBefore(']').trim()
                        .toLongOrNull() ?: 0L
                }
                return@forEach
            }

            val matches = timeTag.findAll(line).toList()
            if (matches.isEmpty()) {
                plain += line
            } else {
                val content = timeTag.replace(line, "").trim()
                if (content.isNotEmpty()) {
                    matches.forEach { match ->
                        val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                        val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                        val fraction = match.groupValues[3]
                        val millis = when (fraction.length) {
                            1 -> fraction.toLongOrNull()?.times(100) ?: 0L
                            2 -> fraction.toLongOrNull()?.times(10) ?: 0L
                            3 -> fraction.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        timed += LyricLine(
                            timeMs = minutes * 60_000L + seconds * 1_000L + millis,
                            text = content
                        )
                    }
                }
            }
        }

        if (timed.isNotEmpty()) {
            val adjusted = timed
                .map { it.copy(timeMs = (it.timeMs + offset).coerceAtLeast(0L)) }
                .sortedBy { it.timeMs }
            return Lyrics(adjusted, synced = true, source = source)
        }

        val lines = plain.ifEmpty {
            text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
        if (lines.isEmpty()) return null
        return Lyrics(lines.map { LyricLine(-1L, it) }, synced = false, source = source)
    }

    /** Extracts lyrics text from the first tag block in [bytes], or null if none was found. */
    fun extractEmbedded(bytes: ByteArray, path: String = ""): String? {
        parseId3(bytes)?.let { return it }
        parseFlac(bytes)?.let { return it }
        parseMp4(bytes)?.let { return it }
        return null
    }

    // ── ID3 ──────────────────────────────────────────────────────────────────

    private fun parseId3(bytes: ByteArray): String? {
        if (bytes.size < 10) return null
        if (bytes[0] != 'I'.code.toByte() ||
            bytes[1] != 'D'.code.toByte() ||
            bytes[2] != '3'.code.toByte()
        ) {
            return null
        }

        val version = bytes[3].toInt() and 0xFF
        if (version < 2 || version > 4) return null
        val flags = bytes[5].toInt() and 0xFF
        val tagSize = syncSafe(bytes, 6)
        val end = minOf(bytes.size, 10 + tagSize)
        var position = 10

        if (flags and 0x40 != 0) {
            position += if (version == 4) {
                syncSafe(bytes, position)
            } else {
                readIntBe(bytes, position) + 4
            }
        }
        if (position < 10 || position >= end) return null

        return if (version == 2) {
            parseId3v22(bytes, position, end)
        } else {
            parseId3v23(bytes, position, end, version)
        }
    }

    private fun parseId3v22(bytes: ByteArray, start: Int, end: Int): String? {
        var position = start
        while (position + 6 <= end) {
            if (bytes[position] == 0.toByte()) break
            val id = String(bytes, position, 3, Charsets.ISO_8859_1)
            val size = readUInt24Be(bytes, position + 3)
            if (size <= 0 || position + 6 + size > end) break
            if (id == "ULT" || id == "USL") {
                parseUslt(bytes, position + 6, position + 6 + size)?.let { return it }
            }
            position += 6 + size
        }
        return null
    }

    private fun parseId3v23(
        bytes: ByteArray,
        start: Int,
        end: Int,
        version: Int
    ): String? {
        var position = start
        while (position + 10 <= end) {
            if (bytes[position] == 0.toByte()) break
            val id = String(bytes, position, 4, Charsets.ISO_8859_1)
            if (!id.all { it.isLetterOrDigit() }) break
            val size = if (version == 4) {
                syncSafe(bytes, position + 4)
            } else {
                readIntBe(bytes, position + 4)
            }
            if (size <= 0 || position + 10 + size > end) break

            if (id == "USLT" || id == "ULT") {
                parseUslt(bytes, position + 10, position + 10 + size)?.let { return it }
            }
            // Some taggers put unsynchronised lyrics into a TXXX frame.
            if (id == "TXXX") {
                parseTxxxLyrics(bytes, position + 10, position + 10 + size)?.let { return it }
            }
            position += 10 + size
        }
        return null
    }

    private fun parseUslt(bytes: ByteArray, start: Int, end: Int): String? {
        if (start >= end) return null
        val encoding = bytes[start].toInt() and 0xFF
        var position = start + 1
        if (position + 3 > end) return null
        position += 3 // language code

        val wide = encoding == 1 || encoding == 2
        while (position < end) {
            if (wide) {
                if (position + 1 < end &&
                    bytes[position] == 0.toByte() &&
                    bytes[position + 1] == 0.toByte()
                ) {
                    position += 2
                    break
                }
                position += 2
            } else {
                if (bytes[position] == 0.toByte()) {
                    position += 1
                    break
                }
                position += 1
            }
        }
        if (position >= end) return null

        val charset = charsetForEncoding(encoding)
        return textOrNull(bytes, position, end, charset)
    }

    private fun parseTxxxLyrics(bytes: ByteArray, start: Int, end: Int): String? {
        if (start >= end) return null
        val encoding = bytes[start].toInt() and 0xFF
        val charset = charsetForEncoding(encoding)
        val wide = encoding == 1 || encoding == 2
        var position = start + 1

        // Read the description, then check whether it identifies a lyrics field.
        val descriptionStart = position
        while (position < end) {
            if (wide) {
                if (position + 1 < end &&
                    bytes[position] == 0.toByte() &&
                    bytes[position + 1] == 0.toByte()
                ) {
                    break
                }
                position += 2
            } else {
                if (bytes[position] == 0.toByte()) break
                position += 1
            }
        }
        val description = runCatching {
            String(bytes, descriptionStart, position - descriptionStart, charset)
        }.getOrNull().orEmpty()
        if (!description.contains("lyric", ignoreCase = true)) return null

        position += if (wide) 2 else 1
        if (position >= end) return null
        return textOrNull(bytes, position, end, charset)
    }

    // ── FLAC / Vorbis comments ───────────────────────────────────────────────

    private fun parseFlac(bytes: ByteArray): String? {
        if (bytes.size < 8) return null
        if (!startsWith(bytes, "fLaC")) return null

        var position = 4
        while (position + 4 <= bytes.size) {
            val header = bytes[position].toInt() and 0xFF
            val last = header and 0x80 != 0
            val type = header and 0x7F
            val length = readUInt24Be(bytes, position + 1)
            val dataStart = position + 4
            val dataEnd = dataStart + length
            if (length <= 0 || dataEnd > bytes.size) break

            if (type == 4) {
                parseVorbisComment(bytes, dataStart, dataEnd)?.let { return it }
            }
            position = dataEnd
            if (last) break
        }
        return null
    }

    private fun parseVorbisComment(bytes: ByteArray, start: Int, end: Int): String? {
        var position = start
        if (position + 4 > end) return null
        val vendorLength = readIntLe(bytes, position)
        position += 4
        if (vendorLength < 0 || position + vendorLength > end) return null
        position += vendorLength

        if (position + 4 > end) return null
        val fieldCount = readIntLe(bytes, position)
        position += 4
        if (fieldCount <= 0) return null

        var found: String? = null
        for (index in 0 until minOf(fieldCount, 1024)) {
            if (position + 4 > end) break
            val length = readIntLe(bytes, position)
            position += 4
            if (length < 0 || position + length > end) break
            val entry = runCatching {
                String(bytes, position, length, Charsets.UTF_8)
            }.getOrNull()
            position += length
            if (entry == null) continue

            val separator = entry.indexOf('=')
            if (separator <= 0) continue
            val key = entry.substring(0, separator).trim().uppercase(Locale.US)
            if (key in LYRICS_KEYS) {
                val value = entry.substring(separator + 1).trim()
                if (value.isNotEmpty()) found = value
            }
        }
        return found
    }

    // ── MP4 ©lyr atom ────────────────────────────────────────────────────────

    private fun parseMp4(bytes: ByteArray): String? {
        if (bytes.size < 32) return null
        for (index in 4 until bytes.size - 8) {
            val isCopyright = bytes[index] == 0xA9.toByte() &&
                bytes[index + 1] == 'l'.code.toByte() &&
                bytes[index + 2] == 'y'.code.toByte() &&
                bytes[index + 3] == 'r'.code.toByte()
            if (!isCopyright) continue

            val atomStart = index - 4
            val atomSize = readIntBe(bytes, atomStart)
            if (atomSize <= 8 || atomStart + atomSize > bytes.size) continue

            val dataStart = index + 4
            if (dataStart + 16 > bytes.size) continue
            val dataType = runCatching {
                String(bytes, dataStart + 4, 4, Charsets.ISO_8859_1)
            }.getOrNull()
            if (dataType != "data") continue

            val payloadStart = dataStart + 16
            val payloadEnd = minOf(bytes.size, atomStart + atomSize)
            if (payloadStart >= payloadEnd) continue
            val value = runCatching {
                String(bytes, payloadStart, payloadEnd - payloadStart, Charsets.UTF_8)
            }.getOrNull()
            if (!value.isNullOrBlank()) return value.trim('\u0000', '\uFEFF', ' ', '\t', '\r', '\n')
        }
        return null
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun charsetForEncoding(encoding: Int): Charset = when (encoding) {
        0 -> Charsets.ISO_8859_1
        1 -> Charsets.UTF_16
        2 -> Charsets.UTF_16BE
        3 -> Charsets.UTF_8
        else -> Charsets.ISO_8859_1
    }

    private fun textOrNull(bytes: ByteArray, start: Int, end: Int, charset: Charset): String? {
        if (start >= end) return null
        return runCatching {
            String(bytes, start, end - start, charset)
                .trim('\u0000', '\uFEFF', ' ', '\t', '\r', '\n')
                .ifBlank { null }
        }.getOrNull()
    }

    private fun syncSafe(bytes: ByteArray, offset: Int): Int {
        if (offset + 4 > bytes.size) return 0
        return ((bytes[offset].toInt() and 0x7F) shl 21) or
            ((bytes[offset + 1].toInt() and 0x7F) shl 14) or
            ((bytes[offset + 2].toInt() and 0x7F) shl 7) or
            (bytes[offset + 3].toInt() and 0x7F)
    }

    private fun readIntBe(bytes: ByteArray, offset: Int): Int {
        if (offset < 0 || offset + 4 > bytes.size) return 0
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
    }

    private fun readIntLe(bytes: ByteArray, offset: Int): Int {
        if (offset < 0 || offset + 4 > bytes.size) return 0
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun readUInt24Be(bytes: ByteArray, offset: Int): Int {
        if (offset < 0 || offset + 3 > bytes.size) return 0
        return ((bytes[offset].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            (bytes[offset + 2].toInt() and 0xFF)
    }

    private fun startsWith(bytes: ByteArray, value: String): Boolean {
        if (bytes.size < value.length) return false
        return value.indices.all { bytes[it] == value[it].code.toByte() }
    }

    private val LYRICS_KEYS = setOf("LYRICS", "LYRIC", "UNSYNCEDLYRICS", "SYNCEDLYRICS")
}
