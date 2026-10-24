package com.sonicplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.sonicplayer.ui.components.SonicLogo
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.components.SongOptionsBottomSheet
import com.sonicplayer.ui.components.SongRow
import com.sonicplayer.ui.components.SurfacePill
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.SurfaceElevatedDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun SongsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val songs by viewModel.sortedSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val listState = rememberLazyListState()

    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var heroVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { heroVisible = true }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Lagu",
                subtitle = "Perpustakaan"
            ) {
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
            when {
                isLoading && songs.isEmpty() -> {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                songs.isEmpty() -> {
                    EmptyState(
                        message = "Belum ada lagu di perangkat ini.\nTambahkan file audio lalu pindai ulang.",
                        actionLabel = "Pindai Ulang",
                        onAction = { viewModel.refreshLibrary() }
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = heroVisible,
                                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 5 }
                            ) {
                                CollectionHero(
                                    songCount = songs.size,
                                    totalDuration = songs.sumOf { it.duration },
                                    onPlayAll = { viewModel.playSongs(songs) },
                                    onShuffle = {
                                        viewModel.playSongs(songs.shuffled(), 0)
                                    }
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Semua Lagu",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Box {
                                    SurfacePill(
                                        text = sortLabel(sortBy),
                                        icon = Icons.Filled.Sort,
                                        selected = true,
                                        modifier = Modifier.clickable { showSortMenu = true }
                                    )
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false }
                                    ) {
                                        MainViewModel.SortOption.entries.forEach { option ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        sortLabel(option),
                                                        color = if (option == sortBy) GoldLight else TextPrimary
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.setSortOption(option)
                                                    showSortMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                            SongRow(
                                song = song,
                                isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                                isFavorite = favoriteIds.contains(song.id),
                                onClick = { viewModel.playSongs(songs, index) },
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
    }

    if (showOptions && selectedSong != null) {
        val song = selectedSong!!
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { showOptions = false },
            onPlayNext = {
                viewModel.addNext(song)
                showOptions = false
            },
            onAddToQueue = {
                viewModel.addToQueue(song)
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
            onPlaylistSelected = { playlistId ->
                selectedSong?.let { viewModel.addToPlaylist(playlistId, it.id) }
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
                viewModel.createPlaylist(name) { playlistId ->
                    if (songId != null) viewModel.addToPlaylist(playlistId, songId)
                }
                showCreatePlaylist = false
            }
        )
    }
}

@Composable
private fun CollectionHero(
    songCount: Int,
    totalDuration: Long,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceDark)
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
    ) {
        // Decorative gold glow.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 18.dp)
                .size(150.dp)
                .background(Gold.copy(alpha = 0.05f), CircleShape)
        )

        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SurfacePill(
                    text = "KOLEKSI PRIBADI",
                    selected = true
                )
                Spacer(modifier = Modifier.weight(1f))
                SonicLogo(size = 34.dp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Musik yang",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "berkilau",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Gold,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "  ♪",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldLight
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$songCount lagu · ${Song.formatDuration(totalDuration)}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Ink
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Putar Semua", style = MaterialTheme.typography.titleSmall)
                }
                OutlinedButton(
                    onClick = onShuffle,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.45f))
                ) {
                    Icon(Icons.Filled.Shuffle, contentDescription = null, tint = Gold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Acak", color = Gold, style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Audio lokal · Kualitas tinggi",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

private fun sortLabel(option: MainViewModel.SortOption): String = when (option) {
    MainViewModel.SortOption.TITLE -> "Judul"
    MainViewModel.SortOption.ARTIST -> "Artis"
    MainViewModel.SortOption.ALBUM -> "Album"
    MainViewModel.SortOption.DURATION -> "Durasi"
    MainViewModel.SortOption.DATE_ADDED -> "Terbaru"
    MainViewModel.SortOption.DATE_PLAYED -> "Terakhir Diputar"
}
