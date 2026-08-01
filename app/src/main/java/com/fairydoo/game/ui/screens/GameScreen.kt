package com.fairydoo.game.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fairydoo.game.audio.FairyAudio
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.data.PlayerProfile
import com.fairydoo.game.game.GameInput
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.GameStatus
import com.fairydoo.game.game.GameViewModel
import com.fairydoo.game.game.PowerUp
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.GameCopy
import com.fairydoo.game.ui.components.FairydokuBoard
import com.fairydoo.game.ui.components.FireflyLayer
import com.fairydoo.game.ui.components.GameOverOverlay
import com.fairydoo.game.ui.components.IntroOverlay
import com.fairydoo.game.ui.components.LevelUpOverlay
import com.fairydoo.game.ui.components.PowerUpBar
import com.fairydoo.game.ui.components.SoundMenuButton
import com.fairydoo.game.ui.components.SoundSettingsOverlay
import com.fairydoo.game.ui.theme.BlossomPink
import com.fairydoo.game.ui.theme.DangerPink
import com.fairydoo.game.ui.theme.GlowBlue
import com.fairydoo.game.ui.theme.GlowPink
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldDark
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.GoldPale
import com.fairydoo.game.ui.theme.LeafGreen
import com.fairydoo.game.ui.theme.NightBottom
import com.fairydoo.game.ui.theme.NightHalo
import com.fairydoo.game.ui.theme.NightMiddle
import com.fairydoo.game.ui.theme.NightTop
import com.fairydoo.game.ui.theme.PanelBottom
import com.fairydoo.game.ui.theme.PanelTop
import com.fairydoo.game.ui.theme.StatusPurple
import com.fairydoo.game.ui.theme.TextPrimary

/** Breitengrenze des Spielbretts, entspricht den 352 px der Vorlage. */
private val BOARD_MAX_WIDTH = 352.dp

