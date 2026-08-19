package com.fairydoo.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import com.fairydoo.game.data.PlayerProfile
import com.fairydoo.game.game.FairySpecies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
     * Für den aufgenommenen Aufschrei bei falsch gesetzten Feen.
     *
     * SoundPool statt AudioTrack, weil es MP3 selbst dekodiert und mehrere
     * Clips gleichzeitig mischen kann. Im selben Pool liegen die zehn
     * Feentöne — beim schnellen Setzen mehrerer Feen sollen sie sich
     * überlagern statt einander abzuschneiden.
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

    /** Welches Stück laufen soll; der Bildschirm entscheidet. */
    @Volatile
    private var musicTrack: MusicTrack = MusicTrack.Forest

    private val musicEnabled: Boolean get() = musicVolume > 0f

    init {
        loadClips()
        // Effekte und Musik nebeneinander vorbereiten: Nacheinander summierten
        // sich beide Berechnungen, und bis sie fertig waren, blieb das Spiel
        // stumm. Sie hängen nicht voneinander ab.
        scope.launch { prepare() }
        if (musicEnabled) musicJob = scope.launch { startMusic() }
    }

    /** Lädt die aufgenommene Aufschrei-Stimme; das Dekodieren übernimmt SoundPool. */
    private fun loadClips() {
        clipPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedClips += sampleId
            } else {
                Log.w(TAG, "Feenstimme $sampleId konnte nicht geladen werden (Status $status)")
            }
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

        // Dazu die zehn Feentöne — einer je Art. Sie sind winzig (0,42 s) und
        // werden beim Setzen jeder Fee gebraucht, gehören also in denselben
        // Pool wie die übrigen Effekte.
        val alle = builders + FairySpecies.entries.associate { species ->
            chimeKey(species) to { FairyChimes.render(species) }
        }

        effects = alle.mapNotNull { (key, build) ->
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
     * Berechnet wird nur beim ersten Mal. Ein Stück besteht aus rund vierzig
     * Stimmen über eine halbe Minute — das kostet auf einem langsamen Gerät
     * spürbar Zeit, und so lange bliebe es still. Abgelegt wird rohes PCM, weil
     * AudioTrack die Daten als Zahlenfeld erwartet; ein Container brächte nur
     * einen Kopfsatz zum Überspringen.
     */
    private fun loadOrRenderMusic(track: MusicTrack, cacheDir: File): ShortArray {
        val name = "musik-${track.name.lowercase()}-v$MUSIC_VERSION.pcm"
        val file = File(cacheDir, name)

        // Was eine frühere Fassung hinterlassen hat, wird nicht mehr gefunden —
        // es läge sonst für immer da und belegte Platz.
        //
        // Entscheidend ist die Versionsnummer, nicht der Dateiname: Wer hier
        // alles löscht, was gerade *nicht* geladen wird, wirft bei jedem
        // Bildschirmwechsel das andere Stück weg. Dann rechnet die App bei
        // jedem Wechsel eine halbe Minute Musik neu, und während sie das tut,
        // reißt die Tonausgabe hörbar ab.
        val endung = "-v$MUSIC_VERSION.pcm"
        runCatching {
            cacheDir.listFiles()
                ?.filter { it.name.startsWith("musik-") && !it.name.endsWith(endung) }
                ?.forEach { it.delete() }
        }

        if (file.exists() && file.length() >= 2 && file.length() % 2 == 0L) {
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

        val samples = Synth.toPcm16(Music.loopFor(track))

        runCatching {
            val bytes = ByteArray(samples.size * 2)
            samples.forEachIndexed { index, value ->
                bytes[index * 2] = (value.toInt() and 0xFF).toByte()
                bytes[index * 2 + 1] = (value.toInt() shr 8).toByte()
            }
            // Erst danebenschreiben, dann umbenennen. Wird die App mitten im
            // Schreiben beendet — und fünf Megabyte dauern —, läge sonst eine
            // abgeschnittene Schleife im Zwischenspeicher, die ab dann *immer*
            // benutzt würde: Der Name ändert sich ja nicht mehr. Das Umbenennen
            // ist der einzige Schritt, den das Dateisystem nicht halb erledigt.
            val temp = File(cacheDir, "$name.tmp")
            temp.writeBytes(bytes)
            if (!temp.renameTo(file)) temp.delete()
        }.onFailure { error ->
            Log.w(TAG, "Musik konnte nicht zwischengespeichert werden", error)
        }
        return samples
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
        // Aufschrei liegt im SoundPool und die Sprachausgabe hat ihre eigene
        // Bereitschaftsprüfung — beides unabhängig von den berechneten
        // Klängen spielbereit, deshalb wird hier nicht auf `prepared` gewartet.
        when (event) {
            SoundEvent.FairyStartled -> playClip(startledId)
            else -> Unit
        }

        if (!prepared) return

        when (event) {
            SoundEvent.FairyStartled -> Unit
            // Der Ton der Fee läuft über die Feenstimmen-Lautstärke, nicht über
            // die der Klänge: Er ertönt bei jedem Zug und ist damit das, was
            // man am ehesten leiser haben will, ohne Tick und Jubel mit zu
            // dämpfen. Der Regler heißt weiter „Feenstimme" — es ist ja immer
            // noch die Äußerung der Fee, nur ohne Worte.
            is SoundEvent.FairyPlaced -> playEffect(chimeKey(event.species), voiceVolume)
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

    /**
     * Wechselt das Stück — Wald beim Rätseln, Feenpfad auf der Levelkarte.
     *
     * Die Karte ist der Atemzug zwischen zwei Leveln, und das hört man: dieselbe
     * Tonart, aber halb so viele Akkorde, ein Drittel der Glocken, leiser. Es
     * soll sich anfühlen wie ein Ortswechsel im selben Wald, nicht wie ein
     * Senderwechsel.
     *
     * Ein Wechsel schneidet das laufende Stück hart ab. Das ist bei einem
     * Bildschirmwechsel richtig so — hier blendet ohnehin das Bild, und ein
     * Überblenden der Musik bräuchte eine zweite Spur, die die ganze Zeit
     * mitliefe.
     */
    fun setMusicTrack(next: MusicTrack) {
        if (next == musicTrack) return
        musicTrack = next
        if (!musicEnabled) return

        musicJob?.cancel()
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

    private fun playEffect(key: String, atVolume: Float? = null) {
        val volume = atVolume ?: soundVolume
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
        val wanted = musicTrack
        stopMusic()
        runCatching {
            val loop = withContext(Dispatchers.Default) {
                loadOrRenderMusic(wanted, soundCacheDir())
            }
            // Beim ersten Mal dauert das Rechnen; wer in dieser Zeit auf die
            // Levelkarte wechselt, bekäme sonst noch das Waldstück zu hören —
            // und der Wechsel danach fiele aus, weil ja schon etwas läuft.
            if (musicTrack != wanted) return@runCatching

            val audioTrack = createMusicTrack(loop.size)
            audioTrack.write(loop, 0, loop.size)
            // Der ganze Puffer ist die Schleife — dadurch läuft die Musik ohne
            // Lücke weiter, ohne dass jemand nachfüllen muss.
            audioTrack.setLoopPoints(0, loop.size, -1)
            audioTrack.setVolume(musicVolume)
            audioTrack.play()
            music = audioTrack
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

    /** Der Schlüssel, unter dem der Ton einer Fee im Pool liegt. */
    private fun chimeKey(species: FairySpecies) = "fee-${species.name.lowercase()}"

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

        /**
         * Hochzählen, wenn sich [Music] ändert — sonst spielt ein Gerät, auf
         * dem das Spiel schon lief, weiter die alte Fassung aus dem
         * Zwischenspeicher. Getrennt von [SOUND_CACHE_VERSION], damit eine
         * Änderung an der Musik nicht auch alle Effekte neu berechnen lässt.
         */
        const val MUSIC_VERSION = 1
    }
}
