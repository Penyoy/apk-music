package com.sonicplayer.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sonicplayer.MainActivity
import com.sonicplayer.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the ExoPlayer instance and the MediaSession.
 *
 * The notification is produced by Media3's [DefaultMediaNotificationProvider]. It reads
 * the artwork URI from the current [androidx.media3.common.MediaMetadata], so the large
 * icon (thumbnail) is always shown, including the bundled placeholder when a track has
 * no cover. Keeping the notification in Media3 also avoids duplicate foreground
 * notifications, which was a source of crashes on newer Android versions.
 */
@AndroidEntryPoint
@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .setChannelName(R.string.notification_channel_name)
            .setNotificationId(NOTIFICATION_ID)
            .build()
        notificationProvider.setSmallIcon(R.drawable.ic_notification)
        setMediaNotificationProvider(notificationProvider)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        runCatching {
            val player = mediaSession?.player
            if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
                stopSelf()
            }
        }.onFailure { stopSelf() }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        runCatching {
            mediaSession?.run {
                player.release()
                release()
            }
        }
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "sonic_player_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
