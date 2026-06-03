package com.sonicplayer.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int,
    val duration: Long
) : Parcelable {
    val displayName: String
        get() = name.ifBlank { "Unknown Artist" }
    
    val durationText: String
        get() = Song.formatDuration(duration)
}
