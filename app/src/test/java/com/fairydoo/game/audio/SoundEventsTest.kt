package com.fairydoo.game.audio

import com.fairydoo.game.game.FairydokuEngine
import com.fairydoo.game.game.GameInput
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.model.Pos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Zuordnung von Spielzug zu Klang ist reine Logik und deshalb prüfbar —
 * anders als die Frage, ob ein Kichern hübsch klingt.
 */
class SoundEventsTest {

    private val engine = FairydokuEngine()

    private fun startedGame() = engine.onInput(engine.newGame(), GameInput.Begin)

    private fun tap(state: GameState, pos: Pos) = engine.onInput(state, GameInput.TapCell(pos))

    /** Das Halten setzt die Fee — aus dem Leeren wie aus dem Merkzeichen. */
    private fun place(state: GameState, pos: Pos) =
        engine.onInput(state, GameInput.HoldCell(pos))

    @Test
    fun `ein Merkzeichen macht Tick`() {
        val before = startedGame()
        val pos = requireNotNull(before.puzzle).solution.first()

        val events = SoundEvents.diff(before, tap(before, pos))

        assertEquals(listOf(SoundEvent.Ward), events)
    }

    @Test
    fun `eine richtig gesetzte Fee ruft ihren Ausruf`() {
        val warded = tap(startedGame(), requireNotNull(startedGame().puzzle).solution.first())
        val pos = requireNotNull(warded.puzzle).solution.first()

        val events = SoundEvents.diff(warded, place(warded, pos))

        assertTrue("Erwartet wurde ein Ausruf, war: $events", events.single() is SoundEvent.FairyPlaced)
    }

    @Test
    fun `der Ausruf gehoert zur Fee auf dem gesetzten Feld`() {
        var state = startedGame()
        val solution = requireNotNull(state.puzzle).solution.toList()

        // Die letzte Fee löst das Rätsel — dann jubelt es statt zu rufen (siehe
        // "das geloeste Raetsel jubelt statt zu kichern"), deshalb hier außen vor.
        for (pos in solution.dropLast(1)) {
            val warded = tap(state, pos)
            val placed = place(warded, pos)
            val event = SoundEvents.diff(warded, placed)
                .filterIsInstance<SoundEvent.FairyPlaced>()
                .single()

            assertEquals(placed.speciesAt(pos), event.species)
            state = placed
        }
    }

    @Test
    fun `eine falsch gesetzte Fee erschrickt`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val anchor = puzzle.solution.first()
        state = place(state, anchor)

        val clashing = puzzle.allPositions.first { it.row == anchor.row && it != anchor }
        val warded = tap(state, clashing)

        val events = SoundEvents.diff(warded, place(warded, clashing))

        assertEquals(listOf(SoundEvent.FairyStartled), events)
    }

    @Test
    fun `das Wegnehmen einer Fee klingt nach Ruecknahme`() {
        var state = startedGame()
        val pos = requireNotNull(state.puzzle).solution.first()
        state = place(state, pos)

        val events = SoundEvents.diff(state, tap(state, pos))

        assertEquals(listOf(SoundEvent.Undo), events)
    }

    @Test
    fun `der Feenstaub klingt nach dem Zug, den er tut`() {
        val before = startedGame()
        val nachher = engine.onInput(before, GameInput.UseFairyDust)

        val events = SoundEvents.diff(before, nachher)

        // Der Feenstaub setzt eine Fee, also ist die Fee zu hören — kein
        // eigener Klang. Bis zum 30. August war es umgekehrt: ein Funkeln
        // statt des Zuges.
        assertEquals(1, events.size)
        assertTrue("Erwartet wurde eine gesetzte Fee, war: $events",
            events.single() is SoundEvent.FairyPlaced)
    }

    @Test
    fun `das Irrlicht klingt wie ein Merkzeichen`() {
        val before = startedGame()

        val events = SoundEvents.diff(
            before,
            engine.onInput(before, GameInput.UseIrrlicht),
        )

        assertEquals(listOf(SoundEvent.Ward), events)
    }

    @Test
    fun `erschoepfter Feenstaub bleibt still`() {
        var state = startedGame()
        repeat(state.fairyDust) {
            state = engine.onInput(state, GameInput.UseFairyDust)
        }

        val events = SoundEvents.diff(state, engine.onInput(state, GameInput.UseFairyDust))

        assertTrue("Erwartet wurde Stille, war: $events", events.isEmpty())
    }

    @Test
    fun `das geloeste Raetsel jubelt statt zu kichern`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val solution = puzzle.solution.toList()

        // Alle bis auf die letzte Fee setzen.
        for (pos in solution.dropLast(1)) {
            state = place(state, pos)
        }

        val last = solution.last()
        val warded = tap(state, last)
        val events = SoundEvents.diff(warded, place(warded, last))

        assertEquals(listOf(SoundEvent.LevelComplete), events)
    }

    @Test
    fun `das Spielende ist zu hoeren`() {
        var state = startedGame()
        val puzzle = requireNotNull(state.puzzle)
        val anchor = puzzle.solution.first()
        state = place(state, anchor)

        val clashing = puzzle.allPositions.filter { it.row == anchor.row && it != anchor }
        // Zwei Fehler vorweg, der dritte beendet die Partie.
        for (pos in clashing.take(GameState.MAX_LIVES - 1)) {
            state = place(state, pos)
        }

        val lastMistake = clashing[GameState.MAX_LIVES - 1]
        val warded = tap(state, lastMistake)
        val events = SoundEvents.diff(warded, place(warded, lastMistake))

        assertTrue("Der Schreck fehlt: $events", SoundEvent.FairyStartled in events)
        assertTrue("Das Spielende fehlt: $events", SoundEvent.GameOver in events)
    }

    /**
     * Das Aufräumen des Bretts ist kein Zug.
     *
     * Bis zum 30. August war es einer: Beim Wechsel ins nächste Level wurden aus
     * den gesetzten Feen leere Felder, und daraus las [SoundEvents] für jede
     * einzelne eine Rücknahme. Bei einem gelösten 8×8-Brett waren das acht
     * Abwärts-Wispern auf einen Schlag — das „komische Geräusch", das Nataly
     * beim Spielen gemeldet hat.
     */
    @Test
    fun `der Levelwechsel raeumt das Brett ohne Ruecknahmen`() {
        val geloest = solvedGame()
        val naechstes = engine.onInput(geloest, GameInput.NextLevel)

        val events = SoundEvents.diff(geloest, naechstes)

        assertEquals(listOf(SoundEvent.LevelStart), events)
    }

    /** Löst das erste Level, indem alle Feen der Lösung gesetzt werden. */
    private fun solvedGame(): GameState {
        var state = startedGame()
        for (pos in requireNotNull(state.puzzle).solution) {
            state = place(state, pos)
        }
        return state
    }
}
