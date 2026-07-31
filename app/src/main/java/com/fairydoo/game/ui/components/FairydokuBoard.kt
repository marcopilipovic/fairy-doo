package com.fairydoo.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.game.model.Puzzle
import com.fairydoo.game.ui.theme.ErrorRed
import com.fairydoo.game.ui.theme.RegionColors

/**
 * Das Spielbrett: Zonen-Gitter mit gesetzten Feen.
 *
 * Zeichnet ausschließlich aus dem übergebenen [GameState] — keine eigene Logik,
 * kein eigener Zustand. Dadurch bleibt die Engine die einzige Wahrheit, und die
 * Darstellung ist ohne laufendes Spiel in einer @Preview prüfbar.
 *
 * Die Zonengrenzen bekommen dicke Linien, die Zellgrenzen innerhalb einer Zone
 * dünne: Ohne diesen Unterschied ist die Zonen-Regel auf dem Brett nicht
 * ablesbar, und genau um sie dreht sich das Rätsel.
 */
@Composable
fun FairydokuBoard(
    state: GameState,
    onTapCell: (Pos) -> Unit,
    modifier: Modifier = Modifier,
) {
    val puzzle = state.puzzle ?: return
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fairyColor = MaterialTheme.colorScheme.primary
    val revealedColor = MaterialTheme.colorScheme.tertiary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(puzzle, state.isActive) {
                if (!state.isActive) return@pointerInput
                detectTapGestures { offset ->
                    val cell = size.width.toFloat() / puzzle.size
                    val col = (offset.x / cell).toInt()
                    val row = (offset.y / cell).toInt()
                    val pos = Pos(row, col)
                    if (puzzle.contains(pos)) onTapCell(pos)
                }
            },
    ) {
        val cellSize = size.width / puzzle.size
        val inset = 2.dp.toPx()

        for (pos in puzzle.allPositions) {
            val topLeft = Offset(pos.col * cellSize, pos.row * cellSize)
            val regionColor = RegionColors[puzzle.regionAt(pos) % RegionColors.size]

            drawRoundRect(
                color = regionColor.copy(alpha = 0.22f),
                topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
                size = Size(cellSize - inset * 2, cellSize - inset * 2),
                cornerRadius = CornerRadius(6.dp.toPx()),
            )

            when (state.markAt(pos)) {
                CellMark.Fairy -> drawFairy(
                    center = Offset(topLeft.x + cellSize / 2, topLeft.y + cellSize / 2),
                    radius = cellSize * 0.30f,
                    color = when {
                        pos in state.conflicts -> ErrorRed
                        pos in state.revealed -> revealedColor
                        else -> fairyColor
                    },
                )

                CellMark.Warded -> drawWard(
                    center = Offset(topLeft.x + cellSize / 2, topLeft.y + cellSize / 2),
                    radius = cellSize * 0.16f,
                    color = gridColor.copy(alpha = 0.55f),
                    strokeWidth = 2.dp.toPx(),
                )

                CellMark.Empty -> Unit
            }

            if (pos in state.conflicts) {
                drawRoundRect(
                    color = ErrorRed,
                    topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
                    size = Size(cellSize - inset * 2, cellSize - inset * 2),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        drawRegionBorders(
            puzzle = puzzle,
            cellSize = cellSize,
            color = gridColor,
            strokeWidth = 3.dp.toPx(),
        )
    }
}

/** Zieht dicke Linien zwischen benachbarten Feldern verschiedener Zonen. */
private fun DrawScope.drawRegionBorders(
    puzzle: Puzzle,
    cellSize: Float,
    color: Color,
    strokeWidth: Float,
) {
    val size = puzzle.size

    for (row in 0 until size) {
        for (col in 0 until size) {
            val pos = Pos(row, col)
            val region = puzzle.regionAt(pos)
            val x = col * cellSize
            val y = row * cellSize

            val topDiffers = row == 0 || puzzle.regionAt(Pos(row - 1, col)) != region
            val leftDiffers = col == 0 || puzzle.regionAt(Pos(row, col - 1)) != region

            if (topDiffers) {
                drawLine(
                    color = color,
                    start = Offset(x, y),
                    end = Offset(x + cellSize, y),
                    strokeWidth = strokeWidth,
                )
            }
            if (leftDiffers) {
                drawLine(
                    color = color,
                    start = Offset(x, y),
                    end = Offset(x, y + cellSize),
                    strokeWidth = strokeWidth,
                )
            }
        }
    }

    // Rechter und unterer Abschluss des Bretts.
    val edge = size * cellSize
    drawLine(color, Offset(edge, 0f), Offset(edge, edge), strokeWidth)
    drawLine(color, Offset(0f, edge), Offset(edge, edge), strokeWidth)
}

/**
 * Platzhalter-Fee: ein vierzackiger Funkel.
 *
 * Wird durch die gezeichneten Feen aus dem finalen Design ersetzt — die Form
 * steckt bewusst nur hier, nicht in der Spiellogik.
 */
private fun DrawScope.drawFairy(center: Offset, radius: Float, color: Color) {
    val waist = radius * 0.18f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x + waist, center.y - waist, center.x + radius, center.y)
        quadraticTo(center.x + waist, center.y + waist, center.x, center.y + radius)
        quadraticTo(center.x - waist, center.y + waist, center.x - radius, center.y)
        quadraticTo(center.x - waist, center.y - waist, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}

/** Merkzeichen des Spielers: „hier sitzt sicher keine Fee“. */
private fun DrawScope.drawWard(
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float,
) {
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y - radius),
        end = Offset(center.x + radius, center.y + radius),
        strokeWidth = strokeWidth,
    )
    drawLine(
        color = color,
        start = Offset(center.x + radius, center.y - radius),
        end = Offset(center.x - radius, center.y + radius),
        strokeWidth = strokeWidth,
    )
}
