package com.sonicplayer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sonicplayer.ui.components.SonicTopBar
import com.sonicplayer.ui.theme.Gold
import com.sonicplayer.ui.theme.GoldLight
import com.sonicplayer.ui.theme.Ink
import com.sonicplayer.ui.theme.SurfaceDark
import com.sonicplayer.ui.theme.SurfaceHighDark
import com.sonicplayer.ui.theme.TextPrimary
import com.sonicplayer.ui.theme.TextSecondary
import com.sonicplayer.ui.theme.TextTertiary
import com.sonicplayer.ui.viewmodel.MainViewModel

@Composable
fun SleepTimerScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val sleepTimer = viewModel.sleepTimerManager
    val isActive by sleepTimer.isActive.collectAsState()
    val remainingMillis by sleepTimer.remainingMillis.collectAsState()
    val totalMillis by sleepTimer.totalMillis.collectAsState()

    var selectedMinutes by remember { mutableStateOf(15) }
    val presetTimes = listOf(5, 10, 15, 30, 45, 60)
    val progress = if (totalMillis > 0) {
        (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Scaffold(
        containerColor = Ink,
        topBar = {
            SonicTopBar(
                title = "Timer Tidur",
                subtitle = "Istirahat",
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
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isActive) "Musik akan berhenti dalam" else "Pilih durasi istirahatmu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.size(252.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(252.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(Gold.copy(alpha = 0.14f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                    Canvas(modifier = Modifier.size(232.dp)) {
                        val stroke = 9.dp.toPx()
                        drawCircle(
                            color = SurfaceHighDark,
                            style = Stroke(width = stroke)
                        )
                        if (progress > 0f || isActive) {
                            drawArc(
                                color = Gold.copy(alpha = 0.22f),
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = stroke * 2.2f, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Gold,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.NightlightRound,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        AnimatedContent(
                            targetState = isActive,
                            transitionSpec = {
                                (fadeIn(tween(250)) + scaleIn(tween(250), initialScale = 0.85f))
                                    .togetherWith(
                                        fadeOut(tween(150)) +
                                            scaleOut(tween(150), targetScale = 0.9f)
                                    )
                            },
                            label = "timerDisplay"
                        ) { active ->
                            if (active) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatRemaining(remainingMillis),
                                        style = MaterialTheme.typography.displaySmall,
                                        color = GoldLight,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "tersisa",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        letterSpacing = 1.6.sp
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$selectedMinutes",
                                        style = MaterialTheme.typography.displayLarge,
                                        color = TextPrimary,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "MENIT",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Gold,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                AnimatedContent(
                    targetState = isActive,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                    label = "timerControls"
                ) { active ->
                    if (!active) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Preset waktu",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gold,
                                letterSpacing = 1.6.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                            )
                            presetTimes.chunked(3).forEach { rowTimes ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowTimes.forEach { minutes ->
                                        FilterChip(
                                            selected = selectedMinutes == minutes,
                                            onClick = { selectedMinutes = minutes },
                                            label = {
                                                Text(
                                                    "$minutes menit",
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = SurfaceDark,
                                                labelColor = TextSecondary,
                                                selectedContainerColor = Gold.copy(alpha = 0.16f),
                                                selectedLabelColor = Gold
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = selectedMinutes == minutes,
                                                borderColor = Gold.copy(alpha = 0.18f),
                                                selectedBorderColor = Gold.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                    repeat(3 - rowTimes.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            Button(
                                onClick = {
                                    sleepTimer.startTimer(selectedMinutes) {
                                        viewModel.pause()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold,
                                    contentColor = Ink
                                )
                            ) {
                                Icon(Icons.Filled.Timer, null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    "Mulai Timer",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceDark, RoundedCornerShape(18.dp))
                                    .border(
                                        1.dp,
                                        Gold.copy(alpha = 0.16f),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Durasi awal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${totalMillis / 60000} menit",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Gold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            OutlinedButton(
                                onClick = { sleepTimer.cancelTimer() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Gold.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    "Batalkan Timer",
                                    color = Gold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Saat timer habis, musik akan dijeda otomatis.\nKamu tetap bisa melanjutkan pemutaran kapan saja.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

private fun formatRemaining(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0) / 1000)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
