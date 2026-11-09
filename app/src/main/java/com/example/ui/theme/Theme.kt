package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = StudioPrimaryDark,
    onPrimary = StudioOnPrimaryDark,
    primaryContainer = StudioPrimaryContainerDark,
    onPrimaryContainer = StudioOnPrimaryContainerDark,
    secondary = StudioSecondaryDark,
    onSecondary = StudioOnSecondaryDark,
    secondaryContainer = StudioSecondaryContainerDark,
    onSecondaryContainer = StudioOnSecondaryContainerDark,
    tertiary = StudioTertiaryDark,
    onTertiary = StudioOnTertiaryDark,
    background = StudioBackgroundDark,
    surface = StudioSurfaceDark,
    surfaceVariant = StudioSurfaceVariantDark,
    onSurface = StudioOnSurfaceDark,
    onSurfaceVariant = StudioOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = StudioPrimaryLight,
    onPrimary = StudioOnPrimaryLight,
    primaryContainer = StudioPrimaryContainerLight,
    onPrimaryContainer = StudioOnPrimaryContainerLight,
    secondary = StudioSecondaryLight,
    onSecondary = StudioOnSecondaryLight,
    secondaryContainer = StudioSecondaryContainerLight,
    onSecondaryContainer = StudioOnSecondaryContainerLight,
    tertiary = StudioTertiaryLight,
    onTertiary = StudioOnTertiaryLight,
    background = StudioBackgroundLight,
    surface = StudioSurfaceLight,
    surfaceVariant = StudioSurfaceVariantLight,
    onSurface = StudioOnSurfaceLight,
    onSurfaceVariant = StudioOnSurfaceVariantLight
)

@Composable
fun CreativePaintTheme(
    themePreference: String = "SYSTEM",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
