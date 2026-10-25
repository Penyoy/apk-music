package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.artwork.ArtworkProvider
import com.sonicplayer.data.model.Lyrics
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.AlbumArt
import com.sonicplayer.ui.components.AmbientArtwork
import com.sonicplayer.ui.components.CreatePlaylistDialog
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.PlayPauseIcon
import com.sonicplayer.ui.components.PlaylistPickerDialog
import com.sonicplayer.ui.components.SonicDivider
import com.sonicplayer.ui.theme.BorderGoldSoft
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.LyricsUiState
import com.sonicplayer.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NowPlayingScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val activeQueue by viewModel.activeQueue.collectAsState()
    val lyricsState by viewModel.currentLyrics.collectAsState()
    val song = playbackState.currentSong

    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    LaunchedEffect(lyricsState) {
        if (lyricsState !is LyricsUiState.Ready) showLyrics = false
    }

    if (song == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Sedang diputar",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            EmptyState(
                message = "Belum ada lagu yang diputar.\nPilih lagu dari koleksimu.",
                icon = Icons.Filled.MusicNote
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        AmbientArtwork(
            artwork = ArtworkProvider.rawSongUri(song.id),
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Tutup",
                        tint = TextPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sedang diputar",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = activeQueue?.displayName ?: "Antrean",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { viewModel.toggleFavorite(song.id) }) {
                    Icon(
                        imageVector = if (favoriteIds.contains(song.id)) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = "Favorit",
                        tint = if (favoriteIds.contains(song.id)) Gold else TextSecondary
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Opsi",
                            tint = TextSecondary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tambah ke playlist", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Filled.PlaylistAdd, null, tint = TextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                showPlaylistPicker = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lihat antrean", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Filled.QueueMusic, null, tint = TextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                navController.navigate("queue") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Equalizer", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Filled.Equalizer, null, tint = TextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                navController.navigate("equalizer")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Timer tidur", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Filled.Timer, null, tint = TextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                navController.navigate("sleep_timer")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pengaturan", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Filled.Settings, null, tint = TextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                navController.navigate("settings")
                            }
                        )
                    }
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val artSize = minOf(
                    (maxWidth - 48.dp).coerceAtLeast(108.dp),
                    (maxHeight - 360.dp).coerceAtLeast(108.dp),
                    320.dp
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AlbumArt(
                        artwork = song.artworkUri,
                        contentDescription = song.album,
                        modifier = Modifier
                            .size(artSize)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(3.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = song.displayTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${song.displayArtist} · ${song.displayAlbum}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val duration = playbackState.duration.coerceAtLeast(0L)
                        val position = playbackState.position.coerceIn(
                            0L,
                            if (duration > 0) duration else Long.MAX_VALUE
                        )
                        Slider(
                            value = if (duration > 0) position.toFloat() / duration else 0f,
                            onValueChange = { fraction ->
                                if (duration > 0) viewModel.seekTo((fraction * duration).toLong())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Gold,
                                activeTrackColor = Gold,
                                inactiveTrackColor = BorderGoldSoft
                            )
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = Song.formatDuration(position),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = Song.formatDuration(duration),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.toggleShuffleMode() }) {
                                Icon(
                                    imageVector = Icons.Filled.Shuffle,
                                    contentDescription = "Acak",
                                    tint = if (playbackState.shuffleMode) Gold else TextSecondary
                                )
                            }
                            IconButton(onClick = { viewModel.previous() }) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "Sebelumnya",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.playPause() },
                                modifier = Modifier.size(64.dp)
                            ) {
                                PlayPauseIcon(
                                    isPlaying = playbackState.isPlaying,
                                    tint = Gold,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.next() }) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Berikutnya",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                                Icon(
                                    imageVector = if (playbackState.repeatMode ==
                                        PlaybackState.REPEAT_MODE_ONE
                                    ) {
                                        Icons.Filled.RepeatOne
                                    } else {
                                        Icons.Filled.Repeat
                                    },
                                    contentDescription = "Ulangi",
                                    tint = if (playbackState.repeatMode !=
                                        PlaybackState.REPEAT_MODE_OFF
                                    ) {
                                        Gold
                                    } else {
                                        TextSecondary
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        InlineLyrics(
                            state = lyricsState,
                            positionMs = playbackState.position,
                            onOpen = { showLyrics = true }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (lyricsState is LyricsUiState.Ready) {
                    TextAction(
                        icon = Icons.Filled.Subject,
                        label = "Lirik",
                        onClick = { showLyrics = true }
                    )
                }
                TextAction(
                    icon = Icons.Filled.QueueMusic,
                    label = "Antrean",
                    onClick = {
                        navController.navigate("queue") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                TextAction(
                    icon = Icons.Filled.Equalizer,
                    label = "Equalizer",
                    onClick = { navController.navigate("equalizer") }
                )
            }
        }
    }

    if (showPlaylistPicker) {
        PlaylistPickerDialog(
            playlists = playlists,
            onDismiss = { showPlaylistPicker = false },
            onPlaylistSelected = { id ->
                viewModel.addToPlaylist(id, song.id)
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
                viewModel.createPlaylist(name) { id -> viewModel.addToPlaylist(id, song.id) }
                showCreatePlaylist = false
            }
        )
    }

    val readyLyrics = (lyricsState as? LyricsUiState.Ready)?.lyrics
    if (showLyrics && readyLyrics != null) {
        LyricsSheet(
            lyrics = readyLyrics,
            positionMs = playbackState.position,
            onDismiss = { showLyrics = false }
        )
    }
}

