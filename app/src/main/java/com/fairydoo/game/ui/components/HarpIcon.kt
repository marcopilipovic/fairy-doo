package com.fairydoo.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Harfe aus der Design-Vorlage `Bilder/harfe.svg` (viewBox 200×260) — Pfade
 * 1:1 übernommen, aber einfarbig statt mit Farbverlauf gefüllt, damit die
 * Harfe denselben Parchment-Ton trägt wie ❔ und 🗺️.
 */
@Composable
fun HarpIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // Die eigentliche Zeichnung sitzt nicht mittig im 200×260-viewBox der
        // Vorlage (die Vordersäule steht links, der Korpus ragt weiter nach
        // rechts als die Saiten nach links). Deshalb wird anhand der
        // tatsächlichen Bounding-Box aller Pfadpunkte zentriert statt anhand
        // des vollen viewBox-Rechtecks — sonst wirkt die Harfe im Kreis-Knopf
        // leicht nach links oben verschoben.
        val contentMinX = 30f
        val contentMinY = 26f
        val contentWidth = 151f - contentMinX
        val contentHeight = 252f - contentMinY
        val scale = minOf(size.width / contentWidth, size.height / contentHeight)
        val offsetX = (size.width - contentWidth * scale) / 2f - contentMinX * scale
        val offsetY = (size.height - contentHeight * scale) / 2f - contentMinY * scale

        withTransform({
            translate(offsetX, offsetY)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            // Klangkorpus
            val board = Path().apply {
                moveTo(133f, 108f)
                lineTo(151f, 127f)
                lineTo(99f, 246f)
                lineTo(73f, 240f)
                close()
            }
            drawPath(board, color = tint.copy(alpha = tint.alpha * 0.85f))

            // Saiten
            val strings = listOf(
                Offset(53.1f, 43.6f) to Offset(88.7f, 232.1f),
                Offset(65.2f, 44.0f) to Offset(93.6f, 221.5f),
                Offset(76.5f, 45.9f) to Offset(98.4f, 210.9f),
                Offset(87.0f, 49.2f) to Offset(103.3f, 200.2f),
                Offset(96.6f, 53.9f) to Offset(108.1f, 189.6f),
                Offset(105.5f, 60.0f) to Offset(113.0f, 179.0f),
                Offset(113.6f, 67.6f) to Offset(117.9f, 168.4f),
                Offset(120.8f, 76.5f) to Offset(122.7f, 157.8f),
                Offset(127.2f, 86.9f) to Offset(127.6f, 147.1f),
                Offset(132.9f, 98.8f) to Offset(132.4f, 136.6f),
            )
            strings.forEach { (start, end) ->
                drawLine(color = tint, start = start, end = end, strokeWidth = 1.6f, cap = StrokeCap.Round)
            }

            // Gebogener Hals
            val neck = Path().apply {
                moveTo(41f, 44f)
                cubicTo(80f, 26f, 124f, 44f, 142f, 118f)
                lineTo(130f, 124f)
                cubicTo(114f, 58f, 78f, 42f, 45f, 56f)
                close()
            }
            drawPath(neck, color = tint)

            // Gerade Vordersäule
            val pillar = Path().apply {
                moveTo(39f, 240f)
                cubicTo(35f, 160f, 37f, 92f, 41f, 44f)
                lineTo(53f, 46f)
                cubicTo(49f, 94f, 47f, 162f, 51f, 240f)
                close()
            }
            drawPath(pillar, color = tint)

            // Sockel
            drawRoundRect(
                color = tint,
                topLeft = Offset(30f, 238f),
                size = Size(80f, 14f),
                cornerRadius = CornerRadius(7f, 7f),
            )

            // Stimmwirbel
            drawCircle(color = tint, radius = 7f, center = Offset(44f, 44f))
        }
    }
}
