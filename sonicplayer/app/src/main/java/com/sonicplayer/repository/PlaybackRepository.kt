package com.sonicplayer.repository

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.RepeatMode
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.sonicplayer.data.model.Song
import com.sonicplayer.data.model.QueueItem
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.data.dao.QueueDao
import com.sonicplayer.data.dao.SongDao
import com.sonicplayer.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            setupMediaControllerListener()
            startPositionUpdates()
        }, MoreExecutors.directExecutor())

        scope.launch {
            queueDao.getQueueSongs().collect { songs ->
                _currentPlaylist.value = songs
            }
        }
    }

    private fun setupMediaControllerListener() {
        mediaController?.addListener(object : Player.Listener {
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
                    currentSong = currentSong,
                    position = controller.currentPosition,
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
        scope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    _playbackState.update { state ->
                        state.copy(
                            position = controller.currentPosition,
                            duration = controller.duration.coerceAtLeast(0)
                        )
                    }
                }
                delay(500)
            }
        }
    }

    fun playSong(song: Song) {
        scope.launch {
            queueDao.setQueue(listOf(song.id), 0)
            playMediaItem(song)
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        scope.launch {
            queueDao.setQueue(songs.map { it.id }, startIndex)
            val controller = mediaController ?: return@launch
            
            controller.clearMediaItems()
            songs.forEach { song ->
                val mediaItem = MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(song.uri)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(song.displayTitle)
                            .setArtist(song.displayArtist)
                            .setAlbumTitle(song.displayAlbum)
                            .setArtworkUri(AudioRepository.getSongUri(song.id))
                            .build()
                    )
                    .build()
                controller.addMediaItem(mediaItem)
            }
            controller.seekToDefaultPosition(startIndex)
            controller.prepare()
            controller.play()
        }
    }

    private fun playMediaItem(song: Song) {
        val controller = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(song.displayTitle)
                    .setArtist(song.displayArtist)
                    .setAlbumTitle(song.displayAlbum)
                    .setArtworkUri(AudioRepository.getSongUri(song.id))
                    .build()
            )
            .build()
        
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
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
    }

    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        controller.repeatMode = when (controller.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
            else -> RepeatMode.OFF
        }
    }

    fun toggleShuffleMode() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
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
            val controller = mediaController ?: return@launch
            val mediaItem = MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.displayTitle)
                        .setArtist(song.displayArtist)
                        .build()
                )
                .build()
            controller.addMediaItem(mediaItem)
        }
    }

    fun addNext(song: Song) {
        scope.launch {
            queueDao.addNext(song.id)
            val controller = mediaController ?: return@launch
            val mediaItem = MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.displayTitle)
                        .setArtist(song.displayArtist)
                        .build()
                )
                .build()
            val nextIndex = controller.currentMediaItemIndex + 1
            controller.addMediaItem(nextIndex, mediaItem)
        }
    }

    fun release() {
        scope.cancel()
        MediaController.releaseFuture(controllerFuture)
    }
}
