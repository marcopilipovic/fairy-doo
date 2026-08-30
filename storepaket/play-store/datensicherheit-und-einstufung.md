# Datensicherheit und Alterseinstufung — Vorschläge zum Ausfüllen

Zwei Fragebögen in der Play Console, die beide gern falsch ausgefüllt werden.
Hier steht, was **die App tatsächlich tut** und wie das jeweils zu beantworten
wäre. Prüft es gegen, bevor ihr absendet — falsche Angaben führen zur
Sperrung, und niemand außer euch haftet dafür.

---

## Was die App tatsächlich tut

Das ist die Grundlage für beide Fragebögen:

- **Keine eigene Datenerhebung.** Kein Konto, keine Anmeldung, kein
  Analysedienst, kein Absturzberichtsdienst. Spielstand, Tageswertung,
  Bestleistung, Spielername und Avatar-Fee liegen ausschließlich lokal.
- **Ein einziger Fremddienst: Google AdMob.** Werbung erscheint nie von selbst
  — es gibt weder Banner noch Unterbrechungen. Sie läuft ausschließlich als
  Video, das der Spieler selbst startet, um dafür Feenstaub, ein Irrlicht oder
  ein Leben zu bekommen.
- **Das Werbe-SDK startet erst beim ersten Druck auf einen Werbe-Knopf.** Vorher
  wird es gar nicht geladen. Wer nie Werbung ansieht, bei dem gehen keine Daten
  an Google.
- **Vor dem ersten Start wird die Einwilligung eingeholt** (Googles User
  Messaging Platform). Ohne Einwilligung wird keine Anzeige angefragt.
- **Werbung ist immer nicht personalisiert** (`npa=1`), Inhaltsfreigabe „G".
- Zurücknehmen geht jederzeit in den App-Einstellungen unter
  „Datenschutz-Einstellungen ändern".

---

## Fragebogen „Datensicherheit"

### Erhebt oder teilt eure App eine der geforderten Datenarten?

**Ja** — wegen AdMob. Auch wenn ihr selbst nichts erhebt: Was ein eingebundenes
SDK erhebt, gilt als eure Erhebung.

### Welche Datenarten

| Datenart | Erhoben | Geteilt | Zweck | Pflicht? |
| --- | --- | --- | --- | --- |
| Geräte- oder andere IDs | Ja | Ja (Google) | Werbung, Betrugsvermeidung | **optional** |
| App-Interaktionen | Ja | Ja (Google) | Werbung | **optional** |

„Optional" ist hier wichtig und stimmt auch: Es gibt eine Einwilligungsabfrage,
und ohne Werbung ist die App vollständig spielbar.

**Nicht** anzukreuzen: Name, E-Mail, Anschrift, Telefonnummer, Standort,
Kontakte, Fotos, Dateien, Kalender, Gesundheitsdaten, Zahlungsdaten,
Sprachaufnahmen, Nachrichten. Nichts davon wird angefasst.

Zum Feld „Name" eine Klarstellung, falls jemand nachfragt: In den Einstellungen
lässt sich ein **Anzeigename** eintragen, der in der eigenen Tageswertung
erscheint. Er wird ausschließlich auf dem Gerät gespeichert und an niemanden
übertragen — auch nicht an uns. Googles Fragebogen fragt nach Erhebung und
Weitergabe, nicht nach lokalem Speichern; das Feld bleibt deshalb leer.

**Spielstand und Bestleistung nicht als „erhoben" angeben** — sie verlassen das
Gerät nicht. Der Fragebogen fragt nach Übertragung, nicht nach lokalem
Speichern.

### Sicherheitsangaben

| Frage | Antwort |
| --- | --- |
| Werden Daten bei der Übertragung verschlüsselt? | Ja |
| Können Nutzer die Löschung ihrer Daten verlangen? | Nein — wir speichern keine. App-Daten löschen genügt und liegt beim Nutzer. |
| Ist die App für Familien / an Kinder gerichtet? | **Nein** — ab 13 Jahren |

---

## Fragebogen zur Alterseinstufung (IARC)

Die Inhalte sind harmlos; die Fragen sind trotzdem der Reihe nach mit **Nein**
zu beantworten:

| Frage | Antwort |
| --- | --- |
| Gewalt jeder Art | Nein |
| Sexualität, Nacktheit | Nein |
| Schimpfwörter, Diskriminierung | Nein |
| Drogen, Alkohol, Tabak | Nein |
| Glücksspiel oder Simulation davon | Nein |
| Angsteinflößende Inhalte | Nein |
| Nutzer können miteinander kommunizieren | Nein |
| Standort wird mit anderen Nutzern geteilt | Nein |
| Nutzergenerierte Inhalte | Nein |
| Käufe im Spiel | Nein |
| **Enthält Werbung** | **Ja** |

Erwartetes Ergebnis: USK 0 bzw. PEGI 3, mit dem Hinweis „Enthält Werbung".

**Die Zielgruppenfrage getrennt davon beantworten.** Alterseinstufung und
Zielgruppe sind zwei verschiedene Dinge, und genau hier passieren die Fehler:
Die Inhalte sind für jedes Alter unbedenklich — das *Angebot* richtet sich an
Personen **ab 13 Jahren**. Bei der Zielgruppenauswahl also **keine**
Altersgruppe unter 13 ankreuzen und **nicht** am Programm „Designed for
Families" teilnehmen. Genau so steht es in AGB und Datenschutzerklärung, und
genau so meldet die App es auch an AdMob
(`TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE`).

Kreuzt ihr dort eine Gruppe unter 13 an, greifen die Familienrichtlinien mit
deutlich strengeren Werbeauflagen — und die Angaben widersprächen den
Rechtstexten.

---

## Vor dem Einreichen prüfen

- [ ] Die echten AdMob-Kennungen sind eingetragen — App-ID im
      `AndroidManifest.xml`, Anzeigenblock-ID im `RewardedAdManager`.
      **Solange dort die Test-IDs stehen, darf die App nicht veröffentlicht
      werden.**
- [ ] Im AdMob-Konto ist unter den EU-Einstellungen eine Einwilligungsnachricht
      angelegt und veröffentlicht — sonst zeigt die App zwar den Dialog an,
      bekommt aber keinen Inhalt dafür.
- [ ] Die Datenschutz-Seite ist unter der angegebenen Adresse erreichbar.
- [ ] Jemand mit Rechtskenntnis hat die Texte freigegeben.
