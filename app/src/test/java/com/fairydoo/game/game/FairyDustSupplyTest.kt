package com.fairydoo.game.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Der Feenstaub-Vorrat: höchstens drei, und ein verbrauchter wächst in zwei
 * Stunden nach.
 *
 * Geprüft wird die Rechnerei ohne Uhr — die Zeit kommt als Parameter herein.
 * Dadurch lässt sich auch der Fall abbilden, der im Spiel am schwersten zu
 * erzeugen wäre: dass die App stundenlang geschlossen war.
 */
class FairyDustSupplyTest {

    private val supply = FairyDustSupply
    private val twoHours = 2 * 60 * 60_000L

    @Test
    fun `drei Stueck sind das Maximum`() {
        assertEquals(3, supply.max)
        assertEquals(twoHours, supply.intervalMillis)
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
        assertEquals(1_000L + twoHours, used.nextAtMillis)
    }

    @Test
    fun `ein zweiter Verbrauch verschiebt die laufende Uhr nicht`() {
        // Sonst ließe sich durch Verbrauchen kurz vor Ablauf die Wartezeit
        // immer wieder verlängern.
        val once = supply.consume(SupplyState(3, 0L), nowMillis = 1_000L)

        val twice = supply.consume(once, nowMillis = 1_000L + twoHours / 2)

        assertEquals(1, twice.amount)
        assertEquals(once.nextAtMillis, twice.nextAtMillis)
    }

    @Test
    fun `nach zwei Stunden ist eines zurueck`() {
        val used = supply.consume(SupplyState(3, 0L), nowMillis = 0L)

        val later = supply.normalize(used.amount, used.nextAtMillis, nowMillis = twoHours)

        assertEquals(3, later.amount)
        assertEquals("Bei vollem Vorrat steht die Uhr", 0L, later.nextAtMillis)
    }

    @Test
    fun `eine lange Pause holt alles nach, aber nicht mehr als das Maximum`() {
        // Die App war einen Tag geschlossen: Der Vorrat ist voll, nicht
        // zwölffach.
        val empty = SupplyState(0, twoHours)

        val later = supply.normalize(empty.amount, empty.nextAtMillis, nowMillis = 24 * 3_600_000L)

        assertEquals(3, later.amount)
        assertEquals(0L, later.nextAtMillis)
    }

    @Test
    fun `angebrochene Wartezeit geht nicht verloren`() {
        // Wer kurz vor Ablauf nachsieht, darf nicht wieder ganz von vorn anfangen.
        val used = supply.consume(SupplyState(3, 0L), nowMillis = 0L)

        val soon = supply.normalize(used.amount, used.nextAtMillis, nowMillis = twoHours - 60_000L)

        assertEquals(2, soon.amount)
        assertEquals(twoHours, soon.nextAtMillis)
    }
}

/**
 * Was passiert, wenn ein Geschenk über die Obergrenze hinausgeht.
 *
 * Der Fall, an dem es aufgefallen ist: Die Tagesbelohnung wird morgens
 * gutgeschrieben — da ist der Vorrat aber längst voll nachgewachsen, denn über
 * Nacht vergehen mehr als die sechs Stunden, die drei Stück brauchen. Wurde
 * dabei auf das Maximum gedeckelt, enthielt das Geschenk nichts.
 */
class SupplyOverflowTest {

    private val supply = FairyDustSupply

    @Test
    fun `ein Vorrat ueber der Grenze bleibt erhalten`() {
        val state = supply.normalize(storedAmount = 4, nextAtMillis = 0L, nowMillis = 1_000L)

        assertEquals("das Geschenk darf nicht verfallen", 4, state.amount)
        assertEquals("oberhalb wächst nichts nach", 0L, state.nextAtMillis)
    }

    @Test
    fun `oberhalb der Grenze waechst nichts weiter nach`() {
        val start = supply.normalize(5, 0L, 0L)
        val muchLater = supply.normalize(start.amount, start.nextAtMillis, 48 * 60 * 60_000L)

        assertEquals(5, muchLater.amount)
    }

    @Test
    fun `verbrauchen baut den Ueberschuss ab und startet die Uhr erst darunter`() {
        var state = SupplyState(5, 0L)

        state = supply.consume(state, nowMillis = 1_000L)
        assertEquals(4, state.amount)

        state = supply.normalize(state.amount, state.nextAtMillis, 1_000L)
        assertEquals("bei 4 steht die Uhr noch", 0L, state.nextAtMillis)

        state = supply.consume(state, nowMillis = 2_000L)
        assertEquals(3, state.amount)

        state = supply.consume(state, nowMillis = 3_000L)
        assertEquals("unter der Grenze läuft die Uhr", 2, state.amount)
        assertTrue(state.nextAtMillis > 0L)
    }

    @Test
    fun `ein negativer Stand wird weiterhin abgefangen`() {
        assertEquals(0, supply.normalize(-2, 0L, 1_000L).amount)
    }
}
