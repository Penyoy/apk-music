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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicplayer.data.model.MusicQueue
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.SurfaceElevatedDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary

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
    isFavorite: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showQueueChoices by remember { mutableStateOf(false) }
    var showCreateQueue by remember { mutableStateOf(false) }

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
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.55f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArt(
                    artwork = song.artworkUri,
                    contentDescription = song.album,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${song.displayArtist} · ${song.displayAlbum}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            GoldDivider(modifier = Modifier.padding(horizontal = 22.dp))

            if (showQueueChoices) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showQueueChoices = false }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tambahkan ke antrean",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                val active = queues.firstOrNull { it.id == activeQueueId }
                SheetOption(
                    icon = Icons.Filled.Check,
                    title = "Antrean aktif",
                    subtitle = active?.displayName ?: "Antrean saat ini",
                    onClick = { onAddToQueue(null) }
                )
                queues.filter { it.id != activeQueueId }.forEach { queue ->
                    SheetOption(
                        icon = Icons.Filled.QueueMusic,
                        title = queue.displayName,
                        subtitle = "Tambahkan ke akhir antrean",
                        onClick = { onAddToQueue(queue.id) }
                    )
                }
                SheetOption(
                    icon = Icons.Filled.Add,
                    title = "Antrean baru",
                    subtitle = "Buat antrean lalu masukkan lagu ini",
                    onClick = {
                        showQueueChoices = false
                        showCreateQueue = true
                    }
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                SheetOption(
                    icon = Icons.Filled.PlaylistPlay,
                    title = "Putar Berikutnya",
                    subtitle = "Sisipkan setelah lagu yang diputar",
                    onClick = onPlayNext
                )
                SheetOption(
                    icon = Icons.Filled.QueueMusic,
                    title = "Tambah ke Antrean",
                    subtitle = "Pilih antrean tujuan",
                    onClick = { showQueueChoices = true }
                )
                SheetOption(
                    icon = Icons.Filled.PlaylistAdd,
                    title = "Tambah ke Playlist",
                    subtitle = "Simpan ke koleksi pribadi",
                    onClick = onAddToPlaylist
                )
                SheetOption(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    title = if (isFavorite) "Hapus dari Favorit" else "Tambah ke Favorit",
                    subtitle = if (isFavorite) "Keluarkan dari koleksi favorit" else "Tandai sebagai lagu favorit",
                    tint = if (isFavorite) Gold else GoldLight,
                    onClick = onToggleFavorite
                )
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
private fun SheetOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color = Gold,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(SurfaceElevatedDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
