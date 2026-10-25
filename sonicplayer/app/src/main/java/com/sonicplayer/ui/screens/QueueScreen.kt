package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.data.model.MusicQueue
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.AlbumArt
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.GoldDivider
import com.sonicplayer.ui.components.PlayingIndicator
import com.sonicplayer.ui.components.QueueNameDialog
import com.sonicplayer.ui.components.QueuePickerSheet
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun QueueScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val queues by viewModel.queues.collectAsState()
    val activeQueue by viewModel.activeQueue.collectAsState()
    val songs by viewModel.currentPlaylist.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    var showQueuePicker by remember { mutableStateOf(false) }
    var showCreateQueue by remember { mutableStateOf(false) }
    var queueToRename by remember { mutableStateOf<MusicQueue?>(null) }
    var queueToDelete by remember { mutableStateOf<MusicQueue?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Antrean",
                subtitle = activeQueue?.displayName ?: "Pemutaran"
            ) {
                IconButton(onClick = { showQueuePicker = true }) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = "Daftar antrean")
                }
                IconButton(onClick = { showCreateQueue = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Antrean baru")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            QueueHeader(
                queue = activeQueue,
                songCount = songs.size,
                totalDuration = songs.sumOf { it.duration },
                onPickQueue = { showQueuePicker = true },
                onClear = { showClearDialog = true }
            )

            GoldDivider()

            if (songs.isEmpty()) {
                EmptyState(
                    message = "Antrean ini kosong.\nTambahkan lagu lewat menu opsi lagu.",
                    icon = Icons.Filled.QueueMusic
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(
                        songs,
                        key = { index, song -> "${song.id}-$index" }
                    ) { index, song ->
                        QueueRow(
                            song = song,
                            index = index,
                            isCurrent = index == playbackState.currentIndex,
                            isPlaying = index == playbackState.currentIndex && playbackState.isPlaying,
                            canMoveUp = index > 0,
                            canMoveDown = index < songs.lastIndex,
                            onClick = { viewModel.playQueueItem(index) },
                            onPlay = { viewModel.playQueueItem(index) },
                            onMoveUp = { viewModel.moveQueueItem(index, index - 1) },
                            onMoveDown = { viewModel.moveQueueItem(index, index + 1) },
                            onRemove = { viewModel.removeQueueItem(index) }
                        )
                    }
                }
            }
        }
    }

    if (showQueuePicker) {
        QueuePickerSheet(
            queues = queues,
            activeQueueId = activeQueue?.id,
            onDismiss = { showQueuePicker = false },
            onSelect = { queue ->
                viewModel.switchQueue(queue.id)
                showQueuePicker = false
            },
            onCreateQueue = {
                showQueuePicker = false
                showCreateQueue = true
            },
            onRenameQueue = { queue ->
                queueToRename = queue
                showQueuePicker = false
            },
            onDeleteQueue = { queue ->
                queueToDelete = queue
                showQueuePicker = false
            }
        )
    }

    if (showCreateQueue) {
        QueueNameDialog(
            title = "Antrean Baru",
            confirmLabel = "Buat",
            onDismiss = { showCreateQueue = false },
            onConfirm = { name ->
                viewModel.createQueue(name)
                showCreateQueue = false
            }
        )
    }

    queueToRename?.let { queue ->
        QueueNameDialog(
            title = "Ganti Nama Antrean",
            initialName = queue.name,
            confirmLabel = "Simpan",
            onDismiss = { queueToRename = null },
            onConfirm = { name ->
                viewModel.renameQueue(queue.id, name)
                queueToRename = null
            }
        )
    }

    queueToDelete?.let { queue ->
        AlertDialog(
            onDismissRequest = { queueToDelete = null },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(8.dp),
            title = {
                Text("Hapus Antrean?", color = TextPrimary)
            },
            text = {
                Text(
                    text = "\"${queue.displayName}\" beserta isinya akan dihapus. Lagu tetap aman di perangkat.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteQueue(queue.id)
                    queueToDelete = null
                }) {
                    Text("Hapus", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { queueToDelete = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(8.dp),
            title = {
                Text("Kosongkan Antrean?", color = TextPrimary)
            },
            text = {
                Text(
                    text = "Semua lagu akan dikeluarkan dari antrean aktif. Lagu tetap aman di perangkat.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearActiveQueue()
                    showClearDialog = false
                }) {
                    Text("Kosongkan", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun QueueHeader(
    queue: MusicQueue?,
    songCount: Int,
    totalDuration: Long,
    onPickQueue: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.QueueMusic,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onPickQueue)
        ) {
            Text(
                text = queue?.displayName ?: "Antrean",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$songCount lagu · ${Song.formatDuration(totalDuration)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        IconButton(onClick = onClear, enabled = songCount > 0) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Kosongkan antrean",
                tint = if (songCount > 0) TextSecondary else TextTertiary
            )
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    index: Int,
    isCurrent: Boolean,
    isPlaying: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                PlayingIndicator()
            } else {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isCurrent) Gold else TextTertiary
                )
            }
        }

        AlbumArt(
            artwork = song.artworkUri,
            contentDescription = song.album,
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(2.dp),
            noteSize = 18
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                color = if (isCurrent) Gold else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.displayArtist,
                style = MaterialTheme.typography.bodySmall,
                color = if (isCurrent) Gold.copy(alpha = 0.8f) else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = song.durationText,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opsi antrean",
                    tint = TextSecondary
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Putar sekarang", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.PlayArrow, null, tint = Gold) },
                    onClick = {
                        showMenu = false
                        onPlay()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Pindah ke atas", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.ArrowUpward, null, tint = Gold) },
                    enabled = canMoveUp,
                    onClick = {
                        showMenu = false
                        onMoveUp()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Pindah ke bawah", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.ArrowDownward, null, tint = Gold) },
                    enabled = canMoveDown,
                    onClick = {
                        showMenu = false
                        onMoveDown()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hapus dari antrean", color = TextPrimary) },
                    leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Gold) },
                    onClick = {
                        showMenu = false
                        onRemove()
                    }
                )
            }
        }
    }
}
