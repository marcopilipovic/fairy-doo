package ug.humb.fairydoku.ui

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
 * **Auch die Seite selbst entsteht hier.** Bis August 2026 wurden nur die
 * Rohtexte herausgeschrieben und Markdown und HTML von Hand daraus gebaut —
 * also genau die zweite, handgepflegte Fassung, die der Kommentar oben
 * ausschließen wollte. Sie war denn auch schon abgedriftet: In der Seite
 * standen die DSGVO-Rechte als sechs lose Zeilen statt als Liste, und ein
 * Abschnitt war als Überschrift ausgezeichnet, ein gleichartiger nicht.
 *
 * Die Gliederung kommt aus [LegalText] — derselben Stelle, aus der die App
 * ihre Überschriften und Aufzählungen liest.
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

            // Reste der Vorlage, die es nie in die Veröffentlichung schaffen
            // dürfen.
            //
            // Die eckige Klammer ist bei den Lizenzen ausgenommen: Der Anhang
            // der Apache-Lizenz enthält den Mustervermerk „Copyright [yyyy]
            // [name of copyright owner]". Das ist kein vergessener Platzhalter,
            // sondern Teil des Lizenztextes — und der wird wortgleich
            // wiedergegeben oder gar nicht.
            val verboten = if (page == LegalPage.Lizenzen) {
                listOf("ENTWURF", "Platzhalter", "auszufüllen")
            } else {
                listOf("[", "ENTWURF", "Platzhalter", "auszufüllen")
            }
            for (rest in verboten) {
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

        File(outputDir, "rechtstexte.md").writeText(buildMarkdown())
        File(outputDir, "rechtstexte.html").writeText(buildHtml())

        // Und jede Seite noch einmal einzeln als Markdown.
        //
        // Die Webseite unter fairydoku.sites.humb.ug führt die vier Teile auf
        // getrennten Adressen — /de/impressum, /de/nutzungsbedingungen,
        // /de/datenschutz, /de/lizenzen. Wer sie aus der Gesamtdatei
        // heraustrennt, macht das von Hand, und von Hand Getrenntes läuft
        // auseinander: Am 31. August stand auf der Datenschutzseite noch die
        // Fassung vom Vortag, ohne die beiden TDDDG-Absätze.
        //
        // Die Dateinamen sind deshalb die der Adressen, nicht die der
        // Aufzählung. Wer eine Seite aktualisiert, sucht nach ihrem Pfad.
        val webseitenNamen = mapOf(
            LegalPage.Impressum to "impressum",
            LegalPage.Agb to "nutzungsbedingungen",
            LegalPage.Datenschutz to "datenschutz",
            LegalPage.Lizenzen to "lizenzen",
        )
        for ((page, name) in webseitenNamen) {
            File(outputDir, "seite-$name.md").writeText(buildSeite(page))
        }

        println("Rechtstexte geschrieben nach: ${outputDir.absolutePath}")
    }

    /**
     * Die Haftungsklausel ist gestrichen und darf nicht zurückkehren.
     *
     * Ein eigener Test, weil eine gestrichene Klausel sich schlecht selbst
     * verteidigt: Wer die AGB später aus einer Vorlage auffrischt, holt sie
     * versehentlich zurück, und niemandem fällt es auf.
     */
    @Test
    fun `die AGB enthalten keine Haftungsklausel`() {
        val agb = GameCopy.legalBody(LegalPage.Agb)

        assertFalse(
            "Die Haftungsklausel ist am 24.08.2026 bewusst entfernt worden — " +
                "vor dem Wiedereinsetzen anwaltlich prüfen lassen",
            agb.contains("Gewährleistung und Haftung") || agb.contains("grobe Fahrlässigkeit"),
        )

        // Nach dem Streichen wurde umnummeriert. Bleibt eine Lücke oder ein
        // doppelter Paragraf stehen, ist das hier zu sehen, bevor es jemand
        // im Store liest.
        val nummern = Regex("""^§ (\d+) """, RegexOption.MULTILINE)
            .findAll(agb).map { it.groupValues[1].toInt() }.toList()

        assertTrue("Die AGB haben keine Paragrafen mehr", nummern.isNotEmpty())
        assertTrue(
            "Die Paragrafen laufen nicht lückenlos von 1 bis ${nummern.size}: $nummern",
            nummern == (1..nummern.size).toList(),
        )
    }

    // ── Ausgabeformate ──────────────────────────────────────────────────

    private fun buildMarkdown(): String = buildString {
        appendLine("# Fairydoku — Rechtstexte")
        appendLine()
        appendLine("Wortgleich mit dem, was in der App steht. Quelle ist der App-Quelltext")
        appendLine("(`GameCopy.legalBody`); dieser Text entsteht daraus automatisch.")

        for (page in LegalPage.entries) {
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## ${GameCopy.legalTitle(page)}")

            for (block in LegalText.parse(GameCopy.legalBody(page))) {
                appendLine()
                when (block) {
                    is LegalText.Block.Heading -> appendLine("### ${block.text}")
                    // Zwei Leerzeichen am Zeilenende sind der harte Umbruch in
                    // Markdown — Anschriften dürfen nicht zusammenlaufen.
                    is LegalText.Block.Paragraph ->
                        appendLine(block.lines.joinToString("  \n"))
                    is LegalText.Block.Bullets ->
                        block.items.forEach { appendLine("- $it") }
                }
            }
        }
    }

    /** Eine einzelne Rechtsseite als Markdown — für die getrennten Webseiten. */
    private fun buildSeite(page: LegalPage): String = buildString {
        appendLine("# ${GameCopy.legalTitle(page)}")
        appendLine()
        appendLine("Wortgleich mit dem, was in der App steht. Quelle ist der")
        appendLine("App-Quelltext (`GameCopy.legalBody`); dieser Text entsteht daraus")
        appendLine("automatisch und darf nicht von Hand geändert werden.")

        for (block in LegalText.parse(GameCopy.legalBody(page))) {
            appendLine()
            when (block) {
                is LegalText.Block.Heading -> appendLine("## ${block.text}")
                is LegalText.Block.Paragraph -> appendLine(block.lines.joinToString("  \n"))
                is LegalText.Block.Bullets -> block.items.forEach { appendLine("- $it") }
            }
        }
    }

    private fun buildHtml(): String = buildString {
        appendLine(HTML_HEAD)
        appendLine("""<h1>Fairydoku</h1>""")
        appendLine("""<p class="sub">Impressum, Nutzungsbedingungen und Datenschutzerklärung</p>""")

        appendLine("<nav>")
        for (page in LegalPage.entries) {
            val anker = page.name.lowercase()
            appendLine("""  <a href="#$anker">${escape(GameCopy.legalTitle(page))}</a>""")
        }
        appendLine("</nav>")

        for (page in LegalPage.entries) {
            val anker = page.name.lowercase()
            appendLine("""<h2 id="$anker">${escape(GameCopy.legalTitle(page))}</h2>""")

            for (block in LegalText.parse(GameCopy.legalBody(page))) {
                when (block) {
                    is LegalText.Block.Heading ->
                        appendLine("<h3>${escape(block.text)}</h3>")
                    is LegalText.Block.Paragraph ->
                        appendLine("<p>${block.lines.joinToString("<br>") { escape(it) }}</p>")
                    is LegalText.Block.Bullets -> {
                        appendLine("<ul>")
                        block.items.forEach { appendLine("  <li>${escape(it)}</li>") }
                        appendLine("</ul>")
                    }
                }
            }
        }

        appendLine(HTML_FOOT)
    }

    /**
     * Nur die drei Zeichen, die im Fließtext etwas kaputt machen können.
     *
     * Anführungszeichen bleiben stehen: In einem Textknoten sind sie
     * unbedenklich, und `&quot;` mitten im Satz macht die Quelltextansicht
     * unlesbar — die vorige, handgebaute Fassung hatte genau das.
     */
    private fun escape(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

    private companion object {
        val HTML_HEAD = """
            <!doctype html>
            <html lang="de">
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Fairydoku — Impressum, AGB und Datenschutz</title>
            <style>
              :root { color-scheme: light dark; }
              body {
                margin: 0 auto; max-width: 46rem; padding: 2rem 1.25rem 5rem;
                font: 16px/1.65 system-ui, -apple-system, "Segoe UI", sans-serif;
                background: #fbfaff; color: #1c1e33;
              }
              @media (prefers-color-scheme: dark) {
                body { background: #0d1022; color: #e7e4f5; }
                a { color: #ffd76b; }
                nav { border-color: #262a4d; }
                h2 { border-color: #262a4d; }
              }
              h1 { font-size: 1.7rem; margin: 0 0 .3rem; }
              .sub { color: #6a6d90; margin: 0 0 2rem; }
              nav { display: flex; gap: 1.2rem; flex-wrap: wrap;
                    border-bottom: 1px solid #e2dff0; padding-bottom: 1rem; margin-bottom: 2rem; }
              h2 { font-size: 1.3rem; margin: 3rem 0 1rem;
                   border-top: 1px solid #e2dff0; padding-top: 2rem; }
              h2:first-of-type { border-top: 0; padding-top: 0; margin-top: 0; }
              h3 { font-size: 1.02rem; margin: 1.8rem 0 .4rem; }
              p { margin: 0 0 .9rem; }
              ul { margin: 0 0 .9rem; padding-left: 1.3rem; }
              li { margin: .2rem 0; }
              footer { margin-top: 4rem; font-size: .9rem; color: #6a6d90; }
            </style>
            </head>
            <body>
        """.trimIndent()

        val HTML_FOOT = """
            <footer>App HUMB UG (haftungsbeschränkt) · info@humb.ug</footer>
            </body>
            </html>
        """.trimIndent()
    }
}
