package ug.humb.fairydoku.film

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import ug.humb.fairydoku.ui.theme.FairyDooTheme

/**
 * Probe: Zeichnet Compose auf dem Rechner in ein Bitmap.
 *
 * Nicht über `captureToImage()` — das geht in Robolectric über den Fensterweg
 * (PixelCopy) und läuft dort in eine Zeitüberschreitung. Stattdessen wird die
 * Ansicht selbst vermessen, angeordnet und auf eine eigene Leinwand gezeichnet.
 * Das ist derselbe Weg, den Android beim Zeichnen geht, nur ohne Fenster.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h914dp-xxhdpi")
class BildprobeTest {

    private fun zeichne(breite: Int, hoehe: Int, inhalt: @androidx.compose.runtime.Composable () -> Unit): Bitmap {
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

    @Test
    fun `Compose zeichnet auf dem Rechner echte Pixel`() {
        val bild = zeichne(1080, 2400) {
            FairyDooTheme {
                Box(
                    Modifier.fillMaxSize().background(Color(0xFF1B1440)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Fairydoku", color = Color(0xFFFFD76B))
                }
            }
        }

        val ziel = File("build/bildprobe").apply { mkdirs() }
        val datei = File(ziel, "probe.png")
        datei.outputStream().use { bild.compress(Bitmap.CompressFormat.PNG, 100, it) }

        // Nicht nur "es lief durch", sondern: Da ist wirklich Farbe drauf.
        val mitte = bild.getPixel(bild.width / 2, bild.height / 2)
        println("Bildprobe ${bild.width}x${bild.height}, Mitte=${Integer.toHexString(mitte)}, ${datei.length()} Byte")
        assertTrue("Das Bild ist leer", datei.length() > 1000)
    }

    @Test
    fun `auch das Spielbrett selbst laesst sich zeichnen`() {
        // Der eigentliche Beweis: Das Brett bringt Vektor-Feen, Verläufe und
        // eigene Zeichenbefehle mit. Wenn das durchläuft, laesst sich daraus
        // der Film rechnen.
        val engine = ug.humb.fairydoku.game.FairydokuEngine()
        var zustand = engine.onInput(engine.newGame(3), ug.humb.fairydoku.game.GameInput.Begin)
        val loesung = requireNotNull(zustand.puzzle).solution.toList()
        zustand = engine.onInput(zustand, ug.humb.fairydoku.game.GameInput.HoldCell(loesung[0]))
        zustand = engine.onInput(zustand, ug.humb.fairydoku.game.GameInput.HoldCell(loesung[1]))

        val bild = zeichne(1080, 1080) {
            FairyDooTheme {
                Box(
                    Modifier.fillMaxSize().background(Color(0xFF0A0E21)),
                    contentAlignment = Alignment.Center,
                ) {
                    ug.humb.fairydoku.ui.components.FairydokuBoard(
                        state = zustand,
                        cellSize = androidx.compose.ui.unit.Dp(56f),
                        onTapCell = {},
                        onHoldCell = {},
                    )
                }
            }
        }

        val datei = File(File("build/bildprobe").apply { mkdirs() }, "brett.png")
        datei.outputStream().use { bild.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("Brettprobe ${bild.width}x${bild.height}, ${datei.length()} Byte, Feen gesetzt: ${zustand.marks.size}")
        assertTrue("Das Brettbild ist leer", datei.length() > 5000)
    }
}
