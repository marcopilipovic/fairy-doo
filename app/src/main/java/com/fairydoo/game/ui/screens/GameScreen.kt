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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import com.fairydoo.game.ads.RewardedAdManager
import com.fairydoo.game.audio.FairyAudio
import com.fairydoo.game.audio.MusicTrack
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.data.PlayerProfile
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameInput
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.GameStatus
import com.fairydoo.game.game.GameViewModel
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.ui.GameCopy
import com.fairydoo.game.ui.components.BoardFrameInsets
import com.fairydoo.game.ui.components.FairydokuBoard
import com.fairydoo.game.ui.components.FireflyLayer
import com.fairydoo.game.ui.components.GameOverOverlay
import com.fairydoo.game.ui.components.GiftKind
import com.fairydoo.game.ui.components.GiftOverlay
import com.fairydoo.game.ui.components.IntroOverlay
import com.fairydoo.game.ui.components.LevelUpOverlay
import com.fairydoo.game.ui.components.PowerUpBar
import com.fairydoo.game.ui.components.SoundMenuButton
import com.fairydoo.game.ui.components.SoundSettingsOverlay
import com.fairydoo.game.ui.components.TutorialOverlay
import com.fairydoo.game.ui.sprites.FairyImage
import com.fairydoo.game.ui.sprites.fairyInlineContent
import com.fairydoo.game.ui.sprites.fairyText
import com.fairydoo.game.ui.theme.BlossomPink
import com.fairydoo.game.ui.theme.DangerPink
import com.fairydoo.game.ui.theme.GlowBlue
import com.fairydoo.game.ui.theme.GlowPink
import com.fairydoo.game.ui.theme.GlowTeal
import com.fairydoo.game.ui.theme.GlowViolet
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldDark
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.GoldPale
import com.fairydoo.game.ui.theme.LeafGreen
import com.fairydoo.game.ui.theme.NightBottom
import com.fairydoo.game.ui.theme.NightHalo
import com.fairydoo.game.ui.theme.NightDeep
import com.fairydoo.game.ui.theme.NightMiddle
import com.fairydoo.game.ui.theme.NightTop
import com.fairydoo.game.ui.theme.PanelBottom
import com.fairydoo.game.ui.theme.PanelTop
import com.fairydoo.game.ui.theme.PanelBorder
import com.fairydoo.game.ui.theme.PanelText
import com.fairydoo.game.ui.theme.StatusPurple
import com.fairydoo.game.ui.theme.TextPrimary
import com.fairydoo.game.ui.theme.TitleBottom
import com.fairydoo.game.ui.theme.TitleMiddle
import com.fairydoo.game.ui.theme.TitleTop

/** Breitengrenze des Spielbretts, entspricht den 352 px der Vorlage. */
private val BOARD_MAX_WIDTH = 352.dp

/**
 * Der nächtliche Wald-Hintergrund — vier Verlaufsschichten plus Glühwürmchen.
 *
 * Gemeinsames Gerüst für Spielbildschirm und Levelkarte, damit beide sich wie
 * derselbe Ort anfühlen statt wie zwei verschiedene Apps.
 */
@Composable
internal fun NightBackdrop(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Der „gemalte" Nachtwald: erst der Grundverlauf von tiefem
                // Petrol nach Violett, darüber der dunkle Bogen am oberen Rand
                // und vier farbige Schimmer, die den Wald von den Seiten her
                // erhellen. Die Reihenfolge ist wichtig — die Schimmer liegen
                // obenauf, sonst erstickt sie der Grundverlauf.
                drawRect(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to NightTop,
                            0.40f to NightMiddle,
                            0.75f to NightDeep,
                            1f to NightBottom,
                        ),
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(NightHalo, Color.Transparent),
                        center = Offset(size.width * 0.5f, -size.height * 0.15f),
                        radius = size.height * 0.75f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(GlowPink, Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.92f),
                        radius = size.width * 0.85f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(GlowViolet, Color.Transparent),
                        center = Offset(size.width * 0.90f, size.height * 0.88f),
                        radius = size.width * 0.80f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(GlowTeal, Color.Transparent),
                        center = Offset(size.width * 0.08f, size.height * 0.45f),
                        radius = size.width * 0.70f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(GlowBlue, Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.40f),
                        radius = size.width * 0.70f,
                    ),
                )
            },
    ) {
        GlowingMushrooms()
        FireflyLayer()
        content()
    }
}

