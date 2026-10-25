package com.sonicplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.CreatePlaylistDialog
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.PlaylistPickerDialog
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.components.SongOptionsBottomSheet
import com.sonicplayer.ui.components.SongRow
import com.sonicplayer.ui.components.SurfacePill
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val favorites by viewModel.favoriteSongs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val queues by viewModel.queues.collectAsState()
    val activeQueue by viewModel.activeQueue.collectAsState()

    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { headerVisible = true }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(title = "Favorit", subtitle = "Disukai") {
                IconButton(onClick = { navController.navigate("search") }) {
                    Icon(Icons.Filled.Search, contentDescription = "Cari")
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Pengaturan")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (favorites.isEmpty()) {
                EmptyState(
                    message = "Belum ada lagu favorit.\nTekan ikon hati pada lagu untuk menyimpannya di sini.",
                    icon = Icons.Filled.Favorite,
                    actionLabel = "Jelajahi Lagu",
                    onAction = { navController.navigate("songs") }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 5 }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(SurfaceDark)
                                    .border(
                                        1.dp,
                                        Gold.copy(alpha = 0.3f),
                                        RoundedCornerShape(28.dp)
                                    )
                                    .padding(22.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(130.dp)
                                        .background(Gold.copy(alpha = 0.05f), CircleShape)
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(Gold.copy(alpha = 0.14f), CircleShape)
                                                .border(1.dp, Gold.copy(alpha = 0.4f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Favorite,
                                                contentDescription = null,
                                                tint = Gold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "FAVORIT",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Gold,
                                            letterSpacing = 2.2.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Lagu pilihan",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "hatimu",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Gold,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SurfacePill(text = "${favorites.size} lagu", selected = true)
                                        SurfacePill(
                                            text = Song.formatDuration(favorites.sumOf { it.duration })
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.playSongs(favorites) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Gold,
                                                contentColor = Ink
                                            )
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Putar Semua",
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.playSongs(favorites.shuffled()) },
                                            modifier = Modifier.height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                Gold.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Icon(Icons.Filled.Shuffle, null, tint = Gold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(favorites, key = { _, song -> song.id }) { index, song ->
                        SongRow(
                            song = song,
                            isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                            isFavorite = favoriteIds.contains(song.id),
                            onClick = { viewModel.playSongs(favorites, index) },
                            onMoreClick = {
                                selectedSong = song
                                showOptions = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showOptions && selectedSong != null) {
        val song = selectedSong!!
        SongOptionsBottomSheet(
            song = song,
            queues = queues,
            activeQueueId = activeQueue?.id,
            onDismiss = { showOptions = false },
            onPlayNext = {
                viewModel.addNext(song)
                showOptions = false
            },
            onAddToQueue = { queueId ->
                viewModel.addToQueue(song, queueId)
                showOptions = false
            },
            onCreateQueue = { name ->
                viewModel.createQueue(name) { queueId ->
                    viewModel.addToQueue(song, queueId)
                }
                showOptions = false
            },
            onAddToPlaylist = {
                showOptions = false
                showPlaylistPicker = true
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(song.id)
                showOptions = false
            },
            isFavorite = favoriteIds.contains(song.id)
        )
    }

    if (showPlaylistPicker && selectedSong != null) {
        PlaylistPickerDialog(
            playlists = playlists,
            onDismiss = { showPlaylistPicker = false },
            onPlaylistSelected = { id ->
                selectedSong?.let { viewModel.addToPlaylist(id, it.id) }
                showPlaylistPicker = false
            },
            onCreatePlaylist = {
                showPlaylistPicker = false
                showCreatePlaylist = true
            }
        )
    }

    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylist = false },
            onCreate = { name ->
                val songId = selectedSong?.id
                viewModel.createPlaylist(name) { id ->
                    if (songId != null) viewModel.addToPlaylist(id, songId)
                }
                showCreatePlaylist = false
            }
        )
    }
}
