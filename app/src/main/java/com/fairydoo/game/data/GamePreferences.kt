package com.fairydoo.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fairydoo.game.game.DailyReward
import com.fairydoo.game.game.DailyScore
import com.fairydoo.game.game.DailyScoring
import com.fairydoo.game.game.DailySettlement
import com.fairydoo.game.game.FairyDustSupply
import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GlobalLives
import com.fairydoo.game.game.IrrlichtSupply
import java.util.TimeZone
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
    /** Der Irrlicht-Vorrat — genauso übergreifend und nachwachsend. */
    val irrlicht: Int = IrrlichtSupply.max,
    val nextIrrlichtAtMillis: Long = 0L,
    /** Ob die Anleitung schon einmal zu Ende gesehen oder übersprungen wurde. */
    val hasSeenTutorial: Boolean = false,
    /** Wie die Spielerin in der Rangliste heißen möchte — leer, bis gesetzt. */
    val playerName: String = "",
    /** Die Fee, die als Avatar in Profil und Rangliste erscheint. */
    val selectedAvatar: FairySpecies = FairySpecies.Flora,

    /**
     * Roher Stand der Tageswertung, siehe [DailyScoring].
     *
     * Roh wie [globalLives]: Ob der Tag inzwischen gewechselt hat, entscheidet
     * erst der Abgleich mit der Uhr — nicht das, was zuletzt geschrieben wurde.
     */
    val dailyScore: DailyScore = DailyScore(),
    /** Ein abgerechneter Tag, der noch nicht angezeigt wurde. */
    val pendingSettlement: DailySettlement? = null,
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
            irrlicht = prefs[KeyIrrlicht] ?: IrrlichtSupply.max,
            nextIrrlichtAtMillis = prefs[KeyNextIrrlichtAt] ?: 0L,
            hasSeenTutorial = prefs[KeyTutorialSeen] ?: false,
            playerName = prefs[KeyPlayerName] ?: "",
            selectedAvatar = prefs[KeySelectedAvatar]
                ?.let { stored -> FairySpecies.entries.find { it.name == stored } }
                ?: FairySpecies.Flora,
            dailyScore = prefs.dailyScore(),
            pendingSettlement = prefs.pendingSettlement(),
        )
    }

    /**
     * Der gespeicherte Tagesstand.
     *
     * Bei einer frischen Installation gibt es noch keine Zyklus-Kennung. Dann
     * gilt der heutige Tag — sonst sähe der Stand aus wie ein uralter,
     * unabgerechneter Tag aus dem Jahr 1970.
     */
    private fun Preferences.dailyScore(): DailyScore {
        val current = currentCycleId()
        return DailyScore(
            cycleId = this[KeyDailyCycle] ?: current,
            points = this[KeyDailyPoints] ?: 0,
            bestPoints = this[KeyDailyBest] ?: 0,
            settledCycleId = this[KeyDailySettled] ?: (current - 1),
        )
    }

    /** Ein abgerechneter Tag, der noch auf sein Overlay wartet — oder keiner. */
    private fun Preferences.pendingSettlement(): DailySettlement? {
        val cycleId = this[KeyPendingCycle] ?: return null
        return DailySettlement(
            cycleId = cycleId,
            points = this[KeyPendingPoints] ?: 0,
            wasBest = this[KeyPendingWasBest] ?: false,
            reward = DailyReward(
                fairyDust = this[KeyPendingDust] ?: 0,
                irrlicht = this[KeyPendingIrrlicht] ?: 0,
            ),
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
     * Werbung angesehen — ein Wald-Leben extra, unabhängig vom natürlichen
     * Nachwachsen. Erreicht der Vorrat dadurch das Maximum, steht die
     * Nachwachs-Uhr wieder still wie bei einem ohnehin vollen Vorrat.
     */
    suspend fun grantGlobalLife() {
        store.edit { prefs ->
            val now = System.currentTimeMillis()
            val normalized = GlobalLives.normalize(
                storedLives = prefs[KeyGlobalLives] ?: GlobalLives.MAX,
                nextLifeAtMillis = prefs[KeyNextGlobalLifeAt] ?: 0L,
                nowMillis = now,
            )
            val granted = (normalized.lives + 1).coerceAtMost(GlobalLives.MAX)
            prefs[KeyGlobalLives] = granted
            prefs[KeyNextGlobalLifeAt] = if (granted >= GlobalLives.MAX) 0L else normalized.nextLifeAtMillis
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

    /** Werbung angesehen — ein Feenstaub extra, siehe [grantGlobalLife]. */
    suspend fun grantFairyDust() {
        store.edit { prefs ->
            val now = System.currentTimeMillis()
            val normalized = FairyDustSupply.normalize(
                storedAmount = prefs[KeyFairyDust] ?: FairyDustSupply.max,
                nextAtMillis = prefs[KeyNextFairyDustAt] ?: 0L,
                nowMillis = now,
            )
            val granted = (normalized.amount + 1).coerceAtMost(FairyDustSupply.max)
            prefs[KeyFairyDust] = granted
            prefs[KeyNextFairyDustAt] = if (granted >= FairyDustSupply.max) 0L else normalized.nextAtMillis
        }
    }

    /**
     * Irrlicht einsetzen — zieht eines vom Vorrat ab.
     *
     * Gleiches Vorgehen wie bei [consumeFairyDust], nur für den zweiten Vorrat.
     */
    suspend fun consumeIrrlicht() {
        store.edit { prefs ->
            val now = System.currentTimeMillis()
            val normalized = IrrlichtSupply.normalize(
                storedAmount = prefs[KeyIrrlicht] ?: IrrlichtSupply.max,
                nextAtMillis = prefs[KeyNextIrrlichtAt] ?: 0L,
                nowMillis = now,
            )
            val consumed = IrrlichtSupply.consume(normalized, now)
            prefs[KeyIrrlicht] = consumed.amount
            prefs[KeyNextIrrlichtAt] = consumed.nextAtMillis
        }
    }

    /** Werbung angesehen — ein Irrlicht extra, siehe [grantGlobalLife]. */
    suspend fun grantIrrlicht() {
        store.edit { prefs ->
            val now = System.currentTimeMillis()
            val normalized = IrrlichtSupply.normalize(
                storedAmount = prefs[KeyIrrlicht] ?: IrrlichtSupply.max,
                nextAtMillis = prefs[KeyNextIrrlichtAt] ?: 0L,
                nowMillis = now,
            )
            val granted = (normalized.amount + 1).coerceAtMost(IrrlichtSupply.max)
            prefs[KeyIrrlicht] = granted
            prefs[KeyNextIrrlichtAt] = if (granted >= IrrlichtSupply.max) 0L else normalized.nextAtMillis
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

    /**
     * Gleicht die Tageswertung gegen die Uhr ab — beim App-Start und immer
     * dann, wenn die laufende Uhr einen Tageswechsel bemerkt.
     */
    suspend fun settleDailyCycle() {
        store.edit { it.settleDaily(pointsToAdd = 0) }
    }

    /**
     * Zählt die Punkte eines geschafften Levels zum heutigen Tag.
     *
     * Gutgeschrieben wird pro Level, nicht erst am Ende eines Laufs: Wer
     * mittendrin aufhört, soll das Gesammelte behalten. Sonst wäre die
     * Tageswertung eine Wette darauf, den Lauf auch zu Ende zu bringen.
     */
    suspend fun addDailyPoints(points: Int) {
        if (points <= 0) return
        store.edit { it.settleDaily(pointsToAdd = points) }
    }

    /** Das Abschluss-Overlay wurde weggetippt — der Tag ist damit erledigt. */
    suspend fun acknowledgeDailySettlement() {
        store.edit { prefs ->
            prefs.remove(KeyPendingCycle)
            prefs.remove(KeyPendingPoints)
            prefs.remove(KeyPendingWasBest)
            prefs.remove(KeyPendingDust)
            prefs.remove(KeyPendingIrrlicht)
        }
    }

    /**
     * Abrechnen, fortschreiben und gegebenenfalls belohnen — alles in einem
     * Schreibvorgang.
     *
     * Die Belohnung wird hier gutgeschrieben und nicht erst beim Wegtippen des
     * Overlays: Nur so kann sie nicht verloren gehen, wenn die App zwischen
     * Abrechnung und Anzeige beendet wird. Doppelt gutgeschrieben werden kann
     * sie ebenfalls nicht, weil im selben Zug `settledCycleId` vorrückt.
     */
    private fun MutablePreferences.settleDaily(pointsToAdd: Int) {
        val now = System.currentTimeMillis()
        val offset = zoneOffsetAt(now)
        val stored = dailyScore()

        val result = if (pointsToAdd > 0) {
            DailyScoring.add(stored, pointsToAdd, now, offset)
        } else {
            DailyScoring.settle(stored, now, offset)
        }

        this[KeyDailyCycle] = result.score.cycleId
        this[KeyDailyPoints] = result.score.points
        this[KeyDailyBest] = result.score.bestPoints
        this[KeyDailySettled] = result.score.settledCycleId

        result.settlement?.let { settlement ->
            this[KeyPendingCycle] = settlement.cycleId
            this[KeyPendingPoints] = settlement.points
            this[KeyPendingWasBest] = settlement.wasBest
            this[KeyPendingDust] = settlement.reward.fairyDust
            this[KeyPendingIrrlicht] = settlement.reward.irrlicht
            grantReward(settlement.reward, now)
        }
    }

    /**
     * Schreibt die Tagesbelohnung den beiden Vorräten gut.
     *
     * Anders als beim Belohnungsvideo **ohne** Deckelung auf die Obergrenze:
     * Über Nacht ist der Vorrat ohnehin voll nachgewachsen, ein gedeckeltes
     * Geschenk wäre also praktisch immer leer. Der Vorrat darf durch die
     * Tagesbelohnung über sein Maximum steigen und baut sich dann durchs
     * Spielen wieder ab — nachwachsen tut oberhalb nichts.
     */
    private fun MutablePreferences.grantReward(reward: DailyReward, now: Long) {
        if (reward.fairyDust > 0) {
            val normalized = FairyDustSupply.normalize(
                storedAmount = this[KeyFairyDust] ?: FairyDustSupply.max,
                nextAtMillis = this[KeyNextFairyDustAt] ?: 0L,
                nowMillis = now,
            )
            val granted = normalized.amount + reward.fairyDust
            this[KeyFairyDust] = granted
            this[KeyNextFairyDustAt] =
                if (granted >= FairyDustSupply.max) 0L else normalized.nextAtMillis
        }
        if (reward.irrlicht > 0) {
            val normalized = IrrlichtSupply.normalize(
                storedAmount = this[KeyIrrlicht] ?: IrrlichtSupply.max,
                nextAtMillis = this[KeyNextIrrlichtAt] ?: 0L,
                nowMillis = now,
            )
            val granted = normalized.amount + reward.irrlicht
            this[KeyIrrlicht] = granted
            this[KeyNextIrrlichtAt] =
                if (granted >= IrrlichtSupply.max) 0L else normalized.nextAtMillis
        }
    }

    private fun currentCycleId(): Long {
        val now = System.currentTimeMillis()
        return com.fairydoo.game.game.DailyCycle.cycleIdAt(now, zoneOffsetAt(now))
    }

    /** Der Versatz der Ortszeit gegenüber UTC — inklusive Sommerzeit. */
    private fun zoneOffsetAt(millis: Long): Long =
        TimeZone.getDefault().getOffset(millis).toLong()

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
        val KeyIrrlicht = intPreferencesKey("irrlicht")
        val KeyNextIrrlichtAt = longPreferencesKey("next_irrlicht_at")
        val KeyTutorialSeen = booleanPreferencesKey("tutorial_seen")
        val KeyPlayerName = stringPreferencesKey("player_name")
        val KeySelectedAvatar = stringPreferencesKey("selected_avatar")

        // Die Tageswertung. „daily_settled" ist der zuletzt abgerechnete Tag,
        // die „pending_"-Schlüssel halten einen abgerechneten Tag fest, dessen
        // Overlay noch aussteht.
        val KeyDailyCycle = longPreferencesKey("daily_cycle")
        val KeyDailyPoints = intPreferencesKey("daily_points")
        val KeyDailyBest = intPreferencesKey("daily_best")
        val KeyDailySettled = longPreferencesKey("daily_settled")
        val KeyPendingCycle = longPreferencesKey("pending_cycle")
        val KeyPendingPoints = intPreferencesKey("pending_points")
        val KeyPendingWasBest = booleanPreferencesKey("pending_was_best")
        val KeyPendingDust = intPreferencesKey("pending_dust")
        val KeyPendingIrrlicht = intPreferencesKey("pending_irrlicht")
    }
}
