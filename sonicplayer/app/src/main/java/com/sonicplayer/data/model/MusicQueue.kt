package com.sonicplayer.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * A named playback queue. Musicolet-style multiple queues let the user keep
 * several playlists/albums ready without losing their place in another one.
 */
@Parcelize
@Entity(tableName = "queues")
data class MusicQueue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val active: Boolean = false,
    val lastPosition: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    val displayName: String
        get() = name.ifBlank { "Antrean" }
}
