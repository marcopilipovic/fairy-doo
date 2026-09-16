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
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import ug.humb.fairydoku.ads.AdOffer
import ug.humb.fairydoku.data.PlayerProfile
import ug.humb.fairydoku.game.FairydokuEngine
import ug.humb.fairydoku.game.GameInput
import ug.humb.fairydoku.game.GameState
import ug.humb.fairydoku.game.GlobalLives
import ug.humb.fairydoku.game.GlobalLivesState
import ug.humb.fairydoku.game.DailyScoreState
import ug.humb.fairydoku.ui.screens.GameContent
import ug.humb.fairydoku.ui.screens.LevelSelectScreen
import ug.humb.fairydoku.ui.theme.FairyDooTheme

/**
 * Wie sieht der Spielbildschirm auf anderen Geräten aus?
 *
 * Die Frage steht seit dem 25. August offen: Mit Ziel-API 36 achtet Android auf
 * großen Bildschirmen nicht mehr auf die Festlegung `screenOrientation`, und die
 * Play Console mahnt sie an. „Das Brett ist vorbereitet, gesehen hat es dort
 * noch niemand" — genau das lässt sich jetzt nachsehen, ohne ein Tablet zu
 * besitzen.
 *
 *     ./gradlew testDebugUnitTest --tests '*GeraeteprobeTest*' -Dwerbefilm=ja
 *
 * Die Bilder liegen danach in `app/build/geraeteprobe/`.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class GeraeteprobeTest {

    private val ziel = File("build/geraeteprobe")

    @Test
    fun `Hochformat, Querformat und Tablet`() {
        // Vier Bildschirmgroessen zu zeichnen dauert eine Viertelstunde —
        // Robolectric baut fuer jede Kennung seine Welt neu auf. Das gehoert
        // nicht in jeden Testlauf, sondern dorthin, wo man hinsehen will.
        assumeTrue("Nur mit -Dwerbefilm=ja", System.getProperty("werbefilm") == "ja")

        ziel.mkdirs()

        val engine = FairydokuEngine()
        var zustand = engine.onInput(engine.newGame(5), GameInput.Begin)
        requireNotNull(zustand.puzzle).solution.take(2).forEach {
            zustand = engine.onInput(zustand, GameInput.HoldCell(it))
        }

        val faelle = listOf(
            // Ein Samsung S21: 1080 x 2400 bei 420 dpi, also 360 x 800 dp. Aus
            // der Testrunde am 16. September 2026: "Da ist die Karte klein."
            Triple("s21-spiel", "w360dp-h800dp-xxhdpi", 1080 to 2400),
            Triple("hochformat-telefon", "w360dp-h640dp-xxhdpi", 1080 to 1920),
            Triple("querformat-telefon", "w640dp-h360dp-land-xxhdpi", 1920 to 1080),
            Triple("tablet-hoch", "w800dp-h1280dp-xhdpi", 1600 to 2560),
            Triple("tablet-quer", "w1280dp-h800dp-land-xhdpi", 2560 to 1600),
            Triple("s21-karte", "w360dp-h800dp-xxhdpi", 1080 to 2400),
            // Samsungs Bildschirmzoom aendert nicht die Aufloesung, sondern die
            // Dichte: Dieselben 1080 Bildpunkte sind bei 480 dpi 360 dp breit,
            // bei 420 dpi schon 411 und bei 400 dpi 432. Feste Masse schrumpfen
            // dadurch im Verhaeltnis zum Bildschirm — daran lag die Meldung
            // "Da ist die Karte klein" vom 16. September 2026.
            Triple("zoom-klein-karte", "w411dp-h914dp-420dpi", 1080 to 2400),
            Triple("zoom-winzig-karte", "w432dp-h960dp-400dpi", 1080 to 2400),
        )

        for ((name, kennung, groesse) in faelle) {
            RuntimeEnvironment.setQualifiers(kennung)
            val bild = zeichne(groesse.first, groesse.second) {
                FairyDooTheme { if (name.endsWith("-karte")) Karte() else Buehne(zustand) }
            }
            val datei = File(ziel, "$name.png")
            datei.outputStream().use { bild.compress(Bitmap.CompressFormat.PNG, 100, it) }
            println("Geräteprobe $name: ${bild.width}x${bild.height}, ${datei.length()} Byte")
            assertTrue("$name ist leer", datei.length() > 5_000)
            bild.recycle()
        }
    }

    private fun zeichne(breite: Int, hoehe: Int, inhalt: @Composable () -> Unit): Bitmap {
        val steuerung = Robolectric.buildActivity(ComponentActivity::class.java).setup()
        val ansicht = ComposeView(steuerung.get()).apply { setContent(inhalt) }
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
        return bild
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
    private fun Buehne(state: GameState) {
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
