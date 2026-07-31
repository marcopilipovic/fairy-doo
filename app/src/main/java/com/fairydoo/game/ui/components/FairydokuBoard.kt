package com.fairydoo.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.theme.ConflictRed
import com.fairydoo.game.ui.theme.FaintBorder
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.MossDarkA
import com.fairydoo.game.ui.theme.MossDarkB
import com.fairydoo.game.ui.theme.MossLightA
import com.fairydoo.game.ui.theme.MossLightB
import com.fairydoo.game.ui.theme.RegionColors

/** Der Schein, den eine Feen-Art um sich verbreitet. */
fun FairySpecies.glowColor(): Color = when (this) {
    FairySpecies.Blossom -> Color(0xE6FF9ECF)
    FairySpecies.Water -> Color(0xE65BC8FF)
    FairySpecies.Fire -> Color(0xE6FF9A5B)
    FairySpecies.Star -> Color(0xE6FFE66B)
}

/**
 * Das Spielbrett: moosige Steinfelder, von leuchtenden Zonengrenzen durchzogen.
 *
 * Zeichnet ausschließlich aus dem übergebenen [GameState] — keine eigene Logik,
 * kein eigener Zustand. Dadurch bleibt die Engine die einzige Wahrheit.
 *
 * Der Unterschied zwischen Zonengrenze (kräftige Zonenfarbe) und Zellgrenze
 * (blasses Weiß) trägt die ganze Lesbarkeit des Rätsels: Ohne ihn ist die
 * Zonen-Regel auf dem Brett nicht ablesbar, und genau um sie dreht es sich.
 */
@Composable
fun FairydokuBoard(
    state: GameState,
    cellSize: Dp,
    onTapCell: (Pos) -> Unit,
    modifier: Modifier = Modifier,
) {
    val puzzle = state.puzzle ?: return

    Box(
        modifier = modifier
            // Der bläuliche Schein, der das Brett in der Vorlage vom
            // Hintergrund abhebt (`0 0 40px rgba(120,140,255,.12)`).
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1F788CFF), Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.maxDimension * 0.75f,
                    ),
                )
            }
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x8C0A0E1E))
            .padding(4.dp),
    ) {
        Column {
            for (row in 0 until puzzle.size) {
                Row {
                    for (col in 0 until puzzle.size) {
                        val pos = Pos(row, col)
                        BoardCell(
                            state = state,
                            pos = pos,
                            cellSize = cellSize,
                            onTap = { onTapCell(pos) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    state: GameState,
    pos: Pos,
    cellSize: Dp,
    onTap: () -> Unit,
) {
    val puzzle = state.puzzle ?: return
    val region = puzzle.regionAt(pos)
    val regionColor = RegionColors[region % RegionColors.size]
    val mark = state.markAt(pos)
    val isConflicting = pos in state.conflicts

    // Schachbrettvariation der Moosfelder, damit das Brett nicht flach wirkt.
    val evenCell = (pos.row + pos.col) % 2 == 0

    // Eine Kante gehört zur Zonengrenze, wenn dahinter eine andere Zone liegt.
    fun borderColor(other: Pos): Color =
        if (!puzzle.contains(other) || puzzle.regionAt(other) != region) regionColor else FaintBorder

    val top = borderColor(Pos(pos.row - 1, pos.col))
    val bottom = borderColor(Pos(pos.row + 1, pos.col))
    val left = borderColor(Pos(pos.row, pos.col - 1))
    val right = borderColor(Pos(pos.row, pos.col + 1))

    Box(
        modifier = Modifier
            .size(cellSize)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = if (evenCell) {
                            listOf(MossLightA, MossDarkA)
                        } else {
                            listOf(MossLightB, MossDarkB)
                        },
                        center = Offset(
                            size.width * if (evenCell) 0.35f else 0.60f,
                            size.height * if (evenCell) 0.30f else 0.65f,
                        ),
                        radius = size.maxDimension * 0.7f,
                    ),
                )

                val stroke = 2.5.dp.toPx()
                drawRect(top, size = Size(size.width, stroke))
                drawRect(
                    bottom,
                    topLeft = Offset(0f, size.height - stroke),
                    size = Size(size.width, stroke),
                )
                drawRect(left, size = Size(stroke, size.height))
                drawRect(
                    right,
                    topLeft = Offset(size.width - stroke, 0f),
                    size = Size(stroke, size.height),
                )

                if (isConflicting) {
                    // Der innere rote Schein der Vorlage: von der Kante nach
                    // innen glühend, die Mitte bleibt frei — sonst wäre die Fee
                    // darunter kaum noch zu erkennen.
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                1.0f to ConflictRed,
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.maxDimension * 0.62f,
                        ),
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (mark) {
            CellMark.Fairy -> FairyGlyph(
                species = state.species,
                cellSize = cellSize,
                pulsing = state.hintCell == pos,
            )

            CellMark.Warded -> Text(
                text = "✕",
                style = TextStyle(
                    fontSize = (cellSize.value * 0.34f).sp,
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center,
                ),
            )

            CellMark.Empty -> Unit
        }
    }
}

/**
 * Die Fee auf dem Feld.
 *
 * Platzhalter wie im Prototyp: das Emoji, umgeben vom Schein ihrer Art. Sobald
 * gezeichnete Feen-Sprites vorliegen, wird nur diese Funktion ersetzt.
 */
@Composable
private fun FairyGlyph(
    species: FairySpecies,
    cellSize: Dp,
    pulsing: Boolean,
) {
    // popIn: von 0.3 über 1.15 auf 1 — die Fee „landet" auf dem Feld.
    val scale = remember { Animatable(0.3f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 350
                0.3f at 0
                1.15f at 245
                1f at 350
            },
        )
    }

    // Ein per Feenstaub aufgedecktes Feld leuchtet zwei Sekunden lang nach.
    val pulseTransition = rememberInfiniteTransition(label = "hintPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hintPulseValue",
    )

    val glow = species.glowColor()
    val glowRadius = if (pulsing) 0.55f + pulse * 0.25f else 0.5f
    val glowColor = if (pulsing) Gold else glow

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(cellSize)
                .graphicsLayer { alpha = scale.value.coerceIn(0f, 1f) }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = if (pulsing) 0.55f else 0.38f),
                                Color.Transparent,
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.minDimension * glowRadius,
                        ),
                        radius = size.minDimension * glowRadius,
                    )
                },
        )
        Text(
            text = "🧚",
            style = TextStyle(fontSize = (cellSize.value * 0.58f).sp, textAlign = TextAlign.Center),
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        )
    }
}
