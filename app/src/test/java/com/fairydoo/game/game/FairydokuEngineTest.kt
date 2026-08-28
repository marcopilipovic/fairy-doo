package com.fairydoo.game.game

import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Engine ist reine Logik ohne Android-Abhängigkeiten und läuft deshalb als
 * schneller JVM-Test. Fester Seed, damit jeder Lauf dasselbe Brett sieht.
 */
class FairydokuEngineTest {

    private val engine = FairydokuEngine()

    /** Startet eine Partie und überspringt das Willkommens-Overlay. */
    private fun startedGame(level: Int = 1): GameState =
        engine.onInput(engine.newGame(level), GameInput.Begin)

    /** Setzt eine Fee auf [pos] — das Halten ist ihre Geste. */
    private fun placeFairy(state: GameState, pos: Pos): GameState =
        engine.onInput(state, GameInput.HoldCell(pos))

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
    fun `einmal Tippen setzt und entfernt das Merkzeichen`() {
        var state = startedGame()
        val pos = requireNotNull(state.puzzle).solution.first()

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Warded, state.markAt(pos))

        state = engine.onInput(state, GameInput.TapCell(pos))
        assertEquals(CellMark.Empty, state.markAt(pos))
    }

    @Test
    fun `Halten setzt die Fee - aus dem Leeren wie aus dem Merkzeichen`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val fromEmpty = puzzle.solution.first()
        // Zweites Feld derselben Lösung: kollidiert nicht mit dem ersten.
        val fromWarded = puzzle.solution.last()

        state = engine.onInput(state, GameInput.HoldCell(fromEmpty))
        assertEquals(CellMark.Fairy, state.markAt(fromEmpty))

        state = engine.onInput(state, GameInput.TapCell(fromWarded))
        assertEquals(CellMark.Warded, state.markAt(fromWarded))
        state = engine.onInput(state, GameInput.HoldCell(fromWarded))
        assertEquals(CellMark.Fairy, state.markAt(fromWarded))
    }

    @Test
    fun `auf einer Fee raeumen beide Gesten das Feld`() {
        val started = startedGame()
        val pos = requireNotNull(started.puzzle).solution.first()
        val withFairy = engine.onInput(started, GameInput.HoldCell(pos))

        assertEquals(
            CellMark.Empty,
            engine.onInput(withFairy, GameInput.TapCell(pos)).markAt(pos),
        )
        assertEquals(
            CellMark.Empty,
            engine.onInput(withFairy, GameInput.HoldCell(pos)).markAt(pos),
        )
    }

    @Test
    fun `jedes Tippen meldet die Zone des Feldes und ihre Bewohnerin`() {
        val state = startedGame()
        val pos = Pos(0, 0)
        val expected = requireNotNull(state.puzzle).regionAt(pos)

        val tapped = engine.onInput(state, GameInput.TapCell(pos))

        assertEquals(
            StatusMessage.Zone(expected, GameState.speciesForZone(state.level, expected)),
            tapped.statusMessage,
        )
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
    fun `die Zeit beendet die Partie nicht mehr`() {
        var state = startedGame()

        // Weit über jede frühere Leveldauer hinaus.
        state = engine.tick(state, 10 * 60 * 1000L)

        assertEquals(GameStatus.Running, state.status)

        // Bis zum 28. August 2026 lief hier ein Countdown, und war er
        // abgelaufen, war das Level verloren. Der Zeitdruck machte das Spiel
        // für die Jüngsten unspielbar und ist deshalb herausgenommen worden.
        // Der Test steht umgedreht weiter da, damit die Uhr nicht
        // versehentlich zurückkommt.
        assertNull("Ein Level darf nicht mehr an der Zeit scheitern", state.overReason)
    }

    @Test
    fun `der Feenstaub setzt eine Fee auf ein Loesungsfeld`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)

        state = engine.onInput(state, GameInput.UseFairyDust)

        val revealed = requireNotNull(state.hintCell)
        assertTrue("Der Hinweis muss zur Lösung gehören", revealed in puzzle.solution)
        assertEquals(CellMark.Fairy, state.markAt(revealed))
        assertTrue(state.conflicts.isEmpty())
        assertEquals(StatusMessage.FairyDustUsed, state.statusMessage)
    }

    @Test
    fun `das Nachleuchten des Hinweises verglueht`() {
        var state = engine.onInput(startedGame(), GameInput.UseFairyDust)
        assertNotNull(state.hintCell)

        state = engine.tick(state, GameState.HINT_PULSE_MILLIS)

        assertEquals(null, state.hintCell)
    }

    @Test
    fun `ohne Vorrat bewirkt der Feenstaub nichts`() {
        var state = startedGame()
        repeat(state.fairyDust) {
            state = engine.onInput(state, GameInput.UseFairyDust)
        }
        val marksBefore = state.marks

        state = engine.onInput(state, GameInput.UseFairyDust)

        // Kein Feld aufgedeckt, und der Vorrat rutscht nicht ins Minus.
        assertEquals(0, state.fairyDust)
        assertEquals(marksBefore, state.marks)
    }

    @Test
    fun `das Irrlicht markiert ein sicheres Feld ohne Fee`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)

        state = engine.onInput(state, GameInput.UseIrrlicht)

        val revealed = requireNotNull(state.hintCell)
        assertTrue(
            "Der Hinweis darf nicht zur Lösung gehören",
            revealed !in puzzle.solution,
        )
        assertEquals(CellMark.Warded, state.markAt(revealed))
        assertEquals(StatusMessage.IrrlichtUsed, state.statusMessage)
    }

    @Test
    fun `das Irrlicht kostet nie ein Leben und loest nie das Raetsel`() {
        var state = startedGame()
        repeat(GameState.MAX_SIZE * GameState.MAX_SIZE) {
            state = engine.onInput(state, GameInput.UseIrrlicht)
        }

        assertEquals(GameStatus.Running, state.status)
        assertEquals(GameState.MAX_LIVES, state.lives)
        assertTrue(state.conflicts.isEmpty())
    }

    @Test
    fun `ohne Vorrat bewirkt das Irrlicht nichts`() {
        var state = startedGame()
        repeat(state.irrlicht) {
            state = engine.onInput(state, GameInput.UseIrrlicht)
        }
        val marksBefore = state.marks

        state = engine.onInput(state, GameInput.UseIrrlicht)

        assertEquals(0, state.irrlicht)
        assertEquals(marksBefore, state.marks)
    }

    @Test
    fun `das Irrlicht geht ins naechste Level mit`() {
        val started = startedGame()
        val used = engine.onInput(started, GameInput.UseIrrlicht)
        val solved = solve(used)
        val next = engine.onInput(solved, GameInput.NextLevel)

        assertEquals(started.irrlicht - 1, used.irrlicht)
        assertEquals(used.irrlicht, next.irrlicht)
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
    }

    @Test
    fun `ein Fehler im vorigen Level darf die Versuche im naechsten nicht schmaelern`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val anchor = puzzle.solution.first()
        state = placeFairy(state, anchor)
        // Ein Fehler, aber nicht genug, um das Level zu verlieren.
        val clashing = puzzle.allPositions.first { it.row == anchor.row && it != anchor }
        state = placeFairy(state, clashing)
        state = engine.onInput(state, GameInput.TapCell(clashing))
        assertEquals(GameState.MAX_LIVES - 1, state.lives)
        // Zuruecknehmen, damit `solve` gleich jedes Loesungsfeld frisch setzen
        // kann, statt den Anker durch erneutes Halten wieder zu entfernen.
        state = placeFairy(state, anchor)

        val solved = solve(state)
        val next = engine.onInput(solved, GameInput.NextLevel)

        assertEquals(GameState.MAX_LIVES, next.lives)
    }

    @Test
    fun `der Feenstaub geht ins naechste Level mit`() {
        // Es gibt keinen Nachschub je Level mehr: Der Vorrat gehört dem
        // Spieler und wächst über die Zeit nach. Was übrig ist, nimmt das
        // nächste Level unverändert mit — sonst wäre das Nachwachsen sinnlos,
        // weil ein Levelwechsel den Vorrat ohnehin auffüllte.
        val started = startedGame()
        val used = engine.onInput(started, GameInput.UseFairyDust)
        val solved = solve(used)
        val next = engine.onInput(solved, GameInput.NextLevel)

        assertEquals(started.fairyDust - 1, used.fairyDust)
        assertEquals(used.fairyDust, next.fairyDust)
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
        assertEquals(paused, engine.onInput(paused, GameInput.UseFairyDust))
        assertEquals(paused, engine.onInput(paused, GameInput.UseIrrlicht))
        assertEquals(paused, engine.tick(paused, 1_000L))
    }

    @Test
    fun `der Levelwechsel greift nur nach geloestem Raetsel`() {
        val running = startedGame()

        assertEquals(running, engine.onInput(running, GameInput.NextLevel))
    }

    @Test
    fun `ein frischer Start bei hoeherem Level setzt Punkte, Leben und Vorraete zurueck`() {
        // Regression: newGame(level) mit level > 1 gab früher angereicherte
        // Vorräte statt eines wirklich frischen Levels — die Levelkarte
        // erlaubt genau diesen Einstieg, darum muss er sauber sein.
        val solvedFirstLevel = solve(startedGame())
        val continued = engine.onInput(solvedFirstLevel, GameInput.NextLevel)
        assertTrue("Vorbedingung: Level 2 hat schon etwas Punktestand", continued.score > 0)

        val freshAtLevelFive = engine.newGame(level = 5)

        assertEquals(5, freshAtLevelFive.level)
        assertEquals(0, freshAtLevelFive.score)
        assertEquals(GameState.MAX_LIVES, freshAtLevelFive.lives)
        assertEquals(FairyDustSupply.max, freshAtLevelFive.fairyDust)
        assertEquals(IrrlichtSupply.max, freshAtLevelFive.irrlicht)
        assertEquals(GameState.sizeForLevel(5), freshAtLevelFive.boardSize)
    }
}
