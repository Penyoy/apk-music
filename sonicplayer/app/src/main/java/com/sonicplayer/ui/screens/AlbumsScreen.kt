package com.sonicplayer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Album
import com.sonicplayer.ui.components.AlbumArt
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun AlbumsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val albums by viewModel.albums.collectAsState()

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Album",
                subtitle = "${albums.size} album"
            )
        }
    ) { padding ->
        if (albums.isEmpty()) {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(message = "Belum ada album.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(albums, key = { it.id }) { album ->
                    AlbumRow(
                        album = album,
                        onClick = { navController.navigate("album_detail/${album.id}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumRow(album: Album, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(
            artwork = album.coverUri,
            contentDescription = album.displayTitle,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(2.dp),
            noteSize = 18
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${album.displayArtist} · ${album.songCount} lagu",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = album.durationText,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun AlbumDetailScreen(
    albumId: Long,
    navController: NavController,
    viewModel: MainViewModel
) {
    val albums by viewModel.albums.collectAsState()
    val songs by viewModel.getSongsByAlbum(albumId).collectAsState(initial = emptyList())
    val album = albums.firstOrNull { it.id == albumId }

    SongListScreen(
        viewModel = viewModel,
        title = album?.displayTitle ?: "Album",
        subtitle = album?.let { "${it.displayArtist} · ${it.songCount} lagu" },
        songs = songs,
        onBack = { navController.navigateUp() },
        emptyMessage = "Album ini tidak berisi lagu."
    )
}
