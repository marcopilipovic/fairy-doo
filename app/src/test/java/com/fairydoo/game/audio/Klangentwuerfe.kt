package com.fairydoo.game.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Entwürfe für die beiden Klänge, die noch aus Dateien kommen.
 *
 * **Warum sie hier stehen und nicht in [FairySounds].** Nataly will sie hören,
 * bevor sie ins Spiel wandern: „Berechne es synthetischerweise, aber bau es
 * noch nicht ein. Ich möchte es vorher hören." Im Testbereich lassen sie sich
 * erzeugen und als WAV ausgeben, ohne dass sich an der App irgendetwas ändert
 * — kein Byte davon landet in einer Installation.
 *
 * Gefallen sie, wandern sie unverändert nach [FairySounds] und ersetzen dort
 * `ambient_forest.mp3` und `fairy_startled.mp3`. Beide sind mit ElevenLabs
 * erzeugt worden, und ob die Lizenz kommerzielle Nutzung deckt, hängt am Tarif
 * zum Zeitpunkt der Erzeugung. Berechnete Klänge machen diese Frage
 * gegenstandslos: Ihre Herkunft steht als Code da, nachlesbar, für immer.
 */
object Klangentwuerfe {

    /**
     * Der Schreck einer falsch gesetzten Fee.
     *
     * Gebaut wie ein Erschrecken klingt: ein hörbarer Ruck nach oben, ein
     * kurzes Zittern auf dem Gipfel, dann ein rascher Abfall. Kein „Aua",
     * sondern ein „Hupsch" — die Fee tut sich nicht weh, sie erschrickt nur.
     *
     * Das Zittern macht die Sache. Ein glatter Auf- und Abstieg klingt nach
     * Rutschpfeife; erst das schnelle Vibrato auf dem höchsten Punkt lässt es
     * lebendig wirken.
     */
    fun aufschrei(): FloatArray {
        val ruck = Synth.tone(
            durationSeconds = 0.34f,
            frequencyAt = { fortschritt ->
                // Steil hoch in den ersten fünfzehn Prozent, dann langsam
                // wieder herunter — die Kurve eines Schrecks.
                if (fortschritt < 0.15f) {
                    620f + 1150f * (fortschritt / 0.15f)
                } else {
                    1770f - 900f * ((fortschritt - 0.15f) / 0.85f)
                }
            },
            amplitudeAt = { fortschritt ->
                // Sofort da, dann verklingend. Ein Schreck hat keinen Anlauf.
                val anstieg = (fortschritt / 0.02f).coerceAtMost(1f)
                anstieg * kotlin.math.exp(-5.5f * fortschritt) * 0.5f
            },
            harmonics = listOf(1f to 1f, 2f to 0.3f, 3f to 0.12f),
            vibratoHz = 26f,
            vibratoDepth = 0.045f,
        )

        // Ein winziges Luftholen davor — der Moment vor dem Schreck.
        val luft = rauschen(0.05f, 0.16f) { fortschritt -> fortschritt * fortschritt }

        return Synth.normalize(Synth.concat(luft, ruck), target = 0.55f)
    }


