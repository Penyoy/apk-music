package com.sonicplayer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun RecentlyAddedScreen(navController: NavController, viewModel: MainViewModel) {
    val songs by viewModel.recentlyAdded.collectAsState()
    SongListScreen(
        viewModel = viewModel,
        title = "Terakhir ditambahkan",
        songs = songs,
        onBack = { navController.navigateUp() },
        emptyMessage = "Belum ada lagu yang baru ditambahkan."
    )
}

@Composable
fun RecentlyPlayedScreen(navController: NavController, viewModel: MainViewModel) {
    val songs by viewModel.recentlyPlayed.collectAsState()
    SongListScreen(
        viewModel = viewModel,
        title = "Terakhir diputar",
        songs = songs,
        onBack = { navController.navigateUp() },
        emptyMessage = "Belum ada riwayat pemutaran."
    )
}

@Composable
fun MostPlayedScreen(navController: NavController, viewModel: MainViewModel) {
    val songs by viewModel.mostPlayed.collectAsState()
    SongListScreen(
        viewModel = viewModel,
        title = "Paling sering diputar",
        songs = songs,
        onBack = { navController.navigateUp() },
        emptyMessage = "Belum ada statistik pemutaran."
    )
}

@Composable
fun NotPlayedScreen(navController: NavController, viewModel: MainViewModel) {
    val songs by viewModel.notPlayed.collectAsState()
    SongListScreen(
        viewModel = viewModel,
        title = "Belum pernah diputar",
        songs = songs,
        onBack = { navController.navigateUp() },
        emptyMessage = "Semua lagu sudah pernah diputar."
    )
}
