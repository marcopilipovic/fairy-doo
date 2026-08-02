package com.fairydoo.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.sprites.FairySpriteCache
import com.fairydoo.game.ui.theme.CellSeam
import com.fairydoo.game.ui.theme.ConflictRed
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldLight
import androidx.compose.foundation.border
import com.fairydoo.game.ui.theme.MossMatBorder
import com.fairydoo.game.ui.theme.MossMatBottom
import com.fairydoo.game.ui.theme.MossMatMiddle
import com.fairydoo.game.ui.theme.MossMatTop
import com.fairydoo.game.ui.theme.MossPatchA
import com.fairydoo.game.ui.theme.MossPatchB
import com.fairydoo.game.ui.theme.RegionColors
import com.fairydoo.game.ui.theme.StoneDark
import com.fairydoo.game.ui.theme.StoneLight

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
    onHoldCell: (Pos) -> Unit,
    modifier: Modifier = Modifier,
) {
    val puzzle = state.puzzle ?: return

    // Der Systemwert für langes Drücken liegt bei einer halben Sekunde — als
    // Schutz vor versehentlichem Auslösen sinnvoll, hier aber zäh: Das Halten
    // ist eine gewollte Spielgeste, keine versehentliche. Nur fürs Brett.
    val system = LocalViewConfiguration.current
    val boardTiming = remember(system) {
        object : ViewConfiguration by system {
            override val longPressTimeoutMillis: Long = HOLD_MILLIS
        }
    }

    CompositionLocalProvider(LocalViewConfiguration provides boardTiming) {
        // Die Moos-Matte, auf der das Gitter liegt. Sie ist nicht nur Rahmen:
        // Erst dadurch sitzt das Brett *im* Wald, statt vor ihm zu schweben.
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(22.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0f to MossMatTop,
                                0.6f to MossMatMiddle,
                                1f to MossMatBottom,
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width * 0.5f, size.height),
                        ),
                    )
                    // Helle Moosstellen, damit die Matte lebt.
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x2ED2FFA0), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.12f),
                            radius = size.minDimension * 0.6f,
                        ),
                    )
                    // Lichtkante oben, Schattensaum unten — der Rand wölbt sich.
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x2ED2FFA0), Color.Transparent),
                            startY = 0f,
                            endY = 8.dp.toPx(),
                        ),
                        size = Size(size.width, 8.dp.toPx()),
                    )
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x66000000)),
                            startY = size.height - 14.dp.toPx(),
                            endY = size.height,
                        ),
                        topLeft = Offset(0f, size.height - 14.dp.toPx()),
                        size = Size(size.width, 14.dp.toPx()),
                    )
                }
                .border(2.dp, MossMatBorder, RoundedCornerShape(22.dp))
                .padding(MAT_PADDING),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x8C0C120A))
                    .drawBehind {
                        // Die dunkle Vertiefung, in der das Gitter sitzt.
                        drawRect(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.55f to Color.Transparent,
                                    1f to Color(0xA6000000),
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.maxDimension * 0.7f,
                            ),
                        )
                    }
                    .padding(GRID_PADDING),
                // Die Feldgröße wird abgerundet, damit das Gitter aufgeht.
                // Der Rest von bis zu einem Punkt je Feld sammelt sich sonst an
                // einer Seite und lässt die Fassung schief wirken; zentriert
                // verteilt er sich auf beide.
                contentAlignment = Alignment.Center,
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
                                    onHold = { onHoldCell(pos) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Die Breite der Moos-Matte rings um das Gitter. */
private val MAT_PADDING = 12.dp

/** Die Vertiefung zwischen Matte und Gitter. */
private val GRID_PADDING = 5.dp

/**
 * Was die Fassung des Bretts an Breite verbraucht — auf beiden Seiten zusammen.
 *
 * Wer die Feldgröße bestimmt, muss das von der verfügbaren Breite abziehen.
 * Ohne diese Konstante stand die Zahl an zwei Orten, und als die Moos-Matte
 * hinzukam, wurde nur einer von beiden nachgezogen: Das Gitter rechnete mit
 * Platz, den es nicht mehr gab, und die rechte Feldspalte wurde beschnitten.
 */
val BoardFrameInsets = (MAT_PADDING + GRID_PADDING) * 2

/**
 * Wie lange der Finger liegen muss, bis die Fee erscheint.
 *
 * Kürzer als die 500 ms des Systems, weil das Halten hier eine der beiden
 * Hauptgesten ist und nicht wie sonst ein seltener Sonderweg. Viel kürzer darf
 * es nicht sein: Unter etwa einer Viertelsekunde geriete ein bloß etwas
 * behäbiger Tipp zur Fee.
 */
private const val HOLD_MILLIS = 350L

@Composable
private fun BoardCell(
    state: GameState,
    pos: Pos,
    cellSize: Dp,
    onTap: () -> Unit,
    onHold: () -> Unit,
) {
    val puzzle = state.puzzle ?: return
    val haptics = LocalHapticFeedback.current
    val region = puzzle.regionAt(pos)
    val regionColor = RegionColors[region % RegionColors.size]
    val mark = state.markAt(pos)
    val isConflicting = pos in state.conflicts

    // Schachbrettvariation der Steinplatten, damit das Brett nicht flach wirkt.
    val evenCell = (pos.row + pos.col) % 2 == 0

    // Eine Kante gehört zur Zonengrenze, wenn dahinter eine andere Zone liegt.
    fun isZoneEdge(other: Pos): Boolean =
        !puzzle.contains(other) || puzzle.regionAt(other) != region

    val topEdge = isZoneEdge(Pos(pos.row - 1, pos.col))
    val bottomEdge = isZoneEdge(Pos(pos.row + 1, pos.col))
    val leftEdge = isZoneEdge(Pos(pos.row, pos.col - 1))
    val rightEdge = isZoneEdge(Pos(pos.row, pos.col + 1))

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(zoneCorners(topEdge, bottomEdge, leftEdge, rightEdge))
            .drawBehind {
                // Steinplatte mit Moosflecken, in der Zonenfarbe angehaucht:
                // Die Zone färbt den Stein nur leicht ein — kenntlich wird sie
                // über ihre leuchtenden Ränder, nicht über die Fläche.
                drawStonePlate(regionColor, evenCell)
                drawZoneGlow(regionColor, topEdge, bottomEdge, leftEdge, rightEdge)
                drawStoneRelief()
                drawZoneBorders(
                    color = neonOf(regionColor),
                    top = topEdge,
                    bottom = bottomEdge,
                    left = leftEdge,
                    right = rightEdge,
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
            // Ohne `onDoubleTap` meldet sich der Tipp sofort beim Loslassen.
            // Genau darum hängt die Fee am Halten und nicht am Doppeltipp: Ein
            // Doppeltipp zwänge jeden einzelnen Tipp zu warten, ob noch einer
            // folgt — und das Merkzeichen ist der Zug, den man am häufigsten
            // macht.
            .pointerInput(pos) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = {
                        // Die Fee erscheint, während der Finger noch liegt; ohne
                        // ein Rütteln bliebe unklar, ob die Geste schon zählt.
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHold()
                    },
                )
            }
            // Die Gestenerkennung ersetzt `clickable` und damit auch dessen
            // Barrierefreiheit; für die Sprachausgabe bleiben beide Gesten.
            .semantics {
                role = Role.Button
                onClick {
                    onTap()
                    true
                }
                onLongClick {
                    onHold()
                    true
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        when (mark) {
            // Welche Fee erscheint, entscheidet die Zone des Feldes.
            CellMark.Fairy -> FairyGlyph(
                species = GameState.speciesForZone(state.level, region),
                zoneColor = regionColor,
                cellSize = cellSize,
                pulsing = state.hintCell == pos,
                phaseOffset = (pos.row * 3 + pos.col) * 260,
            )

            CellMark.Warded -> WardMark(cellSize = cellSize, pos = pos)

            CellMark.Empty -> Unit
        }
    }
}

/**
 * Die Steinplatte eines Feldes, in der Zonenfarbe angehaucht.
 *
 * Vier Schichten übereinander, wie in der Vorlage: ein Lichtfleck, zwei
 * Moosflecken und darunter der Stein selbst. Die Zonenfarbe färbt den Stein nur
 * zu einem Zehntel ein — kenntlich wird die Zone über ihre leuchtenden Ränder,
 * nicht über die Fläche. Auf einer voll eingefärbten Fläche stünden acht
 * Neontöne gleichzeitig im Bild und nähmen den Feen die Aufmerksamkeit.
 *
 * Die Schachbrettvariation verschiebt Lichtfleck und Moos, statt die Farbe zu
 * ändern: Sonst sähe ein 8×8-Brett aus wie gekachelte Tapete statt wie Steine.
 */
private fun DrawScope.drawStonePlate(zone: Color, evenCell: Boolean) {
    val tintA = lerp(StoneLight, zone, 0.10f)
    val tintB = lerp(StoneDark, zone, 0.08f)

    val base = if (evenCell) Offset(0.40f, 0.45f) else Offset(0.55f, 0.60f)
    val baseEnd = if (evenCell) 0.78f else 0.75f
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to tintA, baseEnd to tintB, 1f to tintB),
            center = Offset(size.width * base.x, size.height * base.y),
            radius = size.maxDimension * 0.80f,
        ),
    )

    // Zwei Moosflecken an wechselnden Stellen.
    val patchOne = if (evenCell) Offset(0.70f, 0.82f) else Offset(0.22f, 0.78f)
    val patchTwo = if (evenCell) Offset(0.78f, 0.20f) else Offset(0.15f, 0.22f)
    // Der Farbstopp bei 0,7 ist entscheidend: Läuft der Fleck erst am
    // Radiusende aus, verwäscht er die ganze Platte zu einem grünen Nebel.
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to MossPatchA, 0.7f to Color.Transparent),
            center = Offset(size.width * patchOne.x, size.height * patchOne.y),
            radius = size.minDimension * 0.38f,
        ),
    )
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(0f to MossPatchB, 0.7f to Color.Transparent),
            center = Offset(size.width * patchTwo.x, size.height * patchTwo.y),
            radius = size.minDimension * 0.28f,
        ),
    )

    // Der Lichtfleck obenauf — er lässt den Stein gewölbt wirken.
    val light = if (evenCell) Offset(0.30f, 0.25f) else Offset(0.65f, 0.28f)
    val lightStrength = if (evenCell) 0.22f else 0.16f
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0f to Color.White.copy(alpha = lightStrength),
                0.42f to Color.Transparent,
            ),
            center = Offset(size.width * light.x, size.height * light.y),
            radius = size.minDimension * 0.45f,
        ),
    )
}