    /**
     * Die Stimmung des Feenwalds — getragen, ohne Rauschen.
     *
     * **Zwei Versuche waren falsch, und beide auf dieselbe Weise.** Der erste
     * war ein an- und abschwellendes Breitbandrauschen: „Das klingt eher wie
     * das Meer." Der zweite bestand aus einzelnen Raschlern — weniger Rauschen,
     * aber immer noch Rauschen, nur zerhackt: „Das klingt echt komisch. Du
     * musst noch mehr Rauschen rausnehmen. Es muss langgezogener werden."
     *
     * Der Denkfehler war, einen Wald **abbilden** zu wollen. Eine Feldaufnahme
     * besteht nun einmal aus Rauschen, und jeder Versuch, sie nachzubauen,
     * endet bei Rauschen. Das Spiel spielt aber nicht im Wald, sondern in einem
     * *Feenwald* — dort darf die Luft klingen statt zu rascheln.
     *
     * Deshalb hier **kein Rauschen mehr**, sondern getragene Töne: wenige leise
     * Stimmen in derselben Pentatonik, in der das ganze Spiel klingt, jede mit
     * ihrer eigenen sehr langsamen Schwellung. Weil die Schwellungen
     * verschieden lang sind, treffen sie nie zweimal gleich zusammen — so
     * entsteht Bewegung ohne einzelne Ereignisse. Darüber ein paar Vogelrufe,
     * sparsam.
     *
     * **Die Schleife schließt von selbst, ohne Überblendung.** Jede Stimme hat
     * eine Frequenz in ganzen Hertz, jede Schwellung eine ganzzahlige Anzahl
     * Durchläufe — bei einer Länge in ganzen Sekunden steht am Ende damit genau
     * dasselbe wie am Anfang. Das ist sauberer als jedes Überblenden, weil es
     * mathematisch stimmt und nicht nur ungefähr.
     */
    fun waldstimmung(sekunden: Float = 24f, dichte: Float = 1f): FloatArray {
        val zufall = Random(20260810)

        /** Eine Schwellung, die am Anfang und am Ende bei null steht. */
        fun schwellung(durchlaeufe: Int, staerke: Float): (Float) -> Float = { t ->
            staerke * (0.5f - 0.5f * kotlin.math.cos(2f * PI.toFloat() * durchlaeufe * t))
        }

        // Die tragenden Stimmen. Ganze Hertz, damit die Schleife aufgeht.
        val stimmen = listOf(
            Triple(220f, 2, 0.30f),
            Triple(330f, 3, 0.22f),
            Triple(440f, 2, 0.16f),
            Triple(554f, 5, 0.11f),
            Triple(660f, 3, 0.08f),
        )
        val schichten = stimmen.map { (frequenz, durchlaeufe, staerke) ->
            0f to Synth.tone(
                durationSeconds = sekunden,
                frequencyAt = { frequenz },
                amplitudeAt = schwellung(durchlaeufe, staerke),
                // Wenige, weiche Obertöne: Der Klang soll tragen, nicht stechen.
                harmonics = listOf(1f to 1f, 2f to 0.10f, 3f to 0.04f),
            )
        }.toMutableList()

        // Ein Schimmer weit oben — er macht aus dem Akkord einen Wald.
        schichten += 0f to Synth.tone(
            durationSeconds = sekunden,
            frequencyAt = { 1320f },
            amplitudeAt = schwellung(7, 0.05f),
            harmonics = listOf(1f to 1f),
        )

        // Vögel, sparsam. Weit von den Rändern, damit keiner die Naht kreuzt.
        var zeit = 2.5f
        var nummer = 0
        while (zeit < sekunden - 2.5f) {
            val hoehe = 1800f + zufall.nextFloat() * 1600f
            val pegel = if (nummer % 3 == 2) 0.05f else 0.11f
            val toene = if (nummer % 2 == 0) 2 else 1
            repeat(toene) { ton ->
                schichten += (zeit + ton * 0.16f) to Synth.tone(
                    durationSeconds = 0.2f,
                    frequencyAt = { fortschritt ->
                        hoehe * (1f + 0.06f * ton) * (1f + 0.16f * sin(fortschritt * 5f))
                    },
                    amplitudeAt = Synth.pluck(decay = 11f, peak = pegel),
                    harmonics = listOf(1f to 1f, 2f to 0.1f),
                    vibratoHz = 12f,
                    vibratoDepth = 0.04f,
                )
            }
            zeit += (2.2f + zufall.nextFloat() * 2.4f) / dichte
            nummer++
        }

        return Synth.normalize(Synth.mix(*schichten.toTypedArray()), target = 0.45f)
    }

    /** Gefiltertes Rauschen fester Länge, mit eigener Hüllkurve. */
    private fun rauschen(
        sekunden: Float,
        pegel: Float,
        huelle: (Float) -> Float,
    ): FloatArray {
        val laenge = Synth.secondsToSamples(sekunden)
        val zufall = Random(4711)
        var tief = 0f
        return FloatArray(laenge) { i ->
            tief += ((zufall.nextFloat() * 2f - 1f) - tief) * 0.08f
            tief * pegel * huelle(i.toFloat() / laenge)
        }
    }
}
