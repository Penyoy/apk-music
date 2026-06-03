package com.sonicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // App Info
            ListItem(
                headlineContent = { Text("SonicPlayer") },
                supportingContent = { Text("Versi 1.0.0") },
                leadingContent = {
                    Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
                }
            )

            Divider()

            // Playback Settings
            Text(
                text = "Pemutaran",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Equalizer") },
                supportingContent = { Text("Atur suara musik") },
                leadingContent = {
                    Icon(Icons.Default.Equalizer, null)
                },
                modifier = Modifier.clickable { navController.navigate("equalizer") }
            )

            ListItem(
                headlineContent = { Text("Timer Tidur") },
                supportingContent = { Text("Atur waktu berhenti") },
                leadingContent = {
                    Icon(Icons.Default.Timer, null)
                },
                modifier = Modifier.clickable { navController.navigate("sleep_timer") }
            )

            Divider()

            // Library
            Text(
                text = "Library",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Pindai Ulang") },
                supportingContent = { Text("Perbarui daftar lagu") },
                leadingContent = {
                    Icon(Icons.Default.Refresh, null)
                },
                modifier = Modifier.clickable { viewModel.refreshLibrary() }
            )

            Divider()

            // About
            Text(
                text = "Tentang",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("SonicPlayer") },
                supportingContent = { Text("Pemutar Audio Modern untuk Android") },
                leadingContent = {
                    Icon(Icons.Default.Info, null)
                }
            )
        }
    }
}
