package com.fairydoo.game.audio

import kotlin.random.Random

/**
 * Die beiden Hintergrundstücke — Wald und Feenpfad.
 *
 * Gerechnet statt aufgenommen, und das aus drei Gründen. Erstens gehört eine
 * berechnete Schleife dem Spiel: keine Lizenz, keine Quelle, keine Frage nach
 * Rechten. Zweitens schließt sie sich exakt — [Synth.mixLooping] faltet, was
 * hinten übersteht, nach vorn, sodass es keine Naht gibt, die knacken könnte.
 * Drittens kostet sie keinen Platz: Die vorherige Aufnahme war 1,4 MB und damit
 * über ein Drittel der ganzen App.
 *
 * Der Preis ist ehrlich zu nennen: Das klingt nach Flächen und Glöckchen, nicht
 * nach eingespielten Instrumenten. Für einen Klangteppich, der stundenlang
 * unter einem Rätsel liegen soll, ist das kein Nachteil — er darf nicht
 * interessant sein, er darf nur nicht stören.
 *
 * ## Warum D-dorisch
 *
 * Die Tonleiter D E F G A B C: kleine Terz wie Moll, aber große Sexte. Das
 * nimmt der Grundstimmung das Schwere, ohne sie fröhlich zu machen — nach
 * altem Wald, nicht nach Trauer und nicht nach Kindergeburtstag. Die
 * charakteristische Note ist das H im G-Dur-Akkord; ohne sie wäre es schlicht
 * d-Moll.
 *
 * Beide Stücke teilen sich diesen Tonvorrat und die Grundtonart. Der Wechsel
 * zwischen Levelkarte und Spielfeld soll wie ein Ortswechsel im selben Wald
 * klingen, nicht wie ein Senderwechsel.
 */
/** Welches der beiden Stücke gerade laufen soll. */
enum class MusicTrack { Forest, Path }

object Music {

    /** Die Schleife zu einem Bildschirm. */
    fun loopFor(track: MusicTrack): FloatArray = when (track) {
        MusicTrack.Forest -> forestLoop()
        MusicTrack.Path -> pathLoop()
    }

    /**
     * Die Akkorde des Waldes, als Frequenzen in Hertz.
     *
     * Dm(add9) – G – Am7 – F(add9). Die Folge dreht sich, statt irgendwohin zu
     * streben: Kein Akkord löst den vorigen auf, keiner verlangt nach einem
     * bestimmten nächsten. Genau das soll sie auch tun — eine Folge mit
     * Zielrichtung hörte man beim zwanzigsten Durchlauf als Schleife.
     */
    private val FOREST_CHORDS = listOf(
        listOf(146.83f, 174.61f, 220.00f, 329.63f),   // Dm add9
        listOf(196.00f, 246.94f, 293.66f, 392.00f),   // G
        listOf(220.00f, 261.63f, 329.63f, 392.00f),   // Am7
        listOf(174.61f, 220.00f, 261.63f, 392.00f),   // F add9
    )

    /** Die Grundtöne darunter, eine Oktave tiefer als die Akkorde. */
    private val FOREST_BASS = listOf(73.42f, 98.00f, 110.00f, 87.31f)

    /**
     * Der Feenpfad kommt mit zwei Akkorden aus — Heimat und Gegenpol.
     *
     * Auf der Karte wird nicht nachgedacht, sondern ausgeatmet. Weniger
     * Bewegung, längere Felder, mehr Stille dazwischen.
     */
    private val PATH_CHORDS = listOf(
        listOf(146.83f, 174.61f, 220.00f, 329.63f),   // Dm add9
        listOf(220.00f, 261.63f, 329.63f, 392.00f),   // Am7
    )

    private val PATH_BASS = listOf(73.42f, 110.00f)

    /**
     * Die Glockentöne, pentatonisch aus derselben Tonleiter.
     *
     * Fünf Töne statt sieben: Aus einer Pentatonik klingt jede Kombination
     * zusammen. Weil die Glocken zufällig gesät werden, ist das keine
     * Bequemlichkeit — es ist die Bedingung dafür, dass nie ein Ton gegen die
     * Fläche darunter steht.
     */
    private val BELLS = listOf(587.33f, 659.25f, 783.99f, 880.00f, 1046.50f)

    /** Der Klangteppich des Spielfelds. */
    fun forestLoop(): FloatArray = weave(
        loopSeconds = FOREST_SECONDS,
        chords = FOREST_CHORDS,
        bass = FOREST_BASS,
        bellCount = 22,
        bellPeak = 0.085f,
        padPeak = 0.085f,
        seed = 20_260_819L,
    )

