package ug.humb.fairydoku.film

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import ug.humb.fairydoku.ads.AdOffer
import ug.humb.fairydoku.game.GameState
import ug.humb.fairydoku.game.GlobalLivesState
import ug.humb.fairydoku.ui.components.GameOverOverlay
import ug.humb.fairydoku.ui.components.PowerUpBar
import ug.humb.fairydoku.ui.theme.FairyDooTheme
import ug.humb.fairydoku.ui.theme.NightDeep
import java.io.File

/**
 * Zeichnet die Zustände, in denen ein Knopf nichts tun kann.
 *
 * Am 29. September 2026 kam aus der Runde „der Feenkreis macht irgendwie
 * nichts". Es waren zwei Stellen, nicht eine: der Feenkreis-Knopf bei leerem
 * Vorrat und der Werbe-Knopf im Verloren-Dialog, wenn keine Anzeige kommt.
 * Beide sahen voll bedienbar aus und taten nichts.
 *
 * Geprüft wird die Regel woanders — in `PowerUpKnopfTest`, ohne Bilder. Hier
 * geht es um das, was eine Prüfung nicht sieht: **ob man es dem Knopf ansieht.**
 * Ein `enabled = false`, das man nicht erkennt, ist derselbe Fehler noch einmal.
 *
 * **Was diese Probe NICHT sieht:** ob der Unterschied auffällt. Sie zeichnet
 * den gesperrten Zustand und stellt sicher, dass ein Bild entsteht — ob die
 * halbe Deckkraft für ein müdes Auge, bei Sonnenlicht oder für jemanden mit
 * schwachem Kontrastsehen erkennbar ist, kann sie nicht beantworten. Das Bild
 * ist zum Ansehen da, nicht zum Bestehen. Eine blind bestandene Probe hier
 * wäre derselbe Fehler, den sie sucht.
 *
 * Läuft nur auf Anforderung:
 *
 *     ./gradlew testDebugUnitTest --tests '*ToterKnopfTest*' -Dwerbefilm=ja
 *
 * Die Bilder liegen danach in `app/build/knopfproben/`.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class ToterKnopfTest {

    private val breite = 1080
    private val ziel = File("build/knopfproben")

    @Test
    fun `die Zustaende, in denen nichts zu holen ist`() {
        assumeTrue(
            "Nur auf Anforderung: -Dwerbefilm=ja",
            System.getProperty("werbefilm") == "ja",
        )
        ziel.mkdirs()

        val leer = GameState(fairyDust = 0, irrlicht = 0, feenkreis = 0)

        // 1. Vor Level 4: Feenstaub und Irrlicht führen zum Geschenk, der
        //    Feenkreis zu nichts. Genau hier war er bedienbar und still.
        halte("1-leiste-vor-level-4", 360) {
            PowerUpBar(
                state = leer,
                nextDustInMillis = 3_600_000L,
                nextIrrlichtInMillis = 3_600_000L,
                nextFeenkreisInMillis = 7_200_000L,
                onUseFairyDust = {}, onUseIrrlicht = {}, onUseFeenkreis = {},
                adsUnlocked = false,
                adOffer = AdOffer.Unavailable,
                onWatchAdForFairyDust = {}, onWatchAdForIrrlicht = {},
                onOpenGiftForFairyDust = {}, onOpenGiftForIrrlicht = {},
                onWatchAdForFeenkreis = {},
            )
        }

        // 2. Ab Level 4, und die Anzeigeneinheit liefert nichts — der Zustand,
        //    in dem das Spiel seit der Veröffentlichung steckt.
        halte("2-leiste-ohne-anzeige", 360) {
            PowerUpBar(
                state = leer,
                nextDustInMillis = 3_600_000L,
                nextIrrlichtInMillis = 3_600_000L,
                nextFeenkreisInMillis = 7_200_000L,
                onUseFairyDust = {}, onUseIrrlicht = {}, onUseFeenkreis = {},
                adsUnlocked = true,
                adOffer = AdOffer.Unavailable,
                onWatchAdForFairyDust = {}, onWatchAdForIrrlicht = {},
                onOpenGiftForFairyDust = {}, onOpenGiftForIrrlicht = {},
                onWatchAdForFeenkreis = {},
            )
        }

        // 3. Ein brennender Feenkreis sperrt seinen eigenen Knopf.
        halte("3-leiste-kreis-brennt", 360) {
            PowerUpBar(
                state = GameState(fairyDust = 2, irrlicht = 1, feenkreis = 1, feenkreisMillis = 21_000L),
                nextDustInMillis = 0L,
                nextIrrlichtInMillis = 0L,
                nextFeenkreisInMillis = 0L,
                onUseFairyDust = {}, onUseIrrlicht = {}, onUseFeenkreis = {},
                adsUnlocked = true,
                adOffer = AdOffer.Available,
                onWatchAdForFairyDust = {}, onWatchAdForIrrlicht = {},
                onOpenGiftForFairyDust = {}, onOpenGiftForIrrlicht = {},
                onWatchAdForFeenkreis = {},
            )
        }

        // 4. Der Verloren-Dialog ohne Leben und ohne Anzeige. Der zweite Fund.
        halte("4-verloren-ohne-anzeige", 1800) {
            GameOverOverlay(
                reason = "Drei Versuche verbraucht.",
                score = 480,
                level = 7,
                bestScore = 1240,
                globalLives = GlobalLivesState(lives = 0, nextLifeAtMillis = 0L),
                onRetry = {}, onShowLevelMap = {},
                adsUnlocked = true,
                adOffer = AdOffer.Unavailable,
                onWatchAd = {}, onOpenGift = {},
                werbeHinweis = "Gerade kommt keine Anzeige — versuch es später noch einmal.",
            )
        }
    }

    private fun halte(name: String, hoehe: Int, inhalt: @Composable () -> Unit) {
        val steuerung = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val ansicht = ComposeView(steuerung.get()).apply {
            setContent {
                FairyDooTheme {
                    Box(Modifier.fillMaxSize().background(NightDeep).padding(12.dp)) { inhalt() }
                }
            }
        }
        steuerung.get().setContentView(ansicht)
        ShadowLooper.idleMainLooper()
        ansicht.measure(
            View.MeasureSpec.makeMeasureSpec(breite, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(hoehe, View.MeasureSpec.EXACTLY),
        )
        ansicht.layout(0, 0, breite, hoehe)
        ShadowLooper.idleMainLooper()

        val bild = Bitmap.createBitmap(breite, hoehe, Bitmap.Config.ARGB_8888)
        ansicht.draw(Canvas(bild))
        val datei = File(ziel, "$name.png")
        datei.outputStream().use { bild.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("Knopfprobe $name: ${datei.length() / 1024} KB")
        assertTrue("$name ist leer", datei.length() > 5_000)
        bild.recycle()
    }
}
