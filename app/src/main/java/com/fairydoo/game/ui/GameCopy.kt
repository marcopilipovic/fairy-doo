package com.fairydoo.game.ui

import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameOverReason
import com.fairydoo.game.game.StatusMessage
import com.fairydoo.game.ui.sprites.FAIRY_TOKEN

/**
 * Alle Texte der Oberfläche an einem Ort — wörtlich aus dem Handoff.
 *
 * Die Engine meldet nur, *was* geschehen ist ([StatusMessage]); formuliert wird
 * es hier. So lässt sich die Ansprache ändern, ohne die Spielregeln anzufassen,
 * und eine spätere Übersetzung hat genau eine Anlaufstelle.
 */
object GameCopy {

    /** Die Waldzonen, in der Reihenfolge der Zonenfarben. */
    val zoneNames = listOf(
        "Mondlicht-Lichtung",
        "Pilzkreis",
        "Flussbett",
        "Glühwürmchen-Hain",
        "Nebelmoor",
        "Sternenwiese",
        "Wurzelhöhle",
        "Elfentor",
    )

    fun zoneName(index: Int): String = zoneNames[index % zoneNames.size]

    fun statusText(message: StatusMessage): String = when (message) {
        StatusMessage.Hint -> "Tippen: ✕ · gedrückt halten: $FAIRY_TOKEN"
        // Knapp gehalten: Das ist die mit Abstand längste Meldung, und je
        // kürzer sie ist, desto seltener bricht sie auf zwei Zeilen um.
        is StatusMessage.Zone ->
            "${zoneName(message.regionIndex)} · ${fairyIntroduction(message.species)}"
        StatusMessage.MistakeMade -> "⚡ Die Zauberkräfte stören sich! (−1 Leben)"
        StatusMessage.FairyDustUsed -> "✨ Der Feenstaub zeigt dir ein sicheres Feld!"
        is StatusMessage.NoFairyDust -> {
            val minutes = (message.nextInMillis / 60_000L).toInt() + 1
            "Kein Feenstaub mehr — neuer in etwa $minutes Minuten"
        }
        StatusMessage.IrrlichtUsed -> "🔮 Ein Irrlicht markiert ein Feld ohne Fee!"
        is StatusMessage.NoIrrlicht -> {
            val minutes = (message.nextInMillis / 60_000L).toInt() + 1
            "Kein Irrlicht mehr — neues in etwa $minutes Minuten"
        }
    }

    fun gameOverReason(reason: GameOverReason?): String = when (reason) {
        GameOverReason.TimeUp -> "Die Zeit ist verronnen – der Wald schläft ein."
        GameOverReason.TooManyConflicts -> "Zu viele Zauberkräfte sind kollidiert."
        null -> ""
    }

    /** Die Wesensart jeder Fee — Beiname, nicht Eigenname. */
    fun fairyTitle(species: FairySpecies): String = when (species) {
        FairySpecies.Flora -> "Waldfee"
        FairySpecies.Nebula -> "Staubfee"
        FairySpecies.Salta -> "Hüpffee"
        FairySpecies.Aura -> "Strahlfee"
        FairySpecies.Nixie -> "Frostfee"
        FairySpecies.Zephyr -> "Windfee"
        FairySpecies.Ignis -> "Funkenfee"
        FairySpecies.Terra -> "Kristallfee"
        FairySpecies.Chrono -> "Pendelfee"
        FairySpecies.Trixie -> "Chaosfee"
    }

    /** „Nixie, die Frostfee" */
    fun fairyIntroduction(species: FairySpecies): String =
        "${species.displayName}, die ${fairyTitle(species)}"

    /** „3 / 5 Feen platziert" */
    fun progressText(placed: Int, total: Int): String = "$placed / $total Feen platziert"

    /**
     * „Der Wald wird dichter: 5×5-Gitter — Nixie und Salta warten schon…"
     *
     * Kündigt die Neuzugänge an statt einer Feen-Art: Seit in jeder Zone eine
     * andere Fee lebt, ist das die Information, auf die man sich freut.
     */
    fun nextLevelTeaser(nextSize: Int, newcomers: List<FairySpecies>): String {
        val grid = "$nextSize×$nextSize-Gitter"
        if (newcomers.isEmpty()) {
            return "Der Wald wird dichter: ein neues $grid erwartet dich…"
        }

        val shown = newcomers.take(MAX_TEASER_NAMES).map { it.displayName }
        val hidden = newcomers.size - shown.size
        val names = enumerate(if (hidden > 0) shown + "$hidden weitere" else shown)
        val verb = if (newcomers.size == 1) "wartet" else "warten"
        return "Der Wald wird dichter: $grid — $names $verb schon…"
    }

