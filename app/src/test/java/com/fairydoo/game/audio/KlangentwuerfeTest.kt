package com.fairydoo.game.audio

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Schreibt die Entwürfe als WAV zum Anhören heraus.
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
            // Die Waldstimmung ist abgenommen und steht in FairySounds. Sie
            // wird hier trotzdem mitgeschrieben — zum Gegenhören nach jeder
            // Änderung, und weil die Prüfungen darunter an ihr hängen.
            "waldstimmung" to FairySounds.waldstimmung(),
        )

        for ((name, samples) in entwuerfe) {
            val spitze = samples.maxOf { abs(it) }
            val rms = sqrt(samples.sumOf { (it * it).toDouble() } / samples.size).toFloat()

            assertTrue("$name ist stumm", spitze > 0.05f)
            assertTrue("$name übersteuert (Spitze $spitze)", spitze <= 1.0f)
            assertTrue("$name ist praktisch leer (RMS $rms)", rms > 0.005f)

            File(ziel, "$name.wav").writeBytes(Synth.toWavBytes(samples))
        }

        val wald = entwuerfe.getValue("waldstimmung")

        /**
         * Die Naht der Schleife.
         *
         * Ein Ambiente wird endlos wiederholt; springt der Pegel an der
         * Nahtstelle, hört man bei jedem Durchlauf ein Knacken.
         *
         * Hier schließt sie ohne Überblendung: Jede Stimme hat eine Frequenz in
         * ganzen Hertz und eine Schwellung mit ganzzahliger Anzahl Durchläufe,
         * bei einer Länge in ganzen Sekunden steht am Ende also genau dasselbe
         * wie am Anfang. Diese Prüfung hält fest, dass das so bleibt — es geht
         * verloren, sobald jemand eine krumme Frequenz einträgt.
         */
        val naht = abs(wald.last() - wald.first())
        assertTrue("Die Schleife hat eine hörbare Naht (Sprung $naht)", naht < 0.02f)

        /**
         * Wie viel Rauschen steckt noch drin?
         *
         * **Die dritte Prüfung an dieser Stelle, und die ersten beiden waren
         * die falschen.** Erst maß ich Spitze zu mittlerer Lautheit — die Zahl
         * bewegte sich kaum, obwohl das Ergebnis noch nach Brandung klang. Dann
         * den Anteil ruhiger Abschnitte — der fand zwar den zugedeckten Wald,
         * sagte aber nichts darüber, ob das Übrige rauscht oder klingt.
         *
         * Nataly hat zweimal dasselbe bemängelt: zu viel Rauschen. Also wird
         * genau das gemessen und nichts anderes. Rauschen springt von einem
         * Abtastwert zum nächsten weit; ein getragener Ton wandert. Der mittlere
         * Sprung im Verhältnis zur Lautheit trennt beides zuverlässig — weißes
         * Rauschen liegt bei etwa 1,4, ein reiner Sinuston nahe null.
         *
         * Der Grenzwert lässt Vogelrufe und Obertöne zu, schlägt aber an,
         * sobald wieder eine Rauschschicht hineingerät.
         */
        val lautheit = sqrt(wald.sumOf { (it * it).toDouble() } / wald.size).toFloat()
        val sprung = (1 until wald.size)
            .sumOf { abs(wald[it] - wald[it - 1]).toDouble() } / (wald.size - 1)
        val rauschanteil = (sprung / lautheit).toFloat()
        println("  Wald: Rauschanteil %.3f, Naht %.4f".format(rauschanteil, naht))
        assertTrue(
            "Es ist wieder Rauschen im Wald (Anteil $rauschanteil, erlaubt bis 0,3)",
            rauschanteil < 0.3f,
        )

        println("KLANGENTWÜRFE in ${ziel.absolutePath}")
        ziel.listFiles()?.sortedBy { it.name }?.forEach {
            println("  ${it.name} — ${it.length() / 1024} KB")
        }
    }
}
