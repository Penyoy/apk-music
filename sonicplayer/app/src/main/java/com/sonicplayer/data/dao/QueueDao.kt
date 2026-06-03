package com.sonicplayer.data.dao

import androidx.room.*
import com.sonicplayer.data.model.QueueItem
import com.sonicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue ORDER BY position")
    fun getQueue(): Flow<List<QueueItem>>

    @Query("SELECT s.* FROM songs s INNER JOIN queue q ON s.id = q.songId ORDER BY q.position")
    fun getQueueSongs(): Flow<List<Song>>

    @Query("SELECT * FROM queue WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentQueueItem(): QueueItem?

    @Query("UPDATE queue SET isCurrent = 0")
    suspend fun clearCurrent()

    @Query("UPDATE queue SET isCurrent = 1 WHERE id = :queueItemId")
    suspend fun setCurrent(queueItemId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QueueItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QueueItem>)

    @Query("DELETE FROM queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM queue")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM queue")
    suspend fun getCount(): Int

    @Transaction
    suspend fun setQueue(songIds: List<Long>, startPosition: Int = 0) {
        clear()
        val items = songIds.mapIndexed { index, songId ->
            QueueItem(songId = songId, position = index, isCurrent = index == startPosition)
        }
        insertAll(items)
    }

    @Transaction
    suspend fun addToQueue(songId: Long) {
        val count = getCount()
        insert(QueueItem(songId = songId, position = count))
    }

    @Transaction
    suspend fun addNext(songId: Long) {
        val current = getCurrentQueueItem()
        val currentPos = current?.position ?: -1
        
        // Shift items after current
        shiftPositions(currentPos + 1)
        
        // Insert new item
        insert(QueueItem(songId = songId, position = currentPos + 1))
    }

    @Query("UPDATE queue SET position = position + 1 WHERE position >= :fromPosition")
    suspend fun shiftPositions(fromPosition: Int)

    @Query("DELETE FROM queue WHERE songId = :songId")
    suspend fun removeBySongId(songId: Long)
}