/**
 * Drei große, weich leuchtende Pilze an den Rändern.
 *
 * Sie stehen weit außerhalb des Spielgeschehens und ragen nur zur Hälfte ins
 * Bild — dadurch wirkt der Bildschirm wie ein Ausschnitt aus einem größeren
 * Wald statt wie eine Bühne mit Kulisse.
 *
 * Als Emoji mit weichem Schein, wie in der Vorlage. Sie sind ausdrücklich als
 * Platzhalter gedacht: Sobald illustrierte Pilze vorliegen, wird nur diese
 * Funktion ersetzt.
 */
@Composable
private fun BoxScope.GlowingMushrooms() {
    @Composable
    fun mushroom(
        modifier: Modifier,
        size: androidx.compose.ui.unit.TextUnit,
        glow: Color,
        opacity: Float,
        mirrored: Boolean = false,
    ) {
        Text(
            text = "🍄",
            fontSize = size,
            modifier = modifier.graphicsLayer {
                alpha = opacity
                scaleX = if (mirrored) -1f else 1f
                // Der Schein liegt als weicher Farbfleck hinter dem Pilz; ein
                // echter Weichzeichner wäre auf jedem Bild neu zu berechnen und
                // steht in keinem Verhältnis zu einer Hintergrunddekoration.
                shadowElevation = 0f
            },
            color = Color.Unspecified,
            style = MaterialTheme.typography.bodyLarge.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = glow,
                    offset = Offset.Zero,
                    blurRadius = 40f,
                ),
            ),
        )
    }

    mushroom(
        modifier = Modifier.align(Alignment.BottomStart).offset(x = (-10).dp, y = 8.dp),
        size = 56.sp,
        glow = Color(0xE6FF82BE),
        opacity = 0.28f,
    )
    mushroom(
        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = 6.dp),
        size = 46.sp,
        glow = Color(0xE6BE82FF),
        opacity = 0.28f,
        mirrored = true,
    )
    mushroom(
        modifier = Modifier.align(Alignment.TopStart).offset(x = (-6).dp, y = 120.dp),
        size = 34.sp,
        glow = Color(0xCC50DCC8),
        opacity = 0.20f,
    )
}

