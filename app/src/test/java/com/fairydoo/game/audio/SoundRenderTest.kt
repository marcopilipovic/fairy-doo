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
    fun `das Ueberblenden schliesst die Schleifennaht`() {
        // Ein Sägezahn mit hartem Sprung am Ende: Ohne Überblenden stünde dort
        // ein Knacks, mit Überblenden geht der Schluss in den Anfang über.
        val period = Synth.SAMPLE_RATE / 220
        val raw = ShortArray(Synth.SAMPLE_RATE * 2) { index ->
            ((index % period).toFloat() / period * 20_000 - 10_000).toInt().toShort()
        }

        val looped = Synth.crossfadeLoop(raw, seconds = 0.2f)

        assertTrue("Die Schleife wurde nicht gekürzt", looped.size < raw.size)

        // Der Sprung vom letzten zum ersten Abtastwert muss kleiner sein als
        // der eines vollen Sägezahn-Zyklus.
        val naht = kotlin.math.abs(looped.first().toInt() - looped.last().toInt())
        assertTrue("Die Naht springt zu weit ($naht)", naht < 12_000)
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
