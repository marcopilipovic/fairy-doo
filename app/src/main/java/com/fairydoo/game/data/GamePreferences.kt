package com.fairydoo.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fairy_doo")

/** Persistente Spielerdaten und Einstellungen. Überlebt App-Neustarts. */
data class PlayerProfile(
    val highScore: Int = 0,
    val gamesPlayed: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
)

/**
 * Einziger Zugriffspunkt auf gespeicherte Daten. DataStore statt SharedPreferences,
 * weil Schreibvorgänge asynchron laufen und den Spiel-Loop nicht blockieren.
 */
class GamePreferencesRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    val profile: Flow<PlayerProfile> = store.data.map { prefs ->
        PlayerProfile(
            highScore = prefs[KeyHighScore] ?: 0,
            gamesPlayed = prefs[KeyGamesPlayed] ?: 0,
            soundEnabled = prefs[KeySound] ?: true,
            hapticsEnabled = prefs[KeyHaptics] ?: true,
        )
    }

    /** Speichert das Ergebnis einer Partie. Der Highscore wird nur erhöht, nie gesenkt. */
    suspend fun recordFinishedGame(score: Int) {
        store.edit { prefs ->
            val previousBest = prefs[KeyHighScore] ?: 0
            if (score > previousBest) prefs[KeyHighScore] = score
            prefs[KeyGamesPlayed] = (prefs[KeyGamesPlayed] ?: 0) + 1
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        store.edit { it[KeySound] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        store.edit { it[KeyHaptics] = enabled }
    }

    /** Setzt Fortschritt und Einstellungen zurück. */
    suspend fun resetProgress() {
        store.edit { it.clear() }
    }

    private companion object {
        val KeyHighScore = intPreferencesKey("high_score")
        val KeyGamesPlayed = intPreferencesKey("games_played")
        val KeySound = booleanPreferencesKey("sound_enabled")
        val KeyHaptics = booleanPreferencesKey("haptics_enabled")
    }
}
