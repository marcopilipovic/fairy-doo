package ug.humb.fairydoku.film

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import ug.humb.fairydoku.ads.AdOffer
import ug.humb.fairydoku.data.PlayerProfile
import ug.humb.fairydoku.game.DailyScoreState
import ug.humb.fairydoku.game.FairySpecies
import ug.humb.fairydoku.game.FairydokuEngine
import ug.humb.fairydoku.game.GameInput
import ug.humb.fairydoku.game.GameState
import ug.humb.fairydoku.game.GameStatus
import ug.humb.fairydoku.game.GlobalLives
import ug.humb.fairydoku.game.GlobalLivesState
import ug.humb.fairydoku.ui.screens.GameContent
import ug.humb.fairydoku.ui.screens.LevelSelectScreen
import ug.humb.fairydoku.ui.theme.FairyDooTheme

/**
 * Die fünf Bildschirmfotos für den Store — gerechnet statt abfotografiert.
 *
 * Bis zum 11. September 2026 stammten sie von einem Emulator: aufnehmen,
 * zuschneiden, Statusleiste wegretuschieren, und beim nächsten Umbau von vorn.
 * Drei der fünf zeigten deshalb noch die Spieluhr, die es seit dem 28. August
 * nicht mehr gibt, und keines den dritten Helfer.
 *
 * Hier steht stattdessen, **welcher Spielstand** zu sehen sein soll; das Bild
 * entsteht daraus. Ändert sich die Oberfläche, laufen die fünf neu durch.
 *
 *     ./gradlew testDebugUnitTest --tests '*BildschirmfotosTest*' -Dwerbefilm=ja
 *
 * Die Bilder liegen danach in `app/build/bildschirmfotos/` und gehören von dort
 * nach `storepaket/play-store/bildschirmfotos/`.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class BildschirmfotosTest {

    private val breite = 1080
    private val hoehe = 1920
    private val ziel = File("build/bildschirmfotos")
    private val engine = FairydokuEngine()

    @Test
    fun `die fuenf Bilder fuer den Store`() {
        assumeTrue("Nur mit -Dwerbefilm=ja", System.getProperty("werbefilm") == "ja")
        ziel.deleteRecursively()
        ziel.mkdirs()

        // 1. Mitten im Spiel: ein paar Kreuze, zwei Feen, alle drei Helfer.
        var spiel = engine.onInput(engine.newGame(5), GameInput.Begin)
        val loesung = requireNotNull(spiel.puzzle).solution.toList()
        spiel = engine.onInput(spiel, GameInput.HoldCell(loesung[0]))
        spiel = engine.onInput(spiel, GameInput.HoldCell(loesung[1]))
        halte("1-Spielbrett") { Bühne(spiel) }

        // 2. Der Feenpfad — die Levelkarte.
        halte("2-Feenpfad") { Karte() }

        // 3. Der brennende Feenkreis: Er hat gerade das halbe Brett zugekreuzt.
        var kreis = engine.onInput(spiel, GameInput.UseFeenkreis)
        kreis = engine.onInput(kreis, GameInput.HoldCell(loesung[2]))
        halte("3-Feenkreis") { Bühne(kreis) }

        // 4. Ein grosses Gitter: Level 9, acht Feen.
        var gross = engine.onInput(engine.newGame(9), GameInput.Begin)
        requireNotNull(gross.puzzle).solution.toList().take(5).forEach {
            gross = engine.onInput(gross, GameInput.HoldCell(it))
        }
        halte("4-Grosses-Gitter") { Bühne(gross) }

        // 5. Geschafft — der Gewinn-Dialog über dem geloesten Brett.
        var geloest = engine.onInput(engine.newGame(4), GameInput.Begin)
        requireNotNull(geloest.puzzle).solution.forEach {
            geloest = engine.onInput(geloest, GameInput.HoldCell(it))
        }
        assertTrue(
            "Level 4 sollte nach allen Feen geloest sein, war aber ${geloest.status}",
            geloest.status == GameStatus.LevelComplete,
        )
        halte("5-Level-geschafft") { Bühne(geloest) }
    }

    private fun halte(name: String, inhalt: @Composable () -> Unit) {
        val steuerung = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val ansicht = ComposeView(steuerung.get()).apply {
            setContent { FairyDooTheme { inhalt() } }
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
        println("Bildschirmfoto $name: ${datei.length() / 1024} KB")
        assertTrue("$name ist leer", datei.length() > 20_000)
        bild.recycle()
    }

    @Composable
    private fun Karte() {
        LevelSelectScreen(
            profile = PlayerProfile(highScore = 8_575, highestLevelUnlocked = 9),
            currentLevel = 5,
            daily = DailyScoreState(points = 480, bestPoints = 1_240, remainingSeconds = 26_400),
            globalLives = GlobalLivesState(GlobalLives.MAX, 0L),
            onClose = null,
            onSelectLevel = {}, onOpenTutorial = {}, onSetPlayerName = {}, onSetAvatar = {},
            onMusicChange = {}, onSoundChange = {}, onVoiceChange = {},
            adsUnlocked = false, adOffer = AdOffer.Available,
            onWatchAdForLife = {}, onOpenGiftForLife = {},
            privacyOptionsAvailable = false, onOpenPrivacyOptions = {},
        )
    }

    @Composable
    private fun Bühne(state: GameState) {
        GameContent(
            state = state, isPreparing = false, bestScore = 8_575, tagesPunkte = 480,
            profile = PlayerProfile(), showSoundSettings = false,
            onTapCell = {}, onHoldCell = {}, onUseFairyDust = {}, onUseIrrlicht = {},
            onUseFeenkreis = {}, onBegin = {}, onNextLevel = {}, onOpenLevelSelect = {},
            onRetryLevel = {}, globalLives = GlobalLivesState(GlobalLives.MAX, 0L),
            nextDustInMillis = 0L, nextIrrlichtInMillis = 0L, nextFeenkreisInMillis = 0L,
            adsUnlocked = false, adOffer = AdOffer.Available,
            onWatchAdForFairyDust = {}, onWatchAdForIrrlicht = {}, onWatchAdForLife = {},
            onOpenGiftForFairyDust = {}, onOpenGiftForIrrlicht = {}, onWatchAdForFeenkreis = {},
            onOpenGiftForLife = {}, onOpenSoundSettings = {}, onCloseSoundSettings = {},
            onMusicChange = {}, onSoundChange = {}, onVoiceChange = {}, onOpenTutorial = {},
            onClearBoard = {},
        )
    }
}
