package com.fairydoo.game.audio

import com.fairydoo.game.game.FairySpecies
import kotlin.math.pow

/**
 * Der kurze Klang, den eine Fee von sich gibt, sobald sie richtig sitzt.
 *
 * Vorher sprach die Sprachausgabe des Geräts „Juhuu!", „Brrr!", „Hihi!". Das
 * konnte nicht gut klingen: Eine Sprachsynthese ist auf Wörter trainiert, und
 * Laute wie „Brrr" liest sie halb buchstabierend vor. Dazu kam eine Tonhöhe von
 * bis zu 1.9 — ab etwa 1.3 entsteht der bekannte Helium-Effekt. Und wie es am
 * Ende klang, entschied ohnehin die Stimme, die zufällig auf dem Gerät
 * installiert war.
 *
 * Ein zweiter Anlauf mit Retro-Effekten (Blip, Jump, Laser) scheiterte an
 * etwas anderem: Das ist die Klangwelt eines Arcade-Automaten, nicht die eines
 * gemalten Nachtwalds.
 *
 * Deshalb hier dieselbe Sprache, in der das Spiel ohnehin klingt — A-Dur-
 * Pentatonik, Glocken- und Zupftöne, wie in [FairySounds]. Jede Art bekommt
 * eine eigene kurze Figur statt einer verbogenen Systemstimme.
 *
 * Alle Motive sind **kurz** (unter einer halben Sekunde): Beim schnellen Setzen
 * mehrerer Feen dürfen sie sich nicht zu Matsch überlagern.
 */
object FairyMotifs {

    /** Die Tonleiter des Waldes — dieselbe wie in [FairySounds]. */
    private const val A4 = 440f

    /** Halbtonschritte von A4 aus, damit die Figuren lesbar bleiben. */
    private fun note(semitones: Int): Float = A4 * 2f.pow(semitones / 12f)

    /**
     * Glockenartige Obertöne: leicht unrein, dadurch metallisch statt orgelhaft.
     *
     * Ganzzahlige Vielfache klingen nach Blasinstrument. Eine echte Glocke hat
     * unreine Teiltöne — genau die machen den kristallinen Klang aus.
     */
    private val bell = listOf(1f to 1f, 2.76f to 0.32f, 5.40f to 0.14f, 8.93f to 0.05f)

    /** Weicher, holziger Klang — für die bodenständigen Feen. */
    private val wood = listOf(1f to 1f, 2f to 0.45f, 3f to 0.16f, 4.2f to 0.06f)

    /** Heller Zupfton ohne Metall — freundlich, nicht kalt. */
    private val chime = listOf(1f to 1f, 2f to 0.34f, 3f to 0.12f, 5f to 0.05f)

    fun of(species: FairySpecies): FloatArray = when (species) {
        FairySpecies.Viridis -> viridis()
        FairySpecies.Nebula -> nebula()
        FairySpecies.Salta -> salta()
        FairySpecies.Aura -> aura()
        FairySpecies.Nixie -> nixie()
        FairySpecies.Zephyr -> zephyr()
        FairySpecies.Ignis -> ignis()
        FairySpecies.Terra -> terra()
        FairySpecies.Chrono -> chrono()
        FairySpecies.Trixie -> trixie()
    }

