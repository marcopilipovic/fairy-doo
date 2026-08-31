package ug.humb.fairydoku.ui.sprites

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import ug.humb.fairydoku.game.FairySpecies

/**
 * Die gezeichneten Feen-Bilder.
 *
 * Die Vorlagen sind seit dem Handoff „Feen schlicht" **Vektoren**
 * (`res/drawable/fairy_*.xml`), keine Pixelbilder mehr. Gezeichnet wird
 * trotzdem aus einer Bitmap: Das Spielbrett malt jede Fee mitten in seiner
 * Zeichenphase, und eine Bitmap ist dort ein Kopiervorgang, während ein Vektor
 * bei jedem Bild neu aus Pfaden aufgebaut würde. Bei bis zu acht Feen
 * gleichzeitig, jede mit Schweben und Pulsieren, ist das der Unterschied
 * zwischen ruhigem und ruckelndem Bild.
 *
 * Einmal gerastert, beliebig oft gemalt — der Vektor ist die Quelle, die
 * Bitmap der Abzug.
 *
 * Bewusst ein `object` statt `remember`: Die Bilder überleben Recomposition,
 * Levelwechsel und Neustart der Activity. Zehn Abzüge sind zusammen rund 3 MB
 * — ein Bruchteil dessen, was Compose ohnehin belegt, und jede Verwaltung, die
 * sie wieder freigäbe, wäre teurer als der Speicher.
 *
 * Gehalten wird der Anwendungskontext nicht: Die Bilder werden über einen
 * übergebenen Kontext geladen und danach nur noch als [ImageBitmap] behalten —
 * es kann also nichts auslaufen.
 */
object FairySpriteCache {

    /**
     * Kantenlänge des Abzugs.
     *
     * Im Spiel ist eine Fee höchstens rund 44 dp breit; auf einem Gerät mit
     * vierfacher Dichte sind das 176 Bildpunkte. Mit 240 bleibt Luft nach oben,
     * ohne dass zehn Abzüge den Speicher belasten. Das Seitenverhältnis ist das
     * der Vorlage (120 : 164).
     */
    private const val RENDER_WIDTH = 240
    private const val RENDER_HEIGHT = 328

    private val bitmaps = HashMap<FairySpecies, ImageBitmap>(FairySpecies.entries.size)

    fun bitmapOf(context: Context, species: FairySpecies): ImageBitmap =
        bitmaps.getOrPut(species) {
            val drawable = requireNotNull(
                ResourcesCompat.getDrawable(context.resources, species.drawableRes, context.theme),
            ) { "Feen-Zeichnung fehlt: ${species.displayName}" }

            val bitmap = Bitmap.createBitmap(RENDER_WIDTH, RENDER_HEIGHT, Bitmap.Config.ARGB_8888)
            drawable.setBounds(0, 0, RENDER_WIDTH, RENDER_HEIGHT)
            drawable.draw(Canvas(bitmap))
            bitmap.asImageBitmap()
        }

    /**
     * Lädt die Feen eines Bretts vorab.
     *
     * Ohne das entstünden die Bilder beim allerersten Setzen einer Fee mitten in
     * der Zeichenphase — und das Rastern eines Vektors kostet spürbar mehr als
     * das Entpacken eines PNG.
     */
    fun warmUp(context: Context, species: Iterable<FairySpecies>) =
        species.forEach { bitmapOf(context, it) }
}
