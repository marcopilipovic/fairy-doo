package com.fairydoo.game.audio

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
        val sounds = buildMap {
            repeat(FairySounds.GIGGLE_VARIANTS) { variant ->
                put("kichern-$variant", FairySounds.giggle(variant))
            }
            put("schreck", FairySounds.yelp())
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
    fun `die Kicher-Varianten unterscheiden sich`() {
        val lengths = (0 until FairySounds.GIGGLE_VARIANTS).map { FairySounds.giggle(it).size }

        assertTrue(
            "Alle Varianten sind gleich lang — dann klingen sie vermutlich gleich",
            lengths.toSet().size > 1,
        )
    }

    /** Schreibt 16-Bit-Mono-PCM als WAV. */
    private fun writeWav(file: File, samples: FloatArray) {
        val pcm = Synth.toPcm16(samples)
        val dataBytes = pcm.size * 2
        val buffer = ByteBuffer.allocate(44 + dataBytes).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataBytes)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)                       // Länge des Format-Blocks
        buffer.putShort(1)                      // PCM, unkomprimiert
        buffer.putShort(1)                      // Mono
        buffer.putInt(Synth.SAMPLE_RATE)
        buffer.putInt(Synth.SAMPLE_RATE * 2)    // Bytes pro Sekunde
        buffer.putShort(2)                      // Bytes pro Frame
        buffer.putShort(16)                     // Bits pro Sample
        buffer.put("data".toByteArray())
        buffer.putInt(dataBytes)
        pcm.forEach { buffer.putShort(it) }

        file.writeBytes(buffer.array())
    }
}
