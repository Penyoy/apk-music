package com.sonicplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sonicplayer.data.model.PlaybackState
import com.sonicplayer.data.model.Song
import com.sonicplayer.repository.AudioRepository
import com.sonicplayer.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong

    if (currentSong == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada lagu yang diputar")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sedang Diputar") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ExpandMore, contentDescription = "Tutup")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("equalizer") }) {
                        Icon(Icons.Default.Equalizer, contentDescription = "Equalizer")
                    }
                    IconButton(onClick = { navController.navigate("sleep_timer") }) {
                        Icon(Icons.Default.Timer, contentDescription = "Timer Tidur")
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opsi")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Tambah ke Playlist") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(if (currentSong.isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit") },
                                onClick = {
                                    viewModel.toggleFavorite(currentSong.id)
                                    showMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        NowPlayingContent(
            song = currentSong,
            playbackState = playbackState,
            onPlayPause = { viewModel.playPause() },
            onNext = { viewModel.next() },
            onPrevious = { viewModel.previous() },
            onSeek = { viewModel.seekTo(it) },
            onToggleRepeat = { viewModel.toggleRepeatMode() },
            onToggleShuffle = { viewModel.toggleShuffleMode() },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun NowPlayingContent(
    song: Song,
    playbackState: PlaybackState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Album Art
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = AudioRepository.getSongUri(song.id),
                contentDescription = song.album,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Song Info
        Text(
            text = song.displayTitle,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = song.displayArtist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Slider
        Slider(
            value = if (playbackState.duration > 0) playbackState.position.toFloat() / playbackState.duration else 0f,
            onValueChange = { progress ->
                onSeek((progress * playbackState.duration).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = playbackState.positionText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = playbackState.durationText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = if (playbackState.shuffleMode) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                    contentDescription = "Acak",
                    tint = if (playbackState.shuffleMode) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Previous
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Sebelumnya",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "Jeda" else "Putar",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Berikutnya",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Repeat
            IconButton(onClick = onToggleRepeat) {
                Icon(
                    imageVector = when (playbackState.repeatMode) {
                        PlaybackState.REPEAT_MODE_ALL -> Icons.Default.RepeatOn
                        PlaybackState.REPEAT_MODE_ONE -> Icons.Default.RepeatOneOn
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Ulangi",
                    tint = if (playbackState.repeatMode != PlaybackState.REPEAT_MODE_OFF) 
                           MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
