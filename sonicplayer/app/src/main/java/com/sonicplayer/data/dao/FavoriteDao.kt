package com.sonicplayer.data.dao

import androidx.room.*
import com.sonicplayer.data.model.Favorite
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()

    @Transaction
    suspend fun toggleFavorite(songId: Long) {
        if (isFavorite(songId)) {
            removeFavorite(songId)
        } else {
            addFavorite(Favorite(songId))
        }
    }
}
