package com.fairydoo.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fairydoo.game.game.GlobalLives
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fairy_doo")

/** Persistente Spielerdaten und Einstellungen. Überlebt App-Neustarts. */
data class PlayerProfile(
    val highScore: Int = 0,
    val gamesPlayed: Int = 0,
    /** Kichern, Aufschrei, Jubel und die übrigen Klänge. */
    val soundEnabled: Boolean = true,
    /** Der Ambient-Teppich im Hintergrund. */
    val musicEnabled: Boolean = true,
    /** Die lobende Feenstimme nach einem gelösten Rätsel. */
    val voiceEnabled: Boolean = true,
    /** Höchstes je erreichtes Level + 1 — der Fortschritt im Levelraster. */
    val highestLevelUnlocked: Int = 1,
    /** Roher Stand des App-weiten Lebenspools, siehe [GlobalLives]. */
    val globalLives: Int = GlobalLives.MAX,
    val nextGlobalLifeAtMillis: Long = 0L,
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
            musicEnabled = prefs[KeyMusic] ?: true,
            voiceEnabled = prefs[KeyVoice] ?: true,
            highestLevelUnlocked = prefs[KeyHighestLevel] ?: 1,
            globalLives = prefs[KeyGlobalLives] ?: GlobalLives.MAX,
            nextGlobalLifeAtMillis = prefs[KeyNextGlobalLifeAt] ?: 0L,
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

    /** Ein Level ist geschafft — schaltet das nächste dauerhaft frei, auch wenn spätere Versuche misslingen. */
    suspend fun recordLevelCompleted(level: Int) {
        store.edit { prefs ->
            val previousBest = prefs[KeyHighestLevel] ?: 1
            prefs[KeyHighestLevel] = maxOf(previousBest, level + 1)
        }
    }

    /** Ein Level ist verloren — kostet eins der fünf App-weiten Leben. */
    suspend fun consumeGlobalLife() {
        store.edit { prefs ->
            val now = System.currentTimeMillis()
            val normalized = GlobalLives.normalize(
                storedLives = prefs[KeyGlobalLives] ?: GlobalLives.MAX,
                nextLifeAtMillis = prefs[KeyNextGlobalLifeAt] ?: 0L,
                nowMillis = now,
            )
            val consumed = GlobalLives.consume(normalized, now)
            prefs[KeyGlobalLives] = consumed.lives
            prefs[KeyNextGlobalLifeAt] = consumed.nextLifeAtMillis
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        store.edit { it[KeySound] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        store.edit { it[KeyMusic] = enabled }
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        store.edit { it[KeyVoice] = enabled }
    }

    /** Setzt Fortschritt und Einstellungen zurück. */
    suspend fun resetProgress() {
        store.edit { it.clear() }
    }

    private companion object {
        val KeyHighScore = intPreferencesKey("high_score")
        val KeyGamesPlayed = intPreferencesKey("games_played")
        val KeySound = booleanPreferencesKey("sound_enabled")
        val KeyMusic = booleanPreferencesKey("music_enabled")
        val KeyVoice = booleanPreferencesKey("voice_enabled")
        val KeyHighestLevel = intPreferencesKey("highest_level_unlocked")
        val KeyGlobalLives = intPreferencesKey("global_lives")
        val KeyNextGlobalLifeAt = longPreferencesKey("next_global_life_at")
    }
}
