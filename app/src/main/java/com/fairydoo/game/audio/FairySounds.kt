package com.fairydoo.game.audio

import com.fairydoo.game.audio.Synth.between
import kotlin.math.pow
import kotlin.random.Random

/**
 * Die berechnete Klangwelt des Feenwalds.
 *
 * Die Feenstimmen selbst — Kichern und Aufschrei — sind **keine** Synthese
 * mehr, sondern echte Aufnahmen aus `res/raw` (siehe [FairyClips]). Berechnet
 * wird hier alles Übrige: Jubel, Fähigkeiten, Ticks und die Musik. Für
 * Instrumente und Ambiente ist Synthese ideal, für eine Stimme nicht — deren
 * Klangfarbe lässt sich aus Sinustönen nicht überzeugend bauen.
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
    fun tick(): FloatArray = Synth.normalize(
        Synth.tone(
            durationSeconds = 0.09f,
            frequencyAt = { progress -> 1500f - 400f * progress },
            amplitudeAt = Synth.pluck(decay = 24f, peak = 0.3f),
            harmonics = listOf(1f to 1f, 3f to 0.2f),
        ),
        target = 0.35f,
    )

    /**
     * Die falsch gesetzte Fee: ein kurzer Schreck.
     *
     * Vorher eine Aufnahme — der einzige fremde Ton, der noch im Spiel war, und
     * ohne belegbare Herkunft. Jetzt gerechnet wie alles andere, damit an der
     * App nichts hängt, dessen Rechte niemand nachweisen kann.
     *
     * Zwei Töne im Tritonus, beide fallend. Das ist Absicht und nicht
     * beliebig: Alle guten Klänge des Spiels — Feentöne wie Glocken der Musik
     * — stammen aus einer Pentatonik und passen immer zusammen. Der Tritonus
     * kommt darin nicht vor. Der Fehler klingt deshalb hörbar *daneben*, ohne
     * laut oder unangenehm zu sein.
     */
    fun startled(): FloatArray {
        val fall = { start: Float -> { progress: Float -> start * (1f - 0.28f * progress) } }
        return Synth.normalize(
            Synth.mix(
                0f to Synth.tone(
                    durationSeconds = 0.34f,
                    frequencyAt = fall(740f),
                    amplitudeAt = Synth.pluck(decay = 9f, peak = 0.5f),
                    harmonics = listOf(1f to 1f, 2f to 0.28f, 3f to 0.1f),
                ),
                // Der zweite Ton setzt einen Hauch später ein — dadurch wirkt
                // es wie ein Zusammenzucken statt wie ein Signalton.
                0.02f to Synth.tone(
                    durationSeconds = 0.32f,
                    frequencyAt = fall(1046f),
                    amplitudeAt = Synth.pluck(decay = 11f, peak = 0.38f),
                    harmonics = listOf(1f to 1f, 2f to 0.2f),
                ),
            ),
            target = 0.45f,
        )
    }

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

}
