package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.components.SurfacePill
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.SurfaceElevatedDark
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
                subtitle = "Suara",
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
                            uncheckedBorderColor = Gold.copy(alpha = 0.3f)
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Hero card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark)
                        .border(1.dp, Gold.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(Gold.copy(alpha = 0.12f), CircleShape)
                                .border(1.dp, Gold.copy(alpha = 0.38f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = Gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(14.dp))
                        Column {
                            Text(
                                text = "Studio Sound",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEnabled) {
                                    "Efek audio aktif · sesuaikan karaktermu"
                                } else {
                                    "Aktifkan untuk mengubah karakter suara"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                EqualizerSectionTitle("Preset Suara")
                if (presets.isEmpty()) {
                    Text(
                        text = "Preset tidak tersedia pada perangkat ini. Kamu tetap dapat mengatur band secara manual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                } else {
                    presets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { presetName ->
                                val index = presets.indexOf(presetName)
                                OutlinedButton(
                                    onClick = { equalizerManager.usePreset(index) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = isEnabled,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (currentPreset == index) Gold else Gold.copy(alpha = 0.2f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (currentPreset == index) {
                                            Gold.copy(alpha = 0.14f)
                                        } else {
                                            SurfaceDark
                                        },
                                        contentColor = if (currentPreset == index) Gold else TextSecondary
                                    )
                                ) {
                                    Text(
                                        text = presetName,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            if (rowPresets.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (bandLevels.isNotEmpty()) {
                    EqualizerSectionTitle("Band Frekuensi")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceDark)
                            .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        bandLevels.forEachIndexed { index, level ->
                            val frequency = frequencies.getOrNull(index) ?: 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (frequency >= 1000) {
                                        "${frequency / 1000}kHz"
                                    } else {
                                        "${frequency}Hz"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.size(width = 46.dp, height = 18.dp)
                                )
                                Slider(
                                    value = level,
                                    onValueChange = { equalizerManager.setBandLevel(index, it) },
                                    valueRange = levelRange.first.toFloat()..levelRange.second.toFloat(),
                                    enabled = isEnabled,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = GoldLight,
                                        activeTrackColor = Gold,
                                        inactiveTrackColor = SurfaceHighDark
                                    )
                                )
                                Text(
                                    text = "${(level / 100f).toInt()}dB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.size(width = 38.dp, height = 18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                EqualizerSectionTitle("Efek Tambahan")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SurfaceDark)
                        .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    EffectSlider(
                        title = "Bass Boost",
                        value = bassBoost,
                        enabled = isEnabled,
                        onValueChange = { equalizerManager.setBassBoost(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    EffectSlider(
                        title = "Virtualizer",
                        value = virtualizer,
                        enabled = isEnabled,
                        onValueChange = { equalizerManager.setVirtualizer(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Gold.copy(alpha = 0.05f))
                        .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Ketersediaan efek bergantung pada chip audio perangkatmu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))
            }
        }
    }
}

@Composable
private fun EqualizerSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Gold,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun EffectSlider(
    title: String,
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            SurfacePill(
                text = "${(value / 10)}%",
                selected = value > 0
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..1000f,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = GoldLight,
                activeTrackColor = Gold,
                inactiveTrackColor = SurfaceHighDark
            )
        )
    }
}
