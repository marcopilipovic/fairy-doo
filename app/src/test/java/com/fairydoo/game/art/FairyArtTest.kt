package com.fairydoo.game.art

import com.fairydoo.game.game.FairySpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Hält fest, dass zu jeder Fee auch ein Bild vorliegt.
 *
 * Die Zuordnung im Code ist ein `when` über alle Enum-Werte und damit vom
 * Compiler abgesichert — dass die referenzierte Datei *existiert*, prüft er
 * jedoch nicht. Ein fehlendes Bild fiele sonst erst im laufenden Spiel als
 * leeres Feld auf.
 */
class FairyArtTest {

    private val artDir = File("src/main/res/drawable-nodpi")

    @Test
    fun `jede Fee hat ein Bild`() {
        for (species in FairySpecies.entries) {
            val file = File(artDir, "fairy_${species.name.lowercase()}.png")
            assertTrue("Bild fehlt: ${file.path}", file.isFile)
            assertTrue("Bild ist leer: ${file.path}", file.length() > 1_000)
        }
    }

    @Test
    fun `es liegen keine verwaisten Bilder herum`() {
        // Ein Bild ohne Fee wäre toter Ballast in der App — der Release-Build
        // entfernt es zwar, aber im Projekt stiftet es Verwirrung.
        val vorhanden = artDir.listFiles { f -> f.name.startsWith("fairy_") }
            ?.map { it.nameWithoutExtension }
            ?.toSet()
            .orEmpty()
        val erwartet = FairySpecies.entries.map { "fairy_${it.name.lowercase()}" }.toSet()

        assertEquals(erwartet, vorhanden)
    }

    @Test
    fun `jede Fee hat einen eigenen Schein`() {
        // Zwei Feen mit demselben Ton wären auf dem Brett schwerer zu
        // unterscheiden — der Schein ist neben der Figur das zweite Merkmal.
        val toene = FairySpecies.entries.map { it.glowArgb }

        assertEquals("Zwei Feen teilen sich einen Ton", toene.size, toene.toSet().size)
        assertTrue(
            "Ein Schein ist durchsichtig",
            toene.all { (it ushr 24 and 0xFF) > 0x80 },
        )
    }
}
