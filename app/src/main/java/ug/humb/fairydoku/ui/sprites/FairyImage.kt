package ug.humb.fairydoku.ui.sprites

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import ug.humb.fairydoku.game.FairySpecies

/**
 * Eine Fee außerhalb des Spielbretts — im Titel, in der Anleitung, in den
 * Overlays.
 *
 * Es sind dieselben Illustrationen wie auf dem Brett. Vorher stand an diesen
 * Stellen ein 🧚-Emoji: Das zeichnet jedes Gerät in seiner eigenen Schrift, mal
 * rundlich, mal eckig, nie in unseren Farben — direkt neben den gezeichneten
 * Feen des Spielfelds sah es aus, als stammte es aus einem anderen Spiel.
 *
 * Angegeben wird die **Höhe**, nicht die Breite: Die Figuren sind hochformatig
 * und unterschiedlich breit (Ignis' Flügel greifen weiter aus als Terras). Über
 * die Höhe stehen mehrere nebeneinander gleich groß da; die Breite folgt dem
 * Seitenverhältnis des Bildes, so wie auf dem Brett auch.
 */
@Composable
fun FairyImage(
    species: FairySpecies,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap = remember(species) { FairySpriteCache.bitmapOf(context, species) }

    Image(
        bitmap = bitmap,
        contentDescription = species.displayName,
        modifier = modifier
            .height(height)
            .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat()),
        contentScale = ContentScale.Fit,
        // Weiche Illustration statt Pixel-Art — hier zählt eine glatte Kante
        // beim Skalieren, genau wie beim Zeichnen auf dem Brett.
        filterQuality = FilterQuality.High,
    )
}

/**
 * Steht in einem Text für „hier gehört eine Fee hin".
 *
 * Mitten im Satz lässt sich kein Bild unterbringen, ohne den Satz zu zerlegen.
 * Die Texte in `GameCopy` sollen aber weiterhin reine Zeichenketten sein — dort
 * liegt die gesamte Ansprache des Spiels an einem Ort, und eine spätere
 * Übersetzung soll es mit Sätzen zu tun haben, nicht mit Compose-Bausteinen.
 *
 * Deshalb dieser Platzhalter: Der Text sagt, *wo* die Fee steht, und
 * [fairyText] setzt sie dort ein.
 */
const val FAIRY_TOKEN = "{fee}"

private const val FAIRY_INLINE_ID = "fee"

/** Ersetzt jedes [FAIRY_TOKEN] durch die Stelle, an die das Bild eingesetzt wird. */
fun fairyText(raw: String): AnnotatedString = buildAnnotatedString {
    val parts = raw.split(FAIRY_TOKEN)
    parts.forEachIndexed { index, part ->
        append(part)
        // Der Ersatztext greift, wenn kein `inlineContent` mitgegeben wurde —
        // dann steht dort wenigstens ein Wort statt einer Lücke.
        if (index < parts.lastIndex) appendInlineContent(FAIRY_INLINE_ID, "Fee")
    }
}

/**
 * Das Bild zu [fairyText], passend zur Schriftgröße der Zeile.
 *
 * [height] ist in `sp` statt `dp`, damit die Fee mitwächst, wenn jemand die
 * Systemschrift vergrößert — sonst schrumpfte sie neben dem Text zusammen.
 */
@Composable
fun fairyInlineContent(
    species: FairySpecies,
    height: TextUnit,
): Map<String, InlineTextContent> {
    val context = LocalContext.current
    val bitmap = remember(species) { FairySpriteCache.bitmapOf(context, species) }

    return mapOf(
        FAIRY_INLINE_ID to InlineTextContent(
            Placeholder(
                width = height * (bitmap.width.toFloat() / bitmap.height.toFloat()),
                height = height,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
            ),
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High,
            )
        },
    )
}
