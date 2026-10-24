package com.sonicplayer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Gold palette ─────────────────────────────────────────────────────────────
val Gold = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF5D76E)
val GoldPale = Color(0xFFFFF1C2)
val GoldDark = Color(0xFF9C7A1E)
val GoldDeep = Color(0xFF6B5210)
val GoldMuted = Color(0xFFBFA45C)

// ── Ink palette ──────────────────────────────────────────────────────────────
val Ink = Color(0xFF070707)
val InkSoft = Color(0xFF0D0D0D)
val SurfaceDark = Color(0xFF121212)
val SurfaceVariantDark = Color(0xFF1B1915)
val SurfaceElevatedDark = Color(0xFF232019)
val SurfaceHighDark = Color(0xFF2C2820)
val BorderGold = Color(0xFF3A3222)
val BorderGoldSoft = Color(0xFF272218)

// ── Type ─────────────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFF6F1E3)
val TextSecondary = Color(0xFFB9B09D)
val TextTertiary = Color(0xFF827A6A)

// ── Feedback ─────────────────────────────────────────────────────────────────
val Error = Color(0xFFE05A5A)
val Success = Color(0xFF7BCB97)
val Warning = Color(0xFFE0B15A)
val Info = Color(0xFF7FA6C9)

// ── Gradients ────────────────────────────────────────────────────────────────
val GoldGradient = Brush.linearGradient(listOf(GoldLight, Gold, GoldDark))
val GoldGradientSoft = Brush.linearGradient(
    listOf(Gold.copy(alpha = 0.35f), Gold.copy(alpha = 0.05f), Gold.copy(alpha = 0.25f))
)
val CoverPlaceholderGradient = Brush.linearGradient(
    listOf(SurfaceHighDark, SurfaceVariantDark, SurfaceDark)
)

// ── Legacy aliases kept for compatibility ────────────────────────────────────
val Primary = Gold
val PrimaryDark = GoldDark
val PrimaryLight = GoldLight
val Accent = GoldLight
val AccentDark = GoldDark
val BackgroundDark = Ink
val BackgroundLight = Ink
val SurfaceLight = SurfaceDark
val SurfaceVariantLight = SurfaceVariantDark
val SurfaceElevatedLight = SurfaceElevatedDark
val TextPrimaryDark = TextPrimary
val TextSecondaryDark = TextSecondary
val TextTertiaryDark = TextTertiary
val TextPrimaryLight = TextPrimary
val TextSecondaryLight = TextSecondary
val TextTertiaryLight = TextTertiary
val GradientStart = Gold
val GradientEnd = GoldDark
val NowPlayingBackground = InkSoft
val ProgressBarBackground = BorderGold
val ProgressBarActive = Gold
