package com.fairydoo.game.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.theme.ZoneTexture

/**
 * Zeichnet das Motiv einer Zone über ihre Fläche.
 *
 * Alle Maße hängen an der Feldgröße statt an festen Punktwerten: Dasselbe Motiv
 * muss auf dem 4×4-Brett mit großen Feldern und auf dem 10×10 mit kleinen
 * gleich aussehen.
 *
 * [pos] geht in die Streuung ein, damit benachbarte Felder derselben Zone nicht
 * identisch aussehen. Ohne das zerfiele die Zone optisch in ihre Felder — man
 * sähe eine gekachelte Tapete statt eines zusammenhängenden Gebiets. Die
 * Streuung ist berechnet und nicht zufällig, damit ein Feld bei jedem Neuzeichnen
 * dasselbe Bild zeigt.
 */
fun DrawScope.drawZoneTexture(texture: ZoneTexture, color: Color, pos: Pos) {
    val unit = size.minDimension
    val stroke = unit * 0.045f

    // Zwei voneinander unabhängige Streuwerte je Feld, beide aus der Position.
    // Teilerfremde Faktoren, damit sich das Muster nicht schon nach zwei
    // Feldern wiederholt.
    val varyA = ((pos.row * 7 + pos.col * 3) % 5) / 5f
    val varyB = ((pos.row * 5 + pos.col * 11) % 4) / 4f

    when (texture) {
        ZoneTexture.Sparkles -> {
            // Vierzackige Funken mit eingezogenen Flanken. Gerade Striche wären
            // hier fatal: Ein Kreuz aus zwei Linien sieht aus wie die Markierung
            // „hier keine Fee" — ausgerechnet das Zeichen, das der Spieler
            // selbst setzt.
            sparkle(color, Offset(unit * (0.26f + varyA * 0.08f), unit * 0.30f), unit * 0.15f)
            sparkle(color, Offset(unit * 0.74f, unit * (0.64f + varyB * 0.10f)), unit * 0.10f)
            drawCircle(color, unit * 0.026f, Offset(unit * 0.62f, unit * 0.20f))
            drawCircle(color, unit * 0.018f, Offset(unit * 0.36f, unit * 0.76f))
            drawCircle(color, unit * 0.016f, Offset(unit * 0.88f, unit * 0.88f))
        }

        ZoneTexture.Berries -> {
            // Drei Beeren an einem Stiel, dazu eine schräge Ranke.
            val cluster = Offset(unit * (0.30f + varyA * 0.12f), unit * (0.32f + varyB * 0.10f))
            val berry = unit * 0.075f
            drawCircle(color, berry, cluster)
            drawCircle(color, berry, cluster + Offset(berry * 1.7f, berry * 0.9f))
            drawCircle(color, berry * 0.85f, cluster + Offset(berry * 0.7f, berry * 2.2f))
            drawLine(
                color,
                cluster + Offset(-berry, -berry * 1.6f),
                cluster + Offset(berry * 2.4f, berry * 0.2f),
                stroke * 0.7f,
                cap = StrokeCap.Round,
            )
            // Ranke quer durchs Feld, damit die Zone zusammenhängt.
            drawLine(
                color,
                Offset(0f, size.height * (0.75f + varyB * 0.15f)),
                Offset(size.width, size.height * (0.55f + varyA * 0.15f)),
                stroke * 0.8f,
                cap = StrokeCap.Round,
            )
            drawCircle(color, berry * 0.8f, Offset(unit * 0.78f, unit * (0.68f + varyA * 0.08f)))
        }

        ZoneTexture.FallenLeaves -> {
            leaf(color, Offset(unit * 0.28f, unit * 0.26f), unit * 0.42f, 25f + varyA * 40f, stroke)
            leaf(color, Offset(unit * 0.74f, unit * 0.56f), unit * 0.36f, 200f + varyB * 50f, stroke)
            leaf(color, Offset(unit * 0.30f, unit * 0.80f), unit * 0.30f, 115f + varyA * 30f, stroke)
            leaf(color, Offset(unit * 0.88f, unit * 0.16f), unit * 0.24f, 300f + varyB * 30f, stroke)
        }

        ZoneTexture.Waves -> {
            // Durchlaufende Wellenlinien: Sie setzen sich im Nachbarfeld fort,
            // weil Höhe und Phase nur an der Zeile hängen, nicht an der Spalte.
            val lines = 4
            repeat(lines) { index ->
                val y = size.height * (index + 0.5f) / lines
                val path = Path().apply {
                    moveTo(0f, y)
                    val step = size.width / 4f
                    var x = 0f
                    var up = (index + pos.row) % 2 == 0
                    while (x < size.width) {
                        val peak = if (up) -unit * 0.05f else unit * 0.05f
                        quadraticBezierTo(x + step / 2f, y + peak, x + step, y)
                        x += step
                        up = !up
                    }
                }
                drawPath(path, color, style = Stroke(width = stroke * 0.9f, cap = StrokeCap.Round))
            }
        }

        ZoneTexture.Thorns -> {
            // Bögen mit kurzen Dornen — sie greifen über den Feldrand hinaus
            // und verhaken sich dadurch mit dem Nachbarfeld.
            clipRect {
                repeat(2) { index ->
                    val radius = unit * (0.42f + index * 0.24f + varyA * 0.06f)
                    val center = Offset(size.width * varyB, size.height * (1f - varyA * 0.4f))
                    drawCircle(
                        color = color,
                        radius = radius,
                        center = center,
                        style = Stroke(width = stroke),
                    )
                    // Dornen auf dem Bogen.
                    for (step in 0..5) {
                        val angle = Math.toRadians((step * 60 + index * 25).toDouble())
                        val onArc = Offset(
                            center.x + (kotlin.math.cos(angle) * radius).toFloat(),
                            center.y + (kotlin.math.sin(angle) * radius).toFloat(),
                        )
                        val outward = Offset(
                            (kotlin.math.cos(angle) * unit * 0.07f).toFloat(),
                            (kotlin.math.sin(angle) * unit * 0.07f).toFloat(),
                        )
                        drawLine(color, onArc, onArc + outward, stroke * 0.8f, cap = StrokeCap.Round)
                    }
                }
            }
        }

        ZoneTexture.CrystalCells -> {
            // Unregelmäßiges Mauerwerk aus gerundeten Zellen.
            val rows = 2
            val cols = 2
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val inset = unit * 0.06f
                    val cellWidth = size.width / cols
                    val cellHeight = size.height / rows
                    // Jede zweite Reihe versetzt, wie bei einer Mauer.
                    val shift = if ((row + pos.row) % 2 == 0) 0f else cellWidth / 2f
                    val left = col * cellWidth + shift - cellWidth / 2f
                    drawRoundedCell(
                        color = color,
                        rect = Rect(
                            left + inset,
                            row * cellHeight + inset,
                            left + cellWidth - inset,
                            (row + 1) * cellHeight - inset,
                        ),
                        corner = unit * 0.12f,
                        stroke = stroke,
                    )
                }
            }
        }

        ZoneTexture.Sunflowers -> {
            sunflower(color, Offset(unit * (0.34f + varyA * 0.08f), unit * 0.34f), unit * 0.20f, stroke)
            sunflower(color, Offset(unit * 0.76f, unit * (0.74f + varyB * 0.06f)), unit * 0.14f, stroke * 0.85f)
        }

        ZoneTexture.PineNeedles -> {
            pineSprig(color, Offset(unit * 0.30f, unit * 0.24f), unit * 0.34f, 20f + varyA * 25f, stroke)
            pineSprig(color, Offset(unit * 0.66f, unit * 0.58f), unit * 0.30f, 195f + varyB * 30f, stroke)
        }

        ZoneTexture.Speckles -> {
            // Feiner Moosteppich: viele kleine Tupfen in zwei Größen.
            for (index in 0 until 14) {
                val a = ((index * 37 + pos.row * 13 + pos.col * 29) % 100) / 100f
                val b = ((index * 61 + pos.row * 7 + pos.col * 17) % 100) / 100f
                val radius = if (index % 3 == 0) unit * 0.030f else unit * 0.018f
                drawCircle(color, radius, Offset(size.width * a, size.height * b))
            }
        }

        ZoneTexture.Constellations -> {
            // Sterne mit Verbindungslinien — und ab und zu eine Sternschnuppe.
            val stars = listOf(
                Offset(unit * (0.22f + varyA * 0.08f), unit * 0.26f),
                Offset(unit * 0.52f, unit * (0.16f + varyB * 0.10f)),
                Offset(unit * 0.74f, unit * 0.44f),
                Offset(unit * (0.38f + varyB * 0.06f), unit * 0.62f),
                Offset(unit * 0.68f, unit * 0.82f),
            )
            for (index in 0 until stars.size - 1) {
                drawLine(color, stars[index], stars[index + 1], stroke * 0.5f)
            }
            stars.forEachIndexed { index, star ->
                drawCircle(color, if (index % 2 == 0) unit * 0.035f else unit * 0.022f, star)
            }
            if ((pos.row + pos.col) % 3 == 0) {
                val tail = Offset(unit * 0.86f, unit * 0.14f)
                drawLine(color, tail, tail + Offset(-unit * 0.22f, unit * 0.16f), stroke * 0.7f, cap = StrokeCap.Round)
                drawCircle(color, unit * 0.030f, tail)
            }
        }
    }
}