@Composable
fun GameScreen(preferences: GamePreferencesRepository) {
    val viewModel: GameViewModel = viewModel(
        factory = remember(preferences) { GameViewModel.factory(preferences) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isPreparing by viewModel.isPreparing.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    // Erstes Rätsel erzeugen — nur einmal, nicht bei jedem Recompose.
    LaunchedEffect(Unit) {
        if (state.puzzle == null) viewModel.startNewGame()
    }

    // Die Klangwelt lebt so lange wie der Bildschirm; beim Verlassen wird sie
    // freigegeben, sonst liefen Musikspur und Sprachausgabe weiter.
    val context = LocalContext.current
    val audio = remember(context) { FairyAudio(context) }
    DisposableEffect(audio) {
        onDispose { audio.release() }
    }

    // Lautstärken durchreichen, sobald sie sich ändern.
    LaunchedEffect(profile.musicVolume, profile.soundVolume, profile.voiceVolume) {
        audio.setMusicVolume(profile.musicVolume)
        audio.setSoundVolume(profile.soundVolume)
        audio.setVoiceVolume(profile.voiceVolume)
    }

    // Spielgeschehen hörbar machen. Level und Punktestand gehen mit, damit die
    // Feenstimme sie im Lob nennen kann.
    LaunchedEffect(audio) {
        viewModel.soundEvents.collect { event ->
            val current = viewModel.state.value
            audio.play(event, level = current.level, score = current.score)
        }
    }

    // Wandert die App in den Hintergrund, wird pausiert statt weitergespielt.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, audio) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    viewModel.pause()
                    audio.pause()
                }
                Lifecycle.Event.ON_START -> {
                    viewModel.resume()
                    audio.resume()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Beim Öffnen der Klang-Einstellungen pausiert die Uhr: Wer die Lautstärke
    // sucht, soll dafür keine Zeit verlieren.
    var showSoundSettings by rememberSaveable { mutableStateOf(false) }

    GameContent(
        state = state,
        isPreparing = isPreparing,
        bestScore = profile.highScore,
        profile = profile,
        showSoundSettings = showSoundSettings,
        onTapCell = { viewModel.onInput(GameInput.TapCell(it)) },
        onUsePowerUp = { viewModel.onInput(GameInput.UsePowerUp(it)) },
        onBegin = { viewModel.onInput(GameInput.Begin) },
        onNextLevel = { viewModel.onInput(GameInput.NextLevel) },
        onRestart = viewModel::restart,
        onOpenSoundSettings = {
            viewModel.pause()
            showSoundSettings = true
        },
        onCloseSoundSettings = {
            showSoundSettings = false
            viewModel.resume()
        },
        onMusicChange = viewModel::setMusicVolume,
        onSoundChange = viewModel::setSoundVolume,
        onVoiceChange = viewModel::setVoiceVolume,
    )
}

@Composable
private fun GameContent(
    state: GameState,
    isPreparing: Boolean,
    bestScore: Int,
    profile: PlayerProfile,
    showSoundSettings: Boolean,
    onTapCell: (Pos) -> Unit,
    onUsePowerUp: (PowerUp) -> Unit,
    onBegin: () -> Unit,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
    onOpenSoundSettings: () -> Unit,
    onCloseSoundSettings: () -> Unit,
    onMusicChange: (Float) -> Unit,
    onSoundChange: (Float) -> Unit,
    onVoiceChange: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Vier Schichten wie in der Vorlage: erst der Grundverlauf von
                // oben nach unten, darüber der helle Halo am oberen Rand und
                // die beiden farbigen Schimmer unten links und rechts.
                drawRect(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to NightTop,
                            0.45f to NightMiddle,
                            1f to NightBottom,
                        ),
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(NightHalo, Color.Transparent),
                        center = Offset(size.width * 0.5f, -size.height * 0.1f),
                        radius = size.height * 0.7f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(GlowPink, Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height),
                        radius = size.width * 0.8f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(GlowBlue, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.95f),
                        radius = size.width * 0.7f,
                    ),
                )
            },
    ) {
        FireflyLayer()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Wie in der Vorlage ein kompakter Stapel mit 12 dp Abstand — auf
            // hohen Displays mittig statt am oberen Rand klebend.
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            TitleRow()

            ScoreRow(score = state.score, level = state.level)

            LevelProgress(state = state)

            StatusRow(state = state)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (isPreparing || state.puzzle == null) {
                    CircularProgressIndicator(color = Gold)
                } else {
                    BoxWithConstraints(
                        // Die Vorlage deckelt das Brett bei 352 px; auf breiteren
                        // Displays soll es nicht mitwachsen, sonst werden die
                        // Felder unhandlich groß.
                        modifier = Modifier.widthIn(max = BOARD_MAX_WIDTH),
                    ) {
                        // Zellgröße abgerundet, damit das Gitter exakt aufgeht
                        // und rechts kein halbes Feld übrig bleibt.
                        val available = maxWidth - 8.dp
                        val cell = (available.value / state.boardSize).toInt().dp

                        FairydokuBoard(
                            state = state,
                            cellSize = cell,
                            onTapCell = onTapCell,
                        )
                    }
                }
            }

            Text(
                text = GameCopy.statusText(state.statusMessage),
                style = MaterialTheme.typography.bodyMedium,
                color = StatusPurple,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 20.dp),
            )

            PowerUpBar(state = state, onUse = onUsePowerUp)
        }

        SoundMenuButton(
            anythingAudible = profile.musicEnabled || profile.soundEnabled || profile.voiceEnabled,
            onClick = onOpenSoundSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(end = 6.dp, top = 2.dp),
        )

        when (state.status) {
            GameStatus.Intro -> IntroOverlay(bestScore = bestScore, onStart = onBegin)

            GameStatus.LevelComplete -> LevelUpOverlay(
                gained = state.gained,
                teaser = GameCopy.nextLevelTeaser(
                    nextSize = GameState.sizeForLevel(state.level + 1),
                    // Wer im nächsten Level dazukommt: die Feen des neuen
                    // Bretts ohne die des jetzigen.
                    newcomers = GameState.speciesOnBoard(state.level + 1) -
                        GameState.speciesOnBoard(state.level).toSet(),
                ),
                onContinue = onNextLevel,
            )

            GameStatus.GameOver -> GameOverOverlay(
                reason = GameCopy.gameOverReason(state.overReason),
                score = state.score,
                level = state.level,
                bestScore = bestScore,
                onRestart = onRestart,
            )

            else -> Unit
        }

        // Zuletzt und damit zuoberst: Die Einstellungen sollen auch über einem
        // Pausen- oder Ergebnis-Overlay erreichbar bleiben.
        if (showSoundSettings) {
            SoundSettingsOverlay(
                musicVolume = profile.musicVolume,
                soundVolume = profile.soundVolume,
                voiceVolume = profile.voiceVolume,
                onMusicChange = onMusicChange,
                onSoundChange = onSoundChange,
                onVoiceChange = onVoiceChange,
                onClose = onCloseSoundSettings,
            )
        }
    }
}

