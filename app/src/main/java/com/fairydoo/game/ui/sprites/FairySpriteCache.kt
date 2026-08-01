package com.fairydoo.game.ui.sprites

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fairydoo.game.game.FairySpecies

/**
 * Die geladenen Feen-Bilder.
 *
 * Bewusst ein `object` statt `remember`: Die Bilder überleben Recomposition,
 * Levelwechsel und Neustart der Activity. Zehn Bilder à 256×256 Bildpunkte sind
 * zusammen rund 2,5 MB — ein Bruchteil dessen, was Compose ohnehin belegt, und
 * jede Verwaltung, die sie wieder freigäbe, wäre teurer als der Speicher.
 *
 * Gehalten wird der Anwendungskontext nicht: Die Bilder werden über einen
 * übergebenen Kontext geladen und danach nur noch als [ImageBitmap] behalten —
 * es kann also nichts auslaufen.
 */
object FairySpriteCache {

    private val bitmaps = HashMap<FairySpecies, ImageBitmap>(FairySpecies.entries.size)

    fun bitmapOf(context: Context, species: FairySpecies): ImageBitmap =
        bitmaps.getOrPut(species) {
            val options = BitmapFactory.Options().apply {
                // Die Bilder liegen in drawable-nodpi und sollen exakt in ihrer
                // Originalauflösung geladen werden — sonst skaliert Android sie
                // je nach Gerätedichte vor und macht die Pixelkanten weich.
                inScaled = false
            }
            BitmapFactory
                .decodeResource(context.resources, species.drawableRes, options)
                .asImageBitmap()
        }

    /**
     * Lädt die Feen eines Bretts vorab.
     *
     * Ohne das entstünden die Bilder beim allerersten Setzen einer Fee mitten in
     * der Zeichenphase.
     */
    fun warmUp(context: Context, species: Iterable<FairySpecies>) =
        species.forEach { bitmapOf(context, it) }
}
