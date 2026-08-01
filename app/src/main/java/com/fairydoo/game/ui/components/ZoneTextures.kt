package com.fairydoo.game.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.theme.ZoneTexture
import kotlin.math.cos
import kotlin.math.sin

/**
 * Zeichnet das Motiv einer Zone über ihre Fläche.
 *
 * Alle Maße hängen an der Feldgröße statt an festen Punktwerten: Dasselbe Motiv
 * muss auf dem 4×4-Brett mit großen Feldern und auf dem 8×8 mit kleinen gleich
 * aussehen.
 *
 * Die Motive sind **dicht** gestreut, nicht als zwei, drei große Zeichen. Ein
 * einzelnes großes Blatt pro Feld liest sich als Symbol, das auf der Fläche
 * klebt; viele kleine lesen sich als Beschaffenheit der Fläche selbst — und
 * genau das sollen sie sein.
 *
 * [pos] geht in die Streuung ein, damit benachbarte Felder derselben Zone nicht
 * identisch aussehen. Ohne das zerfiele die Zone optisch in ihre Felder — man
 * sähe eine gekachelte Tapete statt eines zusammenhängenden Gebiets. Die
 * Streuung ist berechnet und nicht zufällig, damit ein Feld bei jedem
 * Neuzeichnen dasselbe Bild zeigt.
 */
