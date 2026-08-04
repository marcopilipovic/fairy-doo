package com.fairydoo.game.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

/**
 * Die "Twilight"-Parallax-Waldszene hinter dem Feenpfad, nach der Vorlage
 * `Bilder/Fairydoku Levelkarte/design_handoff_feenpfad_karte/`.
 *
 * Fünf Ebenen, jede mit eigenem Parallaxe-Faktor. Alle Ebenen liegen als
 * Geschwister im selben scrollenden Container wie die Level-Knoten (nicht
 * außerhalb) — sie erben damit den normalen Scroll wie im Vorlagen-Prototyp
 * (der Browser scrollt den ganzen Inner-Canvas), und bekommen zusätzlich
 * einen `graphicsLayer`-Versatz `scrollPx * depth` obendrauf. Bei `depth`
 * nahe 1 hebt sich dieser Zusatz-Versatz fast auf (Ebene wirkt fast fix =
 * fern), bei negativem `depth` (Vordergrund) verstärkt er die Bewegung.
 */
private object TwilightTokens {
    val deepGradient = listOf(
        0f to Color(0xFF173B2B),
        0.34f to Color(0x99123025),
        0.68f to Color(0xFF102A20),
        1f to Color(0xFF0D241C),
    )
    val ridgeFar = listOf(Color(0xFF3D7050), Color(0xFF2E5A3E), Color(0xFF1F4029))
    val ridgeMid = listOf(Color(0xFF264B32), Color(0xFF17331F), Color(0xFF0D2214))
    val pineDeep = Color(0xFF31593F)
    val pineMid = Color(0xFF1D4030)
    val pineForeground = Color(0xFF071410)
    val distantTree = Color(0xFF2F5B42)
    val mossPool = Color(0x1E86BE78)
    val mushroomCaps = listOf(Color(0xFF6D4A63), Color(0xFF4C4A72), Color(0xFF5C5A48))
    val stoneColor = Color(0xFF1E2E25)
    val starColors = listOf(Color(0xFFFFF6DD), Color(0xFFEAFFF2), Color(0xFFF7ECFF), Color(0xFFFFF3CF))
}

/** Deterministischer Pseudozufall, exakt wie in der Vorlage. */
private fun hash(i: Float, seed: Float): Float =
    abs((sin((i + seed) * 12.9898f) * 43758.5453f) % 1f)

@Composable
internal fun BoxScope.TwilightScenery(
    canvasHeight: Dp,
    scrollState: ScrollState,
    maxScrollPx: Float,
    laneWidth: Dp,
) {
    val maxScrollDp = with(LocalDensity.current) { maxScrollPx.toDp() }

    // 1. deep (0.78)
    ParallaxLayer(depth = 0.78f, canvasHeight = canvasHeight, maxScrollDp = maxScrollDp, scrollState = scrollState) { height ->
        DeepLayerContent(laneWidth, height)
    }
    // 2. far (0.50)
    ParallaxLayer(depth = 0.50f, canvasHeight = canvasHeight, maxScrollDp = maxScrollDp, scrollState = scrollState) { height ->
        FarLayerContent(laneWidth, height)
    }
    // 3. mid (0.26)
    ParallaxLayer(depth = 0.26f, canvasHeight = canvasHeight, maxScrollDp = maxScrollDp, scrollState = scrollState) { height ->
        MidLayerContent(laneWidth, height)
    }
}

/** Vordergrund — wird nach den Knoten aufgerufen, damit er sie leicht rahmt. */
@Composable
internal fun BoxScope.TwilightForeground(
    canvasHeight: Dp,
    scrollState: ScrollState,
    maxScrollPx: Float,
    laneWidth: Dp,
) {
    val maxScrollDp = with(LocalDensity.current) { maxScrollPx.toDp() }
    ParallaxLayer(depth = -0.20f, canvasHeight = canvasHeight, maxScrollDp = maxScrollDp, scrollState = scrollState) { height ->
        ForegroundLayerContent(laneWidth, height)
    }
}

