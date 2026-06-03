package com.sonicplayer.data.dao

import androidx.room.*
import com.sonicplayer.data.model.RecentlyPlayed
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT 50")
    fun getRecentlyPlayed(): Flow<List<RecentlyPlayed>>

    @Query("SELECT * FROM recently_played WHERE songId = :songId")
    suspend fun getBySongId(songId: Long): RecentlyPlayed?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recentlyPlayed: RecentlyPlayed)

    @Query("DELETE FROM recently_played WHERE songId = :songId")
    suspend fun delete(songId: Long)

    @Query("DELETE FROM recently_played")
    suspend fun clearAll()

    @Transaction
    suspend fun recordPlay(songId: Long) {
        val existing = getBySongId(songId)
        if (existing != null) {
            insert(RecentlyPlayed(songId, System.currentTimeMillis(), existing.playCount + 1))
        } else {
            insert(RecentlyPlayed(songId))
        }
    }
}
