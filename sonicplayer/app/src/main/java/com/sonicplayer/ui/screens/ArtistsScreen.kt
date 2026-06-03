package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Artist
import com.sonicplayer.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val artists by viewModel.artists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artis") },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading && artists.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (artists.isEmpty()) {
                EmptyState("Tidak ada artis")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(artists, key = { it.id }) { artist ->
                        ArtistItem(artist = artist, onClick = {
                            navController.navigate("artist_detail/${artist.name}")
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistItem(artist: Artist, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(text = artist.displayName, maxLines = 1)
        },
        supportingContent = {
            Text(
                text = "${artist.songCount} lagu · ${artist.albumCount} album",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = artist.displayName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val songs by viewModel.audioRepository.getSongsByArtist(artistName)
        .collectAsState(initial = emptyList())
    val playbackState by viewModel.playbackState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artistName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = artistName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${songs.size} lagu",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { if (songs.isNotEmpty()) viewModel.playSongs(songs) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Putar Semua")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            items(songs, key = { it.id }) { song ->
                SongItem(
                    song = song,
                    isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                    onClick = { viewModel.playSong(song) },
                    onMoreClick = {}
                )
            }
        }
    }
}
