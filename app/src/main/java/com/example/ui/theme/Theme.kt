package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealNeonDark,
    onPrimary = Color(0xFF003733),
    primaryContainer = TealDark,
    onPrimaryContainer = TealLight,
    secondary = AmberPrimary,
    onSecondary = Color(0xFF452200),
    secondaryContainer = AmberDark,
    onSecondaryContainer = AmberLight,
    tertiary = InfoBlue,
    error = DangerRed,
    background = PaperDark,
    onBackground = InkLight,
    surface = CardBackgroundDark,
    onSurface = InkLight,
    surfaceVariant = PaperDimDark,
    onSurfaceVariant = InkSoftDark,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealLight,
    onPrimaryContainer = TealDark,
    secondary = AmberPrimary,
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = AmberDark,
    tertiary = InfoBlue,
    error = DangerRed,
    background = PaperLight,
    onBackground = InkDark,
    surface = CardBackgroundLight,
    onSurface = InkDark,
    surfaceVariant = PaperDim,
    onSurfaceVariant = InkSoft,
    outline = BorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinct brand identity by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

