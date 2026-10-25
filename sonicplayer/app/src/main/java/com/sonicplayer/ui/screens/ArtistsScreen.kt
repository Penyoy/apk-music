package com.sonicplayer.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Artist
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceVariantDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun ArtistsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val artists by viewModel.artists.collectAsState()

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Artis",
                subtitle = "${artists.size} artis"
            )
        }
    ) { padding ->
        if (artists.isEmpty()) {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(message = "Belum ada artis.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(artists, key = { it.id }) { artist ->
                    ArtistRow(
                        artist = artist,
                        onClick = {
                            navController.navigate("artist_detail/${Uri.encode(artist.name)}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistRow(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SurfaceVariantDark, RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${artist.songCount} lagu · ${artist.albumCount} album",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = artist.durationText,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun ArtistDetailScreen(
    artistName: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val resolvedName = remember(artistName) { Uri.decode(artistName) }
    val songs by viewModel.getSongsByArtist(resolvedName).collectAsState(initial = emptyList())
    val artists by viewModel.artists.collectAsState()
    val artist = artists.firstOrNull { it.name == resolvedName }

    SongListScreen(
        viewModel = viewModel,
        title = artist?.displayName ?: resolvedName.ifBlank { "Artis" },
        subtitle = artist?.let { "${it.songCount} lagu · ${it.albumCount} album" },
        songs = songs,
        onBack = { navController.navigateUp() },
        emptyMessage = "Tidak ada lagu dari artis ini."
    )
}