/**
 * Ein vierzackiger Funke.
 *
 * Die Flanken sind zur Mitte hin eingezogen — daher die Kontrollpunkte nahe am
 * Zentrum. Genau das macht den Unterschied zwischen einem Funkeln und einem
 * Kreuz: Die Zacken laufen spitz zu, statt gleich breit zu bleiben.
 */
private fun DrawScope.sparkle(color: Color, center: Offset, radius: Float) {
    val waist = radius * 0.22f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x + waist, center.y - waist, center.x + radius, center.y)
        quadraticBezierTo(center.x + waist, center.y + waist, center.x, center.y + radius)
        quadraticBezierTo(center.x - waist, center.y + waist, center.x - radius, center.y)
        quadraticBezierTo(center.x - waist, center.y - waist, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}

/** Ein Blatt: zwei Bögen zu einer Spitze, mit Mittelrippe. */
private fun DrawScope.leaf(
    color: Color,
    center: Offset,
    length: Float,
    degrees: Float,
    stroke: Float,
) {
    rotate(degrees, center) {
        val half = length / 2f
        val width = length * 0.42f
        val path = Path().apply {
            moveTo(center.x, center.y - half)
            quadraticBezierTo(center.x + width, center.y, center.x, center.y + half)
            quadraticBezierTo(center.x - width, center.y, center.x, center.y - half)
            close()
        }
        drawPath(path, color)
        // Mittelrippe in der Flächenfarbe ausgespart wäre teuer — eine dünne
        // Linie in derselben Tinte genügt für den Eindruck.
        drawLine(
            color.copy(alpha = color.alpha * 0.5f),
            Offset(center.x, center.y - half * 0.8f),
            Offset(center.x, center.y + half * 0.8f),
            stroke * 0.6f,
        )
    }
}