/** „Fairydoku" zwischen zwei schwebenden Feen. */
@Composable
private fun TitleRow() {
    val transition = rememberInfiniteTransition(label = "floaty")

    // Beide Feen schweben im selben Takt, die rechte um 1,2 s versetzt.
    val leftOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatLeft",
    )
    val rightOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1200),
        ),
        label = "floatRight",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "🧚‍♀️",
            fontSize = 30.sp,
            modifier = Modifier.graphicsLayer { translationY = leftOffset },
        )
        Text(
            text = "Fairydoku",
            // Gold-Verlauf im Text — in der Vorlage per background-clip,
            // in Compose als Brush im TextStyle.
            style = MaterialTheme.typography.displayLarge.copy(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to GoldPale,
                        0.55f to Gold,
                        1f to GoldDark,
                    ),
                ),
            ),
            maxLines = 1,
        )
        Text(
            text = "🧚",
            fontSize = 30.sp,
            modifier = Modifier.graphicsLayer { translationY = rightOffset },
        )
    }
}

/** SCORE- und Level-Pille. */
@Composable
private fun ScoreRow(score: Int, level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Pill(
            text = "SCORE: $score",
            borderColor = Gold.copy(alpha = 0.5f),
            fontSize = 15.sp,
            horizontalPadding = 18.dp,
        )
        Pill(
            text = "Level $level",
            borderColor = LeafGreen.copy(alpha = 0.4f),
            fontSize = 14.sp,
            horizontalPadding = 14.dp,
        )
    }
}

@Composable
private fun Pill(
    text: String,
    borderColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = horizontalPadding, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontSize = fontSize,
            color = TextPrimary,
        )
    }
}

/** Goldbalken mit „3 / 5 Wasserfeen platziert". */
@Composable
private fun LevelProgress(state: GameState) {
    val progress by animateFloatAsState(
        targetValue = state.levelProgress,
        animationSpec = tween(300),
        label = "levelProgress",
    )

    Column(
        modifier = Modifier.width(250.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Gold, GoldLight))),
            )
        }

        Text(
            // Ohne Artnamen: In jeder Zone lebt inzwischen eine andere Fee,
            // eine gemeinsame Bezeichnung gibt es nicht mehr.
            text = GameCopy.progressText(state.placedFairies, state.boardSize),
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary.copy(alpha = 0.75f),
        )
    }
}

/** Leben, Uhr und Schild-Hinweis. */
@Composable
private fun StatusRow(state: GameState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "🍃".repeat(state.lives) +
                "🥀".repeat((GameState.MAX_LIVES - state.lives).coerceAtLeast(0)),
            fontSize = 15.sp,
        )

        Text(
            text = (if (state.timeFrozen) "🌸 " else "⏳ ") +
                GameCopy.formatTime(state.remainingSeconds),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 15.sp,
            color = when {
                state.timeFrozen -> BlossomPink
                state.remainingSeconds < 20 -> DangerPink
                else -> TextPrimary
            },
        )

        if (state.shieldActive) {
            Text(
                text = "🍃 Schild aktiv",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 13.sp,
                color = LeafGreen,
            )
        }
    }
}
