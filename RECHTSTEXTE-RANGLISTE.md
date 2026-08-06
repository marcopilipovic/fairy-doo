# Rechtstexte für die Fassung mit Online-Rangliste

**Entwurf, keine Rechtsberatung.** Dieser Text ist eine sorgfältige Vorlage und
ersetzt keine anwaltliche Prüfung. Er beschreibt die App **nicht, wie sie heute
ist**, sondern wie sie mit Play Games und Online-Rangliste sein wird.

Die Texte in `GameCopy.kt` beschreiben den heutigen Stand: alles liegt lokal auf
dem Gerät. Erst wenn die Rangliste tatsächlich gebaut ist, werden die Abschnitte
hier gegen die dortigen getauscht — vorher wäre die Erklärung falsch in die
andere Richtung.

Zweck dieser Datei: Sie lässt sich einer Anwältin geben, solange noch nichts
gebaut ist. Die Prüfung wird dadurch schneller und billiger, und die Umsetzung
weiß vorher, was sie zusagen darf.

---

## Was sich ändert und warum

| Stelle | Heute | Mit Rangliste |
|---|---|---|
| Datenschutz, Abschnitt 2 | „Wir erheben selbst keine personenbezogenen Daten … Eine Anmeldung findet nicht statt." | Wird falsch. Play Games meldet still an. |
| Datenschutz, Abschnitt 4 | Punktestand ausschließlich lokal | Punktzahlen und Spieler-Kennung gehen an Google |
| Datenschutz, neu | — | Eigener Abschnitt zu Play Games |
| Datenschutz, Abschnitt 7 + 8 | Google nur als Werbeempfänger | Google zusätzlich als Empfänger der Spieldaten |
| Datenschutz, Abschnitt 9 | Löschung über Kontaktadresse | Zusätzlich der Selbstbedienungsweg bei Google |
| AGB, § 6 | Spielstand nur lokal | Übertragung, Sichtbarkeit für andere |
| AGB, neu | — | Eigener Paragraf zu Ranglistenregeln und Ausschluss |

Nicht betroffen: das Impressum.

---

## Datenschutzerklärung — geänderte Abschnitte

### 2. Grundsatz der Datensparsamkeit

> Fairydoku ist ein reines Logikspiel und kostenlos nutzbar. Wir betreiben keine
> eigene Nutzerverwaltung und verlangen keine Registrierung mit E-Mail-Adresse
> oder Passwort.
>
> Für die Rangliste nutzt die App den Dienst Google Play Games. Dabei wird
> automatisch die Kennung des Google-Kontos verwendet, das bereits auf deinem
> Gerät eingerichtet ist. Ein zusätzliches Konto musst du nicht anlegen. Die
> Nutzung der Rangliste ist freiwillig; lehnst du die Anmeldung ab oder ist Play
> Games auf deinem Gerät nicht verfügbar, bleibt die App vollständig spielbar.
> Die Tageswertung läuft dann nur lokal weiter.

### 4. Spielstand, Tageswertung und Bestleistungen

> Dein Spielfortschritt, deine Tageswertung und deine Bestleistungen werden
> lokal auf deinem Gerät gespeichert.
>
> Nimmst du an der Rangliste teil, wird zusätzlich deine erreichte Tagespunktzahl
> an Google Play Games übertragen und dort gemeinsam mit deiner
> Play-Games-Spielerkennung gespeichert. Andere Teilnehmerinnen und Teilnehmer
> sehen in der Rangliste deinen Play-Games-Spielernamen, dein Play-Games-Profilbild
> und deine Punktzahl. Diesen Namen und dieses Bild verwaltest du selbst in
> deinem Google-Konto; wir haben darauf keinen Einfluss.
>
> Wir selbst betreiben keinen Server für die Rangliste und speichern die
> Punktzahlen nicht bei uns.

### 5. Google Play Games *(neu)*

> Zur Verwaltung der Rangliste nutzen wir Google Play Games Services, einen
> Dienst der Google Ireland Limited, Gordon House, Barrow Street, Dublin 4,
> Irland.
>
> Verarbeitet werden dabei: die Play-Games-Spielerkennung, dein
> Play-Games-Spielername, dein Play-Games-Profilbild, die übermittelten
> Punktzahlen sowie technische Angaben zum Gerät, die für den Betrieb des
> Dienstes erforderlich sind.
>
> Rechtsgrundlage ist deine Einwilligung (Art. 6 Abs. 1 lit. a DSGVO), die du
> mit der Bestätigung der Play-Games-Anmeldung erteilst. Du kannst sie jederzeit
> mit Wirkung für die Zukunft widerrufen, indem du die Verknüpfung in den
> Play-Games-Einstellungen deines Google-Kontos aufhebst.
>
> Weitere Informationen: Datenschutzerklärung von Google.

### 8. Speicherdauer

> Wir selbst speichern keine personenbezogenen Daten.
>
> Tages-Ranglisten bei Play Games werden von Google turnusmäßig zurückgesetzt.
> Die Speicherdauer der von Google verarbeiteten Daten richtet sich nach dessen
> Datenschutzbestimmungen.

### 9. Deine Rechte — Ergänzung

> Deine bei Play Games gespeicherten Spieldaten kannst du selbst löschen: über
> die Play-Games-Einstellungen deines Google-Kontos lässt sich der Spielstand
> für einzelne Spiele entfernen. Da wir diese Daten nicht bei uns speichern,
> ist dies der schnellste Weg.

