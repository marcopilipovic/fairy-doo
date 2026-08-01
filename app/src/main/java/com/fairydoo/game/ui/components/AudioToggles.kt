package com.fairydoo.game.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
 * Der Zugang zu den Klang-Einstellungen.
 *
 * Ein einzelnes Zeichen am Rand statt einer Reihe von Schaltern: Die
 * Feineinstellung gehört nicht ins Spielfeld, muss aber jederzeit erreichbar
 * sein — ein Spiel, dessen Ton sich nicht regeln lässt, wird im Bus oder im
 * Wartezimmer einfach geschlossen.
 *
 * Das Zeichen zeigt zugleich den Zustand: durchgestrichen, wenn alles stumm
 * ist.
 */
@Composable
fun SoundMenuButton(
    anythingAudible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            // Großzügige Tippfläche trotz kleiner Darstellung.
            .size(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = "Klang-Einstellungen" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (anythingAudible) "🔊" else "🔇",
            fontSize = 18.sp,
            modifier = Modifier.graphicsLayer { alpha = if (anythingAudible) 0.9f else 0.45f },
        )
    }
}