fun DrawScope.drawZoneTexture(texture: ZoneTexture, color: Color, pos: Pos) {
    val unit = size.minDimension
    val stroke = unit * 0.032f
    val seed = pos.row * 31 + pos.col * 17

    /** Streuwert Nummer [index] für dieses Feld, zwischen 0 und 1. */
    fun vary(index: Int): Float = (((seed + index * 43) * 2654435761u.toLong()) shr 8 and 0xFFL) / 255f

    when (texture) {
        ZoneTexture.StarsAndFerns -> {
            repeat(3) { i ->
                sparkle(
                    color = color,
                    center = Offset(size.width * vary(i), size.height * vary(i + 7)),
                    radius = unit * (0.07f + vary(i + 3) * 0.05f),
                )
            }
            repeat(2) { i ->
                fern(
                    color = color,
                    start = Offset(size.width * vary(i + 11), size.height * vary(i + 13)),
                    length = unit * 0.26f,
                    degrees = vary(i + 17) * 360f,
                    stroke = stroke * 0.7f,
                )
            }
            repeat(5) { i ->
                drawCircle(
                    color = color,
                    radius = unit * 0.012f,
                    center = Offset(size.width * vary(i + 23), size.height * vary(i + 29)),
                )
            }
        }

        ZoneTexture.Sunflowers -> {
            sunflower(color, Offset(size.width * (0.20f + vary(1) * 0.2f), size.height * 0.26f), unit * 0.15f, stroke)
            sunflower(color, Offset(size.width * (0.66f + vary(2) * 0.2f), size.height * 0.62f), unit * 0.19f, stroke)
            sunflower(color, Offset(size.width * 0.30f, size.height * (0.78f + vary(3) * 0.12f)), unit * 0.11f, stroke)
            // Kleine Blütenblätter als Streu dazwischen.
            repeat(4) { i ->
                petal(
                    color = color,
                    center = Offset(size.width * vary(i + 31), size.height * vary(i + 37)),
                    length = unit * 0.09f,
                    degrees = vary(i + 41) * 360f,
                )
            }
        }

        ZoneTexture.PineNeedles -> {
            // Senkrecht und schräg ineinander, wie ein Dickicht.
            pineSprig(color, Offset(size.width * 0.22f, -unit * 0.05f), unit * 0.55f, 8f + vary(1) * 14f, stroke)
            pineSprig(color, Offset(size.width * 0.62f, size.height * 0.16f), unit * 0.48f, 34f + vary(2) * 16f, stroke)
            pineSprig(color, Offset(size.width * 0.90f, size.height * 0.52f), unit * 0.42f, 200f + vary(3) * 20f, stroke)
            pineSprig(color, Offset(size.width * 0.36f, size.height * 0.98f), unit * 0.40f, 168f + vary(4) * 18f, stroke)
        }

        ZoneTexture.ThornVines -> {
            clipRect {
                repeat(3) { index ->
                    thornVine(
                        color = color,
                        y = size.height * (0.18f + index * 0.32f + vary(index) * 0.06f),
                        amplitude = unit * 0.16f,
                        stroke = stroke,
                        unit = unit,
                        rising = (pos.row + index) % 2 == 0,
                    )
                }
            }
        }

        ZoneTexture.AutumnLeaves -> {
            // Überlappendes Laub: groß nach klein, damit es wie gefallen wirkt.
            leaf(color, Offset(size.width * 0.28f, size.height * 0.24f), unit * 0.44f, vary(1) * 360f, broad = true)
            leaf(color, Offset(size.width * 0.74f, size.height * 0.42f), unit * 0.36f, vary(2) * 360f, broad = false)
            leaf(color, Offset(size.width * 0.42f, size.height * 0.74f), unit * 0.40f, vary(3) * 360f, broad = true)
            leaf(color, Offset(size.width * 0.92f, size.height * 0.86f), unit * 0.28f, vary(4) * 360f, broad = false)
            leaf(color, Offset(size.width * 0.06f, size.height * 0.64f), unit * 0.26f, vary(5) * 360f, broad = true)
        }

        ZoneTexture.FigsAndHatching -> {
            // Erst die Schraffur, dann die Früchte darüber.
            clipRect {
                var offset = -size.height
                while (offset < size.width + size.height) {
                    drawLine(
                        color.copy(alpha = color.alpha * 0.45f),
                        Offset(offset, size.height),
                        Offset(offset + size.height, 0f),
                        stroke * 0.6f,
                    )
                    offset += unit * 0.28f
                }
            }
            fig(color, Offset(size.width * (0.26f + vary(1) * 0.12f), size.height * 0.30f), unit * 0.13f, stroke)
            fig(color, Offset(size.width * 0.70f, size.height * (0.62f + vary(2) * 0.12f)), unit * 0.11f, stroke)
            fig(color, Offset(size.width * 0.46f, size.height * 0.88f), unit * 0.09f, stroke)
        }

        ZoneTexture.CrystalVeins -> {
            // Adern, die über den Feldrand hinauslaufen, und ein Facettengitter
            // dazwischen — Marmor, kein Fliesenraster.
            clipRect {
                repeat(3) { index ->
                    val path = Path()
                    val startY = size.height * (0.18f + index * 0.32f + vary(index) * 0.08f)
                    path.moveTo(-unit * 0.1f, startY)
                    path.cubicTo(
                        size.width * 0.3f, startY - unit * 0.18f,
                        size.width * 0.6f, startY + unit * 0.20f,
                        size.width + unit * 0.1f, startY - unit * 0.06f,
                    )
                    drawPath(path, color, style = Stroke(width = stroke * (1.2f - index * 0.25f)))
                }
                // Feine Facetten als kurze, schräge Striche.
                repeat(6) { i ->
                    val at = Offset(size.width * vary(i + 3), size.height * vary(i + 9))
                    val reach = unit * 0.12f
                    drawLine(
                        color.copy(alpha = color.alpha * 0.7f),
                        at,
                        at + Offset(reach * 0.7f, -reach * 0.7f),
                        stroke * 0.5f,
                    )
                }
            }
        }

        ZoneTexture.Waves -> {
            // Durchlaufende Wellen: Höhe und Phase hängen nur an der Zeile,
            // deshalb setzen sie sich im Nachbarfeld nahtlos fort.
            val lines = 5
            repeat(lines) { index ->
                val y = size.height * (index + 0.5f) / lines
                val path = Path().apply {
                    moveTo(0f, y)
                    val step = size.width / 3f
                    var x = 0f
                    var up = (index + pos.row) % 2 == 0
                    while (x < size.width) {
                        val peak = if (up) -unit * 0.055f else unit * 0.055f
                        quadraticBezierTo(x + step / 2f, y + peak, x + step, y)
                        x += step
                        up = !up
                    }
                }
                drawPath(path, color, style = Stroke(width = stroke * 0.85f, cap = StrokeCap.Round))
            }
        }

        ZoneTexture.Constellations -> {
            val stars = List(6) { i ->
                Offset(size.width * vary(i), size.height * vary(i + 6))
            }
            // Punkt-zu-Linie-Netz: jeder Stern mit dem nächsten verbunden.
            for (index in 0 until stars.size - 1) {
                drawLine(
                    color.copy(alpha = color.alpha * 0.5f),
                    stars[index],
                    stars[index + 1],
                    stroke * 0.45f,
                )
            }
            stars.forEachIndexed { index, star ->
                drawCircle(color, unit * (0.016f + (index % 3) * 0.010f), star)
            }
            // Ab und zu eine Sternschnuppe.
            if ((pos.row + pos.col) % 3 == 0) {
                val head = Offset(size.width * 0.82f, size.height * 0.16f)
                drawLine(
                    color.copy(alpha = color.alpha * 0.6f),
                    head,
                    head + Offset(-unit * 0.26f, unit * 0.18f),
                    stroke * 0.6f,
                    cap = StrokeCap.Round,
                )
                drawCircle(color, unit * 0.026f, head)
            }
        }

        ZoneTexture.CrackedEarth -> {
            // Risse aus einer Handvoll Keimpunkte — jeder Riss läuft zum
            // Nachbarn, wie bei aufgesprungenem Lehm.
            clipRect {
                val seeds = List(5) { i ->
                    Offset(size.width * vary(i), size.height * vary(i + 5))
                }
                for (first in seeds.indices) {
                    for (second in first + 1 until seeds.size) {
                        // Nur nahe Keimpunkte verbinden, sonst wird es ein Netz
                        // aus lauter Diagonalen statt einer Zellstruktur.
                        val gap = (seeds[first] - seeds[second]).getDistance()
                        if (gap > unit * 0.62f) continue
                        drawLine(color, seeds[first], seeds[second], stroke * 0.8f, cap = StrokeCap.Round)
                    }
                }
                // Ränder anbinden, damit die Zellen über das Feld hinausgehen.
                seeds.take(3).forEach { seed ->
                    drawLine(color, seed, Offset(seed.x, if (seed.y < size.height / 2) 0f else size.height), stroke * 0.6f)
                }
            }
        }
    }
}

