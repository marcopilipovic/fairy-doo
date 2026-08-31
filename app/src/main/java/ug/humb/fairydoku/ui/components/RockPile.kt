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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

private val ROCK_STROKE = Color(0xFFC3CDF2).copy(alpha = 0.4f)
private val MOSS_GREEN = Color(0xFF4F8F5F)
private val SPARKLE_GOLD = Color(0xFFFFE9A8)

/**
 * Ein Steinhaufen aus der Design-Vorlage `Bilder/steinhaufen_*.svg` — Pfade
 * 1:1 übernommen. `tall` wählt zwischen der liegenden (200×130) und der
 * stehenden (170×200) Variante, `withMushrooms` blendet die winzigen Pilze
 * ein, die in der Vorlage oben auf dem Haufen sitzen.
 */
@Composable
fun RockPile(modifier: Modifier = Modifier, tall: Boolean = false, withMushrooms: Boolean = false) {
    Canvas(modifier = modifier) {
        val viewboxWidth = if (tall) 170f else 200f
        val viewboxHeight = if (tall) 200f else 130f
        val scale = minOf(size.width / viewboxWidth, size.height / viewboxHeight)
        val offsetX = (size.width - viewboxWidth * scale) / 2f
        val offsetY = (size.height - viewboxHeight * scale) / 2f

        withTransform({
            translate(offsetX, offsetY)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            if (tall) drawTallPile() else drawWidePile()

            if (withMushrooms) {
                val spots = if (tall) {
                    listOf(
                        Triple(Offset(116f, 168f), 1.2f, 0),
                        Triple(Offset(14f, 158f), 0.9f, 0),
                        Triple(Offset(96f, 116f), 0.75f, 0),
                        Triple(Offset(74f, 52f), 0.6f, 0),
                    )
                } else {
                    listOf(
                        Triple(Offset(132f, 74f), 1.15f, 0),
                        Triple(Offset(24f, 76f), 0.85f, 0),
                        Triple(Offset(84f, 34f), 0.7f, 0),
                    )
                }
                spots.forEach { (position, memberScale, _) ->
                    drawTinyMushroom(position.x, position.y, memberScale)
                }
            }
        }
    }
}

private fun DrawScope.drawWidePile() {
    drawOval(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color(0xFFFFD76B).copy(alpha = 0.18f),
                1f to Color(0xFFFFD76B).copy(alpha = 0f),
            ),
            center = Offset(100f, 86f),
            radius = 98f,
        ),
        topLeft = Offset(2f, 42f),
        size = Size(196f, 88f),
    )

    drawRockPath(
        "M18 104 C14 80 30 62 54 64 C78 66 88 84 84 104 Z",
        colorStops = arrayOf(0f to Color(0xFF9BA4CC), 0.5f to Color(0xFF5B6392), 1f to Color(0xFF333A63)),
        gradientStart = Offset(25.1f, 62f),
        gradientEnd = Offset(76.9f, 104f),
    )
    drawRockPath(
        "M90 104 C86 86 100 70 122 70 C148 70 162 86 158 104 Z",
        colorStops = arrayOf(0f to Color(0xFF8A93BD), 0.55f to Color(0xFF4E5583), 1f to Color(0xFF2A3055)),
        gradientStart = Offset(101.2f, 70f),
        gradientEnd = Offset(154.4f, 104f),
    )
    drawRockPath(
        "M62 68 C58 50 74 38 94 40 C114 42 124 56 120 70 C104 62 78 62 62 68 Z",
        colorStops = arrayOf(0f to Color(0xFFAAB3D8), 1f to Color(0xFF454C78)),
        gradientStart = Offset(64.6f, 38f),
        gradientEnd = Offset(110.8f, 70f),
    )
    drawRockPath(
        "M160 104 C158 94 166 88 176 90 C186 92 190 98 186 104 Z",
        colorStops = arrayOf(0f to Color(0xFF8A93BD), 0.55f to Color(0xFF4E5583), 1f to Color(0xFF2A3055)),
        gradientStart = Offset(164.4f, 88f),
        gradientEnd = Offset(186.8f, 104f),
    )

    val ground = Path().apply {
        moveTo(6f, 106f)
        cubicTo(48f, 100f, 90f, 110f, 132f, 105f)
        cubicTo(158f, 102f, 180f, 108f, 194f, 105f)
    }
    drawPath(ground, color = MOSS_GREEN.copy(alpha = 0.7f), style = Stroke(width = 4.5f, cap = StrokeCap.Round))

    val moss1 = Path().apply {
        moveTo(30f, 100f)
        cubicTo(26f, 92f, 34f, 88f, 40f, 92f)
        cubicTo(44f, 86f, 52f, 90f, 50f, 100f)
        close()
    }
    val moss2 = Path().apply {
        moveTo(126f, 100f)
        cubicTo(132f, 92f, 142f, 90f, 146f, 96f)
        cubicTo(150f, 92f, 156f, 96f, 154f, 102f)
        close()
    }
    drawPath(moss1, color = MOSS_GREEN.copy(alpha = 0.85f))
    drawPath(moss2, color = MOSS_GREEN.copy(alpha = 0.85f))

    listOf(Offset(176f, 52f) to 2.4f, Offset(24f, 44f) to 2f, Offset(150f, 40f) to 1.8f).forEach { (center, radius) ->
        drawCircle(color = SPARKLE_GOLD, radius = radius, center = center)
    }
}

