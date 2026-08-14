package com.fairydoo.game.audio

import com.fairydoo.game.audio.Synth.between
import kotlin.math.pow
import kotlin.random.Random

/**
 * Die berechnete Klangwelt des Feenwalds.
 *
 * Berechnet wird alles Kurze: der Klick beim Setzen, Ticks, Fähigkeiten, Jubel
 * und die Musik. Aus `res/raw` kommt nur noch der Aufschrei beim Falschsetzen
 * (siehe [FairyClips]).
 *
 * **Die gesprochenen Feenausrufe sind entfallen.** Sie liefen über die
 * Sprachausgabe des Geräts und sagten je Feenart ein anderes Wort. Beim
 * Spielen setzt man dutzende Feen, und ein gesprochenes Wort verträgt das
 * nicht — siehe [place].
 */
object FairySounds {

    /** Die Töne der Pentatonik, in der alles klingt (A-Dur-Pentatonik). */
    private val scale = listOf(440f, 495f, 554f, 660f, 740f, 880f, 990f, 1108f)

    /**
     * Der Jubel am Levelende: eine aufsteigende Glockenfigur mit Nachklang.
     *
     * Läuft in Dur und endet auf der Oktave — die Auflösung nach oben ist das,
     * was als „geschafft" gehört wird.
     */
    fun cheer(): FloatArray {
        val melody = listOf(0, 2, 4, 7).map { step -> 523.25f * 2f.pow(step / 12f) }
        val layers = mutableListOf<Pair<Float, FloatArray>>()

        melody.forEachIndexed { index, frequency ->
            layers += index * 0.11f to Synth.tone(
                durationSeconds = 1.1f,
                frequencyAt = { frequency },
                amplitudeAt = Synth.pluck(decay = 4.5f, peak = 0.42f),
                harmonics = listOf(1f to 1f, 2f to 0.4f, 3f to 0.18f, 5f to 0.07f),
            )
        }

        // Schlussakkord eine Oktave höher, als Krönung.
        layers += 0.44f to Synth.tone(
            durationSeconds = 1.6f,
            frequencyAt = { 1046.5f },
            amplitudeAt = Synth.pluck(decay = 3f, peak = 0.5f),
            harmonics = listOf(1f to 1f, 1.5f to 0.4f, 2f to 0.3f, 3f to 0.12f),
        )

        // Glitzerregen aus schnellen, hohen Funken.
        repeat(14) { index ->
            val random = Random(index * 104_729L)
            layers += random.between(0.05f, 1.0f) to Synth.tone(
                durationSeconds = 0.4f,
                frequencyAt = { progress -> scale.random(random) * 2f + 600f * progress },
                amplitudeAt = Synth.pluck(decay = 11f, peak = 0.12f),
            )
        }

        return Synth.normalize(Synth.mix(*layers.toTypedArray()), target = 0.85f)
    }

    /** Der Feenstaub-Hinweis: eine aufsteigende Funkenkaskade. */
    fun sparkle(): FloatArray {
        val layers = (0 until 7).map { index ->
            index * 0.045f to Synth.tone(
                durationSeconds = 0.5f,
                frequencyAt = { 1200f * 1.16f.pow(index) },
                amplitudeAt = Synth.pluck(decay = 10f, peak = 0.3f),
                harmonics = listOf(1f to 1f, 2f to 0.2f),
            )
        }
        return Synth.normalize(Synth.mix(*layers.toTypedArray()), target = 0.6f)
    }

    /** Der Natur-Schild: ein warmer, sich öffnender Zweiklang. */
    fun shield(): FloatArray {
        val low = Synth.tone(
            durationSeconds = 1.0f,
            frequencyAt = { progress -> 220f + 40f * progress },
            amplitudeAt = Synth.envelope(attack = 0.08f, release = 0.5f, peak = 0.4f),
            harmonics = listOf(1f to 1f, 2f to 0.3f, 3f to 0.1f),
        )
        val high = Synth.tone(
            durationSeconds = 1.0f,
            frequencyAt = { progress -> 330f + 60f * progress },
            amplitudeAt = Synth.envelope(attack = 0.12f, release = 0.5f, peak = 0.3f),
            harmonics = listOf(1f to 1f, 2f to 0.25f),
        )
        return Synth.normalize(Synth.mix(0f to low, 0.05f to high), target = 0.55f)
    }

