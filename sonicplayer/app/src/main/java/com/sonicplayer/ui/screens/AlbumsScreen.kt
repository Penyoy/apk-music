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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Album
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.AlbumArt
import com.sonicplayer.ui.components.CreatePlaylistDialog
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.PlaylistPickerDialog
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.components.SongOptionsBottomSheet
import com.sonicplayer.ui.components.SongRow
import com.sonicplayer.ui.components.SurfacePill
import com.sonicplayer.ui.components.pressScale
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
fun AlbumsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { headerVisible = true }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(title = "Album", subtitle = "Diskografi") {
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
                isLoading && albums.isEmpty() -> CircularProgressIndicator(
                    color = Gold,
                    modifier = Modifier.align(Alignment.Center)
                )

                albums.isEmpty() -> EmptyState(
                    message = "Belum ada album.\nPindai ulang library musikmu.",
                    actionLabel = "Pindai Ulang",
                    onAction = { viewModel.refreshLibrary() }
                )

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 156.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AnimatedVisibility(
                                visible = headerVisible,
                                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 5 }
                            ) {
                                Column(modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)) {
                                    Text(
                                        text = "Album pilihan",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${albums.size} album dalam koleksimu",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        items(albums, key = { it.id }) { album ->
                            AlbumCard(
                                album = album,
                                onClick = { navController.navigate("album_detail/${album.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumCard(album: Album, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(3.dp)
    ) {
        Box {
            AlbumArt(
                artwork = album.coverUri,
                contentDescription = album.displayTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(20.dp),
                noteSize = 42
            )
            SurfacePill(
                text = "${album.songCount} lagu",
                selected = true,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = album.displayTitle,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.displayArtist,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AlbumDetailScreen(
    albumId: Long,
    navController: NavController,
    viewModel: MainViewModel
) {
    val songs by viewModel.getSongsByAlbum(albumId).collectAsState(initial = emptyList())
    val albums by viewModel.albums.collectAsState()
    val album = albums.find { it.id == albumId }
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

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
                title = album?.displayTitle ?: "Album",
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
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val artSize = minOf(maxWidth - 70.dp, 260.dp).coerceAtLeast(150.dp)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(artSize)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Gold.copy(alpha = 0.22f), Color.Transparent)
                                        ),
                                        CircleShape
                                    )
                                    .padding(8.dp)
                            ) {
                                AlbumArt(
                                    artwork = album?.coverUri,
                                    contentDescription = album?.displayTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(24.dp),
                                    noteSize = 64
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = album?.displayTitle ?: "Album",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = album?.displayArtist ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SurfacePill(text = "${album?.songCount ?: songs.size} lagu")
                            SurfacePill(text = album?.durationText ?: "")
                            if ((album?.year ?: 0) > 0) {
                                SurfacePill(text = album?.year.toString())
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { if (songs.isNotEmpty()) viewModel.playSongs(songs) },
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
                                onClick = {
                                    if (songs.isNotEmpty()) viewModel.playSongs(songs.shuffled())
                                },
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
                        Spacer(modifier = Modifier.height(12.dp))
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
