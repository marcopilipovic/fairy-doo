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

    /**
     * Sinus-Wertetabelle.
     *
     * Ein Klang von 24 Sekunden mit drei Obertönen sind über drei Millionen
     * Sinus-Aufrufe; für die ganze Klangwelt kommen zweistellige Millionen
     * zusammen. Auf einem langsamen Gerät dauerte das gut eine halbe Minute,
     * und so lange blieb das Spiel stumm. Die Tabelle mit linearer
     * Zwischenwertbildung ist um ein Vielfaches schneller, und ihr Fehler liegt
     * weit unter der Hörschwelle.
     */
    private const val TABLE_BITS = 13
    private const val TABLE_SIZE = 1 shl TABLE_BITS
    private const val TABLE_MASK = TABLE_SIZE - 1

    private val sineTable = FloatArray(TABLE_SIZE) { index ->
        sin(2.0 * PI * index / TABLE_SIZE).toFloat()
    }

    /** @param turns Phase in Umdrehungen: 1.0 ist eine volle Schwingung. */
    private fun tableSin(turns: Float): Float {
        val scaled = turns * TABLE_SIZE
        val index = scaled.toInt()
        val fraction = scaled - index
        val a = sineTable[index and TABLE_MASK]
        val b = sineTable[index + 1 and TABLE_MASK]
        return a + (b - a) * fraction
    }

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

        // Phasen in Umdrehungen statt Radiant und auf 0..1 gehalten: Über
        // Zehntausende Schwingungen würde ein stetig wachsender Winkel die
        // Genauigkeit einer Fließkommazahl aufbrauchen und der Ton verstimmte
        // sich hörbar.
        val phases = FloatArray(harmonics.size)
        var vibratoPhase = 0f
        val vibratoStep = vibratoHz / SAMPLE_RATE

        for (index in 0 until length) {
            val progress = index.toFloat() / length

            val vibrato = if (vibratoHz > 0f) {
                vibratoPhase += vibratoStep
                if (vibratoPhase >= 1f) vibratoPhase -= 1f
                1f + vibratoDepth * tableSin(vibratoPhase)
            } else {
                1f
            }
            val baseFrequency = frequencyAt(progress) * vibrato

            var value = 0f
            harmonics.forEachIndexed { harmonicIndex, (multiple, strength) ->
                var phase = phases[harmonicIndex] + baseFrequency * multiple / SAMPLE_RATE
                if (phase >= 1f) phase -= phase.toInt()
                phases[harmonicIndex] = phase
                value += strength * tableSin(phase)
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

    /**
     * Anschlag mit exponentiellem Ausklang — für Glocken und Funken.
     *
     * [attack] ist ein Anteil der Gesamtdauer, nicht eine Zeit: Der Aufrufer
     * kennt seine Dauer und rechnet um. Bei einem Hundertstel setzt der Ton
     * praktisch sofort ein — das hört man als Klick, und ein Klick zieht
     * Aufmerksamkeit auf sich, ganz gleich wie leise er ist. Wer einen Ton
     * unauffälliger haben will, dreht zuerst hier und erst danach an der
     * Lautstärke: Ein Ton, der in zehn Millisekunden aufblüht statt in einer,
     * verschwindet im Hintergrund, bleibt aber genauso gut hörbar.
     */
    fun pluck(decay: Float = 6f, peak: Float = 1f, attack: Float = 0.01f): (Float) -> Float =
        { progress ->
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

    /**
     * Mischt Klänge in eine Schleife fester Länge — was hinten übersteht, läuft
     * vorne weiter.
     *
     * Bis hierher wurde die Naht überblendet: Schluss über Anfang, Überhang
     * abgeschnitten. Das nahm dem Übergang das Knacken, aber nicht das
     * Stolpern — an der Naht schoben sich weiterhin zwei unzusammenhängende
     * Takte ineinander. Hier gibt es gar keine Naht: Ein Akkord, der bei
     * Sekunde 30 einer 32-Sekunden-Schleife anfängt, klingt bei Sekunde 0
     * weiter aus. Die Schleife schließt sich, weil sie nie geöffnet war.
     *
     * Das geht nur bei berechneten Klängen. Eine Aufnahme kann man nicht um
     * ihre eigene Länge herumfalten — deshalb braucht sie das Überblenden.
     */
    fun mixLooping(loopSeconds: Float, vararg layers: Pair<Float, FloatArray>): FloatArray {
        val length = secondsToSamples(loopSeconds)
        val output = FloatArray(length)

        for ((offsetSeconds, samples) in layers) {
            var cursor = secondsToSamples(offsetSeconds) % length
            if (cursor < 0) cursor += length
            for (value in samples) {
                output[cursor] += value
                cursor++
                if (cursor == length) cursor = 0
            }
        }
        return output
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
