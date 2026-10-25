package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.SongActionsHost
import com.sonicplayer.ui.components.SongRow
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.BorderGoldSoft
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

/** Compact, reusable list of songs. Every collection screen funnels through this. */
@Composable
fun SongListScreen(
    viewModel: MainViewModel,
    title: String,
    songs: List<Song>,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    emptyMessage: String = "Tidak ada lagu.",
    sortOption: MainViewModel.SortOption? = null,
    onSortSelected: ((MainViewModel.SortOption) -> Unit)? = null,
    playlistId: Long? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    var actionSong by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = title,
                subtitle = subtitle ?: songSummary(songs),
                onBack = onBack,
                actions = actions
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ListControlRow(
                songs = songs,
                sortOption = sortOption,
                onPlayAll = { viewModel.playSongs(songs, 0) },
                onShuffle = { if (songs.isNotEmpty()) viewModel.playSongs(songs.shuffled(), 0) },
                onSortSelected = onSortSelected
            )

            if (songs.isEmpty()) {
                EmptyState(message = emptyMessage)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(
                        songs,
                        key = { index, song -> "${song.id}-$index" }
                    ) { index, song ->
                        SongRow(
                            song = song,
                            isPlaying = playbackState.currentSong?.id == song.id,
                            isFavorite = favoriteIds.contains(song.id),
                            onClick = { viewModel.playSongs(songs, index) },
                            onMoreClick = { actionSong = song }
                        )
                    }
                }
            }
        }
    }

    SongActionsHost(
        song = actionSong,
        viewModel = viewModel,
        onDismiss = { actionSong = null },
        playlistId = playlistId
    )
}

@Composable
private fun ListControlRow(
    songs: List<Song>,
    sortOption: MainViewModel.SortOption?,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onSortSelected: ((MainViewModel.SortOption) -> Unit)?
) {
    var showSort by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${songs.size} lagu",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onShuffle, enabled = songs.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Acak",
                tint = if (songs.isNotEmpty()) TextSecondary else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onPlayAll, enabled = songs.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Putar semua",
                tint = if (songs.isNotEmpty()) Gold else TextTertiary,
                modifier = Modifier.size(22.dp)
            )
        }

        if (onSortSelected != null && sortOption != null) {
            Box {
                IconButton(onClick = { showSort = true }) {
                    Icon(
                        imageVector = Icons.Filled.Sort,
                        contentDescription = "Urutkan",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showSort,
                    onDismissRequest = { showSort = false }
                ) {
                    MainViewModel.SortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = sortLabel(option),
                                    color = if (option == sortOption) Gold else TextPrimary
                                )
                            },
                            onClick = {
                                showSort = false
                                onSortSelected(option)
                            }
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(BorderGoldSoft)
    )
}

internal fun sortLabel(option: MainViewModel.SortOption): String = when (option) {
    MainViewModel.SortOption.TITLE -> "Judul"
    MainViewModel.SortOption.ARTIST -> "Artis"
    MainViewModel.SortOption.ALBUM -> "Album"
    MainViewModel.SortOption.DURATION -> "Durasi"
    MainViewModel.SortOption.DATE_ADDED -> "Terbaru ditambahkan"
    MainViewModel.SortOption.DATE_PLAYED -> "Terakhir diputar"
}

internal fun songSummary(songs: List<Song>): String {
    if (songs.isEmpty()) return "Tidak ada lagu"
    val total = songs.sumOf { it.duration }
    val minutes = total / 60_000
    val duration = when {
        minutes >= 60 -> "${minutes / 60} j ${minutes % 60} mnt"
        else -> "$minutes mnt"
    }
    return "${songs.size} lagu · $duration"
}
