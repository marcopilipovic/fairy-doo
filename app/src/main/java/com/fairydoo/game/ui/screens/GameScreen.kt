package com.fairydoo.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.game.GameInput
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.GameStatus
import com.fairydoo.game.game.GameViewModel
import com.fairydoo.game.ui.components.GameBoard
import com.fairydoo.game.ui.theme.FairyDooTheme

@Composable
fun GameScreen(
    preferences: GamePreferencesRepository,
    onExit: () -> Unit,
) {
    val viewModel: GameViewModel = viewModel(
        factory = remember(preferences) { GameViewModel.factory(preferences) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Partie beim Betreten starten — nur einmal, nicht bei jedem Recompose.
    LaunchedEffect(Unit) {
        if (state.status == GameStatus.Idle) viewModel.startNewGame()
    }

    // Wandert die App in den Hintergrund, wird pausiert statt weitergespielt.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    GameContent(
        state = state,
        onTap = { x, y -> viewModel.onInput(GameInput.Tap(x, y)) },
        onPause = viewModel::pause,
        onResume = viewModel::resume,
        onRestart = { viewModel.startNewGame() },
        onExit = {
            viewModel.abandon()
            onExit()
        },
    )
}

@Composable
private fun GameContent(
    state: GameState,
    onTap: (Float, Float) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            GameHud(state = state, onPause = onPause)

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surface)
                    .pointerInput(state.isActive) {
                        if (!state.isActive) return@pointerInput
                        detectTapGestures { offset ->
                            onTap(
                                offset.x / size.width.toFloat(),
                                offset.y / size.height.toFloat(),
                            )
                        }
                    },
            ) {
                GameBoard(state = state, modifier = Modifier.fillMaxSize())
            }
        }

        when (state.status) {
            GameStatus.Paused -> Overlay(
                title = "Pause",
                message = null,
                primaryLabel = "Weiter",
                onPrimary = onResume,
                secondaryLabel = "Beenden",
                onSecondary = onExit,
            )

            GameStatus.Finished -> Overlay(
                title = "Runde vorbei",
                message = "${state.score} Punkte in ${state.moves} Zügen",
                primaryLabel = "Nochmal",
                onPrimary = onRestart,
                secondaryLabel = "Zum Menü",
                onSecondary = onExit,
            )

            else -> Unit
        }
    }
}

@Composable
private fun GameHud(state: GameState, onPause: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HudStat(label = "Punkte", value = state.score.toString())
        HudStat(label = "Zeit", value = "${state.remainingSeconds}s")
        HudStat(label = "Level", value = state.level.toString())

        TextButton(onClick = onPause, enabled = state.isActive) {
            Text("Pause", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HudStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/** Modales Overlay für Pause und Rundenende. */
@Composable
private fun Overlay(
    title: String,
    message: String?,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            // Fängt Taps ab, damit das Spielfeld darunter nicht reagiert.
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(32.dp),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )

                if (message != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth()) {
                    Text(primaryLabel, style = MaterialTheme.typography.labelLarge)
                }

                TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) {
                    Text(secondaryLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameRunningPreview() {
    FairyDooTheme {
        GameContent(
            state = GameState(status = GameStatus.Running, score = 320, moves = 32, remainingMillis = 24_000),
            onTap = { _, _ -> }, onPause = {}, onResume = {}, onRestart = {}, onExit = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GameFinishedPreview() {
    FairyDooTheme {
        GameContent(
            state = GameState(status = GameStatus.Finished, score = 780, moves = 78, remainingMillis = 0),
            onTap = { _, _ -> }, onPause = {}, onResume = {}, onRestart = {}, onExit = {},
        )
    }
}
