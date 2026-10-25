package com.sonicplayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val favorites by viewModel.favoriteSongs.collectAsState()

    SongListScreen(
        viewModel = viewModel,
        title = "Favorit",
        songs = favorites,
        onBack = { navController.navigateUp() },
        emptyMessage = "Belum ada lagu favorit.\nTandai lagu lewat menu opsi."
    )
}
