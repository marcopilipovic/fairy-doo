package ug.humb.fairydoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ug.humb.fairydoku.ui.theme.PanelBorder
import ug.humb.fairydoku.ui.theme.PanelBottom
import ug.humb.fairydoku.ui.theme.PanelTop

/**
 * Der Zugang zu den Klang-Einstellungen.
 *
 * Ein einzelnes Zeichen am Rand statt einer Reihe von Schaltern: Die
 * Feineinstellung gehört nicht ins Spielfeld, muss aber jederzeit erreichbar
 * sein — ein Spiel, dessen Ton sich nicht regeln lässt, wird im Bus oder im
 * Wartezimmer einfach geschlossen.
 *
 * Das Zeichen zeigt zugleich den Zustand: blass, wenn alles stumm ist.
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
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, PanelBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = "Klang-Einstellungen" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = Color(0xFFFFD8A1),
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { alpha = if (anythingAudible) 0.9f else 0.45f },
        )
    }
}
