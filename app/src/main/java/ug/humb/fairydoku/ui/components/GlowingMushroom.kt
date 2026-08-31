package ug.humb.fairydoku.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Ein leuchtender Pilz aus der Design-Vorlage `Bilder/pilz.svg`
 * (viewBox 160×170) — Pfade 1:1 übernommen: rosa Kappe mit weißen Flecken,
 * cremefarbener Stiel, Moosbüschel am Fuß und ein weicher rosa Schein
 * drumherum.
 */
@Composable
fun GlowingMushroom(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val viewboxWidth = 160f
        val viewboxHeight = 170f
        val scale = minOf(size.width / viewboxWidth, size.height / viewboxHeight)
        val offsetX = (size.width - viewboxWidth * scale) / 2f
        val offsetY = (size.height - viewboxHeight * scale) / 2f

        withTransform({
            translate(offsetX, offsetY)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            // Weicher rosa Schein.
            drawOval(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFF9ECF).copy(alpha = 0.55f),
                        1f to Color(0xFFFF9ECF).copy(alpha = 0f),
                    ),
                    center = Offset(80f, 98f),
                    radius = 78f,
                ),
                topLeft = Offset(2f, 36f),
                size = Size(156f, 124f),
            )

            // Stiel.
            val stem = Path().apply {
                moveTo(67f, 88f)
                cubicTo(63f, 112f, 61f, 126f, 55f, 140f)
                cubicTo(69f, 147f, 91f, 147f, 105f, 140f)
                cubicTo(99f, 126f, 97f, 112f, 93f, 88f)
                close()
            }
            drawPath(
                stem,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(0f to Color(0xFFFFFBE8), 1f to Color(0xFFE6CFA0)),
                    start = Offset(55f, 88f),
                    end = Offset(105f, 105.7f),
                ),
            )
            drawPath(stem, color = Color(0xFFB8862F), style = Stroke(width = 2f))

            // Schattierung auf dem Stiel.
            val stemShade = Path().apply {
                moveTo(60f, 100f)
                cubicTo(72f, 106f, 88f, 106f, 100f, 100f)
            }
            drawPath(
                stemShade,
                color = Color(0xFFD9B877).copy(alpha = 0.8f),
                style = Stroke(width = 2.4f, cap = StrokeCap.Round),
            )

            // Lamellen-Schatten unter dem Kappenrand.
            val gill = Path().apply {
                moveTo(26f, 86f)
                cubicTo(40f, 96f, 120f, 96f, 134f, 86f)
                cubicTo(132f, 96f, 110f, 102f, 80f, 102f)
                cubicTo(50f, 102f, 28f, 96f, 26f, 86f)
                close()
            }
            drawPath(gill, color = Color(0xFFA8558C))

            // Kappe.
            val cap = Path().apply {
                moveTo(24f, 88f)
                cubicTo(24f, 42f, 48f, 20f, 80f, 20f)
                cubicTo(112f, 20f, 136f, 42f, 136f, 88f)
                cubicTo(118f, 80f, 100f, 76f, 80f, 76f)
                cubicTo(60f, 76f, 42f, 80f, 24f, 88f)
                close()
            }
            drawPath(
                cap,
                brush = Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFC7E4),
                        0.5f to Color(0xFFFF9ECF),
                        1f to Color(0xFFD95F9C),
                    ),
                    start = Offset(46.4f, 20f),
                    end = Offset(113.6f, 88f),
                ),
            )
            drawPath(cap, color = Color(0xFFB8467F), style = Stroke(width = 2.5f))

            // Helle Flecken.
            val spots = listOf(
                Triple(Offset(60f, 46f), 11f to 9f, 0.9f),
                Triple(Offset(96f, 40f), 8f to 6.5f, 0.85f),
                Triple(Offset(112f, 62f), 7f to 5.5f, 0.8f),
                Triple(Offset(40f, 68f), 6f to 5f, 0.75f),
                Triple(Offset(80f, 60f), 5.5f to 4.5f, 0.7f),
            )
            spots.forEach { (center, radii, alpha) ->
                drawOval(
                    color = Color(0xFFFFFBE8).copy(alpha = alpha),
                    topLeft = Offset(center.x - radii.first, center.y - radii.second),
                    size = Size(radii.first * 2, radii.second * 2),
                )
            }

            // Moosbüschel am Fuß.
            val mossLeft = Path().apply {
                moveTo(46f, 142f)
                cubicTo(40f, 132f, 34f, 130f, 30f, 138f)
                cubicTo(26f, 132f, 20f, 136f, 22f, 146f)
                lineTo(52f, 146f)
                close()
            }
            val mossRight = Path().apply {
                moveTo(108f, 144f)
                cubicTo(114f, 134f, 122f, 132f, 126f, 140f)
                cubicTo(132f, 136f, 138f, 140f, 136f, 148f)
                lineTo(104f, 148f)
                close()
            }
            drawPath(mossLeft, color = Color(0xFF4F8F5F))
            drawPath(mossRight, color = Color(0xFF4F8F5F))

            // Glitzerfunken.
            val sparkles = listOf(
                Offset(136f, 30f) to 2.6f,
                Offset(20f, 42f) to 2f,
                Offset(126f, 112f) to 1.8f,
                Offset(30f, 108f) to 2.2f,
            )
            sparkles.forEach { (center, radius) ->
                drawCircle(color = Color(0xFFFFE9A8), radius = radius, center = center)
            }
        }
    }
}
