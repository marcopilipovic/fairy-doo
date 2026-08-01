package com.fairydoo.game.ui.sprites

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fairydoo.game.art.PixelSprite
import com.fairydoo.game.art.SPRITE_SIZE
import com.fairydoo.game.art.sprite
import com.fairydoo.game.game.FairySpecies

/**
 * Die fertigen Sprite-Bilder.
 *
 * Bewusst ein `object` statt `remember`: Die Bilder überleben Recomposition,
 * Levelwechsel und Neustart der Activity. Zehn Feen à zwei Bilder à 32×32
 * Pixel sind zusammen rund 80 KB — jede Verwaltung, die das wieder freigäbe,
 * wäre teurer als der Speicher. Ein Kontext wird nicht gehalten, es kann also
 * nichts auslaufen.
 *
 * Gecacht wird das **unskalierte** Bild. Die Zellgröße ändert sich mit Level,
 * Gerät und Ausrichtung; ein größenabhängiger Cache bräuchte einen
 * zusammengesetzten Schlüssel und eine Invalidierung — Aufwand ohne Gewinn,
 * denn das Vergrößern per Nearest-Neighbour kostet die Grafikeinheit nichts.
 *
 * Zugriff erfolgt ausschließlich aus dem UI-Thread (Zeichenphase), deshalb
 * genügt eine einfache HashMap.
 */
object FairySpriteCache {

    private val frames = HashMap<FairySpecies, List<ImageBitmap>>(FairySpecies.entries.size)

    fun framesOf(species: FairySpecies): List<ImageBitmap> =
        frames.getOrPut(species) { species.sprite.toImageBitmaps() }

    /**
     * Baut die Bilder eines Bretts vorab.
     *
     * Ohne das entstünden sie beim allerersten Setzen einer Fee mitten in der
     * Zeichenphase.
     */
    fun warmUp(species: Iterable<FairySpecies>) = species.forEach { framesOf(it) }

    private fun PixelSprite.toImageBitmaps(): List<ImageBitmap> =
        frames.indices.map { frame ->
            val pixels = IntArray(SPRITE_SIZE * SPRITE_SIZE)
            for (y in 0 until SPRITE_SIZE) {
                for (x in 0 until SPRITE_SIZE) {
                    pixels[y * SPRITE_SIZE + x] = colorAt(frame, x, y)
                }
            }
            Bitmap.createBitmap(pixels, SPRITE_SIZE, SPRITE_SIZE, Bitmap.Config.ARGB_8888)
                .asImageBitmap()
        }
}
