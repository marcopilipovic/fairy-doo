package com.fairydoo.game.audio

import com.fairydoo.game.audio.Synth.between
import kotlin.math.pow
import kotlin.random.Random

/**
 * Die Klangwelt des Feenwalds — alles zur Laufzeit berechnet.
 *
 * Die Stimmen sind bewusst schmal und hoch gehalten: Feen sollen klein und
 * luftig klingen, nicht wie ein Chor. Tiefe Anteile würden im Nachtwald-Ambiente
 * außerdem mit der Musik kollidieren.
 */
object FairySounds {

    /** Die Töne der Pentatonik, in der alles klingt (A-Dur-Pentatonik). */
    private val scale = listOf(440f, 495f, 554f, 660f, 740f, 880f, 990f, 1108f)

    /**
     * Ein Kichern.
     *
     * Aufgebaut aus mehreren kurzen Silben mit leicht steigender Tonhöhe — das
     * „hi-hi-hi"-Muster trägt den Eindruck, nicht der einzelne Ton. Jede
     * [variant] klingt etwas anders, damit wiederholtes Setzen nicht mechanisch
     * wirkt.
     */
    fun giggle(variant: Int): FloatArray {
        val random = Random(variant * 7919L)
        val syllables = 4 + variant % 3
        val basePitch = scale[variant % scale.size] * random.between(0.95f, 1.12f)
        val step = random.between(1.04f, 1.12f)

        val layers = mutableListOf<Pair<Float, FloatArray>>()
        var offset = 0f

        repeat(syllables) { index ->
            val pitch = basePitch * step.pow(index)
            val duration = random.between(0.070f, 0.105f)

            // Innerhalb einer Silbe steigt die Tonhöhe kurz an und fällt wieder —
            // das gibt dem Ton den lachenden „Knick".
            val syllable = Synth.tone(
                durationSeconds = duration,
                frequencyAt = { progress ->
                    pitch * (1f + 0.16f * kotlin.math.sin(progress * Math.PI.toFloat()))
                },
                amplitudeAt = Synth.pluck(decay = 7f, peak = 0.55f),
                harmonics = listOf(1f to 1f, 2f to 0.35f, 3f to 0.12f),
                vibratoHz = 32f,
                vibratoDepth = 0.02f,
            )
            layers += offset to syllable
            offset += duration + random.between(0.028f, 0.052f)
        }

        // Ein Funkeln obendrauf, damit es nach Fee klingt und nicht nach Vogel.
        layers += 0.02f to Synth.tone(
            durationSeconds = 0.5f,
            frequencyAt = { progress -> 2400f + 900f * progress },
            amplitudeAt = Synth.pluck(decay = 9f, peak = 0.10f),
        )

        return Synth.normalize(Synth.mix(*layers.toTypedArray()), target = 0.7f)
    }

    /** Wie viele verschiedene Kicher-Varianten es gibt. */
    const val GIGGLE_VARIANTS = 6

    /**
     * Der erschrockene Aufschrei, wenn eine Fee falsch gesetzt wird.
     *
     * Steil abfallende Tonhöhe mit kräftigem Vibrato — das ist das Muster, das
     * wir als Erschrecken hören. Die Obertöne machen ihn schneidend genug, um
     * sich vom freundlichen Rest abzuheben.
     */
    fun yelp(): FloatArray {
        val cry = Synth.tone(
            durationSeconds = 0.55f,
            frequencyAt = { progress -> 1150f * (1f - 0.68f * progress.pow(0.7f)) },
            amplitudeAt = { progress ->
                when {
                    progress < 0.02f -> progress / 0.02f
                    else -> (1f - progress).pow(1.4f) * 0.75f
                }
            },
            harmonics = listOf(1f to 1f, 2f to 0.5f, 3f to 0.28f, 4f to 0.12f),
            vibratoHz = 26f,
            vibratoDepth = 0.07f,
        )

        // Ein kurzer, tiefer Schreck darunter gibt dem Aufschrei Gewicht.
        val thud = Synth.tone(
            durationSeconds = 0.3f,
            frequencyAt = { progress -> 180f * (1f - 0.4f * progress) },
            amplitudeAt = Synth.pluck(decay = 9f, peak = 0.35f),
            harmonics = listOf(1f to 1f, 2f to 0.2f),
        )

        return Synth.normalize(Synth.mix(0f to cry, 0.01f to thud), target = 0.8f)
    }

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
     * Der Ambient-Teppich, der in Schleife läuft.
     *
     * Vier langsam wechselnde Akkorde mit darüber gestreuten Glockentönen. Die
     * Länge ist absichtlich krumm zur Akkordfolge, damit die Wiederholung nicht
     * auffällt; Anfang und Ende werden ineinander geblendet, damit die Schleife
     * nicht klickt.
     */
    fun ambientLoop(seconds: Float = 24f): FloatArray {
        val chords = listOf(
            listOf(220.0f, 261.63f, 329.63f), // a-Moll
            listOf(174.61f, 220.0f, 261.63f), // F-Dur
            listOf(196.0f, 246.94f, 293.66f), // G-Dur
            listOf(261.63f, 329.63f, 392.0f), // C-Dur
        )
        val chordSeconds = seconds / chords.size
        val layers = mutableListOf<Pair<Float, FloatArray>>()

        chords.forEachIndexed { index, chord ->
            chord.forEach { frequency ->
                layers += index * chordSeconds to Synth.tone(
                    // Überlappung, damit die Akkorde ineinander übergehen.
                    durationSeconds = chordSeconds * 1.5f,
                    frequencyAt = { frequency },
                    amplitudeAt = Synth.envelope(attack = 0.3f, release = 0.45f, peak = 0.10f),
                    harmonics = listOf(1f to 1f, 2f to 0.22f, 3f to 0.07f),
                    // Sehr langsames Schweben hält den Klang lebendig.
                    vibratoHz = 0.18f,
                    vibratoDepth = 0.004f,
                )
            }
        }

        // Vereinzelte Glockentöne, fest gesät — bei jedem Start dieselben.
        val random = Random(20_260_801L)
        repeat(18) {
            val at = random.between(0.5f, seconds - 2f)
            layers += at to Synth.tone(
                durationSeconds = 2.2f,
                frequencyAt = { scale.random(random) * 2f },
                amplitudeAt = Synth.pluck(decay = 3.2f, peak = 0.075f),
                harmonics = listOf(1f to 1f, 2f to 0.3f, 4f to 0.08f),
            )
        }

        val mixed = Synth.mix(*layers.toTypedArray())
        val trimmed = mixed.copyOf(Synth.secondsToSamples(seconds))
        return Synth.fadeEdges(Synth.normalize(trimmed, target = 0.45f), seconds = 1.2f)
    }
}
