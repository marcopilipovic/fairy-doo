package com.fairydoo.game.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Prüft die Rechtstexte und schreibt sie zum Weiterverwenden heraus.
 *
 * Der Play Store verlangt eine im Netz erreichbare Datenschutzerklärung, und
 * dort muss dasselbe stehen wie in der App. Zwei Fassungen, die von Hand
 * gepflegt werden, laufen früher oder später auseinander — meist unbemerkt,
 * weil niemand beide nebeneinander liest.
 *
 * Deshalb ist der Code die Quelle: Dieser Test schreibt die Texte nach
 * `app/build/rechtstexte/`, und daraus entsteht die Seite für humb.ug. Wer die
 * Texte ändert, ändert sie einmal.
 *
 * Geprüft wird zusätzlich, dass keine Platzhalter übrig sind. Ein `[Firmenname]`
 * im veröffentlichten Impressum wäre ein Abmahngrund.
 */
class RechtstexteExportTest {

    private val outputDir = File("build/rechtstexte").apply { mkdirs() }

    @Test
    fun `die Rechtstexte sind vollstaendig und werden herausgeschrieben`() {
        for (page in LegalPage.entries) {
            val titel = GameCopy.legalTitle(page)
            val text = GameCopy.legalBody(page)

            assertTrue("$titel ist zu kurz für einen Rechtstext", text.length > 400)

            // Reste der Vorlage, die es nie in die Veröffentlichung schaffen dürfen.
            for (rest in listOf("[", "ENTWURF", "Platzhalter", "auszufüllen")) {
                assertFalse(
                    "$titel enthält noch \"$rest\" — das ist ein Rest der Vorlage",
                    text.contains(rest),
                )
            }

            File(outputDir, "${page.name.lowercase()}.txt").writeText(text)
        }

        // Die Anbieterangaben müssen in allen drei Texten auffindbar sein —
        // wer nur eine Seite liest, soll wissen, mit wem er es zu tun hat.
        for (page in listOf(LegalPage.Impressum, LegalPage.Datenschutz)) {
            val text = GameCopy.legalBody(page)
            assertTrue(
                "${GameCopy.legalTitle(page)} nennt den Anbieter nicht",
                text.contains("App HUMB UG") && text.contains("info@humb.ug"),
            )
        }

        println("Rechtstexte geschrieben nach: ${outputDir.absolutePath}")
    }
}
