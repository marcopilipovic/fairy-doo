package com.fairydoo.game.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Die drei Tonschalter.
 *
 * Bewusst klein und blass am Rand: Sie gehören nicht zum Spiel, müssen aber
 * erreichbar sein — ein Spiel, dessen Ton sich nicht abstellen lässt, wird im
 * Bus oder im Wartezimmer einfach geschlossen. Ausgeschaltete Schalter sind
 * deutlich abgeblendet, damit der Zustand ohne Beschriftung ablesbar ist.
 */
@Composable
fun AudioToggles(
    musicEnabled: Boolean,
    soundEnabled: Boolean,
    voiceEnabled: Boolean,
    onMusicChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onVoiceChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleGlyph(
            glyph = "🎵",
            label = if (musicEnabled) "Musik ausschalten" else "Musik einschalten",
            enabled = musicEnabled,
            onClick = { onMusicChange(!musicEnabled) },
        )
        ToggleGlyph(
            glyph = "🔔",
            label = if (soundEnabled) "Klänge ausschalten" else "Klänge einschalten",
            enabled = soundEnabled,
            onClick = { onSoundChange(!soundEnabled) },
        )
        ToggleGlyph(
            glyph = "🗣",
            label = if (voiceEnabled) "Feenstimme ausschalten" else "Feenstimme einschalten",
            enabled = voiceEnabled,
            onClick = { onVoiceChange(!voiceEnabled) },
        )
    }
}

@Composable
private fun ToggleGlyph(
    glyph: String,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            // Großzügige Tippfläche trotz kleiner Darstellung.
            .size(36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            fontSize = 17.sp,
            modifier = Modifier.graphicsLayer { alpha = if (enabled) 0.9f else 0.25f },
        )
    }
}
