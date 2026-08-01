package com.fairydoo.game.art

import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.game.model.PuzzleGenerator
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

/**
 * Prüft die Sprites und schreibt sie zum Anschauen als PNG heraus.
 *
 * Ob eine Fee *hübsch* ist, kann kein Test beantworten — ob sie sichtbar,
 * unterscheidbar und innerhalb ihres Feldes ist, schon. Die PNG-Ausgabe ist
 * dabei der eigentliche Zweck: der schnellste Weg, ein 32×32-Raster zu
 * beurteilen, ohne die App zu starten — das Gegenstück zum WAV-Export der
 * Klänge.
 */
class SpriteRenderTest {

    private val outputDir = File("build/sprites").apply { mkdirs() }

    /** Der Moosgrund des Spielbretts — eine Fee auf Weiß sagt nichts aus. */
    private val moss = 0xFF3B4C33.toInt()
    private val night = 0xFF0A0E21.toInt()

    /** Die Zonenfarben des Spiels (aus ui/theme/Color.kt). */
    private val zoneColors = listOf(
        0xFFFF6B8A, 0xFF5BC8FF, 0xFF7DFF9E, 0xFFFFD76B,
        0xFFC58BFF, 0xFFFF9A5B, 0xFF6BFFF2, 0xFFFF9ECF,
    ).map { it.toInt() }

    @Test
    fun `alle Feen werden als PNG ausgegeben`() {
        for ((index, species) in FairySpecies.entries.withIndex()) {
            val number = (index + 1).toString().padStart(2, '0')
            val slug = species.name.lowercase()

            write("$number-$slug.png", renderSingle(species, frame = 0, scale = 10))
            write("$number-$slug-frames.png", renderFilmStrip(species, scale = 6))
        }

        write("00-alle-feen.png", renderContactSheet())
        for (level in 1..3) {
            write("brett-level-$level.png", renderBoard(level))
        }

        println("Sprites geschrieben nach: ${outputDir.absolutePath}")
    }

    @Test
    fun `jedes Raster hat das richtige Format`() {
        // Eine verrutschte Zeile wäre sonst nur ein unerklärliches Loch im Bild.
        for ((species, sprite) in allFairySprites) {
            sprite.validate(species.displayName)
        }
    }

    @Test
    fun `keine Fee ist leer`() {
        for ((species, sprite) in allFairySprites) {
            val visible = (0 until SPRITE_SIZE).sumOf { y ->
                (0 until SPRITE_SIZE).count { x -> sprite.colorAt(0, x, y) != 0 }
            }
            assertTrue(
                "${species.displayName} ist praktisch unsichtbar ($visible Pixel)",
                visible > 120,
            )
        }
    }

    @Test
    fun `jede Fee bewegt sich`() {
        // Zwei identische Bilder wären eine tote Animation — zu sehen wäre nur,
        // dass nichts passiert.
        for ((species, sprite) in allFairySprites) {
            if (sprite.frames.size < 2) continue
            assertTrue(
                "${species.displayName}: die Bilder sind identisch",
                sprite.frames[0] != sprite.frames[1],
            )
        }
    }

    @Test
    fun `keine Fee ragt aus ihrem Feld`() {
        // Die äußerste Pixelreihe bleibt frei, damit die Fee bei aufgerundeter
        // Vergrößerung nicht an der Zellkante klebt.
        for ((species, sprite) in allFairySprites) {
            sprite.frames.indices.forEach { frame ->
                for (i in 0 until SPRITE_SIZE) {
                    val edges = listOf(
                        sprite.colorAt(frame, i, 0),
                        sprite.colorAt(frame, i, SPRITE_SIZE - 1),
                        sprite.colorAt(frame, 0, i),
                        sprite.colorAt(frame, SPRITE_SIZE - 1, i),
                    )
                    assertTrue(
                        "${species.displayName}, Bild $frame: berührt den Rand bei $i",
                        edges.all { it == 0 },
                    )
                }
            }
        }
    }

