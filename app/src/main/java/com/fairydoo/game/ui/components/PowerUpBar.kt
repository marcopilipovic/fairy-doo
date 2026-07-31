package com.fairydoo.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.PowerUp
import com.fairydoo.game.ui.theme.BlossomPink
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.LeafGreen
import com.fairydoo.game.ui.theme.TextPrimary

/** Die drei Magie-Fähigkeiten am unteren Rand. */
@Composable
fun PowerUpBar(
    state: GameState,
    onUse: (PowerUp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
    ) {
        PowerUpButton(
            glyph = "✨",
            label = "Feenstaub\nHinweis",
            count = state.powerUpCount(PowerUp.FairyDust),
            accent = Gold,
            badgeTextColor = Color(0xFF2A1C05),
            active = false,
            onClick = { onUse(PowerUp.FairyDust) },
        )
        PowerUpButton(
            glyph = "🍃",
            label = "Natur-\nSchild",
            count = state.powerUpCount(PowerUp.NatureShield),
            accent = LeafGreen,
            badgeTextColor = Color(0xFF05310F),
            // Ein bereits aktiver Schild leuchtet grün — so ist erkennbar,
            // dass ein weiterer Tipp nichts bringt.
            active = state.shieldActive,
            onClick = { onUse(PowerUp.NatureShield) },
        )
        PowerUpButton(
            glyph = "🌸",
            label = "Zeiten-\nBlüte",
            count = state.powerUpCount(PowerUp.TimeBlossom),
            accent = BlossomPink,
            badgeTextColor = Color(0xFF3D0824),
            active = state.timeFrozen,
            onClick = { onUse(PowerUp.TimeBlossom) },
        )
    }
}

@Composable
private fun PowerUpButton(
    glyph: String,
    label: String,
    count: Int,
    accent: Color,
    badgeTextColor: Color,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(86.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(
                        brush = if (active) {
                            Brush.verticalGradient(
                                listOf(Color(0xFF2F6B45), Color(0xFF1D4A2E)),
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFF33407A), Color(0xFF1E2450)),
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                    )
                    .border(2.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = glyph, fontSize = 28.sp)
            }

            Box(
                modifier = Modifier
                    .offset(x = 7.dp, y = (-7).dp)
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .background(accent, CircleShape)
                    .padding(horizontal = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    color = badgeTextColor,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}