@Composable
fun GameScreen(preferences: GamePreferencesRepository, ads: RewardedAdManager) {
    val viewModel: GameViewModel = viewModel(
        factory = remember(preferences) { GameViewModel.factory(preferences) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isPreparing by viewModel.isPreparing.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val globalLives by viewModel.globalLives.collectAsStateWithLifecycle()
    val fairyDust by viewModel.fairyDust.collectAsStateWithLifecycle()
    val irrlicht by viewModel.irrlicht.collectAsStateWithLifecycle()
    val adsUnlocked by viewModel.adsUnlocked.collectAsStateWithLifecycle()
    val adReady by ads.isReady.collectAsStateWithLifecycle()
    val showLevelSelect by viewModel.showLevelSelect.collectAsStateWithLifecycle()
    val tutorialOpen by viewModel.tutorialOpen.collectAsStateWithLifecycle()
    val tutorialStep by viewModel.tutorialStep.collectAsStateWithLifecycle()

    // Der Activity-Bezug wird erst hier gebraucht, direkt beim Zeigen der
    // Anzeige — der ViewModel bleibt dadurch Activity-unabhängig.
    val activity = LocalContext.current as Activity
    val onWatchAdForFairyDust = { ads.show(activity) { viewModel.grantFairyDust() } }
    val onWatchAdForIrrlicht = { ads.show(activity) { viewModel.grantIrrlicht() } }
    val onWatchAdForLife = { ads.show(activity) { viewModel.grantGlobalLife() } }

    // Vor Level 11 tritt an die Stelle der Werbung ein Geschenk-Popup, das
    // sofort auffüllt — welche Zauberhilfe bzw. welches Leben gerade dran ist,
    // hält dieser Zustand fest, solange das Popup offen ist. Das letzte
    // Geschenk ist an Level 10 geknüpft — beim Antippen im laufenden Spiel
    // zählt das gerade gespielte Level (auch beim Wiederholen älterer Level
    // bleibt so korrekt, dass nur Level 10 selbst als "letztes" gilt und
    // nicht schon jedes Level, sobald Level 10 insgesamt freigeschaltet ist);
    // auf der Levelkarte gibt es kein laufendes Level, dort zählt stattdessen
    // das höchste freigeschaltete Level, weil das dem nächsten Levelstart am
    // nächsten kommt.
    var giftKind by rememberSaveable { mutableStateOf<GiftKind?>(null) }
    var giftIsLast by rememberSaveable { mutableStateOf(false) }
    val onOpenGiftForFairyDust = { giftKind = GiftKind.FairyDust; giftIsLast = state.level == 10 }
    val onOpenGiftForIrrlicht = { giftKind = GiftKind.Irrlicht; giftIsLast = state.level == 10 }
    val onOpenGiftForLifeInGame = { giftKind = GiftKind.Life; giftIsLast = state.level == 10 }
    val onOpenGiftForLifeOnMap = {
        giftKind = GiftKind.Life
        giftIsLast = profile.highestLevelUnlocked == 10
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

    // Jeder Bildschirm hat sein Stück: der Wald trägt die Konzentration beim
    // Rätseln, der Feenpfad ist der Atemzug dazwischen. Vorher lief überall
    // dasselbe, und die Karte fühlte sich dadurch an wie eine Unterbrechung
    // des Spiels statt wie ein Teil davon.
    LaunchedEffect(showLevelSelect) {
        audio.setMusicTrack(if (showLevelSelect) MusicTrack.Path else MusicTrack.Forest)
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
                    // Steht die Levelkarte oder die Anleitung offen, bleibt
                    // das Spiel pausiert.
                    if (!showLevelSelect && !tutorialOpen) viewModel.resume()
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (showLevelSelect) {
            LevelSelectScreen(
                profile = profile,
                score = state.score,
                globalLives = globalLives,
                // Nur zurückkehrbar, wenn es überhaupt ein Spiel gibt, zu dem man
                // zurückkönnte — nicht beim allerersten Start der App.
                onClose = if (state.puzzle != null) viewModel::closeLevelSelect else null,
                onSelectLevel = viewModel::startLevel,
                onOpenTutorial = viewModel::openTutorial,
                onSetPlayerName = viewModel::setPlayerName,
                onSetAvatar = viewModel::setSelectedAvatar,
                onMusicChange = viewModel::setMusicVolume,
                onSoundChange = viewModel::setSoundVolume,
                onVoiceChange = viewModel::setVoiceVolume,
                adsUnlocked = adsUnlocked,
                adReady = adReady,
                onWatchAdForLife = onWatchAdForLife,
                onOpenGiftForLife = onOpenGiftForLifeOnMap,
            )
        } else {
            GameContent(
                state = state,
                isPreparing = isPreparing,
                bestScore = profile.highScore,
                profile = profile,
                showSoundSettings = showSoundSettings,
                onTapCell = { viewModel.onInput(GameInput.TapCell(it)) },
                onHoldCell = { viewModel.onInput(GameInput.HoldCell(it)) },
                onUseFairyDust = { viewModel.onInput(GameInput.UseFairyDust) },
                onUseIrrlicht = { viewModel.onInput(GameInput.UseIrrlicht) },
                onBegin = { viewModel.onInput(GameInput.Begin) },
                onNextLevel = { viewModel.onInput(GameInput.NextLevel) },
                onOpenLevelSelect = viewModel::openLevelSelect,
                onRetryLevel = viewModel::startLevel,
                globalLives = globalLives,
                // Der Countdown im Feenstaub-Knopf: 0, solange der Vorrat voll
                // ist — dann steht dort nichts zu warten.
                nextDustInMillis = (fairyDust.nextAtMillis - System.currentTimeMillis())
                    .coerceAtLeast(0L),
                nextIrrlichtInMillis = (irrlicht.nextAtMillis - System.currentTimeMillis())
                    .coerceAtLeast(0L),
                adsUnlocked = adsUnlocked,
                adReady = adReady,
                onWatchAdForFairyDust = onWatchAdForFairyDust,
                onWatchAdForIrrlicht = onWatchAdForIrrlicht,
                onWatchAdForLife = onWatchAdForLife,
                onOpenGiftForFairyDust = onOpenGiftForFairyDust,
                onOpenGiftForIrrlicht = onOpenGiftForIrrlicht,
                onOpenGiftForLife = onOpenGiftForLifeInGame,
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
                onOpenTutorial = viewModel::openTutorial,
            )
        }

        if (tutorialOpen) {
            TutorialOverlay(
                step = tutorialStep,
                totalSteps = GameViewModel.TUTORIAL_STEP_COUNT,
                onNext = viewModel::tutorialNext,
                onSkip = viewModel::skipTutorial,
            )
        }

        // Über beiden Bildschirmen (Levelkarte wie Spiel), damit ein leerer
        // Vorrat an beiden Stellen sofort per Geschenk aufgefüllt werden kann.
        giftKind?.let { kind ->
            GiftOverlay(
                kind = kind,
                isLastGift = giftIsLast,
                onAccept = {
                    when (kind) {
                        GiftKind.FairyDust -> viewModel.grantFairyDust()
                        GiftKind.Irrlicht -> viewModel.grantIrrlicht()
                        GiftKind.Life -> viewModel.grantGlobalLife()
                    }
                    giftKind = null
                },
            )
        }
    }
}

@Composable
private fun GameContent(
    state: GameState,
    isPreparing: Boolean,
    bestScore: Int,
    profile: PlayerProfile,
    showSoundSettings: Boolean,
    onTapCell: (Pos) -> Unit,
    onHoldCell: (Pos) -> Unit,
    onUseFairyDust: () -> Unit,
    onUseIrrlicht: () -> Unit,
    onBegin: () -> Unit,
    onNextLevel: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onRetryLevel: (Int) -> Unit,
    globalLives: com.fairydoo.game.game.GlobalLivesState,
    nextDustInMillis: Long,
    nextIrrlichtInMillis: Long,
    adsUnlocked: Boolean,
    adReady: Boolean,
    onWatchAdForFairyDust: () -> Unit,
    onWatchAdForIrrlicht: () -> Unit,
    onWatchAdForLife: () -> Unit,
    onOpenGiftForFairyDust: () -> Unit,
    onOpenGiftForIrrlicht: () -> Unit,
    onOpenGiftForLife: () -> Unit,
    onOpenSoundSettings: () -> Unit,
    onCloseSoundSettings: () -> Unit,
    onMusicChange: (Float) -> Unit,
    onSoundChange: (Float) -> Unit,
    onVoiceChange: (Float) -> Unit,
    onOpenTutorial: () -> Unit,
) {
    NightBackdrop {
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
                        // und rechts kein halbes Feld übrig bleibt. Was die
                        // Moos-Matte an Breite verbraucht, weiß das Brett
                        // selbst — hier wird es nur abgezogen.
                        val available = maxWidth - BoardFrameInsets
                        val cell = (available.value / state.boardSize).toInt().dp

                        FairydokuBoard(
                            state = state,
                            cellSize = cell,
                            onTapCell = onTapCell,
                            onHoldCell = onHoldCell,
                        )
                    }
                }
            }

            StatusMessageLine(text = GameCopy.statusText(state.statusMessage))

            PowerUpBar(
            state = state,
            nextDustInMillis = nextDustInMillis,
            nextIrrlichtInMillis = nextIrrlichtInMillis,
            onUseFairyDust = onUseFairyDust,
            onUseIrrlicht = onUseIrrlicht,
            adsUnlocked = adsUnlocked,
            adReady = adReady,
            onWatchAdForFairyDust = onWatchAdForFairyDust,
            onWatchAdForIrrlicht = onWatchAdForIrrlicht,
            onOpenGiftForFairyDust = onOpenGiftForFairyDust,
            onOpenGiftForIrrlicht = onOpenGiftForIrrlicht,
        )
        }

        // ❔ bleibt auf beiden Bildschirmen links — auf der Levelkarte steht
        // es dort schon. 🗺️ übernimmt rechts die Rolle, die dort 📜 auf der
        // Levelkarte hat.
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(start = 6.dp, top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HelpButton(onClick = onOpenTutorial)
            SoundMenuButton(
                anythingAudible = profile.musicEnabled || profile.soundEnabled || profile.voiceEnabled,
                onClick = onOpenSoundSettings,
            )
        }

        MapButton(
            onClick = onOpenLevelSelect,
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
                onShowLevelMap = onOpenLevelSelect,
            )

            GameStatus.GameOver -> GameOverOverlay(
                reason = GameCopy.gameOverReason(state.overReason),
                score = state.score,
                level = state.level,
                bestScore = bestScore,
                globalLives = globalLives,
                onRetry = { onRetryLevel(state.level) },
                onShowLevelMap = onOpenLevelSelect,
                adsUnlocked = adsUnlocked,
                adReady = adReady,
                onWatchAd = onWatchAdForLife,
                onOpenGift = onOpenGiftForLife,
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

/**
 * Die Rückmeldung unter dem Brett.
 *
 * Der Platz ist fest für zwei Zeilen reserviert, auch wenn nur eine gebraucht
 * wird. Die Meldungen sind unterschiedlich lang — der Zonenname mit ihrer
 * Bewohnerin braucht zwei Zeilen, ein kurzer Hinweis eine —, und ohne feste
 * Höhe verschöbe jeder Wechsel das ganze Spielfeld nach oben oder unten.
 * Beim Tippen auf ein Feld wäre das fatal: Man zielt auf ein Feld und trifft
 * ein anderes, weil das Brett zwischen Fingerbewegung und Berührung gesprungen
 * ist.
 *
 * Die Höhe leitet sich aus der Zeilenhöhe der Schrift ab statt aus einem festen
 * dp-Wert, damit sie bei vergrößerter Systemschrift mitwächst.
 */
@Composable
private fun StatusMessageLine(text: String) {
    val style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
    val lineHeight = if (style.lineHeight.isSpecified) style.lineHeight else style.fontSize * 1.4f
    // Der Zuschlag ist nicht Kosmetik: Ohne ihn ist die Fläche um Haaresbreite
    // zu klein für die zweite Zeile, und der Text wird stattdessen mitten im
    // Satz abgeschnitten — sichtbar erst bei vergrößerter Systemschrift.
    val height = with(LocalDensity.current) { (lineHeight * 2).toDp() + 6.dp }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = fairyText(text),
            // Etwas höher als die Schrift: Die Feen sind hochformatig und
            // schmal, auf reiner Zeilenhöhe verschwänden sie neben den
            // Buchstaben. Die eine Meldung mit Fee ist ein Einzeiler — die
            // zweite reservierte Zeile fängt den Zuschlag auf.
            inlineContent = fairyInlineContent(FairySpecies.Nebula, style.fontSize * 1.6f),
            style = style,
            color = StatusPurple,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Kleiner Rundknopf oben links — führt jederzeit zurück zur Levelkarte. */
@Composable
private fun MapButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, PanelBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Map,
            contentDescription = "Zur Karte",
            // Derselbe Parchment-Ton wie beim ❔ — beide Vollfarb-Emojis
            // wurden durch ein einfärbbares Icon ersetzt, der gleiche Ton
            // hält sie als zusammengehöriges Paar erkennbar.
            tint = Color(0xFFFFD8A1),
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * Rundknopf mit ❔ — öffnet die Anleitung von vorn.
 *
 * `internal`, damit die Levelkarte denselben Knopf verwenden kann: Die
 * Anleitung soll von beiden Orten aus gleich aussehen und gleich erreichbar
 * sein, nicht nur vom Spielbildschirm aus.
 */
@Composable
internal fun HelpButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, PanelBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.QuestionMark,
            contentDescription = "Anleitung",
            // Der gemessene Parchment-Ton der 📜-Emoji-Grafik daneben — so
            // liest sich das Fragezeichen als zugehörig statt als Fremdkörper.
            tint = Color(0xFFFFD8A1),
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * „Fairydoku" zwischen zwei schwebenden Feen.
 *
 * Zwei verschiedene Arten statt zweimal derselben: Nebula trägt den Nachthimmel
 * in den Flügeln und steht deshalb ruhig vor dem dunklen Kopfbereich, Nixies
 * Türkis setzt sich rechts davon ab. Zweimal dieselbe Figur sähe gespiegelt
 * aus, und gerade im Titel fällt das auf.
 */
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FairyImage(
            species = FairySpecies.Nebula,
            height = 40.dp,
            modifier = Modifier.graphicsLayer { translationY = leftOffset },
        )
        Text(text = "✦", fontSize = 16.sp, color = GoldLight)
        Text(
            text = "Fairydoku",
            // Creme-Gold-Verlauf im Text — in der Vorlage per background-clip,
            // in Compose als Brush im TextStyle.
            style = MaterialTheme.typography.displayLarge.copy(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to TitleTop,
                        0.45f to TitleMiddle,
                        1f to TitleBottom,
                    ),
                ),
            ),
            maxLines = 1,
        )
        Text(text = "✦", fontSize = 16.sp, color = GoldLight)
        FairyImage(
            species = FairySpecies.Nixie,
            height = 40.dp,
            modifier = Modifier.graphicsLayer { translationY = rightOffset },
        )
    }
}