/**
 * Ein vierzackiger Funke.
 *
 * Die Flanken sind zur Mitte hin eingezogen — daher die Kontrollpunkte nahe am
 * Zentrum. Genau das macht den Unterschied zwischen einem Funkeln und einem
 * Kreuz: Die Zacken laufen spitz zu, statt gleich breit zu bleiben. Ein Kreuz
 * wäre hier fatal, denn genau das setzt der Spieler selbst aufs Feld.
 */
private fun DrawScope.sparkle(color: Color, center: Offset, radius: Float) {
    val waist = radius * 0.20f
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

/** Ein Farnwedel: Mittelrippe mit paarweise abstehenden Fiedern. */
private fun DrawScope.fern(
    color: Color,
    start: Offset,
    length: Float,
    degrees: Float,
    stroke: Float,
) {
    rotate(degrees, start) {
        drawLine(color, start, start + Offset(0f, length), stroke, cap = StrokeCap.Round)
        val leaflets = 6
        repeat(leaflets) { index ->
            val t = (index + 1f) / (leaflets + 1f)
            val at = start + Offset(0f, length * t)
            // Nach oben hin kürzer, damit der Wedel spitz zuläuft.
            val reach = length * 0.26f * (1f - t * 0.6f)
            drawLine(color, at, at + Offset(-reach, -reach * 0.5f), stroke * 0.7f, cap = StrokeCap.Round)
            drawLine(color, at, at + Offset(reach, -reach * 0.5f), stroke * 0.7f, cap = StrokeCap.Round)
        }
    }
}

/** Eine Sonnenblume: Kern mit Strahlenkranz. */
private fun DrawScope.sunflower(color: Color, center: Offset, radius: Float, stroke: Float) {
    val petals = 12
    repeat(petals) { index ->
        val angle = Math.toRadians(index * 360.0 / petals)
        val inner = Offset(
            center.x + (cos(angle) * radius * 0.40f).toFloat(),
            center.y + (sin(angle) * radius * 0.40f).toFloat(),
        )
        val outer = Offset(
            center.x + (cos(angle) * radius).toFloat(),
            center.y + (sin(angle) * radius).toFloat(),
        )
        drawLine(color, inner, outer, stroke * 1.5f, cap = StrokeCap.Round)
    }
    drawCircle(color, radius * 0.32f, center)
}

/** Ein einzelnes Blütenblatt als Streu. */
private fun DrawScope.petal(color: Color, center: Offset, length: Float, degrees: Float) {
    rotate(degrees, center) {
        val path = Path().apply {
            moveTo(center.x, center.y - length)
            quadraticBezierTo(center.x + length * 0.5f, center.y, center.x, center.y + length)
            quadraticBezierTo(center.x - length * 0.5f, center.y, center.x, center.y - length)
            close()
        }
        drawPath(path, color)
    }
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
        drawLine(color, start, start + Offset(0f, length), stroke, cap = StrokeCap.Round)
        val needles = 7
        val needleLength = length * 0.26f
        repeat(needles) { index ->
            val t = (index + 1f) / (needles + 1f)
            val at = start + Offset(0f, length * t)
            drawLine(color, at, at + Offset(-needleLength * 0.9f, needleLength * 0.45f), stroke * 0.7f, cap = StrokeCap.Round)
            drawLine(color, at, at + Offset(needleLength * 0.9f, needleLength * 0.45f), stroke * 0.7f, cap = StrokeCap.Round)
        }
    }
}