/**
 * Der Neonton eines Zonenrandes: die Zonenfarbe, um ein Fünftel aufgehellt.
 *
 * Die reine Farbe wäre als Linie zu dunkel gegen den Stein; erst die Beimischung
 * von Weiß lässt den Rand leuchten statt nur farbig zu sein.
 */
private fun neonOf(zone: Color): Color = lerp(zone, Color.White, 0.20f)

/**
 * Die Eckenrundung eines Feldes.
 *
 * Wo zwei Zonengrenzen aufeinandertreffen, ist die Ecke rund — dadurch erscheint
 * eine Zone als zusammenhängender, abgerundeter Block statt als Ansammlung
 * quadratischer Felder. Alle übrigen Ecken bleiben fast spitz, damit die Felder
 * innerhalb einer Zone bündig aneinanderstoßen.
 */
private fun zoneCorners(top: Boolean, bottom: Boolean, left: Boolean, right: Boolean) =
    RoundedCornerShape(
        topStart = if (top && left) ZONE_CORNER else CELL_CORNER,
        topEnd = if (top && right) ZONE_CORNER else CELL_CORNER,
        bottomEnd = if (bottom && right) ZONE_CORNER else CELL_CORNER,
        bottomStart = if (bottom && left) ZONE_CORNER else CELL_CORNER,
    )

