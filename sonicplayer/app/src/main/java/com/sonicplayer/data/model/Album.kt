package com.sonicplayer.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long,
    val songCount: Int,
    val year: Int,
    val duration: Long,
    val coverUri: Uri? = null
) : Parcelable {
    val displayTitle: String
        get() = title.ifBlank { "Unknown Album" }
    
    val displayArtist: String
        get() = artist.ifBlank { "Unknown Artist" }
    
    val durationText: String
        get() = Song.formatDuration(duration)
}