/** Eine Sonnenblume: Kern mit Strahlenkranz. */
private fun DrawScope.sunflower(color: Color, center: Offset, radius: Float, stroke: Float) {
    val petals = 10
    repeat(petals) { index ->
        val angle = Math.toRadians((index * 360.0 / petals))
        val inner = Offset(
            center.x + (kotlin.math.cos(angle) * radius * 0.42f).toFloat(),
            center.y + (kotlin.math.sin(angle) * radius * 0.42f).toFloat(),
        )
        val outer = Offset(
            center.x + (kotlin.math.cos(angle) * radius).toFloat(),
            center.y + (kotlin.math.sin(angle) * radius).toFloat(),
        )
        drawLine(color, inner, outer, stroke * 1.6f, cap = StrokeCap.Round)
    }
    drawCircle(color, radius * 0.34f, center)
}

/** Ein Nadelzweig: Mittelachse mit paarweise abstehenden Nadeln. */
private fun DrawScope.pineSprig(
    color: Color,
    start: Offset,
    length: Float,
    degrees: Float,
    stroke: Float,
) {
    rotate(degrees, start) {
        val end = start + Offset(0f, length)
        drawLine(color, start, end, stroke, cap = StrokeCap.Round)

        val needles = 5
        val needleLength = length * 0.30f
        repeat(needles) { index ->
            val t = (index + 1f) / (needles + 1f)
            val at = start + Offset(0f, length * t)
            // Nach unten geneigt, wie bei einem hängenden Zweig.
            drawLine(
                color,
                at,
                at + Offset(-needleLength * 0.85f, needleLength * 0.5f),
                stroke * 0.8f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color,
                at,
                at + Offset(needleLength * 0.85f, needleLength * 0.5f),
                stroke * 0.8f,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Eine gerundete Kristallzelle als Umriss. */
private fun DrawScope.drawRoundedCell(
    color: Color,
    rect: Rect,
    corner: Float,
    stroke: Float,
) {
    translate(rect.left, rect.top) {
        drawRoundRect(
            color = color,
            size = Size(rect.width, rect.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
            style = Stroke(width = stroke),
        )
    }
}