private val ZONE_CORNER = 14.dp
private val CELL_CORNER = 2.dp

/**
 * Der Schein, den eine Zonengrenze ins Feld wirft.
 *
 * Erst dadurch wirken die Zonen wie leuchtende Bänder statt wie aufgemalte
 * Striche — und man erkennt beim Überfliegen, welche Felder zusammengehören.
 */
private fun DrawScope.drawZoneGlow(
    color: Color,
    top: Boolean,
    bottom: Boolean,
    left: Boolean,
    right: Boolean,
) {
    val depth = size.minDimension * 0.32f
    val glow = color.copy(alpha = 0.42f)

    if (top) {
        drawRect(
            brush = Brush.verticalGradient(listOf(glow, Color.Transparent), 0f, depth),
            size = Size(size.width, depth),
        )
    }
    if (bottom) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, glow),
                size.height - depth,
                size.height,
            ),
            topLeft = Offset(0f, size.height - depth),
            size = Size(size.width, depth),
        )
    }
    if (left) {
        drawRect(
            brush = Brush.horizontalGradient(listOf(glow, Color.Transparent), 0f, depth),
            size = Size(depth, size.height),
        )
    }
    if (right) {
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, glow),
                size.width - depth,
                size.width,
            ),
            topLeft = Offset(size.width - depth, 0f),
            size = Size(depth, size.height),
        )
    }
}

