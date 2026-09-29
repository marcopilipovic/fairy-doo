package ug.humb.fairydoku.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ug.humb.fairydoku.ads.AdOffer
import ug.humb.fairydoku.game.GameState
import ug.humb.fairydoku.game.GlobalLives
import ug.humb.fairydoku.game.GlobalLivesState
import ug.humb.fairydoku.ui.components.GameOverOverlay
import ug.humb.fairydoku.ui.components.PowerUpBar
import ug.humb.fairydoku.ui.theme.FairyDooTheme

/**
 * Drückt die Knöpfe wirklich.
 *
 * Alles andere in diesem Ordner liest Zustände ab oder zeichnet Bilder. Hier
 * wird **getippt** — auf dieselben Knöpfe, die eine Spielerin tippt — und
 * gezählt, ob dahinter etwas passiert ist.
 *
 * Der Anlass: Am 29.9.2026 waren zwei Knöpfe bedienbar und taten nichts. Ein
 * `enabled`-Kennzeichen zu setzen ist das eine; dass es den Druck auch
 * wirklich aufhält, ist das andere. Nataly hat kein Testgerät — diese
 * Prüfungen sind das Nächste, was an einen Fingerdruck herankommt.
 *
 * **Was diese Probe NICHT sieht:** den Finger. Sie schickt den Druck an den
 * Knoten, den sie über seine Beschriftung gefunden hat — sie prüft nicht, ob
 * dieser Knoten auf einem echten Bildschirm groß genug ist, ob ihn etwas
 * überdeckt oder ob er beim Scrollen wegrutscht. Ein Knopf, der richtig
 * reagiert und trotzdem nicht zu treffen ist, besteht hier.
 *
 * Ebenso wenig sieht sie das, was hinter dem Rückruf liegt: Gezählt wird der
 * Aufruf, nicht seine Wirkung. Ob danach wirklich ein Leben gutgeschrieben
 * wird, steht in `BelohnungTest`.
 *
 * Geprüft wird beides, und das zweite ist das wichtigere:
 *  - ein Knopf, der etwas kann, **löst aus**
 *  - ein Knopf, der nichts kann, **löst nicht aus** — auch wenn man ihn trifft
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class KnopfdruckTest {

    @get:Rule
    val regel = createAndroidComposeRule<ComponentActivity>()

    private val vollerVorrat = GameState(fairyDust = 3, irrlicht = 3, feenkreis = 2)
    private val leererVorrat = GameState(fairyDust = 0, irrlicht = 0, feenkreis = 0)

    /**
     * Nur der Feenkreis ist leer, die beiden anderen sind voll.
     *
     * Noetig, weil bei leerem Vorrat alle drei Kacheln dieselbe Aufschrift
     * tragen — „Werbung ansehen" steht dann dreimal da, und ein Klick darauf
     * ist nicht eindeutig. Zwei Pruefungen sind zuerst genau daran
     * gescheitert.
     */
    private val nurFeenkreisLeer = GameState(fairyDust = 3, irrlicht = 3, feenkreis = 0)

    private fun leiste(
        state: GameState,
        adsUnlocked: Boolean,
        adOffer: AdOffer = AdOffer.Unavailable,
        onFeenkreis: () -> Unit = {},
        onWerbungFeenkreis: () -> Unit = {},
    ) {
        regel.setContent {
            FairyDooTheme {
                PowerUpBar(
                    state = state,
                    nextDustInMillis = 3_600_000L,
                    nextIrrlichtInMillis = 3_600_000L,
                    nextFeenkreisInMillis = 7_200_000L,
                    onUseFairyDust = {},
                    onUseIrrlicht = {},
                    onUseFeenkreis = onFeenkreis,
                    adsUnlocked = adsUnlocked,
                    adOffer = adOffer,
                    onWatchAdForFairyDust = {},
                    onWatchAdForIrrlicht = {},
                    onOpenGiftForFairyDust = {},
                    onOpenGiftForIrrlicht = {},
                    onWatchAdForFeenkreis = onWerbungFeenkreis,
                )
            }
        }
    }

    // --- Der Feenkreis, der den ganzen Ärger gemacht hat --------------------

    @Test
    fun `mit Vorrat loest der Feenkreis aus`() {
        var gedrueckt = 0
        leiste(vollerVorrat, adsUnlocked = true, onFeenkreis = { gedrueckt++ })

        regel.onNodeWithText("Feenkreis\nkreuzt selbst an").performClick()

        assertEquals("Der Knopf haette ausloesen muessen", 1, gedrueckt)
    }

    @Test
    fun `ohne Vorrat und vor Level 4 loest nichts aus`() {
        // Der gemeldete Fehler, als Druck nachgestellt: Vor der Werbe-Schwelle
        // gibt es fuer den Feenkreis keinen Ausweg. Vor der Berichtigung war
        // der Knopf hier anfassbar und still.
        var gedrueckt = 0
        leiste(leererVorrat, adsUnlocked = false, onFeenkreis = { gedrueckt++ })

        regel.onNodeWithText("Feenkreis\nin 2 Std.").performClick()

        assertEquals("Ein gesperrter Knopf darf nicht ausloesen", 0, gedrueckt)
    }

    @Test
    fun `ohne Vorrat und ohne Anzeige loest nichts aus`() {
        // Der Zustand seit der Veroeffentlichung: Werbung freigeschaltet, aber
        // die Anzeigeneinheit liefert nichts.
        var werbung = 0
        leiste(
            nurFeenkreisLeer,
            adsUnlocked = true,
            adOffer = AdOffer.Unavailable,
            onWerbungFeenkreis = { werbung++ },
        )

        regel.onNodeWithText("Werbung\nnicht da").performClick()

        assertEquals("Ohne Anzeige darf der Druck nichts anstossen", 0, werbung)
    }

    @Test
    fun `mit bereiter Anzeige loest der Werbe-Knopf aus`() {
        var werbung = 0
        leiste(
            nurFeenkreisLeer,
            adsUnlocked = true,
            adOffer = AdOffer.Available,
            onWerbungFeenkreis = { werbung++ },
        )

        regel.onNodeWithText("Werbung\nansehen").performClick()

        assertEquals("Mit bereiter Anzeige muss der Druck durchgehen", 1, werbung)
    }

    @Test
    fun `ein brennender Feenkreis nimmt keinen zweiten an`() {
        // Sonst verpufft der zweite Vorrat im ersten Kreis.
        var gedrueckt = 0
        leiste(
            vollerVorrat.copy(feenkreisMillis = 21_000L),
            adsUnlocked = true,
            onFeenkreis = { gedrueckt++ },
        )

        regel.onNodeWithText("Feenkreis\nnoch 21 s").performClick()

        assertEquals(0, gedrueckt)
    }

    // --- Der Verloren-Dialog: der zweite tote Knopf -------------------------

    private fun verloren(
        adOffer: AdOffer,
        lives: Int = 0,
        onWatchAd: () -> Unit = {},
        onShowLevelMap: () -> Unit = {},
    ) {
        regel.setContent {
            FairyDooTheme {
                GameOverOverlay(
                    reason = "Drei Versuche verbraucht.",
                    score = 480,
                    level = 7,
                    bestScore = 1240,
                    globalLives = GlobalLivesState(lives = lives, nextLifeAtMillis = 0L),
                    onRetry = {},
                    onShowLevelMap = onShowLevelMap,
                    adsUnlocked = true,
                    adOffer = adOffer,
                    onWatchAd = onWatchAd,
                    onOpenGift = {},
                )
            }
        }
    }

    @Test
    fun `im Verloren-Dialog loest der Werbe-Knopf ohne Anzeige nicht aus`() {
        var werbung = 0
        verloren(AdOffer.Unavailable, onWatchAd = { werbung++ })

        regel.onNodeWithText("Werbung nicht verfügbar").performClick()

        assertEquals("Der goldene Knopf war hier bedienbar und tat nichts", 0, werbung)
    }

    @Test
    fun `im Verloren-Dialog loest er mit bereiter Anzeige aus`() {
        var werbung = 0
        verloren(AdOffer.Available, onWatchAd = { werbung++ })

        regel.onNodeWithText("📺 Werbung ansehen (+1 Leben)").performClick()

        assertEquals(1, werbung)
    }

    @Test
    fun `der Weg zur Karte steht immer offen`() {
        // Die wichtigste Prueffrage des ganzen Dialogs: Wer ohne Leben und ohne
        // Anzeige dasteht, darf nicht eingesperrt sein.
        var karte = 0
        verloren(AdOffer.Unavailable, lives = 0, onShowLevelMap = { karte++ })

        regel.onNodeWithText("🗺️ Zur Karte").assertIsDisplayed().assertHasClickAction()
        regel.onNodeWithText("🗺️ Zur Karte").performClick()

        assertEquals("Ohne Ausweg waere der Spieler gefangen", 1, karte)
    }

    @Test
    fun `mit Leben fuehrt der Dialog zurueck ins Level`() {
        var neustart = 0
        regel.setContent {
            FairyDooTheme {
                GameOverOverlay(
                    reason = "Drei Versuche verbraucht.",
                    score = 480, level = 7, bestScore = 1240,
                    globalLives = GlobalLivesState(GlobalLives.MAX, 0L),
                    onRetry = { neustart++ },
                    onShowLevelMap = {},
                    adsUnlocked = true,
                    adOffer = AdOffer.Unavailable,
                    onWatchAd = {}, onOpenGift = {},
                )
            }
        }

        regel.onNodeWithText("Level neu starten").performClick()

        assertEquals(1, neustart)
    }
}
