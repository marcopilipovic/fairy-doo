package com.fairydoo.game.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.fairydoo.game.data.PlayerProfile
import com.fairydoo.game.game.DailyScoreState
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GlobalLives
import com.fairydoo.game.game.GlobalLivesState
import com.fairydoo.game.ui.GameCopy
import com.fairydoo.game.ui.LegalPage
import com.fairydoo.game.ui.components.DailyScoreOverlay
import com.fairydoo.game.ui.components.FireflyLayer
import com.fairydoo.game.ui.components.GlowingMushroom
import com.fairydoo.game.ui.components.LegalOverlay
import com.fairydoo.game.ui.components.RockPile
import com.fairydoo.game.ui.components.TreeGroup
import com.fairydoo.game.ui.components.SettingsOverlay
import com.fairydoo.game.ui.components.SoundMenuButton
import com.fairydoo.game.ui.components.SoundSettingsOverlay
import com.fairydoo.game.ui.theme.DangerRose
import com.fairydoo.game.ui.theme.GoldCream
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.MossMatBorder
import com.fairydoo.game.ui.theme.MossMatBottom
import com.fairydoo.game.ui.theme.MossMatMiddle
import com.fairydoo.game.ui.theme.MossMatTop
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import com.fairydoo.game.ui.theme.PanelBorder
import com.fairydoo.game.ui.theme.PanelBottom
import com.fairydoo.game.ui.theme.PanelText
import com.fairydoo.game.ui.theme.PanelTop
import com.fairydoo.game.ui.theme.RegionColors
import com.fairydoo.game.ui.theme.StatusPurple
import com.fairydoo.game.ui.theme.StoneDark
import com.fairydoo.game.ui.theme.StoneLight
import com.fairydoo.game.ui.theme.TextPrimary
import com.fairydoo.game.ui.theme.TitleBottom
import com.fairydoo.game.ui.theme.TitleMiddle
import com.fairydoo.game.ui.theme.TitleTop
import kotlin.math.roundToInt
import kotlin.math.sin

/** Wie viele gesperrte Level als Vorschau hinter dem höchsten freigeschalteten stehen. */
private const val LOCKED_PREVIEW_COUNT = 6

/** Waagerechte Auslenkung des Pfads aus der Mitte. */
private val PATH_AMPLITUDE = 118.dp

/** Senkrechter Abstand zweier Level-Knoten. */
private val PATH_STEP = 92.dp

/** Durchmesser eines Knotens. */
private val NODE_SIZE = 54.dp

/**
 * Wie schnell die Sinuskurve schwingt.
 *
 * Der Wert stammt aus der Vorlage. Er ist bewusst kein glatter Bruchteil von
 * 2π: Sonst läge jeder dritte oder vierte Knoten wieder an derselben Stelle,
 * und aus dem Waldpfad würde eine Zickzacktreppe.
 */
private const val PATH_FREQUENCY = 1.05

/**
 * Der Feenpfad: die Levelkarte als Weg durch den Wald.
 *
 * Die Level liegen auf einer Sinuskurve untereinander, verbunden von einer
 * gepunkteten goldenen Spur. Ein Raster hätte dieselbe Information getragen,
 * aber ein Pfad erzählt, was das Spiel meint: dass man Schritt für Schritt
 * tiefer in den Wald geht.
 */
