package com.fairydoo.game.ui.sprites

import android.content.Context
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
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

    /**
     * **Hier wurden bis eben zehn PNG-Dateien geladen.**
     *
     * Jetzt wird die Fee gezeichnet — dieselbe Vorschrift für alle zehn, nur
     * mit verschiedenen Farben, siehe [Feenbild].
     *
     * Der Grund ist kein technischer. Die alten Bilder waren aus Vorlagen
     * entstanden, die Nataly seinerzeit im Netz gefunden hatte; ob eines davon
     * einer fremden Zeichnung zu nahe kam, konnte niemand mehr sagen. „Ich hab
     * keine Lust auf Stress", und das ist bei einer Veröffentlichung die
     * richtige Haltung: Was man nicht belegen kann, nimmt man nicht mit.
     *
     * **Der Zwischenspeicher bleibt, und die beiden Aufrufer merken nichts.**
     * Sie bekommen weiterhin ein [ImageBitmap] — nur kommt es nicht mehr aus
     * einer Datei, sondern aus ein paar Kurven. Gezeichnet wird einmal je Art,
     * danach liegt das Ergebnis hier.
     *
     * `context` wird nicht mehr gebraucht, bleibt aber im Aufruf stehen: Ihn
     * zu entfernen hieße, zwei weitere Dateien anzufassen, ohne dass sich
     * etwas verbessert.
     */
    @Suppress("UNUSED_PARAMETER")
    fun bitmapOf(context: Context, species: FairySpecies): ImageBitmap =
        bitmaps.getOrPut(species) { male(species) }

    private fun male(art: FairySpecies): ImageBitmap {
        val kante = 256
        val bild = ImageBitmap(kante, kante)
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(bild),
            size = androidx.compose.ui.geometry.Size(kante.toFloat(), kante.toFloat()),
        ) {
            feeFuerProbe(art)
        }
        return bild
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
