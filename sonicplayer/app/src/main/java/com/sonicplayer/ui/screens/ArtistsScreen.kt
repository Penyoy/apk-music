package com.sonicplayer.ui.screens

import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Artist
import com.sonicplayer.data.model.Song
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
import com.sonicplayer.ui.theme.SurfaceVariantDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun ArtistsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val artists by viewModel.artists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var headerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { headerVisible = true }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(title = "Artis", subtitle = "Musisi") {
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
                isLoading && artists.isEmpty() -> CircularProgressIndicator(
                    color = Gold,
                    modifier = Modifier.align(Alignment.Center)
                )

                artists.isEmpty() -> EmptyState(
                    message = "Belum ada artis.\nPindai ulang library musikmu.",
                    actionLabel = "Pindai Ulang",
                    onAction = { viewModel.refreshLibrary() }
                )

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 210.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
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
                                        text = "Artis favoritmu",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${artists.size} artis dalam koleksimu",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        items(artists, key = { it.id }) { artist ->
                            ArtistCard(
                                artist = artist,
                                onClick = {
                                    navController.navigate("artist_detail/${Uri.encode(artist.name)}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistCard(artist: Artist, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(GoldDeepAlpha, CircleShape)
                .border(1.dp, Gold.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = artist.displayName.firstOrNull()?.uppercase() ?: "♪",
                color = GoldLight,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${artist.songCount} lagu · ${artist.albumCount} album",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = artist.durationText,
                style = MaterialTheme.typography.labelSmall,
                color = Gold
            )
        }
    }
}

private val GoldDeepAlpha = Color(0xFF5A4610)

@Composable
fun ArtistDetailScreen(
    artistName: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val resolvedName = Uri.decode(artistName)
    val songs by viewModel.getSongsByArtist(resolvedName).collectAsState(initial = emptyList())
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val artist = artists.find { it.name == resolvedName }

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
                title = artist?.displayName ?: resolvedName.ifBlank { "Artis" },
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
                            val avatarSize = minOf(maxWidth * 0.42f, 190.dp).coerceAtLeast(130.dp)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(avatarSize + 28.dp)
                                    .background(Gold.copy(alpha = 0.06f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(avatarSize)
                                        .background(GoldDeepAlpha, CircleShape)
                                        .border(1.5.dp, Gold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (artist?.displayName ?: resolvedName)
                                            .firstOrNull()?.uppercase() ?: "♪",
                                        color = GoldLight,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (avatarSize.value * 0.42f).sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = artist?.displayName ?: resolvedName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SurfacePill(text = "${songs.size} lagu")
                            SurfacePill(text = "${artist?.albumCount ?: 0} album")
                            SurfacePill(text = artist?.durationText ?: "")
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
