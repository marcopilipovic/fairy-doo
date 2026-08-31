package ug.humb.fairydoku.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ug.humb.fairydoku.ads.AdOffer
import ug.humb.fairydoku.ui.theme.CardBottom
import ug.humb.fairydoku.ui.theme.CardTop
import ug.humb.fairydoku.game.FairySpecies
import ug.humb.fairydoku.game.GlobalLives
import ug.humb.fairydoku.game.GlobalLivesState
import ug.humb.fairydoku.ui.sprites.FAIRY_TOKEN
import ug.humb.fairydoku.ui.sprites.FairyImage
import ug.humb.fairydoku.ui.sprites.fairyInlineContent
import ug.humb.fairydoku.ui.sprites.fairyText
import ug.humb.fairydoku.ui.theme.DangerRose
import ug.humb.fairydoku.ui.theme.Gold
import ug.humb.fairydoku.ui.theme.GoldLight
import ug.humb.fairydoku.ui.theme.LeafGreen
import ug.humb.fairydoku.ui.theme.TextOnGold
import ug.humb.fairydoku.ui.theme.TextPrimary
import ug.humb.fairydoku.game.GameViewModel

/** Wie die Karte hereinkommt. */
enum class OverlayEntrance { RiseUp, PopIn }

/**
 * Gemeinsames Gerüst aller drei Overlays: abgedunkelter Hintergrund, Karte,
 * Gold-Knopf. Der Hintergrund fängt Tipps ab, damit das Brett darunter nicht
 * reagiert.
 */
@Composable
internal fun OverlayScaffold(
    borderColor: Color,
    entrance: OverlayEntrance,
    scrimAlpha: Float,
    content: @Composable ColumnScope.() -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = when (entrance) {
                OverlayEntrance.RiseUp -> tween(450)
                OverlayEntrance.PopIn -> keyframes {
                    durationMillis = 500
                    0f at 0
                    1.15f at 350
                    1f at 500
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080A1C).copy(alpha = scrimAlpha))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 340.dp)
                .graphicsLayer {
                    when (entrance) {
                        OverlayEntrance.RiseUp -> {
                            alpha = progress.value.coerceIn(0f, 1f)
                            translationY = (1f - progress.value.coerceIn(0f, 1f)) * 16.dp.toPx()
                        }
                        OverlayEntrance.PopIn -> {
                            alpha = (progress.value / 0.6f).coerceIn(0f, 1f)
                            scaleX = progress.value
                            scaleY = progress.value
                        }
                    }
                }
                .background(
                    brush = Brush.verticalGradient(listOf(CardTop, CardBottom)),
                    shape = RoundedCornerShape(22.dp),
                )
                .border(1.5.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 24.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

/** Der goldene Handlungsknopf, der in allen Overlays gleich aussieht. */
@Composable
internal fun GoldButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(GoldLight, Gold)),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = TextOnGold,
            textAlign = TextAlign.Center,
        )
    }
}

/** Willkommen im Wald — erklärt Regeln und Fähigkeiten. */
@Composable
fun IntroOverlay(bestScore: Int, onStart: () -> Unit) {
    OverlayScaffold(
        borderColor = Gold.copy(alpha = 0.5f),
        entrance = OverlayEntrance.RiseUp,
        scrimAlpha = 0.82f,
    ) {
        Text(
            text = "✨ Willkommen, Hüter:in ✨",
            style = MaterialTheme.typography.headlineMedium,
            color = Gold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Platziere in jeder Reihe, jeder Spalte und jeder leuchtenden " +
                "Zone genau eine Fee – und keine zwei Feen dürfen sich berühren " +
                "(auch nicht diagonal), sonst stören sich ihre Zauberkräfte!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = fairyText(
                "Tippen: leer → ✕ (hier keine Fee) → $FAIRY_TOKEN Fee\n" +
                    "✨ Feenstaub: deckt ein sicheres Feld mit Fee auf\n" +
                    "🔮 Irrlicht: deckt ein sicheres Feld ohne Fee auf",
            ),
            inlineContent = fairyInlineContent(FairySpecies.Nebula, 20.sp),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary.copy(alpha = 0.9f),
            fontSize = 13.sp,
        )

        if (bestScore > 0) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Deine Bestleistung: $bestScore Punkte",
                style = MaterialTheme.typography.bodyLarge,
                color = LeafGreen,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(16.dp))

        GoldButton(label = "Den Wald betreten", onClick = onStart)
    }
}

/** Rätsel gelöst. */
@Composable
fun LevelUpOverlay(
    gained: Int,
    teaser: String,
    onContinue: () -> Unit,
    onShowLevelMap: () -> Unit,
) {
    OverlayScaffold(
        borderColor = Gold.copy(alpha = 0.6f),
        entrance = OverlayEntrance.PopIn,
        scrimAlpha = 0.8f,
    ) {
        val transition = rememberInfiniteTransition(label = "levelUpGlow")
        val glow by transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "levelUpGlowValue",
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.graphicsLayer { alpha = glow },
        ) {
            FairyImage(species = FairySpecies.Nebula, height = 58.dp)
            Text(text = "✨", fontSize = 34.sp)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "LEVEL UP!",
            style = MaterialTheme.typography.headlineLarge,
            color = Gold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Alle Feen leben in Harmonie!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "+$gained Punkte",
            style = MaterialTheme.typography.bodyLarge,
            color = LeafGreen,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = teaser,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary.copy(alpha = 0.85f),
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(16.dp))

        GoldButton(label = "Tiefer in den Wald →", onClick = onContinue)

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Zur Levelkarte",
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onShowLevelMap,
                )
                .padding(6.dp),
        )
    }
}

