package com.fairydoo.game.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = FairyPink,
    onPrimary = NightDeep,
    primaryContainer = FairyPinkDark,
    onPrimaryContainer = NightDeep,
    secondary = FairyCyan,
    onSecondary = NightDeep,
    secondaryContainer = FairyCyanDark,
    onSecondaryContainer = NightDeep,
    tertiary = FairyGold,
    onTertiary = NightDeep,
    background = NightBase,
    onBackground = TextOnDark,
    surface = NightSurface,
    onSurface = TextOnDark,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = TextOnDarkMuted,
    error = ErrorRed,
    onError = NightDeep,
)

private val LightColors = lightColorScheme(
    primary = FairyPinkDark,
    onPrimary = DaySurface,
    primaryContainer = FairyPink,
    onPrimaryContainer = TextOnLight,
    secondary = FairyCyanDark,
    onSecondary = DaySurface,
    secondaryContainer = FairyCyan,
    onSecondaryContainer = TextOnLight,
    tertiary = FairyGold,
    onTertiary = TextOnLight,
    background = DayBase,
    onBackground = TextOnLight,
    surface = DaySurface,
    onSurface = TextOnLight,
    surfaceVariant = DaySurfaceHigh,
    onSurfaceVariant = TextOnLightMuted,
    error = ErrorRed,
    onError = DaySurface,
)

/**
 * Wurzel-Theme der App.
 *
 * Bewusst **ohne** Dynamic Color (Material You): Ein Spiel soll auf jedem Gerät
 * gleich aussehen, nicht die Systemfarben des Nutzers übernehmen.
 */
@Composable
fun FairyDooTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FairyDooTypography,
        content = content,
    )
}
