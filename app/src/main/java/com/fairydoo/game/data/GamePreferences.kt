package com.fairydoo.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fairydoo.game.game.FairyDustSupply
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GlobalLives
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fairy_doo")

/** Persistente Spielerdaten und Einstellungen. Überlebt App-Neustarts. */
data class PlayerProfile(
    val highScore: Int = 0,
    val gamesPlayed: Int = 0,
    /** Der Ambient-Teppich im Hintergrund, 0f..1f. */
    val musicVolume: Float = DEFAULT_MUSIC_VOLUME,
    /** Kichern, Aufschrei, Jubel und die übrigen Klänge, 0f..1f. */
    val soundVolume: Float = DEFAULT_SOUND_VOLUME,
    /** Die lobende Feenstimme nach einem gelösten Rätsel, 0f..1f. */
    val voiceVolume: Float = DEFAULT_VOICE_VOLUME,
    /** Höchstes je erreichtes Level + 1 — der Fortschritt im Levelraster. */
    val highestLevelUnlocked: Int = 1,
    /** Roher Stand des App-weiten Lebenspools, siehe [GlobalLives]. */
    val globalLives: Int = GlobalLives.MAX,
    val nextGlobalLifeAtMillis: Long = 0L,

    /** Der Feenstaub-Vorrat — wie die Wald-Leben übergreifend und nachwachsend. */
    val fairyDust: Int = FairyDustSupply.max,
    val nextFairyDustAtMillis: Long = 0L,
    /** Ob die Anleitung schon einmal zu Ende gesehen oder übersprungen wurde. */
    val hasSeenTutorial: Boolean = false,
    /** Wie die Spielerin in der Rangliste heißen möchte — leer, bis gesetzt. */
    val playerName: String = "",
    /** Die Fee, die als Avatar in Profil und Rangliste erscheint. */
    val selectedAvatar: FairySpecies = FairySpecies.Flora,
) {
    val musicEnabled: Boolean get() = musicVolume > 0f
    val soundEnabled: Boolean get() = soundVolume > 0f
    val voiceEnabled: Boolean get() = voiceVolume > 0f

    companion object {
        /**
         * Die Musik liegt bewusst unter den Klängen: Sie läuft ununterbrochen,
         * die Effekte sollen sich darüber behaupten.
         */
        const val DEFAULT_MUSIC_VOLUME = 0.7f
        const val DEFAULT_SOUND_VOLUME = 0.9f
        const val DEFAULT_VOICE_VOLUME = 1.0f
    }
}

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
            musicVolume = prefs.volume(
                KeyMusicVolume, KeyMusicOn, PlayerProfile.DEFAULT_MUSIC_VOLUME,
            ),
            soundVolume = prefs.volume(
                KeySoundVolume, KeySoundOn, PlayerProfile.DEFAULT_SOUND_VOLUME,
            ),
            voiceVolume = prefs.volume(
                KeyVoiceVolume, KeyVoiceOn, PlayerProfile.DEFAULT_VOICE_VOLUME,
            ),
            highestLevelUnlocked = prefs[KeyHighestLevel] ?: 1,
            globalLives = prefs[KeyGlobalLives] ?: GlobalLives.MAX,
            nextGlobalLifeAtMillis = prefs[KeyNextGlobalLifeAt] ?: 0L,
            fairyDust = prefs[KeyFairyDust] ?: FairyDustSupply.max,
            nextFairyDustAtMillis = prefs[KeyNextFairyDustAt] ?: 0L,
            hasSeenTutorial = prefs[KeyTutorialSeen] ?: false,
            playerName = prefs[KeyPlayerName] ?: "",
            selectedAvatar = prefs[KeySelectedAvatar]
                ?.let { stored -> FairySpecies.entries.find { it.name == stored } }
                ?: FairySpecies.Flora,
        )
    }

    /**
     * Liest eine Lautstärke und übernimmt dabei die frühere Ein/Aus-Einstellung.
     *
     * Vor den Reglern gab es nur Schalter. Wer den Ton damals abgeschaltet
     * hatte, soll ihn nach dem Update nicht plötzlich wieder hören — ein „aus"
     * wird deshalb zu Lautstärke null.
     */
    private fun Preferences.volume(
        volumeKey: Preferences.Key<Float>,
        legacySwitchKey: Preferences.Key<Boolean>,
        default: Float,
    ): Float {
        this[volumeKey]?.let { return it.coerceIn(0f, 1f) }
        return if (this[legacySwitchKey] == false) 0f else default
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

    /**
     * Feenstaub einsetzen — zieht eines vom Vorrat ab.
     *
     * Wie bei den Wald-Leben wird vor dem Abziehen nachgeholt, was inzwischen
     * gewachsen ist. Sonst verlöre man beim Verbrauchen genau das Stück, das in
     * derselben Sekunde fällig geworden wäre.
     */
    suspend fun consumeFairyDust() {
        store.edit { prefs ->
            val now = System.currentTimeMillis()
            val normalized = FairyDustSupply.normalize(
                storedAmount = prefs[KeyFairyDust] ?: FairyDustSupply.max,
                nextAtMillis = prefs[KeyNextFairyDustAt] ?: 0L,
                nowMillis = now,
            )
            val consumed = FairyDustSupply.consume(normalized, now)
            prefs[KeyFairyDust] = consumed.amount
            prefs[KeyNextFairyDustAt] = consumed.nextAtMillis
        }
    }

    suspend fun setMusicVolume(volume: Float) {
        store.edit { it[KeyMusicVolume] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setSoundVolume(volume: Float) {
        store.edit { it[KeySoundVolume] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setVoiceVolume(volume: Float) {
        store.edit { it[KeyVoiceVolume] = volume.coerceIn(0f, 1f) }
    }

    /** Anleitung zu Ende gesehen oder übersprungen — erscheint nicht mehr von selbst. */
    suspend fun markTutorialSeen() {
        store.edit { it[KeyTutorialSeen] = true }
    }

    suspend fun setPlayerName(name: String) {
        store.edit { it[KeyPlayerName] = name }
    }

    suspend fun setSelectedAvatar(species: FairySpecies) {
        store.edit { it[KeySelectedAvatar] = species.name }
    }

    /** Setzt Fortschritt und Einstellungen zurück. */
    suspend fun resetProgress() {
        store.edit { it.clear() }
    }

    private companion object {
        val KeyHighScore = intPreferencesKey("high_score")
        val KeyGamesPlayed = intPreferencesKey("games_played")
        val KeyMusicVolume = floatPreferencesKey("music_volume")
        val KeySoundVolume = floatPreferencesKey("sound_volume")
        val KeyVoiceVolume = floatPreferencesKey("voice_volume")

        // Die Schalter von früher — nur noch zum Übernehmen der alten Wahl.
        val KeyMusicOn = booleanPreferencesKey("music_enabled")
        val KeySoundOn = booleanPreferencesKey("sound_enabled")
        val KeyVoiceOn = booleanPreferencesKey("voice_enabled")

        val KeyHighestLevel = intPreferencesKey("highest_level_unlocked")
        val KeyGlobalLives = intPreferencesKey("global_lives")
        val KeyNextGlobalLifeAt = longPreferencesKey("next_global_life_at")
        val KeyFairyDust = intPreferencesKey("fairy_dust")
        val KeyNextFairyDustAt = longPreferencesKey("next_fairy_dust_at")
        val KeyTutorialSeen = booleanPreferencesKey("tutorial_seen")
        val KeyPlayerName = stringPreferencesKey("player_name")
        val KeySelectedAvatar = stringPreferencesKey("selected_avatar")
    }
}
