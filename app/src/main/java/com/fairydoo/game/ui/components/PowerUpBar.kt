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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.fairydoo.game.ui.theme.GoldCream
import com.fairydoo.game.ui.theme.PowerTileBorder
import com.fairydoo.game.ui.theme.PowerTileBottom
import com.fairydoo.game.ui.theme.PowerTileMiddle
import com.fairydoo.game.ui.theme.PowerTileShieldBorder
import com.fairydoo.game.ui.theme.PowerTileShieldBottom
import com.fairydoo.game.ui.theme.PowerTileShieldMiddle
import com.fairydoo.game.ui.theme.PowerTileShieldTop
import com.fairydoo.game.ui.theme.PowerTileTop

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
            // Bonbon-Relief wie in der Vorlage: heller Verlauf oben, dunkler
            // unten, dazu eine Lichtkante innen oben und ein Schatten innen
            // unten. Erst diese vier Schichten lassen die Kachel gewölbt
            // aussehen statt flach — der Look, den Casual-Games gemeinsam haben.
            val shape = RoundedCornerShape(22.dp)
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .background(
                        brush = if (active) {
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to PowerTileShieldTop,
                                    0.6f to PowerTileShieldMiddle,
                                    1f to PowerTileShieldBottom,
                                ),
                            )
                        } else {
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to PowerTileTop,
                                    0.6f to PowerTileMiddle,
                                    1f to PowerTileBottom,
                                ),
                            )
                        },
                        shape = shape,
                    )
                    .drawBehind {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0x40FFFFFF), Color.Transparent),
                                startY = 0f,
                                endY = 5.dp.toPx(),
                            ),
                            size = Size(size.width, 5.dp.toPx()),
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x59000000)),
                                startY = size.height - 8.dp.toPx(),
                                endY = size.height,
                            ),
                            topLeft = Offset(0f, size.height - 8.dp.toPx()),
                            size = Size(size.width, 8.dp.toPx()),
                        )
                    }
                    .border(
                        width = 2.5.dp,
                        color = if (active) PowerTileShieldBorder else PowerTileBorder,
                        shape = shape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = glyph, fontSize = 30.sp)
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
            fontSize = 12.sp,
            color = GoldCream,
            textAlign = TextAlign.Center,
        )
    }
}
