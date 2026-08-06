package com.fairydoo.game.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fairydoo.game.BuildConfig
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.ui.sprites.FairySpriteCache
import com.fairydoo.game.ui.theme.CardBottom
import com.fairydoo.game.ui.theme.CardTop
import com.fairydoo.game.ui.theme.Gold
import com.fairydoo.game.ui.theme.GoldLight
import com.fairydoo.game.ui.theme.PanelBorder
import com.fairydoo.game.ui.theme.StatusPurple
import com.fairydoo.game.ui.theme.TextOnGold
import com.fairydoo.game.ui.theme.TextPrimary

/**
 * Einstellungen — nur von der Levelkarte aus erreichbar (📜-Knopf).
 *
 * Bündelt Profil (Spielername + Fee-Avatar) und den Weg zum Sound-Regler.
 * Rechtliches steht bewusst *nicht* hier, sondern als eigene Fußzeile direkt
 * auf der Levelkarte — leichter erreichbar als hinter einem Menü. Kein
 * eigener Pausenmechanismus nötig: Die Levelkarte hat ohnehin keine laufende
 * Uhr, die geschützt werden müsste.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsOverlay(
    playerName: String,
    selectedAvatar: FairySpecies,
    onPlayerNameChange: (String) -> Unit,
    onAvatarSelected: (FairySpecies) -> Unit,
    onOpenSound: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080A1C).copy(alpha = 0.85f))
            .pointerInput(Unit) { detectTapGestures { onClose() } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 340.dp)
                .heightIn(max = 560.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(CardTop, CardBottom)),
                    shape = RoundedCornerShape(22.dp),
                )
                .border(1.5.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .pointerInput(Unit) { detectTapGestures { } }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Einstellungen",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 19.sp,
                color = Gold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            SectionLabel("Profil")

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Spielername",
                style = MaterialTheme.typography.labelSmall,
                color = StatusPurple,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))

            PlayerNameField(value = playerName, onValueChange = onPlayerNameChange)

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Wähle deine Fee",
                style = MaterialTheme.typography.labelSmall,
                color = StatusPurple,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FairySpecies.entries.forEach { species ->
                    AvatarSwatch(
                        species = species,
                        selected = species == selectedAvatar,
                        onClick = { onAvatarSelected(species) },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            MenuRow(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFFFFD8A1),
                        modifier = Modifier.size(18.dp),
                    )
                },
                label = "Sound",
                onClick = onOpenSound,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Fairydoku · Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = TextPrimary.copy(alpha = 0.4f),
            )

            Spacer(Modifier.height(14.dp))

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
                Text(text = "Zurück", style = MaterialTheme.typography.labelLarge, color = TextOnGold)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = TextPrimary.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MenuRow(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon()
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
    }
    Spacer(Modifier.height(6.dp))
}

/** Eingabefeld im dunklen Fassungs-Look der Vorlage — kein Material-Textfeld-Chrome. */
@Composable
private fun PlayerNameField(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
        cursorBrush = Brush.verticalGradient(listOf(Gold, Gold)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.32f))
            .border(1.5.dp, PanelBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = "z. B. Lichtfängerin",
                    color = TextPrimary.copy(alpha = 0.35f),
                    fontSize = 14.sp,
                )
            }
            inner()
        },
    )
}

/** Ein wählbarer Fee-Avatar — dieselbe Illustration wie auf dem Spielbrett, nur rund freigestellt. */
@Composable
private fun AvatarSwatch(species: FairySpecies, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(species) { FairySpriteCache.bitmapOf(context, species) }

    Image(
        bitmap = bitmap,
        contentDescription = species.displayName,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .border(
                width = if (selected) 3.dp else 2.dp,
                color = if (selected) GoldLight else Color.White.copy(alpha = 0.25f),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    )
}