/** Welche Zauberhilfe bzw. welches Leben ein [GiftOverlay] gerade auffüllt. */
enum class GiftKind { FairyDust, Irrlicht, Life }

/**
 * Das Geschenk-Popup der ersten Level: So lange gibt es noch keine Werbung, ein
 * leerer Vorrat lässt sich stattdessen sofort per Antippen auffüllen. Danach
 * übernimmt an derselben Stelle das Werbevideo — der Knopf ist dann schon
 * vertraut.
 *
 * Wie lange „so lange" ist, steht in [GameViewModel.ADS_UNLOCK_AFTER_LEVEL] und
 * wird hier eingesetzt statt abgeschrieben. Hier stand bis zum 29. August „Bis
 * Level 10" — eine Zahl aus der Zeit vor der Zusammenführung, die auch dann noch
 * dastand, als die Geschenke längst nach dem dritten Level endeten. Wer bei
 * Level 3 sein letztes Geschenk bekam, las darüber, dass es bis Level 10 welche
 * gäbe.
 */
@Composable
fun GiftOverlay(kind: GiftKind, isLastGift: Boolean, onAccept: () -> Unit) {
    val bis = GameViewModel.ADS_UNLOCK_AFTER_LEVEL
    val (icon, body) = when (kind) {
        GiftKind.FairyDust -> "✨" to "Bis Level $bis schenke ich dir diesen Tipp! " +
            "Hier ist neuer Feenstaub – nimm ihn und rätsle weiter."
        GiftKind.Irrlicht -> "🔮" to "Bis Level $bis schenke ich dir dieses Irrlicht! " +
            "Es zeigt dir ein Feld, auf dem keine Fee sitzt."
        GiftKind.Life -> "💚" to "Bis Level $bis schenke ich dir dieses Leben! " +
            "Kopf hoch – weiter geht's."
    }

    OverlayScaffold(
        borderColor = Gold.copy(alpha = 0.5f),
        entrance = OverlayEntrance.PopIn,
        scrimAlpha = 0.82f,
    ) {
        Text(text = "🎁", fontSize = 40.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Ein Geschenk für dich!",
            style = MaterialTheme.typography.headlineMedium,
            color = Gold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "$icon $body",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        if (isLastGift) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Das war mein letztes Geschenk – ab jetzt bekommst du " +
                    "mit einem kurzen Video neue Hilfen.",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(16.dp))

        GoldButton(label = "Annehmen", onClick = onAccept)
    }
}

/** Zeit abgelaufen oder Leben verbraucht. */
@Composable
fun GameOverOverlay(
    reason: String,
    score: Int,
    level: Int,
    bestScore: Int,
    globalLives: GlobalLivesState,
    onRetry: () -> Unit,
    onShowLevelMap: () -> Unit,
    adsUnlocked: Boolean,
    adOffer: AdOffer,
    onWatchAd: () -> Unit,
    onOpenGift: () -> Unit,
) {
    OverlayScaffold(
        borderColor = Color(0xFFFF788C).copy(alpha = 0.55f),
        entrance = OverlayEntrance.RiseUp,
        scrimAlpha = 0.85f,
    ) {
        Text(text = "🥀", fontSize = 40.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Der Zauber verblasst…",
            style = MaterialTheme.typography.headlineMedium,
            color = DangerRose,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = reason,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Endstand: $score Punkte · Level $level",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        if (bestScore > 0) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (score >= bestScore) {
                    "Neue Bestleistung!"
                } else {
                    "Bestleistung: $bestScore"
                },
                style = MaterialTheme.typography.labelSmall,
                color = LeafGreen,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(10.dp))

        // Die Wald-Leben stehen hier, weil sich genau jetzt entscheidet, ob es
        // sofort weitergeht: Das verlorene Leben ist bereits abgezogen, ein
        // neuer Versuch kostet also nichts extra — solange überhaupt eines da
        // ist.
        Text(
            text = "💚".repeat(globalLives.lives) +
                "🖤".repeat((GlobalLives.MAX - globalLives.lives).coerceAtLeast(0)),
            fontSize = 15.sp,
        )

        Spacer(Modifier.height(16.dp))

        if (globalLives.lives > 0) {
            GoldButton(label = "Level neu starten", onClick = onRetry)
        } else if (adsUnlocked) {
            GoldButton(
                label = when (adOffer) {
                    AdOffer.Available -> "📺 Werbung ansehen (+1 Leben)"
                    AdOffer.Preparing -> "Werbung lädt…"
                    AdOffer.Unavailable -> "Werbung nicht verfügbar"
                },
                onClick = if (adOffer == AdOffer.Available) onWatchAd else ({}),
            )
        } else {
            // Vor der Werbe-Schwelle gibt es keine Werbung — ein leerer Vorrat
            // wartet nicht auf den Countdown, sondern lässt sich sofort per
            // Geschenk auffüllen.
            GoldButton(label = "🎁 Geschenk annehmen", onClick = onOpenGift)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "🗺️ Zur Karte",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary.copy(alpha = 0.85f),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onShowLevelMap,
                )
                .padding(8.dp),
        )
    }
}
