package com.sonicplayer.di

import android.content.Context
import androidx.room.Room
import com.sonicplayer.data.database.SonicDatabase
import com.sonicplayer.data.dao.SongDao
import com.sonicplayer.data.dao.PlaylistDao
import com.sonicplayer.data.dao.QueueDao
import com.sonicplayer.data.dao.FavoriteDao
import com.sonicplayer.data.dao.RecentlyPlayedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SonicDatabase {
        return Room.databaseBuilder(
            context,
            SonicDatabase::class.java,
            "sonic_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideSongDao(database: SonicDatabase): SongDao = database.songDao()

    @Provides
    @Singleton
    fun providePlaylistDao(database: SonicDatabase): PlaylistDao = database.playlistDao()

    @Provides
    @Singleton
    fun provideQueueDao(database: SonicDatabase): QueueDao = database.queueDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: SonicDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    @Singleton
    fun provideRecentlyPlayedDao(database: SonicDatabase): RecentlyPlayedDao = database.recentlyPlayedDao()
}
