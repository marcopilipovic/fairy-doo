package com.fairydoo.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairydoo.game.ui.theme.CardBottom
import com.fairydoo.game.ui.theme.CardTop
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.TextOnGold
import com.fairydoo.game.ui.theme.TextPrimary
import kotlin.math.roundToInt

/**
 * Der Klang-Einstellungen im Spiel.
 *
 * Musik und Klänge lassen sich getrennt regeln, weil sie unterschiedlichen
 * Zwecken dienen: Die Musik läuft ununterbrochen und stört beim Nachdenken
 * schneller, die Klänge sind Rückmeldung auf eigene Züge und dürfen lauter
 * bleiben. Ein Regler auf null ist zugleich der Stummschalter — dafür braucht
 * es keinen zweiten Bedienweg.
 */
@Composable
fun SoundSettingsOverlay(
    musicVolume: Float,
    soundVolume: Float,
    voiceVolume: Float,
    onMusicChange: (Float) -> Unit,
    onSoundChange: (Float) -> Unit,
    onVoiceChange: (Float) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080A1C).copy(alpha = 0.82f))
            // Tippen neben der Karte schließt — der übliche Weg aus einem
            // Overlay, ohne dass ein Zug auf dem Brett darunter ausgelöst wird.
            .pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 340.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(CardTop, CardBottom)),
                    shape = RoundedCornerShape(22.dp),
                )
                .border(1.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .pointerInput(Unit) { detectTapGestures { } }
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Klang im Wald",
                style = MaterialTheme.typography.headlineMedium,
                color = Gold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(18.dp))

            VolumeRow(
                glyph = "🎵",
                label = "Musik",
                value = musicVolume,
                onChange = onMusicChange,
            )
            VolumeRow(
                glyph = "🔔",
                label = "Klänge",
                value = soundVolume,
                onChange = onSoundChange,
            )
            VolumeRow(
                glyph = "🗣",
                label = "Feenstimme",
                value = voiceVolume,
                onChange = onVoiceChange,
            )

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(listOf(GoldLight, Gold)),
                        shape = CircleShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose,
                    )
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Weiterspielen",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextOnGold,
                )
            }
        }
    }
}

@Composable
private fun VolumeRow(
    glyph: String,
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
) {
    val percent = (value * 100).roundToInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = glyph, fontSize = 15.sp, modifier = Modifier.size(22.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                )
            }
            Text(
                // „stumm" statt „0 %": Der Zustand ist wichtiger als die Zahl.
                text = if (percent == 0) "stumm" else "$percent %",
                style = MaterialTheme.typography.labelSmall,
                color = if (percent == 0) TextPrimary.copy(alpha = 0.5f) else Gold,
            )
        }

        Slider(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$label-Lautstärke" },
            colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = Gold,
                inactiveTrackColor = Color.White.copy(alpha = 0.18f),
            ),
        )
    }
}
