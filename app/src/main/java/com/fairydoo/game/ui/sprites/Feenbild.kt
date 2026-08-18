package com.fairydoo.game.ui.sprites

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.fairydoo.game.game.FairySpecies

/**
 * Die zehn Feen.
 *
 * **Nachgebaut aus dem Entwurf „Feen schlicht" von Claude Design**, den Nataly
 * am 17. August übergeben hat — nicht als Bilddatei eingebunden, sondern nach
 * derselben Bauanleitung gezeichnet, die im README des Entwurfs steht:
 *
 *     Flügel   vier Ellipsen in der Aufhell-Farbe, oben .85, unten .7
 *     Kleid    Dreieck in der Hauptfarbe, Saum als Bogenband in Dunkel
 *     Arme     Striche in Hautfarbe, Strichbreite 7
 *     Gesicht  zwei Punkte (r 2.3) und ein Mundbogen — bewusst minimal
 *     Kontur   durchgehend #4a3326, Strichbreite 2.2
 *
 * **Warum gezeichnet und nicht als Datei.** Vorher lagen hier zehn PNGs,
 * entstanden aus Vorlagen, die Nataly im Netz gefunden hatte. Ob eines davon
 * einer fremden Zeichnung zu nahe kam, konnte niemand mehr sagen — „ich hab
 * keine Lust auf Stress". Die neuen Entwürfe haben dieses Problem nicht; sie
 * sind ohne Vorlage entstanden.
 *
 * Trotzdem bleiben sie im Quelltext statt in `res/drawable`, und zwar aus zwei
 * Gründen, die beide nichts mit Recht zu tun haben: Alle sechs anderen Apps
 * halten es so, und was gezeichnet wird, kann niemand aus der fertigen App
 * herauskopieren.
 *
 * **Der Entwurfsrahmen ist 120 × 164.** Alle Maße unten stehen genau so im
 * SVG; gezeichnet wird in dieses Koordinatenfeld und am Ende auf die
 * tatsächliche Größe skaliert. Dadurch lässt sich jede Zahl hier mit der
 * Vorlage vergleichen, ohne umzurechnen — und wenn Claude Design etwas
 * nachbessert, findet man die Stelle sofort wieder.
 */
@Composable
fun Feenbild(art: FairySpecies, modifier: Modifier = Modifier) {
    val farben = art.farben
    Canvas(modifier) { zeichneFee(farben) }
}

/**
 * Die Palette je Art — Zeile für Zeile aus der Tabelle des Entwurfs.
 *
 * Die zehn Zonenfarben sind dort schon aufeinander abgestimmt. Genau daran
 * hatte es bei den alten Feen gefehlt: Jede war für sich entstanden, und eine
 * fiel heraus. Nataly: „Die mit dem Orangeroten hat mich sowieso geärgert,
 * weil sie überhaupt nicht zu den Farben passt."
 */
private class Feenfarben(
    val haupt: Color,
    val dunkel: Color,
    val hell: Color,
    val haar: Color,
)

private val FairySpecies.farben: Feenfarben
    get() = when (this) {
        FairySpecies.Nixie -> Feenfarben(Color(0xFF2F9C9C), Color(0xFF1F7A7A), Color(0xFF7FD6D0), Color(0xFF3C2A20))
        FairySpecies.Aura -> Feenfarben(Color(0xFFE8B93A), Color(0xFFBF8F1E), Color(0xFFFFE08A), Color(0xFFA9763F))
        FairySpecies.Flora -> Feenfarben(Color(0xFF6FAA4F), Color(0xFF4D8036), Color(0xFFBCE39B), Color(0xFF6B4A2E))
        FairySpecies.Ignis -> Feenfarben(Color(0xFFE28A42), Color(0xFFB96524), Color(0xFFFFC48E), Color(0xFFC98A4A))
        FairySpecies.Salta -> Feenfarben(Color(0xFFE37A9C), Color(0xFFBB5678), Color(0xFFFFB9CF), Color(0xFF4A3226))
        FairySpecies.Trixie -> Feenfarben(Color(0xFF8A6AC4), Color(0xFF664A9C), Color(0xFFC9B4EC), Color(0xFF5A3F6A))
        FairySpecies.Terra -> Feenfarben(Color(0xFFD4657A), Color(0xFFA94257), Color(0xFFF5AAB6), Color(0xFF8A4A4A))
        FairySpecies.Zephyr -> Feenfarben(Color(0xFF4A8AC4), Color(0xFF2F6699), Color(0xFFA5CDEC), Color(0xFF3F4A6A))
        FairySpecies.Chrono -> Feenfarben(Color(0xFF8AC7D8), Color(0xFF5E9CB0), Color(0xFFD3EEF5), Color(0xFF7A6A5A))
        FairySpecies.Nebula -> Feenfarben(Color(0xFF46579C), Color(0xFF2C3A72), Color(0xFF93A2D8), Color(0xFF2E2A3A))
    }

private val KONTUR = Color(0xFF4A3326)
private val HAUT = Color(0xFFF7DCC4)
private val BEIN = Color(0xFFE8C1A2)

/** Der Entwurfsrahmen aus dem SVG. Alle Maße unten beziehen sich darauf. */
private const val BREITE = 120f
private const val HOEHE = 164f

