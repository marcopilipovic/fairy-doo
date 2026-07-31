package com.fairydoo.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Engine ist reine Logik ohne Android-Abhängigkeiten und läuft deshalb als
 * schneller JVM-Test. Wenn die echte Puzzle-Mechanik einzieht, gehören ihre
 * Regeln genau hierher.
 */
class PlaceholderEngineTest {

    private val engine = PlaceholderEngine()

    @Test
    fun `neue Partie startet laufend mit voller Zeit`() {
        val state = engine.newGame()

        assertEquals(GameStatus.Running, state.status)
        assertEquals(0, state.score)
        assertEquals(GameState.ROUND_DURATION_MILLIS, state.remainingMillis)
    }

    @Test
    fun `Tap erhoeht Punkte und Zuege`() {
        val state = engine.onInput(engine.newGame(), GameInput.Tap(0.5f, 0.5f))

        assertTrue(state.score > 0)
        assertEquals(1, state.moves)
    }

    @Test
    fun `Eingaben ausserhalb der laufenden Partie werden ignoriert`() {
        val paused = engine.newGame().copy(status = GameStatus.Paused)

        val after = engine.onInput(paused, GameInput.Tap(0.5f, 0.5f))

        assertEquals(paused, after)
    }

    @Test
    fun `Runde endet wenn die Zeit abgelaufen ist`() {
        var state = engine.newGame()
        val ticks = GameState.ROUND_DURATION_MILLIS / TICK + 1

        repeat(ticks.toInt()) { state = engine.tick(state, TICK) }

        assertEquals(GameStatus.Finished, state.status)
        assertEquals(0L, state.remainingMillis)
    }

    @Test
    fun `Tick ohne laufende Partie veraendert nichts`() {
        val finished = engine.newGame().copy(status = GameStatus.Finished)

        assertEquals(finished, engine.tick(finished, TICK))
    }

    private companion object {
        const val TICK = 16L
    }
}
