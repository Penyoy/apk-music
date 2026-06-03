package com.sonicplayer.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sonicplayer.data.model.Song
import com.sonicplayer.data.model.Album
import com.sonicplayer.data.model.Artist
import com.sonicplayer.data.model.Playlist
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.repository.AudioRepository
import com.sonicplayer.repository.PlaybackRepository
import com.sonicplayer.repository.PlaylistRepository
import com.sonicplayer.util.SleepTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val audioRepository: AudioRepository,
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository
) : AndroidViewModel(application) {

    // Library data
    val songs: StateFlow<List<Song>> = audioRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = audioRepository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = audioRepository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = audioRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = audioRepository.getRecentlyPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<Song>> = audioRepository.getRecentlyAddedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = audioRepository.getMostPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Playback
    val playbackState: StateFlow<PlaybackState> = playbackRepository.playbackState
    val currentPlaylist: StateFlow<List<Song>> = playbackRepository.currentPlaylist

    // Sleep Timer
    val sleepTimerManager = SleepTimerManager()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                audioRepository.searchSongs(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Selected tab
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Sort options
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                audioRepository.refreshLibrary()
            } catch (e: Exception) {
                e.printStackTrace()
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

    // Playback controls
    fun playSong(song: Song) {
        playbackRepository.playSong(song)
        viewModelScope.launch {
            audioRepository.recordPlay(song.id)
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        playbackRepository.playSongs(songs, startIndex)
        viewModelScope.launch {
            songs.getOrNull(startIndex)?.let {
                audioRepository.recordPlay(it.id)
            }
        }
    }

    fun playPause() {
        playbackRepository.playPause()
    }

    fun next() {
        playbackRepository.next()
    }

    fun previous() {
        playbackRepository.previous()
    }

    fun seekTo(position: Long) {
        playbackRepository.seekTo(position)
    }

    fun toggleRepeatMode() {
        playbackRepository.toggleRepeatMode()
    }

    fun toggleShuffleMode() {
        playbackRepository.toggleShuffleMode()
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackRepository.setPlaybackSpeed(speed)
    }

    fun addToQueue(song: Song) {
        playbackRepository.addToQueue(song)
    }

    fun addNext(song: Song) {
        playbackRepository.addNext(song)
    }

    // Favorites
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            audioRepository.toggleFavorite(songId)
        }
    }

    // Playlist management
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(id)
        }
    }

    fun addToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistRepository.getSongsInPlaylist(playlistId)
    }

    override fun onCleared() {
        super.onCleared()
        playbackRepository.release()
    }

    enum class SortOption {
        TITLE, ARTIST, ALBUM, DURATION, DATE_ADDED, DATE_PLAYED
    }
}
