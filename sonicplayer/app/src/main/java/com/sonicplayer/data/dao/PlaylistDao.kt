package com.sonicplayer.data.dao

import androidx.room.*
import com.sonicplayer.data.model.Playlist
import com.sonicplayer.data.model.PlaylistSong
import com.sonicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("UPDATE playlists SET songCount = (SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId) WHERE id = :playlistId")
    suspend fun updateSongCount(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Query("""
        SELECT s.* FROM songs s 
        INNER JOIN playlist_songs ps ON s.id = ps.songId 
        WHERE ps.playlistId = :playlistId 
        ORDER BY ps.position
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean

    @Transaction
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val existingCount = getPlaylistSongCount(playlistId)
        songIds.forEachIndexed { index, songId ->
            addSongToPlaylist(PlaylistSong(playlistId, songId, existingCount + index))
        }
        updateSongCount(playlistId)
        updateTimestamp(playlistId)
    }

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getPlaylistSongCount(playlistId: Long): Int

    @Query("UPDATE playlists SET updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updateTimestamp(playlistId: Long, timestamp: Long = System.currentTimeMillis())
}
