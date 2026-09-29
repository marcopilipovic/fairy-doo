package ug.humb.fairydoku.film

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ug.humb.fairydoku.game.FairySpecies
import ug.humb.fairydoku.ui.sprites.drawableRes
import java.io.File

/**
 * Gibt die zehn Feen einzeln als PNG mit Alphakanal heraus.
 *
 * Gebraucht wird das außerhalb der App: für Filme, Aushänge, Beiträge. Auf
 * diesem Rechner ist kein SVG-Wandler installiert — kein rsvg-convert, kein
 * Inkscape, kein ImageMagick —, und Python hat weder PIL noch numpy. An die
 * Vektorfassungen unter `Bilder/feen-schlicht/` kommt von außen also niemand
 * heran.
 *
 * Android kann es: Die Zeichnungen liegen ohnehin als VectorDrawable vor, und
 * Robolectric zeichnet sie auf diesem Rechner in eine Bitmap. Kein Emulator,
 * kein Telefon.
 *
 * Wichtig ist der durchsichtige Hintergrund. Die Bitmap beginnt leer, und
 * gezeichnet wird nur die Figur selbst — kein Thema, keine Fläche darunter.
 * Wer sie in einen Film legt, bekommt die Fee und nicht ihren Kasten.
 *
 * **Was diese Probe NICHT sieht:** ob die Feen *gut aussehen*. Sie prüft, dass
 * eine Datei entsteht, dass sie nicht leer ist und dass ihre Ecke durchsichtig
 * bleibt — mehr nicht. Eine Fee, die falsch gezeichnet, verzerrt oder in der
 * falschen Farbe herauskommt, besteht diese Probe anstandslos. Dafür muss
 * jemand hinsehen.
 *
 * Läuft nur auf Anforderung, wie die übrigen Werkzeuge in diesem Ordner:
 *
 *     ./gradlew testDebugUnitTest --tests '*FeenbilderTest*' -Dwerbefilm=ja
 *
 * Die Bilder liegen danach in `app/build/feen/`.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "xxhdpi")
class FeenbilderTest {

    /**
     * Viermal das Raster aus `FairyArt` (120 × 164). Groß genug, um in einem
     * Hochkantfilm formatfüllend zu stehen, und ein glattes Vielfaches — beim
     * Verkleinern entstehen so keine Treppen.
     */
    private val breite = 480
    private val hoehe = 656

    private val ziel = File("build/feen")

    @Test
    fun `die zehn Feen einzeln, mit durchsichtigem Hintergrund`() {
        assumeTrue(
            "Nur auf Anforderung: -Dwerbefilm=ja",
            System.getProperty("werbefilm") == "ja",
        )
        ziel.mkdirs()

        val zusammenhang = ApplicationProvider.getApplicationContext<android.content.Context>()

        for (fee in FairySpecies.entries) {
            val zeichnung = ContextCompat.getDrawable(zusammenhang, fee.drawableRes)
            requireNotNull(zeichnung) { "${fee.name} hat keine Zeichnung" }

            val bild = Bitmap.createBitmap(breite, hoehe, Bitmap.Config.ARGB_8888)
            zeichnung.setBounds(0, 0, breite, hoehe)
            zeichnung.draw(Canvas(bild))

            // Die Ecke muss leer bleiben. Malte hier etwas eine Fläche, wäre
            // der Alphakanal dahin und die Datei im Film ein Klotz.
            assertTrue(
                "${fee.name} hat einen undurchsichtigen Hintergrund",
                Bitmap.createBitmap(bild, 0, 0, 1, 1).getPixel(0, 0) == 0,
            )

            val datei = File(ziel, "${fee.name.lowercase()}.png")
            datei.outputStream().use { bild.compress(Bitmap.CompressFormat.PNG, 100, it) }
            println("Fee ${fee.name}: ${datei.length() / 1024} KB")
            assertTrue("${fee.name} ist leer", datei.length() > 1_000)
            bild.recycle()
        }
    }
}
