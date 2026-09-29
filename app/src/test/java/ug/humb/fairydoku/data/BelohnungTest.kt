package ug.humb.fairydoku.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import ug.humb.fairydoku.game.FairyDustSupply
import ug.humb.fairydoku.game.FeenkreisSupply
import ug.humb.fairydoku.game.GlobalLives
import ug.humb.fairydoku.game.IrrlichtSupply

/**
 * Kommt die Belohnung an, wenn ein Video zu Ende gesehen wurde?
 *
 * Das ist die Frage, bei der ein Fehler wirklich teuer wäre: Wer ein Video
 * ansieht und dafür nichts bekommt, kommt nicht wieder. Nataly am 29.9.2026:
 * „Es wäre ja fatal, wenn das nicht so wäre."
 *
 * Geprüft wird hier alles **hinter** dem Video — von dem Augenblick an, in dem
 * Googles SDK `onUserEarnedReward` meldet, bis zu dem Augenblick, in dem das
 * Leben gespeichert auf der Platte liegt. Ob überhaupt ein Video kommt, hängt
 * an Googles Auslieferung und lässt sich hier nicht erzwingen; ob die
 * Belohnung ankommt, wenn eines kam, schon.
 *
 * Gearbeitet wird mit einem **echten** DataStore, nicht mit einer Attrappe.
 * Der Fehler, den diese Prüfungen suchen, säße genau dort: zwischen Aufruf und
 * Platte.
 *
 * Es sind die ersten Prüfungen für diese Schicht überhaupt. Bis heute war der
 * Spielkern dicht geprüft und der Weg dorthin blank — und genau dort saß der
 * Fehler mit dem toten Feenkreis-Knopf.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class BelohnungTest {

    private fun lager() = GamePreferencesRepository(ApplicationProvider.getApplicationContext())

    /**
     * Ein bekannter Ausgangszustand: alles voll, keine Uhr am Laufen.
     *
     * Der Speicher ist eine echte Datei und überlebt die einzelne Prüfung.
     * Ohne diese Zeilen erbt jede Prüfung, was die vorige hinterlassen hat —
     * und zwei von ihnen sind genau daran zuerst gescheitert. Schenken deckelt
     * beim Höchststand, deshalb genügt oft genug schenken.
     */
    @Before
    fun alleVorraeteVoll() = runBlocking {
        val lager = lager()
        repeat(GlobalLives.MAX) { lager.grantGlobalLife() }
        repeat(FairyDustSupply.max) { lager.grantFairyDust() }
        repeat(IrrlichtSupply.max) { lager.grantIrrlicht() }
        repeat(FeenkreisSupply.max) { lager.grantFeenkreis() }
    }

    // --- Wald-Leben: der Fall aus dem Verloren-Dialog ------------------------

    @Test
    fun `ohne Leben bringt ein Video genau eines`() = runBlocking {
        val lager = lager()
        // Alle fünf aufbrauchen — der Zustand, in dem der Werbe-Knopf erscheint.
        repeat(GlobalLives.MAX) { lager.consumeGlobalLife() }
        assertEquals(0, lager.profile.first().globalLives)

        lager.grantGlobalLife()

        assertEquals(1, lager.profile.first().globalLives)
    }

    @Test
    fun `die Belohnung ueberlebt den App-Neustart`() = runBlocking {
        val lager = lager()
        repeat(GlobalLives.MAX) { lager.consumeGlobalLife() }
        lager.grantGlobalLife()

        // Ein frisches Lager auf demselben Speicher — das ist, was nach einem
        // Neustart passiert. Läge das Leben nur im Arbeitsspeicher, wäre es weg.
        assertEquals(1, lager().profile.first().globalLives)
    }

    @Test
    fun `mehr als voll wird niemand`() = runBlocking {
        val lager = lager()
        repeat(3) { lager.grantGlobalLife() }

        assertEquals(GlobalLives.MAX, lager.profile.first().globalLives)
    }

    @Test
    fun `ein geschenktes Leben laesst die Nachwuchsuhr weiterlaufen`() = runBlocking {
        // Sonst würde ein Video den Countdown verschenken: Wer bei null Leben
        // eines geschenkt bekommt, soll das zweite trotzdem zur geplanten Zeit
        // bekommen und nicht von vorn warten.
        val lager = lager()
        repeat(GlobalLives.MAX) { lager.consumeGlobalLife() }
        val vorher = lager.profile.first().nextGlobalLifeAtMillis
        assertTrue("Ohne Leben muss eine Uhr laufen", vorher > 0L)

        lager.grantGlobalLife()

        assertEquals(vorher, lager.profile.first().nextGlobalLifeAtMillis)
    }

    @Test
    fun `beim letzten Leben hoert die Uhr auf`() = runBlocking {
        val lager = lager()
        lager.consumeGlobalLife()
        assertTrue(lager.profile.first().nextGlobalLifeAtMillis > 0L)

        lager.grantGlobalLife()

        assertEquals(GlobalLives.MAX, lager.profile.first().globalLives)
        assertEquals(0L, lager.profile.first().nextGlobalLifeAtMillis)
    }

    // --- Die drei Helfer: derselbe Weg, drei weitere Male --------------------

    @Test
    fun `ein Video bringt Feenstaub`() = runBlocking {
        val lager = lager()
        repeat(FairyDustSupply.max) { lager.consumeFairyDust() }
        assertEquals(0, lager.profile.first().fairyDust)

        lager.grantFairyDust()

        assertEquals(1, lager.profile.first().fairyDust)
        assertEquals(1, lager().profile.first().fairyDust)
    }

    @Test
    fun `ein Video bringt ein Irrlicht`() = runBlocking {
        val lager = lager()
        repeat(IrrlichtSupply.max) { lager.consumeIrrlicht() }
        assertEquals(0, lager.profile.first().irrlicht)

        lager.grantIrrlicht()

        assertEquals(1, lager.profile.first().irrlicht)
        assertEquals(1, lager().profile.first().irrlicht)
    }

    @Test
    fun `ein Video bringt einen Feenkreis`() = runBlocking {
        // Der Helfer, dessen Knopf heute tot war. Hier wird geprüft, dass der
        // Weg dahinter trägt — der Knopf ist woanders geprüft.
        val lager = lager()
        repeat(FeenkreisSupply.max) { lager.consumeFeenkreis() }
        assertEquals(0, lager.profile.first().feenkreis)

        lager.grantFeenkreis()

        assertEquals(1, lager.profile.first().feenkreis)
        assertEquals(1, lager().profile.first().feenkreis)
    }

    @Test
    fun `kein Helfer wird ueber seinen Vorrat hinaus geschenkt`() = runBlocking {
        val lager = lager()
        repeat(FairyDustSupply.max + 2) { lager.grantFairyDust() }
        repeat(IrrlichtSupply.max + 2) { lager.grantIrrlicht() }
        repeat(FeenkreisSupply.max + 2) { lager.grantFeenkreis() }

        val stand = lager.profile.first()
        assertEquals(FairyDustSupply.max, stand.fairyDust)
        assertEquals(IrrlichtSupply.max, stand.irrlicht)
        assertEquals(FeenkreisSupply.max, stand.feenkreis)
    }
}