private fun DrawScope.drawTallPile() {
    drawOval(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color(0xFFFFD76B).copy(alpha = 0.18f),
                1f to Color(0xFFFFD76B).copy(alpha = 0f),
            ),
            center = Offset(85f, 130f),
            radius = 84f,
        ),
        topLeft = Offset(1f, 60f),
        size = Size(168f, 140f),
    )

    drawRockPath(
        "M12 172 C8 152 34 140 76 140 C124 140 152 152 148 172 Z",
        colorStops = arrayOf(0f to Color(0xFF9BA4CC), 0.5f to Color(0xFF5B6392), 1f to Color(0xFF333A63)),
        gradientStart = Offset(29.6f, 140f),
        gradientEnd = Offset(130.4f, 172f),
    )
    drawRockPath(
        "M26 140 C22 124 44 114 80 114 C114 114 134 124 130 140 Z",
        colorStops = arrayOf(0f to Color(0xFF8A93BD), 0.55f to Color(0xFF4E5583), 1f to Color(0xFF2A3055)),
        gradientStart = Offset(44.4f, 114f),
        gradientEnd = Offset(122.8f, 140f),
    )
    drawRockPath(
        "M38 114 C34 100 54 92 82 92 C108 92 122 100 118 114 Z",
        colorStops = arrayOf(0f to Color(0xFFAAB3D8), 1f to Color(0xFF454C78)),
        gradientStart = Offset(42.8f, 92f),
        gradientEnd = Offset(104.4f, 114f),
    )
    drawRockPath(
        "M52 92 C48 80 64 72 84 72 C102 72 112 80 108 92 Z",
        colorStops = arrayOf(0f to Color(0xFF8A93BD), 0.55f to Color(0xFF4E5583), 1f to Color(0xFF2A3055)),
        gradientStart = Offset(60.8f, 72f),
        gradientEnd = Offset(105.6f, 92f),
    )
    drawRockPath(
        "M64 72 C60 60 72 52 86 52 C98 52 106 60 102 72 Z",
        colorStops = arrayOf(0f to Color(0xFFAAB3D8), 1f to Color(0xFF454C78)),
        gradientStart = Offset(64.6f, 52f),
        gradientEnd = Offset(96.8f, 72f),
    )

    val ground = Path().apply {
        moveTo(4f, 176f)
        cubicTo(42f, 170f, 84f, 180f, 124f, 175f)
        cubicTo(144f, 172f, 158f, 178f, 166f, 175f)
    }
    drawPath(ground, color = MOSS_GREEN.copy(alpha = 0.7f), style = Stroke(width = 4.5f, cap = StrokeCap.Round))

    val moss1 = Path().apply {
        moveTo(28f, 168f)
        cubicTo(24f, 158f, 34f, 154f, 40f, 158f)
        cubicTo(44f, 152f, 54f, 156f, 52f, 168f)
        close()
    }
    val moss2 = Path().apply {
        moveTo(114f, 138f)
        cubicTo(120f, 130f, 130f, 128f, 134f, 134f)
        cubicTo(138f, 130f, 142f, 134f, 140f, 140f)
        close()
    }
    drawPath(moss1, color = MOSS_GREEN.copy(alpha = 0.85f))
    drawPath(moss2, color = MOSS_GREEN.copy(alpha = 0.85f))

    listOf(
        Offset(146f, 60f) to 2.4f,
        Offset(20f, 84f) to 2f,
        Offset(152f, 108f) to 1.8f,
        Offset(26f, 44f) to 1.6f,
    ).forEach { (center, radius) ->
        drawCircle(color = SPARKLE_GOLD, radius = radius, center = center)
    }
}

