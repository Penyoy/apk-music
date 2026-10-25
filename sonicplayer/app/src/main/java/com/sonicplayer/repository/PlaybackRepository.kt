package com.sonicplayer.repository

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.sonicplayer.data.dao.QueueDao
import com.sonicplayer.data.dao.SongDao
import com.sonicplayer.data.model.MusicQueue
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.data.model.Song
import com.sonicplayer.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the media controller and the persisted queues.
 *
 * The database is the source of truth for queue contents. The media controller is
 * kept in sync for the active queue only; other queues stay dormant until switched.
 */
@OptIn(ExperimentalCoroutinesApi::class)
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

    private val _queues = MutableStateFlow<List<MusicQueue>>(emptyList())
    val queues: StateFlow<List<MusicQueue>> = _queues.asStateFlow()

    private val _activeQueue = MutableStateFlow<MusicQueue?>(null)
    val activeQueue: StateFlow<MusicQueue?> = _activeQueue.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val queueMutex = Mutex()

    /** Queue currently copied into the media controller, if any. */
    private var loadedQueueId: Long? = null
    private var suppressPositionPersistence = false

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
            queueDao.observeQueues().collect { queues ->
                _queues.value = queues
                val active = queues.firstOrNull { it.active }
                if (active != null) {
                    _activeQueue.value = active
                    if (loadedQueueId == null) {
                        queueMutex.withLock { restoreActiveQueueLocked() }
                    }
                }
            }
        }

        scope.launch {
            queueDao.observeActiveQueue()
                .flatMapLatest { queue ->
                    if (queue == null) flowOf(emptyList()) else queueDao.observeQueueSongs(queue.id)
                }
                .collect { songs -> _currentPlaylist.value = songs }
        }

        scope.launch {
            queueMutex.withLock { ensureActiveQueueLocked() }
        }
    }

    private fun attachController(controller: MediaController) {
        if (mediaController === controller) return
        mediaController = controller
        setupMediaControllerListener(controller)
        startPositionUpdates()
        updatePlaybackState()
        scope.launch {
            queueMutex.withLock { restoreActiveQueueLocked() }
        }
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
                if (!isPlaying) persistCurrentQueuePosition()
                updatePlaybackState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                persistCurrentQueuePosition()
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
        val currentMediaItem = runCatching { controller.currentMediaItem }.getOrNull()

        scope.launch {
            val currentSong = currentMediaItem?.mediaId?.toLongOrNull()?.let { songId ->
                runCatching { songDao.getSongById(songId) }.getOrNull()
            }

            runCatching {
                _playbackState.update { state ->
                    state.copy(
                        isPlaying = controller.isPlaying,
                        currentSong = currentSong ?: if (currentMediaItem == null) null else state.currentSong,
                        currentIndex = controller.currentMediaItemIndex.coerceAtLeast(0),
                        position = controller.currentPosition.coerceAtLeast(0),
                        duration = controller.duration.coerceAtLeast(0),
                        repeatMode = when (controller.repeatMode) {
                            Player.REPEAT_MODE_ALL -> PlaybackState.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ONE -> PlaybackState.REPEAT_MODE_ONE
                            else -> PlaybackState.REPEAT_MODE_OFF
                        },
                        shuffleMode = controller.shuffleModeEnabled,
                        isBuffering = controller.playbackState == Player.STATE_BUFFERING
                    )
                }
            }
        }
    }

    private fun startPositionUpdates() {
        if (positionUpdatesStarted) return
        positionUpdatesStarted = true
        scope.launch {
            var tick = 0
            while (isActive) {
                val controller = mediaController
                if (controller != null && controller.isPlaying) {
                    _playbackState.update { state ->
                        state.copy(
                            position = controller.currentPosition.coerceAtLeast(0),
                            duration = controller.duration.coerceAtLeast(0),
                            currentIndex = controller.currentMediaItemIndex.coerceAtLeast(0)
                        )
                    }
                    tick++
                    if (tick % 10 == 0) persistCurrentQueuePosition()
                }
                delay(500)
            }
        }
    }

    // ---------------------------------------------------------------------
    // Queue bootstrapping and synchronisation
    // ---------------------------------------------------------------------

    private suspend fun ensureActiveQueueLocked(): MusicQueue {
        queueDao.getActiveQueue()?.let { return it }

        val existing = queueDao.getQueues().firstOrNull()
        if (existing != null) {
            queueDao.activateQueue(existing.id)
            return existing.copy(active = true)
        }

        val now = System.currentTimeMillis()
        val id = queueDao.createQueue(
            name = "Antrean",
            activate = true,
            timestamp = now
        )
        val created = queueDao.getQueue(id)
            ?: MusicQueue(id = id, name = "Antrean", active = true, createdAt = now, updatedAt = now)
        _activeQueue.value = created
        return created
    }

    /**
     * Copies the active queue into the controller when the service does not already
     * own a queue (cold start). A live service keeps playing and its position wins.
     */
    private suspend fun restoreActiveQueueLocked() {
        val queue = ensureActiveQueueLocked()
        _activeQueue.value = queue
        val controller = awaitController() ?: return

        if (controller.mediaItemCount > 0) {
            loadedQueueId = queue.id
            persistCurrentQueuePositionLocked()
            updatePlaybackState()
            return
        }

        val songs = queueDao.getQueueSongs(queue.id)
        _currentPlaylist.value = songs
        if (songs.isEmpty()) {
            loadedQueueId = queue.id
            return
        }

        val start = queue.lastPosition.coerceIn(0, songs.lastIndex)
        suppressPositionPersistence = true
        runCatching {
            controller.setMediaItems(songs.map { it.toMediaItem() }, start, 0L)
            controller.prepare()
        }
        suppressPositionPersistence = false
        loadedQueueId = queue.id
        updatePlaybackState()
    }

    private suspend fun applyQueueLocked(queue: MusicQueue, autoPlay: Boolean) {
        val songs = queueDao.getQueueSongs(queue.id)
        _currentPlaylist.value = songs
        _activeQueue.value = queue
        loadedQueueId = queue.id

        val controller = awaitController() ?: return
        val start = if (songs.isEmpty()) 0 else queue.lastPosition.coerceIn(0, songs.lastIndex)

        suppressPositionPersistence = true
        runCatching {
            if (controller.isPlaying) controller.pause()
            controller.stop()
            controller.clearMediaItems()
            if (songs.isNotEmpty()) {
                controller.setMediaItems(songs.map { it.toMediaItem() }, start, 0L)
                controller.prepare()
                if (autoPlay) controller.play()
            }
        }
        suppressPositionPersistence = false

        if (songs.isEmpty()) {
            _playbackState.update {
                it.copy(currentSong = null, currentIndex = 0, position = 0, duration = 0, isPlaying = false)
            }
        } else {
            updatePlaybackState()
        }
        persistCurrentQueuePositionLocked()
    }

    private fun persistCurrentQueuePosition() {
        if (suppressPositionPersistence) return
        val queueId = loadedQueueId ?: return
        val controller = mediaController ?: return
        val index = controller.currentMediaItemIndex.coerceAtLeast(0)
        scope.launch {
            runCatching {
                queueDao.setLastPosition(queueId, index, System.currentTimeMillis())
            }
        }
    }

    private suspend fun persistCurrentQueuePositionLocked() {
        if (suppressPositionPersistence) return
        val queueId = loadedQueueId ?: return
        val controller = mediaController ?: return
        val index = controller.currentMediaItemIndex.coerceAtLeast(0)
        runCatching {
            queueDao.setLastPosition(queueId, index, System.currentTimeMillis())
        }
    }

    // ---------------------------------------------------------------------
    // Playback
    // ---------------------------------------------------------------------

    fun playSong(song: Song) {
        playSongs(listOf(song), 0)
    }

    /** Replaces the active queue with [songs] and starts playback at [startIndex]. */
    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        scope.launch {
            queueMutex.withLock {
                persistCurrentQueuePositionLocked()
                val queue = ensureActiveQueueLocked()
                val index = startIndex.coerceIn(0, songs.lastIndex)
                val now = System.currentTimeMillis()
                queueDao.replaceQueueItems(queue.id, songs.map { it.id }, index, now)

                val updated = queue.copy(lastPosition = index, updatedAt = now)
                _activeQueue.value = updated
                _currentPlaylist.value = songs
                loadedQueueId = queue.id

                val controller = awaitController() ?: return@withLock
                suppressPositionPersistence = true
                runCatching {
                    controller.stop()
                    controller.clearMediaItems()
                    controller.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
                    controller.prepare()
                    controller.play()
                }
                suppressPositionPersistence = false
                updatePlaybackState()
                persistCurrentQueuePositionLocked()
            }
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
                    .setArtworkUri(artworkUri)
                    .build()
            )
            .build()
    }

    fun playPause() {
        val controller = mediaController ?: return
        runCatching {
            if (controller.isPlaying) {
                controller.pause()
                persistCurrentQueuePosition()
            } else {
                if (controller.playbackState == Player.STATE_IDLE) {
                    controller.prepare()
                }
                controller.play()
            }
        }
    }

    fun play() {
        val controller = mediaController ?: return
        runCatching {
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        }
    }

    fun pause() {
        runCatching { mediaController?.pause() }
        persistCurrentQueuePosition()
    }

    fun next() {
        runCatching { mediaController?.seekToNextMediaItem() }
    }

    fun previous() {
        runCatching { mediaController?.seekToPreviousMediaItem() }
    }

    fun seekTo(position: Long) {
        val controller = mediaController ?: return
        val safePosition = position.coerceAtLeast(0L)
        runCatching { controller.seekTo(safePosition) }
        _playbackState.update { it.copy(position = safePosition) }
    }

    fun seekBy(delta: Long) {
        val controller = mediaController ?: return
        val duration = controller.duration.coerceAtLeast(0L)
        val target = (controller.currentPosition + delta).coerceIn(0L, duration)
        seekTo(target)
    }

    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        runCatching {
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                else -> Player.REPEAT_MODE_OFF
            }
        }
        updatePlaybackState()
    }

    fun toggleShuffleMode() {
        val controller = mediaController ?: return
        runCatching { controller.shuffleModeEnabled = !controller.shuffleModeEnabled }
        updatePlaybackState()
    }

    fun setPlaybackSpeed(speed: Float) {
        val controller = mediaController ?: return
        runCatching {
            controller.playbackParameters = PlaybackParameters(
                speed.coerceIn(0.25f, 4f),
                controller.playbackParameters.pitch
            )
        }
    }

    fun setPitch(pitch: Float) {
        val controller = mediaController ?: return
        runCatching {
            controller.playbackParameters = PlaybackParameters(
                controller.playbackParameters.speed,
                pitch.coerceIn(0.5f, 2f)
            )
        }
    }

    fun seekToQueueItem(index: Int) {
        scope.launch {
            queueMutex.withLock {
                val queue = ensureActiveQueueLocked()
                val controller = awaitController()
                if (loadedQueueId != queue.id || controller == null || controller.mediaItemCount == 0) {
                    applyQueueLocked(queue, autoPlay = false)
                }
                val target = index.coerceAtLeast(0)
                runCatching {
                    controller?.seekToDefaultPosition(target)
                    controller?.play()
                }
                queueDao.setLastPosition(queue.id, target, System.currentTimeMillis())
            }
        }
    }

    // ---------------------------------------------------------------------
    // Queue management
    // ---------------------------------------------------------------------

    fun createQueue(
        name: String,
        songIds: List<Long> = emptyList(),
        activate: Boolean = true,
        onCreated: ((Long) -> Unit)? = null
    ) {
        scope.launch {
            queueMutex.withLock {
                val now = System.currentTimeMillis()
                val cleanName = name.trim().ifBlank { "Antrean ${queueDao.getQueues().size + 1}" }
                val wasPlaying = mediaController?.isPlaying == true
                if (activate) persistCurrentQueuePositionLocked()

                val id = queueDao.createQueue(
                    name = cleanName,
                    songIds = songIds,
                    activate = activate,
                    timestamp = now
                )

                if (activate) {
                    queueDao.getQueue(id)?.let { queue ->
                        applyQueueLocked(queue, autoPlay = wasPlaying && songIds.isNotEmpty())
                    }
                }
                onCreated?.invoke(id)
            }
        }
    }

    fun switchQueue(queueId: Long) {
        scope.launch {
            queueMutex.withLock {
                val queue = queueDao.getQueue(queueId) ?: return@withLock
                if (loadedQueueId == queue.id) {
                    queueDao.activateQueue(queue.id)
                    _activeQueue.value = queue.copy(active = true)
                    return@withLock
                }

                val wasPlaying = mediaController?.isPlaying == true
                persistCurrentQueuePositionLocked()
                queueDao.activateQueue(queue.id)
                applyQueueLocked(queue.copy(active = true), autoPlay = wasPlaying)
            }
        }
    }

    fun renameQueue(queueId: Long, name: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        scope.launch {
            queueDao.renameQueue(queueId, cleanName, System.currentTimeMillis())
        }
    }

    fun deleteQueue(queueId: Long) {
        scope.launch {
            queueMutex.withLock {
                val queues = queueDao.getQueues()
                if (queues.size <= 1) return@withLock
                val removed = queues.firstOrNull { it.id == queueId } ?: return@withLock
                val wasPlaying = mediaController?.isPlaying == true

                if (removed.active) persistCurrentQueuePositionLocked()
                queueDao.deleteQueue(queueId)

                if (removed.active) {
                    val next = queueDao.getQueues().firstOrNull() ?: return@withLock
                    queueDao.activateQueue(next.id)
                    applyQueueLocked(next.copy(active = true), autoPlay = wasPlaying)
                }
            }
        }
    }

    /** Appends a song to the given queue, or to the active queue when null. */
    fun addToQueue(song: Song, queueId: Long? = null) {
        scope.launch {
            queueMutex.withLock {
                val queue = if (queueId != null) {
                    queueDao.getQueue(queueId) ?: return@withLock
                } else {
                    ensureActiveQueueLocked()
                }
                queueDao.addSongToQueue(queue.id, song.id, System.currentTimeMillis())

                val controller = awaitController()
                if (queue.active) {
                    if (loadedQueueId == queue.id && controller != null) {
                        runCatching { controller.addMediaItem(song.toMediaItem()) }
                    } else {
                        applyQueueLocked(queue, autoPlay = false)
                    }
                }
            }
        }
    }

    /** Inserts a song directly after the current track of the active queue. */
    fun addNext(song: Song) {
        scope.launch {
            queueMutex.withLock {
                val queue = ensureActiveQueueLocked()
                val controller = awaitController()
                val loaded = loadedQueueId == queue.id && controller != null
                val after = if (loaded) controller.currentMediaItemIndex.coerceAtLeast(0) else queue.lastPosition

                queueDao.addNextToQueue(
                    queueId = queue.id,
                    songId = song.id,
                    afterPosition = after,
                    timestamp = System.currentTimeMillis()
                )

                if (loaded) {
                    runCatching {
                        val target = (after + 1).coerceIn(0, controller.mediaItemCount)
                        controller.addMediaItem(target, song.toMediaItem())
                    }
                    persistCurrentQueuePositionLocked()
                } else {
                    applyQueueLocked(queue, autoPlay = false)
                }
            }
        }
    }

    fun removeQueueItem(position: Int) {
        scope.launch {
            queueMutex.withLock {
                val queue = ensureActiveQueueLocked()
                val controller = mediaController
                val countBefore = queueDao.getItemCount(queue.id)
                val loaded = loadedQueueId == queue.id &&
                    controller != null &&
                    controller.mediaItemCount == countBefore

                queueDao.removeItemAt(queue.id, position, System.currentTimeMillis())

                if (loaded) {
                    suppressPositionPersistence = true
                    runCatching {
                        if (position in 0 until controller.mediaItemCount) {
                            controller.removeMediaItem(position)
                        }
                    }
                    suppressPositionPersistence = false
                    updatePlaybackState()
                    persistCurrentQueuePositionLocked()
                } else {
                    val countAfter = queueDao.getItemCount(queue.id)
                    val newPosition = when {
                        position < queue.lastPosition -> queue.lastPosition - 1
                        position == queue.lastPosition -> position.coerceAtMost((countAfter - 1).coerceAtLeast(0))
                        else -> queue.lastPosition
                    }
                    queueDao.setLastPosition(queue.id, newPosition, System.currentTimeMillis())
                }
            }
        }
    }

    fun moveQueueItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        scope.launch {
            queueMutex.withLock {
                val queue = ensureActiveQueueLocked()
                val controller = mediaController
                val count = queueDao.getItemCount(queue.id)
                val loaded = loadedQueueId == queue.id &&
                    controller != null &&
                    controller.mediaItemCount == count

                queueDao.moveItem(queue.id, fromPosition, toPosition, System.currentTimeMillis())

                if (loaded) {
                    suppressPositionPersistence = true
                    runCatching { controller.moveMediaItem(fromPosition, toPosition) }
                    suppressPositionPersistence = false
                    updatePlaybackState()
                    persistCurrentQueuePositionLocked()
                } else {
                    val newPosition = when {
                        queue.lastPosition == fromPosition -> toPosition
                        fromPosition < queue.lastPosition && toPosition >= queue.lastPosition ->
                            queue.lastPosition - 1
                        fromPosition > queue.lastPosition && toPosition <= queue.lastPosition ->
                            queue.lastPosition + 1
                        else -> queue.lastPosition
                    }
                    queueDao.setLastPosition(queue.id, newPosition, System.currentTimeMillis())
                }
            }
        }
    }

    fun clearActiveQueue() {
        scope.launch {
            queueMutex.withLock {
                val queue = ensureActiveQueueLocked()
                queueDao.clearItems(queue.id)
                queueDao.setLastPosition(queue.id, 0, System.currentTimeMillis())
                _currentPlaylist.value = emptyList()
                loadedQueueId = queue.id
                suppressPositionPersistence = true
                runCatching {
                    mediaController?.stop()
                    mediaController?.clearMediaItems()
                }
                suppressPositionPersistence = false
                _playbackState.update {
                    it.copy(currentSong = null, currentIndex = 0, position = 0, duration = 0, isPlaying = false)
                }
            }
        }
    }

    fun release() {
        runCatching { scope.cancel() }
        runCatching { MediaController.releaseFuture(controllerFuture) }
        mediaController = null
    }
}