---

## AGB — geänderte und neue Paragrafen

### § 6 Spielstand, Tageswertung und Rangliste

> Dein Spielfortschritt, deine Tageswertung und deine Bestleistungen werden
> lokal auf deinem Gerät gespeichert.
>
> Nimmst du an der Rangliste teil, wird deine Tagespunktzahl an Google Play
> Games übertragen und ist dort für andere Teilnehmende sichtbar — zusammen mit
> deinem Play-Games-Spielernamen und deinem Play-Games-Profilbild. Die Teilnahme
> ist freiwillig.
>
> Die Tageswertung sammelt Punkte bis zu einem festen täglichen Stichtag. Danach
> verfallen die gesammelten Punkte, und es wird eine Belohnung in virtuellen
> Spielhilfen gutgeschrieben. Ein Anspruch auf den Erhalt gesammelter Punkte
> über den Stichtag hinaus besteht nicht.
>
> Löschst du die App oder hebst du die Verknüpfung mit Play Games auf, gehen
> Spielstand und Platzierung verloren; eine Wiederherstellung durch den Anbieter
> ist nicht möglich.

### § 6a Regeln der Rangliste *(neu — der wichtigste Zusatz)*

> Die Rangliste soll das tatsächliche Spielgeschehen abbilden. Nicht gestattet
> sind insbesondere:
>
> - das Übermitteln von Punktzahlen, die nicht durch reguläres Spielen entstanden sind
> - der Einsatz von Hilfsprogrammen, Manipulation des Spielstands oder Veränderung der App
> - das Verstellen der Geräte-Uhrzeit, um Tageswertungen mehrfach abzuschließen
> - die Nutzung mehrerer Spielerkonten mit dem Ziel, die Rangliste zu beeinflussen
>
> Bei begründetem Verdacht auf einen Verstoß darf der Anbieter einzelne
> Punktzahlen aus der Wertung nehmen, eine Platzierung zurücksetzen oder die
> betreffende Spielerkennung dauerhaft von der Rangliste ausschließen. Ein
> Anspruch auf Teilnahme an der Rangliste besteht nicht.
>
> Belohnungen aus der Tageswertung sind virtuelle Spielelemente im Sinne von
> § 5. Sie haben keinen Geldwert und werden bei einem Ausschluss ersatzlos
> entzogen.

### § 8 Zielgruppe und Nutzung durch Minderjährige — Ergänzung

> Die Rangliste zeigt Anzeigenamen und Profilbilder anderer Teilnehmender. Diese
> stammen aus deren Google-Konten und werden von Google verwaltet und moderiert;
> der Anbieter hat auf ihre Auswahl keinen Einfluss.

---

## Datensicherheitsformular in der Play Console

Muss zur Erklärung passen — Widersprüche sind ein häufiger Ablehnungsgrund.
Nach dem Umbau ist anzugeben:

**Erhoben und geteilt**
- Kennungen: Geräte- oder andere Kennungen — für Werbung *(bereits heute)*
- Kennungen: Nutzer-ID — für App-Funktionalität *(neu: Play-Games-Kennung)*
- App-Aktivität: In-App-Suchverlauf → nein; **Sonstige nutzergenerierte Inhalte** →
  ja, sofern der Spielername als solcher gewertet wird *(mit der Anwältin klären)*
- App-Info und Leistung: Absturzprotokolle → nein, solange kein Crash-Dienst eingebunden ist

**Verschlüsselung bei der Übertragung:** ja
**Löschung anfragbar:** ja — über die Play-Games-Einstellungen

---

## Offene Punkte für die anwaltliche Prüfung

1. **Einwilligungswerkzeug für Werbung.** Google verlangt für EWR und UK ein
   zertifiziertes Werkzeug, auch bei ausschließlich nicht personalisierter
   Werbung. Im Code ist bislang keins. Muss vor Veröffentlichung gebaut werden —
   unabhängig von der Rangliste.

2. **Öffentlich erreichbare Datenschutzerklärung.** Die Play Console verlangt
   einen Link, der ohne Installation aufrufbar ist. Der Text liegt derzeit nur
   in der App (`GameCopy.kt`). Eine kleine Webseite genügt.

3. **Rechtsgrundlage für Play Games.** Oben ist Einwilligung angesetzt, weil die
   Anmeldung freiwillig und ablehnbar ist. Ob berechtigtes Interesse tragfähiger
   wäre, sollte geprüft werden.

4. **Zielgruppe ab 13.** Die Einstufung in der Play Console lautet „ab 13",
   die Inhaltseinstufung bleibt „ab 0". Ob Google die App angesichts ihrer
   Gestaltung dennoch als kindgerichtet einordnet, lässt sich vorab nicht
   sicher sagen. Wird sie es, ist die Rangliste mit fremden Anzeigenamen
   erneut zu bewerten.

5. **Anzeigename und Avatar-Fee in den Einstellungen.** Beide werden mit Play
   Games überflüssig, weil Name und Bild vom Google-Konto kommen. Bleiben sie
   im Spiel, wären es zwei Namen nebeneinander — und der selbst getippte wäre
   der, für den eine Moderationspflicht entstünde. Empfehlung: entfernen oder
   auf reine Zierde ohne Sichtbarkeit für andere beschränken.