@Composable
private fun InlineLyrics(
    state: LyricsUiState,
    positionMs: Long,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        LyricsUiState.Loading -> LyricsStatus(
            text = "Memuat lirik…",
            loading = true,
            modifier = modifier
        )

        LyricsUiState.Unavailable -> LyricsStatus(
            text = "Lirik tidak tersedia untuk lagu ini",
            loading = false,
            modifier = modifier
        )

        is LyricsUiState.Ready -> LyricsPreview(
            lyrics = state.lyrics,
            positionMs = positionMs,
            onOpen = onOpen,
            modifier = modifier
        )
    }
}

@Composable
private fun LyricsStatus(
    text: String,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(SurfaceDark.copy(alpha = 0.48f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Gold,
                strokeWidth = 1.5.dp
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Subject,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LyricsPreview(
    lyrics: Lyrics,
    positionMs: Long,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val activeIndex = if (lyrics.synced) lyrics.activeLineIndex(positionMs) else -1
    var follow by remember(lyrics) { mutableStateOf(true) }

    LaunchedEffect(lyrics) {
        if (lyrics.lines.isNotEmpty()) listState.scrollToItem(0)
        follow = true
    }

    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is DragInteraction.Start -> follow = false
                is DragInteraction.Stop, is DragInteraction.Cancel -> {
                    delay(3_500)
                    follow = true
                }
            }
        }
    }

    LaunchedEffect(activeIndex, follow) {
        if (follow && activeIndex >= 0) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(SurfaceDark.copy(alpha = 0.52f))
            .border(0.5.dp, BorderGoldSoft, RoundedCornerShape(5.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Lirik",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (lyrics.synced) "Tersinkron" else "Tidak tersinkron",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Buka lirik lengkap",
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(lyrics.lines) { index, line ->
                val active = lyrics.synced && index == activeIndex
                Text(
                    text = line.text.ifBlank { "♪" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) {
                        Gold
                    } else {
                        TextSecondary.copy(alpha = if (lyrics.synced) 0.74f else 1f)
                    },
                    fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun TextAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsSheet(
    lyrics: Lyrics,
    positionMs: Long,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val activeIndex = if (lyrics.synced) lyrics.activeLineIndex(positionMs) else -1

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lyrics.lines.size) {
            listState.animateScrollToItem(activeIndex)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        scrimColor = Ink.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 2.dp)
                    .width(32.dp)
                    .height(3.dp)
                    .background(TextTertiary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lirik",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (lyrics.synced) "Tersinkron" else "Tidak tersinkron",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (lyrics.synced) Gold else TextTertiary
                )
            }
            SonicDivider()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(lyrics.lines) { index, line ->
                    val active = lyrics.synced && index == activeIndex
                    Text(
                        text = line.text.ifBlank { "♪" },
                        style = if (active) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (active) Gold else TextSecondary,
                        fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
