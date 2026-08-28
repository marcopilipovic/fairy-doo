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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairydoo.game.game.FairyDustSupply
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.GlobalLives
import com.fairydoo.game.game.IrrlichtSupply
import com.fairydoo.game.ui.sprites.FairyImage
import com.fairydoo.game.ui.theme.ConflictRed
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldCream
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.PanelBottom
import com.fairydoo.game.ui.theme.PanelTop
import com.fairydoo.game.ui.theme.RegionColors
import com.fairydoo.game.ui.theme.StatusPurple
import com.fairydoo.game.ui.theme.TextPrimary

/**
 * Die Anleitung: fünf Schritte, die Regeln und Fähigkeiten an kleinen
 * Beispielen statt an Fließtext erklären. Erscheint von selbst beim
 * allerersten Start und ist über den ❔-Knopf jederzeit wieder erreichbar.
 */
@Composable
fun TutorialOverlay(
    step: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    OverlayScaffold(
        borderColor = Gold.copy(alpha = 0.5f),
        entrance = OverlayEntrance.RiseUp,
        scrimAlpha = 0.85f,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Schließen ✕",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = StatusPurple,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSkip,
                    )
                    .padding(4.dp),
            )
        }

        Spacer(Modifier.height(4.dp))

        when (step) {
            0 -> TutorialRulesStep()
            1 -> TutorialTouchStep()
            2 -> TutorialTapHoldStep()
            3 -> TutorialPowerUpsStep()
            else -> TutorialLivesStep()
        }

        Spacer(Modifier.height(16.dp))

        GoldButton(
            label = if (step < totalSteps - 1) "Weiter" else "Den Wald betreten",
            onClick = onNext,
        )
    }
}

@Composable
private fun TutorialHeadline(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontSize = 19.sp,
        color = Gold,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun TutorialCaption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 12.sp,
        color = TextPrimary.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
    )
}

/** Schritt 1: In jeder Reihe, Spalte und Zone genau eine Fee. */
@Composable
private fun TutorialRulesStep() {
    TutorialHeadline("✨ Willkommen, Hüter:in ✨")

    Text(
        text = buildAnnotatedString {
            append("In jeder ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Reihe") }
            append(", jeder ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Spalte") }
            append(" und jeder ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("leuchtenden Zone") }
            append(" darf genau ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("eine Fee") }
            append(" wohnen.")
        },
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 13.5.sp,
        color = TextPrimary,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(12.dp))
    RuleDemoGrid()
    Spacer(Modifier.height(12.dp))

    TutorialCaption("Die markierte Reihe, Spalte und Zone der Fee bleiben sonst leer (✕ = hier verboten).")
}

/**
 * Ein 4×4-Beispielgitter: eine Fee bei (Zeile 1, Spalte 2), ihre Zone im
 * selben 2×2-Raster wie das echte Spielfeld — die Zonenfarben sind dieselben
 * wie im Spiel, damit die Anleitung wie ein Ausschnitt daraus wirkt, nicht wie
 * eine separate Erklärgrafik.
 */
@Composable
private fun RuleDemoGrid() {
    val fairyRow = 1
    val fairyCol = 2
    val fairyZone = (fairyRow / 2) * 2 + (fairyCol / 2)

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (r in 0 until 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (c in 0 until 4) {
                    val zone = (r / 2) * 2 + (c / 2)
                    val isFairy = r == fairyRow && c == fairyCol
                    val marked = !isFairy && (r == fairyRow || c == fairyCol || zone == fairyZone)
                    DemoCell(
                        size = 34.dp,
                        glyph = if (marked) "✕" else "",
                        fairy = if (isFairy) DEMO_FAIRY else null,
                        background = RegionColors[zone % RegionColors.size].copy(alpha = 0.22f),
                        borderColor = if (isFairy) GoldLight else Color.White.copy(alpha = 0.15f),
                        borderWidth = if (isFairy) 2.dp else 1.dp,
                        glyphSize = 13.sp,
                    )
                }
            }
        }
    }
}

