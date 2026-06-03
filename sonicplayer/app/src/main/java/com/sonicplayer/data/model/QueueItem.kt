package com.sonicplayer.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "queue")
data class QueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val position: Int = 0,
    val isCurrent: Boolean = false
) : Parcelable

@Parcelize
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
@Entity(tableName = "recently_played")
data class RecentlyPlayed(
    @PrimaryKey
    val songId: Long,
    val playedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 1
) : Parcelable

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val position: Long = 0,
    val duration: Long = 0,
    val repeatMode: Int = REPEAT_MODE_OFF,
    val shuffleMode: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val isBuffering: Boolean = false
) {
    companion object {
        const val REPEAT_MODE_OFF = 0
        const val REPEAT_MODE_ALL = 1
        const val REPEAT_MODE_ONE = 2
    }
    
    val progress: Float
        get() = if (duration > 0) position.toFloat() / duration else 0f
    
    val positionText: String
        get() = Song.formatDuration(position)
    
    val durationText: String
        get() = Song.formatDuration(duration)
}

data class EqualizerPreset(
    val name: String,
    val bandValues: List<Float>
)
