package com.fairydoo.game.art

import com.fairydoo.game.game.FairySpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Hält fest, dass zu jeder Fee auch eine Zeichnung vorliegt — und dass sie
 * sich von den anderen unterscheidet.
 *
 * Die Zuordnung im Code ist ein `when` über alle Enum-Werte und damit vom
 * Compiler abgesichert — dass die referenzierte Datei *existiert*, prüft er
 * jedoch nicht. Eine fehlende Zeichnung fiele sonst erst im laufenden Spiel als
 * leeres Feld auf.
 */
class FairyArtTest {

    private val artDir = File("src/main/res/drawable")

    private fun fileFor(species: FairySpecies) =
        File(artDir, "fairy_${species.name.lowercase()}.xml")

    @Test
    fun `jede Fee hat eine Zeichnung`() {
        for (species in FairySpecies.entries) {
            val file = fileFor(species)
            assertTrue("Zeichnung fehlt: ${file.path}", file.isFile)

            val inhalt = file.readText()
            assertTrue("${file.name} ist keine Vektorzeichnung", inhalt.contains("<vector"))
            // Alle Figuren teilen sich ein Raster; nur so stehen sie auf dem
            // Brett gleich hoch und auf derselben Grundlinie.
            assertTrue(
                "${file.name} hat ein abweichendes Raster",
                inhalt.contains("""android:viewportWidth="120"""") &&
                    inhalt.contains("""android:viewportHeight="164""""),
            )
        }
    }

    @Test
    fun `keine zwei Feen tragen dieselbe Farbe`() {
        // Die Farbe ist das einzige, was die Figuren unterscheidet — die Umrisse
        // sind bei allen zehn gleich. Zwei Feen mit derselben Palette wären auf
        // dem Brett nicht auseinanderzuhalten, und weil die Zuordnung von Fee zu
        // Farbdatei von Hand entsteht, ist das ein Fehler, der leicht passiert
        // und im Code nirgends auffällt.
        val farbe = """android:fillColor="(#[0-9a-fA-F]{6})"""".toRegex()

        val paletten = FairySpecies.entries.associateWith { species ->
            farbe.findAll(fileFor(species).readText())
                .map { it.groupValues[1].lowercase() }
                .toSet()
        }

        for ((eine, andere) in paletten.entries.sortedBy { it.key.ordinal }.let { liste ->
            liste.flatMapIndexed { index, a -> liste.drop(index + 1).map { b -> a to b } }
        }) {
            assertTrue(
                "${eine.key.displayName} und ${andere.key.displayName} tragen dieselbe Palette",
                eine.value != andere.value,
            )
        }
    }

    @Test
    fun `es liegen keine verwaisten Zeichnungen herum`() {
        // Eine Zeichnung ohne Fee wäre toter Ballast in der App — der
        // Release-Build entfernt sie zwar, aber im Projekt stiftet sie
        // Verwirrung.
        val vorhanden = artDir.listFiles { f -> f.name.startsWith("fairy_") }
            ?.map { it.nameWithoutExtension }
            ?.toSet()
            .orEmpty()
        val erwartet = FairySpecies.entries.map { "fairy_${it.name.lowercase()}" }.toSet()

        assertEquals(erwartet, vorhanden)
    }
}
