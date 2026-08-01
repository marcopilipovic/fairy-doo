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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.fairydoo.game.art.glowArgb
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.sprites.FairySpriteCache
import com.fairydoo.game.ui.theme.ConflictRed
import com.fairydoo.game.ui.theme.FaintBorder
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.MossDarkA
import com.fairydoo.game.ui.theme.MossDarkB
import com.fairydoo.game.ui.theme.MossLightA
import com.fairydoo.game.ui.theme.MossLightB
import com.fairydoo.game.ui.theme.RegionColors

/** Der Eigenton, den eine Fee um sich verbreitet. */
fun FairySpecies.glowColor(): Color = Color(glowArgb)

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
                .background(Color(0xB3070A18))
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
                                onHold = { onHoldCell(pos) },
                            )
                        }
                    }
                }
            }
        }
    }
}

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

    // Schachbrettvariation der Moosfelder, damit das Brett nicht flach wirkt.
    val evenCell = (pos.row + pos.col) % 2 == 0

    // Zusätzlich bekommt jeder Stein eine eigene, feste Tönung. Ohne diese
    // Streuung sieht ein 8×8-Brett aus wie gekachelte Tapete statt wie Steine.
    val tint = ((pos.row * 7 + pos.col * 13) % 5 - 2) * 0.018f

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
            .drawBehind {
                drawMossStone(evenCell = evenCell, tint = tint)
                drawZoneGlow(regionColor, topEdge, bottomEdge, leftEdge, rightEdge)
                drawStoneRelief()
                drawZoneBorders(
                    color = regionColor,
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

/** Der Moosstein selbst: Grundfarbe, Lichtfleck und Randabdunklung. */
private fun DrawScope.drawMossStone(evenCell: Boolean, tint: Float) {
    fun Color.shift(amount: Float) = Color(
        red = (red + amount).coerceIn(0f, 1f),
        green = (green + amount * 1.1f).coerceIn(0f, 1f),
        blue = (blue + amount * 0.6f).coerceIn(0f, 1f),
        alpha = alpha,
    )

    // Leicht angehoben gegenüber den Token-Werten: Vignette und Schattensaum
    // nehmen anschließend so viel Helligkeit weg, dass das Moos sonst grau
    // statt grün wirkt.
    val light = (if (evenCell) MossLightA else MossLightB).shift(tint + 0.05f)
    val dark = (if (evenCell) MossDarkA else MossDarkB).shift(tint + 0.04f)
    val lightCenter = if (evenCell) Offset(0.35f, 0.30f) else Offset(0.60f, 0.65f)

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(light, dark),
            center = Offset(size.width * lightCenter.x, size.height * lightCenter.y),
            radius = size.maxDimension * 0.75f,
        ),
    )

    // Vignette: nach außen dunkler, damit die Felder als einzelne Steine lesbar
    // werden und nicht als durchgehende Fläche.
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color.Transparent,
                0.62f to Color.Transparent,
                1.0f to Color(0x38000000),
            ),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.maxDimension * 0.72f,
        ),
    )
}

/**
 * Das Stein-Relief aus der Vorlage
 * (`inset 0 2px 6px rgba(255,255,255,.10), inset 0 -3px 8px rgba(0,0,0,.35)`):
 * oben eine Lichtkante, unten ein Schattensaum. Das lässt die Felder aus der
 * Fläche heraustreten.
 */
private fun DrawScope.drawStoneRelief() {
    val topHeight = size.height * 0.22f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x21FFFFFF), Color.Transparent),
            startY = 0f,
            endY = topHeight,
        ),
        size = Size(size.width, topHeight),
    )

    val bottomHeight = size.height * 0.30f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x47000000)),
            startY = size.height - bottomHeight,
            endY = size.height,
        ),
        topLeft = Offset(0f, size.height - bottomHeight),
        size = Size(size.width, bottomHeight),
    )
}

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
    val depth = size.minDimension * 0.30f
    val glow = color.copy(alpha = 0.22f)

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
 * Die Kantenlinien: kräftig an Zonengrenzen, als feine Rille zwischen Feldern
 * derselben Zone.
 *
 * Die Rille (dunkle Fuge mit heller Oberkante) statt einer einfachen blassen
 * Linie ist nötig, seit die Steine Relief haben: Sonst verschwimmen benachbarte
 * Felder einer Zone zu einer Fläche, und man muss die Felder abzählen, statt sie
 * zu sehen.
 */
