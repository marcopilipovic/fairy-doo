package com.fairydoo.game.ui

import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.StatusMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Texte der Oberfläche — geprüft auf das, was auf einem echten Gerät
 * schiefging.
 *
 * Beide Fälle hier sind keine erfundenen Randfälle: Sie standen so auf einem
 * Bildschirmfoto und sind erst dort aufgefallen. Ein Text, der im Entwurf passt,
 * kann bei vergrößerter Systemschrift abgeschnitten werden — und ein Satz, der
 * für sich stimmt, kann trotzdem etwas Falsches behaupten.
 */
class GameCopyTest {

    // ---- Meldungen unter dem Brett ----

    /**
     * Die Statuszeile hat Platz für zwei Zeilen. Bei großer Systemschrift
     * reicht auch der nicht mehr, deshalb bleiben die Meldungen knapp.
     *
     * Der Wert stammt aus dem Fall, der aufgefallen ist: „✨ Der Feenstaub
     * zeigt dir ein sicheres Feld!" mit 44 Zeichen wurde abgeschnitten.
     */
    @Test
    fun `keine Meldung ist laenger als die Zeile traegt`() {
        val messages = listOf(
            StatusMessage.Hint,
            StatusMessage.MistakeMade,
            StatusMessage.FairyDustUsed,
            StatusMessage.IrrlichtUsed,
            StatusMessage.NoFairyDust(nextInMillis = 119 * 60_000L),
            StatusMessage.NoIrrlicht(nextInMillis = 119 * 60_000L),
        )

        messages.forEach { message ->
            val text = GameCopy.statusText(message)
            assertTrue(
                "Zu lang für die Statuszeile (${text.length} Zeichen): „$text\"",
                text.length <= MAX_STATUS_LENGTH,
            )
        }
    }

    /**
     * Die Zonenmeldung darf zwei Zeilen brauchen, aber nicht drei.
     *
     * Sie ist die einzige zusammengesetzte Meldung — Zonenname plus
     * Feenvorstellung — und kann durch lange Namen wachsen, ohne dass es jemand
     * bemerkt. „Glühwürmchen-Hain · Trixie, die Chaosfee" ist der längste Fall.
     *
     * Anmerkung zur Grenze: Dass dieser Text auf dem Gerät trotzdem
     * abgeschnitten wurde, lag nicht an seiner Länge, sondern daran, dass die
     * Zeile eine feste Höhe hatte und deshalb gar nicht erst umbrach. Der Test
     * hier hätte das nie gefunden — er prüft den Text, nicht das Layout.
     */
    @Test
    fun `auch die laengste Zonenmeldung bleibt im Rahmen`() {
        val longest = FairySpecies.entries.maxOf { species ->
            (0 until GameState.MAX_SIZE).maxOf { zone ->
                GameCopy.statusText(StatusMessage.Zone(zone, species)).length
            }
        }

        assertTrue("Längste Zonenmeldung: $longest Zeichen", longest <= MAX_ZONE_LENGTH)
    }

    // ---- Der Ausblick nach einem geschafften Level ----

    @Test
    fun `waechst das Gitter wird der Wald dichter`() {
        val teaser = GameCopy.nextLevelTeaser(
            nextSize = 6,
            newcomers = listOf(FairySpecies.Nixie),
            sizeGrew = true,
        )

        assertTrue(teaser, teaser.startsWith("Der Wald wird dichter: 6×6-Gitter"))
        assertTrue(teaser, teaser.contains("Nixie wartet schon"))
    }

    /**
     * Der Fehler, der auf dem Bildschirmfoto stand.
     *
     * Das Gitter wächst nur jedes zweite Level. Nach Level 3 — schon auf 5×5
     * gespielt — kündigte der Ausblick trotzdem „Der Wald wird dichter:
     * 5×5-Gitter" an. Das nächste Level hielt dieses Versprechen nicht.
     */
    @Test
    fun `bleibt das Gitter gleich wird nichts dichter versprochen`() {
        val teaser = GameCopy.nextLevelTeaser(
            nextSize = 5,
            newcomers = listOf(FairySpecies.Aura, FairySpecies.Ignis),
            sizeGrew = false,
        )

        assertFalse("„dichter\" ist hier gelogen: $teaser", teaser.contains("dichter"))
        assertTrue(teaser, teaser.contains("5×5-Gitter"))
        assertTrue(teaser, teaser.contains("Aura und Ignis warten schon"))
    }

    @Test
    fun `ohne Neuzugaenge steht trotzdem ein Satz da`() {
        listOf(true, false).forEach { grew ->
            val teaser = GameCopy.nextLevelTeaser(
                nextSize = 8,
                newcomers = emptyList(),
                sizeGrew = grew,
            )
            assertTrue(teaser, teaser.contains("8×8-Gitter"))
            assertFalse("Kein Bindestrich ohne Namen dahinter: $teaser", teaser.endsWith("— "))
        }
    }

    @Test
    fun `viele Neuzugaenge werden zusammengefasst statt aufgezaehlt`() {
        val teaser = GameCopy.nextLevelTeaser(
            nextSize = 8,
            newcomers = FairySpecies.entries.take(5),
            sizeGrew = true,
        )

        // Drei Namen, dann „und 2 weitere" — sonst sprengt der Satz das Overlay.
        assertTrue(teaser, teaser.contains("2 weitere"))
        assertEquals(3, FairySpecies.entries.take(5).count { teaser.contains(it.displayName) })
    }

    /**
     * Prüft den Ausblick gegen die echte Gitter-Regel.
     *
     * Der Aufrufer entscheidet über `sizeGrew` — hier wird nachgerechnet, dass
     * das für jedes Level derselbe Wert ist, den `sizeForLevel` hergibt.
     */
    @Test
    fun `der Ausblick stimmt fuer jedes Level mit der Gitter-Regel ueberein`() {
        (1..30).forEach { level ->
            val current = GameState.sizeForLevel(level)
            val next = GameState.sizeForLevel(level + 1)
            val teaser = GameCopy.nextLevelTeaser(
                nextSize = next,
                newcomers = GameState.speciesOnBoard(level + 1) -
                    GameState.speciesOnBoard(level).toSet(),
                sizeGrew = next > current,
            )

            if (next == current) {
                assertFalse(
                    "Level $level → ${level + 1}: Gitter bleibt $current, „dichter\" wäre falsch — $teaser",
                    teaser.contains("dichter"),
                )
            }
            assertTrue("Level $level: $teaser", teaser.contains("$next×$next-Gitter"))
        }
    }

    private companion object {
        /** Zwei Zeilen à rund 22 Zeichen, mit Reserve für große Systemschrift. */
        const val MAX_STATUS_LENGTH = 42

        /** Die Zonenmeldung darf etwas länger sein — sie enthält zwei Namen. */
        const val MAX_ZONE_LENGTH = 46
    }
}
