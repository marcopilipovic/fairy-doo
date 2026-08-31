package ug.humb.fairydoku.game

import ug.humb.fairydoku.game.model.FairydokuRules
import ug.humb.fairydoku.game.model.Pos
import ug.humb.fairydoku.game.model.Puzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FairydokuRulesTest {

    /**
     * Ein festes 4×4-Brett mit vier 2×2-Zonen — dasselbe Muster wie im
     * Design-Entwurf. Feste Zonen statt Generator, damit die Regelprüfung
     * unabhängig vom Zufall ist.
     *
     *   0 0 1 1
     *   0 0 1 1
     *   2 2 3 3
     *   2 2 3 3
     */
    private val puzzle = Puzzle(
        size = 4,
        regions = listOf(
            0, 0, 1, 1,
            0, 0, 1, 1,
            2, 2, 3, 3,
            2, 2, 3, 3,
        ),
        solution = setOf(Pos(0, 1), Pos(1, 3), Pos(2, 0), Pos(3, 2)),
    )

    @Test
    fun `die hinterlegte Loesung ist regelkonform`() {
        assertTrue(FairydokuRules.isSolved(puzzle, puzzle.solution))
        assertTrue(FairydokuRules.conflicts(puzzle, puzzle.solution).isEmpty())
    }

    @Test
    fun `zwei Feen in derselben Zeile stehen im Konflikt`() {
        val fairies = setOf(Pos(0, 0), Pos(0, 3))

        assertEquals(fairies, FairydokuRules.conflicts(puzzle, fairies))
    }

    @Test
    fun `zwei Feen in derselben Spalte stehen im Konflikt`() {
        val fairies = setOf(Pos(0, 2), Pos(3, 2))

        assertEquals(fairies, FairydokuRules.conflicts(puzzle, fairies))
    }

    @Test
    fun `zwei Feen in derselben Zone stehen im Konflikt`() {
        // (0,0) und (1,1) liegen beide in Zone 0 — und berühren sich zusätzlich.
        val fairies = setOf(Pos(0, 0), Pos(1, 1))

        assertEquals(fairies, FairydokuRules.conflicts(puzzle, fairies))
    }

    @Test
    fun `diagonal benachbarte Feen stoeren sich`() {
        // Verschiedene Zeile, Spalte und Zone — allein die Berührung zählt.
        val fairies = setOf(Pos(1, 1), Pos(2, 2))

        assertTrue(FairydokuRules.touches(Pos(1, 1), Pos(2, 2)))
        assertEquals(fairies, FairydokuRules.conflicts(puzzle, fairies))
    }

    @Test
    fun `entfernte Feen stoeren sich nicht`() {
        val fairies = setOf(Pos(0, 1), Pos(2, 0))

        assertTrue(FairydokuRules.conflicts(puzzle, fairies).isEmpty())
        assertFalse(FairydokuRules.isSolved(puzzle, fairies))
    }

    @Test
    fun `ein Feld beruehrt sich nicht selbst`() {
        assertFalse(FairydokuRules.touches(Pos(2, 2), Pos(2, 2)))
    }

    @Test
    fun `isSafe erkennt ein unbedenkliches Feld`() {
        val placed = setOf(Pos(0, 1))

        assertTrue(FairydokuRules.isSafe(puzzle, placed, Pos(2, 0)))
        assertFalse(FairydokuRules.isSafe(puzzle, placed, Pos(1, 0)))
    }

    @Test
    fun `vollstaendig aber regelwidrig gilt nicht als geloest`() {
        val fairies = setOf(Pos(0, 0), Pos(0, 1), Pos(0, 2), Pos(0, 3))

        assertFalse(FairydokuRules.isSolved(puzzle, fairies))
    }
}
