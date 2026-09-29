package ug.humb.fairydoku.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ug.humb.fairydoku.ui.components.knopfBedienbar

/**
 * Wann ein Helfer-Knopf bedienbar sein darf.
 *
 * Diese Prüfungen gibt es, weil genau hier ein Fehler drei Wochen lang saß:
 * Der Feenkreis-Knopf war bei leerem Vorrat bedienbar und tat nichts. Gemeldet
 * aus der Runde am 29. September 2026 — „der Feenkreis macht irgendwie nichts".
 *
 * Gefunden wurde er nicht durch Nachdenken über die Regel, sondern weil der
 * Spielkern geprüft war und die Bedienung nicht. Neun Prüfungen deckten den
 * Feenkreis ab, alle neun sprachen mit der Maschine statt mit dem Knopf.
 */
class PowerUpKnopfTest {

    @Test
    fun `mit Vorrat ist der Knopf bedienbar`() {
        assertTrue(knopfBedienbar(vorrat = 1))
        assertTrue(knopfBedienbar(vorrat = 3))
    }

    @Test
    fun `ohne Vorrat und ohne Ausweg ist er es nicht`() {
        // Der Fall, der den Fehler ausmachte: der Feenkreis vor Level 4. Kein
        // Geschenk, keine Werbung, kein Vorrat — und trotzdem war er anfassbar.
        assertFalse(knopfBedienbar(vorrat = 0))
    }

    @Test
    fun `ein brennender Feenkreis sperrt den Knopf, auch mit Vorrat`() {
        // Sonst verpufft der zweite Vorrat im ersten Kreis.
        assertFalse(knopfBedienbar(vorrat = 2, brennt = true))
    }

    @Test
    fun `ohne Vorrat fuehrt das Geschenk weiter`() {
        assertTrue(knopfBedienbar(vorrat = 0, geschenkMoeglich = true))
    }

    @Test
    fun `ohne Vorrat haengt die Werbung daran, ob eine Anzeige da ist`() {
        assertTrue(
            knopfBedienbar(vorrat = 0, werbungMoeglich = true, anzeigeBereit = true),
        )
        // Der Zustand, in dem das Spiel seit der Veröffentlichung steckt: Die
        // Anzeigeneinheit ist frisch und liefert nichts. Dann bleibt der Knopf
        // blass — richtig so, denn ein Video, das nicht kommt, ist kein Weg.
        assertFalse(
            knopfBedienbar(vorrat = 0, werbungMoeglich = true, anzeigeBereit = false),
        )
    }

    @Test
    fun `der Vorrat schlaegt jeden Ausweg`() {
        // Wer noch etwas hat, soll es ausgeben und kein Video ansehen.
        assertTrue(
            knopfBedienbar(
                vorrat = 1,
                geschenkMoeglich = true,
                werbungMoeglich = true,
                anzeigeBereit = false,
            ),
        )
    }

    @Test
    fun `ein negativer Vorrat zaehlt wie keiner`() {
        // Kommt nicht vor, soll aber nicht zu einem bedienbaren Knopf führen,
        // falls doch einmal etwas danebenrechnet.
        assertFalse(knopfBedienbar(vorrat = -1))
    }
}