    /** „Flora" · „Flora und Nixie" · „Flora, Nixie und Chrono" */
    private fun enumerate(names: List<String>): String = when (names.size) {
        0 -> ""
        1 -> names.first()
        else -> names.dropLast(1).joinToString(", ") + " und " + names.last()
    }

    private const val MAX_TEASER_NAMES = 3

    /** Formatiert die Restzeit als m:ss — für den Level-Timer, der sekundengenau tickt. */
    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    /**
     * Formatiert eine Wartezeit im Stunden-Bereich (Vorräte, Wald-Leben).
     *
     * Ohne Sekunden: Bei zwei Stunden Wartezeit tickt eine Sekundenanzeige nur
     * unruhig, ohne dass sie irgendjemand abliest. Aufgerundet auf die nächste
     * Minute, sonst wirkte "0 Min." kurz vor Ablauf wie "schon da".
     */
    fun formatWaitTime(totalSeconds: Int): String {
        val totalMinutes = (totalSeconds + 59) / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "$hours Std. $minutes Min."
            hours > 0 -> "$hours Std."
            else -> "$minutes Min."
        }
    }

    fun legalTitle(page: LegalPage): String = when (page) {
        LegalPage.Impressum -> "Impressum"
        LegalPage.Agb -> "AGB"
        LegalPage.Datenschutz -> "Datenschutz"
    }

    /**
     * Die drei Rechtsseiten, wie sie in der App stehen.
     *
     * Die Angaben stammen aus dem Rechtstext-Bestand der Webseite (Stand
     * 18. August 2026) und sind vollständig — keine Platzhalter mehr.
     *
     * ## Was hier bewusst anders steht als in der Vorlage
     *
     * Die Vorlage beschrieb eine Einwilligungsabfrage über Googles User
     * Messaging Platform und einen Menüpunkt „Datenschutz-Einstellungen
     * ändern". **Beides gibt es in dieser App nicht** — kein UMP-SDK, kein
     * Dialog, kein solcher Menüpunkt. Ein Datenschutztext, der eine Abfrage
     * verspricht, die nie erscheint, wäre schlechter als gar keiner. Die
     * betreffenden Abschnitte beschreiben deshalb, was tatsächlich passiert.
     *
     * Ebenso angepasst: Die Vorlage sprach von „eingeblendeter" Werbung. Die
     * App hat weder Banner noch Anzeigen, die von selbst erscheinen — Werbung
     * läuft ausschließlich als Videoanzeige, die der Spieler selbst startet,
     * um dafür eine Spielhilfe zu erhalten (siehe
     * [com.fairydoo.game.ads.RewardedAdManager]). Das Werbe-SDK lädt
     * allerdings schon beim App-Start eine Anzeige vor, und genau das steht
     * jetzt auch da.
     *
     * ## Offen und von außen zu klären
     *
     * Als Rechtsgrundlage für die Auslieferung nicht personalisierter Werbung
     * steht hier das berechtigte Interesse (Art. 6 Abs. 1 lit. f DSGVO). Ob
     * das im EWR ohne Einwilligungsabfrage trägt, ist eine Rechtsfrage — die
     * EU-Nutzereinwilligungsrichtlinie von Google verlangt in der Regel eine
     * Abfrage. Diese eine Stelle gehört vor der Veröffentlichung geprüft.
     * Wird später UMP eingebaut, wird daraus lit. a (Einwilligung), und die
     * Abschnitte 3 und 10 sind entsprechend zu ändern.
     */
    fun legalBody(page: LegalPage): String = when (page) {
        LegalPage.Impressum -> """
            Angaben gemäß § 5 DDG
            App HUMB UG (haftungsbeschränkt)
            Parkstraße 9
            31188 Holle

            Vertreten durch
            Marco Pilipovic

            Kontakt
            E-Mail: info@humb.ug

            Registereintrag
            Eintragung im Handelsregister
            Registergericht: Amtsgericht Hildesheim
            Registernummer: HRB 208491

            Umsatzsteuer-ID
            Umsatzsteuer-Identifikationsnummer gemäß § 27a Umsatzsteuergesetz:
            DE359950076

            Verantwortlich für den Inhalt nach § 18 Abs. 2 MStV
            Marco Pilipovic
            Parkstraße 9, 31188 Holle

            Verbraucherstreitbeilegung
            Wir sind nicht verpflichtet und nicht bereit, an einem Streitbeilegungsverfahren vor einer Verbraucherschlichtungsstelle teilzunehmen.
        """.trimIndent()

        LegalPage.Agb -> """
            § 1 Geltungsbereich und Anbieter
            Diese Nutzungsbedingungen gelten für die Nutzung der mobilen App „Fairydoku" (nachfolgend „App"), angeboten von App HUMB UG (haftungsbeschränkt), Parkstraße 9, 31188 Holle (nachfolgend „Anbieter"). Mit der Installation und Nutzung der App erkennst du diese Bedingungen an.

            § 2 Gegenstand
            Die App ist ein kostenloses Logikrätselspiel. Der Anbieter stellt die App zur privaten, nicht-kommerziellen Nutzung zur Verfügung.

            § 3 Nutzungsrecht
            Der Anbieter räumt dir ein einfaches, nicht übertragbares und widerrufliches Recht ein, die App auf deinen Geräten für private Zwecke zu nutzen. Eine Bearbeitung, Vervielfältigung, Verbreitung oder das Zugänglichmachen der App oder ihrer Inhalte über die private Nutzung hinaus ist nicht gestattet.

            § 4 Kosten und Werbung
            Die Nutzung der App ist kostenlos. Die App finanziert sich über Werbung. Werbung erscheint nicht von selbst: Du kannst freiwillig ein kurzes Werbevideo ansehen, um dafür eine Spielhilfe oder ein Leben zu erhalten. Eine Verpflichtung, Werbung anzusehen, besteht nicht, und ohne Werbung ist die App vollständig spielbar.

            § 5 Virtuelle Gegenstände (Spielhilfen und Leben)
            Innerhalb der App gibt es virtuelle Elemente wie Spielhilfen („Feenstaub", „Irrlicht") und Leben. Diese haben keinen Geldwert, sind nicht in echtes Geld umwandelbar, nicht übertragbar und können nicht ausgezahlt werden. Ein Anspruch auf eine bestimmte Menge oder eine dauerhafte Verfügbarkeit besteht nicht; der Anbieter kann die Regeln zu Erhalt und Nachwachsen dieser Elemente anpassen.

            § 6 Spielstand und Tageswertung
            Dein Punktestand, deine Tageswertung und deine bisherigen Bestleistungen werden lokal auf deinem Gerät gespeichert. Es gibt aktuell keine geräteübergreifende oder mit anderen Spieler:innen geteilte Rangliste.

            Die Tageswertung sammelt Punkte bis zu einem festen täglichen Stichtag. Danach verfallen die gesammelten Punkte, und es wird eine Belohnung in virtuellen Spielhilfen gutgeschrieben. Ein Anspruch auf den Erhalt gesammelter Punkte über den Stichtag hinaus besteht nicht. Der Anbieter kann Zeitpunkt des Stichtags, Punkteberechnung und Belohnungsstufen anpassen.

            Löschst du die App oder die App-Daten, gehen Spielstand, Tageswertung und Bestleistungen verloren; eine Wiederherstellung ist nicht möglich.

            § 7 Pflichten des Nutzers
            Du verpflichtest dich, die App nicht missbräuchlich zu nutzen, keine Sicherheitsmechanismen zu umgehen und nicht in die Software einzugreifen (z. B. durch Reverse Engineering), soweit dies nicht gesetzlich ausdrücklich erlaubt ist.

            § 8 Zielgruppe und Nutzung durch Minderjährige
            Die Inhalte der App sind gewaltfrei und für jedes Alter unbedenklich; die Alterseinstufung im Store weist die niedrigste Stufe aus. Die App richtet sich mit ihrem Angebot jedoch an Personen ab 13 Jahren und ist kein an Kinder gerichtetes Angebot im Sinne der Play-Store-Familienrichtlinien.

            Minderjährige dürfen die App nur mit Zustimmung ihrer Erziehungsberechtigten nutzen. Erziehungsberechtigte sind für die Nutzung durch ihre Kinder verantwortlich.

            § 9 Verfügbarkeit und Änderungen
            Der Anbieter ist bemüht, die App störungsfrei bereitzustellen, schuldet jedoch keine ununterbrochene Verfügbarkeit. Der Anbieter darf die App weiterentwickeln, ändern, einschränken oder den Betrieb einstellen, soweit dies für dich zumutbar ist.

            § 10 Gewährleistung und Haftung
            Da die App kostenlos bereitgestellt wird, haftet der Anbieter – gleich aus welchem Rechtsgrund – nur für Vorsatz und grobe Fahrlässigkeit. Für die Verletzung wesentlicher Vertragspflichten (Kardinalpflichten) haftet der Anbieter auch bei einfacher Fahrlässigkeit, jedoch begrenzt auf den vertragstypischen, vorhersehbaren Schaden. Die Haftung für Schäden aus der Verletzung des Lebens, des Körpers oder der Gesundheit sowie die Haftung nach dem Produkthaftungsgesetz bleiben unberührt.

            § 11 Datenschutz
            Informationen zum Umgang mit Daten findest du in der separaten Datenschutzerklärung.

            § 12 Änderungen dieser Bedingungen
            Der Anbieter kann diese Nutzungsbedingungen anpassen, sofern dies erforderlich ist (z. B. bei Änderungen der App oder der Rechtslage) und dies für dich zumutbar ist. Die jeweils aktuelle Fassung wird in der App bzw. im Store bereitgestellt.

            § 13 Schlussbestimmungen
            Es gilt das Recht der Bundesrepublik Deutschland unter Ausschluss des UN-Kaufrechts. Zwingende verbraucherschützende Vorschriften des Staates, in dem du deinen gewöhnlichen Aufenthalt hast, bleiben unberührt. Sollte eine Bestimmung dieser Bedingungen unwirksam sein, bleibt die Wirksamkeit der übrigen Bestimmungen unberührt. Der Anbieter ist nicht verpflichtet und nicht bereit, an Streitbeilegungsverfahren vor einer Verbraucherschlichtungsstelle teilzunehmen.

            Stand: August 2026
        """.trimIndent()

        LegalPage.Datenschutz -> """
            Der Schutz deiner Daten ist uns wichtig. Diese Datenschutzerklärung informiert dich darüber, welche Daten bei der Nutzung der App „Fairydoku" verarbeitet werden. Grundsatz: Fairydoku erhebt so wenige Daten wie möglich. Es gibt keine Registrierung und kein Nutzerkonto.

            1. Verantwortlicher
            Verantwortlich für die Datenverarbeitung im Sinne der Datenschutz-Grundverordnung (DSGVO) ist:
            App HUMB UG (haftungsbeschränkt)
            Parkstraße 9
            31188 Holle
            E-Mail: info@humb.ug
            Vertretungsberechtigt: Marco Pilipovic

            2. Grundsatz der Datensparsamkeit
            Fairydoku ist ein reines Logikspiel und kostenlos nutzbar. Wir erheben selbst keine personenbezogenen Daten und betreiben keine eigene Nutzerverwaltung. Eine Anmeldung findet nicht statt. Eine Datenverarbeitung erfolgt im Wesentlichen nur durch den eingebundenen Google-Dienst für Werbung, der im Folgenden beschrieben wird.

            3. Werbung (Google AdMob)
            Zur Finanzierung der kostenlosen App ist Google AdMob (Google Ireland Limited bzw. Google LLC) eingebunden.

            Es gibt keine Werbebanner und keine Anzeigen, die von selbst erscheinen. Werbung läuft ausschließlich als Videoanzeige, die du selbst startest, um dafür eine Spielhilfe oder ein Leben zu erhalten. Wer das nicht nutzt, bekommt in der App keine Werbung zu sehen.

            Das Werbe-SDK wird jedoch beim Start der App geladen und hält eine Anzeige bereit. Dabei können durch Google Geräte- und Nutzungsinformationen sowie eine Werbekennung (Advertising ID) verarbeitet werden, um eine Anzeige auszuliefern und Missbrauch (z. B. Klickbetrug) zu verhindern. Diese Verarbeitung beginnt also bereits mit dem Start der App, nicht erst beim Ansehen eines Videos.

            Jede Anzeigenanfrage ist ausdrücklich als nicht personalisiert gekennzeichnet, und die maximale Inhaltsfreigabe ist auf „G" gesetzt. Eine auf Interessen basierende Werbung findet nicht statt, es werden keine Nutzerprofile gebildet, und Werbung mit Glücksspiel-, Gewalt- oder sexuellen Inhalten ist ausgeschlossen.

            Rechtsgrundlage ist unser berechtigtes Interesse an der Finanzierung der kostenlos bereitgestellten App (Art. 6 Abs. 1 lit. f DSGVO). Weitere Informationen findest du in der Datenschutzerklärung von Google.

            4. Spielstand, Tageswertung und Bestleistungen
            Dein Punktestand, deine Tageswertung und deine bisherigen Bestleistungen werden ausschließlich lokal auf deinem Gerät gespeichert. Es findet keine Übermittlung an uns oder an Dritte statt, und es gibt aktuell keine geräteübergreifende oder mit anderen Spieler:innen geteilte Rangliste.

            Die Tageswertung speichert dazu, wie viele Punkte am laufenden Tag gesammelt wurden, das beste Tagesergebnis und den Zeitpunkt des letzten Tageswechsels. Auch diese Angaben verlassen dein Gerät nicht. Ein Anzeigename und eine Avatar-Fee lassen sich in den Einstellungen hinterlegen; beides wird ebenfalls nur lokal gespeichert und niemandem angezeigt.

            5. Technische Bereitstellung
            Beim Betrieb der App können technisch notwendige Informationen (z. B. Geräteinformationen) anfallen, soweit dies für Auslieferung und Betrieb erforderlich ist. Wir setzen keine Analyse-, Tracking- oder Absturzberichtsdienste ein.

            6. Zielgruppe sowie Hinweise für Eltern
            Die Inhalte der App sind gewaltfrei und für jedes Alter unbedenklich. Als Angebot richtet sich Fairydoku an Personen ab 13 Jahren; es handelt sich nicht um ein an Kinder gerichtetes Angebot im Sinne der Play-Store-Familienrichtlinien, und die App nimmt nicht am Programm „Designed for Families" teil.

            Unabhängig davon wird niemandem personalisierte Werbung ausgeliefert, und es werden keine Profile gebildet. Erziehungsberechtigte können sich bei Fragen jederzeit an die oben genannte Kontaktadresse wenden.

            7. Empfänger und Datenübermittlung in Drittländer
            Empfänger der oben genannten Daten ist Google. Dabei kann es zu einer Übermittlung von Daten in Länder außerhalb der EU/des EWR (insbesondere USA) kommen. Google stützt solche Übermittlungen auf geeignete Garantien (z. B. EU-Standardvertragsklauseln bzw. das EU-US Data Privacy Framework).

            8. Speicherdauer
            Wir selbst speichern keine personenbezogenen Daten. Die Speicherdauer der durch Google verarbeiteten Daten richtet sich nach dessen Datenschutzbestimmungen.

            9. Deine Rechte
            Dir stehen nach der DSGVO folgende Rechte zu:
            Auskunft über die verarbeiteten Daten (Art. 15 DSGVO)
            Berichtigung unrichtiger Daten (Art. 16 DSGVO)
            Löschung (Art. 17 DSGVO)
            Einschränkung der Verarbeitung (Art. 18 DSGVO)
            Datenübertragbarkeit (Art. 20 DSGVO)
            Widerspruch gegen die Verarbeitung (Art. 21 DSGVO)
            Zur Ausübung genügt eine Nachricht an die oben genannte Kontaktadresse. Zudem hast du das Recht, dich bei einer Datenschutz-Aufsichtsbehörde zu beschweren.

            10. Werbung widersprechen oder einschränken
            Einen Einwilligungsdialog zeigt die App nicht, weil ausschließlich nicht personalisierte Werbung ausgeliefert wird. Möchtest du der Verarbeitung durch Google widersprechen, wende dich an die oben genannte Kontaktadresse.

            Unabhängig davon kannst du die Werbekennung deines Geräts jederzeit selbst löschen oder zurücksetzen: in den Android-Einstellungen unter „Datenschutz" bzw. „Google" im Punkt „Anzeigen". Apps erhalten danach keine Werbekennung mehr.

            11. Änderungen dieser Datenschutzerklärung
            Wir passen diese Datenschutzerklärung an, wenn Änderungen an der App oder der Rechtslage dies erforderlich machen. Es gilt die jeweils in der App bzw. im Play Store verlinkte Fassung.

            Stand: August 2026
        """.trimIndent()
    }
}

/** Die drei rechtlich vorgeschriebenen Seiten, von jeder Stelle in maximal zwei Tipps erreichbar. */
enum class LegalPage { Impressum, Agb, Datenschutz }
