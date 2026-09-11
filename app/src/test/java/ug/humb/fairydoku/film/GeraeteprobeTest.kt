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
import ug.humb.fairydoku.ui.screens.GameContent
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
            Triple("hochformat-telefon", "w360dp-h640dp-xxhdpi", 1080 to 1920),
            Triple("querformat-telefon", "w640dp-h360dp-land-xxhdpi", 1920 to 1080),
            Triple("tablet-hoch", "w800dp-h1280dp-xhdpi", 1600 to 2560),
            Triple("tablet-quer", "w1280dp-h800dp-land-xhdpi", 2560 to 1600),
        )

        for ((name, kennung, groesse) in faelle) {
            RuntimeEnvironment.setQualifiers(kennung)
            val bild = zeichne(groesse.first, groesse.second) {
                FairyDooTheme { Buehne(zustand) }
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
