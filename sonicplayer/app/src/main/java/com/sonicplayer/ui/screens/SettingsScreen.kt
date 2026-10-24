package com.sonicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.ui.components.GoldDivider
import com.sonicplayer.ui.components.SonicLogo
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.components.SurfacePill
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.SurfaceElevatedDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Pengaturan",
                onBack = { navController.navigateUp() }
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
                AppHeaderCard()

                Spacer(modifier = Modifier.height(22.dp))

                SettingSectionTitle("Pemutaran")
                SettingGroup {
                    SettingRow(
                        icon = Icons.Filled.Equalizer,
                        title = "Equalizer",
                        subtitle = "Atur bass, treble, dan preset suara",
                        onClick = { navController.navigate("equalizer") }
                    )
                    GoldDivider(modifier = Modifier.padding(horizontal = 18.dp))
                    SettingRow(
                        icon = Icons.Filled.Timer,
                        title = "Timer Tidur",
                        subtitle = "Hentikan musik secara otomatis",
                        onClick = { navController.navigate("sleep_timer") }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                SettingSectionTitle("Library")
                SettingGroup {
                    SettingRow(
                        icon = Icons.Filled.Storage,
                        title = "Koleksi Musik",
                        subtitle = "${songs.size} lagu terdeteksi",
                        trailing = {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Gold
                                )
                            } else {
                                SurfacePill(text = "Lokal", selected = true)
                            }
                        }
                    )
                    GoldDivider(modifier = Modifier.padding(horizontal = 18.dp))
                    SettingRow(
                        icon = Icons.Filled.Refresh,
                        title = "Pindai Ulang",
                        subtitle = if (isLoading) "Memindai library..." else "Perbarui daftar lagu di perangkat",
                        onClick = { viewModel.refreshLibrary() }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                SettingSectionTitle("Tampilan")
                SettingGroup {
                    SettingRow(
                        icon = Icons.Filled.Palette,
                        title = "Tema Hitam Emas",
                        subtitle = "Tema eksklusif SonicPlayer",
                        trailing = {
                            SurfacePill(text = "Aktif", selected = true)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                SettingSectionTitle("Tentang")
                AboutCard()

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Dibuat dengan ♪ untuk pecinta musik",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AppHeaderCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF251D0B), SurfaceDark, Ink)
                )
            )
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
            .padding(22.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Gold.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            SonicLogo(size = 72.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "SonicPlayer",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hi-fi music, locally yours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                SurfacePill(text = "VERSI 1.0.0", selected = true)
            }
        }
    }
}

@Composable
private fun SettingSectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Gold,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceDark)
            .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SurfaceElevatedDark, CircleShape)
                .border(1.dp, Gold.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
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
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AboutCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceDark)
            .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Tentang Aplikasi",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "SonicPlayer adalah pemutar musik offline minimalis dengan nuansa hitam emas. Semua musik diputar langsung dari perangkatmu tanpa iklan dan tanpa pelacakan.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SurfacePill(text = "Offline")
            SurfacePill(text = "Privat")
            SurfacePill(text = "Tanpa Iklan")
        }
    }
}