/** Schritt 2: Zwei Feen dürfen sich nicht einmal diagonal berühren. */
@Composable
private fun TutorialTouchStep() {
    TutorialHeadline("Die Berührungsregel")

    Text(
        text = buildAnnotatedString {
            append("Zwei Feen dürfen sich nicht berühren – ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("auch nicht diagonal") }
            append(", sonst stören sich ihre Zauberkräfte!")
        },
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 13.5.sp,
        color = TextPrimary,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        for (r in 0 until 3) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (c in 0 until 3) {
                    val isFairy = r == 1 && c == 1
                    DemoCell(
                        size = 40.dp,
                        glyph = if (isFairy) "" else "✕",
                        fairy = if (isFairy) DEMO_FAIRY else null,
                        background = if (isFairy) GoldLight.copy(alpha = 0.12f) else ConflictRed.copy(alpha = 0.18f),
                        borderColor = if (isFairy) GoldLight else ConflictRed.copy(alpha = 0.6f),
                        borderWidth = if (isFairy) 2.dp else 1.dp,
                        glyphSize = 15.sp,
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    TutorialCaption("Alle 8 Nachbarfelder der Fee bleiben leer.")
}

/** Schritt 3: kurz antippen markiert, halten ruft die Fee herbei. */
@Composable
private fun TutorialTapHoldStep() {
    TutorialHeadline("Antippen & Halten")

    Text(
        text = buildAnnotatedString {
            append("Kurz antippen markiert eine Notiz, ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Halten") }
            append(" ruft die Fee herbei:")
        },
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 13.5.sp,
        color = TextPrimary,
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DemoCell(
            size = 44.dp,
            glyph = "leer",
            background = Color.White.copy(alpha = 0.08f),
            borderColor = Color.White.copy(alpha = 0.25f),
            borderWidth = 1.5.dp,
            glyphSize = 11.sp,
        )
        GestureArrow("antippen")
        DemoCell(
            size = 44.dp,
            glyph = "✕",
            background = Color.White.copy(alpha = 0.08f),
            borderColor = Color.White.copy(alpha = 0.25f),
            borderWidth = 1.5.dp,
            glyphSize = 20.sp,
        )
        GestureArrow("halten")
        DemoCell(
            size = 44.dp,
            glyph = "",
            fairy = DEMO_FAIRY,
            background = Color.White.copy(alpha = 0.08f),
            borderColor = Color.White.copy(alpha = 0.25f),
            borderWidth = 1.5.dp,
            glyphSize = 24.sp,
        )
    }

    Spacer(Modifier.height(12.dp))
    TutorialCaption("✕ heißt „hier wohnt sicher keine Fee“. Feld erneut halten, um die Fee wieder zu entfernen.")
}

@Composable
private fun GestureArrow(label: String) {
    Text(
        text = "$label\n→",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 12.sp,
        color = StatusPurple,
        textAlign = TextAlign.Center,
        lineHeight = 15.sp,
    )
}

/** Schritt 4: die zwei Zauberhilfen. */
@Composable
private fun TutorialPowerUpsStep() {
    TutorialHeadline("Deine Zauberhilfen")

    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        MiniPowerTile("✨", "Feenstaub")
        MiniPowerTile("🔮", "Irrlicht")
    }

    Spacer(Modifier.height(12.dp))

    Text(
        text = buildAnnotatedString {
            append("✨ ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Feenstaub") }
            append(": deckt ein sicheres Feld mit Fee auf.\n")
            append("🔮 ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Irrlicht") }
            append(": deckt ein sicheres Feld ohne Fee auf.\n")
            append("Du hast ${FairyDustSupply.max} Feenstaub und ${IrrlichtSupply.max} Irrlicht — ")
            append("verbrauchte wachsen in zwei Stunden nach.")
        },
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 12.5.sp,
        color = TextPrimary.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun MiniPowerTile(glyph: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.verticalGradient(listOf(PanelTop, PanelBottom)),
                    shape = RoundedCornerShape(18.dp),
                )
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = glyph, fontSize = 24.sp)
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 11.sp,
            color = GoldCream,
            textAlign = TextAlign.Center,
        )
    }
}

/** Schritt 5: Level-Leben (pro Runde) gegen Wald-Leben (global, wachsen nach). */
@Composable
private fun TutorialLivesStep() {
    TutorialHeadline("Zwei Arten von Leben")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LivesInfoBox(
            icon = "🍃".repeat(GameState.MAX_LIVES),
            bold = "Level-Leben",
            rest = ": Fehlversuche in diesem Level. Bei 0 ist das Level vorbei.",
        )
        LivesInfoBox(
            icon = "💚".repeat(GlobalLives.MAX),
            bold = "Wald-Leben",
            rest = ": global, kostet 1 pro verlorenem Level. " +
                "Wächst alle ${GlobalLives.REGEN_INTERVAL_MILLIS / 60_000} Min. nach.",
        )
    }

    Spacer(Modifier.height(12.dp))
    TutorialCaption("Abgeschlossene Level kannst du jederzeit erneut spielen.")
}

@Composable
private fun LivesInfoBox(icon: String, bold: String, rest: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(Modifier.height(3.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
                append(rest)
            },
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 12.sp,
            color = TextPrimary.copy(alpha = 0.9f),
        )
    }
}

/**
 * Die Fee, die in allen Beispielfeldern der Anleitung sitzt.
 *
 * Überall dieselbe, obwohl auf dem Brett je Zone eine andere lebt: Hier geht es
 * um die Regel, nicht um die Figuren. Wechselnde Feen ließen die drei Schritte
 * so aussehen, als wäre die Art Teil der Erklärung.
 *
 * Nixie, weil sie die hellste der zehn ist. Die Beispielfelder sind mit 34 bis
 * 44 dp deutlich kleiner als eine echte Spielzelle — auf dieser Größe entscheidet
 * allein der Helligkeitsunterschied, ob man die Figur noch als Fee erkennt.
 * Nebula stand hier zuerst und verschwand mit ihrem Nachthimmel-Kleid im dunklen
 * Feld.
 */
private val DEMO_FAIRY = FairySpecies.Nixie

/** Ein einzelnes Beispielfeld — Baustein aller Mini-Gitter in dieser Anleitung. */
@Composable
private fun DemoCell(
    size: androidx.compose.ui.unit.Dp,
    glyph: String,
    background: Color,
    borderColor: Color,
    borderWidth: androidx.compose.ui.unit.Dp,
    glyphSize: androidx.compose.ui.unit.TextUnit,
    // Statt eines Schriftzeichens: dieselbe Illustration wie auf dem Brett.
    // Die Anleitung soll zeigen, was gleich zu sehen ist — ein Emoji, das das
    // Gerät in seiner eigenen Schrift zeichnet, zeigt etwas anderes.
    fairy: FairySpecies? = null,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (fairy != null) {
            // Höher als der Anteil auf dem Spielbrett: Dort ist eine Zelle ein
            // Vielfaches davon groß, hier bliebe von der Figur sonst zu wenig
            // übrig. Ein schmaler Rand bleibt, damit sie nicht am Rahmen klebt.
            FairyImage(species = fairy, height = size * 0.84f)
        } else {
            Text(text = glyph, fontSize = glyphSize, color = GoldLight, textAlign = TextAlign.Center)
        }
    }
}
