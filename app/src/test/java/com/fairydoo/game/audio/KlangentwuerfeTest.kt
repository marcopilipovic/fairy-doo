package com.fairydoo.game.audio

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Schreibt die beiden Entwürfe als WAV zum Anhören heraus.
 *
 * Sie ersetzen noch nichts — das Spiel spielt weiterhin die Dateien aus
 * `res/raw`. Erst wenn Nataly sie gehört und für gut befunden hat, wandern sie
 * nach [FairySounds].
 *
 * Die Dateien landen unter `app/build/klangentwuerfe/`.
 */
class KlangentwuerfeTest {

    private val ziel = File("build/klangentwuerfe").apply { mkdirs() }

    @Test
    fun `Entwuerfe schreiben und pruefen`() {
        val entwuerfe = mapOf(
            "aufschrei" to Klangentwuerfe.aufschrei(),
            "waldrauschen" to Klangentwuerfe.waldrauschen(),
        )

        for ((name, samples) in entwuerfe) {
            val spitze = samples.maxOf { abs(it) }
            val rms = sqrt(samples.sumOf { (it * it).toDouble() } / samples.size).toFloat()

            assertTrue("$name ist stumm", spitze > 0.05f)
            assertTrue("$name übersteuert (Spitze $spitze)", spitze <= 1.0f)
            assertTrue("$name ist praktisch leer (RMS $rms)", rms > 0.005f)

            File(ziel, "$name.wav").writeBytes(Synth.toWavBytes(samples))
        }

        /**
         * Die Naht der Schleife.
         *
         * Ein Ambiente wird endlos wiederholt; springt der Pegel an der
         * Nahtstelle, hört man bei jedem Durchlauf ein Knacken. Geprüft wird
         * deshalb, dass der letzte Abtastwert nahe genug am ersten liegt —
         * genau das leistet das Überblenden in [Synth.crossfadeLoop], und genau
         * das würde beim Ändern der Klangschichten leicht wieder kaputtgehen.
         */
        val wald = entwuerfe.getValue("waldrauschen")
        val sprung = abs(wald.last() - wald.first())
        assertTrue("Die Schleife hat eine hörbare Naht (Sprung $sprung)", sprung < 0.08f)

        println("KLANGENTWÜRFE in ${ziel.absolutePath}")
        ziel.listFiles()?.sortedBy { it.name }?.forEach {
            println("  ${it.name} — ${it.length() / 1024} KB")
        }
    }
}
