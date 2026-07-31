package com.fairydoo.game.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.data.PlayerProfile
import com.fairydoo.game.ui.theme.FairyDooTheme

@Composable
fun HomeScreen(
    preferences: GamePreferencesRepository,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
) {
    val profileFlow = remember(preferences) { preferences.profile }
    val profile by profileFlow.collectAsState(initial = PlayerProfile())

    HomeContent(profile = profile, onPlay = onPlay, onSettings = onSettings)
}

/**
 * Reiner Darstellungsteil ohne Datenquelle — dadurch in @Preview und in
 * UI-Tests direkt verwendbar. Dieses Muster (Screen holt Daten, Content stellt
 * dar) gilt für alle Screens im Projekt.
 */
@Composable
private fun HomeContent(
    profile: PlayerProfile,
    onPlay: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant)))
            .safeDrawingPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Fairy Doo",
                style = MaterialTheme.typography.displayLarge,
                color = colors.primary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Platzhalter-Startbildschirm — wird durch das finale Design ersetzt.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            StatRow(label = "Bestleistung", value = profile.highScore.toString())
            Spacer(Modifier.height(8.dp))
            StatRow(label = "Partien", value = profile.gamesPlayed.toString())

            Spacer(Modifier.height(48.dp))

            Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) {
                Text("Spielen", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Einstellungen", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    FairyDooTheme {
        HomeContent(
            profile = PlayerProfile(highScore = 1240, gamesPlayed = 17),
            onPlay = {},
            onSettings = {},
        )
    }
}
