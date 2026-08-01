package com.fairydoo.game.game

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Reine Zeitrechnung ohne Android-Uhr — jeder Fall gibt "jetzt" fest vor,
 * damit die Tests deterministisch bleiben.
 */
class GlobalLivesTest {

    @Test
    fun `voller Vorrat braucht keine Uhr`() {
        val state = GlobalLives.normalize(storedLives = GlobalLives.MAX, nextLifeAtMillis = 0L, nowMillis = 1_000L)

        assertEquals(GlobalLives.MAX, state.lives)
        assertEquals(0L, state.nextLifeAtMillis)
    }

    @Test
    fun `vor Ablauf der Frist bleibt der Stand unveraendert`() {
        val state = GlobalLives.normalize(storedLives = 3, nextLifeAtMillis = 10_000L, nowMillis = 5_000L)

        assertEquals(3, state.lives)
        assertEquals(10_000L, state.nextLifeAtMillis)
    }

    @Test
    fun `genau ein Leben waechst nach, wenn die Frist um ist`() {
        val state = GlobalLives.normalize(storedLives = 3, nextLifeAtMillis = 10_000L, nowMillis = 10_000L)

        assertEquals(4, state.lives)
        assertEquals(10_000L + GlobalLives.REGEN_INTERVAL_MILLIS, state.nextLifeAtMillis)
    }

    @Test
    fun `mehrere verpasste Intervalle wachsen alle auf einmal nach`() {
        // Frist um plus zwei volle weitere Intervalle verstrichen: drei Leben wachsen nach.
        val threeIntervalsLater = 10_000L + 2 * GlobalLives.REGEN_INTERVAL_MILLIS
        val state = GlobalLives.normalize(storedLives = 1, nextLifeAtMillis = 10_000L, nowMillis = threeIntervalsLater)

        assertEquals(4, state.lives)
        // Noch nicht voll (5): die Frist läuft weiter, verschoben um die drei nachgewachsenen Intervalle.
        assertEquals(10_000L + 3 * GlobalLives.REGEN_INTERVAL_MILLIS, state.nextLifeAtMillis)
    }

    @Test
    fun `das Nachwachsen deckelt bei MAX und loescht die Frist`() {
        val muchLater = 10_000L + 100 * GlobalLives.REGEN_INTERVAL_MILLIS
        val state = GlobalLives.normalize(storedLives = GlobalLives.MAX - 1, nextLifeAtMillis = 10_000L, nowMillis = muchLater)

        assertEquals(GlobalLives.MAX, state.lives)
        assertEquals(0L, state.nextLifeAtMillis)
    }

    @Test
    fun `Verbrauchen von vollem Vorrat startet die Frist`() {
        val full = GlobalLivesState(GlobalLives.MAX, 0L)

        val consumed = GlobalLives.consume(full, nowMillis = 1_000L)

        assertEquals(GlobalLives.MAX - 1, consumed.lives)
        assertEquals(1_000L + GlobalLives.REGEN_INTERVAL_MILLIS, consumed.nextLifeAtMillis)
    }

    @Test
    fun `Verbrauchen bei bereits laufender Frist aendert sie nicht`() {
        val partial = GlobalLivesState(3, 5_000L)

        val consumed = GlobalLives.consume(partial, nowMillis = 1_000L)

        assertEquals(2, consumed.lives)
        assertEquals(5_000L, consumed.nextLifeAtMillis)
    }

    @Test
    fun `Leben koennen nicht unter null fallen`() {
        val empty = GlobalLivesState(0, 5_000L)

        val consumed = GlobalLives.consume(empty, nowMillis = 1_000L)

        assertEquals(0, consumed.lives)
    }
}
