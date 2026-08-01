package com.fairydoo.game.audio

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
        // Kichern und Aufschrei sind Aufnahmen und daher nicht Teil dieser
        // Prüfung — hier geht es nur um die berechneten Klänge.
        val sounds = buildMap {
            put("jubel", FairySounds.cheer())
            put("feenstaub", FairySounds.sparkle())
            put("natur-schild", FairySounds.shield())
            put("zeiten-bluete", FairySounds.timeFreeze())
            put("merkzeichen", FairySounds.tick())
            put("ruecknahme", FairySounds.undo())
            put("spielende", FairySounds.gameOver())
            put("musik-schleife", FairySounds.ambientLoop())
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
    fun `die Musikschleife schliesst ohne Sprung`() {
        val loop = FairySounds.ambientLoop(seconds = 12f)

        // An der Nahtstelle darf kein Pegelsprung stehen, sonst klickt es bei
        // jedem Durchlauf hörbar.
        val start = loop.take(64).maxOf { abs(it) }
        val end = loop.takeLast(64).maxOf { abs(it) }

        assertTrue("Der Schleifenanfang ist zu laut ($start)", start < 0.02f)
        assertTrue("Das Schleifenende ist zu laut ($end)", end < 0.02f)
    }

    @Test
    fun `fuer jede Kicher-Variante gibt es eine Aufnahme`() {
        // Die Ereignis-Zuordnung rechnet modulo dieser Zahl; stimmt sie nicht
        // mit der Zahl der Dateien überein, bliebe eine Fee stumm.
        assertEquals(FairyClips.GIGGLE_COUNT, FairyClips.giggles.size)
    }

    /** Nutzt dieselbe WAV-Erzeugung wie die App, damit beides nicht auseinanderläuft. */
    private fun writeWav(file: File, samples: FloatArray) {
        file.writeBytes(Synth.toWavBytes(samples))
    }
}
