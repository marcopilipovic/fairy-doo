package com.fairydoo.game.game

import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Die Engine ist reine Logik ohne Android-Abhängigkeiten und läuft deshalb als
 * schneller JVM-Test. Fester Seed, damit jeder Lauf dasselbe Brett sieht.
 */
class FairydokuEngineTest {

    private val engine = FairydokuEngine(Random(42))

    private fun startedGame(level: Int = 1) = engine.newGame(level)

    /** Setzt alle Feen der hinterlegten Lösung. */
    private fun solve(state: GameState): GameState {
        val puzzle = requireNotNull(state.puzzle)
        return puzzle.solution.fold(state) { acc, pos ->
            engine.onInput(acc, GameInput.TapCell(pos))
        }
    }

    /** Ein Feld, das garantiert nicht zur Lösung gehört. */
    private fun wrongCell(state: GameState, avoid: Set<Pos> = emptySet()): Pos {
        val puzzle = requireNotNull(state.puzzle)
        return puzzle.allPositions.first { it !in puzzle.solution && it !in avoid }
    }

    @Test
    fun `neue Partie startet laufend mit Raetsel und voller Uhr`() {
        val state = startedGame()

        assertEquals(GameStatus.Running, state.status)
        assertNotNull(state.puzzle)
        assertEquals(4, state.puzzle?.size)
        assertEquals(GameState.durationForLevel(1), state.remainingMillis)
        assertEquals(0, state.score)
    }