@Composable
fun LevelSelectScreen(
    profile: PlayerProfile,
    daily: DailyScoreState,
    globalLives: GlobalLivesState,
    onClose: (() -> Unit)?,
    onSelectLevel: (Int) -> Unit,
    onOpenTutorial: () -> Unit,
    onSetPlayerName: (String) -> Unit,
    onSetAvatar: (FairySpecies) -> Unit,
    onMusicChange: (Float) -> Unit,
    onSoundChange: (Float) -> Unit,
    onVoiceChange: (Float) -> Unit,
    adsUnlocked: Boolean,
    adReady: Boolean,
    onWatchAdForLife: () -> Unit,
    onOpenGiftForLife: () -> Unit,
    privacyOptionsRequired: Boolean,
    onOpenPrivacyOptions: () -> Unit,
) {
    // Rein lokale UI-Zustände: Die Levelkarte hat keine laufende Uhr, die ein
    // geöffnetes Overlay schützen müsste — anders als beim Tutorial gibt es
    // hier nichts zu pausieren.
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showSound by rememberSaveable { mutableStateOf(false) }
    var showDailyScore by rememberSaveable { mutableStateOf(false) }
    var legalPage by rememberSaveable { mutableStateOf<LegalPage?>(null) }

    NightBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ForestLivesBadge(
                state = globalLives,
                adsUnlocked = adsUnlocked,
                adReady = adReady,
                onWatchAd = onWatchAdForLife,
                onOpenGift = onOpenGiftForLife,
            )

            Spacer(Modifier.height(8.dp))

            // Auf der Karte steht der Tag, nicht der laufende Lauf: Der Lauf
            // gehört auf das Spielbrett, wo er entsteht. Drei Punktzahlen
            // nebeneinander läse ohnehin niemand mehr.
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScoreBadge(daily.points, onClick = { showDailyScore = true })
                // Nur sichtbar, sobald überhaupt ein Tag abgeschlossen wurde —
                // eine "Bestleistung: 0" wäre am ersten Tag nur Rauschen neben
                // dem echten Punktestand.
                if (daily.bestPoints > 0) {
                    BestScoreBadge(daily.bestPoints)
                }
            }

            Spacer(Modifier.height(6.dp))

            // Dieselbe Sprache wie "+💚 in 1:34" bei den Wald-Leben — der
            // Tageswechsel soll sich nicht wie ein neues System anfühlen.
            Text(
                text = "Neuer Tag in ${GameCopy.formatWaitTime(daily.remainingSeconds)}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = GoldLight.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "✦ Der Feenpfad ✦",
                style = MaterialTheme.typography.headlineSmall.copy(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to TitleTop,
                            0.45f to TitleMiddle,
                            1f to TitleBottom,
                        ),
                    ),
                ),
                fontSize = 19.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (globalLives.lives > 0) {
                    "Bereit für etwas Magie? Wähle deinen Pfad!"
                } else {
                    "Ohne Wald-Leben bleibt der Pfad verschlossen"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                color = if (globalLives.lives > 0) StatusPurple else DangerRose,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            ForestPath(
                highestLevelUnlocked = profile.highestLevelUnlocked,
                canPlay = globalLives.lives > 0,
                onSelectLevel = onSelectLevel,
                modifier = Modifier.weight(1f),
            )

            if (onClose != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Weiterspielen",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
                        .border(2.dp, PanelBorder, RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose,
                        )
                        .padding(horizontal = 22.dp, vertical = 8.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            LegalFooter(onOpenLegal = { legalPage = it })
        }

        // Dieselbe Zweiergruppe wie auf dem Spielbildschirm (❔ + 🎵) — der
        // Sound-Regler hing vorher hinter dem 📜-Menü, jetzt genauso direkt
        // erreichbar wie im Level.
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
                onClick = { showSound = true },
            )
        }

        SettingsButton(
            onClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(end = 6.dp, top = 2.dp),
        )

        if (showSettings) {
            SettingsOverlay(
                playerName = profile.playerName,
                selectedAvatar = profile.selectedAvatar,
                onPlayerNameChange = onSetPlayerName,
                onAvatarSelected = onSetAvatar,
                privacyOptionsRequired = privacyOptionsRequired,
                onOpenPrivacyOptions = onOpenPrivacyOptions,
                onClose = { showSettings = false },
            )
        }

        if (showSound) {
            SoundSettingsOverlay(
                musicVolume = profile.musicVolume,
                soundVolume = profile.soundVolume,
                voiceVolume = profile.voiceVolume,
                onMusicChange = onMusicChange,
                onSoundChange = onSoundChange,
                onVoiceChange = onVoiceChange,
                onClose = { showSound = false },
            )
        }

        if (showDailyScore) {
            DailyScoreOverlay(daily = daily, onClose = { showDailyScore = false })
        }

        legalPage?.let { page ->
            LegalOverlay(page = page, onClose = { legalPage = null })
        }
    }
}

/**
 * Der scrollbare Waldpfad in seiner Moos-Fassung.
 *
 * Fassung und Grund sind dieselben wie beim Spielbrett — die Karte soll sich
 * anfühlen wie derselbe Ort, nur von weiter oben gesehen.
 */