    /** Waldfee — zwei aufblühende Töne, warm und rund. */
    private fun viridis(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(0.34f, { note(4) }, Synth.pluck(decay = 7f, peak = 0.55f), chime),
            0.07f to Synth.tone(0.40f, { note(9) }, Synth.pluck(decay = 6f, peak = 0.60f), chime),
        ),
        target = 0.7f,
    )

    /** Staubfee — ein Hauch, fast nur Anklang. Sehr leise, sehr kurz. */
    private fun nebula(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(0.22f, { note(16) }, Synth.pluck(decay = 16f, peak = 0.34f), chime),
            0.02f to Synth.tone(0.26f, { note(21) }, Synth.pluck(decay = 18f, peak = 0.22f), bell),
        ),
        target = 0.5f,
    )

    /** Hüpffee — auf und ab und wieder auf, schnell und federnd. */
    private fun salta(): FloatArray = Synth.normalize(
        Synth.concat(
            Synth.tone(0.10f, { note(9) }, Synth.pluck(decay = 14f, peak = 0.55f), chime),
            Synth.tone(0.09f, { note(4) }, Synth.pluck(decay = 15f, peak = 0.45f), chime),
            Synth.tone(0.22f, { note(16) }, Synth.pluck(decay = 9f, peak = 0.6f), chime),
        ),
        target = 0.72f,
    )

    /** Strahlfee — ein einzelner Ton, der aufgeht statt anzuschlagen. */
    private fun aura(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(
                durationSeconds = 0.55f,
                frequencyAt = { note(12) },
                // Langsamer Anlauf statt Anschlag — das ist das „Aufatmen".
                amplitudeAt = Synth.envelope(attack = 0.35f, release = 0.55f, peak = 0.6f),
                harmonics = bell,
            ),
            0.10f to Synth.tone(
                durationSeconds = 0.45f,
                frequencyAt = { note(19) },
                amplitudeAt = Synth.envelope(attack = 0.4f, release = 0.55f, peak = 0.3f),
                harmonics = chime,
            ),
        ),
        target = 0.66f,
    )

    /** Frostfee — hoch, dünn, gläsern. Klirrt kurz und ist wieder weg. */
    private fun nixie(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(0.28f, { note(24) }, Synth.pluck(decay = 13f, peak = 0.5f), bell),
            0.015f to Synth.tone(0.24f, { note(31) }, Synth.pluck(decay = 17f, peak = 0.28f), bell),
        ),
        target = 0.62f,
    )

    /** Windfee — ein Gleiten, das anschwillt und vorüberzieht. */
    private fun zephyr(): FloatArray = Synth.normalize(
        Synth.tone(
            durationSeconds = 0.42f,
            // Aufwärts und wieder ab: eine Böe, kein Ton.
            frequencyAt = { progress -> note(11) * (1f + 0.5f * kotlin.math.sin(Math.PI * progress).toFloat()) },
            amplitudeAt = Synth.envelope(attack = 0.3f, release = 0.5f, peak = 0.5f),
            harmonics = chime,
            vibratoHz = 7f,
            vibratoDepth = 0.02f,
        ),
        target = 0.6f,
    )

    /** Funkenfee — drei sehr schnelle Funken nach oben. */
    private fun ignis(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(0.14f, { note(21) }, Synth.pluck(decay = 22f, peak = 0.5f), chime),
            0.045f to Synth.tone(0.14f, { note(26) }, Synth.pluck(decay = 22f, peak = 0.45f), chime),
            0.09f to Synth.tone(0.20f, { note(28) }, Synth.pluck(decay = 16f, peak = 0.55f), bell),
        ),
        target = 0.68f,
    )

    /** Kristallfee — tief, satt, mit Boden. Die einzige, die nach unten geht. */
    private fun terra(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(0.42f, { note(-8) }, Synth.pluck(decay = 6f, peak = 0.6f), wood),
            0.03f to Synth.tone(0.34f, { note(4) }, Synth.pluck(decay = 8f, peak = 0.3f), wood),
        ),
        target = 0.72f,
    )

    /** Pendelfee — ein hölzernes Ticken mit einem winzigen Glöckchen darin. */
    private fun chrono(): FloatArray = Synth.normalize(
        Synth.mix(
            0f to Synth.tone(0.07f, { note(2) }, Synth.pluck(decay = 40f, peak = 0.6f), wood),
            0.008f to Synth.tone(0.20f, { note(26) }, Synth.pluck(decay = 24f, peak = 0.22f), bell),
        ),
        target = 0.6f,
    )

    /** Chaosfee — zwei Töne, der zweite absichtlich daneben. Ein Glucksen. */
    private fun trixie(): FloatArray = Synth.normalize(
        Synth.concat(
            Synth.tone(0.09f, { note(16) }, Synth.pluck(decay = 20f, peak = 0.5f), chime),
            // Ein Halbton zu hoch — genau das macht es schelmisch statt hübsch.
            Synth.tone(0.24f, { note(20) }, Synth.pluck(decay = 12f, peak = 0.55f), chime),
        ),
        target = 0.68f,
    )
}
