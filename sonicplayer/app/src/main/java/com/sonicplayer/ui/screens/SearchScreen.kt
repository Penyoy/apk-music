package com.sonicplayer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.data.model.Song
import com.sonicplayer.ui.components.CreatePlaylistDialog
import com.sonicplayer.ui.components.EmptyState
import com.sonicplayer.ui.components.PlaylistPickerDialog
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.components.SongOptionsBottomSheet
import com.sonicplayer.ui.components.SongRow
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showOptions by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showCreatePlaylist by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Cari",
                subtitle = "Temukan",
                onBack = { navController.navigateUp() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        "Lagu, artis, atau album...",
                        color = TextSecondary
                    )
                },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Gold) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, null, tint = TextSecondary)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Gold.copy(alpha = 0.25f),
                    cursorColor = Gold,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )

            AnimatedContent(
                targetState = query.isBlank() to results.size,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "searchState"
            ) { (blank, count) ->
                when {
                    blank -> {
                        SearchHint()
                    }

                    count == 0 -> {
                        EmptyState(
                            message = "Tidak ada hasil untuk\n\"$query\"",
                            icon = Icons.Filled.Search
                        )
                    }

                    else -> {
                        Column {
                            Text(
                                text = "$count hasil ditemukan",
                                style = MaterialTheme.typography.labelMedium,
                                color = Gold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                                itemsIndexed(results, key = { _, song -> song.id }) { index, song ->
                                    SongRow(
                                        song = song,
                                        isPlaying = playbackState.currentSong?.id == song.id &&
                                            playbackState.isPlaying,
                                        isFavorite = favoriteIds.contains(song.id),
                                        onClick = { viewModel.playSongs(results, index) },
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
            }
        }
    }

    if (showOptions && selectedSong != null) {
        val song = selectedSong!!
        SongOptionsBottomSheet(
            song = song,
            onDismiss = { showOptions = false },
            onPlayNext = {
                viewModel.addNext(song)
                showOptions = false
            },
            onAddToQueue = {
                viewModel.addToQueue(song)
                showOptions = false
            },
            onAddToPlaylist = {
                showOptions = false
                showPlaylistPicker = true
            },
            onToggleFavorite = {
                viewModel.toggleFavorite(song.id)
                showOptions = false
            },
            isFavorite = favoriteIds.contains(song.id)
        )
    }

    if (showPlaylistPicker && selectedSong != null) {
        PlaylistPickerDialog(
            playlists = playlists,
            onDismiss = { showPlaylistPicker = false },
            onPlaylistSelected = { id ->
                selectedSong?.let { viewModel.addToPlaylist(id, it.id) }
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
                val songId = selectedSong?.id
                viewModel.createPlaylist(name) { id ->
                    if (songId != null) viewModel.addToPlaylist(id, songId)
                }
                showCreatePlaylist = false
            }
        )
    }
}

@Composable
private fun SearchHint() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Gold.copy(alpha = 0.05f))
                    .border(1.dp, Gold.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♪",
                    color = GoldLight,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Temukan suara favoritmu",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cari berdasarkan judul lagu, nama artis,\natau album di perangkatmu.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchSuggestion("Judul")
                SearchSuggestion("Artis")
                SearchSuggestion("Album")
            }
        }
    }
}

@Composable
private fun SearchSuggestion(text: String) {
    Box(
        modifier = Modifier
            .background(Gold.copy(alpha = 0.07f), RoundedCornerShape(50))
            .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Gold.copy(alpha = 0.9f)
        )
    }
}
