package com.fairydoo.game.ui.theme

import androidx.compose.ui.graphics.Color
import com.fairydoo.game.game.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Prüft, was die zehn Gebiete auseinanderhält.
 *
 * Der Graustufen-Test in Zahlen: Er nimmt jedem Gebiet die Farbe und sieht nach,
 * was übrig bleibt. Ein Ergebnis nimmt er vorweg — zehn Farben können auf einem
 * Schirm ohne Farbe nicht alle zehn verschiedene Helligkeiten haben, dafür ist
 * der Bereich zu schmal. Deshalb prüft er beides getrennt: dass die Farbtöne
 * weit genug auseinanderliegen, *und* dass jedes Gebiet ein eigenes Motiv trägt,
 * das ohne Farbe trägt.
 */
class ZoneStylesTest {

    @Test
    fun `es gibt ein Gebiet je Fee`() {
        // Zehn Feen, zehn Gebiete: So bekommt jede ein eigenes Zuhause, und auf
        // dem größten Brett muss sich keine Zone eine Farbe teilen.
        assertEquals(
            "Für jede Fee muss es ein Gebiet geben",
            com.fairydoo.game.game.FairySpecies.entries.size,
            ZoneStyles.size,
        )
        assertTrue(
            "Das größte Brett hat ${GameState.MAX_SIZE} Zonen, es gibt aber nur " +
                "${ZoneStyles.size} Gebiete",
            ZoneStyles.size >= GameState.MAX_SIZE,
        )
    }

    @Test
    fun `jedes Gebiet ist eigen - in Farbe, Motiv und Namen`() {
        assertEquals("Zwei Gebiete teilen sich eine Farbe", ZoneStyles.size, ZoneStyles.map { it.fill }.toSet().size)
        assertEquals("Zwei Gebiete teilen sich ein Motiv", ZoneStyles.size, ZoneStyles.map { it.texture }.toSet().size)
        assertEquals("Zwei Gebiete teilen sich einen Namen", ZoneStyles.size, ZoneStyles.map { it.name }.toSet().size)
    }

    @Test
    fun `keine zwei Gebiete sind zugleich in Farbton und Helligkeit nah`() {
        // Zwei Gebiete dürfen sich in *einem* Merkmal ähneln — Goldene Lichtung
        // und Sonnengarten liegen im Farbton fast gleich, unterscheiden sich
        // aber deutlich in der Helligkeit. Beides zugleich wäre eine
        // Verwechslung, die auch das schärfste Auge nicht auflöst.
        for (first in ZoneStyles.indices) {
            for (second in first + 1 until ZoneStyles.size) {
                val a = ZoneStyles[first]
                val b = ZoneStyles[second]
                if (setOf(a.name, b.name) in KNOWN_CLOSE_PAIRS) continue

                val hueGap = hueDistance(a.fill, b.fill)
                val lightGap = abs(luminance(a.fill) - luminance(b.fill))

                assertTrue(
                    "${a.name} und ${b.name} liegen zu nah beieinander: " +
                        "Farbton ${"%.0f".format(hueGap)}°, " +
                        "Helligkeit ${"%.3f".format(lightGap)}",
                    hueGap >= MIN_HUE_GAP || lightGap >= MIN_LIGHT_GAP,
                )
            }
        }
    }

    @Test
    fun `die farblich nahen Paare tragen deutlich verschiedene Motive`() {
        // Was in [KNOWN_CLOSE_PAIRS] steht, wird von der Farbe nicht mehr
        // getragen. Dann muss wenigstens das Motiv eindeutig sein — sonst wäre
        // die Ausnahme eine Lücke statt einer bewussten Entscheidung.
        for (pair in KNOWN_CLOSE_PAIRS) {
            val styles = ZoneStyles.filter { it.name in pair }
            assertEquals("Unbekanntes Gebiet in der Ausnahmeliste: $pair", 2, styles.size)
            assertTrue(
                "$pair unterscheidet sich weder in der Farbe noch im Motiv",
                styles[0].texture != styles[1].texture,
            )
        }
    }

    @Test
    fun `das Motiv hebt sich von seiner eigenen Flaeche ab`() {
        // Das Motiv trägt die halbe Unterscheidung. Läge sein Ton zu nah an der
        // Fläche, bliebe es unsichtbar — und mit ihm die Kodierung, die ohne
        // Farbe auskommt.
        for (style in ZoneStyles) {
            val visible = over(style.ink, style.fill)
            val gap = abs(luminance(visible) - luminance(style.fill))

            assertTrue(
                "Das Motiv von ${style.name} verschwindet in seiner Fläche " +
                    "(Abstand ${"%.3f".format(gap)})",
                gap >= MIN_INK_GAP,
            )
        }
    }

