package com.sonicplayer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sonicplayer.data.model.Song
import com.sonicplayer.data.model.Playlist
import com.sonicplayer.data.model.PlaylistSong
import com.sonicplayer.data.model.QueueItem
import com.sonicplayer.data.model.Favorite
import com.sonicplayer.data.model.RecentlyPlayed
import com.sonicplayer.data.dao.SongDao
import com.sonicplayer.data.dao.PlaylistDao
import com.sonicplayer.data.dao.QueueDao
import com.sonicplayer.data.dao.FavoriteDao
import com.sonicplayer.data.dao.RecentlyPlayedDao

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSong::class,
        QueueItem::class,
        Favorite::class,
        RecentlyPlayed::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SonicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun queueDao(): QueueDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
}
