package com.fairydoo.game.art

import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater

/**
 * Ein winziger PNG-Schreiber für die Sprite-Vorschau.
 *
 * Warum von Hand statt mit `javax.imageio`: In Android-Unit-Tests fehlt
 * `java.awt` — `android.jar` steht im Compile-Pfad und bringt die
 * Desktop-Grafikklassen nicht mit. `java.util.zip` ist dagegen vorhanden, und
 * PNG ist im unkomprimierten Farbmodus schnell erklärt: Signatur, ein
 * Kopf-Block, die zlib-gepackten Bildzeilen, ein Endblock.
 */
internal class Canvas(val width: Int, val height: Int, background: Int = 0) {

    /** Bildpunkte als ARGB, zeilenweise. */
    val pixels = IntArray(width * height) { background }

    fun set(x: Int, y: Int, argb: Int) {
        if (x in 0 until width && y in 0 until height) pixels[y * width + x] = argb
    }

    fun fillRect(left: Int, top: Int, w: Int, h: Int, argb: Int) {
        for (y in top until top + h) {
            for (x in left until left + w) set(x, y, argb)
        }
    }

    /** Nur die Umrisslinie — für die Zonenränder der Brett-Vorschau. */
    fun strokeRect(left: Int, top: Int, w: Int, h: Int, argb: Int, thickness: Int = 1) {
        repeat(thickness) { offset ->
            for (x in left + offset until left + w - offset) {
                set(x, top + offset, argb)
                set(x, top + h - 1 - offset, argb)
            }
            for (y in top + offset until top + h - offset) {
                set(left + offset, y, argb)
                set(left + w - 1 - offset, y, argb)
            }
        }
    }

    /** Zeichnet ein Sprite pixelweise, um [scale] vergrößert. */
    fun drawSprite(sprite: PixelSprite, frame: Int, left: Int, top: Int, scale: Int) {
        for (y in 0 until SPRITE_SIZE) {
            for (x in 0 until SPRITE_SIZE) {
                val argb = sprite.colorAt(frame, x, y)
                if (argb == 0) continue
                fillRect(left + x * scale, top + y * scale, scale, scale, argb)
            }
        }
    }

    /** Kodiert das Bild als PNG (8 Bit, RGBA). */
    fun toPng(): ByteArray {
        val raw = ByteArrayOutputStream()
        for (y in 0 until height) {
            raw.write(0) // Filtertyp „keiner" — bei diesen Bildgrößen unnötig
            for (x in 0 until width) {
                val argb = pixels[y * width + x]
                raw.write(argb shr 16 and 0xFF)
                raw.write(argb shr 8 and 0xFF)
                raw.write(argb and 0xFF)
                raw.write(argb ushr 24 and 0xFF)
            }
        }

        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(), 13, 10, 26, 10))

        val header = ByteArrayOutputStream().apply {
            writeInt(width)
            writeInt(height)
            write(8)  // Bit-Tiefe
            write(6)  // Farbtyp: Wahrfarben mit Alpha
            write(0)  // Kompression: zlib
            write(0)  // Filtermethode
            write(0)  // kein Zeilensprung
        }
        out.writeChunk("IHDR", header.toByteArray())
        out.writeChunk("IDAT", deflate(raw.toByteArray()))
        out.writeChunk("IEND", ByteArray(0))

        return out.toByteArray()
    }

    private fun deflate(data: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(data)
        deflater.finish()

        val out = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        while (!deflater.finished()) {
            out.write(buffer, 0, deflater.deflate(buffer))
        }
        deflater.end()
        return out.toByteArray()
    }

    private fun ByteArrayOutputStream.writeInt(value: Int) {
        write(value ushr 24 and 0xFF)
        write(value ushr 16 and 0xFF)
        write(value ushr 8 and 0xFF)
        write(value and 0xFF)
    }

    private fun ByteArrayOutputStream.writeChunk(type: String, data: ByteArray) {
        writeInt(data.size)
        val typeBytes = type.toByteArray(Charsets.US_ASCII)
        write(typeBytes)
        write(data)

        val crc = CRC32().apply {
            update(typeBytes)
            update(data)
        }
        writeInt(crc.value.toInt())
    }
}
