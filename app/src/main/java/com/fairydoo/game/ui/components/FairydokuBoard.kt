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
import com.fairydoo.game.art.glowArgb
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.sprites.FairySpriteCache
import com.fairydoo.game.ui.sprites.ZoneImageCache
import com.fairydoo.game.ui.theme.CellSeam
import com.fairydoo.game.ui.theme.ConflictRed
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.HedgeGreen
import com.fairydoo.game.ui.theme.HedgeLight
import com.fairydoo.game.ui.theme.HedgeShade
import com.fairydoo.game.ui.theme.TwigBark
import com.fairydoo.game.ui.theme.TwigLight
import com.fairydoo.game.ui.theme.TwigShade
import com.fairydoo.game.ui.theme.ZoneGrain
import com.fairydoo.game.ui.theme.ZoneStyles

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
    val zone = ZoneStyles[region % ZoneStyles.size]
    val context = LocalContext.current
    val tile = remember(zone) { ZoneImageCache.bitmapOf(context, zone) }
    val mark = state.markAt(pos)
    val isConflicting = pos in state.conflicts

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
                // Die Zone liegt als volle Fläche im Feld, nicht als Schein an
                // seinen Kanten: So trägt jedes Feld seine Zugehörigkeit in
                // sich, statt sie erst aus der Nachbarschaft ableiten zu lassen.
                if (tile != null) {
                    drawZoneTile(tile, pos)
                } else {
                    // Kein Bild für dieses Gebiet: das gezeichnete Motiv. Die
                    // Körnung gehört nur hierher — eine gemalte Kachel bringt
                    // ihre eigene Beschaffenheit mit.
                    drawRect(zone.fill)
                    drawZoneTexture(texture = zone.texture, color = zone.ink, pos = pos)
                    drawZoneGrain(pos)
                }
                drawZoneShading()
                drawZoneBorders(
                    pos = pos,
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
                zoneColor = zone.fill,
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
 * Legt den Ausschnitt der Zonenkachel in dieses Feld.
 *
 * Eine Kachel deckt [ZoneImageCache.TILE_CELLS] Felder je Kante ab; jedes Feld
 * zeichnet daraus nur seinen Teil. Benachbarte Felder ziehen benachbarte
 * Ausschnitte und ergeben ein durchgehendes Bild — alle drei Felder beginnt die
 * Kachel von vorn, und weil sie nahtlos ist, fällt das nicht auf.
 *
 * Der Weg über den Quellausschnitt statt über einen wiederholenden Shader ist
 * Absicht: Ein Shader kachelt in Bildpunkten, nicht in dp, und käme auf
 * Geräten unterschiedlicher Dichte in verschiedenen Größen heraus. So passt
 * sich die Kachel dagegen jeder Feldgröße von selbst an, vom 4×4-Brett bis zum
 * 8×8.
 */
private fun DrawScope.drawZoneTile(tile: ImageBitmap, pos: Pos) {
    val cells = ZoneImageCache.TILE_CELLS
    val slice = tile.width / cells
    drawImage(
        image = tile,
        srcOffset = IntOffset(
            x = pos.col.mod(cells) * slice,
            y = pos.row.mod(cells) * slice,
        ),
        srcSize = IntSize(slice, slice),
        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
        // Medium statt None: Die Kachel wird verkleinert, nicht vergrößert —
        // ohne Glättung entstünden Treppen in den gemalten Übergängen.
        filterQuality = FilterQuality.Medium,
    )
}

/**
 * Eine sehr leichte Wölbung über der Zonenfläche.
 *
 * Der Vorgänger war ein Stein-Relief mit heller Ober- und dunkler Unterkante.
 * Auf den durchscheinenden Moosfeldern hat es die einzelnen Steine
 * herausgehoben; auf einer deckenden Zonenfläche zerschneidet es das Gebiet
 * in Kacheln — genau die Wirkung, die die Fläche vermeiden soll.
 *
 * Geblieben ist ein Hauch davon: gerade genug, dass die Felder nicht wie
 * aufgemalt wirken, zu wenig, um die Zone zu zerteilen.
 */
private fun DrawScope.drawZoneShading() {
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0x0AFFFFFF),
                0.7f to Color.Transparent,
                1.0f to Color(0x0F000000),
            ),
            center = Offset(size.width * 0.4f, size.height * 0.35f),
            radius = size.maxDimension * 0.8f,
        ),
    )
}

