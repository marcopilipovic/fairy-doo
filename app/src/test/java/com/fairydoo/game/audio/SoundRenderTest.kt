package com.fairydoo.game.audio

import com.fairydoo.game.game.FairySpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Prüft die erzeugten Klänge und schreibt sie zum Anhören als WAV heraus.
 *
 * Ob ein Kichern *schön* klingt, kann kein Test beantworten — ob es überhaupt
 * klingt, schon: Ein Fehler in der Synthese liefert typischerweise Stille,
 * Dauerübersteuerung oder eine falsche Länge, und genau darauf wird geprüft.
 *
 * Die WAV-Dateien landen unter `app/build/sounds/` und lassen sich mit jedem
 * Abspielprogramm öffnen — der schnellste Weg, eine Klangänderung zu beurteilen,
 * ohne die App zu starten.
 */
class SoundRenderTest {

    private val outputDir = File("build/sounds").apply { mkdirs() }

    @Test
    fun `alle Klaenge sind hoerbar und uebersteuern nicht`() {
        // Der Aufschrei ist eine Aufnahme und daher nicht Teil dieser Prüfung
        // — hier geht es nur um die berechneten Klänge.
        val sounds = buildMap {
            put("jubel", FairySounds.cheer())
            put("feenstaub", FairySounds.sparkle())
            put("natur-schild", FairySounds.shield())
            put("zeiten-bluete", FairySounds.timeFreeze())
            put("merkzeichen", FairySounds.tick())
            put("ruecknahme", FairySounds.undo())
            put("spielende", FairySounds.gameOver())
        }

        for ((name, samples) in sounds) {
            val peak = samples.maxOf { abs(it) }
            val rms = sqrt(samples.sumOf { (it * it).toDouble() } / samples.size).toFloat()

            assertTrue("$name ist stumm", peak > 0.05f)
            assertTrue("$name übersteuert (Spitze $peak)", peak <= 1.0f)
            assertTrue("$name ist praktisch leer (RMS $rms)", rms > 0.005f)

            writeWav(File(outputDir, "$name.wav"), samples)
        }

        println("Klänge geschrieben nach: ${outputDir.absolutePath}")
    }

    @Test
    fun `jede Fee hat ihren eigenen Ton`() {
        val toene = FairySpecies.entries.associateWith { FairyChimes.render(it) }

        for ((species, samples) in toene) {
            val peak = samples.maxOf { abs(it) }
            assertTrue("${species.displayName} ist stumm", peak > 0.05f)
            assertTrue("${species.displayName} übersteuert (Spitze $peak)", peak <= 1.0f)
            writeWav(File(outputDir, "fee-${species.name.lowercase()}.wav"), samples)
        }

        // Zwei Feen mit derselben Tonhöhe wären am Klang nicht auseinander-
        // zuhalten — und die Zuordnung entsteht von Hand.
        val hoehen = FairySpecies.entries.map { FairyChimes.of(it).hertz }
        assertEquals("Zwei Feen teilen sich eine Tonhöhe", hoehen.size, hoehen.toSet().size)

        // Sie sollen sich außerdem deutlich unterscheiden, nicht nur messbar:
        // Ein Halbton Abstand hörte man beim Spielen nicht heraus.
        val sortiert = hoehen.sorted()
        for (i in 1 until sortiert.size) {
            val verhaeltnis = sortiert[i] / sortiert[i - 1]
            assertTrue(
                "Zwei Tonhöhen liegen zu dicht beieinander ($verhaeltnis)",
                verhaeltnis > 1.05f,
            )
        }
    }

    @Test
    fun `beide Musikstuecke klingen und schliessen sich ohne Naht`() {
        val stuecke = mapOf(
            "musik-wald" to Music.forestLoop(),
            "musik-feenpfad" to Music.pathLoop(),
        )

        for ((name, samples) in stuecke) {
            val peak = samples.maxOf { abs(it) }
            val rms = sqrt(samples.sumOf { (it * it).toDouble() } / samples.size).toFloat()

            assertTrue("$name ist stumm", peak > 0.05f)
            assertTrue("$name übersteuert (Spitze $peak)", peak <= 1.0f)
            assertTrue("$name ist zu leise (RMS $rms)", rms > 0.02f)

            // Die eigentliche Prüfung: Der Sprung von der letzten auf die erste
            // Probe muss in derselben Größenordnung liegen wie ein gewöhnlicher
            // Schritt mitten im Stück. Ist er das, gibt es an der Naht nichts
            // zu hören — und genau daran krankte die Aufnahme vorher.
            val schritte = (1 until samples.size).map { abs(samples[it] - samples[it - 1]) }
            val ueblich = schritte.sorted()[schritte.size * 99 / 100]
            val naht = abs(samples.first() - samples.last())

            assertTrue(
                "$name springt an der Naht ($naht gegen sonst höchstens $ueblich)",
                naht <= ueblich * 2f,
            )

            writeWav(File(outputDir, "$name.wav"), samples)
            println("$name: ${"%.1f".format(samples.size / Synth.SAMPLE_RATE.toFloat())} s, " +
                "Naht $naht, sonst bis $ueblich")
        }
    }

    /** Nutzt dieselbe WAV-Erzeugung wie die App, damit beides nicht auseinanderläuft. */
    private fun writeWav(file: File, samples: FloatArray) {
        file.writeBytes(Synth.toWavBytes(samples))
    }
}