@Composable
private fun ForestPath(
    highestLevelUnlocked: Int,
    canPlay: Boolean,
    onSelectLevel: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val levelCount = highestLevelUnlocked + LOCKED_PREVIEW_COUNT
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
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
            }
            .border(2.dp, MossMatBorder, RoundedCornerShape(26.dp))
            .padding(10.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                // Reine Sicherheitsfüllung, bevor die Twilight-Ebenen unten
                // zeichnen — verhindert nur ein Aufblitzen des Fassungsgrüns
                // beim allerersten Frame, trägt sonst keine eigene Gestaltung
                // mehr (das übernimmt jetzt komplett [TwilightScenery]).
                .drawBehind { drawRect(color = Color(0xFF0D241C)) },
        ) {
            // Die Karte öffnet auf dem höchsten freigeschalteten Level statt
            // ganz oben. Wer sie aufschlägt, will dort weiter, wo er steht —
            // bei Level dreißig wären das sonst dreißig Wischer nach unten.
            //
            // Der Startwert steht schon vor dem ersten Bildpunkt fest, denn
            // BoxWithConstraints kennt die sichtbare Höhe bereits beim Aufbau.
            // Nachträglich zu scrollen würde die Karte einen Bildlauf lang ganz
            // oben zeigen und dann sichtbar springen.
            //
            // Einziger Schlüssel ist das Level, nicht die sichtbare Höhe: Die
            // ändert sich im Betrieb, sobald der Countdown im Kopf verschwindet,
            // weil die Wald-Leben wieder voll sind. Stünde sie mit im Schlüssel,
            // risse es den Spieler in diesem Moment aus seiner Scrollposition
            // zurück in die Mitte.
            //
            // Beim Schließen verlässt der Bildschirm die Komposition — das
            // nächste Öffnen fängt also wieder beim aktuellen Level an.
            val scrollState = remember(highestLevelUnlocked) {
                ScrollState(
                    with(density) {
                        LevelPathLayout.scrollToCenter(
                            level = highestLevelUnlocked,
                            viewportHeight = maxHeight.toPx(),
                            stepHeight = PATH_STEP.toPx(),
                            nodeSize = NODE_SIZE.toPx(),
                        )
                    }.roundToInt(),
                )
            }

            val pathHeight = PATH_STEP * (levelCount - 1) + NODE_SIZE * 2
            val maxScrollPx = with(density) {
                (pathHeight.toPx() - maxHeight.toPx()).coerceAtLeast(0f)
            }
            val laneWidth = maxWidth
            // In "dp-Einheiten" (nicht Pixel!), passend zur Canvas-Skalierung
            // in [TwilightScenery]/[TwilightGlowLayer] — die zeichnen ihre
            // Design-Werte in derselben Einheit und skalieren erst beim
            // Zeichnen auf echte Bildschirm-Pixel hoch.
            val nodeCenters = remember(levelCount, laneWidth) {
                (1..levelCount).map { level ->
                    val cx = laneWidth.value / 2f +
                        PATH_AMPLITUDE.value * sin(level * PATH_FREQUENCY).toFloat()
                    val cy = PATH_STEP.value * (level - 1) + NODE_SIZE.value
                    Offset(cx, cy)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                // Die fünf Parallaxe-Ebenen der "Twilight"-Szenerie, nach der
                // Vorlage in Bilder/Fairydoku Levelkarte/. Deep/far/mid liegen
                // hinter dem Pfad, die Glühwürmchen-/Requisiten-Ebene (depth 0)
                // direkt darüber, der Vordergrund zuletzt darüber den Knoten.
                TwilightScenery(
                    canvasHeight = pathHeight,
                    scrollState = scrollState,
                    maxScrollPx = maxScrollPx,
                    laneWidth = laneWidth,
                )
                TwilightGlowLayer(
                    laneWidth = laneWidth,
                    canvasHeight = pathHeight,
                    nodeCenters = nodeCenters,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(pathHeight)
                        .drawBehind {
                            drawGoldenTrail(levelCount, density)
                        },
                ) {
                    for (level in 1..levelCount) {
                        val offsetX = PATH_AMPLITUDE * sin(level * PATH_FREQUENCY).toFloat()
                        val offsetY = PATH_STEP * (level - 1) + NODE_SIZE * 0.5f

                        LevelNode(
                            level = level,
                            // Das höchste freigeschaltete ist das nächste, das
                            // ansteht — es pulsiert. Ein laufendes Level bekommt
                            // keine eigene Kennzeichnung: Wer die Karte öffnet,
                            // sucht, wo es weitergeht, nicht wo er gerade steht.
                            completed = level < highestLevelUnlocked,
                            current = level == highestLevelUnlocked,
                            locked = level > highestLevelUnlocked,
                            canPlay = canPlay,
                            onClick = { onSelectLevel(level) },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(x = offsetX, y = offsetY),
                        )
                    }
                }

                TwilightForeground(
                    canvasHeight = pathHeight,
                    scrollState = scrollState,
                    maxScrollPx = maxScrollPx,
                    laneWidth = laneWidth,
                )
            }
        }

        // Dieselben Waldmotive wie im Hintergrund außen — Pilze, eine Fee,
        // Glitzer —, hier aber auf dem Moosgrund selbst statt nur drumherum.
        // Sonst bleibt der Pfad eine leere Fläche mit Punkten drauf statt ein
        // Stück desselben verzauberten Walds.
        PathDecorations()

        // Derselbe Glühwürmchen-Schleier wie im nächtlichen Wald außen herum,
        // hier aber vor dem Moosgrund statt dahinter: Sonst würde ihn der
        // undurchsichtige Verlauf der Karte komplett verdecken. Etwas
        // gedämpft, damit er die Knoten nicht überstrahlt.
        FireflyLayer(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.55f },
        )
    }
}

