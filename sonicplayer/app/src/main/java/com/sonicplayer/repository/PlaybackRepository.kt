package com.sonicplayer.repository

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.RepeatMode
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.sonicplayer.data.dao.QueueDao
import com.sonicplayer.data.dao.SongDao
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.data.model.Song
import com.sonicplayer.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val queueDao: QueueDao,
    private val songDao: SongDao
) {
    private var mediaController: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>
    private var positionUpdatesStarted = false

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            runCatching { controllerFuture.get() }.getOrNull()?.let { attachController(it) }
        }, ContextCompat.getMainExecutor(context))

        scope.launch {
            queueDao.getQueueSongs().collect { songs ->
                _currentPlaylist.value = songs
            }
        }
    }

    private fun attachController(controller: MediaController) {
        if (mediaController === controller) return
        mediaController = controller
        setupMediaControllerListener(controller)
        startPositionUpdates()
        updatePlaybackState()
    }

    private suspend fun awaitController(): MediaController? {
        mediaController?.let { return it }
        val controller = withContext(Dispatchers.IO) {
            runCatching { controllerFuture.get() }.getOrNull()
        } ?: return null
        attachController(controller)
        return controller
    }

    private fun setupMediaControllerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlaybackState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updatePlaybackState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updatePlaybackState()
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                _playbackState.update { state ->
                    state.copy(
                        playbackSpeed = playbackParameters.speed,
                        pitch = playbackParameters.pitch
                    )
                }
            }
        })
    }

    private fun updatePlaybackState() {
        val controller = mediaController ?: return
        val currentMediaItem = controller.currentMediaItem

        scope.launch {
            val currentSong = currentMediaItem?.mediaId?.toLongOrNull()?.let {
                songDao.getSongById(it)
            }

            _playbackState.update { state ->
                state.copy(
                    isPlaying = controller.isPlaying,
                    currentSong = currentSong ?: if (currentMediaItem == null) null else state.currentSong,
                    position = controller.currentPosition.coerceAtLeast(0),
                    duration = controller.duration.coerceAtLeast(0),
                    repeatMode = when (controller.repeatMode) {
                        RepeatMode.ALL -> PlaybackState.REPEAT_MODE_ALL
                        RepeatMode.ONE -> PlaybackState.REPEAT_MODE_ONE
                        else -> PlaybackState.REPEAT_MODE_OFF
                    },
                    shuffleMode = controller.shuffleModeEnabled,
                    isBuffering = controller.playbackState == Player.STATE_BUFFERING
                )
            }
        }
    }

    private fun startPositionUpdates() {
        if (positionUpdatesStarted) return
        positionUpdatesStarted = true
        scope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    _playbackState.update { state ->
                        state.copy(
                            position = controller.currentPosition.coerceAtLeast(0),
                            duration = controller.duration.coerceAtLeast(0)
                        )
                    }
                }
                delay(500)
            }
        }
    }

    fun playSong(song: Song) {
        playSongs(listOf(song), 0)
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        scope.launch {
            queueDao.setQueue(songs.map { it.id }, startIndex)
            val controller = awaitController() ?: return@launch

            controller.clearMediaItems()
            songs.forEach { song ->
                controller.addMediaItem(song.toMediaItem())
            }
            controller.seekToDefaultPosition(startIndex.coerceIn(0, songs.lastIndex))
            controller.prepare()
            controller.play()
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(displayTitle)
                    .setArtist(displayArtist)
                    .setAlbumTitle(displayAlbum)
                    .setArtworkUri(AudioRepository.getAlbumArtUri(albumId))
                    .build()
            )
            .build()
    }

    fun playPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        }
    }

    fun play() {
        val controller = mediaController ?: return
        if (controller.playbackState == Player.STATE_IDLE) {
            controller.prepare()
        }
        controller.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun next() {
        mediaController?.seekToNextMediaItem()
    }

    fun previous() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _playbackState.update { it.copy(position = position.coerceAtLeast(0)) }
    }

    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        controller.repeatMode = when (controller.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
            else -> RepeatMode.OFF
        }
        updatePlaybackState()
    }

    fun toggleShuffleMode() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        updatePlaybackState()
    }

    fun setPlaybackSpeed(speed: Float) {
        val controller = mediaController ?: return
        controller.playbackParameters = PlaybackParameters(speed, controller.playbackParameters.pitch)
    }

    fun setPitch(pitch: Float) {
        val controller = mediaController ?: return
        controller.playbackParameters = PlaybackParameters(controller.playbackParameters.speed, pitch)
    }

    fun addToQueue(song: Song) {
        scope.launch {
            queueDao.addToQueue(song.id)
            val controller = awaitController() ?: return@launch
            controller.addMediaItem(song.toMediaItem())
        }
    }

    fun addNext(song: Song) {
        scope.launch {
            queueDao.addNext(song.id)
            val controller = awaitController() ?: return@launch
            val nextIndex = (controller.currentMediaItemIndex + 1)
                .coerceIn(0, controller.mediaItemCount)
            controller.addMediaItem(nextIndex, song.toMediaItem())
        }
    }

    fun release() {
        scope.cancel()
        MediaController.releaseFuture(controllerFuture)
    }
}
