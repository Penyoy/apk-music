package com.sonicplayer.ui.viewmodel

import com.sonicplayer.data.model.Lyrics

/** Explicit lyrics loading state so "still loading" never looks like "no lyrics". */
sealed interface LyricsUiState {
    data object Loading : LyricsUiState
    data class Ready(val lyrics: Lyrics) : LyricsUiState
    data object Unavailable : LyricsUiState
}
