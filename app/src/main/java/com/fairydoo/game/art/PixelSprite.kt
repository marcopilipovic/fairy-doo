package com.fairydoo.game.art

/** Kantenlänge jedes Sprites in Pixeln. */
const val SPRITE_SIZE = 32

/**
 * Ein Pixel-Sprite: eine Farbpalette und mehrere Einzelbilder.
 *
 * Die Feen werden **im Code gezeichnet**, nicht als Bilddateien mitgeliefert —
 * dasselbe Motiv wie bei den berechneten Klängen: Das Raster steht im
 * Versionsverlauf, jedes einzelne Pixel ist nachträglich änderbar, und es gibt
 * keine Binärdateien, deren Herkunft man später nicht mehr nachvollziehen kann.
 *
 * Bewusst frei von Android-Abhängigkeiten (Farben als schlichte `Int`, Bilder
 * als Zeichenketten): So kann der Vorschau-Test die Sprites auf der JVM zu PNG
 * rendern, ohne dass ein Android-Grafikstapel vorhanden sein muss.
 *
 * @property palette Zeichen → ARGB-Farbe. Jedes Zeichen, das hier fehlt, ist
 *   durchsichtig — dadurch braucht `.` keinen Sonderfall.
 * @property frames Einzelbilder, je [SPRITE_SIZE] Zeilen à [SPRITE_SIZE] Zeichen.
 * @property frameMillis Standzeit eines Einzelbildes. Rieselnder Staub darf
 *   schneller takten als ein Flügelschlag.
 * @property grounded Feen schweben normalerweise. Terra steht auf dem Boden —
 *   für sie entfällt die Schwebebewegung.
 */
data class PixelSprite(
    val palette: Map<Char, Int>,
    val frames: List<List<String>>,
    val frameMillis: Int = 220,
    val grounded: Boolean = false,
) {
    /** Farbe eines Pixels als ARGB; 0 bedeutet durchsichtig. */
    fun colorAt(frame: Int, x: Int, y: Int): Int =
        palette[frames[frame][y][x]] ?: 0
}

/**
 * Zerlegt einen mehrzeiligen Roh-String in Rasterzeilen.
 *
 * Der Roh-String ist einer Liste von Zeichenketten überlegen: Die Spalten
 * fluchten im Editor, es gibt keine Anführungszeichen und Kommas je Zeile, und
 * man kann mit Blockauswahl arbeiten — beim Pixelzeichnen ist das der
 * Unterschied zwischen machbar und mühsam.
 */
fun String.spriteRows(): List<String> =
    trimIndent().lines().filter { it.isNotBlank() }

/**
 * Prüft ein Sprite und wirft mit einer Meldung, die zur Fundstelle führt.
 *
 * Ein verrutschtes Zeichen wäre sonst nur ein unerklärliches Loch im Bild —
 * beim Zeichnen von 32×32-Rastern von Hand ist das der häufigste Fehler.
 */
fun PixelSprite.validate(name: String) {
    require(frames.isNotEmpty()) { "$name hat kein einziges Bild" }

    frames.forEachIndexed { frameIndex, rows ->
        require(rows.size == SPRITE_SIZE) {
            "$name, Bild $frameIndex: ${rows.size} Zeilen statt $SPRITE_SIZE"
        }
        rows.forEachIndexed { y, row ->
            require(row.length == SPRITE_SIZE) {
                "$name, Bild $frameIndex, Zeile $y: ${row.length} Zeichen statt $SPRITE_SIZE " +
                    "— „$row\""
            }
            row.forEachIndexed { x, char ->
                require(char == '.' || char in palette) {
                    "$name, Bild $frameIndex, Zeile $y, Spalte $x: " +
                        "Zeichen '$char' fehlt in der Palette"
                }
            }
        }
    }
}

/** Die Grundfarben, die alle Feen teilen — dadurch wirken die zehn wie eine Familie. */
object FairyInk {
    const val OUTLINE = 0xFF241436.toInt()
    const val SKIN = 0xFFFFE3C6.toInt()
    const val SKIN_SHADE = 0xFFE8B894.toInt()
    const val EYE = 0xFF1A0F2E.toInt()
    const val SPARK = 0xCCFFF3C8.toInt()

    /** Kontur, Haut, Auge und Funke; dazu kommen je Fee die Akzentfarben. */
    val base: Map<Char, Int> = mapOf(
        'o' to OUTLINE,
        's' to SKIN,
        'S' to SKIN_SHADE,
        'e' to EYE,
        '*' to SPARK,
    )
}

/** Baut eine Palette aus den gemeinsamen Grundfarben plus eigenen Akzenten. */
fun fairyPalette(vararg accents: Pair<Char, Int>): Map<Char, Int> =
    FairyInk.base + accents
