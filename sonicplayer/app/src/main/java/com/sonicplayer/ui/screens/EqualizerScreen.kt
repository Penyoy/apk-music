package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.BorderGoldSoft
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.SurfaceHighDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.util.EqualizerManager
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun EqualizerScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val equalizerManager = remember { EqualizerManager(context) }

    DisposableEffect(Unit) {
        equalizerManager.initialize(0)
        onDispose { equalizerManager.release() }
    }

    val isEnabled by equalizerManager.isEnabled.collectAsState()
    val presets by equalizerManager.presets.collectAsState()
    val currentPreset by equalizerManager.currentPreset.collectAsState()
    val bassBoost by equalizerManager.bassBoostStrength.collectAsState()
    val virtualizer by equalizerManager.virtualizerStrength.collectAsState()
    val bandLevels by equalizerManager.bandLevels.collectAsState()
    val levelRange = remember(bandLevels.size) { equalizerManager.getBandLevelRange() }
    val frequencies = remember(bandLevels.size) { equalizerManager.getBandFrequencies() }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Equalizer",
                onBack = { navController.navigateUp() },
                actions = {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { equalizerManager.setEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Ink,
                            checkedTrackColor = Gold,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceHighDark,
                            uncheckedBorderColor = BorderGoldSoft
                        )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isEnabled) {
                        "Efek suara aktif. Perubahan langsung terdengar."
                    } else {
                        "Efek suara nonaktif. Aktifkan lewat sakelar di atas."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                EqualizerSectionTitle("Preset")
                if (presets.isEmpty()) {
                    Text(
                        text = "Preset tidak tersedia pada perangkat ini. Band dapat diatur manual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    presets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowPresets.forEach { presetName ->
                                val index = presets.indexOf(presetName)
                                val selected = currentPreset == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (selected) Gold.copy(alpha = 0.10f) else SurfaceDark)
                                        .clickable(enabled = isEnabled) {
                                            equalizerManager.usePreset(index)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 11.dp)
                                ) {
                                    Text(
                                        text = presetName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = when {
                                            !isEnabled -> TextTertiary
                                            selected -> Gold
                                            else -> TextSecondary
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                            if (rowPresets.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (bandLevels.isNotEmpty()) {
                    EqualizerSectionTitle("Band frekuensi")
                    Column(modifier = Modifier.fillMaxWidth()) {
                        bandLevels.forEachIndexed { index, level ->
                            val frequency = frequencies.getOrNull(index) ?: 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (frequency >= 1000) {
                                        "${frequency / 1000}kHz"
                                    } else {
                                        "${frequency}Hz"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.width(46.dp)
                                )
                                Slider(
                                    value = level,
                                    onValueChange = { equalizerManager.setBandLevel(index, it) },
                                    valueRange = levelRange.first.toFloat()..levelRange.second.toFloat(),
                                    enabled = isEnabled,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Gold,
                                        activeTrackColor = Gold,
                                        inactiveTrackColor = BorderGoldSoft
                                    )
                                )
                                Text(
                                    text = "${(level / 100f).toInt()}dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isEnabled) TextSecondary else TextTertiary,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(42.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                EqualizerSectionTitle("Efek tambahan")
                EffectSlider(
                    title = "Bass boost",
                    value = bassBoost,
                    enabled = isEnabled,
                    onValueChange = { equalizerManager.setBassBoost(it) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                EffectSlider(
                    title = "Virtualizer",
                    value = virtualizer,
                    enabled = isEnabled,
                    onValueChange = { equalizerManager.setVirtualizer(it) }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ketersediaan efek bergantung pada chip audio perangkat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun EqualizerSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Gold,
        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
    )
}

@Composable
private fun EffectSlider(
    title: String,
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) TextPrimary else TextTertiary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${value / 10}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (value > 0) Gold else TextTertiary
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = Gold,
                inactiveTrackColor = BorderGoldSoft
            )
        )
    }
}