    /**
     * Der Klangteppich der Levelkarte.
     *
     * Dieselbe Tonart, deutlich dünner besetzt: halb so viele Akkorde auf
     * längerer Schleife, ein Drittel der Glocken, leiser. Vorher lief hier gar
     * nichts — die Karte war der einzige stille Bildschirm im Spiel und fühlte
     * sich dadurch an wie eine Pause vom Spiel statt wie ein Teil davon.
     */
    fun pathLoop(): FloatArray = weave(
        loopSeconds = PATH_SECONDS,
        chords = PATH_CHORDS,
        bass = PATH_BASS,
        bellCount = 8,
        bellPeak = 0.07f,
        padPeak = 0.065f,
        seed = 20_260_820L,
    )

    /**
     * Webt Fläche, Bass und Glocken zu einer geschlossenen Schleife.
     *
     * Jeder Akkord klingt anderthalb Felder lang — er überlappt also mit dem
     * folgenden. Der letzte reicht über das Ende hinaus und klingt am Anfang
     * aus, wo der erste gerade einsetzt. Deshalb braucht diese Schleife weder
     * Ausblenden noch Überblenden: An der Stelle, wo sie sich schließt, ist
     * genauso viel los wie überall sonst.
     */
    private fun weave(
        loopSeconds: Float,
        chords: List<List<Float>>,
        bass: List<Float>,
        bellCount: Int,
        bellPeak: Float,
        padPeak: Float,
        seed: Long,
    ): FloatArray {
        val chordSeconds = loopSeconds / chords.size
        val layers = mutableListOf<Pair<Float, FloatArray>>()

        chords.forEachIndexed { index, chord ->
            val at = index * chordSeconds

            chord.forEachIndexed { voice, frequency ->
                layers += at to Synth.tone(
                    durationSeconds = chordSeconds * 1.55f,
                    frequencyAt = { frequency },
                    // Die oberen Stimmen etwas leiser: Sonst schiebt sich der
                    // Akkord in den Vordergrund, statt zu tragen.
                    amplitudeAt = Synth.envelope(
                        attack = 0.34f,
                        release = 0.44f,
                        peak = padPeak * (1f - voice * 0.13f),
                    ),
                    harmonics = listOf(1f to 1f, 2f to 0.20f, 3f to 0.06f),
                    // Sehr langsames Schweben, für jede Stimme ein anderes
                    // Tempo — dadurch stehen die Stimmen nie ganz still
                    // zueinander, und die Fläche wirkt lebendig statt gehalten.
                    vibratoHz = 0.11f + voice * 0.017f,
                    vibratoDepth = 0.0035f,
                )
            }

            layers += at to Synth.tone(
                durationSeconds = chordSeconds * 1.35f,
                frequencyAt = { bass[index] },
                amplitudeAt = Synth.envelope(attack = 0.3f, release = 0.4f, peak = padPeak * 0.85f),
                // Fast nur Grundton: Ein Bass mit vielen Obertönen macht auf
                // einem Telefonlautsprecher Matsch statt Tiefe.
                harmonics = listOf(1f to 1f, 2f to 0.12f),
            )
        }

        // Feste Saat: Die Glocken fallen bei jedem Start an dieselben Stellen.
        // Das ist Absicht — wer das Spiel oft öffnet, soll sein Stück
        // wiedererkennen, nicht jedes Mal ein anderes hören.
        val random = Random(seed)
        repeat(bellCount) {
            val at = random.nextFloat() * loopSeconds
            val high = random.nextFloat() < 0.3f
            layers += at to Synth.tone(
                durationSeconds = if (high) 2.0f else 3.2f,
                frequencyAt = { BELLS[random.nextInt(BELLS.size)] * if (high) 2f else 1f },
                amplitudeAt = Synth.pluck(
                    decay = if (high) 5.0f else 3.4f,
                    peak = bellPeak * if (high) 0.6f else 1f,
                ),
                harmonics = listOf(1f to 1f, 2.76f to 0.18f, 5.4f to 0.05f),
            )
        }

        // Kein fadeEdges: Das würde genau die Stille an die Naht setzen, die
        // mixLooping gerade vermeidet.
        return Synth.normalize(
            Synth.mixLooping(loopSeconds, *layers.toTypedArray()),
            target = 0.72f,
        )
    }

    /**
     * Schleifenlängen.
     *
     * Krumm zur Akkordzahl gewählte Glockensaat und ungleiche Vibrato-Tempi
     * sorgen dafür, dass sich innerhalb der Schleife nichts exakt wiederholt.
     * Länger wäre unauffälliger, kostet aber Arbeitsspeicher: 32 Sekunden sind
     * als 16-Bit-Mono rund 2,8 MB — schon jetzt weniger als die 60-Sekunden-
     * Aufnahme vorher belegte.
     */
    private const val FOREST_SECONDS = 32f
    private const val PATH_SECONDS = 40f
}