    @Test
    fun `jede Fee hebt sich vom Moosgrund ab`() {
        // Kein Selbstzweck: Terra ist erdbraun und sitzt auf dunkelgrünem Moos —
        // sie ist der Entwurf, der real unsichtbar werden kann.
        val mossLuminance = luminance(moss)
        for ((species, sprite) in allFairySprites) {
            val brightest = (0 until SPRITE_SIZE).maxOf { y ->
                (0 until SPRITE_SIZE).maxOf { x ->
                    val argb = sprite.colorAt(0, x, y)
                    if (argb == 0) 0.0 else luminance(argb)
                }
            }
            assertTrue(
                "${species.displayName} hebt sich kaum vom Moos ab " +
                    "(hellster Punkt $brightest gegen $mossLuminance)",
                brightest - mossLuminance > 0.25,
            )
        }
    }

    // ── Zeichnen ────────────────────────────────────────────────────────────

    private fun luminance(argb: Int): Double {
        val r = (argb shr 16 and 0xFF) / 255.0
        val g = (argb shr 8 and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun renderSingle(species: FairySpecies, frame: Int, scale: Int): Canvas {
        val side = SPRITE_SIZE * scale
        return Canvas(side, side, moss).apply {
            drawSprite(species.sprite, frame, 0, 0, scale)
        }
    }

    /** Alle Bilder einer Fee nebeneinander — Animation ohne Abspieler beurteilen. */
    private fun renderFilmStrip(species: FairySpecies, scale: Int): Canvas {
        val sprite = species.sprite
        val cell = SPRITE_SIZE * scale
        val gap = 8
        val width = sprite.frames.size * cell + (sprite.frames.size - 1) * gap

        return Canvas(width, cell, moss).apply {
            sprite.frames.indices.forEach { frame ->
                drawSprite(sprite, frame, frame * (cell + gap), 0, scale)
            }
        }
    }

    /** Alle zehn im Raster, jede in ihrem eigenen Zonenrahmen. */
    private fun renderContactSheet(): Canvas {
        val scale = 5
        val cell = SPRITE_SIZE * scale
        val columns = 5
        val rows = (FairySpecies.entries.size + columns - 1) / columns

        return Canvas(columns * cell, rows * cell, night).apply {
            FairySpecies.entries.forEachIndexed { index, species ->
                val x = (index % columns) * cell
                val y = (index / columns) * cell

                fillRect(x, y, cell, cell, zoneColors[index % zoneColors.size])
                fillRect(x + 5, y + 5, cell - 10, cell - 10, moss)
                drawSprite(species.sprite, 0, x, y, scale)
            }
        }
    }

    /**
     * Ein echtes Brett mit Zonen und ihren Bewohnerinnen.
     *
     * Der wertvollste Teil der Vorschau: Er zeigt, ob die Zuordnung
     * funktioniert — statt sie glauben zu müssen.
     */
    private fun renderBoard(level: Int): Canvas {
        val size = GameState.sizeForLevel(level)
        val scale = 4
        val cell = SPRITE_SIZE * scale
        val puzzle = PuzzleGenerator.generate(size, Random(level.toLong()))

        return Canvas(size * cell, size * cell, moss).apply {
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val zone = puzzle.regionAt(Pos(row, col))
                    strokeRect(
                        col * cell, row * cell, cell, cell,
                        zoneColors[zone % zoneColors.size], thickness = 3,
                    )
                }
            }
            // In jede Zone die Fee ihrer Lösung setzen.
            for (pos in puzzle.solution) {
                val species = GameState.speciesForZone(level, puzzle.regionAt(pos))
                drawSprite(species.sprite, 0, pos.col * cell, pos.row * cell, scale)
            }
        }
    }

    private fun write(name: String, canvas: Canvas) {
        File(outputDir, name).writeBytes(canvas.toPng())
    }
}
