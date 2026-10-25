package com.sonicplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonicplayer.data.model.Album
import com.sonicplayer.data.model.Artist
import com.sonicplayer.data.model.Lyrics
import com.sonicplayer.data.model.MusicFolder
import com.sonicplayer.data.model.MusicQueue
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.data.model.Playlist
import com.sonicplayer.data.model.Song
import com.sonicplayer.repository.AudioRepository
import com.sonicplayer.repository.LyricsRepository
import com.sonicplayer.repository.PlaybackRepository
import com.sonicplayer.repository.PlaylistRepository
import com.sonicplayer.util.SleepTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val audioRepository: AudioRepository,
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository,
    private val lyricsRepository: LyricsRepository
) : AndroidViewModel(application) {

    // Library
    val songs: StateFlow<List<Song>> = audioRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums: StateFlow<List<Album>> = audioRepository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists: StateFlow<List<Artist>> = audioRepository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = audioRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteIds: StateFlow<Set<Long>> = favoriteSongs
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val recentlyPlayed: StateFlow<List<Song>> = audioRepository.getRecentlyPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentlyAdded: StateFlow<List<Song>> = audioRepository.getRecentlyAddedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = audioRepository.getMostPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notPlayed: StateFlow<List<Song>> = songs
        .map { list -> list.filter { it.lastPlayed == 0L }.sortedBy { it.title.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val folders: StateFlow<List<MusicFolder>> = songs
        .map { list ->
            list.groupBy { song -> song.path.substringBeforeLast('/', "") }
                .filterKeys { path -> path.isNotBlank() }
                .map { (path, tracks) ->
                    MusicFolder(
                        path = path,
                        name = path.substringAfterLast('/').ifBlank { path },
                        songCount = tracks.size
                    )
                }
                .sortedBy { folder -> folder.name.lowercase() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Playback
    val playbackState: StateFlow<PlaybackState> = playbackRepository.playbackState
    val currentPlaylist: StateFlow<List<Song>> = playbackRepository.currentPlaylist
    val queues: StateFlow<List<MusicQueue>> = playbackRepository.queues
    val activeQueue: StateFlow<MusicQueue?> = playbackRepository.activeQueue

    /** Lyrics for the current track, loaded lazily when the track changes. */
    val currentLyrics: StateFlow<Lyrics?> = playbackState
        .map { it.currentSong }
        .distinctUntilChanged { old, new -> old?.id == new?.id }
        .mapLatest { song -> song?.let { lyricsRepository.getLyrics(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sleepTimerManager = SleepTimerManager()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _searchQuery
        .debounce(280)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList()) else audioRepository.searchSongs(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Selected tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Sorting
    private val _sortBy = MutableStateFlow(SortOption.TITLE)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()

    val sortedSongs: StateFlow<List<Song>> = combine(songs, _sortBy) { songList, sort ->
        when (sort) {
            SortOption.TITLE -> songList.sortedBy { it.title.lowercase() }
            SortOption.ARTIST -> songList.sortedBy { it.artist.lowercase() }
            SortOption.ALBUM -> songList.sortedBy { it.album.lowercase() }
            SortOption.DURATION -> songList.sortedBy { it.duration }
            SortOption.DATE_ADDED -> songList.sortedByDescending { it.dateAdded }
            SortOption.DATE_PLAYED -> songList.sortedByDescending { it.lastPlayed }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                audioRepository.refreshLibrary()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSortOption(option: SortOption) {
        _sortBy.value = option
    }

    // Library helpers used by detail screens
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> =
        audioRepository.getSongsByAlbum(albumId)

    fun getSongsByArtist(artist: String): Flow<List<Song>> =
        audioRepository.getSongsByArtist(artist)

    fun getSongsInFolder(folderPath: String): Flow<List<Song>> =
        audioRepository.getSongsInFolder("$folderPath/")

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistRepository.getSongsInPlaylist(playlistId)

    // Playback
    fun playSong(song: Song) {
        playbackRepository.playSong(song)
        viewModelScope.launch { audioRepository.recordPlay(song.id) }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        playbackRepository.playSongs(songs, startIndex)
        viewModelScope.launch {
            songs.getOrNull(startIndex)?.let { audioRepository.recordPlay(it.id) }
        }
    }

    fun playPause() = playbackRepository.playPause()
    fun pause() = playbackRepository.pause()
    fun next() = playbackRepository.next()
    fun previous() = playbackRepository.previous()
    fun seekTo(position: Long) = playbackRepository.seekTo(position)
    fun seekBy(delta: Long) = playbackRepository.seekBy(delta)
    fun toggleRepeatMode() = playbackRepository.toggleRepeatMode()
    fun toggleShuffleMode() = playbackRepository.toggleShuffleMode()
    fun setPlaybackSpeed(speed: Float) = playbackRepository.setPlaybackSpeed(speed)
    fun addToQueue(song: Song, queueId: Long? = null) =
        playbackRepository.addToQueue(song, queueId)
    fun addNext(song: Song) = playbackRepository.addNext(song)

    // Queues
    fun createQueue(
        name: String,
        songIds: List<Long> = emptyList(),
        onCreated: ((Long) -> Unit)? = null
    ) = playbackRepository.createQueue(name, songIds, onCreated = onCreated)

    fun switchQueue(queueId: Long) = playbackRepository.switchQueue(queueId)
    fun renameQueue(queueId: Long, name: String) =
        playbackRepository.renameQueue(queueId, name)
    fun deleteQueue(queueId: Long) = playbackRepository.deleteQueue(queueId)
    fun clearActiveQueue() = playbackRepository.clearActiveQueue()
    fun removeQueueItem(position: Int) = playbackRepository.removeQueueItem(position)
    fun moveQueueItem(fromPosition: Int, toPosition: Int) =
        playbackRepository.moveQueueItem(fromPosition, toPosition)

    fun playQueueItem(position: Int) {
        playbackRepository.seekToQueueItem(position)
        currentPlaylist.value.getOrNull(position)?.let { song ->
            viewModelScope.launch { audioRepository.recordPlay(song.id) }
        }
    }

    // Favorites
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { audioRepository.toggleFavorite(songId) }
    }

    // Playlists
    fun createPlaylist(name: String, onCreated: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = playlistRepository.createPlaylist(name)
            onCreated?.invoke(id)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch { playlistRepository.deletePlaylist(id) }
    }

    fun addToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { playlistRepository.addSongToPlaylist(playlistId, songId) }
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { playlistRepository.removeSongFromPlaylist(playlistId, songId) }
    }

    enum class SortOption {
        TITLE, ARTIST, ALBUM, DURATION, DATE_ADDED, DATE_PLAYED
    }
}