@Composable
private fun BoxScope.ParallaxLayer(
    depth: Float,
    canvasHeight: Dp,
    maxScrollDp: Dp,
    scrollState: ScrollState,
    content: @Composable BoxScope.(height: Dp) -> Unit,
) {
    val buffer = maxScrollDp * abs(depth) + 24.dp
    val topPad = if (depth < 0f) buffer else 0.dp
    val expandedHeight = canvasHeight + buffer
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            // scrollState.value wird hier gelesen, nicht außerhalb übergeben,
            // damit dieser Layer bei jedem Scroll-Schritt neu positioniert
            // wird, ohne dass die ganze Elternkomposition neu läuft.
            .graphicsLayer { translationY = scrollState.value * depth },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = -topPad)
                .height(expandedHeight),
        ) {
            content(expandedHeight)
        }
    }
}

@Composable
private fun DeepLayerContent(laneWidth: Dp, canvasHeight: Dp) {
    Canvas(modifier = Modifier.fillMaxWidth().height(canvasHeight)) {
        drawRect(brush = Brush.verticalGradient(colorStops = TwilightTokens.deepGradient.toTypedArray()))

        val islandsY = floatArrayOf(0.06f, 0.34f, 0.62f, 0.90f)
        val islandColors = listOf(
            Color(0x21B2E0B2), Color(0x1C9ECEE8), Color(0x1CCEB6EE), Color(0x1AB2E0B2),
        )
        islandsY.forEachIndexed { index, fy ->
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(islandColors[index % islandColors.size], Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * fy),
                    radius = size.width * 0.55f,
                ),
                topLeft = Offset(0f, size.height * fy - size.width * 0.09f),
                size = Size(size.width, size.width * 0.18f),
            )
        }

        // Die Design-Werte (Abstand, Größe, Einzug) sind in derselben
        // "dp-Einheit" wie die Vorlage gemeint — deshalb hier auf
        // Bildschirm-Pixel skalieren, statt sie direkt gegen die (viel
        // breiteren) rohen Canvas-Pixel zu zeichnen.
        scale(density, density, pivot = Offset.Zero) {
            drawPineField(
                laneWidth = laneWidth.value,
                fieldHeight = size.height / density,
                spacing = 40f,
                scaleMin = 0.5f,
                scaleMax = 0.7f,
                xMin = 6f,
                xMax = 26f,
                color = TwilightTokens.pineDeep,
                opacity = 0.85f,
                seed = 3.1f,
            )
        }
    }
}

@Composable
private fun FarLayerContent(laneWidth: Dp, canvasHeight: Dp) {
    Canvas(modifier = Modifier.fillMaxWidth().height(canvasHeight)) {
        scale(density, density, pivot = Offset.Zero) {
            drawRidgeRow(
                spacing = 214f, ridgeWidth = 560f, ridgeHeight = 170f,
                colors = TwilightTokens.ridgeFar, fieldHeight = size.height / density,
            )
        }

        val fogY = floatArrayOf(0.08f, 0.36f, 0.64f, 0.90f)
        val fogColors = listOf(Color(0x12C4E2D0), Color(0x0FBAD6E8), Color(0x0FD2C4EC), Color(0x12C4E2D0))
        fogY.forEachIndexed { index, fy ->
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, fogColors[index % fogColors.size], Color.Transparent),
                ),
                topLeft = Offset(0f, size.height * fy - 90f),
                size = Size(size.width, 180f),
            )
        }
    }
}

@Composable
private fun MidLayerContent(laneWidth: Dp, canvasHeight: Dp) {
    Canvas(modifier = Modifier.fillMaxWidth().height(canvasHeight)) {
        scale(density, density, pivot = Offset.Zero) {
            drawRidgeRow(
                spacing = 152f, ridgeWidth = 520f, ridgeHeight = 120f,
                colors = TwilightTokens.ridgeMid, fieldHeight = size.height / density,
            )

            // Ferne, sehr kleine Baumreihen dicht am Rand.
            drawPineField(
                laneWidth = laneWidth.value,
                fieldHeight = size.height / density,
                spacing = 58f,
                scaleMin = 0.30f,
                scaleMax = 0.46f,
                xMin = 10f,
                xMax = 30f,
                color = TwilightTokens.distantTree,
                opacity = 0.6f,
                seed = 7.7f,
            )

            drawPineField(
                laneWidth = laneWidth.value,
                fieldHeight = size.height / density,
                spacing = 58f,
                scaleMin = 0.8f,
                scaleMax = 1.05f,
                xMin = 4f,
                xMax = 24f,
                color = TwilightTokens.pineMid,
                opacity = 0.85f,
                seed = 5.2f,
            )
        }

        val poolsY = floatArrayOf(0.12f, 0.33f, 0.55f, 0.76f, 0.95f)
        poolsY.forEach { fy ->
            drawOval(
                color = TwilightTokens.mossPool,
                topLeft = Offset(size.width * 0.32f, size.height * fy - 28f),
                size = Size(size.width * 0.36f, 56f),
            )
        }
    }
}