/**
 * Feine Körnung über der Zonenfläche.
 *
 * Zehn satte Farben als glatte Blöcke nebeneinander wirken plakativ — nach
 * Buntpapier, nicht nach gemaltem Wald. Die Körnung nimmt den Flächen den Lack,
 * ohne ihre Farbe anzutasten: der Unterschied zwischen bedrucktem Papier und
 * lackiertem Blech.
 *
 * Die Punkte liegen fest, nicht zufällig — ein Feld sieht bei jedem
 * Neuzeichnen gleich aus, und über die Position gestreut wiederholt sich das
 * Korn auch zwischen benachbarten Feldern nicht.
 */
private fun DrawScope.drawZoneGrain(pos: Pos) {
    val seed = pos.row * 131 + pos.col * 71
    val radius = size.minDimension * 0.014f

    repeat(GRAIN_DOTS) { index ->
        val a = (((seed + index * 97) * 1103515245L + 12345L) ushr 16) % 1000L / 1000f
        val b = (((seed + index * 61) * 1103515245L + 54321L) ushr 16) % 1000L / 1000f
        drawCircle(
            color = ZoneGrain,
            radius = radius,
            center = Offset(size.width * a, size.height * b),
        )
    }
}

/**
 * Wie viele Körner je Feld.
 *
 * Genug, dass die Fläche lebt; wenige genug, dass das Zeichnen eines
 * 8×8-Bretts nicht spürbar wird — die Körnung wird nur bei einer Änderung neu
 * gezeichnet, nicht in jedem Bild.
 */
private const val GRAIN_DOTS = 26

/**
 * Die Kanten eines Feldes: eine Hecke an Zonengrenzen, eine feine Fuge zwischen
 * Feldern derselben Zone.
 *
 * Die Hecke trägt für alle Gebiete dieselbe Farbe. Welche Zone hinter ihr
 * liegt, beantwortet die Fläche — die Grenze sagt nur, *dass* dort eine
 * verläuft. Das entlastet das Auge, das sonst zehn Farbtöne gleichzeitig an
 * Kanten auseinanderhalten müsste.
 *
 * Die Fuge innerhalb der Zone bleibt dagegen dunkel und schmal: Sie soll die
 * Felder abzählbar machen, ohne die Zone optisch zu zerschneiden.
 */
private fun DrawScope.drawZoneBorders(
    pos: Pos,
    top: Boolean,
    bottom: Boolean,
    left: Boolean,
    right: Boolean,
) {
    val seamStroke = 1.dp.toPx()

    fun seam(atStart: Boolean, horizontal: Boolean) {
        val topLeft = when {
            horizontal && atStart -> Offset.Zero
            horizontal -> Offset(0f, size.height - seamStroke)
            atStart -> Offset.Zero
            else -> Offset(size.width - seamStroke, 0f)
        }
        val edgeSize =
            if (horizontal) Size(size.width, seamStroke) else Size(seamStroke, size.height)
        drawRect(CellSeam, topLeft = topLeft, size = edgeSize)
    }

    fun edge(isZoneEdge: Boolean, atStart: Boolean, horizontal: Boolean) {
        if (isZoneEdge) {
            drawHedge(pos = pos, atStart = atStart, horizontal = horizontal)
        } else {
            seam(atStart, horizontal)
        }
    }

    edge(top, atStart = true, horizontal = true)
    edge(bottom, atStart = false, horizontal = true)
    edge(left, atStart = true, horizontal = false)
    edge(right, atStart = false, horizontal = false)
}

