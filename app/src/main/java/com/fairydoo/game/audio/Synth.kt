package com.fairydoo.game.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Ein kleiner Klangsynthesizer.
 *
 * Das Spiel bringt **keine** Audiodateien mit — jeder Ton wird beim Start
 * berechnet. Das hält die App klein, macht jede Stimmlage nachträglich
 * änderbar (eine Zahl statt einer neuen Aufnahme) und erspart die Klärung von
 * Lizenzen für fremde Samples.
 *
 * Reines Kotlin ohne Android-Abhängigkeiten: Die Klänge lassen sich damit im
 * Unit-Test erzeugen und als WAV zum Anhören ausgeben.
 */
object Synth {

    const val SAMPLE_RATE = 44_100

    fun secondsToSamples(seconds: Float): Int = (seconds * SAMPLE_RATE).toInt()

    /** Leerer Puffer der angegebenen Länge. */
    fun silence(seconds: Float) = FloatArray(secondsToSamples(seconds))

    /**
     * Ein Ton mit über die Zeit veränderlicher Tonhöhe und Lautstärke.
     *
     * Die Frequenz wird als Verlauf übergeben und phasenrichtig integriert —
     * ohne das würde ein Gleiten in der Tonhöhe bei jedem Sample einen Sprung
     * in der Wellenform erzeugen und hörbar knacken.
     *
     * @param harmonics Obertöne als Vielfache der Grundfrequenz mit ihrer
     *   jeweiligen Stärke. Erst sie geben dem Ton einen Charakter: reine
     *   Sinustöne klingen wie ein Hörtest.
     */
    fun tone(
        durationSeconds: Float,
        frequencyAt: (progress: Float) -> Float,
        amplitudeAt: (progress: Float) -> Float,
        harmonics: List<Pair<Float, Float>> = listOf(1f to 1f),
        vibratoHz: Float = 0f,
        vibratoDepth: Float = 0f,
    ): FloatArray {
        val length = secondsToSamples(durationSeconds)
        val output = FloatArray(length)
        val phases = FloatArray(harmonics.size)

        for (index in 0 until length) {
            val progress = index.toFloat() / length
            val time = index.toFloat() / SAMPLE_RATE

            val vibrato = if (vibratoHz > 0f) {
                1f + vibratoDepth * sin(2f * PI.toFloat() * vibratoHz * time)
            } else {
                1f
            }
            val baseFrequency = frequencyAt(progress) * vibrato

            var value = 0f
            harmonics.forEachIndexed { harmonicIndex, (multiple, strength) ->
                phases[harmonicIndex] += 2f * PI.toFloat() * baseFrequency * multiple / SAMPLE_RATE
                value += strength * sin(phases[harmonicIndex])
            }

            output[index] = value * amplitudeAt(progress)
        }
        return output
    }

    /** Hüllkurve mit weichem Ein- und Ausklang; verhindert Knacken an den Rändern. */
    fun envelope(
        attack: Float = 0.02f,
        release: Float = 0.3f,
        peak: Float = 1f,
    ): (Float) -> Float = { progress ->
        when {
            progress < attack -> peak * (progress / attack)
            progress > 1f - release -> peak * ((1f - progress) / release)
            else -> peak
        }
    }

    /** Anschlag mit exponentiellem Ausklang — für Glocken und Funken. */
    fun pluck(decay: Float = 6f, peak: Float = 1f): (Float) -> Float = { progress ->
        val attack = 0.01f
        if (progress < attack) {
            peak * (progress / attack)
        } else {
            peak * exp(-decay * progress)
        }
    }

    /** Legt Klänge übereinander; [offsetSeconds] verschiebt sie auf der Zeitachse. */
    fun mix(vararg layers: Pair<Float, FloatArray>): FloatArray {
        val totalLength = layers.maxOfOrNull { (offset, samples) ->
            secondsToSamples(offset) + samples.size
        } ?: 0
        val output = FloatArray(totalLength)

        for ((offsetSeconds, samples) in layers) {
            val start = secondsToSamples(offsetSeconds)
            for (index in samples.indices) {
                output[start + index] += samples[index]
            }
        }
        return output
    }

    /** Hängt Klänge hintereinander. */
    fun concat(vararg parts: FloatArray): FloatArray {
        val output = FloatArray(parts.sumOf { it.size })
        var cursor = 0
        for (part in parts) {
            part.copyInto(output, cursor)
            cursor += part.size
        }
        return output
    }

    /**
     * Begrenzt den Pegel auf [target].
     *
     * Weiches Begrenzen statt hartem Abschneiden: Beim Übereinanderlegen
     * mehrerer Stimmen entstehen sonst Spitzen, die als Knacken hörbar sind.
     */
    fun normalize(samples: FloatArray, target: Float = 0.85f): FloatArray {
        val peak = samples.maxOfOrNull { kotlin.math.abs(it) } ?: 0f
        if (peak <= 0.0001f) return samples
        val factor = target / peak
        return FloatArray(samples.size) { samples[it] * factor }
    }

    /** Blendet Anfang und Ende aus, damit ein Loop nahtlos schließt. */
    fun fadeEdges(samples: FloatArray, seconds: Float): FloatArray {
        val fadeLength = secondsToSamples(seconds).coerceAtMost(samples.size / 2)
        if (fadeLength <= 0) return samples

        val output = samples.copyOf()
        for (index in 0 until fadeLength) {
            val factor = index.toFloat() / fadeLength
            output[index] *= factor
            output[output.size - 1 - index] *= factor
        }
        return output
    }

    /** Wandelt in 16-Bit-PCM, wie AudioTrack es erwartet. */
    fun toPcm16(samples: FloatArray): ShortArray = ShortArray(samples.size) { index ->
        (samples[index].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
    }

    /**
     * Verpackt die Samples als WAV (16 Bit, Mono).
     *
     * Wird zweifach gebraucht: Zur Laufzeit legt die App die berechneten Klänge
     * als WAV im Cache ab, damit SoundPool sie laden kann; im Test dient
     * dasselbe Format zum Anhören.
     */
    fun toWavBytes(samples: FloatArray): ByteArray {
        val pcm = toPcm16(samples)
        val dataBytes = pcm.size * 2
        val buffer = java.nio.ByteBuffer.allocate(WAV_HEADER_BYTES + dataBytes)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataBytes)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)                 // Länge des Format-Blocks
        buffer.putShort(1)                // PCM, unkomprimiert
        buffer.putShort(1)                // Mono
        buffer.putInt(SAMPLE_RATE)
        buffer.putInt(SAMPLE_RATE * 2)    // Bytes pro Sekunde
        buffer.putShort(2)                // Bytes pro Frame
        buffer.putShort(16)               // Bits pro Sample
        buffer.put("data".toByteArray())
        buffer.putInt(dataBytes)
        pcm.forEach { buffer.putShort(it) }

        return buffer.array()
    }

    private const val WAV_HEADER_BYTES = 44

    /** Zufallszahl in einem Bereich — für die Streuung zwischen Klangvarianten. */
    fun Random.between(min: Float, max: Float): Float = min + nextFloat() * (max - min)
}