@Composable
private fun ForegroundLayerContent(laneWidth: Dp, canvasHeight: Dp) {
    Canvas(modifier = Modifier.fillMaxWidth().height(canvasHeight)) {
        scale(density, density, pivot = Offset.Zero) {
            drawPineField(
                laneWidth = laneWidth.value,
                fieldHeight = size.height / density,
                spacing = 112f,
                scaleMin = 1.1f,
                scaleMax = 1.4f,
                xMin = 2f,
                xMax = 12f,
                color = TwilightTokens.pineForeground,
                opacity = 0.92f,
                seed = 9.4f,
            )
        }
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0x80040A08), Color.Transparent, Color.Transparent, Color(0x80040A08),
                ),
                startX = 0f,
                endX = size.width,
            ),
        )
    }
}

/** Zeichnet ein Tannenfeld: je Zeile links und rechts ein Baum, deterministisch verteilt. */
private fun DrawScope.drawPineField(
    laneWidth: Float,
    fieldHeight: Float,
    spacing: Float,
    scaleMin: Float,
    scaleMax: Float,
    xMin: Float,
    xMax: Float,
    color: Color,
    opacity: Float,
    seed: Float,
    onlyOutsideCorridor: Boolean = false,
) {
    val centerX = laneWidth / 2f
    var i = 0
    var y = -spacing
    while (y < fieldHeight) {
        for (side in intArrayOf(-1, 1)) {
            val r1 = hash(i * 2f + if (side < 0) 0f else 1f, seed)
            val r2 = hash(i * 2f + 61f + if (side < 0) 0f else 1f, seed)
            val sc = scaleMin + (scaleMax - scaleMin) * r1
            val w = 60f * sc
            val h = 100f * sc
            val inset = xMin + (xMax - xMin) * r2
            val treeTop = y + (if (side > 0) spacing * 0.5f else 0f) + r2 * spacing * 0.35f
            val treeLeft = if (side < 0) -inset else centerX * 2f + inset - w
            val insideCorridor = treeLeft + w > centerX - laneWidth / 2f && treeLeft < centerX + laneWidth / 2f
            if (!onlyOutsideCorridor || !insideCorridor) {
                drawPineTree(Offset(treeLeft, treeTop), w, h, color.copy(alpha = opacity))
            }
            i++
        }
        y += spacing
    }
}

/** Die Tannen-Silhouette, 1:1 aus der Vorlage (viewBox 60×100). */
private fun DrawScope.drawPineTree(topLeft: Offset, w: Float, h: Float, color: Color) {
    val sx = w / 60f
    val sy = h / 100f
    fun p(x: Float, y: Float) = Offset(topLeft.x + x * sx, topLeft.y + y * sy)

    drawRect(color = color, topLeft = p(26.5f, 72f), size = Size(7f * sx, 28f * sy))

    val tiers = listOf(
        Triple(p(30f, 40f), p(57f, 87f), listOf(p(42f, 82f), p(18f, 82f), p(3f, 87f))),
        Triple(p(30f, 20f), p(51f, 63f), listOf(p(39f, 58f), p(21f, 58f), p(9f, 63f))),
        Triple(p(30f, 3f), p(46f, 41f), listOf(p(36f, 36f), p(24f, 36f), p(14f, 41f))),
    )
    for ((top, right, rest) in tiers) {
        val path = Path().apply {
            moveTo(top.x, top.y)
            lineTo(right.x, right.y)
            rest.forEach { lineTo(it.x, it.y) }
            close()
        }
        drawPath(path, color = color)
    }
}

