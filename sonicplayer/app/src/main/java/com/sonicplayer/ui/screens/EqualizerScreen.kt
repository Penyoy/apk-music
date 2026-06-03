package com.sonicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.ui.viewmodel.MainViewModel
import com.sonicplayer.util.EqualizerManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val equalizerManager = remember { EqualizerManager(navController.context) }
    val isEnabled by equalizerManager.isEnabled.collectAsState()
    val presets by equalizerManager.presets.collectAsState()
    val currentPreset by equalizerManager.currentPreset.collectAsState()
    val bassBoost by equalizerManager.bassBoostStrength.collectAsState()
    val virtualizer by equalizerManager.virtualizerStrength.collectAsState()

    // Initialize equalizer when playback starts
    LaunchedEffect(viewModel.playbackState.value.currentSong) {
        if (viewModel.playbackState.value.currentSong != null) {
            // Use audio session from player
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equalizer") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { equalizerManager.setEnabled(it) }
                    )
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Presets
            Text("Preset", style = MaterialTheme.typography.titleMedium)
            presets.forEachIndexed { index, preset ->
                OutlinedButton(
                    onClick = { equalizerManager.usePreset(index) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (currentPreset == index) 
                            MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(preset)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Bass Boost
            Text("Bass Boost", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = bassBoost.toFloat(),
                onValueChange = { equalizerManager.setBassBoost(it.toInt()) },
                valueRange = 0f..1000f,
                modifier = Modifier.fillMaxWidth()
            )

            // Virtualizer
            Text("Virtualizer", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = virtualizer.toFloat(),
                onValueChange = { equalizerManager.setVirtualizer(it.toInt()) },
                valueRange = 0f..1000f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