@Composable
private fun BoxScope.PathDecorations() {
    Text(
        text = "🍄",
        fontSize = 30.sp,
        color = Color.Unspecified,
        style = MaterialTheme.typography.bodyLarge.copy(
            shadow = Shadow(color = Color(0xE6FF82BE), offset = Offset.Zero, blurRadius = 30f),
        ),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = 8.dp, y = 2.dp)
            .graphicsLayer { alpha = 0.4f },
    )
    Text(
        text = "🍄",
        fontSize = 22.sp,
        color = Color.Unspecified,
        style = MaterialTheme.typography.bodyLarge.copy(
            shadow = Shadow(color = Color(0xCC50DCC8), offset = Offset.Zero, blurRadius = 24f),
        ),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = (-10).dp, y = 0.dp)
            .graphicsLayer {
                alpha = 0.35f
                scaleX = -1f
            },
    )
    // Die obere linke Ecke wirkte im Vergleich zu den anderen drei kahl —
    // dort stand bisher nur ein einzelnes Glitzern, kein Pilz.
    Text(
        text = "🍄",
        fontSize = 20.sp,
        color = Color.Unspecified,
        style = MaterialTheme.typography.bodyLarge.copy(
            shadow = Shadow(color = Color(0xCC86A6FF), offset = Offset.Zero, blurRadius = 22f),
        ),
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 10.dp, y = 6.dp)
            .graphicsLayer { alpha = 0.38f },
    )
    Text(
        text = "✦",
        fontSize = 13.sp,
        color = GoldLight.copy(alpha = 0.4f),
        modifier = Modifier.align(Alignment.TopStart).offset(x = 22.dp, y = 64.dp),
    )
    Text(
        text = "✦",
        fontSize = 9.sp,
        color = GoldLight.copy(alpha = 0.3f),
        modifier = Modifier.align(Alignment.TopStart).offset(x = 48.dp, y = 18.dp),
    )
    Text(
        text = "✦",
        fontSize = 11.sp,
        color = GoldLight.copy(alpha = 0.34f),
        modifier = Modifier.align(Alignment.TopStart).offset(x = 6.dp, y = 44.dp),
    )
    Text(
        text = "✦",
        fontSize = 10.sp,
        color = GoldLight.copy(alpha = 0.32f),
        modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-34).dp, y = (-30).dp),
    )
    Text(
        text = "✦",
        fontSize = 11.sp,
        color = GoldLight.copy(alpha = 0.35f),
        modifier = Modifier.align(Alignment.BottomStart).offset(x = 56.dp, y = (-48).dp),
    )
}

/**
 * Die gepunktete Spur zwischen den Knoten.
 *
 * Zwei Linien übereinander: erst ein breiter weicher Schein, darüber die feine
 * Punktreihe. Ohne den Schein wäre die Spur auf dem dunklen Waldgrund kaum zu
 * sehen; ohne die Punkte wäre sie ein gezogener Strich und kein Weg.
 */
