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

        /**
         * Wald oder Meer?
         *
         * **Diese Prüfung ist die zweite.** Die erste maß das Verhältnis von
         * Spitze zu mittlerer Lautheit und ließ eine Fassung durch, die
         * weiterhin nach Brandung klang: Sie kam auf 7 gegenüber 5,9 vorher —
         * ein Unterschied, der nichts bewies. Die Zahl war einfach das falsche
         * Maß.
         *
         * Was Wald von See unterscheidet, ist **Stille**. Die See hört nie auf;
         * ein Wald hat zwischen zwei Böen Abschnitte, in denen fast nichts
         * passiert. Also wird das gemessen: Wie viel der Zeit liegt deutlich
         * unter der mittleren Lautheit?
         *
         * Beim Entwurf, der nach Meer klang, waren es **null Prozent**. Die
         * Grundschicht deckte jede Lücke zu. Ausgemessen wurde dann die Reihe
         * 0,30 → 0 %, 0,15 → 22 %, 0,12 → 57 %, 0,08 → 77 %; bei 0,08 wirkt der
         * Wald tot, gewählt ist 0,12.
         *
         * Das beweist nicht, dass es nach Wald klingt — das kann kein Test.
         * Aber es fängt den Rückfall ins Dauerrauschen, und der ist beim
         * Nachjustieren der wahrscheinlichste Fehler.
         */
        val wald2 = entwuerfe.getValue("waldrauschen")
        val lautheit = sqrt(wald2.sumOf { (it * it).toDouble() } / wald2.size).toFloat()
        val block = Synth.SAMPLE_RATE / 20
        val bloecke = wald2.toList().chunked(block).filter { it.size == block }
        val ruhig = bloecke.count { teil ->
            sqrt(teil.sumOf { (it * it).toDouble() } / block) < 0.35 * lautheit
        }
        val anteil = 100 * ruhig / bloecke.size
        println("  Wald: ruhige Abschnitte $anteil %")
        assertTrue(
            "Der Wald rauscht durchgehend und klingt wieder nach Meer " +
                "($anteil % ruhig, erwartet mindestens 30)",
            anteil >= 30,
        )

        println("KLANGENTWÜRFE in ${ziel.absolutePath}")
        ziel.listFiles()?.sortedBy { it.name }?.forEach {
            println("  ${it.name} — ${it.length() / 1024} KB")
        }
    }
}
