package ug.humb.fairydoku.game

import ug.humb.fairydoku.game.model.FairydokuRules
import ug.humb.fairydoku.game.model.Pos
import ug.humb.fairydoku.game.model.PuzzleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Der Generator ist die Stelle, an der ein Fehler am teuersten wäre: Ein
 * unlösbares oder mehrdeutiges Rätsel merkt der Spieler erst nach Minuten
 * vergeblichen Grübelns. Deshalb wird über alle Gittergrößen geprüft.
 */
class PuzzleGeneratorTest {

    @Test
    fun `erzeugt fuer jede Groesse ein regelkonformes Raetsel`() {
        for (size in PuzzleGenerator.MIN_SIZE..9) {
            val puzzle = PuzzleGenerator.generate(size, Random(size.toLong()))

            assertEquals("Größe $size", size, puzzle.size)
            assertEquals("Größe $size: Feenzahl", size, puzzle.solution.size)
            assertTrue(
                "Größe $size: hinterlegte Lösung verletzt die Regeln",
                FairydokuRules.isSolved(puzzle, puzzle.solution),
            )
        }
    }

    @Test
    fun `jede Zone enthaelt genau eine Fee`() {
        for (size in PuzzleGenerator.MIN_SIZE..9) {
            val puzzle = PuzzleGenerator.generate(size, Random(size * 31L))

            val regionsOfFairies = puzzle.solution.map { puzzle.regionAt(it) }
            assertEquals(
                "Größe $size: Zonen mehrfach oder gar nicht belegt",
                size,
                regionsOfFairies.toSet().size,
            )
        }
    }

    @Test
    fun `jedes Feld gehoert zu genau einer Zone und alle Zonen existieren`() {
        val puzzle = PuzzleGenerator.generate(6, Random(99))

        assertEquals(36, puzzle.regions.size)
        assertEquals(6, puzzle.regions.toSet().size)
        assertTrue("Zonennummern außerhalb des Bereichs", puzzle.regions.all { it in 0 until 6 })
    }

    @Test
    fun `keine Zone besteht aus einem einzigen Feld`() {
        // Eine Ein-Feld-Zone verrät ihre Fee sofort und nimmt dem Rätsel den
        // Reiz. Sehr *große* Zonen sind dagegen erwünscht — die ungleichen,
        // langgezogenen Zonen sind es, die die Lösung eindeutig machen.
        for (size in PuzzleGenerator.MIN_SIZE..9) {
            repeat(10) { round ->
                val puzzle = PuzzleGenerator.generate(size, Random(size * 1000L + round))
                val sizes = puzzle.regions.groupingBy { it }.eachCount().values

                assertTrue(
                    "Größe $size, Runde $round: Zone mit nur einem Feld ($sizes)",
                    sizes.min() >= 2,
                )
            }
        }
    }

    @Test
    fun `alle Zonen haengen zusammen`() {
        // Eine Zone aus zwei getrennten Inseln wäre auf dem Brett nicht als
        // eine Zone lesbar.
        for (size in PuzzleGenerator.MIN_SIZE..9) {
            val puzzle = PuzzleGenerator.generate(size, Random(size * 7L))

            for (region in 0 until size) {
                val cells = puzzle.allPositions.filter { puzzle.regionAt(it) == region }.toSet()
                assertTrue("Größe $size: Zone $region ist leer", cells.isNotEmpty())

                // Flutfüllung von einem beliebigen Feld aus.
                val reached = mutableSetOf(cells.first())
                val queue = ArrayDeque(reached)
                while (queue.isNotEmpty()) {
                    val current = queue.removeFirst()
                    listOf(
                        Pos(current.row - 1, current.col),
                        Pos(current.row + 1, current.col),
                        Pos(current.row, current.col - 1),
                        Pos(current.row, current.col + 1),
                    ).filter { it in cells && it !in reached }
                        .forEach { reached += it; queue += it }
                }

                assertEquals("Größe $size: Zone $region zerfällt", cells.size, reached.size)
            }
        }
    }

    @Test
    fun `jedes Raetsel hat genau eine Loesung`() {
        // Der wichtigste Test überhaupt: Bei mehreren Lösungen ließe sich das
        // Rätsel nicht durch Logik allein lösen, und der Feenstaub könnte ein
        // Feld aufdecken, das zu einer anderen Lösung gehört als der, die der
        // Spieler gerade baut.
        for (size in PuzzleGenerator.MIN_SIZE..9) {
            repeat(8) { round ->
                val puzzle = PuzzleGenerator.generate(size, Random(size * 100L + round))

                assertEquals(
                    "Größe $size, Runde $round ist nicht eindeutig",
                    1,
                    PuzzleGenerator.countSolutions(puzzle, limit = 2),
                )
            }
        }
    }

    @Test
    fun `die Erzeugung bleibt schnell genug fuer den Levelwechsel`() {
        // Das Rätsel entsteht zwischen zwei Levels, während der Spieler das
        // Ergebnis liest. Sekunden wären hier störend.
        val start = System.nanoTime()
        repeat(10) { round -> PuzzleGenerator.generate(9, Random(round.toLong())) }
        val millisEach = (System.nanoTime() - start) / 1_000_000 / 10

        assertTrue("Ein 9x9-Rätsel brauchte ${millisEach}ms", millisEach < 500)
    }

    @Test
    fun `gleicher Seed erzeugt dasselbe Raetsel`() {
        val first = PuzzleGenerator.generate(6, Random(1234))
        val second = PuzzleGenerator.generate(6, Random(1234))

        assertEquals(first, second)
    }

    @Test
    fun `zu kleine Gitter werden abgelehnt`() {
        val error = runCatching { PuzzleGenerator.generate(3, Random(1)) }.exceptionOrNull()

        assertTrue(
            "Erwartet wurde eine IllegalArgumentException, war: $error",
            error is IllegalArgumentException,
        )
    }
}
