package com.fairydoo.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairydoo.game.game.GlobalLives
import com.fairydoo.game.game.GlobalLivesState
import com.fairydoo.game.ui.GameCopy
import com.fairydoo.game.ui.theme.DangerRose
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.LeafGreen
import com.fairydoo.game.ui.theme.PanelBottom
import com.fairydoo.game.ui.theme.PanelTop
import com.fairydoo.game.ui.theme.TextOnGold
import com.fairydoo.game.ui.theme.TextPrimary

/** Wie viele gesperrte Level als Vorschau unter dem höchsten freigeschalteten stehen. */
private const val LOCKED_PREVIEW_COUNT = 8

/**
 * Die Levelkarte: ein Zahlenraster von 1 bis zum höchsten je erreichten Level
 * plus ein paar gesperrten Vorschauen. Der Fortschritt bleibt erhalten, auch
 * wenn ein späterer Versuch misslingt — nur das Gewinnen schaltet weiter frei.
 */
@Composable
fun LevelSelectScreen(
    highestLevelUnlocked: Int,
    currentLevel: Int,
    globalLives: GlobalLivesState,
    onClose: (() -> Unit)?,
    onSelectLevel: (Int) -> Unit,
) {
    NightBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🗺️ Die Levelkarte",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                )
                if (onClose != null) {
                    CloseButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd))
                }
            }

            Spacer(Modifier.height(14.dp))

            GlobalLivesRow(globalLives)

            Spacer(Modifier.height(18.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(highestLevelUnlocked + LOCKED_PREVIEW_COUNT) { index ->
                    val level = index + 1
                    LevelTile(
                        level = level,
                        unlocked = level <= highestLevelUnlocked,
                        completed = level < highestLevelUnlocked,
                        current = level == currentLevel,
                        canPlay = globalLives.lives > 0,
                        onClick = { onSelectLevel(level) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalLivesRow(state: GlobalLivesState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "❤️".repeat(state.lives) + "🤍".repeat((GlobalLives.MAX - state.lives).coerceAtLeast(0)),
            fontSize = 18.sp,
        )
        if (state.lives < GlobalLives.MAX) {
            val remainingSeconds = ((state.nextLifeAtMillis - System.currentTimeMillis())
                .coerceAtLeast(0L) / 1000L).toInt()
            Text(
                text = if (state.lives == 0) {
                    "Keine Leben mehr — nächstes in ${GameCopy.formatTime(remainingSeconds)}"
                } else {
                    "Nächstes Leben in ${GameCopy.formatTime(remainingSeconds)}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (state.lives == 0) DangerRose else TextPrimary.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LevelTile(
    level: Int,
    unlocked: Boolean,
    completed: Boolean,
    current: Boolean,
    canPlay: Boolean,
    onClick: () -> Unit,
) {
    val clickableNow = unlocked && canPlay
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = if (current) {
                    Brush.verticalGradient(listOf(GoldLight, Gold))
                } else {
                    Brush.verticalGradient(listOf(PanelTop, PanelBottom))
                },
            )
            .border(
                width = 1.dp,
                color = if (unlocked) Gold.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
            )
            .let {
                if (clickableNow) {
                    it.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    it
                }
            }
            .alpha(
                when {
                    !unlocked -> 0.35f
                    !canPlay -> 0.5f
                    else -> 1f
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (!unlocked) {
            Text(text = "🔒", fontSize = 18.sp)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$level",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (current) TextOnGold else TextPrimary,
                )
                if (completed) {
                    Text(
                        text = "✔",
                        fontSize = 11.sp,
                        color = if (current) TextOnGold else LeafGreen,
                    )
                }
            }
        }
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "✕", color = TextPrimary, fontSize = 16.sp)
    }
}
