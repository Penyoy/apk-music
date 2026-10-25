package com.sonicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicplayer.data.model.MusicQueue
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.InkSoft
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(
    song: Song,
    queues: List<MusicQueue>,
    activeQueueId: Long?,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: (Long?) -> Unit,
    onCreateQueue: (String) -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean,
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showQueueChoices by remember { mutableStateOf(false) }
    var showCreateQueue by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = InkSoft,
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
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArt(
                    artwork = song.artworkUri,
                    contentDescription = song.album,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(2.dp),
                    noteSize = 18
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.displayTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.displayArtist} · ${song.displayAlbum}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            GoldDivider()

            if (showQueueChoices) {
                SheetRow(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    title = "Tambah ke Antrean",
                    subtitle = "Pilih antrean tujuan",
                    tint = TextSecondary,
                    onClick = { showQueueChoices = false }
                )
                val active = queues.firstOrNull { it.id == activeQueueId }
                SheetRow(
                    icon = Icons.Filled.Check,
                    title = active?.displayName ?: "Antrean aktif",
                    subtitle = "Tambah ke akhir antrean aktif",
                    onClick = { onAddToQueue(null) }
                )
                queues.filter { it.id != activeQueueId }.forEach { queue ->
                    SheetRow(
                        icon = Icons.Filled.QueueMusic,
                        title = queue.displayName,
                        subtitle = "Tambah ke akhir antrean ini",
                        onClick = { onAddToQueue(queue.id) }
                    )
                }
                SheetRow(
                    icon = Icons.Filled.Add,
                    title = "Antrean baru",
                    subtitle = "Buat antrean lalu masukkan lagu ini",
                    onClick = {
                        showQueueChoices = false
                        showCreateQueue = true
                    }
                )
            } else {
                SheetRow(
                    icon = Icons.Filled.PlaylistPlay,
                    title = "Putar berikutnya",
                    subtitle = "Sisipkan setelah lagu yang diputar",
                    onClick = onPlayNext
                )
                SheetRow(
                    icon = Icons.Filled.QueueMusic,
                    title = "Tambah ke antrean",
                    subtitle = "Pilih antrean tujuan",
                    onClick = { showQueueChoices = true }
                )
                SheetRow(
                    icon = Icons.Filled.PlaylistAdd,
                    title = "Tambah ke playlist",
                    subtitle = "Simpan ke koleksi pribadi",
                    onClick = onAddToPlaylist
                )
                SheetRow(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    title = if (isFavorite) "Hapus dari favorit" else "Tambah ke favorit",
                    subtitle = if (isFavorite) "Keluarkan dari koleksi favorit" else "Tandai sebagai lagu favorit",
                    onClick = onToggleFavorite
                )
                if (onRemoveFromPlaylist != null) {
                    SheetRow(
                        icon = Icons.Filled.Delete,
                        title = "Hapus dari playlist",
                        subtitle = "Lagu tetap tersimpan di perangkat",
                        tint = TextSecondary,
                        onClick = onRemoveFromPlaylist
                    )
                }
            }
        }
    }

    if (showCreateQueue) {
        QueueNameDialog(
            title = "Antrean Baru",
            confirmLabel = "Buat",
            onDismiss = { showCreateQueue = false },
            onConfirm = { name ->
                showCreateQueue = false
                onCreateQueue(name)
            }
        )
    }
}

@Composable
private fun SheetRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = Gold,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
