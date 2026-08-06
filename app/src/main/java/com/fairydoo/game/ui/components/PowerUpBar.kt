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
import com.fairydoo.game.ui.GameCopy
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.StatusPurple
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
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

/**
 * Die zwei Hilfen am unteren Rand: Feenstaub und Irrlicht.
 *
 * Zuvor stand hier nur der Feenstaub, davor sogar drei Fähigkeiten. Der
 * Natur-Schild nahm dem Fehler die Folge, die Zeiten-Blüte der Uhr den Druck;
 * beide machten das Rätsel beliebig. Feenstaub und Irrlicht bleiben dagegen
 * beide bei der Sache — sie decken ein Feld auf, statt eine Regel
 * abzuschwächen: der eine ein sicheres Lösungsfeld, der andere ein sicher
 * ausgeschlossenes.
 *
 * Ist ein Vorrat leer, steht statt „Hinweis" die Zeit bis zum nächsten Stück.
 * Ohne diese Angabe wäre der blasse Knopf eine Sackgasse ohne Erklärung.
 */
@Composable
fun PowerUpBar(
    state: GameState,
    nextDustInMillis: Long,
    nextIrrlichtInMillis: Long,
    onUseFairyDust: () -> Unit,
    onUseIrrlicht: () -> Unit,
    adsUnlocked: Boolean,
    adReady: Boolean,
    onWatchAdForFairyDust: () -> Unit,
    onWatchAdForIrrlicht: () -> Unit,
    onOpenGiftForFairyDust: () -> Unit,
    onOpenGiftForIrrlicht: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Vor Level 11 ersetzt ein Geschenk (sofortiges Auffüllen per Antippen)
    // die Werbung — ein leerer Vorrat muss nie auf den Countdown warten.
    // Solange noch eines übrig ist, bleibt in beiden Fällen der normale
    // Hinweis-Knopf dran.
    val offerAdForFairyDust = state.fairyDust <= 0 && adsUnlocked
    val offerGiftForFairyDust = state.fairyDust <= 0 && !adsUnlocked
    val offerAdForIrrlicht = state.irrlicht <= 0 && adsUnlocked
    val offerGiftForIrrlicht = state.irrlicht <= 0 && !adsUnlocked

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
    ) {
        PowerUpButton(
            glyph = when {
                offerAdForFairyDust -> "📺"
                offerGiftForFairyDust -> "🎁"
                else -> "✨"
            },
            label = when {
                offerAdForFairyDust -> if (adReady) "Werbung\nansehen" else "Werbung\nlädt…"
                offerGiftForFairyDust -> "Geschenk\nannehmen"
                state.fairyDust > 0 || nextDustInMillis <= 0L -> "Feenstaub\ndeckt Fee auf"
                else -> "Feenstaub\nin ${GameCopy.formatWaitTime((nextDustInMillis / 1000L).toInt())}"
            },
            count = state.fairyDust,
            accent = Gold,
            badgeTextColor = Color(0xFF2A1C05),
            active = false,
            enabled = if (offerAdForFairyDust) adReady else true,
            onClick = when {
                offerAdForFairyDust -> onWatchAdForFairyDust
                offerGiftForFairyDust -> onOpenGiftForFairyDust
                else -> onUseFairyDust
            },
        )
        PowerUpButton(
            glyph = when {
                offerAdForIrrlicht -> "📺"
                offerGiftForIrrlicht -> "🎁"
                else -> "🔮"
            },
            label = when {
                offerAdForIrrlicht -> if (adReady) "Werbung\nansehen" else "Werbung\nlädt…"
                offerGiftForIrrlicht -> "Geschenk\nannehmen"
                state.irrlicht > 0 || nextIrrlichtInMillis <= 0L -> "Irrlicht\ndeckt X auf"
                else -> "Irrlicht\nin ${GameCopy.formatWaitTime((nextIrrlichtInMillis / 1000L).toInt())}"
            },
            count = state.irrlicht,
            accent = StatusPurple,
            badgeTextColor = Color(0xFF241C42),
            active = false,
            enabled = if (offerAdForIrrlicht) adReady else true,
            onClick = when {
                offerAdForIrrlicht -> onWatchAdForIrrlicht
                offerGiftForIrrlicht -> onOpenGiftForIrrlicht
                else -> onUseIrrlicht
            },
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
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(86.dp)
            // Ein leerer Vorrat bleibt sichtbar, aber blass und ohne Wirkung —
            // so ist erkennbar, dass die Hilfe existiert und gerade nur
            // nachwächst.
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
            .clickable(
                enabled = enabled,
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
