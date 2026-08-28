package com.fairydoo.game.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.annotation.RawRes
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Wandelt eine Musikdatei in rohe Abtastwerte.
 *
 * Klingt nach Umweg — ein `MediaPlayer` könnte die MP3 direkt abspielen und
 * sogar wiederholen. Aber MP3 speichert am Anfang und Ende jeder Datei etwas
 * Stille als kodierungsbedingten Vorlauf; beim Wiederholen entsteht daraus eine
 * hörbare Lücke, und genau das soll eine Endlosschleife nicht haben. Mit den
 * Rohdaten kann [android.media.AudioTrack] dagegen exakt an der gewünschten
 * Stelle zurückspringen.
 *
 * Ergebnis ist immer Mono: Die Musik wird ohnehin einkanalig ausgegeben, und
 * das Zusammenlegen hier spart den halben Zwischenspeicher.
 */
object MusicDecoder {

    /** Höchstdauer, damit ein unerwartet langer Titel nicht den Speicher füllt. */
    private const val MAX_SECONDS = 180

    fun decodeToMono(context: Context, @RawRes resId: Int): ShortArray {
        val extractor = MediaExtractor()
        val descriptor = context.resources.openRawResourceFd(resId)

        try {
            descriptor.use {
                extractor.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }

            val trackIndex = (0 until extractor.trackCount).first { index ->
                extractor.getTrackFormat(index)
                    .getString(MediaFormat.KEY_MIME)
                    ?.startsWith("audio/") == true
            }
            extractor.selectTrack(trackIndex)

            val format = extractor.getTrackFormat(trackIndex)
            val mime = requireNotNull(format.getString(MediaFormat.KEY_MIME))
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            try {
                return drain(extractor, codec, channels)
            } finally {
                codec.stop()
                codec.release()
            }
        } finally {
            extractor.release()
        }
    }

    /** Füttert den Dekodierer und sammelt ein, was herauskommt. */
    private fun drain(
        extractor: MediaExtractor,
        codec: MediaCodec,
        channels: Int,
    ): ShortArray {
        val info = MediaCodec.BufferInfo()
        val samples = ArrayList<ShortArray>()
        var total = 0
        val limit = MAX_SECONDS * Synth.SAMPLE_RATE

        var inputDone = false
        var outputDone = false

        while (!outputDone && total < limit) {
            if (!inputDone) {
                val inputIndex = codec.dequeueInputBuffer(TIMEOUT_MICROS)
                if (inputIndex >= 0) {
                    val buffer = requireNotNull(codec.getInputBuffer(inputIndex))
                    val size = extractor.readSampleData(buffer, 0)
                    if (size < 0) {
                        codec.queueInputBuffer(
                            inputIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                        )
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(inputIndex, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            val outputIndex = codec.dequeueOutputBuffer(info, TIMEOUT_MICROS)
            if (outputIndex >= 0) {
                if (info.size > 0) {
                    val buffer = requireNotNull(codec.getOutputBuffer(outputIndex))
                    buffer.position(info.offset)
                    buffer.limit(info.offset + info.size)

                    val chunk = toMono(buffer, channels)
                    samples += chunk
                    total += chunk.size
                }
                codec.releaseOutputBuffer(outputIndex, false)

                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
            }
        }

        val result = ShortArray(total)
        var cursor = 0
        for (chunk in samples) {
            chunk.copyInto(result, cursor)
            cursor += chunk.size
        }
        return result
    }

    /** Legt mehrkanaliges Material auf einen Kanal zusammen. */
    private fun toMono(buffer: ByteBuffer, channels: Int): ShortArray {
        val shorts = buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val frames = shorts.remaining() / channels
        val mono = ShortArray(frames)

        for (frame in 0 until frames) {
            var sum = 0
            for (channel in 0 until channels) {
                sum += shorts.get(frame * channels + channel).toInt()
            }
            mono[frame] = (sum / channels).toShort()
        }
        return mono
    }

    private const val TIMEOUT_MICROS = 10_000L
}