private fun DrawScope.drawGoldenTrail(levelCount: Int, density: androidx.compose.ui.unit.Density) {
    if (levelCount < 2) return

    val amplitude = with(density) { PATH_AMPLITUDE.toPx() }
    val step = with(density) { PATH_STEP.toPx() }
    val startY = with(density) { NODE_SIZE.toPx() } * 0.5f
    val centerX = size.width / 2f

    val points = (1..levelCount).map { level ->
        Offset(
            x = centerX + amplitude * sin(level * PATH_FREQUENCY).toFloat(),
            y = startY + step * (level - 1),
        )
    }

    val glow = Stroke(width = with(density) { 11.dp.toPx() }, cap = StrokeCap.Round)
    val dots = Stroke(
        width = with(density) { 5.dp.toPx() },
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(with(density) { 1.dp.toPx() }, with(density) { 14.dp.toPx() }),
        ),
    )

    for (index in 0 until points.size - 1) {
        val from = points[index]
        val to = points[index + 1]
        drawLine(GoldLight.copy(alpha = 0.12f), from, to, glow.width, glow.cap)
        drawLine(
            color = GoldLight.copy(alpha = 0.35f),
            start = from,
            end = to,
            strokeWidth = dots.width,
            cap = dots.cap,
            pathEffect = dots.pathEffect,
        )
    }
}

/**
 * Ein Level-Knoten: eine runde Steinplatte im Pfad.
 *
 * Abgeschlossene tragen die Neonfarbe ihrer Zone — dadurch wird der Weg mit
 * jedem Level bunter, und man sieht auf einen Blick, wie weit man gekommen ist.
 * Das aktuelle pulsiert golden, gesperrte bleiben dunkler Stein mit Schloss.
 */
@Composable
private fun LevelNode(
    level: Int,
    completed: Boolean,
    current: Boolean,
    locked: Boolean,
    canPlay: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoneColor = RegionColors[(level - 1) % RegionColors.size]
    val neon = lerp(zoneColor, Color.White, 0.20f)

    val transition = rememberInfiniteTransition(label = "node")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "nodePulse",
    )

    Box(
        modifier = modifier
            .size(NODE_SIZE)
            .clip(CircleShape)
            .drawBehind {
                if (locked) {
                    // Dieselbe Steinplatten-Machart wie die freien Knoten
                    // (Lichtfleck inklusive), nur in gedämpften Tönen — sonst
                    // wirkt "gesperrt" wie ein Platzhalter statt wie derselbe
                    // Stein, nur noch unbetreten.
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFF3A443C),
                                0.78f to Color(0xFF222A25),
                                1f to Color(0xFF1B211D),
                            ),
                            center = Offset(size.width * 0.45f, size.height * 0.45f),
                            radius = size.maxDimension * 0.7f,
                        ),
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color.White.copy(alpha = 0.10f),
                                0.45f to Color.Transparent,
                            ),
                            center = Offset(size.width * 0.32f, size.height * 0.28f),
                            radius = size.minDimension * 0.5f,
                        ),
                    )
                    return@drawBehind
                }

                // Dieselbe Steinplatte wie auf dem Spielfeld — Lichtfleck,
                // Moosfleck, Grundton.
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to StoneLight,
                            0.78f to StoneDark,
                            1f to StoneDark,
                        ),
                        center = Offset(size.width * 0.45f, size.height * 0.5f),
                        radius = size.maxDimension * 0.7f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(0f to Color(0x8C3C5A2D), 0.7f to Color.Transparent),
                        center = Offset(size.width * 0.7f, size.height * 0.8f),
                        radius = size.minDimension * 0.45f,
                    ),
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color.White.copy(alpha = 0.18f),
                            0.45f to Color.Transparent,
                        ),
                        center = Offset(size.width * 0.32f, size.height * 0.28f),
                        radius = size.minDimension * 0.5f,
                    ),
                )

                // Der Schein nach innen, in der Farbe des Knotens.
                val inner = if (current) GoldLight else neon
                val strength = if (current) pulse else 0.55f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            1f to inner.copy(alpha = strength * 0.7f),
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension / 2f,
                    ),
                    radius = size.minDimension / 2f,
                )
            }
            .border(
                width = if (locked) 2.dp else 3.dp,
                color = when {
                    locked -> Color(0x2EC8DCC8)
                    current -> Color(0xFFFFFBE8)
                    else -> neon
                },
                shape = CircleShape,
            )
            .clickable(
                enabled = !locked && canPlay,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (locked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Gesperrt",
                tint = StoneLight,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 19.sp,
                color = GoldCream,
            )
        }
    }
}

