package com.sonicplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.sonicplayer.ui.viewmodel.MainViewModel
import com.sonicplayer.ui.components.MiniPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val playbackState by viewModel.playbackState.collectAsState()
    val currentTab by viewModel.selectedTab.collectAsState()

    val items = listOf(
        BottomNavItem("Lagu", Icons.Filled.MusicNote, Icons.Outlined.MusicNote, "songs"),
        BottomNavItem("Album", Icons.Filled.Album, Icons.Outlined.Album, "albums"),
        BottomNavItem("Artis", Icons.Filled.Person, Icons.Outlined.Person, "artists"),
        BottomNavItem("Playlist", Icons.Filled.PlaylistPlay, Icons.Outlined.PlaylistPlay, "playlists"),
        BottomNavItem("Favorit", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "favorites")
    )

    Scaffold(
        bottomBar = {
            Column {
                // Mini Player
                AnimatedVisibility(
                    visible = playbackState.currentSong != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onPlayPause = { viewModel.playPause() },
                        onNext = { viewModel.next() },
                        onClick = {
                            playbackState.currentSong?.let {
                                navController.navigate("now_playing")
                            }
                        }
                    )
                }

                // Bottom Navigation
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (currentTab == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = currentTab == index,
                            onClick = {
                                viewModel.setSelectedTab(index)
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "songs",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("songs") { SongsScreen(navController, viewModel) }
            composable("albums") { AlbumsScreen(navController, viewModel) }
            composable("album_detail/{albumId}") { backStackEntry ->
                val albumId = backStackEntry.arguments?.getString("albumId")?.toLongOrNull() ?: 0
                AlbumDetailScreen(albumId, navController, viewModel)
            }
            composable("artists") { ArtistsScreen(navController, viewModel) }
            composable("artist_detail/{artistName}") { backStackEntry ->
                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                ArtistDetailScreen(artistName, navController, viewModel)
            }
            composable("playlists") { PlaylistsScreen(navController, viewModel) }
            composable("playlist_detail/{playlistId}") { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0
                PlaylistDetailScreen(playlistId, navController, viewModel)
            }
            composable("favorites") { FavoritesScreen(navController, viewModel) }
            composable("now_playing") { NowPlayingScreen(navController, viewModel) }
            composable("equalizer") { EqualizerScreen(navController, viewModel) }
            composable("sleep_timer") { SleepTimerScreen(navController, viewModel) }
            composable("search") { SearchScreen(navController, viewModel) }
            composable("settings") { SettingsScreen(navController, viewModel) }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)
