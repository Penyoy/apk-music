package com.sonicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val favorites by viewModel.favoriteSongs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorit") },
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
            if (favorites.isEmpty()) {
                EmptyState("Belum ada lagu favorit")
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(favorites, key = { it.id }) { song ->
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
    }
}