/**
 * SCORE- und Level-Abzeichen.
 *
 * Abgerundete Rechtecke statt Pillen: Die Vorlage hat den Look gewechselt, weil
 * runde Pillen neben den eckigen Fähigkeitskacheln aus einem anderen Spiel
 * wirkten.
 */
@Composable
private fun ScoreRow(score: Int, level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Badge(
            text = "🌅 $score",
            borderColor = PanelBorder,
            textColor = PanelText,
            fontSize = 16.sp,
            letterSpacing = 1.5.sp,
            horizontalPadding = 22.dp,
        )
        Badge(
            text = "Level $level",
            borderColor = PanelBorder,
            textColor = GoldLight,
            fontSize = 15.sp,
            letterSpacing = 0.sp,
            horizontalPadding = 16.dp,
        )
    }
}

@Composable
private fun Badge(
    text: String,
    borderColor: Color,
    textColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, borderColor, shape)
            .drawBehind {
                // Der helle Streifen an der Oberkante — er lässt das Abzeichen
                // gewölbt statt aufgeklebt wirken.
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x26FFFFFF), Color.Transparent),
                        startY = 0f,
                        endY = 3.dp.toPx(),
                    ),
                    size = Size(size.width, 3.dp.toPx()),
                )
            }
            .padding(horizontal = horizontalPadding, vertical = 7.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontSize = fontSize,
            letterSpacing = letterSpacing,
            color = textColor,
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
            text = "⏳ " + GameCopy.formatTime(state.remainingSeconds),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 15.sp,
            color = if (state.remainingSeconds < 20) DangerPink else TextPrimary,
        )
    }
}
