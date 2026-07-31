package com.fairydoo.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.fairydoo.game.game.GameState

/**
 * Das Spielfeld.
 *
 * Aktuell ein Platzhalter-Raster — es zeigt aber schon die Zeichenschicht, in
 * der die echte Mechanik später lebt: ein [Canvas], das ausschließlich aus dem
 * übergebenen [GameState] rendert. Keine eigene Logik, kein eigener Zustand;
 * dadurch bleibt die Darstellung testbar und der Loop die einzige Wahrheit.
 */
@Composable
fun GameBoard(
    state: GameState,
    modifier: Modifier = Modifier,
    columns: Int = 5,
    rows: Int = 7,
) {
    val tileColor = MaterialTheme.colorScheme.surfaceVariant
    val accentColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier) {
        val padding = 16.dp.toPx()
        val gap = 8.dp.toPx()
        val progressHeight = 6.dp.toPx()

        val boardWidth = size.width - padding * 2
        val boardHeight = size.height - padding * 2 - progressHeight - gap

        val tileWidth = (boardWidth - gap * (columns - 1)) / columns
        val tileHeight = (boardHeight - gap * (rows - 1)) / rows
        if (tileWidth <= 0f || tileHeight <= 0f) return@Canvas

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                // Platzhalter-Muster: ein Schachbrett-Akzent, damit man beim
                // Bauen sieht, ob das Layout stimmt.
                val highlighted = (row + column) % 3 == 0

                drawRoundRect(
                    color = if (highlighted) accentColor.copy(alpha = 0.25f) else tileColor,
                    topLeft = Offset(
                        x = padding + column * (tileWidth + gap),
                        y = padding + row * (tileHeight + gap),
                    ),
                    size = Size(tileWidth, tileHeight),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                )
            }
        }

        drawTimeBar(
            fraction = state.remainingMillis.toFloat() /
                GameState.ROUND_DURATION_MILLIS.toFloat(),
            padding = padding,
            height = progressHeight,
            trackColor = trackColor,
            fillColor = accentColor,
        )
    }
}

private fun DrawScope.drawTimeBar(
    fraction: Float,
    padding: Float,
    height: Float,
    trackColor: androidx.compose.ui.graphics.Color,
    fillColor: androidx.compose.ui.graphics.Color,
) {
    val width = size.width - padding * 2
    val top = size.height - padding - height
    val radius = CornerRadius(height / 2f)

    drawRoundRect(
        color = trackColor.copy(alpha = 0.2f),
        topLeft = Offset(padding, top),
        size = Size(width, height),
        cornerRadius = radius,
    )

    val filled = width * fraction.coerceIn(0f, 1f)
    if (filled > 0f) {
        drawRoundRect(
            color = fillColor,
            topLeft = Offset(padding, top),
            size = Size(filled, height),
            cornerRadius = radius,
        )
    }
}
