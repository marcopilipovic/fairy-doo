package com.fairydoo.game.ui

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Schreibt Impressum, AGB und Datenschutzerklärung als Webseite nach
 * `app/build/rechtstexte/`.
 *
 * Die Play Console verlangt eine **öffentlich erreichbare** Adresse für die
 * Datenschutzerklärung — ein Text, der nur in der App steht, genügt nicht. Statt
 * ihn von Hand abzuschreiben (und beim nächsten Mal zu vergessen), entsteht die
 * Seite hier aus derselben Quelle, aus der sie auch die App bezieht. Damit kann
 * sie gar nicht auseinanderlaufen.
 *
 * Dasselbe Vorgehen wie bei den Klängen in `SoundRenderTest`: ein Test, der
 * nebenbei etwas Brauchbares erzeugt.
 *
 * Die Datei ist eine einzelne HTML-Seite ohne Fremdinhalte — sie lässt sich
 * überall hinlegen, wo eine Datei erreichbar ist.
 */
class LegalPageExportTest {

    private val outputDir = File("build/rechtstexte").apply { mkdirs() }

    @Test
    fun `Rechtstexte als Webseite schreiben`() {
        val page = buildString {
            append(HEAD)
            LegalPage.entries.forEach { entry ->
                append("<h2 id=\"${anchorOf(entry)}\">${GameCopy.legalTitle(entry)}</h2>\n")
                append(renderBody(GameCopy.legalBody(entry)))
            }
            append(FOOT)
        }

        File(outputDir, "index.html").writeText(page)
    }

    /**
     * Prüft, dass die Platzhalter beim Veröffentlichen aufgefallen sind.
     *
     * Der Test schlägt bewusst **nicht** fehl, solange sie noch drinstehen —
     * das wäre während der Entwicklung nur lästig. Er schreibt die Fundstellen
     * in eine Datei neben der Seite, damit vor dem Hochladen klar ist, was noch
     * fehlt.
     */
    @Test
    fun `offene Platzhalter auflisten`() {
        val open = LegalPage.entries.flatMap { entry ->
            PLACEHOLDER.findAll(GameCopy.legalBody(entry))
                .map { it.value }
                // Kommt in der Einleitung jedes Textes vor („Alle Angaben in
                // [eckigen Klammern] bitte ausfüllen") und ist selbst keine.
                .filter { it != "[eckigen Klammern]" }
                .map { "${GameCopy.legalTitle(entry)}: $it" }
        }.distinct().sorted()

        File(outputDir, "offene-platzhalter.txt").writeText(
            if (open.isEmpty()) {
                "Keine offenen Platzhalter — die Texte sind vollständig.\n"
            } else {
                "Noch auszufüllen, bevor die Seite hochgeladen wird:\n\n" +
                    open.joinToString("\n") { "  $it" } + "\n"
            },
        )
    }

    @Test
    fun `die Seite enthaelt alle drei Texte und keine Skripte`() {
        val page = File(outputDir, "index.html").also { if (!it.exists()) `Rechtstexte als Webseite schreiben`() }
            .readText()

        LegalPage.entries.forEach { entry ->
            assertTrue(
                "„${GameCopy.legalTitle(entry)}" + "\" fehlt auf der Seite",
                page.contains(GameCopy.legalTitle(entry)),
            )
        }
        // Eine Rechtstext-Seite braucht kein JavaScript. Ohne Skripte und ohne
        // Fremdinhalte gibt es dort auch nichts zu erklären.
        assertFalse("Die Seite soll ohne Skripte auskommen", page.contains("<script"))
    }

    private fun anchorOf(page: LegalPage): String = when (page) {
        LegalPage.Impressum -> "impressum"
        LegalPage.Agb -> "agb"
        LegalPage.Datenschutz -> "datenschutz"
    }

    /**
     * Macht aus dem Fließtext Absätze.
     *
     * Die Texte in der App sind für eine scrollende Textfläche geschrieben:
     * Überschriften stehen als eigene kurze Zeile, Absätze sind durch Leerzeilen
     * getrennt. Genau daran wird hier entlanggeschnitten — eine kurze Zeile ohne
     * Satzzeichen am Ende wird zur Zwischenüberschrift, alles andere zum Absatz.
     */
    private fun renderBody(body: String): String =
        body.split("\n\n").filter { it.isNotBlank() }.joinToString("\n") { block ->
            val lines = block.trim().lines()
            val head = lines.first().trim()
            when {
                lines.size == 1 && looksLikeHeading(head) -> "<h3>${escape(head)}</h3>"
                lines.size > 1 && looksLikeHeading(head) ->
                    "<h3>${escape(head)}</h3>\n" +
                        "<p>${escape(lines.drop(1).joinToString("\n").trim()).replace("\n", "<br>")}</p>"
                else -> "<p>${escape(block.trim()).replace("\n", "<br>")}</p>"
            }
        }

    private fun looksLikeHeading(line: String): Boolean =
        line.length <= 70 && !line.endsWith(".") && !line.endsWith(":") && !line.endsWith(")")

    private fun escape(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private companion object {
        val PLACEHOLDER = Regex("""\[[^\]]+]""")

        val HEAD = """
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
              footer { margin-top: 4rem; font-size: .9rem; color: #6a6d90; }
            </style>
            </head>
            <body>
            <h1>Fairydoku</h1>
            <p class="sub">Impressum, Nutzungsbedingungen und Datenschutzerklärung</p>
            <nav>
              <a href="#impressum">Impressum</a>
              <a href="#agb">AGB</a>
              <a href="#datenschutz">Datenschutz</a>
            </nav>

        """.trimIndent()

        val FOOT = """

            <footer>
            <p>Diese Seite gibt denselben Text wieder, der auch in der App unter „Impressum“, „AGB“ und „Datenschutz“ steht. Sie wird aus derselben Quelle erzeugt und kann daher nicht davon abweichen.</p>
            </footer>
            </body>
            </html>
        """.trimIndent()
    }
}
