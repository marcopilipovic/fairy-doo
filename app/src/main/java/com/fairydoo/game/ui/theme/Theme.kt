package com.fairydoo.game.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Das Nachtwald-Schema aus dem Handoff.
 *
 * Bewusst **nur dunkel** und ohne Dynamic Color: Der Nachtwald ist die Identität
 * des Spiels. Ein helles Schema oder die Systemfarben des Nutzers würden die
 * Zonenfarben und das Gold-Leitmotiv zerstören, auf denen die Lesbarkeit des
 * Bretts beruht.
 */
private val FairydokuColors = darkColorScheme(
    primary = Gold,
    onPrimary = TextOnGold,
    primaryContainer = PanelTop,
    onPrimaryContainer = TextPrimary,
    secondary = LeafGreen,
    onSecondary = NightBase,
    tertiary = BlossomPink,
    onTertiary = NightBase,
    background = NightBase,
    onBackground = TextPrimary,
    surface = CardTop,
    onSurface = TextPrimary,
    surfaceVariant = PanelTop,
    onSurfaceVariant = TextPrimary,
    error = DangerPink,
    onError = NightBase,
)

@Composable
fun FairyDooTheme(content: @Composable () -> Unit) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = FairydokuColors,
        typography = FairyDooTypography,
        content = content,
    )
}
