package com.fairydoo.game.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Hält fest, dass zu jedem Gebiet mit Bildverweis auch eine Datei vorliegt.
 *
 * Der Verweis im Code ist eine Ressourcen-Kennung; ob die Datei existiert, prüft
 * der Compiler. Ob sie *brauchbar* ist, nicht — eine leere oder abgeschnittene
 * Datei fiele erst im laufenden Spiel als graues Feld auf. Dasselbe Prinzip wie
 * bei den Feen-Bildern (`FairyArtTest`).
 *
 * Solange kein Gebiet ein Bild trägt, läuft der Test leer durch. Das ist kein
 * Versehen: Die Kacheln entstehen nach und nach, und bis dahin zeichnet der
 * Rückfall die Motive.
 */
class ZoneImageTest {

    private val artDir = File("src/main/res/drawable-nodpi")

    @Test
    fun `jedes Gebiet mit Bildverweis hat auch eine Datei`() {
        val withImage = ZoneStyles.filter { it.hasImage }
        for (style in withImage) {
            val expected = File(artDir, "${fileNameOf(style)}.png")
            assertTrue(
                "Kachel fehlt für ${style.name}: erwartet ${expected.path}",
                expected.isFile,
            )
            assertTrue(
                "Kachel ist zu klein, um brauchbar zu sein: ${expected.path}",
                expected.length() > 10_000,
            )
        }
    }

    @Test
    fun `es liegen keine verwaisten Kacheln herum`() {
        // Eine Kachel ohne Gebiet wäre toter Ballast im Projekt. Der
        // Release-Build entfernt sie zwar, aber wer sie im Ordner sieht, hält
        // sie für benutzt.
        val onDisk = artDir.listFiles { file -> file.name.startsWith("zone_") }
            ?.map { it.nameWithoutExtension }
            .orEmpty()
        val expected = ZoneStyles.filter { it.hasImage }.map(::fileNameOf).toSet()

        val orphans = onDisk.filterNot { it in expected }
        assertTrue(
            "Diese Kacheln gehören zu keinem Gebiet: $orphans",
            orphans.isEmpty(),
        )
    }

    private companion object {
        /**
         * Der Dateiname, unter dem die Kachel eines Gebiets erwartet wird.
         *
         * Android-Ressourcen dürfen nur Kleinbuchstaben, Ziffern und
         * Unterstriche enthalten — Umlaute werden deshalb ausgeschrieben.
         */
        fun fileNameOf(style: ZoneStyle): String = "zone_" + style.name
            .lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(" ", "_")
    }
}
