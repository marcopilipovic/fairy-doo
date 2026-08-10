package com.fairydoo.game.audio

import kotlin.random.Random

/**
 * Entwürfe für Klänge, die noch aus Dateien kommen.
 *
 * Die Waldstimmung stand hier ebenfalls, bis Nataly sie abgenommen hat — dann
 * ist sie nach [FairySounds] gewandert. Ein Entwurf, der auch nach der Abnahme
 * hier liegen bliebe, wäre eine zweite Kopie, und zwei Kopien laufen
 * auseinander.
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