    @Test
    fun `die Heckengrenze ist auf jedem Gebiet zu sehen`() {
        // Die Grenze besteht aus grünen Blattbüscheln über einem dunklen Saum.
        // Auf einer hellen Fläche trägt die Hecke selbst, auf einem dunklen
        // Gebiet der Saum — und auf dem Tannenhain, der beinahe die Farbe der
        // Hecke hat, bleibt nur der Saum. Gefordert ist deshalb nicht, dass
        // beide auffallen, sondern dass mindestens eines von beiden es tut.
        for (style in ZoneStyles) {
            val hedge = contrastRatio(HedgeGreen, style.fill)
            val light = contrastRatio(HedgeLight, style.fill)
            val shade = contrastRatio(over(HedgeShade, style.fill), style.fill)

            assertTrue(
                "Die Heckengrenze verschwindet auf ${style.name}: Blätter " +
                    "${"%.2f".format(hedge)}:1, Lichtseite ${"%.2f".format(light)}:1, " +
                    "Saum ${"%.2f".format(shade)}:1",
                maxOf(hedge, light, shade) >= MIN_BORDER_CONTRAST,
            )
        }
    }

    /**
     * Die Gebiete in Graustufen — als Tabelle im Testbericht.
     *
     * Kein Prüfkriterium, sondern die Zahlen zum Nachsehen: Wer eine Farbe
     * ändert, erkennt hier sofort, wie sich Helligkeit und Farbton verschieben.
     */
    @Test
    fun `Farbton und Grauwert zum Nachschlagen`() {
        println("Die zehn Gebiete:")
        for (style in ZoneStyles) {
            println(
                "  %-16s %-15s Farbton %3.0f°  Grauwert %3d".format(
                    style.name,
                    style.texture,
                    hue(style.fill),
                    (luminance(style.fill) * 255).toInt(),
                ),
            )
        }
    }

    private companion object {
        /**
         * Gebiete, die sich farblich kaum unterscheiden und allein von ihren
         * Motiven getrennt werden.
         *
         * Herbstboden und Erdreich liegen dreizehn Grad im Farbton auseinander
         * und sind fast gleich hell — als Rostorange und Terracotta sind sie
         * beide erdig gedacht. Die Farben sind vorgegeben; diese Liste macht
         * die Folge sichtbar, statt den Grenzwert so weit zu senken, bis gar
         * nichts mehr auffällt.
         *
         * Sollte sie wachsen, ist das ein Warnzeichen: Dann trägt die Farbe die
         * Unterscheidung nicht mehr, und die Motive sind nicht mehr die zweite
         * Absicherung, sondern die einzige Stütze.
         */
        val KNOWN_CLOSE_PAIRS = setOf(setOf("Herbstboden", "Erdreich"))

        /** Ab diesem Winkel im Farbkreis sind zwei Töne klar verschieden. */
        const val MIN_HUE_GAP = 25f

        /** Ab hier trennt Helligkeit zwei Gebiete auch ohne Farbe. */
        const val MIN_LIGHT_GAP = 0.10f

        /** Ab hier zeichnet sich das Motiv gegen seine Fläche ab. */
        const val MIN_INK_GAP = 0.03f

        /**
         * Mindestkontrast der Zonengrenze.
         *
         * 3:1 nach WCAG 1.4.11 — der Wert für grafische Elemente. Die 4,5:1 aus
         * derselben Norm gelten für Text und wären hier der falsche Maßstab:
         * Eine Trennlinie muss auffallen, nicht gelesen werden.
         */
        const val MIN_BORDER_CONTRAST = 3f

        fun over(top: Color, bottom: Color): Color {
            val a = top.alpha
            return Color(
                red = top.red * a + bottom.red * (1 - a),
                green = top.green * a + bottom.green * (1 - a),
                blue = top.blue * a + bottom.blue * (1 - a),
            )
        }

        /** Relative Helligkeit nach WCAG — das, was in Graustufen übrig bleibt. */
        fun luminance(color: Color): Float {
            fun channel(value: Float): Float =
                if (value <= 0.03928f) {
                    value / 12.92f
                } else {
                    Math.pow(((value + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()
                }

            return 0.2126f * channel(color.red) +
                0.7152f * channel(color.green) +
                0.0722f * channel(color.blue)
        }

        fun contrastRatio(a: Color, b: Color): Float {
            val first = luminance(a) + 0.05f
            val second = luminance(b) + 0.05f
            return if (first > second) first / second else second / first
        }

        /** Lage im Farbkreis, 0–360°. */
        fun hue(color: Color): Float {
            val max = maxOf(color.red, color.green, color.blue)
            val min = minOf(color.red, color.green, color.blue)
            val span = max - min
            if (span < 0.0001f) return 0f

            val value = when (max) {
                color.red -> (color.green - color.blue) / span
                color.green -> 2f + (color.blue - color.red) / span
                else -> 4f + (color.red - color.green) / span
            }
            return ((value * 60f) + 360f) % 360f
        }

        /** Kürzester Weg zwischen zwei Farbtönen — der Kreis schließt sich. */
        fun hueDistance(a: Color, b: Color): Float {
            val gap = abs(hue(a) - hue(b))
            return minOf(gap, 360f - gap)
        }
    }
}