private fun DrawScope.zeichneFee(f: Feenfarben) {
    // Auf den Entwurfsrahmen bringen und mittig einpassen: Die Figur ist
    // hochkant, das Feld auf dem Brett quadratisch.
    val faktor = minOf(size.width / BREITE, size.height / HOEHE)
    val links = (size.width - BREITE * faktor) / 2f
    val oben = (size.height - HOEHE * faktor) / 2f

    // **Die Konturbreite wird nicht mitskaliert.** Der Entwurf sagt es
    // ausdrücklich: „bei Skalierung nicht mitskalieren lassen, sonst wirkt der
    // Spielstein zu fett". Auf einem 40 Punkt breiten Feld wäre eine
    // mitskalierte Kontur ein Drittel des Gesichts.
    val kontur = Stroke(
        width = (2.2f * faktor).coerceIn(1.2f, 2.6f),
        join = StrokeJoin.Round,
        cap = StrokeCap.Round,
    )

    translate(links, oben) {
        scale(faktor, faktor, pivot = Offset.Zero) {
            // ---- Flügel: vier Ellipsen, oben größer als unten ----
            fluegel(f.hell.copy(alpha = 0.85f), 34f, 52f, 20f, 27f, -18f, kontur)
            fluegel(f.hell.copy(alpha = 0.85f), 86f, 52f, 20f, 27f, 18f, kontur)
            fluegel(f.hell.copy(alpha = 0.70f), 38f, 86f, 14f, 19f, -30f, kontur)
            fluegel(f.hell.copy(alpha = 0.70f), 82f, 86f, 14f, 19f, 30f, kontur)

            // ---- Beine ----
            strich(BEIN, 7f) {
                moveTo(54f, 108f); lineTo(50f, 140f); lineTo(46f, 150f)
            }
            strich(BEIN, 7f) {
                moveTo(66f, 108f); lineTo(72f, 138f); lineTo(78f, 147f)
            }

            // ---- Kleid: Dreieck, darunter das Saumband ----
            val kleid = Path().apply {
                moveTo(60f, 62f); lineTo(44f, 110f)
                quadraticBezierTo(60f, 118f, 76f, 110f); close()
            }
            drawPath(kleid, f.haupt)
            drawPath(kleid, KONTUR, style = kontur)

            val saum = Path().apply {
                moveTo(44f, 110f)
                quadraticBezierTo(52f, 104f, 60f, 111f)
                quadraticBezierTo(68f, 104f, 76f, 110f)
                quadraticBezierTo(60f, 118f, 44f, 110f)
                close()
            }
            drawPath(saum, f.dunkel)
            drawPath(saum, KONTUR, style = kontur)

            // ---- Arme ----
            strich(HAUT, 7f) { moveTo(50f, 70f); lineTo(34f, 84f) }
            strich(HAUT, 7f) { moveTo(70f, 70f); lineTo(88f, 82f) }

            // ---- Kopf, Haar, Ohren ----
            drawCircle(HAUT, radius = 17f, center = Offset(60f, 44f))
            drawCircle(KONTUR, radius = 17f, center = Offset(60f, 44f), style = kontur)

            val haar = Path().apply {
                moveTo(45f, 40f)
                quadraticBezierTo(48f, 24f, 60f, 24f)
                quadraticBezierTo(72f, 24f, 75f, 40f)
                quadraticBezierTo(68f, 32f, 60f, 33f)
                quadraticBezierTo(52f, 32f, 45f, 40f)
                close()
            }
            drawPath(haar, f.haar)
            drawPath(haar, KONTUR, style = kontur)

            ohr(43f, 44f, 37f, 40f, 43f, 38f, kontur)
            ohr(77f, 44f, 83f, 40f, 77f, 38f, kontur)

            // ---- Gesicht: zwei Punkte und ein Bogen. Mehr nicht. ----
            drawCircle(KONTUR, radius = 2.3f, center = Offset(54f, 46f))
            drawCircle(KONTUR, radius = 2.3f, center = Offset(66f, 46f))
            val mund = Path().apply {
                moveTo(56f, 53f); quadraticBezierTo(60f, 56f, 64f, 53f)
            }
            drawPath(
                mund,
                KONTUR,
                style = Stroke(width = 1.8f, cap = StrokeCap.Round),
            )
        }
    }
}

private fun DrawScope.fluegel(
    farbe: Color,
    x: Float,
    y: Float,
    rx: Float,
    ry: Float,
    winkel: Float,
    kontur: Stroke,
) = rotate(winkel, Offset(x, y)) {
    val kasten = Rect(Offset(x - rx, y - ry), Size(rx * 2, ry * 2))
    val weg = Path().apply { addOval(kasten) }
    drawPath(weg, farbe)
    drawPath(weg, KONTUR, style = kontur)
}

private fun DrawScope.strich(farbe: Color, breite: Float, bau: Path.() -> Unit) {
    drawPath(
        Path().apply(bau),
        farbe,
        style = Stroke(width = breite, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

private fun DrawScope.ohr(
    x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float,
    kontur: Stroke,
) {
    val weg = Path().apply {
        moveTo(x1, y1); lineTo(x2, y2); lineTo(x3, y3); close()
    }
    drawPath(weg, HAUT)
    drawPath(weg, KONTUR, style = kontur)
}

/** Nur für den Zwischenspeicher: die Zeichnung ohne Compose-Umgebung. */
internal fun DrawScope.feeFuerProbe(art: FairySpecies) = zeichneFee(art.farben)
