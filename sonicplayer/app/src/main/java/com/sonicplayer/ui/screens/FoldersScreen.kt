package com.sonicplayer.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.data.model.MusicFolder
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun FoldersScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val folders by viewModel.folders.collectAsState()

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Folder",
                subtitle = "${folders.size} folder"
            )
        }
    ) { padding ->
        if (folders.isEmpty()) {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(message = "Belum ada folder musik.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(folders, key = { it.path }) { folder ->
                    FolderRow(
                        folder = folder,
                        onClick = {
                            navController.navigate("folder_detail?path=${Uri.encode(folder.path)}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderRow(folder: MusicFolder, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Folder,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${folder.songCount} lagu",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun FolderDetailScreen(
    folderPath: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val path = remember(folderPath) { Uri.decode(folderPath) }
    val allSongs by viewModel.songs.collectAsState()
    val songs = remember(allSongs, path) {
        allSongs.filter { it.path.substringBeforeLast('/', "") == path }
    }
    val name = remember(path) {
        path.substringAfterLast('/').ifBlank { path }
    }

    SongListScreen(
        viewModel = viewModel,
        title = name,
        songs = songs,
        onBack = { navController.navigateUp() },
        emptyMessage = "Folder ini tidak berisi lagu."
    )
}
