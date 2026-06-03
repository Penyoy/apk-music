package com.sonicplayer.data.dao

import androidx.room.*
import com.sonicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE")
    fun searchSongs(query: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): Song?

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber")
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album, trackNumber")
    fun getSongsByArtist(artist: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayed = :timestamp, datePlayed = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM songs ORDER BY datePlayed DESC LIMIT 50")
    fun getRecentlyPlayedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 50")
    fun getRecentlyAddedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT 50")
    fun getMostPlayedSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM songs")
    fun getSongCount(): Flow<Int>

    @Query("SELECT DISTINCT albumId as id, album as title, artist, MIN(id) as artistId, COUNT(*) as songCount, year, SUM(duration) as duration FROM songs GROUP BY albumId, album ORDER BY album COLLATE NOCASE")
    fun getAllAlbums(): Flow<List<AlbumInfo>>

    @Query("SELECT DISTINCT artist, MIN(id) as id, COUNT(*) as songCount, COUNT(DISTINCT albumId) as albumCount, SUM(duration) as duration FROM songs GROUP BY artist ORDER BY artist COLLATE NOCASE")
    fun getAllArtists(): Flow<List<ArtistInfo>>

    @Query("SELECT * FROM songs WHERE path LIKE :folderPath || '%' ORDER BY path")
    fun getSongsInFolder(folderPath: String): Flow<List<Song>>

    @Query("SELECT DISTINCT SUBSTR(path, 1, LENGTH(path) - LENGTH(SUBSTR(path, INSTR(REPLACE(path, '/', '|') || '|', '|')))) as folder FROM songs")
    fun getAllFolders(): Flow<List<String>>
}

data class AlbumInfo(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long,
    val songCount: Int,
    val year: Int,
    val duration: Long
)

data class ArtistInfo(
    val id: Long,
    val artist: String,
    val songCount: Int,
    val albumCount: Int,
    val duration: Long
)
