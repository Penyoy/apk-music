package com.sonicplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Playlist
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.CreatePlaylistDialog
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.PlaylistPickerDialog
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.components.SongOptionsBottomSheet
import com.sonicplayer.ui.components.SongRow
import com.sonicplayer.ui.components.SurfacePill
import com.sonicplayer.ui.components.pressScale
import com.sonicplayer.ui.theme.BorderGold
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
fun PlaylistsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val playlists by viewModel.playlists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { headerVisible = true }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(title = "Playlist", subtitle = "Koleksi") {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Pengaturan")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Gold,
                contentColor = Ink,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Buat Playlist")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (playlists.isEmpty()) {
                EmptyState(
                    message = "Belum ada playlist.\nSusun koleksi musik dengan gayamu.",
                    icon = Icons.Filled.PlaylistPlay,
                    actionLabel = "Buat Playlist",
                    onAction = { showCreateDialog = true }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 168.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AnimatedVisibility(
                            visible = headerVisible,
                            enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 5 }
                        ) {
                            Column(modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)) {
                                Text(
                                    text = "Koleksi pribadi",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${playlists.size} playlist buatanmu",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { navController.navigate("playlist_detail/${playlist.id}") },
                            onDelete = { playlistToDelete = playlist }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    if (playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Hapus Playlist?", style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
            },
            text = {
                Text(
                    text = "Playlist \"${playlistToDelete?.name}\" akan dihapus. Lagu di perangkat tetap aman.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    playlistToDelete?.let { viewModel.deletePlaylist(it.id) }
                    playlistToDelete = null
                }) {
                    Text("Hapus", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceDark)
            .border(1.dp, Gold.copy(alpha = 0.16f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF4A390E), RoundedCornerShape(17.dp))
                    .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(17.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♪",
                    color = GoldLight,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Opsi playlist",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Hapus playlist", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Gold) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SurfacePill(text = "${playlist.songCount} lagu")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    navController: NavController,
    viewModel: MainViewModel
) {
    val songs by viewModel.getSongsInPlaylist(playlistId).collectAsState(initial = emptyList())
    val playlists by viewModel.playlists.collectAsState()
    val playlist = playlists.find { it.id == playlistId }
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val allPlaylists by viewModel.playlists.collectAsState()

    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { headerVisible = true }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = playlist?.name ?: "Playlist",
                onBack = { navController.navigateUp() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 26.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 6 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .background(Gold.copy(alpha = 0.06f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .background(Color(0xFF2E250D), RoundedCornerShape(30.dp))
                                    .border(
                                        1.dp,
                                        Gold.copy(alpha = 0.4f),
                                        RoundedCornerShape(30.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "♪",
                                    color = GoldLight,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 68.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = playlist?.name ?: "Playlist",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SurfacePill(text = "${songs.size} lagu")
                            SurfacePill(text = "Koleksi pribadi")
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        if (songs.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.playSongs(songs) },
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
                                    Text("Putar Semua", style = MaterialTheme.typography.titleSmall)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.playSongs(songs.shuffled()) },
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
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            if (songs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        EmptyState(
                            message = "Playlist masih kosong.\nTambahkan lagu dari menu opsi lagu.",
                            icon = Icons.Filled.PlaylistPlay
                        )
                    }
                }
            } else {
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
            playlists = allPlaylists,
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