/**
 * Das Stein-Relief: oben eine Lichtkante, unten ein Schattensaum. Das lässt die
 * Felder aus der Fläche heraustreten.
 */
private fun DrawScope.drawStoneRelief() {
    val topHeight = size.height * 0.20f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x24FFFFFF), Color.Transparent),
            startY = 0f,
            endY = topHeight,
        ),
        size = Size(size.width, topHeight),
    )

    val bottomHeight = size.height * 0.32f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x66000000)),
            startY = size.height - bottomHeight,
            endY = size.height,
        ),
        topLeft = Offset(0f, size.height - bottomHeight),
        size = Size(size.width, bottomHeight),
    )
}

/**
 * Die Kantenlinien: leuchtend an Zonengrenzen, dunkel zwischen Feldern derselben
 * Zone.
 *
 * Der Unterschied trägt die ganze Lesbarkeit des Rätsels: Ohne ihn ist die
 * Zonen-Regel auf dem Brett nicht ablesbar, und genau um sie dreht es sich.
 */
private fun DrawScope.drawZoneBorders(
    color: Color,
    top: Boolean,
    bottom: Boolean,
    left: Boolean,
    right: Boolean,
) {
    val stroke = 3.5.dp.toPx()

    fun edge(isZoneEdge: Boolean, atStart: Boolean, horizontal: Boolean) {
        val paint = if (isZoneEdge) color else CellSeam
        val topLeft = when {
            horizontal && atStart -> Offset.Zero
            horizontal -> Offset(0f, size.height - stroke)
            atStart -> Offset.Zero
            else -> Offset(size.width - stroke, 0f)
        }
        val edgeSize = if (horizontal) Size(size.width, stroke) else Size(stroke, size.height)
        drawRect(paint, topLeft = topLeft, size = edgeSize)
    }

    edge(top, atStart = true, horizontal = true)
    edge(bottom, atStart = false, horizontal = true)
    edge(left, atStart = true, horizontal = false)
    edge(right, atStart = false, horizontal = false)
}

/**
 * Die Fee auf dem Feld.
 *
 * Gezeichnete Illustration statt Emoji, mit Schattenwurf, zweistufigem Schein
 * und ruhigem Schweben — dadurch sitzt sie *auf* dem Stein, statt darauf zu
 * kleben.
 */
