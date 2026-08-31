package ug.humb.fairydoku.ui.screens

/**
 * Die Rechnung hinter der Startposition der Levelkarte.
 *
 * Bewusst ohne Compose-Bezüge: So läuft sie als schneller JVM-Test, und die
 * Stelle mit dem Abzählfehler-Risiko — Knotenmitte gegen Knotenoberkante, Level
 * gegen Levelabstand — ist geprüft, statt nur im laufenden Spiel begutachtet.
 *
 * Alle Maße in derselben Einheit; im Spiel sind es Bildpunkte.
 */
internal object LevelPathLayout {

    /**
     * Die Mitte des Knotens für [level], gemessen vom oberen Rand des Pfades.
     *
     * Der erste Knoten sitzt eine halbe Knotenhöhe unter dem Rand, jeder
     * weitere [stepHeight] tiefer. Bis zur Mitte kommt eine weitere halbe
     * Knotenhöhe hinzu — zusammen also genau [nodeSize].
     */
    fun nodeCenter(level: Int, stepHeight: Float, nodeSize: Float): Float =
        stepHeight * (level - 1) + nodeSize

    /**
     * Der Scrollabstand, bei dem [level] mittig im sichtbaren Ausschnitt steht.
     *
     * Nach oben bei null abgeschnitten: Bei den ersten Leveln läge die
     * rechnerische Mitte über dem Pfadanfang, und die Karte begänne mit leerem
     * Raum. Nach unten begrenzt die Scrollfläche selbst — liegt das Level nah
     * am Ende des Pfades, rückt es so weit zur Mitte, wie der Pfad es hergibt.
     */
    fun scrollToCenter(
        level: Int,
        viewportHeight: Float,
        stepHeight: Float,
        nodeSize: Float,
    ): Float = (nodeCenter(level, stepHeight, nodeSize) - viewportHeight / 2f)
        .coerceAtLeast(0f)
}
