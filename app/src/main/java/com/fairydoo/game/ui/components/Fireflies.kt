package com.fairydoo.game.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.fairydoo.game.ui.theme.FireflyYellow

/** Ein Glühwürmchen: Position in Prozent, Größe in dp, eigener Rhythmus. */
private data class Firefly(
    val xPercent: Float,
    val yPercent: Float,
    val sizeDp: Float,
    val delaySeconds: Float,
)

/**
 * Der Glühwürmchen-Schleier über dem Nachtwald.
 *
 * Die 16 Positionen sind fest gerechnet statt zufällig: So flackern sie bei
 * jedem Start gleich, und ein Recompose verschiebt sie nicht.
 */
private val fireflies: List<Firefly> = List(16) { index ->
    Firefly(
        xPercent = (index * 61 % 100) / 100f,
        yPercent = (index * 37 + 13) % 100 / 100f,
        sizeDp = 3f + (index % 3) * 2f,
        delaySeconds = (index * 0.43f) % 3f,
    )
}

@Composable
fun FireflyLayer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "fireflies")

    // Je Glühwürmchen ein eigener Verlauf, damit sie versetzt blinken.
    val phases = fireflies.mapIndexed { index, firefly ->
        val durationMillis = ((2.2f + firefly.delaySeconds) * 1000).toInt()
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = androidx.compose.animation.core.StartOffset(
                    (firefly.delaySeconds * 1000).toInt(),
                ),
            ),
            label = "firefly$index",
        )
    }

    val values = phases.map { it.value }

    Canvas(modifier = modifier.fillMaxSize()) {
        fireflies.forEachIndexed { index, firefly ->
            val phase = values[index]
            val alpha = 0.15f + phase * 0.75f
            val scale = 0.7f + phase * 0.45f
            val radius = firefly.sizeDp.dp.toPx() / 2f * scale
            val center = Offset(size.width * firefly.xPercent, size.height * firefly.yPercent)

            // Weicher Hof (entspricht dem box-shadow der Vorlage) …
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FireflyYellow.copy(alpha = alpha * 0.8f),
                        FireflyYellow.copy(alpha = 0f),
                    ),
                    center = center,
                    radius = radius * 4f,
                ),
                radius = radius * 4f,
                center = center,
            )
            // … und der Kern.
            drawCircle(
                color = FireflyYellow.copy(alpha = alpha),
                radius = radius,
                center = center,
            )
        }
    }
}