@Composable
private fun FairyGlyph(
    species: FairySpecies,
    zoneColor: Color,
    cellSize: Dp,
    pulsing: Boolean,
    phaseOffset: Int,
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

    val transition = rememberInfiniteTransition(label = "fairy")

    // Bewusst ohne `by`-Delegat: Die Werte werden erst in den Layer- und
    // Zeichen-Lambdas gelesen. Läse man sie hier im Rumpf, würde jede Fee
    // sechzigmal pro Sekunde neu komponiert — bei acht Feen auf dem Brett
    // rechnet dann das halbe Spielfeld ständig neu.
    val hover = transition.animateFloat(
        initialValue = 0f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(phaseOffset),
        ),
        label = "hover",
    )

    // Ein per Feenstaub aufgedecktes Feld leuchtet zwei Sekunden lang nach.
    val pulse = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val context = LocalContext.current
    val bitmap = remember(species) { FairySpriteCache.bitmapOf(context, species) }

    // Der Kern trägt einen aufgehellten Zonenton statt eines Art-Eigentons —
    // die Zone entscheidet über die Farbe, nicht die Fee: Genau das ist die
    // Zuordnung, an der die Lesbarkeit des Rätsels hängt.
    val ownGlow = if (pulsing) Gold else lerp(zoneColor, Color.White, 0.55f)

    val density = LocalDensity.current
    // Die Vorlagen sind Hochformat-Illustrationen (~1∶1,55, je nach Figur
    // leicht abweichend) statt der quadratischen Pixel-Art von früher — das
    // Seitenverhältnis kommt deshalb aus dem geladenen Bild selbst statt aus
    // einer festen Zahl. Passt die volle Breite nicht in die Zellhöhe, wird
    // stattdessen von der Höhe her skaliert, damit die Fee ihr Feld nie
    // verlässt.
    val spriteSize = remember(cellSize, density, bitmap) {
        val cellPx = with(density) { cellSize.toPx() }
        val aspect = bitmap.height.toFloat() / bitmap.width.toFloat()
        val width = cellPx * SPRITE_WIDTH_FRACTION
        val height = width * aspect
        if (height <= cellPx) {
            IntSize(width.roundToInt().coerceAtLeast(1), height.roundToInt().coerceAtLeast(1))
        } else {
            val clampedHeight = cellPx
            val clampedWidth = clampedHeight / aspect
            IntSize(clampedWidth.roundToInt().coerceAtLeast(1), clampedHeight.roundToInt().coerceAtLeast(1))
        }
    }
    // Das Schweben rastet auf diese Schrittweite ein, damit die Figur nicht in
    // Zwischenschritten wandert und die Kanten flimmern.
    val hoverStep = (spriteSize.width / 32f).coerceAtLeast(1f)

    Box(
        modifier = Modifier.size(cellSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(cellSize)
                .graphicsLayer { alpha = scale.value.coerceIn(0f, 1f) }
                .drawBehind {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val strength = if (pulsing) 0.55f + pulse.value * 0.35f else 0.42f

                    // Schatten auf dem Stein.
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x66000000), Color.Transparent),
                            center = Offset(center.x, size.height * 0.80f),
                            radius = size.minDimension * 0.30f,
                        ),
                        topLeft = Offset(size.width * 0.24f, size.height * 0.70f),
                        size = Size(size.width * 0.52f, size.height * 0.18f),
                    )

                    // Der weite Hof trägt die **Zonenfarbe**: An ihr hängt die
                    // Lesbarkeit des Rätsels, und besetzte Felder verstärken sie
                    // dadurch, statt sie zu verdünnen.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                zoneColor.copy(alpha = strength * 0.55f),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = size.minDimension * 0.62f,
                        ),
                        radius = size.minDimension * 0.62f,
                        center = center,
                    )
                    // Der Kern trägt den Eigenton der Fee.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ownGlow.copy(alpha = strength), Color.Transparent),
                            center = center,
                            radius = size.minDimension * 0.32f,
                        ),
                        radius = size.minDimension * 0.32f,
                        center = center,
                    )
                },
        )

        Box(
            modifier = Modifier
                .size(cellSize)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    val amplitude = cellSize.toPx() * 0.05f
                    translationY = (hover.value * amplitude / hoverStep).roundToInt() * hoverStep
                }
                .drawBehind {
                    // Bodenverankert statt zentriert: Die Fee "steht" am
                    // unteren Zellrand, wie eine Figur im Feld, statt als
                    // schwebender Fleck in der Mitte. Ein schmaler Rand bleibt
                    // zum Schattenoval hin frei, damit die Füße nicht genau
                    // auf der Kante kleben.
                    val left = ((size.width - spriteSize.width) / 2f).roundToInt()
                    val top = (size.height - spriteSize.height - size.height * 0.05f).roundToInt()

                    drawImage(
                        image = bitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bitmap.width, bitmap.height),
                        dstOffset = IntOffset(left, top),
                        dstSize = spriteSize,
                        // Weiche Illustration statt Pixel-Art — hier zählt
                        // eine glatte Kante beim Skalieren, keine erhaltene
                        // Blockstruktur.
                        filterQuality = FilterQuality.High,
                    )
                },
        )
    }
}

