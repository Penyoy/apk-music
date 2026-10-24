package com.sonicplayer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BlackGoldScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Ink,
    primaryContainer = GoldDeep,
    onPrimaryContainer = GoldPale,
    inversePrimary = GoldDark,

    secondary = GoldLight,
    onSecondary = Ink,
    secondaryContainer = SurfaceHighDark,
    onSecondaryContainer = GoldLight,

    tertiary = GoldMuted,
    onTertiary = Ink,
    tertiaryContainer = SurfaceVariantDark,
    onTertiaryContainer = GoldMuted,

    background = Ink,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Gold,
    surfaceBright = SurfaceHighDark,
    surfaceDim = InkSoft,
    surfaceContainer = SurfaceVariantDark,
    surfaceContainerHigh = SurfaceElevatedDark,
    surfaceContainerHighest = SurfaceHighDark,
    surfaceContainerLow = InkSoft,
    surfaceContainerLowest = Ink,

    inverseSurface = TextPrimary,
    inverseOnSurface = Ink,

    error = Error,
    onError = Ink,
    errorContainer = Error.copy(alpha = 0.18f),
    onErrorContainer = Error,

    outline = BorderGold,
    outlineVariant = BorderGoldSoft,
    scrim = Ink.copy(alpha = 0.85f)
)

@Composable
fun SonicPlayerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = BlackGoldScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Ink.toArgb()
            window.navigationBarColor = Ink.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