private fun DrawScope.drawZoneBorders(
    color: Color,
    top: Boolean,
    bottom: Boolean,
    left: Boolean,
    right: Boolean,
) {
    val stroke = 2.5.dp.toPx()
    val groove = Color(0x66000000)
    val grooveEdge = Color(0x2EFFFFFF)

    fun edge(isZoneEdge: Boolean, topLeft: Offset, edgeSize: Size, horizontal: Boolean) {
        if (isZoneEdge) {
            drawRect(color, topLeft = topLeft, size = edgeSize)
            return
        }
        drawRect(groove, topLeft = topLeft, size = edgeSize)
        // Schmale Lichtkante an der Innenseite der Fuge.
        val highlight = if (horizontal) {
            Size(edgeSize.width, edgeSize.height * 0.4f)
        } else {
            Size(edgeSize.width * 0.4f, edgeSize.height)
        }
        drawRect(grooveEdge, topLeft = topLeft, size = highlight)
        drawRect(FaintBorder, topLeft = topLeft, size = edgeSize)
    }

    edge(top, Offset.Zero, Size(size.width, stroke), horizontal = true)
    edge(
        bottom,
        Offset(0f, size.height - stroke),
        Size(size.width, stroke),
        horizontal = true,
    )
    edge(left, Offset.Zero, Size(stroke, size.height), horizontal = false)
    edge(
        right,
        Offset(size.width - stroke, 0f),
        Size(stroke, size.height),
        horizontal = false,
    )
}

/**
 * Die Fee auf dem Feld.
 *
 * Platzhalter wie im Prototyp: das Emoji, aber mit Schattenwurf, zweistufigem
 * Schein und ruhigem Schweben — dadurch sitzt sie *auf* dem Stein, statt darauf
 * zu kleben. Sobald gezeichnete Feen-Sprites vorliegen, wird nur diese Funktion
 * ersetzt.
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

    val ownGlow = if (pulsing) Gold else species.glowColor()

    val density = LocalDensity.current
    val spriteSide = remember(cellSize, density) {
        (with(density) { cellSize.toPx() } * SPRITE_FILL).roundToInt().coerceAtLeast(1)
    }
    // Das Schweben rastet auf diese Schrittweite ein, damit die Figur nicht in
    // Zwischenschritten wandert und die Kanten flimmern.
    val hoverStep = (spriteSide / 32f).coerceAtLeast(1f)

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
                    val left = ((size.width - spriteSide) / 2f).roundToInt()
                    val top = ((size.height - spriteSide) / 2f).roundToInt()

                    drawImage(
                        image = bitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bitmap.width, bitmap.height),
                        dstOffset = IntOffset(left, top),
                        dstSize = IntSize(spriteSide, spriteSide),
                        // Die Vorlagen sind hochauflösende Pixel-Art und werden
                        // hier verkleinert. Ungefiltert fielen dabei Bildpunkte
                        // ersatzlos weg und die Figur bekäme Löcher; gefiltert
                        // bleiben Konturen und Muster erhalten.
                        filterQuality = FilterQuality.Medium,
                    )
                },
        )
    }
}

/** Anteil der Zelle, den eine Fee einnimmt. */
private const val SPRITE_FILL = 0.86f

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
                val arm = size.minDimension * 0.17f
                val center = Offset(size.width / 2f, size.height / 2f)
                val stroke = size.minDimension * 0.055f
                val depth = stroke * 0.55f

                fun cross(offset: Offset, color: Color, width: Float) {
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

                // Eingeritzte Tiefe: erst der dunkle Grund, dann die Lichtkante.
                cross(Offset(0f, depth), Color(0x8C000000), stroke)
                cross(Offset(0f, -depth * 0.4f), Color(0x26FFFFFF), stroke * 0.8f)
                cross(Offset.Zero, Color(0xB3E8E4FF), stroke * 0.85f)
            },
    )
}
