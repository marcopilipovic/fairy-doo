package com.fairydoo.game.ui.sprites

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fairydoo.game.ui.theme.ZoneStyle

/**
 * Die geladenen Zonenkacheln.
 *
 * Dieselbe Bauart wie [FairySpriteCache]: ein `object`, damit die Bilder
 * Recomposition, Levelwechsel und den Neustart der Activity überleben. Anders
 * als die Feen sind Zonenkacheln aber groß — eine 1024×1024-Kachel belegt
 * entpackt vier Megabyte, zehn davon vierzig. Deshalb werden sie beim Laden
 * verkleinert.
 *
 * Wie stark, entscheidet [SAMPLE_SIZE]: Eine Kachel deckt [TILE_CELLS] Felder
 * je Kante ab, ein Feld ist auf dem größten Brett rund 44 dp groß. Selbst auf
 * einem sehr dichten Display braucht die Kachel damit keine 1024 Bildpunkte —
 * die Hälfte genügt, und der Speicher sinkt auf ein Viertel.
 */
object ZoneImageCache {

    /**
     * Wie viele Felder eine Kachel je Kante abdeckt.
     *
     * Läge in jedem Feld dieselbe Kachel, entstünde ein sichtbares Raster —
     * genau der Kacheleffekt, den die Fläche vermeiden soll. Über drei Felder
     * gestreckt wiederholt sich das Bild selten genug, dass es niemandem
     * auffällt, und bleibt scharf genug, dass die Beschaffenheit erkennbar ist.
     */
    const val TILE_CELLS = 3

    private const val SAMPLE_SIZE = 2

    private val bitmaps = HashMap<Int, ImageBitmap>()

    /** Die Kachel eines Gebiets, oder `null`, wenn dafür noch keine vorliegt. */
    fun bitmapOf(context: Context, style: ZoneStyle): ImageBitmap? {
        val resId = style.image ?: return null
        bitmaps[resId]?.let { return it }

        val options = BitmapFactory.Options().apply {
            // Ohne das skalierte Android die Kachel je nach Gerätedichte vor und
            // machte aus einer nahtlosen Kachel eine mit weichen Rändern.
            inScaled = false
            inSampleSize = SAMPLE_SIZE
        }
        val decoded = BitmapFactory.decodeResource(context.resources, resId, options)
            ?: return null

        return decoded.asImageBitmap().also { bitmaps[resId] = it }
    }

    /**
     * Lädt die Kacheln eines Bretts vorab.
     *
     * Ohne das entstünde die erste Kachel mitten in der Zeichenphase — bei
     * mehreren Megabyte je Bild ein sichtbares Stocken beim Levelwechsel.
     */
    fun warmUp(context: Context, styles: Iterable<ZoneStyle>) =
        styles.forEach { bitmapOf(context, it) }
}