/**
 * Eine Ranke, die sich quer durchs Feld windet, mit Dornen auf beiden Seiten.
 *
 * Sie beginnt links außerhalb und endet rechts außerhalb, damit sie im
 * Nachbarfeld weiterläuft, statt am Rand abzubrechen.
 */
private fun DrawScope.thornVine(
    color: Color,
    y: Float,
    amplitude: Float,
    stroke: Float,
    unit: Float,
    rising: Boolean,
) {
    val direction = if (rising) 1f else -1f
    val path = Path().apply {
        moveTo(-unit * 0.1f, y)
        val step = size.width / 2f
        var x = -unit * 0.1f
        var up = true
        while (x < size.width + unit * 0.2f) {
            val peak = if (up) -amplitude * direction else amplitude * direction
            quadraticBezierTo(x + step / 2f, y + peak, x + step, y)
            x += step
            up = !up
        }
    }
    drawPath(path, color, style = Stroke(width = stroke, cap = StrokeCap.Round))

    // Dornen sitzen abwechselnd oben und unten auf der Ranke.
    val thorn = unit * 0.13f
    var x = unit * 0.12f
    var index = 0
    while (x < size.width) {
        val sign = if (index % 2 == 0) -1f else 1f
        val base = Offset(x, y + sign * amplitude * 0.35f)
        drawLine(color, base, base + Offset(thorn * 0.35f, sign * thorn), stroke * 0.8f, cap = StrokeCap.Round)
        x += unit * 0.20f
        index++
    }
}

/**
 * Ein Blatt: längliche Form mit Mittelrippe und Stiel.
 *
 * Der erste Versuch war ein gelapptes Ahornblatt mit drei Ausbuchtungen je
 * Seite. Auf einem Feld von vierzig Bildpunkten wurde daraus ein zackiger
 * Klecks, der wie ein Stern aussah — ausgerechnet das Motiv der Nachbarzone.
 * Ein Blatt erkennt man an Silhouette, Rippe und Stiel, nicht an Lappen, die
 * ohnehin niemand zählen kann.
 *
 * [broad] macht daraus ein breites Blatt statt eines schmalen — zwei Formen
 * genügen, damit das Laub nicht gestempelt wirkt.
 */
private fun DrawScope.leaf(
    color: Color,
    center: Offset,
    length: Float,
    degrees: Float,
    broad: Boolean,
) {
    rotate(degrees, center) {
        val half = length / 2f
        val width = length * (if (broad) 0.34f else 0.24f)
        val tip = Offset(center.x, center.y - half)
        val stemEnd = Offset(center.x, center.y + half)

        val path = Path().apply {
            moveTo(tip.x, tip.y)
            quadraticBezierTo(center.x + width, center.y - half * 0.1f, stemEnd.x, stemEnd.y)
            quadraticBezierTo(center.x - width, center.y - half * 0.1f, tip.x, tip.y)
            close()
        }
        drawPath(path, color)

        // Stiel und Mittelrippe: Sie machen aus der Form ein Blatt.
        drawLine(
            color,
            stemEnd,
            Offset(center.x, center.y + half * 1.32f),
            length * 0.045f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color.copy(alpha = color.alpha * 0.45f),
            Offset(center.x, center.y - half * 0.75f),
            stemEnd,
            length * 0.035f,
        )
    }
}

/** Eine Feige an kurzem Stiel — unten rund, oben zum Stiel verjüngt. */
private fun DrawScope.fig(color: Color, center: Offset, radius: Float, stroke: Float) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius * 0.9f)
        quadraticBezierTo(center.x + radius, center.y - radius * 0.2f, center.x + radius * 0.75f, center.y + radius * 0.6f)
        quadraticBezierTo(center.x, center.y + radius * 1.15f, center.x - radius * 0.75f, center.y + radius * 0.6f)
        quadraticBezierTo(center.x - radius, center.y - radius * 0.2f, center.x, center.y - radius * 0.9f)
        close()
    }
    drawPath(path, color)
    drawLine(
        color,
        Offset(center.x, center.y - radius * 0.9f),
        Offset(center.x + radius * 0.35f, center.y - radius * 1.5f),
        stroke * 0.8f,
        cap = StrokeCap.Round,
    )
}
