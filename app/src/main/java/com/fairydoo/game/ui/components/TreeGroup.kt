package com.fairydoo.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Eine kleine Baumgruppe aus der Design-Vorlage `Bilder/baumgruppe.svg`
 * (viewBox 280×190) — Pfade 1:1 übernommen: zwei Tannen unterschiedlicher
 * Größe, ein Laubbaum dazwischen, ein angedeuteter Waldboden und ein
 * goldener Schein im Hintergrund. Ersetzt die einzelnen Emoji-Baumcluster
 * am Feenpfad durch eine in sich stimmige Illustration.
 */
@Composable
fun TreeGroup(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val viewboxWidth = 280f
        val viewboxHeight = 190f
        val scale = minOf(size.width / viewboxWidth, size.height / viewboxHeight)
        val offsetX = (size.width - viewboxWidth * scale) / 2f
        val offsetY = (size.height - viewboxHeight * scale) / 2f

        withTransform({
            translate(offsetX, offsetY)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            // Goldener Schein im Hintergrund.
            drawOval(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFD76B).copy(alpha = 0.22f),
                        1f to Color(0xFFFFD76B).copy(alpha = 0f),
                    ),
                    center = Offset(140f, 120f),
                    radius = 138f,
                ),
                topLeft = Offset(2f, 58f),
                size = Size(276f, 136f),
            )

            // Rechte, kleinere Tanne mit Stamm.
            drawTrunk(x = 203f, y = 130f, w = 12f, h = 34f)
            drawFirTree(x = 167f, y = 34f, w = 84f, h = 119f, opacity = 0.85f)

            // Laubbaum in der Mitte mit Stamm.
            drawTrunk(x = 128f, y = 126f, w = 13f, h = 40f)
            val leafy = Path().apply {
                moveTo(134f, 132f)
                cubicTo(104f, 132f, 88f, 114f, 92f, 94f)
                cubicTo(74f, 82f, 82f, 56f, 102f, 54f)
                cubicTo(106f, 34f, 134f, 26f, 148f, 40f)
                cubicTo(170f, 32f, 190f, 50f, 184f, 70f)
                cubicTo(202f, 82f, 196f, 112f, 172f, 116f)
                cubicTo(168f, 130f, 150f, 136f, 134f, 132f)
                close()
            }
            drawPath(
                leafy,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(0f to Color(0xFF4D8F6A), 1f to Color(0xFF1E3F2C)),
                    start = Offset(99.6f, 26f),
                    end = Offset(187.6f, 136f),
                ),
            )
            drawPath(
                leafy,
                color = Color(0xFF8FD6A8).copy(alpha = 0.45f),
                style = Stroke(width = 2.5f),
            )

            // Linke, größere Tanne mit Stamm.
            drawTrunk(x = 57f, y = 140f, w = 14f, h = 32f)
            drawFirTree(x = 15f, y = 24f, w = 99f, h = 140f)

            // Waldboden-Andeutung.
            val ground = Path().apply {
                moveTo(4f, 168f)
                cubicTo(60f, 158f, 110f, 176f, 160f, 168f)
                cubicTo(206f, 161f, 250f, 172f, 276f, 166f)
            }
            drawPath(
                ground,
                color = Color(0xFF4F8F5F).copy(alpha = 0.7f),
                style = Stroke(width = 5f, cap = StrokeCap.Round),
            )

            // Glühwürmchen-Funken.
            val sparkles = listOf(
                Offset(118f, 46f) to 2.6f,
                Offset(212f, 86f) to 2.2f,
                Offset(34f, 72f) to 2f,
                Offset(160f, 140f) to 2.4f,
                Offset(252f, 122f) to 1.8f,
                Offset(88f, 152f) to 1.8f,
            )
            sparkles.forEach { (center, radius) ->
                drawCircle(color = Color(0xFFFFE9A8), radius = radius, center = center)
            }
        }
    }
}

private fun DrawScope.drawTrunk(x: Float, y: Float, w: Float, h: Float) {
    drawRect(
        brush = Brush.linearGradient(
            colorStops = arrayOf(0f to Color(0xFF7A5A34), 1f to Color(0xFF4A3520)),
            start = Offset(x, y),
            end = Offset(x + w, y),
        ),
        topLeft = Offset(x, y),
        size = Size(w, h),
    )
}

/**
 * Die wiederverwendete Tannen-Silhouette aus dem `<symbol id="fir">` der
 * Vorlage — dort per `<use>` zweimal in unterschiedlicher Größe platziert,
 * hier als Funktion mit denselben zwei Aufrufstellen.
 */
private fun DrawScope.drawFirTree(x: Float, y: Float, w: Float, h: Float, opacity: Float = 1f) {
    val scaleX = w / 120f
    val scaleY = h / 170f
    withTransform({
        translate(x, y)
        scale(scaleX, scaleY, pivot = Offset.Zero)
    }) {
        val path = Path().apply {
            moveTo(60f, 6f)
            lineTo(92f, 58f)
            lineTo(78f, 58f)
            lineTo(102f, 102f)
            lineTo(86f, 102f)
            lineTo(112f, 152f)
            lineTo(8f, 152f)
            lineTo(34f, 102f)
            lineTo(18f, 102f)
            lineTo(42f, 58f)
            lineTo(28f, 58f)
            close()
        }
        drawPath(
            path,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0f to Color(0xFF3F7D63),
                    0.55f to Color(0xFF28563F),
                    1f to Color(0xFF173525),
                ),
                start = Offset(28.8f, 6f),
                end = Offset(101.6f, 152f),
            ),
            alpha = opacity,
        )
        drawPath(
            path,
            color = Color(0xFF8FD6A8).copy(alpha = 0.45f * opacity),
            style = Stroke(width = 2.5f),
        )
    }
}
