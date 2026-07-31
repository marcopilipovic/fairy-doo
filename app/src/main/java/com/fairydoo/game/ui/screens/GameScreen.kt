package com.fairydoo.game.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
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
import com.fairydoo.game.game.PowerUp
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.game.model.PuzzleGenerator
import com.fairydoo.game.ui.components.FairydokuBoard
import com.fairydoo.game.ui.components.PowerUpBar
import com.fairydoo.game.ui.theme.ErrorRed
import com.fairydoo.game.ui.theme.FairyDooTheme
import kotlin.random.Random

@Composable
fun GameScreen(
    preferences: GamePreferencesRepository,
    onExit: () -> Unit,
) {
    val viewModel: GameViewModel = viewModel(
        factory = remember(preferences) { GameViewModel.factory(preferences) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isPreparing by viewModel.isPreparing.collectAsStateWithLifecycle()

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
        isPreparing = isPreparing,
        onTapCell = { viewModel.onInput(GameInput.TapCell(it)) },
        onUsePowerUp = { viewModel.onInput(GameInput.UsePowerUp(it)) },
        onNextLevel = { viewModel.onInput(GameInput.NextLevel) },
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
    isPreparing: Boolean,
    onTapCell: (Pos) -> Unit,
    onUsePowerUp: (PowerUp) -> Unit,
    onNextLevel: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant)))
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GameHud(state = state, onPause = onPause)

            Spacer(Modifier.height(12.dp))

            LevelProgress(state = state)

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (isPreparing || state.puzzle == null) {
                    CircularProgressIndicator(color = colors.primary)
                } else {
                    FairydokuBoard(state = state, onTapCell = onTapCell)
                }
            }

            Spacer(Modifier.height(16.dp))

            PowerUpBar(state = state, onUse = onUsePowerUp)
        }

        when (state.status) {
            GameStatus.Paused -> Overlay(
                title = "Pause",
                message = "Der Wald wartet.",
                primaryLabel = "Weiter",
                onPrimary = onResume,
                secondaryLabel = "Beenden",
                onSecondary = onExit,
            )

            GameStatus.LevelComplete -> Overlay(
                title = "Level ${state.level} geschafft!",
                message = "${state.score} Punkte — der Wald wird dichter.",
                primaryLabel = "Weiter",
                onPrimary = onNextLevel,
                secondaryLabel = "Zum Menü",
                onSecondary = onExit,
            )

            GameStatus.GameOver -> Overlay(
                title = "Das Licht erlischt",
                message = "Level ${state.level} · ${state.score} Punkte",
                primaryLabel = "Neuer Versuch",
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
        HudStat(label = "Level", value = state.level.toString())
        HudStat(
            label = if (state.slowMotionActive) "Zeit ❄" else "Zeit",
            value = "${state.remainingSeconds}s",
            // Unter zehn Sekunden wird die Uhr rot — der einzige Hinweis, dass
            // es gleich vorbei ist.
            emphasised = state.remainingSeconds <= 10,
        )
        HudStat(
            label = "Fehler",
            value = "${state.mistakesLeft}",
            emphasised = state.mistakesLeft <= 1,
        )

        TextButton(onClick = onPause, enabled = state.isActive) {
            Text("Pause", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HudStat(label: String, value: String, emphasised: Boolean = false) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = if (emphasised) ErrorRed else MaterialTheme.colorScheme.onBackground,
        )
    }
}

/** Der „Level up“-Balken: wie viele Feen bereits richtig sitzen. */
@Composable
private fun LevelProgress(state: GameState) {
    val progress by animateFloatAsState(
        targetValue = state.levelProgress,
        label = "levelProgress",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = when (state.remainingFairies) {
                0 -> "Alle Feen sitzen"
                1 -> "Noch 1 Fee zu platzieren"
                else -> "Noch ${state.remainingFairies} Feen zu platzieren"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/** Modales Overlay für Pause, Levelabschluss und Spielende. */
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

/** Fester Seed, damit die Vorschau immer dasselbe Brett zeigt. */
private fun previewState(status: GameStatus = GameStatus.Running): GameState {
    val puzzle = PuzzleGenerator.generate(size = 5, random = Random(7))
    return GameState(
        status = status,
        level = 3,
        score = 4500,
        puzzle = puzzle,
        remainingMillis = 42_000,
        roundDurationMillis = 85_000,
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GameRunningPreview() {
    FairyDooTheme {
        GameContent(
            state = previewState(),
            isPreparing = false,
            onTapCell = {}, onUsePowerUp = {}, onNextLevel = {},
            onPause = {}, onResume = {}, onRestart = {}, onExit = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun GameOverPreview() {
    FairyDooTheme {
        GameContent(
            state = previewState(GameStatus.GameOver),
            isPreparing = false,
            onTapCell = {}, onUsePowerUp = {}, onNextLevel = {},
            onPause = {}, onResume = {}, onRestart = {}, onExit = {},
        )
    }
}