/**
 * Eine niedrig geschnittene Hecke entlang einer Zonenkante.
 *
 * Von oben gesehen: überlappende Blattbüschel unterschiedlicher Größe, dazu
 * einzelne hellere Blätter obenauf. Das Unregelmäßige ist der ganze Zweck — eine
 * gleichmäßige Reihe wäre wieder ein gezogener Strich, nur in Grün.
 *
 * Die Größen sind aus der Feldposition und der Lage der Kante **berechnet**, nicht
 * zufällig gezogen. Damit sieht ein Feld bei jedem Neuzeichnen gleich aus, und
 * zwei Felder nebeneinander bekommen trotzdem verschiedene Büschel.
 *
 * Unter den Blättern liegt ein dunkler Saum. Er ist nicht Zierrat: Auf dem
 * Tannenhain, der fast dieselbe Farbe hat wie die Hecke, wäre die Grenze sonst
 * nicht mehr zu sehen — und die Zonenregel ist der Kern des Rätsels.
 */
private fun DrawScope.drawHedge(pos: Pos, atStart: Boolean, horizontal: Boolean) {
    val depth = HEDGE_DEPTH_DP.dp.toPx()
    val along = if (horizontal) size.width else size.height

    // Der Saum sitzt an der äußersten Kante, die Blätter wachsen nach innen
    // darüber — so schließt die Hecke sauber gegen die Nachbarzone ab.
    val shadeTopLeft = when {
        horizontal && atStart -> Offset.Zero
        horizontal -> Offset(0f, size.height - depth * 0.42f)
        atStart -> Offset.Zero
        else -> Offset(size.width - depth * 0.42f, 0f)
    }
    drawRect(
        color = HedgeShade,
        topLeft = shadeTopLeft,
        size = if (horizontal) {
            Size(size.width, depth * 0.42f)
        } else {
            Size(depth * 0.42f, size.height)
        },
    )

    // Die Kante wird in gleich breite Abschnitte geteilt; in jedem sitzt ein
    // Büschel. Ein Rest bliebe an der Feldgrenze als Lücke stehen, deshalb
    // rundet die Zahl der Büschel auf.
    val clusters = kotlin.math.ceil(along / (depth * 1.25f)).toInt().coerceAtLeast(2)
    val step = along / clusters
    val seed = pos.row * 47 + pos.col * 23 + (if (horizontal) 0 else 11) + (if (atStart) 0 else 5)

    for (index in 0..clusters) {
        // Drei voneinander unabhängige Streuwerte je Büschel. Ohne die
        // Verschiebung *entlang* der Kante säßen die Büschel exakt äquidistant,
        // und die Hecke sähe aus wie eine aufgefädelte Perlenkette — regelmäßig
        // ist beinahe so schlimm wie ein gezogener Strich.
        val jitterSize = ((seed + index * 37) % 11) / 11f
        val jitterAlong = (((seed + index * 53) % 9) / 9f - 0.5f) * 0.55f
        val jitterDepth = ((seed + index * 29) % 5) / 5f

        val radius = depth * (0.34f + jitterSize * 0.52f)
        val alongAt = (index + jitterAlong) * step
        // Wie tief das Büschel auf der Kante sitzt: mal weiter innen, mal weiter
        // außen, damit der Saum eine unruhige Kontur bekommt.
        val sink = depth * (0.10f + jitterDepth * 0.22f)

        // Die Büschel sitzen mit ihrer Mitte nahe der Kante, sodass ein Teil
        // ins Nachbarfeld ragt — dort zeichnet dessen eigene Hecke weiter, und
        // beide greifen ineinander.
        val center = when {
            horizontal && atStart -> Offset(alongAt, sink)
            horizontal -> Offset(alongAt, size.height - sink)
            atStart -> Offset(sink, alongAt)
            else -> Offset(size.width - sink, alongAt)
        }
        drawCircle(HedgeGreen, radius, center)

        // Ein kleineres, helleres Blatt obenauf: Erst der Ton-in-Ton-Kontrast
        // macht aus dem Büschel Laub statt eines grünen Flecks.
        if ((seed + index) % 4 != 0) {
            val lightOffset = radius * 0.38f
            drawCircle(
                color = HedgeLight,
                radius = radius * 0.52f,
                center = when {
                    horizontal && atStart -> center + Offset(lightOffset * 0.5f, lightOffset)
                    horizontal -> center + Offset(lightOffset * 0.5f, -lightOffset)
                    atStart -> center + Offset(lightOffset, lightOffset * 0.5f)
                    else -> center + Offset(-lightOffset, lightOffset * 0.5f)
                },
            )
        }
    }
}

