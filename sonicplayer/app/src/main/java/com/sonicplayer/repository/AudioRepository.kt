package com.sonicplayer.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.sonicplayer.data.model.Song
import com.sonicplayer.data.model.Album
import com.sonicplayer.data.model.Artist
import com.sonicplayer.data.dao.SongDao
import com.sonicplayer.data.dao.FavoriteDao
import com.sonicplayer.data.dao.RecentlyPlayedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRepository @Inject constructor(
    private val context: Context,
    private val songDao: SongDao,
    private val favoriteDao: FavoriteDao,
    private val recentlyPlayedDao: RecentlyPlayedDao
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>> = songDao.getSongsByAlbum(albumId)
    fun getSongsByArtist(artist: String): Flow<List<Song>> = songDao.getSongsByArtist(artist)
    fun getFavoriteSongs(): Flow<List<Song>> = songDao.getFavoriteSongs()
    fun getRecentlyPlayedSongs(): Flow<List<Song>> = songDao.getRecentlyPlayedSongs()
    fun getRecentlyAddedSongs(): Flow<List<Song>> = songDao.getRecentlyAddedSongs()
    fun getMostPlayedSongs(): Flow<List<Song>> = songDao.getMostPlayedSongs()
    fun getSongCount(): Flow<Int> = songDao.getSongCount()

    fun getAllAlbums(): Flow<List<Album>> = songDao.getAllAlbums().map { albums ->
        albums.map { albumInfo ->
            Album(
                id = albumInfo.id,
                title = albumInfo.title,
                artist = albumInfo.artist,
                artistId = albumInfo.artistId,
                songCount = albumInfo.songCount,
                year = albumInfo.year,
                duration = albumInfo.duration,
                coverUri = getAlbumArtUri(albumInfo.id)
            )
        }
    }

    fun getAllArtists(): Flow<List<Artist>> = songDao.getAllArtists().map { artists ->
        artists.map { artistInfo ->
            Artist(
                id = artistInfo.id,
                name = artistInfo.artist,
                songCount = artistInfo.songCount,
                albumCount = artistInfo.albumCount,
                duration = artistInfo.duration
            )
        }
    }

    suspend fun toggleFavorite(songId: Long) {
        val isFav = favoriteDao.isFavorite(songId)
        songDao.updateFavoriteStatus(songId, !isFav)
        favoriteDao.toggleFavorite(songId)
    }

    suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)

    suspend fun recordPlay(songId: Long) {
        songDao.incrementPlayCount(songId)
        recentlyPlayedDao.recordPlay(songId)
    }

    suspend fun refreshLibrary() = withContext(Dispatchers.IO) {
        val songs = scanAudioFiles()
        songDao.insertAll(songs)
    }

    private fun scanAudioFiles(): List<Song> {
        val songs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.SAMPLE_RATE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val genreColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)
            val bitrateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
            val sampleRateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SAMPLE_RATE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, id).toString()
                
                songs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleColumn) ?: "",
                        artist = cursor.getString(artistColumn) ?: "",
                        album = cursor.getString(albumColumn) ?: "",
                        albumId = cursor.getLong(albumIdColumn),
                        duration = cursor.getLong(durationColumn),
                        trackNumber = cursor.getInt(trackColumn),
                        dateAdded = cursor.getLong(dateAddedColumn) * 1000,
                        dateModified = cursor.getLong(dateModifiedColumn) * 1000,
                        path = cursor.getString(pathColumn) ?: "",
                        uri = uri,
                        year = cursor.getInt(yearColumn),
                        genre = cursor.getString(genreColumn) ?: "",
                        bitrate = cursor.getInt(bitrateColumn),
                        sampleRate = cursor.getInt(sampleRateColumn)
                    )
                )
            }
        }

        return songs
    }

    fun getAlbumArtUri(albumId: Long): Uri {
        val uri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
        return uri
    }

    companion object {
        fun getSongUri(songId: Long): Uri {
            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId)
        }
    }
}
