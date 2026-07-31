package com.fairydoo.game.game.model

/** Feld auf dem Gitter. */
data class Pos(val row: Int, val col: Int)

/** Was der Spieler auf ein Feld gesetzt hat. */
enum class CellMark {
    /** Leeres Waldfeld. */
    Empty,

    /** Hier sitzt eine Fee. */
    Fairy,

    /** Merkhilfe des Spielers: „hier kann keine Fee sitzen“. */
    Warded,
}

/**
 * Ein erzeugtes Rätsel.
 *
 * [regions] ordnet jedem Feld (zeilenweise indiziert) eine Zone zu —
 * Mondlicht-Lichtung, Pilzkreis, Flussbett. [solution] ist die eindeutige
 * Lösung; sie wird für Hinweise und zur Auswertung gebraucht, dem Spieler aber
 * nie gezeigt.
 */
data class Puzzle(
    val size: Int,
    val regions: List<Int>,
    val solution: Set<Pos>,
) {
    fun regionAt(pos: Pos): Int = regions[pos.row * size + pos.col]

    fun contains(pos: Pos): Boolean =
        pos.row in 0 until size && pos.col in 0 until size

    val allPositions: List<Pos>
        get() = (0 until size).flatMap { row -> (0 until size).map { col -> Pos(row, col) } }
}

/**
 * Die Regeln von Fairydoku — bewusst an einer Stelle gebündelt, damit sie
 * genau einmal definiert sind und Generator, Solver und Spielprüfung
 * dieselbe Wahrheit benutzen.
 *
 * Auf jedem Feld darf eine Fee sitzen, solange gilt:
 *  - genau eine Fee je Zeile,
 *  - genau eine Fee je Spalte,
 *  - genau eine Fee je Zone,
 *  - keine zwei Feen berühren sich, auch nicht diagonal
 *    (ihre Zauberkräfte würden sich stören).
 */
object FairydokuRules {

    /** Feen, die gegen mindestens eine Regel verstoßen. */
    fun conflicts(puzzle: Puzzle, fairies: Set<Pos>): Set<Pos> {
        val conflicting = mutableSetOf<Pos>()

        for (fairy in fairies) {
            for (other in fairies) {
                if (fairy == other) continue

                val sameLine = fairy.row == other.row || fairy.col == other.col
                val sameRegion = puzzle.regionAt(fairy) == puzzle.regionAt(other)
                if (sameLine || sameRegion || touches(fairy, other)) {
                    conflicting += fairy
                    conflicting += other
                }
            }
        }
        return conflicting
    }

    /** Grenzen die Felder aneinander — waagerecht, senkrecht oder diagonal? */
    fun touches(a: Pos, b: Pos): Boolean {
        if (a == b) return false
        val rowDistance = kotlin.math.abs(a.row - b.row)
        val colDistance = kotlin.math.abs(a.col - b.col)
        return rowDistance <= 1 && colDistance <= 1
    }

    /** Ist das Rätsel vollständig und regelkonform gelöst? */
    fun isSolved(puzzle: Puzzle, fairies: Set<Pos>): Boolean =
        fairies.size == puzzle.size &&
            conflicts(puzzle, fairies).isEmpty()

    /**
     * Darf an [pos] eine Fee gesetzt werden, ohne dass ein Konflikt entsteht?
     * Wird für Hinweise gebraucht, nicht zur Eingabeprüfung — falsche Züge
     * sind erlaubt und werden als Fehler gewertet.
     */
    fun isSafe(puzzle: Puzzle, fairies: Set<Pos>, pos: Pos): Boolean =
        conflicts(puzzle, fairies + pos).isEmpty()
}
