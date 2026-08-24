package com.fairydoo.game.ui

/**
 * Zerlegt einen Rechtstext aus [GameCopy.legalBody] in seine Bestandteile.
 *
 * Warum überhaupt: Die Rechtstexte liegen als eine flache Zeichenkette vor,
 * damit sie sich an einer Stelle pflegen lassen. Wer sie anzeigt, musste die
 * Gliederung bisher selbst erraten — die App gar nicht (ein Textblock, alles
 * gleich groß, Überschriften nicht von Fließtext zu unterscheiden), die
 * Webseite von Hand.
 *
 * Zwei Leser, die unabhängig voneinander raten, laufen auseinander. Deshalb
 * steht die Regel hier, einmal, und beide fragen dieselbe Stelle.
 *
 * Die Erkennung stützt sich auf das, was die Texte tatsächlich tun, und auf
 * nichts sonst:
 *
 * - Ein leerer Zeilenumbruch trennt Blöcke.
 * - Eine Zeile mit `• ` ist ein Aufzählungspunkt.
 * - `§ 4 Kosten und Werbung` und `9. Deine Rechte` sind Überschriften — die
 *   AGB nummerieren mit Paragrafen, die Datenschutzerklärung mit Ziffern.
 * - Im Impressum tragen die Abschnitte keine Nummer (`Kontakt`,
 *   `Registereintrag`). Dort gilt: erste Zeile eines Blocks, kurz, und ohne
 *   Satzzeichen am Ende. Anschriften darunter bleiben Zeilen, weil sie zur
 *   zweiten Zeile an aufwärts gehören und die Regel dort nicht mehr greift.
 *
 * Bewusst kein Markdown: Die Texte sollen lesbar bleiben, wenn sie jemand
 * roh vor sich hat — etwa im `.txt`, das der Ausgabetest schreibt.
 */
object LegalText {

    sealed interface Block {
        /** Abschnittsüberschrift, etwa `§ 4 Kosten und Werbung`. */
        data class Heading(val text: String) : Block

        /** Fließtext. Mehrere Zeilen eines Blocks bleiben getrennt. */
        data class Paragraph(val lines: List<String>) : Block

        /** Zusammenhängende Aufzählung; jeder Eintrag ohne sein `• `. */
        data class Bullets(val items: List<String>) : Block
    }

    /** Länge, bis zu der eine nummernlose Zeile als Überschrift durchgeht. */
    private const val MAX_HEADING_LENGTH = 60

    private val PARAGRAPH_HEADING = Regex("""^§ \d+\s+\S.*""")
    private val NUMBERED_HEADING = Regex("""^\d+\.\s+\S.*""")

    fun parse(body: String): List<Block> {
        val blocks = mutableListOf<Block>()

        for (chunk in body.trim().split(Regex("\n\\s*\n"))) {
            val lines = chunk.lines().map(String::trim).filter(String::isNotEmpty)
            if (lines.isEmpty()) continue

            // Angesammelte Fließtextzeilen, die noch keinen eigenen Block haben.
            val pending = mutableListOf<String>()
            val bullets = mutableListOf<String>()

            fun flushParagraph() {
                if (pending.isNotEmpty()) {
                    blocks += Block.Paragraph(pending.toList())
                    pending.clear()
                }
            }

            fun flushBullets() {
                if (bullets.isNotEmpty()) {
                    blocks += Block.Bullets(bullets.toList())
                    bullets.clear()
                }
            }

            for ((index, line) in lines.withIndex()) {
                when {
                    line.startsWith("• ") -> {
                        flushParagraph()
                        bullets += line.removePrefix("• ")
                    }

                    isHeading(line, isFirstOfBlock = index == 0) -> {
                        flushParagraph()
                        flushBullets()
                        blocks += Block.Heading(line)
                    }

                    else -> {
                        flushBullets()
                        pending += line
                    }
                }
            }

            flushParagraph()
            flushBullets()
        }

        return blocks
    }

    private fun isHeading(line: String, isFirstOfBlock: Boolean): Boolean {
        if (PARAGRAPH_HEADING.matches(line)) return true
        if (NUMBERED_HEADING.matches(line)) return true

        // Der nummernlose Fall des Impressums. Nur die erste Zeile eines
        // Blocks kommt infrage — sonst würde „Parkstraße 9" unter „Kontakt"
        // ebenfalls zur Überschrift, und die Anschrift zerfiele in Stücke.
        if (!isFirstOfBlock) return false
        if (line.length > MAX_HEADING_LENGTH) return false
        return line.last() !in ".:!?,;"
    }
}
