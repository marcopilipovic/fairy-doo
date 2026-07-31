package com.fairydoo.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.PowerUp

/** Die drei Magie-Fähigkeiten am unteren Rand des Spielbretts. */
@Composable
fun PowerUpBar(
    state: GameState,
    onUse: (PowerUp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        PowerUpButton(
            icon = Icons.Filled.AutoAwesome,
            label = "Feenstaub",
            count = state.powerUpCount(PowerUp.FairyDust),
            enabled = state.isActive && state.powerUpCount(PowerUp.FairyDust) > 0,
            highlighted = false,
            onClick = { onUse(PowerUp.FairyDust) },
        )
        PowerUpButton(
            icon = Icons.Filled.Shield,
            label = "Natur-Schild",
            count = state.powerUpCount(PowerUp.NatureShield),
            enabled = state.isActive &&
                state.powerUpCount(PowerUp.NatureShield) > 0 &&
                !state.shieldActive,
            // Ein bereits aktiver Schild wird hervorgehoben, damit nicht aus
            // Versehen ein zweiter verbraucht wird.
            highlighted = state.shieldActive,
            onClick = { onUse(PowerUp.NatureShield) },
        )
        PowerUpButton(
            icon = Icons.Filled.LocalFlorist,
            label = "Zeiten-Blüte",
            count = state.powerUpCount(PowerUp.TimeBlossom),
            enabled = state.isActive && state.powerUpCount(PowerUp.TimeBlossom) > 0,
            highlighted = state.slowMotionActive,
            onClick = { onUse(PowerUp.TimeBlossom) },
        )
    }
}

@Composable
private fun PowerUpButton(
    icon: ImageVector,
    label: String,
    count: Int,
    enabled: Boolean,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val tint = when {
        highlighted -> colors.tertiary
        enabled -> colors.primary
        else -> colors.onSurfaceVariant.copy(alpha = 0.4f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (highlighted) {
                            colors.tertiary.copy(alpha = 0.20f)
                        } else {
                            colors.surfaceVariant
                        },
                        shape = CircleShape,
                    ),
            )
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "×$count",
            style = MaterialTheme.typography.labelLarge,
            color = tint,
        )
    }
}
