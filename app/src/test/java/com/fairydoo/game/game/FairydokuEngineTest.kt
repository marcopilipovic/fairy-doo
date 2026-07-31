package com.fairydoo.game.game

import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    /** Startet eine Partie und überspringt das Willkommens-Overlay. */
    private fun startedGame(level: Int = 1): GameState =
        engine.onInput(engine.newGame(level), GameInput.Begin)

    /** Setzt eine Fee auf [pos] — zwei Tipps, weil erst das Merkzeichen kommt. */
    private fun placeFairy(state: GameState, pos: Pos): GameState =
        engine.onInput(engine.onInput(state, GameInput.TapCell(pos)), GameInput.TapCell(pos))

    /** Setzt alle Feen der hinterlegten Lösung. */
    private fun solve(state: GameState): GameState {
        val puzzle = requireNotNull(state.puzzle)
        return puzzle.solution.fold(state) { acc, pos -> placeFairy(acc, pos) }
    }

    @Test
    fun `eine neue Partie beginnt im Willkommens-Overlay`() {
        val state = engine.newGame()

        assertEquals(GameStatus.Intro, state.status)
        assertNotNull(state.puzzle)
        assertEquals(4, state.puzzle?.size)
        assertEquals(GameState.MAX_LIVES, state.lives)
        assertEquals(0, state.score)
    }

    @Test
    fun `der Wald wird erst nach dem Betreten betretbar`() {
        val intro = engine.newGame()
        val pos = requireNotNull(intro.puzzle).solution.first()

        // Im Intro laufen weder Uhr noch Eingaben.
        assertEquals(intro, engine.onInput(intro, GameInput.TapCell(pos)))
        assertEquals(intro, engine.tick(intro, 1_000L))

        val started = engine.onInput(intro, GameInput.Begin)
        assertEquals(GameStatus.Running, started.status)
        assertEquals(GameState.durationForLevel(1), started.remainingMillis)
    }

    @Test
    fun `Tippen schaltet leer zu Merkzeichen zu Fee zu leer`() {
        var state = startedGame()
        val pos = requireNotNull(state.puzzle).solution.first()

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Warded, state.markAt(pos))

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Fairy, state.markAt(pos))

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Empty, state.markAt(pos))
    }

    @Test
    fun `jedes Tippen meldet die Zone des Feldes`() {
        val state = startedGame()
        val pos = Pos(0, 0)
        val expected = requireNotNull(state.puzzle).regionAt(pos)

        val tapped = engine.onInput(state, GameInput.TapCell(pos))

        assertEquals(StatusMessage.Zone(expected), tapped.statusMessage)
    }

    @Test
    fun `eine kollidierende Fee kostet ein Leben und wird markiert`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val first = puzzle.solution.first()

        state = placeFairy(state, first)
        // Gleiche Zeile wie die erste Fee: garantierter Konflikt.
        val clashing = puzzle.allPositions.first { it.row == first.row && it != first }
        state = placeFairy(state, clashing)

        assertEquals(GameState.MAX_LIVES - 1, state.lives)
        assertEquals(StatusMessage.MistakeMade, state.statusMessage)
        assertTrue(clashing in state.conflicts)
        assertTrue(first in state.conflicts)
    }

    @Test
    fun `Wegnehmen einer Fee kostet kein Leben`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val first = puzzle.solution.first()
        val clashing = puzzle.allPositions.first { it.row == first.row && it != first }

        state = placeFairy(state, first)
        state = placeFairy(state, clashing)
        val livesAfterMistake = state.lives

        state = engine.onInput(state, GameInput.TapCell(clashing))

        assertEquals(livesAfterMistake, state.lives)
        assertTrue(state.conflicts.isEmpty())
    }

    @Test
    fun `der Natur-Schild faengt genau einen Fehler ab`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val first = puzzle.solution.first()

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.NatureShield))
        assertTrue(state.shieldActive)
        assertEquals(StatusMessage.ShieldActivated, state.statusMessage)

        state = placeFairy(state, first)
        val clashing = puzzle.allPositions.first { it.row == first.row && it != first }
        state = placeFairy(state, clashing)

        assertEquals("Der Schild hätte den Fehler abfangen müssen", GameState.MAX_LIVES, state.lives)
        assertFalse("Der Schild ist verbraucht", state.shieldActive)
        assertEquals(StatusMessage.ShieldSaved, state.statusMessage)
    }

    @Test
    fun `ein zweiter Schild wird nicht verschwendet`() {
        val state = engine.onInput(startedGame(), GameInput.UsePowerUp(PowerUp.NatureShield))
        val again = engine.onInput(state, GameInput.UsePowerUp(PowerUp.NatureShield))

        assertEquals(state.powerUpCount(PowerUp.NatureShield), again.powerUpCount(PowerUp.NatureShield))
        assertEquals(StatusMessage.ShieldAlreadyActive, again.statusMessage)
    }

    @Test
    fun `drei Fehler beenden die Partie`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val anchor = puzzle.solution.first()
        state = placeFairy(state, anchor)

        puzzle.allPositions
            .filter { it.row == anchor.row && it != anchor }
            .take(GameState.MAX_LIVES)
            .forEach { state = placeFairy(state, it) }

        assertEquals(GameStatus.GameOver, state.status)
        assertEquals(0, state.lives)
        assertEquals(GameOverReason.TooManyConflicts, state.overReason)
    }

    @Test
    fun `abgelaufene Zeit beendet die Partie`() {
        var state = startedGame()

        state = engine.tick(state, state.remainingMillis)

        assertEquals(0L, state.remainingMillis)
        assertEquals(GameStatus.GameOver, state.status)
        assertEquals(GameOverReason.TimeUp, state.overReason)
    }

    @Test
    fun `die Zeiten-Bluete haelt die Uhr an`() {
        var state = startedGame()
        val before = state.remainingMillis

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.TimeBlossom))
        assertTrue(state.timeFrozen)
        assertEquals(StatusMessage.TimeFrozen, state.statusMessage)

        state = engine.tick(state, 5_000L)

        assertEquals("Während der Blüte darf keine Zeit vergehen", before, state.remainingMillis)
    }

    @Test
    fun `nach der Zeiten-Bluete laeuft die Uhr weiter`() {
        var state = engine.onInput(startedGame(), GameInput.UsePowerUp(PowerUp.TimeBlossom))

        state = engine.tick(state, GameState.FREEZE_DURATION_MILLIS)
        assertFalse(state.timeFrozen)

        val before = state.remainingMillis
        state = engine.tick(state, 1_000L)
        assertEquals(before - 1_000L, state.remainingMillis)
    }

    @Test
    fun `der Feenstaub setzt eine Fee auf ein Loesungsfeld`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.FairyDust))

        val revealed = requireNotNull(state.hintCell)
        assertTrue("Der Hinweis muss zur Lösung gehören", revealed in puzzle.solution)
        assertEquals(CellMark.Fairy, state.markAt(revealed))
        assertTrue(state.conflicts.isEmpty())
        assertEquals(StatusMessage.FairyDustUsed, state.statusMessage)
    }

    @Test
    fun `das Nachleuchten des Hinweises verglueht`() {
        var state = engine.onInput(startedGame(), GameInput.UsePowerUp(PowerUp.FairyDust))
        assertNotNull(state.hintCell)

        state = engine.tick(state, GameState.HINT_PULSE_MILLIS)

        assertEquals(null, state.hintCell)
    }

    @Test
    fun `eine Faehigkeit ohne Vorrat meldet sich, ohne zu wirken`() {
        var state = startedGame()
        repeat(state.powerUpCount(PowerUp.FairyDust)) {
            state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.FairyDust))
        }
        val marksBefore = state.marks

        state = engine.onInput(state, GameInput.UsePowerUp(PowerUp.FairyDust))

        assertEquals(0, state.powerUpCount(PowerUp.FairyDust))
        assertEquals(marksBefore, state.marks)
        assertEquals(StatusMessage.Exhausted(PowerUp.FairyDust), state.statusMessage)
    }

    @Test
    fun `das geloeste Raetsel schliesst das Level ab`() {
        val state = solve(startedGame())

        assertEquals(GameStatus.LevelComplete, state.status)
        assertEquals(state.boardSize, state.placedFairies)
    }

    @Test
    fun `die Punkte folgen der Formel aus dem Design`() {
        val state = solve(startedGame())

        // 100 Punkte je Gitterfeld plus 5 je verbleibender Sekunde.
        val expected = 100 * state.boardSize + state.remainingSeconds * 5
        assertEquals(expected, state.gained)
        assertEquals(expected, state.score)
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
        assertEquals(solved.lives, next.lives)
    }

    @Test
    fun `nach jedem Level gibt es Nachschub`() {
        val solved = solve(startedGame())
        val next = engine.onInput(solved, GameInput.NextLevel)

        assertEquals(
            solved.powerUpCount(PowerUp.FairyDust) + 1,
            next.powerUpCount(PowerUp.FairyDust),
        )
        assertEquals(
            solved.powerUpCount(PowerUp.TimeBlossom) + 1,
            next.powerUpCount(PowerUp.TimeBlossom),
        )
        // Der Schild kommt nur nach jedem zweiten Level dazu; Level 1 ist ungerade.
        assertEquals(
            solved.powerUpCount(PowerUp.NatureShield),
            next.powerUpCount(PowerUp.NatureShield),
        )
    }

    @Test
    fun `der Wald wird mit steigendem Level dichter`() {
        assertEquals(4, GameState.sizeForLevel(1))
        assertEquals(4, GameState.sizeForLevel(2))
        assertEquals(5, GameState.sizeForLevel(3))
        assertEquals(6, GameState.sizeForLevel(5))
        assertEquals("Größer als 8x8 wird es nicht", 8, GameState.sizeForLevel(50))
    }

    @Test
    fun `die Feen-Art wechselt mit jedem Level`() {
        assertEquals(FairySpecies.Blossom, GameState.speciesForLevel(1))
        assertEquals(FairySpecies.Water, GameState.speciesForLevel(2))
        assertEquals(FairySpecies.Fire, GameState.speciesForLevel(3))
        assertEquals(FairySpecies.Star, GameState.speciesForLevel(4))
        assertEquals("Nach vier Arten beginnt die Reihe von vorn", FairySpecies.Blossom, GameState.speciesForLevel(5))
    }

    @Test
    fun `die Zeit waechst mit der Gittergroesse`() {
        // 60 Sekunden Grundzeit plus 15 je Gitterfeld.
        assertEquals((60 + 4 * 15) * 1000L, GameState.durationForLevel(1))
        assertEquals((60 + 5 * 15) * 1000L, GameState.durationForLevel(3))
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
}