/** Zielbreite einer Fee, relativ zur Zellgröße — die Höhe folgt dem Bildseitenverhältnis. */
private const val SPRITE_WIDTH_FRACTION = 0.74f

/**
 * Das Merkzeichen „hier sitzt sicher keine Fee".
 *
 * Gezeichnet statt als Schriftzeichen, damit es wie in den Stein geritzt wirkt:
 * ein dunkler, versetzter Grund und darüber die helle Kante. Die leichte
 * Schräglage je Feld nimmt dem Brett das Schablonenhafte.
 */
@Composable
private fun WardMark(cellSize: Dp, pos: Pos) {
    // Fest aus der Position abgeleitet, damit dasselbe Feld immer gleich aussieht.
    val tiltDegrees = ((pos.row * 5 + pos.col * 11) % 7 - 3).toFloat()

    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, animationSpec = tween(180))
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .graphicsLayer {
                rotationZ = tiltDegrees
                alpha = appear.value
                scaleX = 0.85f + appear.value * 0.15f
                scaleY = 0.85f + appear.value * 0.15f
            }
            .drawBehind {
                // Goldgelbes Kreuz mit Gold-Glow, wie in der Vorlage: etwa
                // 42 Prozent der Feldgröße. Der Glow ist nicht nur Zierrat —
                // er hebt das Zeichen von der graugrünen Steinplatte ab, auf
                // der ein flaches Gelb blass wirkte.
                val center = Offset(size.width / 2f, size.height / 2f)
                val arm = size.minDimension * 0.21f
                val stroke = size.minDimension * 0.058f

                fun cross(color: Color, width: Float, offset: Offset = Offset.Zero) {
                    drawLine(
                        color = color,
                        start = Offset(center.x - arm + offset.x, center.y - arm + offset.y),
                        end = Offset(center.x + arm + offset.x, center.y + arm + offset.y),
                        strokeWidth = width,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = color,
                        start = Offset(center.x + arm + offset.x, center.y - arm + offset.y),
                        end = Offset(center.x - arm + offset.x, center.y + arm + offset.y),
                        strokeWidth = width,
                        cap = StrokeCap.Round,
                    )
                }

                // Der Schein liegt unter dem Zeichen: erst weich und breit,
                // dann der dunkle Schlagschatten, dann das Gold selbst.
                cross(Gold.copy(alpha = 0.30f), stroke * 2.6f)
                cross(Gold.copy(alpha = 0.45f), stroke * 1.7f)
                cross(Color(0x99000000), stroke, Offset(0f, stroke * 0.35f))
                cross(GoldLight, stroke)
            },
    )
}