/**
 * Die Wald-Leben im Kopf der Karte.
 *
 * Grüne Herzen für den Vorrat, schwarze für das Verbrauchte, und darunter die
 * Zeit bis zum nächsten. Der Countdown ist der Grund, warum die Anzeige oben
 * steht: Wer keine Leben mehr hat, soll nicht erst suchen müssen, wann es
 * weitergeht.
 */
@Composable
private fun ForestLivesBadge(
    state: GlobalLivesState,
    adsUnlocked: Boolean,
    adReady: Boolean,
    onWatchAd: () -> Unit,
    onOpenGift: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
                .border(2.dp, PanelBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Text(
                text = "💚".repeat(state.lives) +
                    "🖤".repeat((GlobalLives.MAX - state.lives).coerceAtLeast(0)),
                fontSize = 15.sp,
            )
        }

        if (state.lives == 0 && adsUnlocked) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (adReady) "📺 Werbung ansehen (+1 Leben)" else "Werbung lädt…",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                color = GoldLight,
                modifier = Modifier
                    .clickable(
                        enabled = adReady,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onWatchAd,
                    )
                    .padding(4.dp),
            )
        } else if (state.lives == 0) {
            // Vor Level 11 gibt es noch keine Werbung — ein leeres Wald-Leben
            // wartet nicht auf den Countdown, sondern lässt sich sofort per
            // Geschenk auffüllen.
            Spacer(Modifier.height(4.dp))
            Text(
                text = "🎁 Geschenk annehmen",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                color = GoldLight,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenGift,
                    )
                    .padding(4.dp),
            )
        } else if (state.lives < GlobalLives.MAX) {
            Spacer(Modifier.height(4.dp))
            val remainingSeconds = ((state.nextLifeAtMillis - System.currentTimeMillis())
                .coerceAtLeast(0L) / 1000L).toInt()
            Text(
                text = "+💚 in ${GameCopy.formatWaitTime(remainingSeconds)}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 12.sp,
                color = if (state.lives == 0) DangerRose else PanelText.copy(alpha = 0.8f),
            )
        }
    }
}

/**
 * Zeile mit den drei Pflichtseiten, klein und unauffällig am Fuß der
 * Levelkarte — nicht hinter den Einstellungen versteckt, damit das
 * Impressum "leicht erkennbar und unmittelbar erreichbar" bleibt (§ 5 TMG).
 */
@Composable
private fun LegalFooter(onOpenLegal: (LegalPage) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegalFooterLink("Impressum") { onOpenLegal(LegalPage.Impressum) }
        LegalFooterDot()
        LegalFooterLink("AGB") { onOpenLegal(LegalPage.Agb) }
        LegalFooterDot()
        LegalFooterLink("Datenschutz") { onOpenLegal(LegalPage.Datenschutz) }
    }
}

@Composable
private fun LegalFooterLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        color = StatusPurple,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(4.dp),
    )
}

@Composable
private fun LegalFooterDot() {
    Text(
        text = "·",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        color = StatusPurple.copy(alpha = 0.5f),
    )
}

/** Rundknopf mit 📜 — öffnet Profil und Sound. Nur auf der Levelkarte. */
@Composable
private fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
        Text(text = "📜", fontSize = 18.sp)
    }
}

/**
 * Die Tagespunkte — und zugleich der Weg in die Tageswertung.
 *
 * Antippbar statt eines eigenen Knopfes: Der Kopfbereich ist voll, und wer auf
 * seine Punkte tippt, will ohnehin wissen, was sie wert sind.
 */
@Composable
private fun ScoreBadge(score: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, PanelBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp, vertical = 6.dp),
    ) {
        Text(
            text = "🌅 $score",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 15.sp,
            letterSpacing = 1.2.sp,
            color = PanelText,
        )
    }
}

/**
 * Das beste Tagesergebnis — golden gerahmt statt neutral wie [ScoreBadge],
 * damit es auf den ersten Blick als Rekord erkennbar ist, nicht als zweiter
 * Punktestand.
 */
@Composable
private fun BestScoreBadge(bestScore: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, PanelBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = "🏆 $bestScore",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 15.sp,
            letterSpacing = 1.2.sp,
            color = GoldCream,
        )
    }
}
