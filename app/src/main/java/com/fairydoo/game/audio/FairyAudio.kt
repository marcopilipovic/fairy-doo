package com.fairydoo.game.audio

import android.content.Context
import com.fairydoo.game.R
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
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
import kotlin.random.Random

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

    /** SoundPool-Kennungen der berechneten Klänge. */
    private var effects: Map<String, Int> = emptyMap()

    @Volatile
    private var music: AudioTrack? = null

    /** Läuft, solange die Musikspur aufgebaut wird. */
    private var musicJob: Job? = null

    /** Läuft, solange die Musik für einen Klang beiseitetritt. */
    private var duckJob: Job? = null

    /**
     * Für alle kurzen Klänge: Ticks, Fähigkeiten, Jubel, die zehn Feentöne.
     *
     * SoundPool statt AudioTrack, weil es mehrere Klänge gleichzeitig mischen
     * kann — beim schnellen Setzen mehrerer Feen sollen sich die Töne
     * überlagern statt einander abzuschneiden. Er lädt aus Dateien, deshalb
     * werden die berechneten Klänge zuvor als WAV in den Zwischenspeicher
     * geschrieben.
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

    /**
     * Merkt sich, welche Klänge der Pool fertig geladen hat.
     *
     * Ein noch nicht geladener Klang bliebe beim Abspielen stumm und belegte
     * trotzdem einen Kanal.
     */
    private fun registerClipLoading() {
        clipPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedClips += sampleId
            } else {
                Log.w(TAG, "Klang $sampleId konnte nicht geladen werden (Status $status)")
            }
        }
    }

    init {
        registerClipLoading()
        // Effekte und Musik nebeneinander vorbereiten: Nacheinander summierten
        // sich beide Berechnungen, und bis sie fertig waren, blieb das Spiel
        // stumm. Sie hängen nicht voneinander ab.
        scope.launch { prepare() }
        if (musicEnabled) musicJob = scope.launch { startMusic() }
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
            KEY_SHIELD to FairySounds::shield,
            KEY_FREEZE to FairySounds::timeFreeze,
            KEY_GAME_OVER to FairySounds::gameOver,
        )

        // Die zehn berechneten Feentöne standen bis zum 29. August hier
        // daneben — einer je Art, die Tonhöhe nach dem Wesen der Figur. Sie
        // sind den sechs Kicheraufnahmen gewichen (siehe unten).
        //
        // [FairyChimes] bleibt im Projekt, samt Tests: Wer sie zurückholen
        // will, hängt sie hier wieder an `builders` und tauscht in `play` den
        // Kicherlaut gegen `chimeKey(event.species)`.
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

        // Der Schreckenslaut kommt wieder vom Band.
        //
        // Er lag als Aufnahme vor, wurde im August berechnet ersetzt und kehrt
        // jetzt zurück — dieselbe Geschichte wie bei der Waldmusik. Anders als
        // dort braucht es keinen Umweg über den Zwischenspeicher: SoundPool
        // lädt Ressourcen unmittelbar, und sechs Kilobyte muss niemand
        // zwischenlagern.
        //
        // [FairySounds.startled] bleibt stehen. Es kostet nichts, solange es
        // niemand aufruft, und wer die Aufnahme wieder herausnehmen will,
        // braucht nur diese Zeilen zu löschen.
        runCatching {
            effects = effects + (KEY_STARTLED to clipPool.load(appContext, R.raw.fairy_startled, 1))
        }.onFailure { error ->
            Log.w(TAG, "Schreckenslaut nicht ladbar", error)
        }

        // Vier Klänge aus einer einzigen Vorlage.
        //
        // Das Stück „Neues Level im Feenwald" ist zwanzig Sekunden lang und hat
        // fünf klar hörbare Ereignisse. Sie sind so verteilt, dass die
        // Dramaturgie des Stücks auf die des Spiels fällt:
        //
        //   1,2 s   der Einstieg          → ein Level beginnt
        //   2,6 s   ein weicher Anschlag  → das Merkzeichen ✕
        //   3,2 s   der Höhepunkt         → ein Level ist geschafft
        //  13,0 s   ein hohes Nachklingen → eine Fee wird weggenommen
        //
        // Das war der Punkt: Vorher kamen Jubel und Levelbeginn aus zwei Welten
        // — der eine gerechnet in C-Dur, der andere eine Aufnahme —, und
        // zwischen ihnen bestand kein Zusammenhang. Jetzt ist es dieselbe Musik
        // an verschiedenen Stellen. Wer ein Level beginnt, hört den Anfang des
        // Stücks; wer es schafft, dessen Höhepunkt.
        //
        // Die Vorlage bleibt unangetastet unter `Audio/`; genommen sind nur
        // Ausschnitte.
        runCatching {
            effects = effects + mapOf(
                KEY_CHEER to clipPool.load(appContext, R.raw.level_complete, 1),
            )
        }.onFailure { error ->
            Log.w(TAG, "Rücknahme oder Jubel nicht ladbar", error)
        }

        // Das Merkzeichen — auch eine Aufnahme, aus derselben Vorlage.
        //
        // Setzen und Entfernen — zwei Klänge, beide aus dem ersten Stück.
        //
        // Elf Fassungen hat das Merkzeichen an einem Tag gehabt: Anschläge,
        // gehaltene Töne, hochgestimmte Töne, herausgefilterte Einzeltöne, drei
        // eigens erzeugte Glocken, zuletzt ein Klang für beide Richtungen.
        // Genommen ist am Ende die Fassung von 8 Uhr früh — die zweite von elf.
        //
        // Das ist kein Rückschritt, sondern das Ergebnis des Vergleichs. Man
        // hört einem Klang nicht an, ob er gut ist; man hört es erst, wenn man
        // die Alternativen kennt. Die Reihe hat sich also gelohnt, auch wenn
        // sie zum Ausgangspunkt zurückführt.
        //
        //   Setzen     ein weicher Anschlag bei 2,6 s, 180 ms
        //   Entfernen  ein hohes Nachklingen bei 13,0 s, 390 ms
        //
        // Beide unverändert aus der Projekthistorie zurückgeholt, nicht neu
        // geschnitten — dieselben Dateien, die an diesem Morgen im Spiel waren.
        //
        // Ihr Verhältnis zur Musik ist trotzdem ein anderes als damals: Die
        // Waldschleife ist inzwischen sechs Dezibel leiser. Die beiden stehen
        // also deutlicher da als heute früh, ohne dass an ihnen etwas geändert
        // wurde.
        runCatching {
            effects = effects + mapOf(
                KEY_WARD to clipPool.load(appContext, R.raw.ward, 1),
                KEY_UNDO to clipPool.load(appContext, R.raw.undo, 1),
            )
        }.onFailure { error ->
            Log.w(TAG, "Merkzeichen oder Rücknahme nicht ladbar", error)
        }

        // Und die sechs Kicherlaute — der Klang, wenn eine Fee richtig sitzt.
        //
        // Keine Zuordnung zur Art: Es wird gewürfelt. Sechs Aufnahmen auf zehn
        // Feen ließen sich ohnehin nicht sauber verteilen, und beim Spielen
        // fällt die Abwechslung angenehmer auf als eine feste Zuordnung, die
        // niemand heraushört.
        //
        // Alle sechs sind auf denselben Pegel gebracht (rund -26 dB RMS, Spitze
        // unter -8 dB). Im Original lagen zwischen dem leisesten und dem
        // lautesten vierzehn Dezibel — roh eingebaut hätte jeder zweite Zug
        // erschreckt.
        runCatching {
            val kichern = listOf(
                R.raw.fairy_giggle_1, R.raw.fairy_giggle_2, R.raw.fairy_giggle_3,
                R.raw.fairy_giggle_4, R.raw.fairy_giggle_5, R.raw.fairy_giggle_6,
            )
            effects = effects + kichern.mapIndexed { index, res ->
                giggleKey(index) to clipPool.load(appContext, res, 1)
            }
        }.onFailure { error ->
            Log.w(TAG, "Kicherlaute nicht ladbar", error)
        }

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
        // Nach der Quelle benannt, nicht nach dem Bildschirm: Teilen sich beide
        // dieselbe Aufnahme, liegt sie auch nur einmal im Zwischenspeicher
        // statt zweimal mit fünf Megabyte.
        val name = "musik-${musicSource(track)}-v$MUSIC_VERSION.pcm"
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

        // Der Wald klingt vom Band, der Feenpfad wird gerechnet.
        //
        // Beides war schon einmal anders herum. Die Waldschleife lag als
        // Aufnahme vor, wurde im August durch ein berechnetes Stück ersetzt —
        // damals, weil niemand die Rechte an der Aufnahme belegen konnte — und
        // kommt jetzt zurück, weil die Testrunde das gerechnete Stück nicht
        // mochte und die Lizenz inzwischen geklärt ist.
        //
        // Für den Feenpfad gibt es keine Aufnahme, nur das gerechnete Stück.
        // Deshalb die Fallunterscheidung statt einer Umstellung überall.
        //
        // Schlägt das Entpacken fehl — beschädigte Datei, kein Decoder auf dem
        // Gerät —, wird gerechnet wie zuvor. Stille wäre die schlechteste aller
        // Antworten.
        val samples = runCatching {
            MusicDecoder.decodeToMono(appContext, R.raw.ambient_forest)
        }.getOrElse { error ->
            Log.w(TAG, "Waldmusik nicht entpackbar — es wird gerechnet", error)
            Synth.toPcm16(Music.loopFor(track))
        }

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
    fun play(event: SoundEvent) {
        // Aufschrei liegt im SoundPool und die Sprachausgabe hat ihre eigene
        // Bereitschaftsprüfung — beides unabhängig von den berechneten
        // Klängen spielbereit, deshalb wird hier nicht auf `prepared` gewartet.
        when (event) {
            else -> Unit
        }

        if (!prepared) return

        when (event) {
            SoundEvent.FairyStartled -> playEffect(KEY_STARTLED)
            // Das Kichern läuft über die Feenstimmen-Lautstärke, nicht über die
            // der Klänge: Es ertönt bei jedem Zug und ist damit das, was man am
            // ehesten leiser haben will, ohne Tick und Jubel mit zu dämpfen.
            is SoundEvent.FairyPlaced -> playEffect(giggleKey(Random.nextInt(GIGGLES)), voiceVolume)
            SoundEvent.Ward -> playEffect(KEY_WARD)
            SoundEvent.Undo -> playEffect(KEY_UNDO)

            // Nur der Jubel. Hier folgte bis zum 29. August ein gesprochener
            // Lobsatz aus der Sprachausgabe des Geräts („Level 4 geschafft!").
            // Er kam knapp eine Sekunde nach dem Jubel und dauerte zwei — beim
            // Weiterspielen war er im Weg, und wer schnell mehrere Level
            // schafft, hörte ihn immer wieder.
            SoundEvent.LevelComplete -> {
                duckMusic(CHEER_MILLIS)
                playEffect(KEY_CHEER)
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

        // Tragen beide Bildschirme dieselbe Aufnahme, wird nichts neu
        // gestartet — die Musik läuft über den Wechsel hinweg weiter.
        //
        // Ohne das setzte sie bei jedem Sprung zwischen Levelkarte und Brett
        // kurz aus und begänne von vorn. Bei zwei verschiedenen Stücken ist
        // genau das gewollt; bei einem gemeinsamen ist es ein Aussetzer ohne
        // Grund.
        val gleicheQuelle = musicSource(next) == musicSource(musicTrack)
        musicTrack = next
        if (gleicheQuelle) return
        if (!musicEnabled) return

        musicJob?.cancel()
        musicJob = scope.launch { startMusic() }
    }

    /**
     * Woher ein Bildschirm seine Musik nimmt.
     *
     * Seit dem 28. August tragen Wald und Feenpfad dieselbe Aufnahme — die
     * berechnete Levelkarten-Fläche hat der Testrunde nicht gefallen. Die
     * Unterscheidung bleibt trotzdem stehen: Kommt später ein eigenes Stück für
     * die Karte, ist es hier eine Zeile, und der Rest funktioniert schon.
     */
    private fun musicSource(track: MusicTrack): String = when (track) {
        MusicTrack.Forest, MusicTrack.Path -> "wald"
    }

    /** Beim Verlassen des Spiels: Musik anhalten. */
    fun pause() {
        runCatching { music?.pause() }
    }

    fun resume() {
        if (!musicEnabled) return

        // `prepared` hing hier bis zum 29. August mit drin. Das war falsch: Es
        // meldet, ob die *berechneten Klänge* fertig sind, und mit der Musik
        // hat das nichts zu tun. Kam die App zurück, bevor die Klänge standen,
        // blieb sie stumm — und niemand versuchte es später noch einmal.
        val laufend = music
        if (laufend != null) {
            runCatching { laufend.play() }
            return
        }

        // Gar keine Spur da: neu aufbauen statt schweigen. Das fängt jeden
        // Aufbau ab, der unterwegs abgebrochen wurde.
        if (musicJob?.isActive != true) musicJob = scope.launch { startMusic() }
    }

    fun release() {
        stopMusic()
        runCatching { clipPool.release() }
        scope.cancel()
    }

    /** Spielt eine aufgenommene Feenstimme. */
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
            // Beim ersten Mal dauert das Entpacken; wer in dieser Zeit den
            // Bildschirm wechselt, bekäme sonst noch das alte Stück zu hören.
            //
            // Verglichen wird die **Quelle**, nicht der Bildschirm — und darin
            // lag der Aussetzer, den Nataly am 29. August beim Spielen gemeldet
            // hat: Wald und Feenpfad tragen dieselbe Aufnahme, aber hier stand
            // `musicTrack != wanted`. Wer während des Aufbaus vom Feenpfad ins
            // Level wechselte, brach ihn damit ab — obwohl der fertige Puffer
            // genau der richtige gewesen wäre. Und niemand startete neu, denn
            // `setMusicTrack` hält sich bei gleicher Quelle absichtlich heraus.
            // Ergebnis: Stille bis zum nächsten Anlass. „Manchmal", weil es nur
            // traf, wer schnell genug tippte.
            if (musicSource(musicTrack) != musicSource(wanted)) return@runCatching

            val audioTrack = createMusicTrack(loop.size)
            audioTrack.write(loop, 0, loop.size)
            // Der ganze Puffer ist die Schleife — dadurch läuft die Musik ohne
            // Lücke weiter, ohne dass jemand nachfüllen muss.
            audioTrack.setLoopPoints(0, loop.size, -1)

            // Nicht bei null einsteigen.
            //
            // Die Waldaufnahme blendet ein: Ihre ersten fünf Sekunden liegen
            // rund sechs Dezibel unter dem Rest, es ist die dünnste Stelle des
            // ganzen Stücks. Wer die App öffnet, hörte bis zum 29. August genau
            // die — der erste Eindruck war ein zaghaftes Anfangen.
            //
            // Die Schleife selbst bleibt unangetastet: Der Einstiegspunkt
            // verschiebt nur, wo man einsteigt, nicht wo sie umschlägt. Die
            // dünne Stelle kommt nach einer vollen Runde wieder und wirkt dort
            // als Atempause, wie die anderen leisen Takte auch.
            runCatching {
                val einstieg = MUSIC_ENTRY_SECONDS * Synth.SAMPLE_RATE
                if (loop.size > einstieg * 2) audioTrack.playbackHeadPosition = einstieg
            }.onFailure { error ->
                // Nicht schlimm: Dann beginnt es eben vorn. Auf keinen Fall darf
                // daran die ganze Musik scheitern.
                Log.w(TAG, "Einstiegspunkt der Musik nicht setzbar", error)
            }

            audioTrack.setVolume(musicVolume)
            audioTrack.play()
            music = audioTrack
        }.onFailure { error ->
            // Ein Wechsel des Stücks bricht den laufenden Aufbau ab — das ist
            // der Normalfall beim Bildschirmwechsel und kein Fehler. Ohne diese
            // Unterscheidung steht bei jedem Wechsel eine Warnung im Protokoll,
            // und echte Fehler gehen darin unter.
            if (error is kotlinx.coroutines.CancellationException) return@onFailure
            Log.w(TAG, "Musik konnte nicht gestartet werden", error)
        }
    }

    /** Der Ordner der aktuellen Klangfassung; er wird von zwei Seiten befüllt. */
    private fun soundCacheDir(): File =
        File(appContext.cacheDir, "$CACHE_PREFIX-v$SOUND_CACHE_VERSION").apply { mkdirs() }

    /**
     * Lässt die Musik für die Dauer eines Klangs beiseitetreten.
     *
     * Bis zum 30. August lief die Waldschleife unter dem Jubel und unter dem
     * Levelbeginn einfach weiter. Beim Spielen fiel auf, dass dabei keiner von
     * beiden zu seinem Recht kommt: Der eine sagt „geschafft", der andere „ein
     * neuer Wald" — und darunter läuft unbeirrt die Fläche, die man die ganze
     * Zeit schon hört. An diesen zwei Stellen soll man *nur* den Klang hören.
     *
     * Weggeblendet statt angehalten, und zwar schnell hinein und langsam wieder
     * heraus (180 ms gegen 700). Ein hartes Abschneiden knackt hörbar, und die
     * Fläche darf nachher zurückkommen, ohne dass es wie ein Einschalten wirkt.
     *
     * Ist der Klang ohnehin stumm gestellt, geschieht nichts — sonst hätte man
     * eine Lücke in der Musik und keinen Grund dafür.
     */
    private fun duckMusic(millis: Long) {
        if (soundVolume <= 0f) return

        duckJob?.cancel()
        duckJob = scope.launch {
            val track = music ?: return@launch
            blende(track, von = musicVolume, nach = 0f, dauer = 180L)
            delay(millis)
            blende(track, von = 0f, nach = musicVolume, dauer = 700L)
            // Zum Schluss auf den Wert von jetzt, nicht auf den von vorhin:
            // Der Spieler kann den Regler zwischendurch bewegt haben.
            runCatching { track.setVolume(musicVolume) }
        }
    }

    private suspend fun blende(track: AudioTrack, von: Float, nach: Float, dauer: Long) {
        val schritte = (dauer / 20L).toInt().coerceAtLeast(1)
        for (i in 1..schritte) {
            val anteil = i / schritte.toFloat()
            runCatching { track.setVolume(von + (nach - von) * anteil) }
            delay(20L)
        }
    }

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
    private fun giggleKey(index: Int) = "kichern-$index"

    private companion object {
        const val TAG = "FairyAudio"
        const val BYTES_PER_SAMPLE = 2

        const val KEY_CHEER = "cheer"
        const val KEY_SHIELD = "shield"
        const val KEY_FREEZE = "freeze"
        const val KEY_WARD = "ward"
        const val KEY_UNDO = "undo"
        const val KEY_GAME_OVER = "gameOver"

        /** Wie lange die Musik unter dem Jubel beiseitetritt — seine Dauer plus ein Atemzug. */
        const val CHEER_MILLIS = 2_700L
        const val KEY_STARTLED = "startled"

        /** So viele Kicherlaute liegen bei. */
        const val GIGGLES = 6

        /**
         * Kanäle für gleichzeitige Klänge. Stimmen und Effekte teilen sie sich,
         * deshalb großzügig: Beim schnellen Setzen mehrerer Feen sollen weder
         * Kichern noch Ticks abgeschnitten werden.
         */
        const val MAX_CLIP_STREAMS = 12

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
        const val SOUND_CACHE_VERSION = 11

        /** Wo die Waldschleife beim Start einsetzt — siehe startMusic. */
        const val MUSIC_ENTRY_SECONDS = 5

        /**
         * Hochzählen, wenn sich [Music] ändert — sonst spielt ein Gerät, auf
         * dem das Spiel schon lief, weiter die alte Fassung aus dem
         * Zwischenspeicher. Getrennt von [SOUND_CACHE_VERSION], damit eine
         * Änderung an der Musik nicht auch alle Effekte neu berechnen lässt.
         */
        // Erhöht, wenn sich ein Stück ändert: Der Zwischenspeicher wird sonst
        // weiter mit der alten Fassung bedient. Auf 2 seit der Rückkehr der
        // Waldaufnahme.
        const val MUSIC_VERSION = 3
    }
}