/** Eine "Rücken"-Reihe (Hügelsilhouetten), abwechselnd links/rechts eingerückt. */
private fun DrawScope.drawRidgeRow(
    spacing: Float,
    ridgeWidth: Float,
    ridgeHeight: Float,
    colors: List<Color>,
    fieldHeight: Float,
) {
    var i = 0
    var y = -spacing
    val brush = Brush.verticalGradient(colors)
    while (y < fieldHeight) {
        val insetLeft = if (i % 2 == 0) -128f else -66f
        val rect = Rect(offset = Offset(insetLeft, y), size = Size(ridgeWidth, ridgeHeight))
        val roundRect = RoundRect(
            rect = rect,
            topLeft = CornerRadius(ridgeWidth / 2f, ridgeHeight),
            topRight = CornerRadius(ridgeWidth / 2f, ridgeHeight),
            bottomLeft = CornerRadius.Zero,
            bottomRight = CornerRadius.Zero,
        )
        val path = Path().apply { addRoundRect(roundRect) }
        drawPath(path, brush = brush)
        y += spacing
        i++
    }
}

/**
 * Bodenrequisiten (Pilz/Steinhäufchen) zwischen zwei Knoten, plus Lichtinsel
 * und Bodenschatten je Knoten, plus Glitzer-Funken. Nicht-parallax (depth 0),
 * deshalb direkt mit den Knoten-Koordinaten verdrahtet statt als eigene
 * ParallaxLayer.
 */
@Composable
internal fun BoxScope.TwilightGlowLayer(
    laneWidth: Dp,
    canvasHeight: Dp,
    nodeCenters: List<Offset>,
) {
    Canvas(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(canvasHeight),
    ) {
        // Seitliche Abdunkelung + Korridorlicht.
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0x99040A08), Color.Transparent, Color.Transparent, Color(0x99040A08)),
            ),
        )
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color(0x0DCEE8C4), Color.Transparent),
            ),
        )

        // Ab hier in "dp-Einheiten" wie die Vorlage — [nodeCenters] kommt
        // bereits so an (siehe Aufrufer), deshalb keine erneute
        // Pixel-Umrechnung nötig, nur die Canvas-Skalierung.
        scale(density, density, pivot = Offset.Zero) {
            // Lichtinsel + Bodenschatten je Knoten.
            nodeCenters.forEach { center ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0x1CFFEEBA),
                            0.45f to Color(0x0BFFEEBA),
                            1f to Color.Transparent,
                        ),
                        center = center,
                        radius = 95f,
                    ),
                    radius = 95f,
                    center = center,
                )
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x80020805), Color.Transparent),
                        center = Offset(center.x, center.y + 30f),
                        radius = 38f,
                    ),
                    topLeft = Offset(center.x - 38f, center.y + 16f),
                    size = Size(76f, 20f),
                )
            }

            // Bodenrequisiten zwischen aufeinanderfolgenden Knoten.
            val centerX = laneWidth.value / 2f
            for (n in 0 until nodeCenters.size - 1) {
                val a = nodeCenters[n]
                val b = nodeCenters[n + 1]
                val r1 = hash(n.toFloat(), 2.9f)
                val r2 = hash(n + 41f, 2.9f)
                val r3 = hash(n + 77f, 2.9f)
                val isStone = r3 < 0.45f
                val sc = 0.72f + 0.5f * r2
                val w = (if (isStone) 54f else 26f) * sc
                val h = 30f * sc
                val midY = (a.y + b.y) / 2f
                val away = (a.x + b.x) / 2f < centerX
                // Feste Werte aus der Vorlage (232…328 rechts, 18…114
                // links, bei 374 dp Referenzbreite) — nicht proportional zur
                // tatsächlichen Kartenbreite, sonst landen die Requisiten je
                // nach Gerät außerhalb des sichtbaren Bereichs.
                val x = if (away) 232f + 96f * r1 else 18f + 96f * r1
                val top = midY - h / 2f + (r2 - 0.5f) * 26f

                // Bodenschatten + warmer Schein.
                drawOval(
                    color = Color(0x8C02080A),
                    topLeft = Offset(x + w * 0.06f, top + h - 4f),
                    size = Size(w * 0.88f, 8f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1AFFE2B4), Color.Transparent),
                        center = Offset(x + w / 2f, top + h / 2f),
                        radius = w * 0.9f,
                    ),
                    radius = w * 0.9f,
                    center = Offset(x + w / 2f, top + h / 2f),
                )

                if (isStone) {
                    drawStoneCluster(Offset(x, top), w, h)
                } else {
                    drawMushroomProp(Offset(x, top), w, h, TwilightTokens.mushroomCaps[n % TwilightTokens.mushroomCaps.size])
                }
            }
        }
    }

    TwilightSparkles(canvasHeight = canvasHeight)
}

