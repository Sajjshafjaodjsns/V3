package com.autovoz.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark industrial theme
val TruckOrange = Color(0xFFFF8C00)
val TruckOrangeLight = Color(0xFFFFAA40)
val TruckYellow = Color(0xFFFFD700)
val DarkBackground = Color(0xFF0F0F0F)
val DarkSurface = Color(0xFF1A1A1A)
val DarkSurfaceVariant = Color(0xFF252525)
val OnDark = Color(0xFFE8E8E8)
val GreenOk = Color(0xFF4CAF50)
val YellowWarn = Color(0xFFFFEB3B)
val RedError = Color(0xFFF44336)

private val DarkColorScheme = darkColorScheme(
    primary = TruckOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3D2200),
    onPrimaryContainer = TruckOrangeLight,
    secondary = TruckYellow,
    onSecondary = Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = OnDark,
    onSurface = OnDark,
    error = RedError,
    onError = Color.Black
)

@Composable
fun AutovozTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
