package ug.humb.fairydoku.ui.components

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
import androidx.compose.foundation.layout.widthIn
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
import ug.humb.fairydoku.ads.AdOffer
import ug.humb.fairydoku.game.GameState
import ug.humb.fairydoku.ui.GameCopy
import ug.humb.fairydoku.ui.theme.Gold
import ug.humb.fairydoku.ui.theme.LeafGreen
import ug.humb.fairydoku.ui.theme.StatusPurple
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import ug.humb.fairydoku.ui.theme.GoldCream
import ug.humb.fairydoku.ui.theme.PowerTileBorder
import ug.humb.fairydoku.ui.theme.PowerTileBottom
import ug.humb.fairydoku.ui.theme.PowerTileMiddle
import ug.humb.fairydoku.ui.theme.PowerTileShieldBorder
import ug.humb.fairydoku.ui.theme.PowerTileShieldBottom
import ug.humb.fairydoku.ui.theme.PowerTileShieldMiddle
import ug.humb.fairydoku.ui.theme.PowerTileShieldTop
import ug.humb.fairydoku.ui.theme.PowerTileTop

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
 * Ist ein Vorrat leer, steht statt der Wirkung die Zeit bis zum nächsten Stück.
 * Ohne diese Angabe wäre der blasse Knopf eine Sackgasse ohne Erklärung.
 */
@Composable
fun PowerUpBar(
    state: GameState,
    nextDustInMillis: Long,
    nextIrrlichtInMillis: Long,
    nextFeenkreisInMillis: Long,
    onUseFairyDust: () -> Unit,
    onUseIrrlicht: () -> Unit,
    onUseFeenkreis: () -> Unit,
    adsUnlocked: Boolean,
    adOffer: AdOffer,
    onWatchAdForFairyDust: () -> Unit,
    onWatchAdForIrrlicht: () -> Unit,
    onOpenGiftForFairyDust: () -> Unit,
    onOpenGiftForIrrlicht: () -> Unit,
    onWatchAdForFeenkreis: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // In den ersten Leveln ersetzt ein Geschenk (sofortiges Auffüllen per Antippen)
    // die Werbung — ein leerer Vorrat muss nie auf den Countdown warten.
    // Solange noch eines übrig ist, bleibt in beiden Fällen der normale
    // Hinweis-Knopf dran.
    val offerAdForFairyDust = state.fairyDust <= 0 && adsUnlocked
    val offerGiftForFairyDust = state.fairyDust <= 0 && !adsUnlocked
    val offerAdForIrrlicht = state.irrlicht <= 0 && adsUnlocked
    val offerGiftForIrrlicht = state.irrlicht <= 0 && !adsUnlocked
    val kreisBrennt = state.feenkreisMillis > 0L
    // Fuer den Feenkreis gibt es keinen Geschenk-Weg: In den ersten drei
    // Leveln, in denen das Geschenk die Werbung ersetzt, braucht ihn niemand —
    // ein 4x4-Gitter hat vier Feen. Wer ohne dasteht, wartet drei Stunden oder
    // sieht sich ein Video an.
    val offerAdForFeenkreis = state.feenkreis <= 0 && !kreisBrennt && adsUnlocked

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        PowerUpButton(
            glyph = when {
                offerAdForFairyDust -> "📺"
                offerGiftForFairyDust -> "🎁"
                else -> "✨"
            },
            label = when {
                offerAdForFairyDust -> werbeLabel(adOffer)
                offerGiftForFairyDust -> "Geschenk\nannehmen"
                state.fairyDust > 0 || nextDustInMillis <= 0L -> "Feenstaub\ndeckt Fee auf"
                else -> "Feenstaub\nin ${GameCopy.formatWaitTime((nextDustInMillis / 1000L).toInt())}"
            },
            count = state.fairyDust,
            accent = Gold,
            badgeTextColor = Color(0xFF2A1C05),
            active = false,
            enabled = if (offerAdForFairyDust) adOffer == AdOffer.Available else true,
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
                offerAdForIrrlicht -> werbeLabel(adOffer)
                offerGiftForIrrlicht -> "Geschenk\nannehmen"
                state.irrlicht > 0 || nextIrrlichtInMillis <= 0L -> "Irrlicht\ndeckt X auf"
                else -> "Irrlicht\nin ${GameCopy.formatWaitTime((nextIrrlichtInMillis / 1000L).toInt())}"
            },
            count = state.irrlicht,
            accent = StatusPurple,
            badgeTextColor = Color(0xFF241C42),
            active = false,
            enabled = if (offerAdForIrrlicht) adOffer == AdOffer.Available else true,
            onClick = when {
                offerAdForIrrlicht -> onWatchAdForIrrlicht
                offerGiftForIrrlicht -> onOpenGiftForIrrlicht
                else -> onUseIrrlicht
            },
        )
        // Der Feenkreis. Er nimmt kein Nachdenken ab, sondern Tipparbeit:
        // Solange er brennt, kreuzt jede gesetzte Fee selbst an, welche
        // Felder sie ausschliesst. Deshalb zeigt sein Knopf als einziger
        // eine laufende Zeit statt eines Nachwachs-Countdowns.
        PowerUpButton(
            glyph = when {
                offerAdForFeenkreis -> "\ud83d\udcfa"
                else -> "\ud83d\udcab"
            },
            label = when {
                kreisBrennt ->
                    "Feenkreis\nnoch ${(state.feenkreisMillis + 999L) / 1000L} s"
                offerAdForFeenkreis -> werbeLabel(adOffer)
                state.feenkreis > 0 || nextFeenkreisInMillis <= 0L ->
                    "Feenkreis\nkreuzt selbst an"
                else ->
                    "Feenkreis\nin ${GameCopy.formatWaitTime((nextFeenkreisInMillis / 1000L).toInt())}"
            },
            count = state.feenkreis,
            accent = LeafGreen,
            badgeTextColor = Color(0xFF0F2A16),
            active = kreisBrennt,
            enabled = when {
                kreisBrennt -> false
                offerAdForFeenkreis -> adOffer == AdOffer.Available
                else -> true
            },
            onClick = when {
                offerAdForFeenkreis -> onWatchAdForFeenkreis
                else -> onUseFeenkreis
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
            // Nicht auf 86 dp festgenagelt, sondern mitwachsend bis 140 dp.
            //
            // Vorher stand hier `width(86.dp)`. Das reicht für „Feenstaub" nur
            // bei normaler Systemschrift; wer sie vergrößert hat, sah
            // „Feenstau" und darunter ein einzelnes „b" — Android bricht mitten
            // im Wort um, wenn das Wort allein schon breiter ist als die Zeile.
            //
            // Die Kachel darüber bleibt bei 66 dp, nur die Beschriftung darf
            // breiter werden. Die Obergrenze ist so gewählt, dass zwei Knöpfe
            // samt Abstand (2 × 140 + 18 dp) auch auf einem schmalen Telefon
            // nebeneinander passen.
            .widthIn(min = 86.dp, max = 140.dp)
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

/**
 * Beschriftung des Werbe-Knopfes.
 *
 * Drei Zustände statt zwei: „lädt…" wäre falsch, wenn jemand die Einwilligung
 * abgelehnt hat — dann kommt nämlich nie etwas, und der Knopf würde ewig
 * warten lassen.
 */
private fun werbeLabel(offer: AdOffer): String = when (offer) {
    AdOffer.Available -> "Werbung\nansehen"
    AdOffer.Preparing -> "Werbung\nlädt…"
    AdOffer.Unavailable -> "Werbung\nnicht da"
}
