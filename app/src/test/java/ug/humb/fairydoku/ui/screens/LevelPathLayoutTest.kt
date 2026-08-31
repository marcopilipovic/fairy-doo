package ug.humb.fairydoku.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft, wo die Levelkarte aufschlägt.
 *
 * Ob die Karte *schön* einrastet, kann kein Test beantworten — ob das richtige
 * Level in der Mitte steht, schon. Die Fehlerquelle ist das Abzählen:
 * Knotenmitte gegen Knotenoberkante und Level gegen Levelabstand. Ein Fehler um
 * einen Schritt fiele im Spiel kaum auf, wäre aber genau daneben.
 */
class LevelPathLayoutTest {

    // Dieselben Verhältnisse wie im Spiel, nur in runden Zahlen: Schritt 92,
    // Knoten 54, sichtbarer Ausschnitt 600.
    private val step = 92f
    private val node = 54f
    private val viewport = 600f

    @Test
    fun `der erste Knoten liegt eine halbe Knotenhoehe unter dem Rand`() {
        // Knotenoberkante ist node/2, die Mitte eine weitere halbe darunter.
        assertEquals(node, LevelPathLayout.nodeCenter(1, step, node), 0.001f)
    }

    @Test
    fun `jedes weitere Level liegt genau einen Schritt tiefer`() {
        val ersteMitte = LevelPathLayout.nodeCenter(1, step, node)
        val zehnteMitte = LevelPathLayout.nodeCenter(10, step, node)

        assertEquals(step * 9, zehnteMitte - ersteMitte, 0.001f)
    }

    @Test
    fun `das gewaehlte Level steht danach mittig im Ausschnitt`() {
        val level = 10
        val scroll = LevelPathLayout.scrollToCenter(level, viewport, step, node)

        // Wo landet die Knotenmitte auf dem Bildschirm, wenn so weit gescrollt
        // wurde? Genau in der Mitte des Ausschnitts.
        val aufDemSchirm = LevelPathLayout.nodeCenter(level, step, node) - scroll

        assertEquals(viewport / 2f, aufDemSchirm, 0.001f)
    }

    @Test
    fun `die ersten Level scrollen nicht ueber den Pfadanfang hinaus`() {
        // Level 1 liegt weit oberhalb der halben Ausschnitthöhe — die Rechnung
        // ergäbe einen negativen Wert. Die Karte soll aber nicht mit leerem
        // Raum über dem Pfad beginnen.
        for (level in 1..3) {
            val scroll = LevelPathLayout.scrollToCenter(level, viewport, step, node)
            assertTrue("Level $level scrollt nach oben aus dem Pfad: $scroll", scroll >= 0f)
        }

        assertEquals(0f, LevelPathLayout.scrollToCenter(1, viewport, step, node), 0.001f)
    }

    @Test
    fun `ab der Ausschnittmitte waechst der Scrollabstand mit jedem Level`() {
        // Das erste Level, das tief genug liegt, um überhaupt zentriert werden
        // zu können — ab hier muss jeder Schritt den Abstand um genau einen
        // Levelabstand vergrößern.
        val ersteZentrierbar = generateSequence(1) { it + 1 }
            .first { LevelPathLayout.scrollToCenter(it, viewport, step, node) > 0f }

        val a = LevelPathLayout.scrollToCenter(ersteZentrierbar + 1, viewport, step, node)
        val b = LevelPathLayout.scrollToCenter(ersteZentrierbar + 2, viewport, step, node)

        assertEquals(step, b - a, 0.001f)
    }

    @Test
    fun `ein hoher Ausschnitt braucht weniger Scrollabstand als ein niedriger`() {
        val hoch = LevelPathLayout.scrollToCenter(20, 1200f, step, node)
        val niedrig = LevelPathLayout.scrollToCenter(20, 600f, step, node)

        assertEquals(300f, niedrig - hoch, 0.001f)
    }
}
