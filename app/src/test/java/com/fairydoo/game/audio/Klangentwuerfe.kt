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
     * Der Wald als Schleife.
     *
     * **Der erste Entwurf klang nach Meer.** Nataly: „Das klingt eher wie das
     * Meer anstatt einen Wald. Das ist definitiv zu viel Rauschen." Sie hat den
     * Finger genau auf den Fehler gelegt: Dort lag ein durchgehendes,
     * tiefpassgefiltertes Rauschen, das langsam an- und abschwoll — und
     * langsames An- und Abschwellen von Breitbandrauschen ist die Signatur von
     * Wellen. Man kann daraus keinen Wald machen, indem man leiser dreht.
     *
     * **Was einen Wald ausmacht, sind Ereignisse mit Stille dazwischen.** Die
     * See hört nie auf; ein Wald raschelt in Stößen, und zwischen zwei Böen
     * passiert nichts. Deshalb ist die Bauweise hier eine andere:
     *
     * - Die Grundschicht ist fast nichts — sehr leise, ohne Schwankung. Nur so
     *   viel, dass es nicht tot klingt. Sie schwillt bewusst **nicht** an.
     * - Darüber einzelne **Raschler**: kurze, helle Stöße zu unregelmäßigen
     *   Zeiten, jeder mit eigener Länge und Lautstärke. Sie sind das, was man
     *   als Blätter hört.
     * - Und **Vögel**, mehr und verschiedener als vorher. Sie sind das
     *   stärkste Erkennungszeichen eines Waldes; Wasser hat keine.
     *
     * Nachmessen lässt sich der Unterschied am Verhältnis von Spitze zu
     * mittlerer Lautheit: Gleichmäßiges Rauschen liegt bei etwa drei bis vier,
     * eine Klanglandschaft aus Einzelereignissen deutlich darüber. Der Test
     * prüft das.
     */
    fun waldrauschen(sekunden: Float = 16f): FloatArray {
        val laenge = Synth.secondsToSamples(sekunden)
        val zufall = Random(20260810)

        // Die Grundschicht: kaum wahrnehmbar, ohne jede Schwankung. Sie füllt
        // nur die Stille, damit zwischen zwei Raschlern kein Loch klafft.
        //
        // **Der Faktor ist ausgemessen, nicht geschätzt.** Bei 0,9 blieb in
        // vierzehn Sekunden kein einziger ruhiger Abschnitt übrig — die
        // Grundschicht deckte alles zu, und genau das klang nach Brandung. Die
        // Reihe: 0,30 → 0 % Ruhe, 0,15 → 22 %, 0,12 → 57 %, 0,08 → 77 %. Bei
        // 0,08 wirkt der Wald tot, bei 0,12 atmet er.
        val luft = FloatArray(laenge)
        var tief = 0f
        for (i in 0 until laenge) {
            tief += ((zufall.nextFloat() * 2f - 1f) - tief) * 0.012f
            luft[i] = tief * 0.12f
        }

        val schichten = mutableListOf(0f to luft)

        // Die Raschler. Unregelmäßig verteilt — gleichmäßige Abstände klängen
        // nach Maschine.
        var zeit = 0.4f
        while (zeit < sekunden - 2.0f) {
            val dauer = 0.12f + zufall.nextFloat() * 0.35f
            val staerke = 0.09f + zufall.nextFloat() * 0.13f
            schichten += zeit to raschler(dauer, staerke, zufall.nextInt())
            // Zwischen 0,5 und 2,3 Sekunden Ruhe. Die Stille gehört dazu.
            zeit += 0.5f + zufall.nextFloat() * 1.8f
        }

        // Vögel: zwei Sorten, damit es nicht nach einem einzelnen Tier klingt.
        // Nah und deutlich, oder fern und leise mit Nachhall.
        val rufe = listOf(1.6f, 3.1f, 4.9f, 7.2f, 8.4f, 10.6f, 12.3f, 13.8f)
        rufe.forEachIndexed { nummer, zeitpunkt ->
            val fern = nummer % 3 == 2
            val hoehe = 1900f + zufall.nextFloat() * 1900f
            val pegel = if (fern) 0.05f else 0.14f
            val toene = if (nummer % 2 == 0) 2 else 1

            repeat(toene) { ton ->
                val versatz = ton * 0.13f
                schichten += (zeitpunkt + versatz) to Synth.tone(
                    durationSeconds = 0.15f,
                    frequencyAt = { fortschritt ->
                        // Ein Ruf steigt und fällt; zwei Töne hintereinander
                        // liegen etwas auseinander.
                        val grund = hoehe * (1f + 0.06f * ton)
                        grund * (1f + 0.18f * sin(fortschritt * 7f))
                    },
                    amplitudeAt = Synth.pluck(decay = 15f, peak = pegel),
                    harmonics = listOf(1f to 1f, 2f to 0.12f),
                    vibratoHz = 16f,
                    vibratoDepth = 0.035f,
                )
            }
            if (fern) {
                schichten += (zeitpunkt + 0.22f) to Synth.tone(
                    durationSeconds = 0.18f,
                    frequencyAt = { hoehe * 0.98f },
                    amplitudeAt = Synth.pluck(decay = 22f, peak = pegel * 0.5f),
                    harmonics = listOf(1f to 1f),
                )
            }
        }

        val gemischt = Synth.normalize(Synth.mix(*schichten.toTypedArray()), target = 0.5f)
        val geschlossen = Synth.crossfadeLoop(Synth.toPcm16(gemischt), seconds = 1.4f)
        return FloatArray(geschlossen.size) { geschlossen[it] / Short.MAX_VALUE.toFloat() }
    }

    /**
     * Ein einzelner Raschler — trockene Blätter, kurz bewegt.
     *
     * Zwei Dinge unterscheiden ihn von Rauschen. Erstens ist er **hell**: Die
     * Differenz aufeinanderfolgender Zufallswerte nimmt die tiefen Anteile
     * heraus, und ohne die klingt es nach Blatt statt nach Brandung. Zweitens
     * ist er **körnig**: Eine schnelle, zufällige Zweithüllkurve zerhackt ihn in
     * viele winzige Anschläge — ein Blätterhaufen besteht aus einzelnen
     * Blättern, kein gleichmäßiges Zischen.
     */
    private fun raschler(sekunden: Float, pegel: Float, saat: Int): FloatArray {
        val laenge = Synth.secondsToSamples(sekunden)
        val zufall = Random(saat)
        var vorher = 0f
        var koernung = 0f
        return FloatArray(laenge) { i ->
            val roh = zufall.nextFloat() * 2f - 1f
            val hell = roh - vorher
            vorher = roh
            // Körnung: springt zufällig und fällt schnell zurück.
            if (zufall.nextFloat() < 0.004f) koernung = 1f
            koernung *= 0.9993f
            val t = i.toFloat() / laenge
            // Schneller Einsatz, langes Ausklingen.
            val huelle = (t / 0.08f).coerceAtMost(1f) * kotlin.math.exp(-3.2f * t)
            hell * (0.35f + 0.65f * koernung) * huelle * pegel
        }
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
