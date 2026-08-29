package com.fairydoo.game.audio

import com.fairydoo.game.game.FairySpecies

/**
 * Der Ton, den eine Fee von sich gibt, sobald sie richtig sitzt.
 *
 * Vorher sprach hier die Sprachausgabe des Geräts einen kurzen Ausruf
 * („Juhuu!", „Hihi!"). Das war einmal charmant und beim zwanzigsten Mal
 * lästig: Eine Stimme verlangt Aufmerksamkeit, und beim Setzen einer Fee will
 * man keine bekommen, sondern eine Bestätigung. Dazu kam, dass jedes Gerät
 * eine andere Stimme mitbringt und man nie wusste, wie es beim Spieler klingt.
 *
 * Jetzt ist es ein Glockenton — verwandt mit dem Tick des Merkzeichens, nur
 * länger und mit Nachklang. Er sagt dasselbe, hält aber niemanden auf.
 *
 * ## Warum diese zehn Tonhöhen
 *
 * Sie stammen aus derselben Pentatonik wie die Glocken der Hintergrundmusik
 * (siehe [Music]): D E G A C über zwei Oktaven. Damit kann kein Ton gegen die
 * Fläche stehen, die gerade läuft — egal, welche Fee man wann setzt und an
 * welcher Stelle des Stücks. Das ist keine Kosmetik: Ein Bestätigungston, der
 * sich mit der Musik reibt, klingt nach Fehler.
 *
 * Die Zuordnung folgt dem Wesen der Figur. Terra, die Kristallfee, bekommt den
 * tiefsten und längsten Ton; Ignis, die Funkenfee, den höchsten und kürzesten.
 * So ist auch ohne Hinsehen zu hören, wer da gerade Platz genommen hat.
 */
object FairyChimes {

    /**
     * @param hertz Grundton aus der Pentatonik.
     * @param decay Wie schnell der Ton verklingt — kleiner heißt länger.
     * @param bell Anteil des Glocken-Obertons. Höher klingt metallischer,
     *   niedriger runder und holzartiger.
     */
    data class Chime(val hertz: Float, val decay: Float, val bell: Float)

    private val chimes: Map<FairySpecies, Chime> = mapOf(
        FairySpecies.Terra to Chime(587.33f, 5.0f, 0.10f),    // D5  — Kristall, schwer
        FairySpecies.Aura to Chime(659.25f, 5.5f, 0.16f),     // E5  — Strahl, weich
        FairySpecies.Nebula to Chime(783.99f, 6.0f, 0.12f),   // G5  — Staub, matt
        FairySpecies.Viridis to Chime(880.00f, 6.5f, 0.14f),    // A5  — Wald, rund
        FairySpecies.Chrono to Chime(1046.50f, 7.0f, 0.30f),  // C6  — Pendel, klar
        FairySpecies.Zephyr to Chime(1174.66f, 7.5f, 0.11f),  // D6  — Wind, luftig
        FairySpecies.Trixie to Chime(1318.51f, 8.5f, 0.26f),  // E6  — Chaos, spitz
        FairySpecies.Salta to Chime(1567.98f, 9.5f, 0.18f),   // G6  — Hüpfen, kurz
        FairySpecies.Nixie to Chime(1760.00f, 10.5f, 0.34f),  // A6  — Frost, gläsern
        FairySpecies.Ignis to Chime(2093.00f, 12.0f, 0.22f),  // C7  — Funke, schnell
    )

    fun of(species: FairySpecies): Chime =
        requireNotNull(chimes[species]) { "Kein Ton für $species hinterlegt" }

    /**
     * Der fertige Klang einer Fee.
     *
     * Ein Hauch Abwärtsglitzern in der Tonhöhe — der Ton fällt über seine
     * Dauer um gut ein Prozent. Das ist zu wenig, um als Rutschen aufzufallen,
     * reicht aber, damit er lebendig klingt statt wie ein Signalton.
     */
    fun render(species: FairySpecies): FloatArray {
        val chime = of(species)

        // Je höher die Fee, desto weniger Metall.
        //
        // Das Spitze eines Glockentons steckt nicht im Grundton, sondern im
        // Teilton darüber. Bei Ignis liegt der Grundton auf 2093 Hz — der
        // Glockenton daraus landet bei 5,8 kHz, also genau dort, wo ein
        // Handylautsprecher am schärfsten ist. Bei Terra auf 587 Hz ist
        // derselbe Anteil völlig harmlos. Deshalb wird er nicht pauschal
        // gekürzt, sondern nach Tonhöhe: unten bleibt die Glocke, oben wird
        // sie zu Holz.
        val metall = (900f / chime.hertz).coerceIn(0.4f, 1f)

        return Synth.normalize(
            Synth.tone(
                durationSeconds = 0.42f,
                frequencyAt = { progress -> chime.hertz * (1f - 0.012f * progress) },
                // 15 ms Anschlag statt 4. Der Ton blüht auf, statt anzuschlagen
                // — das ist der Unterschied zwischen einer Bestätigung und
                // einem Signal.
                amplitudeAt = Synth.pluck(decay = chime.decay, peak = 0.5f, attack = 0.036f),
                // Der zweite Teilton liegt bewusst nicht auf einem ganzen
                // Vielfachen: So klingen echte Glocken, und der Ton bekommt
                // seinen Schimmer, ohne dass eine zweite Tonhöhe hörbar wird.
                harmonics = listOf(
                    1f to 1f,
                    2.76f to chime.bell * metall * 0.6f,
                    5.4f to chime.bell * metall * metall * 0.1f,
                ),
            ),
            target = 0.30f,
        )
    }
}