    /** Die Zeiten-Blüte: ein schwebender, langsam pulsierender Ton. */
    fun timeFreeze(): FloatArray {
        val shimmer = Synth.tone(
            durationSeconds = 1.4f,
            frequencyAt = { progress -> 880f - 120f * progress },
            amplitudeAt = Synth.envelope(attack = 0.1f, release = 0.55f, peak = 0.35f),
            harmonics = listOf(1f to 1f, 2f to 0.3f, 4f to 0.1f),
            vibratoHz = 5.5f,
            vibratoDepth = 0.03f,
        )
        return Synth.normalize(shimmer, target = 0.55f)
    }

    /** Das Setzen eines Merkzeichens: ein leiser, trockener Tick. */
    /**
     * Der Klick beim Setzen einer Fee.
     *
     * **Er ersetzt die gesprochenen Ausrufe.** Bis hierher sagte die
     * Sprachausgabe bei jeder richtig gesetzten Fee „Juhuu!" oder „Jippie!" —
     * je Art ein anderes Wort. Beim Spielen nutzt sich das ab: Man setzt in
     * einer Partie dutzende Feen, und ein gesprochenes Wort ist beim
     * dreißigsten Mal keine Freude mehr, sondern ein Grund, den Ton
     * abzuschalten. Nataly: „die müssen durch einfache irgendwelche
     * Klickgeräusche ersetzt werden."
     *
     * Ein Klick verträgt Wiederholung, ein Wort nicht.
     *
     * Kürzer und trockener als [tick]: Der Merkzeichen-Tick darf nachklingen,
     * dieser hier soll nur bestätigen. Sechzig Millisekunden, steiler Abfall,
     * ein bisschen Obertonglanz, damit es nach Feenwald klingt und nicht nach
     * Schreibmaschine.
     *
     * Dass jede Feenart trotzdem anders klingt, kostet keinen zweiten Klang:
     * Der SoundPool kann denselben schneller oder langsamer abspielen. Siehe
     * `FairyAudio.rateFor`.
     */
    fun place(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(
                durationSeconds = 0.06f,
                frequencyAt = { progress -> 2100f - 900f * progress },
                amplitudeAt = Synth.pluck(decay = 42f, peak = 0.34f),
                harmonics = listOf(1f to 1f, 2f to 0.22f),
            ),
            // Ein hoher Funke obendrauf — er macht aus dem Klick ein Glitzern.
            0.005f to Synth.tone(
                durationSeconds = 0.05f,
                frequencyAt = { 3600f },
                amplitudeAt = Synth.pluck(decay = 60f, peak = 0.16f),
                harmonics = listOf(1f to 1f),
            ),
        ),
        target = 0.32f,
    )

    fun tick(): FloatArray = Synth.normalize(
        Synth.tone(
            durationSeconds = 0.09f,
            frequencyAt = { progress -> 1500f - 400f * progress },
            amplitudeAt = Synth.pluck(decay = 24f, peak = 0.3f),
            harmonics = listOf(1f to 1f, 3f to 0.2f),
        ),
        target = 0.35f,
    )

    /** Das Zurücknehmen einer Fee: ein kurzes Abwärts-Wispern. */
    fun undo(): FloatArray = Synth.normalize(
        Synth.tone(
            durationSeconds = 0.28f,
            frequencyAt = { progress -> 900f - 420f * progress },
            amplitudeAt = Synth.pluck(decay = 8f, peak = 0.3f),
            harmonics = listOf(1f to 1f, 2f to 0.2f),
        ),
        target = 0.4f,
    )

    /** Das Spielende: eine absteigende Molltonfolge, die verklingt. */
    fun gameOver(): FloatArray {
        val steps = listOf(0, -3, -5, -8).map { step -> 523.25f * 2f.pow(step / 12f) }
        val layers = steps.mapIndexed { index, frequency ->
            index * 0.26f to Synth.tone(
                durationSeconds = 1.5f,
                frequencyAt = { frequency },
                amplitudeAt = Synth.pluck(decay = 3f, peak = 0.42f),
                harmonics = listOf(1f to 1f, 2f to 0.25f, 3f to 0.08f),
            )
        }
        return Synth.normalize(Synth.mix(*layers.toTypedArray()), target = 0.7f)
    }


    /**
     * Eine Fee wurde falsch gesetzt und erschrickt.
     *
     * **Er muss aus der Leiter fallen — das ist seine ganze Aufgabe.** Die zehn
     * Feentöne stehen auf einer Pentatonik und klingen nie schief; sie sagen
     * „richtig". Ein Fehlerklang, der ebenfalls hineinpasste, sagte gar nichts.
     * Deshalb gleitet dieser hier **stufenlos nach oben**, statt eine Stufe zu
     * treffen, und trägt Rauschen darüber. Man hört den Unterschied, ohne
     * hinzusehen und ohne dass jemand es erklären muss.
     *
     * Ersetzt `fairy_startled.mp3` — die letzte Aufnahme im Spiel neben dem
     * Waldteppich. Ein Klang, den niemand aufgenommen hat, gehört dem, der ihn
     * berechnet.
     */
    fun startled(): FloatArray {
        val random = kotlin.random.Random(4711)
        return Synth.normalize(
            Synth.mix(
                // Das Aufschrecken: die Tonhöhe steigt, kurz und hell.
                0f to Synth.tone(
                    durationSeconds = 0.45f,
                    frequencyAt = { progress -> 760f + 1_500f * progress },
                    amplitudeAt = Synth.pluck(decay = 7f, peak = 0.85f),
                    harmonics = listOf(1f to 1f, 2.76f to 0.28f),
                ),
                // Der Flügelschlag: schmales Rauschen, das schnell verklingt.
                0f to Synth.bandpass(
                    Synth.noise(0.45f, Synth.pluck(decay = 10f, peak = 0.5f), random),
                    centerHz = 2_600f,
                    guete = 3.5f,
                ),
            ),
            target = 0.7f,
        )
    }

    /**
     * Der Waldteppich für den Pfad.
     *
     * **Ersetzt `ambient_forest.mp3`** — die letzte Aufnahme im Spiel. Ein
     * Klang, den niemand aufgenommen hat, gehört dem, der ihn berechnet; damit
     * ist die Lizenzfrage nicht geklärt, sondern gegenstandslos.
     *
     * **Kein stehender Klang.** Das ist die Lehre aus dem Weltraumspiel: Ein
     * gehaltener Akkord klingt nach Orgel und fällt aus einer Welt heraus, in
     * der alles angeschlagen wird. Hier sind es tiefe, weiche Anschläge, die
     * lange ausklingen und sich immer überlappen — eine Fläche, die atmet.
     *
     * **Die Schleife schließt ohne Naht, weil sie ein Kreis ist.** Was am Ende
     * über den Rand hinausklingt, wird vorn wieder eingesetzt. Dadurch gibt es
     * keinen Punkt, an dem etwas anfängt oder aufhört — anders als beim
     * Überblenden, das immer ein Stück Klang kostet und bei getragenen Tönen
     * ein Schweben erzeugt.
     *
     * Dieselbe Leiter wie die zehn Feentöne, nur zwei Oktaven tiefer: Der
     * Teppich liegt unter ihnen und stört sie nie.
     */
    fun forest(): FloatArray {
        val laenge = Synth.secondsToSamples(LOOP_SEKUNDEN)
        val aus = FloatArray(laenge)

        // Anschlag und Stufe. Unregelmäßig, damit sich kein Takt einstellt —
        // ein Wald hat keinen.
        val anschlaege = listOf(
            0.0f to 0, 2.3f to 2, 4.1f to 1, 6.4f to 3,
            8.0f to 0, 9.7f to 2, 11.2f to 4,
        )
        val leiter = floatArrayOf(130.81f, 164.81f, 196.00f, 220.00f, 261.63f)

        anschlaege.forEach { (zeit, stufe) ->
            val ton = waldton(leiter[stufe])
            val beginn = Synth.secondsToSamples(zeit)
            for (i in ton.indices) {
                aus[(beginn + i) % laenge] += ton[i]
            }
        }
        return Synth.normalize(aus, target = 0.45f)
    }

    private fun waldton(frequenz: Float): FloatArray = Synth.tone(
        durationSeconds = 5.5f,
        frequencyAt = { frequenz },
        amplitudeAt = { fortschritt ->
            val anschlag = 0.22f
            val huelle = if (fortschritt < anschlag) {
                fortschritt / anschlag
            } else {
                Math.E.toFloat().let { e -> Math.pow(e.toDouble(), (-1.7f * fortschritt).toDouble()).toFloat() }
            }
            val schluss = 0.8f
            if (fortschritt <= schluss) huelle else huelle * (1f - (fortschritt - schluss) / 0.2f)
        },
        // Weniger unrein als die Feen: Der Teppich soll tragen, nicht klirren.
        // Ein Hauch Unreinheit bleibt, damit er zur selben Welt gehört.
        harmonics = listOf(1f to 1f, 2.02f to 0.2f, 3.11f to 0.06f),
    )

    /** Wie lang die Waldschleife ist. Zwölf Sekunden, siehe [forest]. */
    const val LOOP_SEKUNDEN = 12f
}
