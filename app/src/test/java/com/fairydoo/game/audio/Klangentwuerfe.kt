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
     * Das Waldrauschen als Schleife.
     *
     * Drei Schichten, wie ein Wald sie hat: der Wind als tiefes Rauschen, die
     * Blätter als helles Zischeln darüber, und ab und zu ein Vogel.
     *
     * **Die Schleife ist das eigentlich Schwierige.** Ein Ambiente darf nicht
     * hörbar von vorn anfangen, sonst zählt das Ohr innerhalb einer Minute mit.
     * Zwei Vorkehrungen dagegen: Alle langsamen Schwankungen haben Perioden,
     * die ganzzahlig in die Länge passen — sie stehen am Ende genau dort, wo
     * sie am Anfang standen. Und der Schluss wird über den Beginn geblendet
     * (siehe [Synth.crossfadeLoop]), womit auch das Rauschen ohne Naht
     * schließt.
     *
     * Die Vögel sitzen bewusst weit von den Rändern entfernt. Einer, der über
     * die Naht liefe, würde abgeschnitten — und ein abgeschnittener Vogelruf
     * ist genau das Geräusch, an dem man eine Schleife erkennt.
     */
    fun waldrauschen(sekunden: Float = 14f): FloatArray {
        val laenge = Synth.secondsToSamples(sekunden)
        val zufall = Random(20260810)

        // Wind: tiefes Rauschen, langsam an- und abschwellend.
        val wind = FloatArray(laenge)
        var tief = 0f
        for (i in 0 until laenge) {
            val roh = zufall.nextFloat() * 2f - 1f
            // Einfacher Tiefpass — er nimmt dem Rauschen die Schärfe und macht
            // daraus Wind statt Zischen.
            tief += (roh - tief) * 0.02f
            val t = i.toFloat() / laenge
            val schwellen = 0.55f + 0.45f * sin(2f * PI.toFloat() * 2f * t)
            wind[i] = tief * schwellen * 3.2f
        }

        // Blätter: helleres Rauschen, schneller moduliert, deutlich leiser.
        val blaetter = FloatArray(laenge)
        var vorher = 0f
        for (i in 0 until laenge) {
            val roh = zufall.nextFloat() * 2f - 1f
            // Hochpass durch Differenzbildung: Was bleibt, ist das Zischeln.
            val hell = roh - vorher
            vorher = roh
            val t = i.toFloat() / laenge
            val boeen = 0.35f + 0.65f * abs(sin(2f * PI.toFloat() * 3f * t))
            blaetter[i] = hell * boeen * 0.16f
        }

        val schichten = mutableListOf(0f to wind, 0f to blaetter)

        // Vögel: kurze Rufe, weit von den Rändern entfernt.
        val rufe = listOf(2.1f, 5.4f, 6.0f, 9.7f, 11.2f)
        rufe.forEachIndexed { nummer, zeitpunkt ->
            val hoehe = 2200f + zufall.nextFloat() * 1400f
            schichten += zeitpunkt to Synth.tone(
                durationSeconds = 0.16f,
                frequencyAt = { fortschritt -> hoehe * (1f + 0.22f * sin(fortschritt * 9f)) },
                amplitudeAt = Synth.pluck(decay = 16f, peak = 0.13f),
                harmonics = listOf(1f to 1f, 2f to 0.15f),
                vibratoHz = 14f,
                vibratoDepth = 0.03f,
            )
            // Jeder zweite Ruf bekommt ein kurzes Echo — das macht den Wald tief.
            if (nummer % 2 == 0) {
                schichten += (zeitpunkt + 0.19f) to Synth.tone(
                    durationSeconds = 0.13f,
                    frequencyAt = { hoehe * 0.97f },
                    amplitudeAt = Synth.pluck(decay = 20f, peak = 0.05f),
                    harmonics = listOf(1f to 1f),
                )
            }
        }

        val gemischt = Synth.normalize(Synth.mix(*schichten.toTypedArray()), target = 0.42f)

        // Schluss über den Anfang blenden — erst danach schließt die Schleife
        // wirklich ohne Naht.
        val geschlossen = Synth.crossfadeLoop(Synth.toPcm16(gemischt), seconds = 1.2f)
        return FloatArray(geschlossen.size) { geschlossen[it] / Short.MAX_VALUE.toFloat() }
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
