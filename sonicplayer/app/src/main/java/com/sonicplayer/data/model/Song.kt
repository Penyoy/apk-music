package com.sonicplayer.data.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val trackNumber: Int,
    val dateAdded: Long,
    val dateModified: Long,
    val path: String,
    val uri: String,
    val year: Int,
    val genre: String,
    val bitrate: Int,
    val sampleRate: Int,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val datePlayed: Long = 0
) : Parcelable {
    val contentUri: Uri
        get() = Uri.parse(uri)
    
    val displayTitle: String
        get() = title.ifBlank { "Unknown Title" }
    
    val displayArtist: String
        get() = artist.ifBlank { "Unknown Artist" }
    
    val displayAlbum: String
        get() = album.ifBlank { "Unknown Album" }
    
    val durationText: String
        get() = formatDuration(duration)
    
    companion object {
        fun formatDuration(durationMs: Long): String {
            val seconds = (durationMs / 1000) % 60
            val minutes = (durationMs / 1000 / 60) % 60
            val hours = durationMs / 1000 / 60 / 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
    }
}
