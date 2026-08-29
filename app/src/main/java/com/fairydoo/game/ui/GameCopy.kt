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
        FairySpecies.Viridis -> "Waldfee"
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

    /** „Viridis" · „Viridis und Nixie" · „Viridis, Nixie und Chrono" */
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
        LegalPage.Lizenzen -> "Lizenzen"
    }

    /**
     * Die vier Rechtsseiten, wie sie in der App stehen.
     *
     * Die Angaben stammen aus dem Rechtstext-Bestand der Webseite (Stand
     * 18. August 2026) und sind vollständig — keine Platzhalter mehr.
     *
     * ## Was hier bewusst anders steht als in der Vorlage
     *
     * Die Vorlage sprach von „eingeblendeter" Werbung. Die App hat weder Banner
     * noch Anzeigen, die von selbst erscheinen — Werbung läuft ausschließlich
     * als Videoanzeige, die der Spieler selbst startet, um dafür eine
     * Spielhilfe zu erhalten (siehe [com.fairydoo.game.ads.RewardedAdManager]).
     *
     * Wichtiger noch: Bis zum ersten Druck auf einen Werbe-Knopf wird das
     * Werbe-SDK gar nicht erst gestartet. Wer nie ein Video ansieht, bei dem
     * gehen in diesem Zusammenhang keine Daten an Google — und genau das steht
     * jetzt auch da. Ein Datenschutztext, der eine Verarbeitung beschreibt, die
     * bei den meisten Spielern nie stattfindet, wäre unnötig abschreckend.
     *
     * ## Diese Texte und der Code müssen zusammen geändert werden
     *
     * Abschnitt 3 und 10 beschreiben die Einwilligungsabfrage über Googles User
     * Messaging Platform und den Menüpunkt „Datenschutz-Einstellungen ändern".
     * Beides existiert — in [com.fairydoo.game.ads.RewardedAdManager] und im
     * [com.fairydoo.game.ui.components.SettingsOverlay]. Wer eines davon
     * ausbaut, muss hier mit ändern; ein Text, der eine Abfrage verspricht, die
     * nie erscheint, wäre schlechter als gar keiner.
     */
    fun legalBody(page: LegalPage): String = when (page) {
        LegalPage.Impressum -> """
            Angaben gemäß § 5 DDG
            App HUMB UG (haftungsbeschränkt)
            Parkstraße 9
            31188 Holle

            Vertreten durch den Geschäftsführer
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

            Verwendete Schriften
            Cinzel Decorative — Copyright © 2012 Natanael Gama, mit dem reservierten Schriftnamen „Cinzel".
            Quicksand — Copyright © 2019 The Quicksand Project Authors.
            Beide stehen unter der SIL Open Font License, Version 1.1 (scripts.sil.org/OFL).

            Alle Bilder der App sind eigene Werke. Die meisten Klänge werden im Spiel selbst berechnet. Die Waldmusik, der Schreckenslaut und die sechs Kicherlaute der Feen sind mit einem KI-Werkzeug erzeugt und unter dem dort erworbenen Tarif lizenziert.
        """.trimIndent()

        // Kein Haftungsparagraf. Hier stand eine Klausel, die die Haftung auf
        // Vorsatz und grobe Fahrlässigkeit begrenzte; sie ist am 24. August
        // 2026 auf Weisung entfernt worden. Damit gilt die gesetzliche Haftung
        // ungekürzt — das ist eine bewusste Entscheidung des Anbieters und
        // keine Lücke, die jemand später „reparieren" sollte. Wer sie wieder
        // aufnehmen will, sollte sie vorher anwaltlich prüfen lassen.
        //
        // Die früheren §§ 11 bis 13 sind dadurch zu §§ 10 bis 12 geworden.
        // Querverweise gab es keine, jeder Paragraf wird nur einmal genannt.
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

            § 10 Datenschutz
            Informationen zum Umgang mit Daten findest du in der separaten Datenschutzerklärung.

            § 11 Änderungen dieser Bedingungen
            Der Anbieter kann diese Nutzungsbedingungen anpassen, sofern dies erforderlich ist (z. B. bei Änderungen der App oder der Rechtslage) und dies für dich zumutbar ist. Die jeweils aktuelle Fassung wird in der App bzw. im Store bereitgestellt.

            § 12 Schlussbestimmungen
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
            Vertreten durch den Geschäftsführer: Marco Pilipovic

            2. Grundsatz der Datensparsamkeit
            Fairydoku ist ein reines Logikspiel und kostenlos nutzbar. Wir erheben selbst keine personenbezogenen Daten und betreiben keine eigene Nutzerverwaltung. Eine Anmeldung findet nicht statt. Eine Datenverarbeitung erfolgt im Wesentlichen nur durch den eingebundenen Google-Dienst für Werbung, der im Folgenden beschrieben wird.

            3. Werbung (Google AdMob)
            Zur Finanzierung der kostenlosen App ist Google AdMob (Google Ireland Limited bzw. Google LLC) eingebunden.

            Es gibt keine Werbebanner und keine Anzeigen, die von selbst erscheinen. Werbung läuft ausschließlich als Videoanzeige, die du selbst startest, um dafür eine Spielhilfe oder ein Leben zu erhalten.

            Solange du das nicht tust, passiert nichts: Das Werbe-SDK wird gar nicht erst gestartet, es wird keine Anzeige geladen, und es gehen keine Daten an Google. Wer nie ein Werbevideo ansieht, bei dem verlässt in diesem Zusammenhang nichts das Gerät.

            Drückst du zum ersten Mal auf einen Werbe-Knopf, fragen wir zuvor deine Einwilligung ab. Dafür ist das von Google zertifizierte Einwilligungswerkzeug (User Messaging Platform) eingebunden. Erst nach erteilter Einwilligung startet das Werbe-SDK und lädt eine Anzeige. Ohne Einwilligung wird keine Anzeige angefragt; die App bleibt vollständig spielbar, es entfällt lediglich die Möglichkeit, für eine Belohnung ein Video anzusehen.

            Bei der Auslieferung können durch Google Geräte- und Nutzungsinformationen sowie eine Werbekennung (Advertising ID) verarbeitet werden, um die Anzeige auszuliefern und Missbrauch (z. B. Klickbetrug) zu verhindern.

            Jede Anzeigenanfrage ist ausdrücklich als nicht personalisiert gekennzeichnet, und die maximale Inhaltsfreigabe ist auf „G" gesetzt. Eine auf Interessen basierende Werbung findet nicht statt, es werden keine Nutzerprofile gebildet, und Werbung mit Glücksspiel-, Gewalt- oder sexuellen Inhalten ist ausgeschlossen.

            Rechtsgrundlage ist deine Einwilligung (Art. 6 Abs. 1 lit. a DSGVO). Du kannst sie jederzeit mit Wirkung für die Zukunft ändern oder zurücknehmen — siehe Abschnitt 10. Weitere Informationen findest du in der Datenschutzerklärung von Google.

            4. Spielstand, Tageswertung und Bestleistungen
            Dein Punktestand, deine Tageswertung und deine bisherigen Bestleistungen werden lokal auf deinem Gerät gespeichert. Eine Übermittlung an uns findet nicht statt, und es gibt aktuell keine geräteübergreifende oder mit anderen Spieler:innen geteilte Rangliste.

            Die Tageswertung speichert dazu, wie viele Punkte am laufenden Tag gesammelt wurden, das beste Tagesergebnis und den Zeitpunkt des letzten Tageswechsels. Ein Anzeigename und eine Avatar-Fee lassen sich in den Einstellungen hinterlegen; beides wird ebenfalls nur lokal gespeichert und niemandem angezeigt.

            Eine Ausnahme, die wir offen nennen wollen: Android sichert App-Daten auf Wunsch in deinem eigenen Google-Konto („Automatische Datensicherung"), und Fairydoku nimmt daran teil. Dadurch findest du deinen Spielstand auf einem neuen Telefon wieder. Diese Sicherung liegt in deinem Konto, nicht bei uns — wir haben darauf keinen Zugriff. Abschalten kannst du sie in den Android-Einstellungen unter „Sicherung" bzw. „Google – Datensicherung".

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
            • Auskunft über die verarbeiteten Daten (Art. 15 DSGVO)
            • Berichtigung unrichtiger Daten (Art. 16 DSGVO)
            • Löschung (Art. 17 DSGVO)
            • Einschränkung der Verarbeitung (Art. 18 DSGVO)
            • Datenübertragbarkeit (Art. 20 DSGVO)
            • Widerspruch gegen die Verarbeitung (Art. 21 DSGVO)
            Zur Ausübung genügt eine Nachricht an die oben genannte Kontaktadresse. Zudem hast du das Recht, dich bei einer Datenschutz-Aufsichtsbehörde zu beschweren.

            10. Einwilligung ändern oder zurücknehmen
            Deine Wahl zur Werbung kannst du jederzeit ändern: in den Einstellungen der App unter „Datenschutz-Einstellungen ändern". Der Punkt erscheint dort, sobald eine Einwilligung abgefragt wurde. Nimmst du sie zurück, wird ab diesem Zeitpunkt keine Werbung mehr ausgeliefert.

            Möchtest du der Verarbeitung darüber hinaus widersprechen, wende dich an die oben genannte Kontaktadresse.

            Unabhängig davon kannst du die Werbekennung deines Geräts jederzeit selbst löschen oder zurücksetzen: in den Android-Einstellungen unter „Datenschutz" bzw. „Google" im Punkt „Anzeigen". Apps erhalten danach keine Werbekennung mehr.

            11. Änderungen dieser Datenschutzerklärung
            Wir passen diese Datenschutzerklärung an, wenn Änderungen an der App oder der Rechtslage dies erforderlich machen. Es gilt die jeweils in der App bzw. im Play Store verlinkte Fassung.

            Stand: August 2026
        """.trimIndent()
        // Die Lizenztexte der mitgelieferten fremden Bestandteile.
        //
        // Kein Beiwerk, sondern Bedingung: Sowohl die SIL Open Font License
        // als auch die Apache-Lizenz erlauben das Mitliefern nur, sofern jede
        // Kopie den Lizenztext enthält. Die APK ist eine solche Kopie — ein
        // Verweis auf eine Webadresse ist nicht die Lizenz.
        //
        // Der Text ist nicht abgetippt, sondern von den Seiten der Lizenzgeber
        // geholt und nur neu umbrochen; die Copyright-Vermerke stammen aus den
        // Namenstabellen der Schriftdateien selbst. Beides bleibt im
        // englischen Original: Eine Übersetzung wäre nicht die Lizenz.
        //
        // Nicht aufgeführt sind Google Mobile Ads und die User Messaging
        // Platform. Sie sind nicht quelloffen und laufen unter Googles eigenen
        // Bedingungen, die keine Weitergabe eines Lizenztexts verlangen.
        //
        // Kommt später ein fremder Bestandteil dazu, gehört er hierher.
        LegalPage.Lizenzen -> """
            Fairydoku benutzt fremde Bestandteile: zwei Schriften und mehrere Programmbibliotheken. Deren Urheber erlauben das ausdrücklich — sie verlangen aber, dass ihr Lizenztext mitgeliefert wird. Genau dafür ist diese Seite da.

            Die Lizenzen stehen im englischen Original. Eine Übersetzung wäre nicht die Lizenz, sondern eine Nacherzählung davon.

            Schriften
            Copyright © 2012 Natanael Gama (info@ndiscovered.com), with Reserved Font Name 'Cinzel'
            Copyright 2019 The Quicksand Project Authors (https://github.com/andrew-paglinawan/QuicksandFamily.git), with Reserved Font Name "Quicksand"

            Beide stehen unter der SIL Open Font License, Version 1.1. Ihr vollständiger Text folgt.

            SIL OPEN FONT LICENSE Version 1.1 - 26 February 2007

            PREAMBLE

            The goals of the Open Font License (OFL) are to stimulate worldwide development of collaborative font projects, to support the font creation efforts of academic and linguistic communities, and to provide a free and open framework in which fonts may be shared and improved in partnership with others.

            The OFL allows the licensed fonts to be used, studied, modified and redistributed freely as long as they are not sold by themselves. The fonts, including any derivative works, can be bundled, embedded, redistributed and/or sold with any software provided that any reserved names are not used by derivative works. The fonts and derivatives, however, cannot be released under any other type of license. The requirement for fonts to remain under this license does not apply to any document created using the fonts or their derivatives.

            DEFINITIONS

            "Font Software" refers to the set of files released by the Copyright Holder(s) under this license and clearly marked as such. This may include source files, build scripts and documentation.

            "Reserved Font Name" refers to any names specified as such after the copyright statement(s).

            "Original Version" refers to the collection of Font Software components as distributed by the Copyright Holder(s).

            "Modified Version" refers to any derivative made by adding to, deleting, or substituting -- in part or in whole -- any of the components of the Original Version, by changing formats or by porting the Font Software to a new environment.

            "Author" refers to any designer, engineer, programmer, technical writer or other person who contributed to the Font Software.

            PERMISSION & CONDITIONS

            Permission is hereby granted, free of charge, to any person obtaining a copy of the Font Software, to use, study, copy, merge, embed, modify, redistribute, and sell modified and unmodified copies of the Font Software, subject to the following conditions:

            1) Neither the Font Software nor any of its individual components, in Original or Modified Versions, may be sold by itself.

            2) Original or Modified Versions of the Font Software may be bundled, redistributed and/or sold with any software, provided that each copy contains the above copyright notice and this license. These can be included either as stand-alone text files, human-readable headers or in the appropriate machine-readable metadata fields within text or binary files as long as those fields can be easily viewed by the user.

            3) No Modified Version of the Font Software may use the Reserved Font Name(s) unless explicit written permission is granted by the corresponding Copyright Holder. This restriction only applies to the primary font name as presented to the users.

            4) The name(s) of the Copyright Holder(s) or the Author(s) of the Font Software shall not be used to promote, endorse or advertise any Modified Version, except to acknowledge the contribution(s) of the Copyright Holder(s) and the Author(s) or with their explicit written permission.

            5) The Font Software, modified or unmodified, in part or in whole, must be distributed entirely under this license, and must not be distributed under any other license. The requirement for fonts to remain under this license does not apply to any document created using the Font Software.

            TERMINATION

            This license becomes null and void if any of the above conditions are not met.

            DISCLAIMER

            THE FONT SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF COPYRIGHT, PATENT, TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, INCLUDING ANY GENERAL, SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF THE USE OR INABILITY TO USE THE FONT SOFTWARE OR FROM OTHER DEALINGS IN THE FONT SOFTWARE.

            Programmbibliotheken
            Oberfläche, Bewegungsabläufe und Datenhaltung stützen sich auf freie Bibliotheken von Google und JetBrains: AndroidX, Jetpack Compose, DataStore und kotlinx.serialization. Sie stehen unter der Apache License, Version 2.0. Ihr vollständiger Text folgt.

            Nicht darunter fallen Googles Werbebausteine — Google Mobile Ads und die User Messaging Platform, über die das Belohnungsvideo und die Einwilligungsabfrage laufen. Sie sind nicht quelloffen und werden unter Googles eigenen Bedingungen bereitgestellt, die keine Weitergabe eines Lizenztexts verlangen.

            Apache License Version 2.0, January 2004 http://www.apache.org/licenses/

            TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

            1. Definitions.

            "License" shall mean the terms and conditions for use, reproduction, and distribution as defined by Sections 1 through 9 of this document.

            "Licensor" shall mean the copyright owner or entity authorized by the copyright owner that is granting the License.

            "Legal Entity" shall mean the union of the acting entity and all other entities that control, are controlled by, or are under common control with that entity. For the purposes of this definition, "control" means (i) the power, direct or indirect, to cause the direction or management of such entity, whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial ownership of such entity.

            "You" (or "Your") shall mean an individual or Legal Entity exercising permissions granted by this License.

            "Source" form shall mean the preferred form for making modifications, including but not limited to software source code, documentation source, and configuration files.

            "Object" form shall mean any form resulting from mechanical transformation or translation of a Source form, including but not limited to compiled object code, generated documentation, and conversions to other media types.

            "Work" shall mean the work of authorship, whether in Source or Object form, made available under the License, as indicated by a copyright notice that is included in or attached to the work (an example is provided in the Appendix below).

            "Derivative Works" shall mean any work, whether in Source or Object form, that is based on (or derived from) the Work and for which the editorial revisions, annotations, elaborations, or other modifications represent, as a whole, an original work of authorship. For the purposes of this License, Derivative Works shall not include works that remain separable from, or merely link (or bind by name) to the interfaces of, the Work and Derivative Works thereof.

            "Contribution" shall mean any work of authorship, including the original version of the Work and any modifications or additions to that Work or Derivative Works thereof, that is intentionally submitted to Licensor for inclusion in the Work by the copyright owner or by an individual or Legal Entity authorized to submit on behalf of the copyright owner. For the purposes of this definition, "submitted" means any form of electronic, verbal, or written communication sent to the Licensor or its representatives, including but not limited to communication on electronic mailing lists, source code control systems, and issue tracking systems that are managed by, or on behalf of, the Licensor for the purpose of discussing and improving the Work, but excluding communication that is conspicuously marked or otherwise designated in writing by the copyright owner as "Not a Contribution."

            "Contributor" shall mean Licensor and any individual or Legal Entity on behalf of whom a Contribution has been received by Licensor and subsequently incorporated within the Work.

            2. Grant of Copyright License.

            Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform, sublicense, and distribute the Work and such Derivative Works in Source or Object form.

            3. Grant of Patent License.

            Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable (except as stated in this section) patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer the Work, where such license applies only to those patent claims licensable by such Contributor that are necessarily infringed by their Contribution(s) alone or by combination of their Contribution(s) with the Work to which such Contribution(s) was submitted. If You institute patent litigation against any entity (including a cross-claim or counterclaim in a lawsuit) alleging that the Work or a Contribution incorporated within the Work constitutes direct or contributory patent infringement, then any patent licenses granted to You under this License for that Work shall terminate as of the date such litigation is filed.

            4. Redistribution.

            You may reproduce and distribute copies of the Work or Derivative Works thereof in any medium, with or without modifications, and in Source or Object form, provided that You meet the following conditions:

            (a) You must give any other recipients of the Work or Derivative Works a copy of this License; and

            (b) You must cause any modified files to carry prominent notices stating that You changed the files; and

            (c) You must retain, in the Source form of any Derivative Works that You distribute, all copyright, patent, trademark, and attribution notices from the Source form of the Work, excluding those notices that do not pertain to any part of the Derivative Works; and

            (d) If the Work includes a "NOTICE" text file as part of its distribution, then any Derivative Works that You distribute must include a readable copy of the attribution notices contained within such NOTICE file, excluding those notices that do not pertain to any part of the Derivative Works, in at least one of the following places: within a NOTICE text file distributed as part of the Derivative Works; within the Source form or documentation, if provided along with the Derivative Works; or, within a display generated by the Derivative Works, if and wherever such third-party notices normally appear. The contents of the NOTICE file are for informational purposes only and do not modify the License. You may add Your own attribution notices within Derivative Works that You distribute, alongside or as an addendum to the NOTICE text from the Work, provided that such additional attribution notices cannot be construed as modifying the License.

            You may add Your own copyright statement to Your modifications and may provide additional or different license terms and conditions for use, reproduction, or distribution of Your modifications, or for any such Derivative Works as a whole, provided Your use, reproduction, and distribution of the Work otherwise complies with the conditions stated in this License.

            5. Submission of Contributions.

            Unless You explicitly state otherwise, any Contribution intentionally submitted for inclusion in the Work by You to the Licensor shall be under the terms and conditions of this License, without any additional terms or conditions. Notwithstanding the above, nothing herein shall supersede or modify the terms of any separate license agreement you may have executed with Licensor regarding such Contributions.

            6. Trademarks.

            This License does not grant permission to use the trade names, trademarks, service marks, or product names of the Licensor, except as required for reasonable and customary use in describing the origin of the Work and reproducing the content of the NOTICE file.

            7. Disclaimer of Warranty.

            Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

            8. Limitation of Liability.

            In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by applicable law (such as deliberate and grossly negligent acts) or agreed to in writing, shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use the Work (including but not limited to damages for loss of goodwill, work stoppage, computer failure or malfunction, or any and all other commercial damages or losses), even if such Contributor has been advised of the possibility of such damages.

            9. Accepting Warranty or Additional Liability.

            While redistributing the Work or Derivative Works thereof, You may choose to offer, and charge a fee for, acceptance of support, warranty, indemnity, or other liability obligations and/or rights consistent with this License. However, in accepting such obligations, You may act only on Your own behalf and on Your sole responsibility, not on behalf of any other Contributor, and only if You agree to indemnify, defend, and hold each Contributor harmless for any liability incurred by, or claims asserted against, such Contributor by reason of your accepting any such warranty or additional liability.

            END OF TERMS AND CONDITIONS

            APPENDIX: How to apply the Apache License to your work.

            To apply the Apache License to your work, attach the following boilerplate notice, with the fields enclosed by brackets "[]" replaced with your own identifying information. (Don't include the brackets!)  The text should be enclosed in the appropriate comment syntax for the file format. We also recommend that a file or class name and description of purpose be included on the same "printed page" as the copyright notice for easier identification within third-party archives.

            Copyright [yyyy] [name of copyright owner]

            Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

            Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
        """.trimIndent()
    }
}

/**
 * Die Rechtliches-Seiten, von jeder Stelle in maximal zwei Tipps erreichbar.
 *
 * Die ersten drei sind gesetzlich vorgeschrieben. [Lizenzen] kommt aus den
 * Lizenzen der mitgelieferten Schriften und Bibliotheken — sie erlauben das
 * Mitliefern nur, wenn ihr Text beiliegt.
 */
enum class LegalPage { Impressum, Agb, Datenschutz, Lizenzen }