    @Test
    fun `Tippen schaltet leer zu Fee zu Merkzeichen zu leer`() {
        var state = startedGame()
        val pos = requireNotNull(state.puzzle).solution.first()

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Fairy, state.markAt(pos))

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Warded, state.markAt(pos))

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Empty, state.markAt(pos))
    }

    @Test
    fun `eine kollidierende Fee zaehlt als Fehler und wird markiert`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val first = puzzle.solution.first()

        state = engine.onInput(state, GameInput.TapCell(first))
        // Gleiche Zeile wie die erste Fee: garantierter Konflikt.
        val clashing = puzzle.allPositions.first { it.row == first.row && it != first }
        state = engine.onInput(state, GameInput.TapCell(clashing))

        assertEquals(1, state.mistakes)
        assertTrue(clashing in state.conflicts)
        assertTrue(first in state.conflicts)
    }

    @Test
    fun `Wegnehmen einer Fee kostet keinen Fehler`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val first = puzzle.solution.first()
        val clashing = puzzle.allPositions.first { it.row == first.row && it != first }

        state = engine.onInput(state, GameInput.TapCell(first))
        state = engine.onInput(state, GameInput.TapCell(clashing))
        val afterMistake = state.mistakes

        // Weiter durch den Zyklus: Fee → Merkzeichen → leer.
        state = engine.onInput(state, GameInput.TapCell(clashing))
        state = engine.onInput(state, GameInput.TapCell(clashing))

        assertEquals(afterMistake, state.mistakes)
        assertTrue(state.conflicts.isEmpty())
    }

    @Test
    fun `der Natur-Schild faengt genau einen Fehler ab`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val first = puzzle.solution.first()

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.NatureShield))
        assertTrue(state.shieldActive)

        state = engine.onInput(state, GameInput.TapCell(first))
        val clashing = puzzle.allPositions.first { it.row == first.row && it != first }
        state = engine.onInput(state, GameInput.TapCell(clashing))

        assertEquals("Der Schild hätte den Fehler abfangen müssen", 0, state.mistakes)
        assertTrue("Der Schild ist verbraucht", !state.shieldActive)
    }

    @Test
    fun `drei Fehler beenden die Partie`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val anchor = puzzle.solution.first()
        state = engine.onInput(state, GameInput.TapCell(anchor))

        // Drei weitere Feen in derselben Zeile — jede kollidiert beim Setzen.
        puzzle.allPositions
            .filter { it.row == anchor.row && it != anchor }
            .take(GameState.MAX_MISTAKES)
            .forEach { state = engine.onInput(state, GameInput.TapCell(it)) }

        assertEquals(GameStatus.GameOver, state.status)
        assertEquals(0, state.mistakesLeft)
    }

    @Test
    fun `abgelaufene Zeit beendet die Partie`() {
        var state = startedGame()

        state = engine.tick(state, state.remainingMillis)

        assertEquals(0L, state.remainingMillis)
        assertEquals(GameStatus.GameOver, state.status)
    }

    @Test
    fun `die Zeiten-Bluete laesst die Uhr halb so schnell laufen`() {
        var state = startedGame()
        val before = state.remainingMillis

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.TimeBlossom))
        assertTrue(state.slowMotionActive)

        state = engine.tick(state, 1_000L)

        assertEquals("Nur die halbe Sekunde darf vergehen", before - 500L, state.remainingMillis)
    }

    @Test
    fun `die Zeiten-Bluete verblueht nach ihrer Dauer`() {
        var state = engine.onInput(startedGame(), GameInput.UsePowerUp(PowerUp.TimeBlossom))

        state = engine.tick(state, GameState.SLOW_MOTION_DURATION_MILLIS)
        assertTrue(!state.slowMotionActive)

        val before = state.remainingMillis
        state = engine.tick(state, 1_000L)
        assertEquals("Danach läuft die Uhr wieder normal", before - 1_000L, state.remainingMillis)
    }

    @Test
    fun `der Feenstaub setzt eine Fee auf ein Loesungsfeld`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.FairyDust))

        assertEquals(1, state.revealed.size)
        assertTrue("Der Hinweis muss zur Lösung gehören", state.revealed.all { it in puzzle.solution })
        assertEquals(CellMark.Fairy, state.markAt(state.revealed.first()))
        assertTrue(state.conflicts.isEmpty())
    }

    @Test
    fun `aufgedeckte Felder lassen sich nicht wegtippen`() {
        var state = engine.onInput(startedGame(), GameInput.UsePowerUp(PowerUp.FairyDust))
        val revealed = state.revealed.first()

        state = engine.onInput(state, GameInput.TapCell(revealed))

        assertEquals(CellMark.Fairy, state.markAt(revealed))
    }

    @Test
    fun `eine Faehigkeit ohne Vorrat bewirkt nichts`() {
        var state = startedGame()
        repeat(state.powerUpCount(PowerUp.FairyDust)) {
            state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.FairyDust))
        }
        val exhausted = state

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.FairyDust))

        assertEquals(exhausted, state)
        assertEquals(0, state.powerUpCount(PowerUp.FairyDust))
    }

    @Test
    fun `das geloeste Raetsel schliesst das Level ab und gibt Punkte`() {
        val state = solve(startedGame())

        assertEquals(GameStatus.LevelComplete, state.status)
        assertTrue("Es müssen Punkte anfallen", state.score > 0)
        assertEquals(0, state.remainingFairies)
    }

    @Test
    fun `das naechste Level bringt ein frisches Brett und behaelt den Punktestand`() {
        val solved = solve(startedGame())
        val next = engine.onInput(solved, GameInput.NextLevel)

        assertEquals(GameStatus.Running, next.status)
        assertEquals(2, next.level)
        assertEquals(solved.score, next.score)
        assertTrue("Das Brett muss leer sein", next.marks.isEmpty())
        assertNotEquals(solved.puzzle, next.puzzle)
        assertEquals(GameState.durationForLevel(2), next.remainingMillis)
    }

    @Test
    fun `der Wald wird mit steigendem Level dichter`() {
        assertEquals(4, GameState.sizeForLevel(1))
        assertEquals(4, GameState.sizeForLevel(2))
        assertEquals(5, GameState.sizeForLevel(3))
        assertEquals(9, GameState.sizeForLevel(11))
        assertEquals("Größer als 9x9 wird es nicht", 9, GameState.sizeForLevel(50))
    }

    @Test
    fun `Eingaben ausserhalb der laufenden Partie werden ignoriert`() {
        val paused = startedGame().copy(status = GameStatus.Paused)
        val pos = requireNotNull(paused.puzzle).solution.first()

        assertEquals(paused, engine.onInput(paused, GameInput.TapCell(pos)))
        assertEquals(paused, engine.onInput(paused, GameInput.UsePowerUp(PowerUp.FairyDust)))
        assertEquals(paused, engine.tick(paused, 1_000L))
    }

    @Test
    fun `der Levelwechsel greift nur nach geloestem Raetsel`() {
        val running = startedGame()

        assertEquals(running, engine.onInput(running, GameInput.NextLevel))
    }

    @Test
    fun `aufgedeckte Felder bringen keine Punkte`() {
        val withHint = engine.onInput(startedGame(), GameInput.UsePowerUp(PowerUp.FairyDust))
        val solvedWithHint = solve(withHint)
        val solvedAlone = solve(startedGame())

        assertTrue(
            "Mit Hinweis darf es nicht mehr Punkte geben als ohne",
            solvedWithHint.score < solvedAlone.score,
        )
    }
}
