package com.fairydoo.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import com.fairydoo.game.game.PowerUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Spielt die Klangwelt ab.
 *
 * Alle Klänge werden beim Start einmal berechnet (siehe [FairySounds]) und
 * danach nur noch abgespielt — Synthese während des Spiels würde beim Tippen
 * hörbar stocken.
 */
class FairyAudio(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val voice = FairyVoice(appContext)

    private var effects: Map<String, ShortArray> = emptyMap()
    private var music: AudioTrack? = null

    /**
     * Für die aufgenommenen Feenstimmen.
     *
     * SoundPool statt AudioTrack, weil es MP3 selbst dekodiert, die Clips im
     * Speicher hält und mehrere gleichzeitig mischen kann — beim schnellen
     * Setzen mehrerer Feen überlappen die Stimmen dadurch, statt sich
     * abzuschneiden.
     */
    private val clipPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_CLIP_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val giggleIds = mutableListOf<Int>()
    private var startledId = 0

    /** Geladene Clips; vorher abgespielt liefert SoundPool nur Stille. */
    private val loadedClips = mutableSetOf<Int>()

    /** Erst wenn die Klänge fertig berechnet sind, gibt es etwas zu hören. */
    @Volatile
    private var prepared = false

    @Volatile
    var soundEnabled: Boolean = true
        private set

    @Volatile
    var musicEnabled: Boolean = true
        private set

    @Volatile
    var voiceEnabled: Boolean = true
        private set

    init {
        loadClips()
        scope.launch { prepare() }
    }

    /** Lädt die aufgenommenen Stimmen; das Dekodieren übernimmt SoundPool. */
    private fun loadClips() {
        clipPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedClips += sampleId
            } else {
                Log.w(TAG, "Feenstimme $sampleId konnte nicht geladen werden (Status $status)")
            }
        }

        FairyClips.giggles.forEach { resId ->
            giggleIds += clipPool.load(appContext, resId, 1)
        }
        startledId = clipPool.load(appContext, FairyClips.startled, 1)
    }

    private suspend fun prepare() = withContext(Dispatchers.Default) {
        val built = buildMap {
            put(KEY_CHEER, Synth.toPcm16(FairySounds.cheer()))
            put(KEY_SPARKLE, Synth.toPcm16(FairySounds.sparkle()))
            put(KEY_SHIELD, Synth.toPcm16(FairySounds.shield()))
            put(KEY_FREEZE, Synth.toPcm16(FairySounds.timeFreeze()))
            put(KEY_TICK, Synth.toPcm16(FairySounds.tick()))
            put(KEY_UNDO, Synth.toPcm16(FairySounds.undo()))
            put(KEY_GAME_OVER, Synth.toPcm16(FairySounds.gameOver()))
        }
        effects = built
        prepared = true

        if (musicEnabled) startMusic()
    }

    /** Spielt, was das Spielgeschehen hergibt. */
    fun play(event: SoundEvent, level: Int = 1, score: Int = 0) {
        // Die Stimmen liegen im SoundPool und sind unabhängig von den
        // berechneten Klängen spielbereit — deshalb wird hier nicht auf
        // `prepared` gewartet.
        when (event) {
            is SoundEvent.FairyPlaced -> playClip(
                giggleIds.getOrNull(event.variant % giggleIds.size.coerceAtLeast(1)),
            )
            SoundEvent.FairyStartled -> playClip(startledId)
            else -> Unit
        }

        if (!prepared) return

        when (event) {
            is SoundEvent.FairyPlaced, SoundEvent.FairyStartled -> Unit
            SoundEvent.ShieldSaved -> playEffect(KEY_SHIELD)
            SoundEvent.Ward -> playEffect(KEY_TICK)
            SoundEvent.Undo -> playEffect(KEY_UNDO)

            is SoundEvent.PowerUpUsed -> playEffect(
                when (event.powerUp) {
                    PowerUp.FairyDust -> KEY_SPARKLE
                    PowerUp.NatureShield -> KEY_SHIELD
                    PowerUp.TimeBlossom -> KEY_FREEZE
                },
            )

            SoundEvent.LevelComplete -> {
                playEffect(KEY_CHEER)
                // Das Lob setzt erst ein, wenn der Jubel abgeklungen ist —
                // sonst reden Fanfare und Stimme durcheinander.
                if (voiceEnabled) {
                    scope.launch {
                        delay(PRAISE_DELAY_MILLIS)
                        voice.praise(level, score)
                    }
                }
            }

            SoundEvent.GameOver -> {
                // Kurz warten, damit der Aufschrei des letzten Fehlers steht.
                scope.launch {
                    delay(GAME_OVER_DELAY_MILLIS)
                    playEffect(KEY_GAME_OVER)
                }
            }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    fun setVoiceEnabled(enabled: Boolean) {
        voiceEnabled = enabled
        if (!enabled) voice.stop()
    }

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (enabled) {
            if (prepared) scope.launch { startMusic() }
        } else {
            stopMusic()
        }
    }

    /** Beim Verlassen des Spiels: Musik anhalten, Stimme verstummen lassen. */
    fun pause() {
        runCatching { music?.pause() }
        voice.stop()
    }

    fun resume() {
        if (musicEnabled && prepared) runCatching { music?.play() }
    }

    fun release() {
        stopMusic()
        runCatching { clipPool.release() }
        voice.release()
        scope.cancel()
    }

    /** Spielt eine aufgenommene Feenstimme. */
    private fun playClip(sampleId: Int?) {
        if (!soundEnabled) return
        if (sampleId == null || sampleId == 0) return
        // Ein noch nicht fertig dekodierter Clip würde stumm bleiben und den
        // Stream trotzdem belegen.
        if (sampleId !in loadedClips) return

        runCatching {
            clipPool.play(sampleId, CLIP_VOLUME, CLIP_VOLUME, 1, 0, 1f)
        }.onFailure { error ->
            Log.w(TAG, "Feenstimme $sampleId konnte nicht abgespielt werden", error)
        }
    }

    private fun playEffect(key: String) {
        if (!soundEnabled) return
        val samples = effects[key] ?: return

        scope.launch {
            runCatching {
                val track = createTrack(samples.size, looping = false)
                track.write(samples, 0, samples.size)
                track.play()

                // Erst nach dem Ausklingen freigeben, sonst bricht der Ton ab.
                val durationMillis = samples.size * 1000L / Synth.SAMPLE_RATE
                delay(durationMillis + TRACK_RELEASE_GRACE_MILLIS)
                track.stop()
                track.release()
            }.onFailure { error ->
                Log.w(TAG, "Klang $key konnte nicht abgespielt werden", error)
            }
        }
    }

    private suspend fun startMusic() {
        stopMusic()
        runCatching {
            val loop = withContext(Dispatchers.Default) {
                Synth.toPcm16(FairySounds.ambientLoop())
            }
            val track = createTrack(loop.size, looping = true)
            track.write(loop, 0, loop.size)
            // Der ganze Puffer ist die Schleife — dadurch läuft die Musik ohne
            // Lücke weiter, ohne dass jemand nachfüllen muss.
            track.setLoopPoints(0, loop.size, -1)
            track.setVolume(MUSIC_VOLUME)
            track.play()
            music = track
        }.onFailure { error ->
            Log.w(TAG, "Musik konnte nicht gestartet werden", error)
        }
    }

    private fun stopMusic() {
        runCatching {
            music?.stop()
            music?.release()
        }
        music = null
    }

    private fun createTrack(sampleCount: Int, looping: Boolean): AudioTrack {
        val sizeInBytes = sampleCount * BYTES_PER_SAMPLE

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(
                        if (looping) {
                            AudioAttributes.CONTENT_TYPE_MUSIC
                        } else {
                            AudioAttributes.CONTENT_TYPE_SONIFICATION
                        },
                    )
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(Synth.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            // STATIC statt STREAM: Der Puffer wird einmal gefüllt und kann
            // beliebig oft (bzw. in Schleife) abgespielt werden.
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(sizeInBytes)
            .build()
            .also { it.setVolume(if (looping) MUSIC_VOLUME else EFFECT_VOLUME) }
    }

    private companion object {
        const val TAG = "FairyAudio"
        const val BYTES_PER_SAMPLE = 2

        const val KEY_CHEER = "cheer"
        const val KEY_SPARKLE = "sparkle"
        const val KEY_SHIELD = "shield"
        const val KEY_FREEZE = "freeze"
        const val KEY_TICK = "tick"
        const val KEY_UNDO = "undo"
        const val KEY_GAME_OVER = "gameOver"

        const val MUSIC_VOLUME = 0.5f
        const val EFFECT_VOLUME = 0.9f
        const val CLIP_VOLUME = 1.0f

        /** Genug, damit sich mehrere Feenstimmen überlagern können. */
        const val MAX_CLIP_STREAMS = 8

        const val PRAISE_DELAY_MILLIS = 900L
        const val GAME_OVER_DELAY_MILLIS = 450L
        const val TRACK_RELEASE_GRACE_MILLIS = 120L
    }
}
