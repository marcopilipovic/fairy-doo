package com.fairydoo.game.audio

import com.fairydoo.game.game.FairySpecies
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die zehn Feenmotive — geprüft und zum Anhören herausgeschrieben.
 *
 * Wie in [SoundRenderTest]: Ob ein Motiv *schön* ist, kann kein Test sagen. Ob
 * es klingt, kurz genug ist und nicht übersteuert, schon. Die WAV-Dateien
 * landen unter `app/build/sounds/feen/` und lassen sich einzeln anhören —
 * schneller als jede Runde im Spiel.
 */
class FairyMotifTest {

    private val outputDir = File("build/sounds/feen").apply { mkdirs() }

    @Test
    fun `jede Fee hat ein hoerbares Motiv und es wird als WAV geschrieben`() {
        FairySpecies.entries.forEachIndexed { index, species ->
            val samples = FairyMotifs.of(species)
            val peak = samples.maxOf { abs(it) }
            val rms = sqrt(samples.sumOf { (it * it).toDouble() } / samples.size).toFloat()

            assertTrue("${species.displayName} ist stumm", peak > 0.05f)
            assertTrue("${species.displayName} übersteuert (Spitze $peak)", peak <= 1.0f)
            assertTrue("${species.displayName} ist praktisch leer (RMS $rms)", rms > 0.004f)

            // Durchnummeriert, damit die Dateien in der Reihenfolge der Feen
            // liegen statt alphabetisch.
            val name = "%02d-%s".format(index + 1, species.displayName.lowercase())
            File(outputDir, "$name.wav").writeBytes(Synth.toWavBytes(samples))
        }
    }

    /**
     * Kurz genug, um sich nicht zu überlagern.
     *
     * Beim schnellen Setzen mehrerer Feen erklingen die Motive dicht
     * hintereinander. Alles über einer halben Sekunde wird dabei zu Matsch —
     * das war schon bei den gesprochenen Ausrufen das Problem.
     */
    @Test
    fun `kein Motiv ist laenger als eine halbe Sekunde`() {
        FairySpecies.entries.forEach { species ->
            val seconds = FairyMotifs.of(species).size.toFloat() / Synth.SAMPLE_RATE
            assertTrue(
                "${species.displayName} dauert ${"%.2f".format(seconds)} s",
                seconds <= 0.6f,
            )
        }
    }

    /**
     * Keine zwei Feen klingen gleich.
     *
     * Vorher unterschieden sich die Ausrufe nur durch eine Tonhöhen-Zahl auf
     * derselben Systemstimme — man hörte zehnmal dieselbe Stimme. Hier wird
     * grob geprüft, dass sich Länge oder Klangschwerpunkt unterscheiden.
     */
    @Test
    fun `die Motive unterscheiden sich hoerbar voneinander`() {
        val fingerprints = FairySpecies.entries.map { species ->
            val samples = FairyMotifs.of(species)
            val length = samples.size
            // Nulldurchgänge je Sekunde sind ein grobes Maß für die Tonhöhe.
            val crossings = (1 until length).count {
                samples[it - 1] < 0f && samples[it] >= 0f
            } * Synth.SAMPLE_RATE / length
            species.displayName to (length / 2205 to crossings / 40)
        }

        val duplicates = fingerprints
            .groupBy { it.second }
            .filterValues { it.size > 1 }
            .map { (_, group) -> group.joinToString(" / ") { it.first } }

        assertTrue("Klingen zu ähnlich: $duplicates", duplicates.isEmpty())
    }
}
