package com.fairydoo.game.ui.sprites

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.fairydoo.game.game.FairySpecies

/**
 * Die zehn Feen — gezeichnet statt geladen.
 *
 * **Warum das sein musste.** Die bisherigen Feen waren Bilddateien, entstanden
 * aus Vorlagen, die Nataly seinerzeit im Netz gefunden und weitergegeben
 * hatte. Ob eines der Ergebnisse einer bestimmten fremden Zeichnung zu nahe
 * kommt, konnte niemand mehr sagen — und genau das wollte sie nicht:
 *
 * > „Ich hab keine Lust auf Stress. Wenn ich dieses Spiel veröffentliche und
 * > einer kackt mich an, weil die Fee so ähnlich aussieht wie seine, die er
 * > vor fünfzig Jahren mal gezeichnet hat, da hab ich keine Lust drauf."
 *
 * Fairydoku war die **einzige** der sieben Apps mit echten Bilddateien. In
 * allen übrigen sind die Figuren im Quelltext gezeichnet — die Hunde, die
 * Molchfürsten, die Brut, die Begleiter. Diese Datei bringt Fairydoku auf
 * denselben Stand. Damit ist die Herkunftsfrage erledigt, und herauskopieren
 * kann die Figuren auch niemand mehr.
 *
 * **Wie sie aussehen sollen.** Natalys Vorgabe, und sie ist die richtige:
 *
 * > „Vielleicht kann man das so zeichnen, dass es nicht so ins Detaillierte
 * > geht, sondern dass man sieht: Da ist eine Fee, und die ist halt jetzt
 * > nicht pink, sondern blau."
 *
 * Das ist mehr als ein Geschmacksurteil. **Je schlichter eine Figur ist, desto
 * weniger kann sie überhaupt jemandes Zeichnung ähneln** — an einer Silhouette
 * aus Kreis, Tropfen und zwei Flügeln ist nichts, was jemand für sich
 * beanspruchen könnte. Und für ein Spiel, in dem zehn Sorten auf einem engen
 * Brett auseinanderzuhalten sein müssen, ist es ohnehin die bessere Lösung:
 * Was zählt, ist die Farbe auf einen Blick, nicht das Gesicht.
 *
 * Nataly hatte an einer der alten Feen ohnehin etwas auszusetzen — „die mit
 * dem Orangeroten hat mich sowieso geärgert, weil sie überhaupt nicht zu den
 * Farben passt". Mit einer gemeinsamen Zeichenvorschrift kann das nicht mehr
 * vorkommen: Alle zehn sind dieselbe Figur in zehn Farben.
 */
@Composable
fun Feenbild(art: FairySpecies, modifier: Modifier = Modifier) {
    val farbe = art.farbe
    Canvas(modifier) { zeichneFee(farbe) }
}

/**
 * Die Farbe je Art.
 *
 * **Zehn Farbtöne, die sich auf einem kleinen Feld unterscheiden lassen.**
 * Nicht zehn beliebige: Sie sind über den Farbkreis verteilt und alle
 * ähnlich hell und satt, damit keine heraussticht oder verschwindet. Genau
 * das war der Vorwurf an die alte orangerote Fee — sie passte nicht zu den
 * übrigen, weil jede für sich entstanden war.
 */
private val FairySpecies.farbe: Color
    get() = when (this) {
        FairySpecies.Flora -> Color(0xFF7BC96F)
        FairySpecies.Nebula -> Color(0xFF9B7BD4)
        FairySpecies.Salta -> Color(0xFFE86FA8)
        FairySpecies.Aura -> Color(0xFFF2C14E)
        FairySpecies.Nixie -> Color(0xFF4FB8D9)
        FairySpecies.Zephyr -> Color(0xFF8FD3C7)
        FairySpecies.Ignis -> Color(0xFFE8734A)
        FairySpecies.Terra -> Color(0xFFB08968)
        FairySpecies.Chrono -> Color(0xFF6C7BD4)
        FairySpecies.Trixie -> Color(0xFFD46FD4)
    }

/**
 * Die Figur selbst — für alle zehn dieselbe.
 *
 * Kopf, Körper, zwei Flügel, ein Lichtschein. Mehr nicht, und das ist Absicht.
 * Kein Gesicht: Zwei Augen und ein Mund wären der erste Schritt zurück ins
 * Detail — und der Punkt, an dem eine Figur anfängt, einer bestimmten anderen
 * zu ähneln.
 */
