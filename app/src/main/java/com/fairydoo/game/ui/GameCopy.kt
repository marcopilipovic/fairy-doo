package com.fairydoo.game.ui

import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameOverReason
import com.fairydoo.game.game.StatusMessage

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
        StatusMessage.Hint -> "Tippen: ✕ · gedrückt halten: 🧚"
        // Alle Meldungen bewusst kurz: Die Zeile hat Platz für zwei Zeilen,
        // aber bei vergrößerter Systemschrift reicht auch der nicht mehr, und
        // dann wird mitten im Satz abgeschnitten. Auf dem Gerät stand so
        // „✨ Der Feenstaub zeigt dir ein sicheres …" — die Meldung erklärte
        // gerade das, was sie selbst nicht mehr zeigen konnte.
        //
        // Knapp gehalten: Das ist die mit Abstand längste Meldung, und je
        // kürzer sie ist, desto seltener bricht sie auf zwei Zeilen um.
        is StatusMessage.Zone ->
            "${zoneName(message.regionIndex)} · ${fairyIntroduction(message.species)}"
        StatusMessage.MistakeMade -> "⚡ Die Zauberkräfte stören sich! (−1 Leben)"
        StatusMessage.FairyDustUsed -> "✨ Ein sicheres Feld mit Fee!"
        is StatusMessage.NoFairyDust -> {
            val minutes = (message.nextInMillis / 60_000L).toInt() + 1
            "Kein Feenstaub — neuer in ~$minutes Min."
        }
        StatusMessage.IrrlichtUsed -> "🔮 Hier wohnt sicher keine Fee!"
        is StatusMessage.NoIrrlicht -> {
            val minutes = (message.nextInMillis / 60_000L).toInt() + 1
            "Kein Irrlicht — neues in ~$minutes Min."
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
     *
     * „Dichter" wird das Gitter aber nur jedes zweite Level — es wächst nach
     * `sizeForLevel` alle zwei Stufen um ein Feld. Stand dort früher trotzdem
     * „Der Wald wird dichter: 5×5-Gitter", während gerade eben schon auf 5×5
     * gespielt wurde, klang das nach einem Versprechen, das das nächste Level
     * nicht hält. Deshalb entscheidet [sizeGrew], welcher Satz erscheint.
     */
    fun nextLevelTeaser(
        nextSize: Int,
        newcomers: List<FairySpecies>,
        sizeGrew: Boolean,
    ): String {
        val grid = "$nextSize×$nextSize-Gitter"
        val opening = if (sizeGrew) "Der Wald wird dichter: $grid" else "Weiter geht's im $grid"

        if (newcomers.isEmpty()) {
            return if (sizeGrew) {
                "Der Wald wird dichter: ein neues $grid erwartet dich…"
            } else {
                "Der Pfad führt weiter — das nächste $grid wartet…"
            }
        }

        val shown = newcomers.take(MAX_TEASER_NAMES).map { it.displayName }
        val hidden = newcomers.size - shown.size
        val names = enumerate(if (hidden > 0) shown + "$hidden weitere" else shown)
        val verb = if (newcomers.size == 1) "wartet" else "warten"
        return "$opening — $names $verb schon…"
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
     * Platzhaltertexte — vor Veröffentlichung durch echte, juristisch geprüfte
     * Texte ersetzen (Anbieterkennzeichnung nach § 5 TMG/DDG, Datenschutzerklärung
     * nach DSGVO Art. 13).
     */
    fun legalBody(page: LegalPage): String = when (page) {
        // Entwurf, kein geprüfter Rechtstext — alle [eckigen Klammern] sind
        // von Nataly auszufüllen; nicht zutreffende Punkte (z. B. USt-IdNr.)
        // kann sie beim Ausfüllen entfernen.
        LegalPage.Impressum -> """
            ENTWURF – keine Rechtsberatung. Vorlage mit Pflichtangaben; bitte alle Felder in [eckigen Klammern] ausfüllen und vor Veröffentlichung prüfen lassen. Nicht zutreffende Punkte (z. B. USt-IdNr.) können entfallen.

            Angaben gemäß § 5 DDG
            App HUMB UG (haftungsbeschränkt)
            Parkstraße 9
            31188 Holle

            Vertreten durch
            Marco Pilipovic

            Kontakt
            Telefon: [Telefonnummer]
            E-Mail: [E-Mail-Adresse]

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

            Hinweis: Dieser Text ist ein Entwurf und bedarf vor Veröffentlichung noch der Überarbeitung und rechtlichen Prüfung.
        """.trimIndent()
        // Entwurf, kein geprüfter Rechtstext — App HUMB UG (haftungsbeschränkt), Parkstraße 9, 31188 Holle und
        // August 2026 sind von Nataly auszufüllen. Gegenüber der Vorlage
        // zweifach an das tatsächliche Spiel angepasst: kein Elternschutz
        // (§ 4, § 8 — bewusst nicht gebaut, siehe Zielgruppen-Anpassung im
        // Play Store) und keine Google-Play-Games-Rangliste (§ 6 — Bestleistung
        // liegt nur lokal auf dem Gerät, es gibt keine geräteübergreifende
        // oder geteilte Rangliste).
        //
        // Zielgruppe ab 13, nicht an Kinder gerichtet (§ 8): Das entkoppelt die
        // harmlose Inhaltseinstufung von den Familienrichtlinien und hält den
        // Weg zu Play Games und einer Online-Rangliste offen. Ein an Kinder
        // gerichtetes Angebot dürfte fremde Anzeigenamen kaum zeigen.
        //
        // Die Tageswertung ist in § 6 beschrieben, weil verfallende Punkte eine
        // Erwartung berühren: Wer sammelt, soll vorher wissen, dass der Stand
        // am Stichtag zurückgesetzt wird.
        //
        // Der Entwurf für die spätere Fassung mit Online-Rangliste liegt in
        // RECHTSTEXTE-RANGLISTE.md — dieser Text hier beschreibt die App, wie
        // sie heute ist.
        LegalPage.Agb -> """
            ENTWURF – keine Rechtsberatung. Dieser Text ist eine sorgfältige Vorlage, ersetzt aber keine anwaltliche Prüfung. Da die App werbefinanziert ist und auch von Jugendlichen genutzt wird, sollte ein Fachanwalt (IT-/Datenschutzrecht) den Text vor Veröffentlichung freigeben. Alle Angaben in [eckigen Klammern] bitte ausfüllen.

            § 1 Geltungsbereich und Anbieter
            Diese Nutzungsbedingungen gelten für die Nutzung der mobilen App „Fairydoku" (nachfolgend „App"), angeboten von App HUMB UG (haftungsbeschränkt), Parkstraße 9, 31188 Holle (nachfolgend „Anbieter"). Mit der Installation und Nutzung der App erkennst du diese Bedingungen an.

            § 2 Gegenstand
            Die App ist ein kostenloses Logikrätselspiel. Der Anbieter stellt die App zur privaten, nicht-kommerziellen Nutzung zur Verfügung.

            § 3 Nutzungsrecht
            Der Anbieter räumt dir ein einfaches, nicht übertragbares und widerrufliches Recht ein, die App auf deinen Geräten für private Zwecke zu nutzen. Eine Bearbeitung, Vervielfältigung, Verbreitung oder das Zugänglichmachen der App oder ihrer Inhalte über die private Nutzung hinaus ist nicht gestattet.

            § 4 Kosten und Werbung
            Die Nutzung der App ist kostenlos. Die App finanziert sich über Werbung. Zusätzlich kann der Nutzer freiwillig kurze Werbevideos ansehen, um Spielhilfen oder ein Leben zu erhalten (Belohnungsvideos). Diese Option wird angeboten, sobald Level 10 abgeschlossen ist. Eine Verpflichtung, Werbung anzusehen, besteht nicht.

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

            Hinweis: Dieser Text ist ein Entwurf und bedarf vor Veröffentlichung noch der Überarbeitung und rechtlichen Prüfung.
        """.trimIndent()
        // Entwurf, kein geprüfter Rechtstext — App HUMB UG (haftungsbeschränkt) usw. sind von
        // Nataly auszufüllen. Gegenüber der Vorlage an das tatsächliche Spiel
        // angepasst: keine Google-Play-Games-Rangliste (Abschnitt 2 + 4 —
        // Bestleistung liegt nur lokal auf dem Gerät), kein Firebase Remote
        // Config (Abschnitt 5 — Werbe-Freischaltung ist fest im Code
        // verdrahtet), kein Elternschutz (Abschnitt 6 — bewusst nicht gebaut)
        // und kein Einwilligungsdialog/keine Einwilligungseinstellungen
        // (Abschnitt 3 + 10 — es gibt kein Google-UMP-SDK im Code, nur
        // durchgehend nicht personalisierte Werbung).
        //
        // Abschnitt 3 und 10 beschreiben inzwischen ein tatsächlich vorhandenes
        // Einwilligungswerkzeug: Googles UMP-SDK ist eingebunden (siehe
        // AdConsentManager.kt), ohne Einwilligung wird gar nicht erst geladen,
        // und der Widerruf liegt in den Einstellungen. Damit entfällt der
        // frühere Vorbehalt „ob das ausreicht, sollte geprüft werden".
        //
        // Abschnitt 4 beschreibt zusätzlich die Tageswertung — Tagespunkte,
        // bestes Tagesergebnis und letzter Tageswechsel, ebenfalls
        // ausschließlich lokal (siehe DailyCycle.kt).
        //
        // Abschnitt 6 folgt der Zielgruppen-Entscheidung „ab 13": Die Inhalte
        // bleiben unbedenklich, das Angebot ist aber keins für Kinder im Sinne
        // der Familienrichtlinien. Der Entwurf für die spätere Fassung mit
        // Online-Rangliste liegt in RECHTSTEXTE-RANGLISTE.md.
        LegalPage.Datenschutz -> """
            ENTWURF – keine Rechtsberatung. Dieser Text ist eine sorgfältige Vorlage, ersetzt aber keine anwaltliche Prüfung. Da die App werbefinanziert ist und auch von Jugendlichen genutzt wird, sollte ein Fachanwalt (IT-/Datenschutzrecht) den Text vor Veröffentlichung freigeben. Alle Angaben in [eckigen Klammern] bitte ausfüllen.

            Der Schutz deiner Daten ist uns wichtig. Diese Datenschutzerklärung informiert dich darüber, welche Daten bei der Nutzung der App „Fairydoku" verarbeitet werden. Grundsatz: Fairydoku erhebt so wenige Daten wie möglich. Es gibt keine Registrierung und kein Nutzerkonto.

            1. Verantwortlicher
            Verantwortlich für die Datenverarbeitung im Sinne der Datenschutz-Grundverordnung (DSGVO) ist:
            App HUMB UG (haftungsbeschränkt)
            Parkstraße 9
            31188 Holle
            E-Mail: [E-Mail-Adresse]
            Vertretungsberechtigt: Marco Pilipovic

            2. Grundsatz der Datensparsamkeit
            Fairydoku ist ein reines Logikspiel und kostenlos nutzbar. Wir erheben selbst keine personenbezogenen Daten und betreiben keine eigene Nutzerverwaltung. Eine Anmeldung findet nicht statt. Eine Datenverarbeitung erfolgt im Wesentlichen nur durch den eingebundenen Google-Dienst für Werbung, der im Folgenden beschrieben wird.

            3. Werbung (Google AdMob)
            Zur Finanzierung der kostenlosen App wird Werbung über Google AdMob (Google Ireland Limited bzw. Google LLC) eingeblendet. Dabei können durch Google Geräte- und Nutzungsinformationen sowie ggf. eine Werbekennung (Advertising ID) verarbeitet werden, um Werbung auszuliefern und Missbrauch (z. B. Klickbetrug) zu verhindern.

            AdMob ist so konfiguriert, dass ausschließlich nicht personalisierte Werbung mit der niedrigsten Inhaltsfreigabe („G") ausgeliefert wird. Werbung mit Glücksspiel-, Gewalt- oder sexuellen Inhalten ist damit ausgeschlossen. Eine auf Interessen basierende (personalisierte) Werbung findet nicht statt.

Für Nutzerinnen und Nutzer im Europäischen Wirtschaftsraum und im Vereinigten Königreich holen wir vor der ersten Werbeauslieferung eine Einwilligung ein. Dafür ist das von Google zertifizierte Einwilligungswerkzeug (User Messaging Platform) eingebunden. Ohne erteilte Einwilligung wird keine Anzeige angefragt und keine Werbung ausgeliefert; die App bleibt vollständig spielbar, es entfällt lediglich die Möglichkeit, für eine Belohnung freiwillig ein Werbevideo anzusehen.

            Rechtsgrundlage ist deine Einwilligung (Art. 6 Abs. 1 lit. a DSGVO). Du kannst sie jederzeit mit Wirkung für die Zukunft ändern oder zurücknehmen — in den Einstellungen der App unter „Datenschutz-Einstellungen ändern". Weitere Informationen: Google-Datenschutzerklärung.

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

            10. Einwilligung ändern oder zurücknehmen
            Deine Wahl zur Werbung kannst du jederzeit ändern: in den Einstellungen der App unter „Datenschutz-Einstellungen ändern". Der Punkt erscheint dort, wo eine Einwilligung erforderlich ist. Nimmst du sie zurück, wird ab diesem Zeitpunkt keine Werbung mehr ausgeliefert. Möchtest du der Verarbeitung darüber hinaus widersprechen, wende dich an die oben genannte Kontaktadresse.

            11. Änderungen dieser Datenschutzerklärung
            Wir passen diese Datenschutzerklärung an, wenn Änderungen an der App oder der Rechtslage dies erforderlich machen. Es gilt die jeweils in der App bzw. im Play Store verlinkte Fassung.

            Stand: August 2026

            Hinweis: Dieser Text ist ein Entwurf und bedarf vor Veröffentlichung noch der Überarbeitung und rechtlichen Prüfung.
        """.trimIndent()
    }
}

/** Die drei rechtlich vorgeschriebenen Seiten, von jeder Stelle in maximal zwei Tipps erreichbar. */
enum class LegalPage { Impressum, Agb, Datenschutz }
