package com.sonicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sonicplayer.data.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            // Song info header
            ListItem(
                headlineContent = { Text(song.displayTitle) },
                supportingContent = { Text(song.displayArtist) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    ) {
                        // Album art placeholder
                    }
                }
            )

            Divider()

            // Options
            ListItem(
                headlineContent = { Text("Putar Berikutnya") },
                leadingContent = {
                    Icon(Icons.Default.PlaylistPlay, null)
                },
                modifier = Modifier.clickable { onPlayNext() }
            )

            ListItem(
                headlineContent = { Text("Tambah ke Antrian") },
                leadingContent = {
                    Icon(Icons.Default.QueueMusic, null)
                },
                modifier = Modifier.clickable { onAddToQueue() }
            )

            ListItem(
                headlineContent = { Text("Tambah ke Playlist") },
                leadingContent = {
                    Icon(Icons.Default.PlaylistAdd, null)
                },
                modifier = Modifier.clickable { onAddToPlaylist() }
            )

            ListItem(
                headlineContent = { 
                    Text(if (isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit") 
                },
                leadingContent = {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else LocalContentColor.current
                    )
                },
                modifier = Modifier.clickable { onToggleFavorite() }
            )
        }
    }
}
