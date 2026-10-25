package com.sonicplayer.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sonicplayer.ui.components.GoldDivider
import com.sonicplayer.ui.components.MiniPlayer
import com.sonicplayer.ui.components.SonicLogo
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.InkSoft
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

private val TOP_LEVEL_ROUTES = setOf("songs", "albums", "artists", "playlists", "favorites")

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("Lagu", Icons.Filled.MusicNote, Icons.Outlined.MusicNote, "songs"),
        BottomNavItem("Album", Icons.Filled.Album, Icons.Outlined.Album, "albums"),
        BottomNavItem("Artis", Icons.Filled.Person, Icons.Outlined.Person, "artists"),
        BottomNavItem("Playlist", Icons.Filled.PlaylistPlay, Icons.Outlined.PlaylistPlay, "playlists"),
        BottomNavItem("Favorit", Icons.Filled.Favorite, Icons.Filled.FavoriteBorder, "favorites")
    )

    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    LaunchedEffect(currentRoute) {
        if (currentRoute in TOP_LEVEL_ROUTES) viewModel.setSelectedTab(selectedIndex)
    }

    val isNowPlaying = currentRoute == "now_playing"
    val showBottomNav = currentRoute == null || currentRoute in TOP_LEVEL_ROUTES
    val showMiniPlayer = !isNowPlaying && playbackState.currentSong != null
    val showRail = !isNowPlaying

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 840.dp

        Scaffold(
            containerColor = Ink,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (!showBottomNav && showMiniPlayer) Modifier.navigationBarsPadding()
                                else Modifier
                            )
                    ) {
                        AnimatedVisibility(
                            visible = showMiniPlayer,
                            enter = slideInVertically(tween(280)) { it } + fadeIn(tween(220)),
                            exit = slideOutVertically(tween(220)) { it } + fadeOut(tween(160))
                        ) {
                            MiniPlayer(
                                playbackState = playbackState,
                                onPlayPause = { viewModel.playPause() },
                                onNext = { viewModel.next() },
                                onClick = {
                                    if (currentRoute != "now_playing") navController.navigate("now_playing")
                                },
                                isFavorite = favoriteIds.contains(playbackState.currentSong?.id),
                                onToggleFavorite = {
                                    playbackState.currentSong?.let { viewModel.toggleFavorite(it.id) }
                                }
                            )
                        }

                        AnimatedVisibility(
                            visible = showBottomNav,
                            enter = fadeIn(tween(160)),
                            exit = fadeOut(tween(120))
                        ) {
                            Column {
                                GoldDivider()
                                NavigationBar(
                                    containerColor = InkSoft,
                                    tonalElevation = 0.dp
                                ) {
                                    items.forEachIndexed { index, item ->
                                        val selected = selectedIndex == index
                                        val scale by animateFloatAsState(
                                            targetValue = if (selected) 1.12f else 1f,
                                            animationSpec = tween(220),
                                            label = "navScale$index"
                                        )
                                        NavigationBarItem(
                                            selected = selected,
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
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.label,
                                                    modifier = Modifier.graphicsLayer {
                                                        scaleX = scale
                                                        scaleY = scale
                                                    }
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    letterSpacing = 0.sp
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Gold,
                                                selectedTextColor = Gold,
                                                unselectedIconColor = TextTertiary,
                                                unselectedTextColor = TextTertiary,
                                                indicatorColor = Gold.copy(alpha = 0.14f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (showMiniPlayer) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        MiniPlayer(
                            playbackState = playbackState,
                            onPlayPause = { viewModel.playPause() },
                            onNext = { viewModel.next() },
                            onClick = { navController.navigate("now_playing") },
                            isFavorite = favoriteIds.contains(playbackState.currentSong?.id),
                            onToggleFavorite = {
                                playbackState.currentSong?.let { viewModel.toggleFavorite(it.id) }
                            }
                        )
                    }
                }
            }
        ) { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (expanded && showRail) {
                    SonicNavigationRail(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = { index ->
                            val item = items[index]
                            viewModel.setSelectedTab(index)
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onSettings = { navController.navigate("settings") }
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = "songs",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    enterTransition = {
                        fadeIn(tween(220)) + slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            tween(320)
                        )
                    },
                    exitTransition = {
                        fadeOut(tween(160))
                    },
                    popEnterTransition = {
                        fadeIn(tween(220)) + slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(320)
                        )
                    },
                    popExitTransition = {
                        fadeOut(tween(160)) + slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            tween(260)
                        )
                    }
                ) {
                    composable("songs") { SongsScreen(navController, viewModel) }
                    composable("albums") { AlbumsScreen(navController, viewModel) }
                    composable("album_detail/{albumId}") { entry ->
                        val albumId = entry.arguments?.getString("albumId")?.toLongOrNull() ?: 0L
                        AlbumDetailScreen(albumId, navController, viewModel)
                    }
                    composable("artists") { ArtistsScreen(navController, viewModel) }
                    composable("artist_detail/{artistName}") { entry ->
                        val artistName = entry.arguments?.getString("artistName").orEmpty()
                        ArtistDetailScreen(artistName, navController, viewModel)
                    }
                    composable("playlists") { PlaylistsScreen(navController, viewModel) }
                    composable("playlist_detail/{playlistId}") { entry ->
                        val playlistId = entry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
                        PlaylistDetailScreen(playlistId, navController, viewModel)
                    }
                    composable("favorites") { FavoritesScreen(navController, viewModel) }
                    composable("queue") { QueueScreen(navController, viewModel) }
                    composable("now_playing") { NowPlayingScreen(navController, viewModel) }
                    composable("equalizer") { EqualizerScreen(navController, viewModel) }
                    composable("sleep_timer") { SleepTimerScreen(navController, viewModel) }
                    composable("search") { SearchScreen(navController, viewModel) }
                    composable("settings") { SettingsScreen(navController, viewModel) }
                }
            }
        }
    }
}

@Composable
private fun SonicNavigationRail(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(92.dp)
            .fillMaxHeight()
            .background(InkSoft)
            .border(width = 0.5.dp, color = Gold.copy(alpha = 0.16f), shape = RoundedCornerShape(0.dp))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SonicLogo(size = 42.dp)
        Spacer(modifier = Modifier.height(24.dp))

        items.forEachIndexed { index, item ->
            val selected = selectedIndex == index
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.05f else 1f,
                animationSpec = tween(220),
                label = "railScale$index"
            )
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .size(width = 68.dp, height = 62.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) Gold.copy(alpha = 0.12f) else Color.Transparent)
                    .then(
                        if (selected) Modifier.border(
                            1.dp,
                            Gold.copy(alpha = 0.35f),
                            RoundedCornerShape(18.dp)
                        ) else Modifier
                    )
                    .clickable { onItemSelected(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                    tint = if (selected) Gold else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Gold else TextTertiary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Pengaturan",
                tint = Gold,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "v1.0",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary.copy(alpha = 0.6f)
        )
    }
}

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)
