package com.sonicplayer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sonicplayer.data.model.MusicQueue
import com.sonicplayer.data.model.QueueItem
import com.sonicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    // ---------------------------------------------------------------------
    // Queues
    // ---------------------------------------------------------------------

    @Query("SELECT * FROM queues ORDER BY createdAt ASC")
    fun observeQueues(): Flow<List<MusicQueue>>

    @Query("SELECT * FROM queues WHERE active = 1 LIMIT 1")
    fun observeActiveQueue(): Flow<MusicQueue?>

    @Query("SELECT * FROM queues WHERE active = 1 LIMIT 1")
    suspend fun getActiveQueue(): MusicQueue?

    @Query("SELECT * FROM queues WHERE id = :queueId LIMIT 1")
    suspend fun getQueue(queueId: Long): MusicQueue?

    @Query("SELECT * FROM queues WHERE name = :name ORDER BY createdAt ASC LIMIT 1")
    suspend fun getQueueByName(name: String): MusicQueue?

    @Query("SELECT * FROM queues ORDER BY createdAt ASC")
    suspend fun getQueues(): List<MusicQueue>

    @Insert
    suspend fun insertQueue(queue: MusicQueue): Long

    @Update
    suspend fun updateQueue(queue: MusicQueue)

    @Query("UPDATE queues SET active = 0")
    suspend fun clearActive()

    @Query("UPDATE queues SET active = 1 WHERE id = :queueId")
    suspend fun setActive(queueId: Long)

    @Query("UPDATE queues SET lastPosition = :position, updatedAt = :timestamp WHERE id = :queueId")
    suspend fun setLastPosition(queueId: Long, position: Int, timestamp: Long)

    @Query("UPDATE queues SET name = :name, updatedAt = :timestamp WHERE id = :queueId")
    suspend fun renameQueue(queueId: Long, name: String, timestamp: Long)

    @Query("DELETE FROM queues WHERE id = :queueId")
    suspend fun deleteQueue(queueId: Long)

    // ---------------------------------------------------------------------
    // Queue items
    // ---------------------------------------------------------------------

    @Query(
        "SELECT s.* FROM songs s INNER JOIN queue q ON s.id = q.songId " +
            "WHERE q.queueId = :queueId ORDER BY q.position"
    )
    fun observeQueueSongs(queueId: Long): Flow<List<Song>>

    @Query(
        "SELECT s.* FROM songs s INNER JOIN queue q ON s.id = q.songId " +
            "WHERE q.queueId = :queueId ORDER BY q.position"
    )
    suspend fun getQueueSongs(queueId: Long): List<Song>

    @Query("SELECT * FROM queue WHERE queueId = :queueId ORDER BY position")
    suspend fun getQueueItems(queueId: Long): List<QueueItem>

    @Query("SELECT COUNT(*) FROM queue WHERE queueId = :queueId")
    suspend fun getItemCount(queueId: Long): Int

    @Query("SELECT songId FROM queue WHERE queueId = :queueId ORDER BY position")
    suspend fun getSongIds(queueId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: QueueItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<QueueItem>)

    @Query("UPDATE queue SET position = :position WHERE id = :itemId")
    suspend fun updateItemPosition(itemId: Long, position: Int)

    @Query("UPDATE queue SET position = position + 1 WHERE queueId = :queueId AND position >= :fromPosition")
    suspend fun shiftPositions(queueId: Long, fromPosition: Int)

    @Query("DELETE FROM queue WHERE queueId = :queueId")
    suspend fun clearItems(queueId: Long)

    @Query("DELETE FROM queue WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM queue WHERE queueId = :queueId AND songId = :songId")
    suspend fun deleteSong(queueId: Long, songId: Long)

    // ---------------------------------------------------------------------
    // Transactions
    // ---------------------------------------------------------------------

    @Transaction
    suspend fun activateQueue(queueId: Long) {
        clearActive()
        setActive(queueId)
    }

    @Transaction
    suspend fun replaceQueueItems(queueId: Long, songIds: List<Long>, startPosition: Int, timestamp: Long) {
        clearItems(queueId)
        insertItems(
            songIds.mapIndexed { index, songId ->
                QueueItem(queueId = queueId, songId = songId, position = index)
            }
        )
        val safePosition = if (songIds.isEmpty()) 0 else startPosition.coerceIn(0, songIds.lastIndex)
        setLastPosition(queueId, safePosition, timestamp)
    }

    @Transaction
    suspend fun createQueue(
        name: String,
        songIds: List<Long> = emptyList(),
        activate: Boolean = true,
        timestamp: Long
    ): Long {
        if (activate) clearActive()
        val queueId = insertQueue(
            MusicQueue(
                name = name,
                active = activate,
                lastPosition = 0,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )
        if (songIds.isNotEmpty()) {
            insertItems(
                songIds.mapIndexed { index, songId ->
                    QueueItem(queueId = queueId, songId = songId, position = index)
                }
            )
        }
        return queueId
    }

    @Transaction
    suspend fun addSongToQueue(queueId: Long, songId: Long, timestamp: Long) {
        val count = getItemCount(queueId)
        insertItem(QueueItem(queueId = queueId, songId = songId, position = count))
        touchQueue(queueId, timestamp)
    }

    @Transaction
    suspend fun addNextToQueue(
        queueId: Long,
        songId: Long,
        afterPosition: Int,
        timestamp: Long
    ) {
        val count = getItemCount(queueId)
        val insertPosition = (afterPosition + 1).coerceIn(0, count)
        shiftPositions(queueId, insertPosition)
        insertItem(QueueItem(queueId = queueId, songId = songId, position = insertPosition))
        touchQueue(queueId, timestamp)
    }

    @Transaction
    suspend fun removeSongFromQueue(queueId: Long, songId: Long, timestamp: Long) {
        deleteSong(queueId, songId)
        normalizePositions(queueId)
        touchQueue(queueId, timestamp)
    }

    @Transaction
    suspend fun removeItemAt(queueId: Long, position: Int, timestamp: Long) {
        val items = getQueueItems(queueId)
        val item = items.getOrNull(position) ?: return
        deleteItem(item.id)
        normalizePositions(queueId)
        touchQueue(queueId, timestamp)
    }

    @Transaction
    suspend fun moveItem(queueId: Long, fromPosition: Int, toPosition: Int, timestamp: Long) {
        val items = getQueueItems(queueId).toMutableList()
        if (fromPosition !in items.indices || toPosition !in items.indices) return
        if (fromPosition == toPosition) return

        val moved = items.removeAt(fromPosition)
        items.add(toPosition, moved)
        items.forEachIndexed { index, item ->
            if (item.position != index) updateItemPosition(item.id, index)
        }
        touchQueue(queueId, timestamp)
    }

    @Transaction
    suspend fun normalizePositions(queueId: Long) {
        getQueueItems(queueId).forEachIndexed { index, item ->
            if (item.position != index) updateItemPosition(item.id, index)
        }
    }

    @Query("UPDATE queues SET updatedAt = :timestamp WHERE id = :queueId")
    suspend fun touchQueue(queueId: Long, timestamp: Long)
}
