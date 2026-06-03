package com.sonicplayer.repository

import com.sonicplayer.data.model.Playlist
import com.sonicplayer.data.model.Song
import com.sonicplayer.data.dao.PlaylistDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): Playlist? = playlistDao.getPlaylistById(id)

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylist(id)
    }

    suspend fun renamePlaylist(playlist: Playlist, newName: String) {
        playlistDao.updatePlaylist(playlist.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        playlistDao.addSongsToPlaylist(playlistId, songIds)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongsToPlaylist(playlistId, listOf(songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        playlistDao.updateSongCount(playlistId)
    }

    suspend fun clearPlaylist(playlistId: Long) {
        playlistDao.clearPlaylist(playlistId)
        playlistDao.updateSongCount(playlistId)
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistDao.getSongsInPlaylist(playlistId)

    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean =
        playlistDao.isSongInPlaylist(playlistId, songId)
}
