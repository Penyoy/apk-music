package com.sonicplayer.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sonicplayer.ui.theme.BorderGoldSoft
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val topDestinations = listOf(
    TopDestination("library", "Perpustakaan", Icons.Filled.LibraryMusic),
    TopDestination("folders", "Folder", Icons.Filled.Folder),
    TopDestination("queue", "Antrean", Icons.Filled.QueueMusic),
    TopDestination("now_playing", "Sedang diputar", Icons.Filled.PlayCircle),
    TopDestination("search", "Cari", Icons.Filled.Search),
    TopDestination("more", "Lainnya", Icons.Filled.MoreHoriz)
)

private val topLevelRoutes = topDestinations.map { it.route }.toSet()

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == null || currentRoute in topLevelRoutes

    Scaffold(
        containerColor = Ink,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                SonicBottomBar(
                    selectedRoute = currentRoute,
                    onSelect = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "library",
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .fillMaxSize(),
            enterTransition = { fadeIn(tween(120)) },
            exitTransition = { fadeOut(tween(90)) },
            popEnterTransition = { fadeIn(tween(120)) },
            popExitTransition = { fadeOut(tween(90)) }
        ) {
            composable("library") { LibraryScreen(navController, viewModel) }
            composable("folders") { FoldersScreen(navController, viewModel) }
            composable("songs") { SongsScreen(navController, viewModel) }
            composable("favorites") { FavoritesScreen(navController, viewModel) }
            composable("albums") { AlbumsScreen(navController, viewModel) }
            composable("artists") { ArtistsScreen(navController, viewModel) }
            composable("playlists") { PlaylistsScreen(navController, viewModel) }
            composable("queue") { QueueScreen(navController, viewModel) }
            composable("now_playing") { NowPlayingScreen(navController, viewModel) }
            composable("search") { SearchScreen(navController, viewModel) }
            composable("more") { MoreScreen(navController, viewModel) }

            composable("recently_added") { RecentlyAddedScreen(navController, viewModel) }
            composable("recently_played") { RecentlyPlayedScreen(navController, viewModel) }
            composable("most_played") { MostPlayedScreen(navController, viewModel) }
            composable("not_played") { NotPlayedScreen(navController, viewModel) }

            composable("album_detail/{albumId}") { entry ->
                val albumId = entry.arguments?.getString("albumId")?.toLongOrNull() ?: 0L
                AlbumDetailScreen(albumId, navController, viewModel)
            }
            composable("artist_detail/{artistName}") { entry ->
                val artistName = entry.arguments?.getString("artistName").orEmpty()
                ArtistDetailScreen(artistName, navController, viewModel)
            }
            composable("playlist_detail/{playlistId}") { entry ->
                val playlistId = entry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
                PlaylistDetailScreen(playlistId, navController, viewModel)
            }
            composable("folder_detail?path={path}") { entry ->
                val path = entry.arguments?.getString("path").orEmpty()
                FolderDetailScreen(path, navController, viewModel)
            }

            composable("equalizer") { EqualizerScreen(navController, viewModel) }
            composable("sleep_timer") { SleepTimerScreen(navController, viewModel) }
            composable("settings") { SettingsScreen(navController, viewModel) }
        }
    }
}

@Composable
private fun SonicBottomBar(
    selectedRoute: String?,
    onSelect: (TopDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(BorderGoldSoft)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(54.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            topDestinations.forEach { destination ->
                val selected = destination.route == selectedRoute
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(destination) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (selected) Gold else TextTertiary,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }
    }
}