private fun DrawScope.drawMushroomProp(topLeft: Offset, w: Float, h: Float, capColor: Color) {
    val sx = w / 26f
    val sy = h / 30f
    fun p(x: Float, y: Float) = Offset(topLeft.x + x * sx, topLeft.y + y * sy)
    drawRect(color = Color(0xFF3B4A3F), topLeft = p(10f, 12f), size = Size(6f * sx, 18f * sy))
    val cap = Path().apply {
        moveTo(p(1f, 14f).x, p(1f, 14f).y)
        cubicTo(p(1f, 5.5f).x, p(1f, 5.5f).y, p(7f, 1f).x, p(7f, 1f).y, p(13f, 1f).x, p(13f, 1f).y)
        cubicTo(p(19f, 1f).x, p(19f, 1f).y, p(25f, 5.5f).x, p(25f, 5.5f).y, p(25f, 14f).x, p(25f, 14f).y)
        cubicTo(p(18.5f, 16.5f).x, p(18.5f, 16.5f).y, p(7.5f, 16.5f).x, p(7.5f, 16.5f).y, p(1f, 14f).x, p(1f, 14f).y)
        close()
    }
    drawPath(cap, color = capColor)
}

private fun DrawScope.drawStoneCluster(topLeft: Offset, w: Float, h: Float) {
    val sx = w / 54f
    val sy = h / 30f
    fun p(x: Float, y: Float) = Offset(topLeft.x + x * sx, topLeft.y + y * sy)
    drawOval(color = TwilightTokens.stoneColor, topLeft = Offset(p(1f, 14f).x, p(1f, 14f).y), size = Size(26f * sx, 16f * sy))
    drawOval(color = TwilightTokens.stoneColor, topLeft = Offset(p(25f, 16f).x, p(25f, 16f).y), size = Size(28f * sx, 14f * sy))
    drawOval(color = TwilightTokens.stoneColor, topLeft = Offset(p(11f, 3f).x, p(11f, 3f).y), size = Size(30f * sx, 20f * sy))
}

@Composable
private fun BoxScope.TwilightSparkles(canvasHeight: Dp) {
    val positions = remember {
        listOf(
            0.11f to 0.29f, 0.24f to 0.41f, 0.41f to 0.30f, 0.57f to 0.26f, 0.70f to 0.38f, 0.85f to 0.37f,
            0.06f to 0.11f, 0.87f to 0.19f, 0.09f to 0.34f, 0.90f to 0.48f, 0.12f to 0.62f, 0.86f to 0.77f, 0.10f to 0.91f,
        )
    }
    val transition = rememberInfiniteTransition(label = "twStars")
    val phases = positions.mapIndexed { index, _ ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400 + (index % 5) * 380),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "star$index",
        )
    }
    Canvas(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(canvasHeight),
    ) {
        positions.forEachIndexed { index, (fx, fy) ->
            val phase = phases[index].value
            val alpha = 0.15f + phase * 0.75f
            val color = TwilightTokens.starColors[index % TwilightTokens.starColors.size]
            val center = Offset(size.width * fx, size.height * fy)
            drawCircle(color = color.copy(alpha = alpha * 0.5f), radius = 8f, center = center)
            drawCircle(color = color.copy(alpha = alpha), radius = 2f, center = center)
        }
    }
}
