package ug.humb.fairydoku.film

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.time.Duration
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import android.os.Looper
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLooper
import ug.humb.fairydoku.ads.AdOffer
import ug.humb.fairydoku.data.PlayerProfile
import ug.humb.fairydoku.game.FairydokuEngine
import ug.humb.fairydoku.game.GameInput
import ug.humb.fairydoku.game.GameState
import ug.humb.fairydoku.game.model.Pos
import ug.humb.fairydoku.game.GlobalLives
import ug.humb.fairydoku.game.GlobalLivesState
import ug.humb.fairydoku.ui.screens.GameContent
import ug.humb.fairydoku.ui.theme.FairyDooTheme

/**
 * Nimmt die laufende App als Bildfolge auf — auf dem Rechner, ohne Telefon.
 *
 * **Warum nicht mit einem Aufnahmeprogramm.** Der übliche Weg zu einem
 * Werbefilm ist ein Telefon, ein Mitschnitt und ein Schnittprogramm: drei
 * Werkzeuge, ein Gerät und ein Mensch, der alles noch einmal von Hand tippt,
 * sobald sich ein Bildschirm ändert. Hier ist der Film ein Testlauf. Ändert
 * sich die Oberfläche, wird er neu gerechnet statt neu gedreht.
 *
 * Läuft nur auf Anforderung, weil er Minuten braucht und Hunderte Bilder
 * schreibt:
 *
 *     ./gradlew testDebugUnitTest --tests '*WerbefilmTest*' -Dwerbefilm=ja
 *     python3 werkzeuge/werbefilm.py
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h640dp-xxhdpi")
class WerbefilmTest {

    private val breite = 1080
    private val hoehe = 1920
    private val bilderJeSekunde = 30
    private val msJeBild = 1000L / bilderJeSekunde

    private val ziel = File("build/werbefilm/bilder")
    private var nummer = 0

    @get:Rule
    val regel = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `den Werbefilm rechnen`() {
        assumeTrue("Nur mit -Dwerbefilm=ja", System.getProperty("werbefilm") == "ja")

        ziel.deleteRecursively()
        ziel.mkdirs()

        val engine = FairydokuEngine()
        val zustand = mutableStateOf(engine.onInput(engine.newGame(5), GameInput.Begin))

        regel.setContent { FairyDooTheme { Bühne(zustand.value) } }

        // Die Uhr von Hand stellen: Jedes Bild ist ein Schritt von einem
        // dreissigstel Sekunde. Automatisch weiterlaufen darf sie nicht, sonst
        // haengt der Lauf an einer Endlos-Animation.
        regel.mainClock.autoAdvance = false
        regel.mainClock.advanceTimeBy(16L)

        val puzzle = requireNotNull(zustand.value.puzzle)
        val loesung = puzzle.solution.toList()
        val alleFelder = (0 until puzzle.size).flatMap { r -> (0 until puzzle.size).map { c -> Pos(r, c) } }

        // 1. Das Brett, einen Augenblick in Ruhe.
        marke("brett")
        halte(1.4)

        // 2. Kreuze setzen — die haeufigste Geste im Spiel.
        marke("kreuze")
        val zumAusschliessen = alleFelder.filter { it !in loesung.toSet() }.take(4)
        for (feld in zumAusschliessen) {
            zustand.value = engine.onInput(zustand.value, GameInput.TapCell(feld))
            halte(0.45)
        }

        // 3. Eine Fee setzen.
        marke("fee")
        zustand.value = engine.onInput(zustand.value, GameInput.HoldCell(loesung[0]))
        halte(1.6)

        // 4. Der Feenkreis: anzuenden, dann eine Fee setzen — das halbe Brett
        //    kreuzt sich von selbst zu. Der beste Augenblick, den das Spiel hat.
        marke("kreis-an")
        zustand.value = engine.onInput(zustand.value, GameInput.UseFeenkreis)
        halte(0.9)
        marke("kreis-wirkt")
        zustand.value = engine.onInput(zustand.value, GameInput.HoldCell(loesung[1]))
        halte(2.0)

        // 5. Fertig loesen.
        marke("loesen")
        for (feld in loesung.drop(2)) {
            zustand.value = engine.onInput(zustand.value, GameInput.HoldCell(feld))
            halte(0.5)
        }
        marke("geschafft")
        halte(2.2)

        // 6. Ein grosses Gitter — dafuer ein neues Spiel, Level 9.
        marke("grosses-gitter")
        val gross = engine.onInput(engine.newGame(9), GameInput.Begin)
        val grossLoesung = requireNotNull(gross.puzzle).solution.toList()
        zustand.value = grossLoesung.take(5).fold(gross) { acc, pos ->
            engine.onInput(acc, GameInput.HoldCell(pos))
        }
        halte(2.4)

        File(ziel.parentFile, "marken.txt").writeText(
            marken.joinToString("\n") { "${it.second}\t${it.first}" } + "\n",
        )
        println("Werbefilm: $nummer Bilder in ${ziel.absolutePath}")
    }

    private val marken = mutableListOf<Pair<String, Int>>()

    /** Haelt fest, bei welchem Bild eine Szene beginnt — fuer die Schrift danach. */
    private fun marke(name: String) {
        marken += name to nummer
    }

    /** Hält den laufenden Stand für [sekunden] und schreibt dabei jedes Bild. */
    private fun halte(sekunden: Double) {
        repeat((sekunden * bilderJeSekunde).toInt()) {
            regel.mainClock.advanceTimeBy(msJeBild)
            val inhalt = regel.activity.findViewById<View>(android.R.id.content)
            val bild = Bitmap.createBitmap(inhalt.width, inhalt.height, Bitmap.Config.ARGB_8888)
            inhalt.draw(Canvas(bild))
            File(ziel, "%05d.png".format(nummer++)).outputStream().use {
                bild.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bild.recycle()
        }
    }

    /** Der Spielbildschirm, wie ihn die App zeigt — mit stummen Knöpfen. */
    @Composable
    private fun Bühne(state: GameState) {
        GameContent(
            state = state,
            isPreparing = false,
            bestScore = 8_575,
            tagesPunkte = 480,
            profile = PlayerProfile(),
            showSoundSettings = false,
            onTapCell = {},
            onHoldCell = {},
            onUseFairyDust = {},
            onUseIrrlicht = {},
            onUseFeenkreis = {},
            onBegin = {},
            onNextLevel = {},
            onOpenLevelSelect = {},
            onRetryLevel = {},
            globalLives = GlobalLivesState(GlobalLives.MAX, 0L),
            nextDustInMillis = 0L,
            nextIrrlichtInMillis = 0L,
            nextFeenkreisInMillis = 0L,
            adsUnlocked = false,
            adOffer = AdOffer.Available,
            onWatchAdForFairyDust = {},
            onWatchAdForIrrlicht = {},
            onWatchAdForLife = {},
            onOpenGiftForFairyDust = {},
            onOpenGiftForIrrlicht = {},
            onWatchAdForFeenkreis = {},
            onOpenGiftForLife = {},
            onOpenSoundSettings = {},
            onCloseSoundSettings = {},
            onMusicChange = {},
            onSoundChange = {},
            onVoiceChange = {},
            onOpenTutorial = {},
            onClearBoard = {},
        )
    }
}
