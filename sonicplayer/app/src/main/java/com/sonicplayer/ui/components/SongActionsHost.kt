package com.sonicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.viewmodel.MainViewModel

/**
 * Single place that owns the per-song action sheet plus the playlist picker.
 * Every list uses this so queue, playlist and favorite actions behave the same.
 */
@Composable
fun SongActionsHost(
    song: Song?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    playlistId: Long? = null
) {
    if (song == null) return

    val queues by viewModel.queues.collectAsState()
    val activeQueue by viewModel.activeQueue.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    var showOptions by remember(song.id) { mutableStateOf(true) }
    var showPlaylistPicker by remember(song.id) { mutableStateOf(false) }
    var showCreatePlaylist by remember(song.id) { mutableStateOf(false) }

    val removeFromPlaylist: (() -> Unit)? = playlistId?.let { targetPlaylistId ->
        {
            viewModel.removeFromPlaylist(targetPlaylistId, song.id)
            onDismiss()
        }
    }

    if (showOptions) {
        SongOptionsBottomSheet(
            song = song,
            queues = queues,
            activeQueueId = activeQueue?.id,
            onDismiss = onDismiss,
            onPlayNext = {
                viewModel.addNext(song)
                onDismiss()
            },
            onAddToQueue = { queueId ->
                viewModel.addToQueue(song, queueId)
                onDismiss()
            },
            onCreateQueue = { name ->
                viewModel.createQueue(name) { queueId ->
                    viewModel.addToQueue(song, queueId)
                }
                onDismiss()
            },
            onAddToPlaylist = {
                showOptions = false
                showPlaylistPicker = true
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(song.id)
                onDismiss()
            },
            isFavorite = favoriteIds.contains(song.id),
            onRemoveFromPlaylist = removeFromPlaylist
        )
    }

    if (showPlaylistPicker) {
        PlaylistPickerDialog(
            playlists = playlists,
            onDismiss = onDismiss,
            onPlaylistSelected = { targetId ->
                viewModel.addToPlaylist(targetId, song.id)
                onDismiss()
            },
            onCreatePlaylist = {
                showPlaylistPicker = false
                showCreatePlaylist = true
            }
        )
    }

    if (showCreatePlaylist) {
        CreatePlaylistDialog(
            onDismiss = onDismiss,
            onCreate = { name ->
                viewModel.createPlaylist(name) { playlistTargetId ->
                    viewModel.addToPlaylist(playlistTargetId, song.id)
                }
                onDismiss()
            }
        )
    }
}