/**
 * Wie tief die Hecke ins Feld hineinragt.
 *
 * Breiter als die 3 dp der früheren Linie — eine Hecke, die man für einen
 * Strich halten kann, ist keine. Deutlich breiter ginge auf Kosten der
 * Spielfläche: Auf dem 8×8-Brett ist ein Feld nur 44 dp groß.
 */
private const val HEDGE_DEPTH_DP = 5

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

/**
 * Ein kleiner Ast von einer Ecke zur anderen.
 *
 * Drei Striche übereinander: eine dunkle Kontur, die Rinde und darüber ein
 * schmales Glanzlicht. Das ist nicht Verzierung, sondern das, was das Zeichen
 * auf allen zehn Gebieten tragfähig macht — auf der cremefarbenen Lichtung
 * fällt die dunkle Kontur auf, auf dem nächtlichen Himmelstor das Glanzlicht.
 * Ein einfarbiger Ast wäre auf dem einen oder dem anderen verschwunden.
 *
 * [bend] biegt den Ast zur Seite; ohne diese Krümmung wäre es wieder ein
 * gezeichnetes Kreuz, nur in Braun.
 */
private fun DrawScope.twig(from: Offset, to: Offset, bend: Float, thickness: Float) {
    // Der Kontrollpunkt liegt seitlich neben der Mitte — senkrecht zur
    // Verbindung, damit die Krümmung unabhängig von der Richtung des Astes ist.
    val middle = Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f)
    val along = to - from
    val length = along.getDistance()
    val across = if (length == 0f) Offset.Zero else Offset(-along.y / length, along.x / length)
    val control = middle + across * bend

    fun stroke(color: Color, width: Float, shift: Offset = Offset.Zero) {
        val path = Path().apply {
            moveTo(from.x + shift.x, from.y + shift.y)
            quadraticBezierTo(
                control.x + shift.x,
                control.y + shift.y,
                to.x + shift.x,
                to.y + shift.y,
            )
        }
        drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Round))
    }

    stroke(TwigShade, thickness * 1.55f)
    stroke(TwigBark, thickness)
    // Das Glanzlicht sitzt leicht nach oben versetzt, als fiele das Licht von
    // vorn — dieselbe Richtung wie beim Schein der Feen.
    stroke(TwigLight, thickness * 0.34f, Offset(0f, -thickness * 0.24f))

    // Zwei Astansätze, damit es ein Zweig ist und kein gebogener Strich.
    val knotAt = from + along * 0.34f
    drawCircle(TwigBark, thickness * 0.42f, knotAt)
    drawCircle(TwigShade, thickness * 0.20f, knotAt + across * thickness * 0.3f)
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
                val center = Offset(size.width / 2f, size.height / 2f)
                val arm = size.minDimension * 0.21f
                val thickness = size.minDimension * 0.052f

                // Zwei Äste, übereinandergelegt. Die Kreuzform bleibt — sie
                // heißt überall „nicht hier" und wird ohne Erklärung verstanden;
                // nur das Material ist jetzt Holz statt weißer Farbe.
                //
                // Die Krümmung geht in unterschiedliche Richtungen: Zwei exakt
                // gleiche Bögen sähen aus wie ein gedrucktes Zeichen, zwei
                // verschiedene wie zwei aufgelesene Äste.
                twig(
                    from = center + Offset(-arm, -arm),
                    to = center + Offset(arm, arm),
                    bend = arm * 0.22f,
                    thickness = thickness,
                )
                twig(
                    from = center + Offset(arm, -arm),
                    to = center + Offset(-arm, arm),
                    bend = -arm * 0.16f,
                    thickness = thickness * 0.92f,
                )
            },
    )
}
