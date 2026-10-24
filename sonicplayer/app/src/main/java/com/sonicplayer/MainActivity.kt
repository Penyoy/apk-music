package com.sonicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sonicplayer.ui.screens.MainScreen
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SonicPlayerTheme
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _permissionGranted = mutableStateOf(false)
    private val _permanentlyDenied = mutableStateOf(false)
    private var permissionRequested = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionRequested = true
        val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        _permissionGranted.value = audioGranted
        _permanentlyDenied.value = !audioGranted && !shouldShowAudioRationale()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        checkPermissions()

        setContent {
            SonicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (_permissionGranted.value) {
                        MainScreen()
                    } else {
                        PermissionScreen(
                            permanentlyDenied = _permanentlyDenied.value && permissionRequested,
                            onRequestPermission = { requestPermissions() },
                            onOpenSettings = { openAppSettings() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!_permissionGranted.value) checkPermissions()
    }

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun shouldShowAudioRationale(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return shouldShowRequestPermissionRationale(permission)
    }

    private fun checkPermissions() {
        _permissionGranted.value = hasAudioPermission()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            if (!hasNotificationPermission()) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun openAppSettings() {
        startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null)
            )
        )
    }
}

@Composable
private fun PermissionScreen(
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val transition = rememberInfiniteTransition(label = "permissionGlow")
    val glow by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .systemBarsPadding()
    ) {
        // Warm ambient glow behind the logo.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 54.dp)
                .size(320.dp)
                .graphicsLayer { scaleX = glow; scaleY = glow }
                .background(Gold.copy(alpha = 0.06f), CircleShape)
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 8 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 460.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .background(Gold.copy(alpha = 0.12f), CircleShape)
                                .border(1.dp, Gold.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "♪",
                                color = GoldLight,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 58.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "SONIC",
                            style = MaterialTheme.typography.displaySmall,
                            color = Gold,
                            letterSpacing = 6.sp
                        )
                        Text(
                            text = "PLAYER",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            letterSpacing = 8.sp
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Text(
                            text = "Musikmu, dalam nuansa hitam emas.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "SonicPlayer membutuhkan izin akses file audio untuk memindai dan memutar musik di perangkatmu. Tidak ada file yang diunggah ke internet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        PermissionFeature(
                            icon = Icons.Filled.MusicNote,
                            title = "Semua format audio",
                            description = "MP3, FLAC, AAC, WAV, dan lainnya"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PermissionFeature(
                            icon = Icons.Filled.Lock,
                            title = "100% privat",
                            description = "Library tetap berada di perangkatmu"
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold,
                                contentColor = Ink
                            )
                        ) {
                            Icon(Icons.Filled.LibraryMusic, contentDescription = null)
                            Spacer(modifier = Modifier.height(0.dp))
                            Text(
                                text = "Izinkan Akses Musik",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        if (permanentlyDenied) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Izin ditolak. Aktifkan izin penyimpanan/audio dari pengaturan aplikasi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gold.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                            TextButton(onClick = onOpenSettings) {
                                Icon(Icons.Filled.Settings, null, tint = Gold)
                                Text(" Buka Pengaturan", color = Gold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gold.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Gold.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
