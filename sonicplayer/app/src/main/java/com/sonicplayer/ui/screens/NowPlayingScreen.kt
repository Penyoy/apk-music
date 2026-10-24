package com.sonicplayer.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Lyrics
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.AlbumArt
import com.sonicplayer.ui.components.CreatePlaylistDialog
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.GoldDivider
import com.sonicplayer.ui.components.PlayPauseIcon
import com.sonicplayer.ui.components.PlaylistPickerDialog
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.SurfaceHighDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun NowPlayingScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val lyrics by viewModel.currentLyrics.collectAsState()
    val song = playbackState.currentSong

    var showMenu by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    LaunchedEffect(lyrics) {
        if (lyrics == null) showLyrics = false
    }

    if (song == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = Gold)
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
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            NowPlayingTopBar(
                song = song,
                isFavorite = favoriteIds.contains(song.id),
                hasLyrics = lyrics != null,
                onBack = { navController.navigateUp() },
                onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                onShowLyrics = { showLyrics = true },
                onMore = { showMenu = true }
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                val wide = maxWidth >= 720.dp || maxWidth > maxHeight
                if (wide) {
                    val artSize = minOf(maxWidth * 0.36f, maxHeight * 0.72f).coerceAtLeast(180.dp)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(42.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            VinylArtwork(
                                song = song,
                                isPlaying = playbackState.isPlaying,
                                size = artSize
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            TrackInfo(song = song, centered = false)
                            Spacer(modifier = Modifier.height(22.dp))
                            ProgressSection(
                                playbackState = playbackState,
                                onSeek = { viewModel.seekTo(it) }
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            NowPlayingControls(
                                playbackState = playbackState,
                                onPlayPause = { viewModel.playPause() },
                                onNext = { viewModel.next() },
                                onPrevious = { viewModel.previous() },
                                onToggleShuffle = { viewModel.toggleShuffleMode() },
                                onToggleRepeat = { viewModel.toggleRepeatMode() }
                            )
                        }
                    }
                } else {
                    val artSize = minOf(maxWidth - 56.dp, maxHeight * 0.34f).coerceAtLeast(130.dp)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        VinylArtwork(
                            song = song,
                            isPlaying = playbackState.isPlaying,
                            size = artSize
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TrackInfo(song = song, centered = true)
                        Spacer(modifier = Modifier.height(18.dp))
                        ProgressSection(
                            playbackState = playbackState,
                            onSeek = { viewModel.seekTo(it) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        NowPlayingControls(
                            playbackState = playbackState,
                            onPlayPause = { viewModel.playPause() },
                            onNext = { viewModel.next() },
                            onPrevious = { viewModel.previous() },
                            onToggleShuffle = { viewModel.toggleShuffleMode() },
                            onToggleRepeat = { viewModel.toggleRepeatMode() }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "SonicPlayer · ♪",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Tambah ke Playlist", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.PlaylistAdd, null, tint = Gold) },
                onClick = {
                    showMenu = false
                    showPlaylistPicker = true
                }
            )
            DropdownMenuItem(
                text = { Text("Equalizer", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Equalizer, null, tint = Gold) },
                onClick = {
                    showMenu = false
                    navController.navigate("equalizer")
                }
            )
            DropdownMenuItem(
                text = { Text("Timer Tidur", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Timer, null, tint = Gold) },
                onClick = {
                    showMenu = false
                    navController.navigate("sleep_timer")
                }
            )
            DropdownMenuItem(
                text = { Text("Pengaturan", color = TextPrimary) },
                leadingIcon = { Icon(Icons.Filled.Settings, null, tint = Gold) },
                onClick = {
                    showMenu = false
                    navController.navigate("settings")
                }
            )
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

    val currentLyrics = lyrics
    if (showLyrics && currentLyrics != null) {
        LyricsSheet(
            lyrics = currentLyrics,
            positionMs = playbackState.position,
            isPlaying = playbackState.isPlaying,
            onDismiss = { showLyrics = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsSheet(
    lyrics: Lyrics,
    positionMs: Long,
    isPlaying: Boolean,
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
        scrimColor = Ink.copy(alpha = 0.72f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .background(Gold.copy(alpha = 0.55f), CircleShape)
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
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lirik",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (lyrics.synced) "Tersinkron" else "Tidak tersinkron",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (lyrics.synced) Gold else TextTertiary
                )
            }
            GoldDivider(modifier = Modifier.padding(horizontal = 22.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun NowPlayingTopBar(
    song: Song,
    isFavorite: Boolean,
    hasLyrics: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowLyrics: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Tutup",
                tint = TextPrimary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SEDANG DIPUTAR",
                style = MaterialTheme.typography.labelSmall,
                color = Gold,
                letterSpacing = 2.4.sp
            )
            Text(
                text = song.displayAlbum,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (hasLyrics) {
            IconButton(onClick = onShowLyrics) {
                Icon(
                    imageVector = Icons.Filled.Subject,
                    contentDescription = "Lirik",
                    tint = Gold
                )
            }
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFavorite) "Hapus favorit" else "Tambah favorit",
                tint = if (isFavorite) Gold else TextSecondary
            )
        }
        IconButton(onClick = onMore) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Opsi", tint = TextPrimary)
        }
    }
}

@Composable
private fun VinylArtwork(
    song: Song,
    isPlaying: Boolean,
    size: Dp
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying, song.id) {
        if (isPlaying) {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(24_000, easing = LinearEasing)
                )
                rotation.snapTo(rotation.value % 360f)
            }
        }
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val glow by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .size(size + 36.dp)
            .graphicsLayer { scaleX = if (isPlaying) glow else 1f; scaleY = if (isPlaying) glow else 1f }
            .background(Gold.copy(alpha = 0.05f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { rotationZ = rotation.value }
                .background(Color(0xFF14110C), CircleShape)
                .border(1.5.dp, Gold.copy(alpha = 0.55f), CircleShape)
                .padding(size * 0.14f),
            contentAlignment = Alignment.Center
        ) {
            AlbumArt(
                artwork = song.artworkUri,
                contentDescription = song.album,
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                noteSize = 48
            )
            Box(
                modifier = Modifier
                    .size(size * 0.075f)
                    .background(Ink, CircleShape)
                    .border(1.dp, Gold.copy(alpha = 0.8f), CircleShape)
            )
        }
    }
}

@Composable
private fun TrackInfo(song: Song, centered: Boolean) {
    Column(
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = song.displayTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = song.displayArtist,
            style = MaterialTheme.typography.bodyLarge,
            color = Gold,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = song.displayAlbum,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProgressSection(
    playbackState: PlaybackState,
    onSeek: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableStateOf(0f) }

    val currentProgress = if (isDragging) {
        dragValue
    } else {
        playbackState.progress.coerceIn(0f, 1f)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = currentProgress,
            onValueChange = {
                isDragging = true
                dragValue = it
            },
            onValueChangeFinished = {
                onSeek((dragValue * playbackState.duration).toLong())
                isDragging = false
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = GoldLight,
                activeTrackColor = Gold,
                inactiveTrackColor = SurfaceHighDark
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime((currentProgress * playbackState.duration).toLong()),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = playbackState.durationText,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun NowPlayingControls(
    playbackState: PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "playPulse")
    val playScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playScale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onToggleShuffle) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Acak",
                tint = if (playbackState.shuffleMode) Gold else TextTertiary
            )
        }

        IconButton(onClick = onPrevious, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Sebelumnya",
                tint = TextPrimary,
                modifier = Modifier.size(30.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(78.dp)
                .graphicsLayer { scaleX = playScale; scaleY = playScale }
                .background(Gold.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Gold,
                    contentColor = Ink
                )
            ) {
                PlayPauseIcon(
                    isPlaying = playbackState.isPlaying,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Berikutnya",
                tint = TextPrimary,
                modifier = Modifier.size(30.dp)
            )
        }

        IconButton(onClick = onToggleRepeat) {
            Icon(
                imageVector = if (playbackState.repeatMode == PlaybackState.REPEAT_MODE_ONE) {
                    Icons.Filled.RepeatOne
                } else {
                    Icons.Filled.Repeat
                },
                contentDescription = "Ulangi",
                tint = if (playbackState.repeatMode != PlaybackState.REPEAT_MODE_OFF) Gold else TextTertiary
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0) / 1000)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