private fun DrawScope.drawRockPath(
    d: String,
    colorStops: Array<Pair<Float, Color>>,
    gradientStart: Offset,
    gradientEnd: Offset,
) {
    val path = parseSimplePath(d)
    drawPath(path, brush = Brush.linearGradient(colorStops = colorStops, start = gradientStart, end = gradientEnd))
    drawPath(path, color = ROCK_STROKE, style = Stroke(width = 2f))
}

/** Winziger Pilz obenauf, wie in den `_pilze_*`-Varianten der Vorlage. */
private fun DrawScope.drawTinyMushroom(x: Float, y: Float, memberScale: Float) {
    withTransform({
        translate(x, y)
        scale(memberScale, memberScale, pivot = Offset.Zero)
    }) {
        drawOval(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to Color(0xFFFF9ECF).copy(alpha = 0.5f),
                    1f to Color(0xFFFF9ECF).copy(alpha = 0f),
                ),
                center = Offset(14f, 10f),
                radius = 30f,
            ),
            topLeft = Offset(-16f, -14f),
            size = Size(60f, 48f),
        )

        val stem = Path().apply {
            moveTo(10f, 12f)
            lineTo(9f, 28f)
            cubicTo(12f, 30f, 17f, 30f, 20f, 28f)
            lineTo(18f, 12f)
            close()
        }
        drawPath(stem, color = Color(0xFFFFFBE8))
        drawPath(stem, color = Color(0xFFB8862F), style = Stroke(width = 1.6f))

        val cap = Path().apply {
            moveTo(0f, 14f)
            cubicTo(0f, 3f, 6f, -3f, 14f, -3f)
            cubicTo(22f, -3f, 28f, 3f, 28f, 14f)
            cubicTo(20f, 10f, 8f, 10f, 0f, 14f)
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
                start = Offset(5.6f, -3f),
                end = Offset(22.4f, 14f),
            ),
        )
        drawPath(cap, color = Color(0xFFB8467F), style = Stroke(width = 1.8f))

        drawCircle(color = Color(0xFFFFFBE8).copy(alpha = 0.9f), radius = 3f, center = Offset(9f, 4f))
        drawCircle(color = Color(0xFFFFFBE8).copy(alpha = 0.8f), radius = 2.2f, center = Offset(19f, 6f))
    }
}

/**
 * Reicht für die schlichten `M x y C .. C .. Z`-Pfade dieser Vorlagen: nur
 * Move-, Cubic- und Close-Befehle, durch Leerzeichen getrennte Zahlen.
 * Kein allgemeiner SVG-Parser — die Handoff-Pfade brauchen keinen.
 */
private fun parseSimplePath(d: String): Path {
    // Befehlsbuchstaben stehen in der Vorlage oft ohne Leerzeichen direkt vor
    // der ersten Zahl (z. B. "M18 104"), deshalb erst Leerzeichen um jeden
    // Buchstaben einfügen, bevor auf Whitespace/Komma aufgeteilt wird.
    val spaced = d.replace(Regex("([A-Za-z])"), " $1 ")
    val tokens = spaced.trim().split(Regex("[\\s,]+")).filter { it.isNotEmpty() }
    val path = Path()
    var i = 0
    while (i < tokens.size) {
        when (tokens[i]) {
            "M" -> {
                path.moveTo(tokens[i + 1].toFloat(), tokens[i + 2].toFloat())
                i += 3
            }
            "C" -> {
                path.cubicTo(
                    tokens[i + 1].toFloat(), tokens[i + 2].toFloat(),
                    tokens[i + 3].toFloat(), tokens[i + 4].toFloat(),
                    tokens[i + 5].toFloat(), tokens[i + 6].toFloat(),
                )
                i += 7
            }
            "Z" -> {
                path.close()
                i += 1
            }
            else -> i += 1
        }
    }
    return path
}
