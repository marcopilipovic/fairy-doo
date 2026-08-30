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
    fun cheer(): FloatArray = fanfare(
        grundton = 523.25f,
        glitzer = 14,
        anschlag = 0.01f,
        // 0,85 → 0,40 → 0,25. Zweimal zurückgenommen, beide Male nach dem
        // Spielen: erst weil er alles andere überragte, dann noch einmal um
        // vier Dezibel, weil er auch danach noch heraussprang.
        ziel = 0.25f,
    )

    /**
     * Der Klang beim Beginn des nächsten Levels.
     *
     * **Wird seit dem 30. August nicht mehr abgespielt** — an seiner Stelle
     * liegt eine Aufnahme (`res/raw/level_start.mp3`). Der Grund war nicht die
     * Lautstärke, sondern die Gebärde: Aufsteigend und in Dur klingt nach „gut
     * gemacht", und beim Beginn eines Levels kommt man an, statt etwas zu
     * schaffen. Er bleibt samt Test hier, falls er zurück soll.
     *
     * **Derselbe Satz wie der Jubel**, nur eine Oktave höher und so leise, dass
     * er unter dem Bildwechsel liegt statt über ihm — so gewünscht, und es hat
     * einen Grund, dass es dieselbe Figur ist: Was man eben als „geschafft"
     * gehört hat, kommt als Echo zurück, wenn das neue Brett erscheint. Ein
     * fremder Klang an dieser Stelle wäre eine zweite Ansage; dieser ist die
     * Erinnerung an die erste.
     *
     * Der Glitzerregen ist auf ein Drittel zurück und der Anschlag weich — er
     * soll nicht anschlagen, sondern schon da sein.
     */
    fun levelStart(): FloatArray = fanfare(
        grundton = 1046.50f,
        glitzer = 5,
        anschlag = 0.05f,
        // Mit dem Jubel um dieselben vier Dezibel zurück, damit das Echo im
        // Verhältnis zu seinem Vorbild bleibt.
        ziel = 0.082f,
    )

    /**
     * Die gemeinsame Figur hinter Jubel und Levelbeginn.
     *
     * Bewusst eine Funktion für beide statt zweier ähnlicher: Der leise Klang
     * *ist* der laute, nur anders eingestellt. Zwei Fassungen nebeneinander
     * liefen früher oder später auseinander, und dann wäre der Zusammenhang
     * weg, der den Klang an dieser Stelle überhaupt richtig macht.
     */
    private fun fanfare(
        grundton: Float,
        glitzer: Int,
        anschlag: Float,
        ziel: Float,
    ): FloatArray {
        val melody = listOf(0, 2, 4, 7).map { step -> grundton * 2f.pow(step / 12f) }
        val layers = mutableListOf<Pair<Float, FloatArray>>()

        melody.forEachIndexed { index, frequency ->
            layers += index * 0.11f to Synth.tone(
                durationSeconds = 1.1f,
                frequencyAt = { frequency },
                amplitudeAt = Synth.pluck(decay = 4.5f, peak = 0.42f, attack = anschlag),
                harmonics = listOf(1f to 1f, 2f to 0.4f, 3f to 0.18f, 5f to 0.07f),
            )
        }

        // Schlussakkord eine Oktave höher, als Krönung.
        layers += 0.44f to Synth.tone(
            durationSeconds = 1.6f,
            frequencyAt = { grundton * 2f },
            amplitudeAt = Synth.pluck(decay = 3f, peak = 0.5f, attack = anschlag),
            harmonics = listOf(1f to 1f, 1.5f to 0.4f, 2f to 0.3f, 3f to 0.12f),
        )

        // Glitzerregen aus schnellen, hohen Funken.
        repeat(glitzer) { index ->
            val random = Random(index * 104_729L)
            layers += random.between(0.05f, 1.0f) to Synth.tone(
                durationSeconds = 0.4f,
                frequencyAt = { progress -> scale.random(random) * 2f + 600f * progress },
                amplitudeAt = Synth.pluck(decay = 11f, peak = 0.12f),
            )
        }

        // Der Jubel stand bis zum 29. August auf 0,85 und war damit mit Abstand
        // der lauteste Klang im Spiel — gut neun Dezibel über einem Feenton und
        // zwölf über dem Merkzeichen. Er kommt einmal je Level und darf
        // heraustreten, aber nicht erschrecken; zwei Sekunden dichter Satz
        // wirken ohnehin lauter als ein einzelner kurzer Ton bei gleichem Pegel.
        return Synth.normalize(Synth.mix(*layers.toTypedArray()), target = ziel)
    }

    /**
     * Der Feenstaub-Hinweis: eine aufsteigende Funkenkaskade.
     *
     * **Wird seit dem 30. August nicht mehr abgespielt.** Eine Hilfe soll nach
     * dem Zug klingen, den sie tut — der Feenstaub setzt eine Fee, also kichert
     * sie, wie bei jedem anderen Zug auch. Die Kaskade bleibt samt Test hier,
     * falls sie doch wieder gebraucht wird; sie kostet nichts, solange sie
     * niemand in `FairyAudio.prepare` einhängt.
     *
     * Die Stufen stammen seit dem 29. August aus derselben Pentatonik wie alles
     * andere — D E G A C, aufwärts über gut eine Oktave.
     *
     * Vorher stiegen sie in Schritten von Faktor 1,16, beginnend bei 1200 Hz.
     * Das sind rund zweieinhalb Halbtöne je Stufe und damit **keine Tonleiter**:
     * Die Kaskade war der einzige melodische Klang des Spiels, der zu keinem
     * anderen passte — sie stand quer zur Musik, zu den Feentönen und zum
     * Jubel. Hörbar wurde das als „klingt nach Spielautomat", und genau das war
     * es auch.
     */
    fun sparkle(): FloatArray {
        val stufen = listOf(587.33f, 659.25f, 783.99f, 880.00f, 1046.50f, 1174.66f, 1318.51f)
        val layers = stufen.mapIndexed { index, hertz ->
            index * 0.055f to Synth.tone(
                durationSeconds = 0.5f,
                frequencyAt = { hertz },
                amplitudeAt = Synth.pluck(decay = 9f, peak = 0.3f, attack = 0.03f),
                harmonics = listOf(1f to 1f, 2.76f to 0.12f),
            )
        }
        // 0,6 war lauter als der Feenton, den die Kaskade ankündigt. Jetzt
        // liegt sie knapp darunter — sie zeigt etwas, sie feiert nichts.
        return Synth.normalize(Synth.mix(*layers.toTypedArray()), target = 0.28f)
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

    /**
     * Das Setzen eines Merkzeichens: ein leiser, trockener Tick.
     *
     * **Wird seit dem 30. August nicht mehr abgespielt** — an seiner Stelle
     * liegt ein Anschlag aus der Levelbeginn-Vorlage (`res/raw/ward.mp3`).
     * Er bleibt samt Test hier, falls der gerechnete Tick zurück soll; die
     * Abwägungen darunter gelten weiter, sie sind der Grund, warum der
     * Nachfolger ebenfalls tief, weich und leise ist.
     *
     * Der häufigste Klang im ganzen Spiel — beim Ausschließen fällt er
     * dutzendfach je Level. Deshalb ist er am 29. August zurückgenommen worden,
     * an vier Stellen zugleich:
     *
     * - **Tiefer.** Statt 1500 Hz beginnt er bei 780. Das Ohr ist um 1,5 kHz
     *   am empfindlichsten; derselbe Pegel eine Oktave tiefer wirkt spürbar
     *   sanfter, ohne undeutlich zu werden.
     * - **Weicherer Anschlag.** 6 ms statt einer. Das nimmt ihm den Klick, und
     *   der Klick war das, was auffiel.
     * - **Weniger Oberton.** Der dritte Teilton macht das Spitze aus.
     * - **Leiser**, zuletzt: von 0,35 auf 0,22.
     *
     * Die Reihenfolge ist Absicht. Nur leiser zu drehen hätte den Tick
     * undeutlich gemacht, ohne ihn unauffälliger zu machen.
     */
    fun tick(): FloatArray = Synth.normalize(
        Synth.tone(
            durationSeconds = 0.11f,
            frequencyAt = { progress -> 780f - 190f * progress },
            amplitudeAt = Synth.pluck(decay = 20f, peak = 0.3f, attack = 0.055f),
            harmonics = listOf(1f to 1f, 3f to 0.07f),
        ),
        target = 0.22f,
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
    // Mit dem Tick zusammen zurückgenommen: Wäre nur der Tick leiser geworden,
    // wäre ausgerechnet das Wegnehmen der lautere der beiden Züge — und das
    // Wegnehmen ist der seltenere.
    fun undo(): FloatArray = Synth.normalize(
        Synth.tone(
            durationSeconds = 0.28f,
            frequencyAt = { progress -> 700f - 330f * progress },
            amplitudeAt = Synth.pluck(decay = 8f, peak = 0.3f, attack = 0.03f),
            harmonics = listOf(1f to 1f, 2f to 0.12f),
        ),
        target = 0.25f,
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