private fun DrawScope.zeichneFee(farbe: Color) {
    val b = size.minDimension
    val mitte = Offset(size.width / 2f, size.height / 2f)

    // Der Schein dahinter. Er gibt der Figur Halt auf dunklem Grund und macht
    // die Farbe schon auf Abstand erkennbar — bei einem Brett voller kleiner
    // Felder ist das der eigentliche Zweck.
    drawCircle(
        Brush.radialGradient(
            listOf(farbe.copy(alpha = 0.38f), Color.Transparent),
            center = mitte,
            radius = b * 0.5f,
        ),
        radius = b * 0.5f,
        center = mitte,
    )

    val fluegel = farbe.copy(alpha = 0.55f)
    val fluegelkante = farbe.copy(alpha = 0.9f)

    // **Die Flügel: je ein Blatt links und rechts, gespiegelt.**
    //
    // Ein Blatt statt eines Schmetterlingsflügels, weil ein Blatt mit drei
    // Punkten auskommt. Was sich mit drei Punkten beschreiben lässt, ist keine
    // Zeichnung, die jemandem gehört.
    for (seite in listOf(-1f, 1f)) {
        val weg = Path().apply {
            moveTo(mitte.x, mitte.y + b * 0.02f)
            quadraticBezierTo(
                mitte.x + seite * b * 0.46f, mitte.y - b * 0.34f,
                mitte.x + seite * b * 0.30f, mitte.y - b * 0.02f,
            )
            quadraticBezierTo(
                mitte.x + seite * b * 0.24f, mitte.y + b * 0.20f,
                mitte.x, mitte.y + b * 0.02f,
            )
            close()
        }
        drawPath(weg, fluegel, style = Fill)
        drawPath(weg, fluegelkante, style = Stroke(width = b * 0.012f))
    }

    // Der Körper: ein Tropfen, unten spitz. Zwei Kurven, sonst nichts.
    val koerper = Path().apply {
        moveTo(mitte.x, mitte.y + b * 0.30f)
        quadraticBezierTo(
            mitte.x - b * 0.13f, mitte.y + b * 0.06f,
            mitte.x - b * 0.08f, mitte.y - b * 0.06f,
        )
        lineTo(mitte.x + b * 0.08f, mitte.y - b * 0.06f)
        quadraticBezierTo(
            mitte.x + b * 0.13f, mitte.y + b * 0.06f,
            mitte.x, mitte.y + b * 0.30f,
        )
        close()
    }
    drawPath(
        koerper,
        Brush.verticalGradient(
            listOf(farbe, farbe.copy(alpha = 0.75f)),
            startY = mitte.y - b * 0.06f,
            endY = mitte.y + b * 0.30f,
        ),
    )

    // Der Kopf: ein Kreis. Ohne Gesicht.
    drawCircle(farbe, radius = b * 0.115f, center = Offset(mitte.x, mitte.y - b * 0.17f))
    // Ein heller Punkt oben links gibt ihm Rundung — dasselbe Mittel wie bei
    // den Steinen im Weltraumspiel.
    drawCircle(
        Color.White.copy(alpha = 0.5f),
        radius = b * 0.035f,
        center = Offset(mitte.x - b * 0.04f, mitte.y - b * 0.21f),
    )

    // Drei Funken darüber. Sie sagen „Fee" deutlicher als jedes Gesicht.
    val funken = listOf(
        Offset(mitte.x - b * 0.20f, mitte.y - b * 0.30f) to b * 0.020f,
        Offset(mitte.x + b * 0.17f, mitte.y - b * 0.35f) to b * 0.028f,
        Offset(mitte.x + b * 0.26f, mitte.y - b * 0.18f) to b * 0.016f,
    )
    funken.forEach { (wo, r) ->
        drawCircle(Color.White.copy(alpha = 0.85f), radius = r, center = wo)
    }
}

/** Nur für die Bildprobe: die Zeichnung ohne Compose-Umgebung. */
internal fun DrawScope.feeFuerProbe(art: FairySpecies) = zeichneFee(art.farbe)

/** Die Größe, in der die Figur entworfen wurde — für Proben und Vorschauen. */
internal val ENTWURFSMASS = Size(256f, 256f)
