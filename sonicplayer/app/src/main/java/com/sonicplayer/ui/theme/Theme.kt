package com.sonicplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextPrimaryDark,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = Accent,
    onSecondary = TextPrimaryDark,
    secondaryContainer = AccentDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = PrimaryLight,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = SurfaceVariantDark,
    onTertiaryContainer = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = Primary,
    inverseSurface = SurfaceLight,
    inverseOnSurface = TextPrimaryLight,
    error = Error,
    onError = TextPrimaryDark,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error,
    outline = TextTertiaryDark,
    outlineVariant = SurfaceVariantDark,
    scrim = BackgroundDark.copy(alpha = 0.8f)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextPrimaryDark,
    primaryContainer = PrimaryLight.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryDark,
    secondary = Accent,
    onSecondary = TextPrimaryDark,
    secondaryContainer = Accent.copy(alpha = 0.2f),
    onSecondaryContainer = AccentDark,
    tertiary = PrimaryLight,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = SurfaceVariantLight,
    onTertiaryContainer = TextPrimaryLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = Primary,
    inverseSurface = SurfaceDark,
    inverseOnSurface = TextPrimaryDark,
    error = Error,
    onError = TextPrimaryDark,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error,
    outline = TextTertiaryLight,
    outlineVariant = SurfaceVariantLight,
    scrim = BackgroundLight.copy(alpha = 0.8f)
)

@Composable
fun SonicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
