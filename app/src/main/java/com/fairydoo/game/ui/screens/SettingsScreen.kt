package com.fairydoo.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.data.PlayerProfile
import com.fairydoo.game.ui.theme.FairyDooTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferences: GamePreferencesRepository,
    onBack: () -> Unit,
) {
    val profileFlow = remember(preferences) { preferences.profile }
    val profile by profileFlow.collectAsState(initial = PlayerProfile())
    val scope = rememberCoroutineScope()

    SettingsContent(
        profile = profile,
        onSoundChange = { scope.launch { preferences.setSoundEnabled(it) } },
        onHapticsChange = { scope.launch { preferences.setHapticsEnabled(it) } },
        onReset = { scope.launch { preferences.resetProgress() } },
        onBack = onBack,
    )
}

@Composable
private fun SettingsContent(
    profile: PlayerProfile,
    onSoundChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
    ) {
        Text(
            text = "Einstellungen",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(32.dp))

        ToggleRow(
            label = "Sound",
            checked = profile.soundEnabled,
            onCheckedChange = onSoundChange,
        )
        ToggleRow(
            label = "Vibration",
            checked = profile.hapticsEnabled,
            onCheckedChange = onHapticsChange,
        )

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onReset) {
            Text(
                text = "Fortschritt zurücksetzen",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Zurück", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    FairyDooTheme {
        SettingsContent(
            profile = PlayerProfile(soundEnabled = true, hapticsEnabled = false),
            onSoundChange = {}, onHapticsChange = {}, onReset = {}, onBack = {},
        )
    }
}
