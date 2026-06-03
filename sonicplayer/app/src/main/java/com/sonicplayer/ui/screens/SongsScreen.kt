package com.sonicplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sonicplayer.data.model.Song
import com.sonicplayer.repository.AudioRepository
import com.sonicplayer.ui.viewmodel.MainViewModel
import com.sonicplayer.ui.components.SongOptionsBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val songs by viewModel.sortedSongs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showSortMenu by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lagu") },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Urutkan")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        MainViewModel.SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(when(option) {
                                        MainViewModel.SortOption.TITLE -> "Judul"
                                        MainViewModel.SortOption.ARTIST -> "Artis"
                                        MainViewModel.SortOption.ALBUM -> "Album"
                                        MainViewModel.SortOption.DURATION -> "Durasi"
                                        MainViewModel.SortOption.DATE_ADDED -> "Terbaru"
                                        MainViewModel.SortOption.DATE_PLAYED -> "Terakhir Diputar"
                                    })
                                },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (songs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.playSongs(songs) },
                    icon = { Icon(Icons.Default.Shuffle, null) },
                    text = { Text("Acak Semua") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading && songs.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (songs.isEmpty()) {
                EmptyState(message = "Tidak ada lagu di perangkat")
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(songs, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            isPlaying = playbackState.currentSong?.id == song.id && playbackState.isPlaying,
                            onClick = { viewModel.playSong(song) },
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
        SongOptionsBottomSheet(
            song = selectedSong!!,
            onDismiss = { showOptions = false },
            onPlayNext = {
                viewModel.addNext(selectedSong!!)
                showOptions = false
            },
            onAddToQueue = {
                viewModel.addToQueue(selectedSong!!)
                showOptions = false
            },
            onAddToPlaylist = {
                showOptions = false
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(selectedSong!!.id)
                showOptions = false
            },
            isFavorite = selectedSong!!.isFavorite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = song.displayTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isPlaying) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = "${song.displayArtist} · ${song.durationText}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = AudioRepository.getSongUri(song.id),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opsi",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
