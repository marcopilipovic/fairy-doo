package com.fairydoo.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import com.fairydoo.game.R
import com.fairydoo.game.data.PlayerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.CRC32

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

    /** SoundPool-Kennungen der berechneten Klänge. */
    private var effects: Map<String, Int> = emptyMap()

    @Volatile
    private var music: AudioTrack? = null

    /** Läuft, solange die Musikspur aufgebaut wird. */
    private var musicJob: Job? = null

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

    // Stufenlos statt an/aus: Der Spieler stellt Musik und Klänge getrennt ein,
    // null bedeutet stumm.
    @Volatile
    private var musicVolume: Float = PlayerProfile.DEFAULT_MUSIC_VOLUME

    @Volatile
    private var soundVolume: Float = PlayerProfile.DEFAULT_SOUND_VOLUME

    @Volatile
    private var voiceVolume: Float = PlayerProfile.DEFAULT_VOICE_VOLUME

    private val musicEnabled: Boolean get() = musicVolume > 0f

    init {
        loadClips()
        // Effekte und Musik nebeneinander vorbereiten: Nacheinander summierten
        // sich Klangberechnung und Musik-Dekodierung, und bis beides fertig war,
        // blieb das Spiel stumm. Sie hängen nicht voneinander ab.
        scope.launch { prepare() }
        if (musicEnabled) musicJob = scope.launch { startMusic() }
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

    /**
     * Stellt die berechneten Klänge bereit.
     *
     * **Berechnet wird nur beim ersten Mal.** Danach liegen die Klänge als
     * Dateien im Cache und werden von dort geladen. Ohne das dauerte es auf
     * einem langsamen Gerät gut eine halbe Minute, bis Musik und Effekte
     * einsetzten — das Spiel wirkte in dieser Zeit schlicht stumm.
     *
     * Der Umweg über Dateien ist ohnehin nötig, weil SoundPool nur aus Dateien
     * oder Ressourcen lädt, nicht aus einem Speicherpuffer. Die Versionsnummer
     * im Ordnernamen sorgt dafür, dass eine geänderte Synthese nicht auf alten
     * Klängen sitzen bleibt.
     */
    private suspend fun prepare() = withContext(Dispatchers.Default) {
        val cacheDir = soundCacheDir()
        pruneOldCaches(cacheDir)

        // Effekte zuerst: Sie sind billiger als die Musikschleife, und ein
        // stummer Tastendruck fällt eher auf als fehlende Hintergrundmusik.
        val builders: Map<String, () -> FloatArray> = mapOf(
            KEY_TICK to FairySounds::tick,
            KEY_UNDO to FairySounds::undo,
            KEY_SPARKLE to FairySounds::sparkle,
            KEY_SHIELD to FairySounds::shield,
            KEY_FREEZE to FairySounds::timeFreeze,
            KEY_CHEER to FairySounds::cheer,
            KEY_GAME_OVER to FairySounds::gameOver,
        )

        effects = builders.mapNotNull { (key, build) ->
            runCatching {
                val file = File(cacheDir, "$key.wav")
                if (!file.exists() || file.length() == 0L) {
                    file.writeBytes(Synth.toWavBytes(build()))
                }
                key to clipPool.load(file.absolutePath, 1)
            }.onFailure { error ->
                Log.w(TAG, "Klang $key konnte nicht vorbereitet werden", error)
            }.getOrNull()
        }.toMap()

        prepared = true
    }

    /**
     * Die Musikschleife als rohe Abtastwerte im Zwischenspeicher.
     *
     * Die Datei wird einmal dekodiert und danach von hier geladen — das
     * Dekodieren einer Minute Musik kostet spürbar Zeit, und so lange bliebe
     * es still. Abgelegt wird rohes PCM, weil AudioTrack die Daten als
     * Zahlenfeld erwartet; ein Container brächte nur einen Kopfsatz zum
     * Überspringen.
     */
    private fun loadOrDecodeMusic(cacheDir: File): ShortArray {
        val file = File(cacheDir, "ambient-${musicFingerprint()}.pcm")
        // Was eine frühere Musik hinterlassen hat, wird jetzt nicht mehr
        // gefunden — aber es läge sonst für immer da und belegte Platz.
        runCatching {
            cacheDir.listFiles()
                ?.filter { it.name.startsWith("ambient") && it.name != file.name }
                ?.forEach { it.delete() }
        }

        if (file.exists() && file.length() > 0) {
            runCatching {
                val bytes = file.readBytes()
                return ShortArray(bytes.size / 2) { index ->
                    val low = bytes[index * 2].toInt() and 0xFF
                    val high = bytes[index * 2 + 1].toInt()
                    ((high shl 8) or low).toShort()
                }
            }.onFailure { error ->
                Log.w(TAG, "Musik aus dem Zwischenspeicher unbrauchbar", error)
            }
        }

        val decoded = MusicDecoder.decodeToMono(appContext, R.raw.ambient_forest)
        // Kurzes Überblenden von Schluss auf Anfang: MP3 trägt kodierungsbedingt
        // etwas Stille an den Rändern, die beim Wiederholen als Lücke hörbar
        // wäre.
        val samples = Synth.crossfadeLoop(decoded, seconds = LOOP_CROSSFADE_SECONDS)

        runCatching {
            val bytes = ByteArray(samples.size * 2)
            samples.forEachIndexed { index, value ->
                bytes[index * 2] = (value.toInt() and 0xFF).toByte()
                bytes[index * 2 + 1] = (value.toInt() shr 8).toByte()
            }
            file.writeBytes(bytes)
        }.onFailure { error ->
            Log.w(TAG, "Musik konnte nicht zwischengespeichert werden", error)
        }
        return samples
    }

    /**
     * Erkennungsmerkmal der Musikaufnahme.
     *
     * Der Zwischenspeicher hing zuvor allein an [SOUND_CACHE_VERSION]. Als die
     * berechnete Schleife durch die komponierte Aufnahme ersetzt wurde, blieb
     * diese Zahl unverändert — und jedes Gerät, auf dem das Spiel vorher
     * gelaufen war, spielte danach weiter die alte Schleife. Der Fehler war von
     * außen nicht zu erkennen: Der Ton war ja da, nur der falsche.
     *
     * Am Dateinamen hängt deshalb jetzt die Prüfsumme der Aufnahme selbst. Wird
     * die Musik ausgetauscht, ändert sich der Name von allein; niemand muss
     * mehr daran denken, eine Nummer hochzusetzen.
     *
     * Das Lesen der Datei kostet bei jedem Start ein paar Millisekunden — der
     * Preis dafür, dass ein Vergessen keine Folgen mehr hat.
     */
    private fun musicFingerprint(): Long = runCatching {
        val checksum = CRC32()
        appContext.resources.openRawResource(R.raw.ambient_forest).use { input ->
            val buffer = ByteArray(FINGERPRINT_BUFFER_BYTES)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                checksum.update(buffer, 0, read)
            }
        }
        checksum.value
    }.getOrElse { error ->
        Log.w(TAG, "Prüfsumme der Musik nicht ermittelbar", error)
        0L
    }

    /**
     * Entfernt die Klangordner früherer Fassungen.
     *
     * Jede Erhöhung von [SOUND_CACHE_VERSION] legt einen neuen Ordner an; die
     * alten blieben bisher liegen. Auf einem lange benutzten Gerät summierte
     * sich das auf etliche Megabyte, die nie wieder jemand anfasst.
     */
    private fun pruneOldCaches(current: File) {
        runCatching {
            appContext.cacheDir.listFiles()
                ?.filter { it.isDirectory && it.name.startsWith(CACHE_PREFIX) && it != current }
                ?.forEach { it.deleteRecursively() }
        }.onFailure { error ->
            Log.w(TAG, "Alte Klangordner nicht aufräumbar", error)
        }
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
            SoundEvent.Ward -> playEffect(KEY_TICK)
            SoundEvent.Undo -> playEffect(KEY_UNDO)
            SoundEvent.FairyDustUsed -> playEffect(KEY_SPARKLE)

            SoundEvent.LevelComplete -> {
                playEffect(KEY_CHEER)
                // Das Lob setzt erst ein, wenn der Jubel abgeklungen ist —
                // sonst reden Fanfare und Stimme durcheinander.
                val volume = voiceVolume
                if (volume > 0f) {
                    scope.launch {
                        delay(PRAISE_DELAY_MILLIS)
                        voice.praise(level, score, volume)
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

    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
    }

    fun setVoiceVolume(volume: Float) {
        voiceVolume = volume.coerceIn(0f, 1f)
        if (voiceVolume == 0f) voice.stop()
    }

    /**
     * Stellt die Musik ein — ohne sie neu zu beginnen.
     *
     * Ein laufender Ton lässt sich in der Lautstärke verändern; nur der
     * Übergang von stumm auf hörbar braucht einen Start. Andernfalls setzte die
     * Schleife bei jeder Reglerbewegung neu ein.
     *
     * Ein Reglerzug erzeugt Dutzende Aufrufe in schneller Folge, und der Start
     * dauert, weil erst die Abtastwerte geladen werden. Deshalb merkt sich
     * [musicJob], dass bereits gestartet wird: Ohne das setzten die
     * nachfolgenden Werte die Lautstärke auf einer Spur, die es noch gar nicht
     * gab — und die Musik blieb nach dem Hochziehen aus der Stille stumm.
     */
    fun setMusicVolume(volume: Float) {
        val next = volume.coerceIn(0f, 1f)
        musicVolume = next

        if (next == 0f) {
            musicJob?.cancel()
            musicJob = null
            stopMusic()
            return
        }

        val running = music
        if (running != null) {
            runCatching { running.setVolume(next) }
            return
        }

        if (musicJob?.isActive == true) return
        musicJob = scope.launch { startMusic() }
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
        val volume = soundVolume
        if (volume <= 0f) return
        if (sampleId == null || sampleId == 0) return
        // Ein noch nicht fertig dekodierter Clip würde stumm bleiben und den
        // Stream trotzdem belegen.
        if (sampleId !in loadedClips) return

        runCatching {
            clipPool.play(sampleId, volume, volume, 1, 0, 1f)
        }.onFailure { error ->
            Log.w(TAG, "Feenstimme $sampleId konnte nicht abgespielt werden", error)
        }
    }

    private fun playEffect(key: String) {
        val volume = soundVolume
        if (volume <= 0f) return
        val sampleId = effects[key] ?: return
        if (sampleId !in loadedClips) return

        runCatching {
            clipPool.play(sampleId, volume, volume, 1, 0, 1f)
        }.onFailure { error ->
            Log.w(TAG, "Klang $key konnte nicht abgespielt werden", error)
        }
    }

    private suspend fun startMusic() {
        stopMusic()
        runCatching {
            val loop = withContext(Dispatchers.Default) { loadOrDecodeMusic(soundCacheDir()) }
            val track = createMusicTrack(loop.size)
            track.write(loop, 0, loop.size)
            // Der ganze Puffer ist die Schleife — dadurch läuft die Musik ohne
            // Lücke weiter, ohne dass jemand nachfüllen muss.
            track.setLoopPoints(0, loop.size, -1)
            track.setVolume(musicVolume)
            track.play()
            music = track
        }.onFailure { error ->
            Log.w(TAG, "Musik konnte nicht gestartet werden", error)
        }
    }

    /** Der Ordner der aktuellen Klangfassung; er wird von zwei Seiten befüllt. */
    private fun soundCacheDir(): File =
        File(appContext.cacheDir, "$CACHE_PREFIX-v$SOUND_CACHE_VERSION").apply { mkdirs() }

    private fun stopMusic() {
        runCatching {
            music?.stop()
            music?.release()
        }
        music = null
    }

    /**
     * Für die Musikschleife.
     *
     * Sie bleibt bei AudioTrack, weil SoundPool Einzelklänge auf etwa ein
     * Megabyte begrenzt — der Ambient-Teppich ist ein Vielfaches davon.
     */
    private fun createMusicTrack(sampleCount: Int): AudioTrack {
        val sizeInBytes = sampleCount * BYTES_PER_SAMPLE

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
            .also { it.setVolume(musicVolume) }
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

        /**
         * Kanäle für gleichzeitige Klänge. Stimmen und Effekte teilen sie sich,
         * deshalb großzügig: Beim schnellen Setzen mehrerer Feen sollen weder
         * Kichern noch Ticks abgeschnitten werden.
         */
        const val MAX_CLIP_STREAMS = 12

        const val PRAISE_DELAY_MILLIS = 900L
        const val GAME_OVER_DELAY_MILLIS = 450L

        const val CACHE_PREFIX = "sounds"

        /**
         * Hochzählen, wenn sich die **Synthese** in [FairySounds] ändert — sonst
         * spielt die App weiter die alten Effekte aus dem Zwischenspeicher.
         *
         * Für die Musik gilt das nicht mehr: Ihr Zwischenspeicher hängt an der
         * Prüfsumme der Aufnahme (siehe `musicFingerprint`), weil genau dieses
         * Hochzählen beim Austausch der Musik einmal vergessen wurde.
         *
         * Auf 5 gesetzt, weil auch die Effekte seit „Musik lauter aussteuern"
         * veraltet im Zwischenspeicher lagen.
         */
        const val SOUND_CACHE_VERSION = 5

        /** Häppchen beim Prüfsummenlesen — größer bringt messbar nichts mehr. */
        const val FINGERPRINT_BUFFER_BYTES = 64 * 1024

        /** Überblendung an der Schleifennaht — kurz genug, um nicht aufzufallen. */
        const val LOOP_CROSSFADE_SECONDS = 0.4f
    }
}
