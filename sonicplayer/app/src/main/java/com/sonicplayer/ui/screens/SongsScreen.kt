package com.sonicplayer.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun SongsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val songs by viewModel.sortedSongs.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    SongListScreen(
        viewModel = viewModel,
        title = "Lagu",
        songs = songs,
        emptyMessage = "Belum ada lagu di perangkat.\nTambahkan file audio lalu pindai ulang.",
        sortOption = sortBy,
        onSortSelected = { option -> viewModel.setSortOption(option) },
        actions = {
            IconButton(onClick = { navController.navigate("search") }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Cari",
                    tint = com.sonicplayer.ui.theme.TextSecondary
                )
            }
        }
    )
}
