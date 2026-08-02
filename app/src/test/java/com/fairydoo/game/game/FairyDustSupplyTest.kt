package com.fairydoo.game.game

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Der Feenstaub-Vorrat: höchstens drei, und ein verbrauchter wächst in einer
 * halben Stunde nach.
 *
 * Geprüft wird die Rechnerei ohne Uhr — die Zeit kommt als Parameter herein.
 * Dadurch lässt sich auch der Fall abbilden, der im Spiel am schwersten zu
 * erzeugen wäre: dass die App stundenlang geschlossen war.
 */
class FairyDustSupplyTest {

    private val supply = FairyDustSupply
    private val halfHour = 30 * 60_000L

    @Test
    fun `drei Stueck sind das Maximum`() {
        assertEquals(3, supply.max)
        assertEquals(halfHour, supply.intervalMillis)
    }

    @Test
    fun `ein voller Vorrat waechst nicht weiter`() {
        // Ohne diese Grenze liefe die Uhr weiter und der Vorrat über das
        // Maximum hinaus — der Countdown zeigte dann eine Wartezeit an, auf die
        // niemand wartet.
        val state = supply.normalize(storedAmount = 3, nextAtMillis = 0L, nowMillis = 1_000L)

        assertEquals(3, state.amount)
        assertEquals(0L, state.nextAtMillis)
    }

    @Test
    fun `verbrauchen startet die Uhr`() {
        val full = SupplyState(3, 0L)

        val used = supply.consume(full, nowMillis = 1_000L)

        assertEquals(2, used.amount)
        assertEquals(1_000L + halfHour, used.nextAtMillis)
    }

    @Test
    fun `ein zweiter Verbrauch verschiebt die laufende Uhr nicht`() {
        // Sonst ließe sich durch Verbrauchen kurz vor Ablauf die Wartezeit
        // immer wieder verlängern.
        val once = supply.consume(SupplyState(3, 0L), nowMillis = 1_000L)

        val twice = supply.consume(once, nowMillis = 1_000L + halfHour / 2)

        assertEquals(1, twice.amount)
        assertEquals(once.nextAtMillis, twice.nextAtMillis)
    }

    @Test
    fun `nach einer halben Stunde ist eines zurueck`() {
        val used = supply.consume(SupplyState(3, 0L), nowMillis = 0L)

        val later = supply.normalize(used.amount, used.nextAtMillis, nowMillis = halfHour)

        assertEquals(3, later.amount)
        assertEquals("Bei vollem Vorrat steht die Uhr", 0L, later.nextAtMillis)
    }

    @Test
    fun `eine lange Pause holt alles nach, aber nicht mehr als das Maximum`() {
        // Die App war einen Tag geschlossen: Der Vorrat ist voll, nicht
        // achtundvierzigfach.
        val empty = SupplyState(0, halfHour)

        val later = supply.normalize(empty.amount, empty.nextAtMillis, nowMillis = 24 * 3_600_000L)

        assertEquals(3, later.amount)
        assertEquals(0L, later.nextAtMillis)
    }

    @Test
    fun `angebrochene Wartezeit geht nicht verloren`() {
        // Wer nach 29 Minuten nachsieht, darf nicht wieder bei 30 anfangen.
        val used = supply.consume(SupplyState(3, 0L), nowMillis = 0L)

        val soon = supply.normalize(used.amount, used.nextAtMillis, nowMillis = halfHour - 60_000L)

        assertEquals(2, soon.amount)
        assertEquals(halfHour, soon.nextAtMillis)
    }
}
