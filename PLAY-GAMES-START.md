# Start mit Play Games — alle Texte

**Entwurf, keine Rechtsberatung.** Sorgfältig geschrieben, aber ungeprüft. Wer
die Rangliste einschaltet, sollte diese Texte einmal jemandem mit
Rechtskenntnis vorlegen — sie beschreiben, was die App dann tatsächlich tut.

Diese Datei enthält **jeden Text, den ein Start mit den Play Games-Diensten
braucht**: die Rechtstexte für die App, die Einrichtung in der Play Console,
die Bedienoberfläche, den Store-Eintrag und die beiden Fragebögen. Zum Kopieren
gedacht, nicht zum Nacherzählen.

**Die eine Bedingung:** Diese Texte gehen mit *derselben* Fassung hinaus wie die
Anbindung — keinen Tag früher. Eine Datenschutzerklärung, die eine Anmeldung
beschreibt, die es noch nicht gibt, ist genauso falsch wie eine, die sie
verschweigt. Bis dahin gelten die heutigen Texte in `ui/GameCopy.kt`.

---

## 0. Was gebaut sein muss, bevor diese Texte gelten

Texte allein starten den Dienst nicht. Dazu gehört:

1. Die Abhängigkeit `com.google.android.gms:play-services-games-v2` und die
   Play-Games-Projekt-ID im Manifest.
2. Die Anmeldung: Play Games meldet beim Start selbst an, wenn der Spieler das
   im Google-Konto erlaubt hat. Lehnt er ab, muss die App vollständig
   weiterlaufen — die Tageswertung dann rein lokal.
3. Das Einreichen der Tagespunktzahl an die Bestenliste, am Stichtag oder nach
   jedem Level.
4. Ein Weg zur Rangliste — Googles eigene Ansicht genügt, eine eigene braucht es
   nicht.
5. Der Eintrag „Play Games" in den Einstellungen der App, der zur Verknüpfung
   führt.
6. Die Einrichtung in der Play Console nach Abschnitt 1.

**Die Schlüsselbindung** dafür liegt bereit: SHA-1 und SHA-256 stehen in
`VEROEFFENTLICHUNG.md`. Play Games verlangt den SHA-1 des Upload-Schlüssels und
zusätzlich den des Verteilschlüssels, den Google beim App Signing verwahrt.

---

## 1. Play Console → Play Games-Dienste

### Das Spiel

| Feld | Wert |
| --- | --- |
| Name des Spiels | `Fairydoku` |
| Kategorie | Puzzle |
| Beschreibung (kurz) | `Setze Feen auf ein Waldgitter — eine je Reihe, Spalte und Zone.` |

### Beschreibung (lang)

```
Fairydoku ist ein Logikrätsel im Feenwald. Setze Feen auf ein Gitter aus
moosigen Steinen: genau eine je Reihe, eine je Spalte, eine je leuchtender
Waldzone — und keine zwei dürfen sich berühren, auch nicht über Eck.

Das Gitter wächst alle zwei Level. Eine Uhr läuft nicht: Du darfst überlegen,
so lange du magst. Drei Helfer nehmen dir Arbeit ab, keiner nimmt dir das
Nachdenken.

In der Tageswertung sammelst du Punkte bis vier Uhr früh. Was du an einem Tag
zusammenbekommst, zählt für die Bestenliste dieses Tages.
```

### Bestenliste

Eine genügt zum Start. Zwei wären eine zweite Baustelle, bevor überhaupt jemand
in der ersten steht.

| Feld | Wert |
| --- | --- |
| Anzeigename | `Tageswertung` |
| Kennung (ID) | wird von Google vergeben — im Code als `leaderboard_tageswertung` referenzieren |
| Format | Ganzzahl, ohne Nachkommastellen |
| Sortierung | **Größer ist besser** |
| Zurücksetzen | **Täglich** |
| Sichtbarkeit | öffentlich |
| Symbol | `storepaket/play-store/symbol-512x512.png` |

**Achtung beim Zurücksetzen:** Googles Tagesbestenliste wechselt nach Googles
Zeitzone, die Tageswertung der App um vier Uhr früh in Ortszeit
(`DailyCycle.cycleIdAt`). Beide Zeitpunkte fallen auseinander. Entweder man
reicht die Punktzahl am eigenen Stichtag ein und lebt mit der Verschiebung, oder
man erklärt sie im Text — verschweigen sollte man sie nicht, sonst hält es
jemand für einen Fehler.

### Erfolge

Zum Start keine. Erfolge, die niemand erreichen kann, weil das Spiel sie nicht
meldet, sind schlimmer als gar keine.

### Testende

Vor der Veröffentlichung sieht nur, wer in der Play Console als Testender für
die Play Games-Dienste eingetragen ist, überhaupt eine Anmeldung. Das ist die
häufigste Fehlersuche der ersten Stunde: Die Anmeldung „geht nicht", weil das
Konto nicht in der Liste steht.

---

## 2. Datenschutzerklärung — für `ui/GameCopy.kt`

Die Abschnitte 1, 3, 5 (Technische Bereitstellung), 6 und 11 bleiben, wie sie
sind. **Neu ist ein Abschnitt zu Play Games**; er kommt hinter Abschnitt 4, die
bisherigen 5 bis 11 rücken auf 6 bis 12. Die Verweise „siehe Abschnitt 3" und
„siehe Abschnitt 10" im Text mitzählen — aus 10 wird 11.

### Abschnitt 2 — ersetzen

```
2. Grundsatz der Datensparsamkeit
Fairydoku ist ein reines Logikspiel und kostenlos nutzbar. Wir betreiben keine eigene Nutzerverwaltung, verlangen keine Registrierung mit E-Mail-Adresse oder Passwort und speichern selbst keine personenbezogenen Daten auf einem Server — wir haben keinen.

Für die Bestenliste nutzt die App den Dienst Google Play Games. Dabei wird die Kennung des Google-Kontos verwendet, das auf deinem Gerät bereits eingerichtet ist; ein zusätzliches Konto brauchst du nicht. Die Teilnahme ist freiwillig: Lehnst du die Anmeldung ab, oder ist Play Games auf deinem Gerät nicht verfügbar, bleibt die App vollständig spielbar. Deine Tageswertung läuft dann allein auf dem Gerät weiter.
```

### Abschnitt 4 — ersetzen

```
4. Spielstand, Tageswertung und Bestleistungen
Dein Punktestand, deine Tageswertung und deine bisherigen Bestleistungen werden lokal auf deinem Gerät gespeichert. An uns wird davon nichts übermittelt.

Für dieses Speichern fragen wir dich nicht um Erlaubnis, und das hat einen Grund: Es ist unbedingt erforderlich, damit die App das tut, wofür du sie geöffnet hast — ohne gespeicherten Spielstand begänne jedes Level wieder bei null. § 25 Abs. 2 Nr. 2 TDDDG nimmt genau diesen Fall von der Einwilligung aus. Alles, was darüber hinausgeht, fragen wir (siehe Abschnitt 3 und Abschnitt 5).

Nimmst du an der Bestenliste teil, wird zusätzlich deine Tagespunktzahl an Google Play Games übertragen und dort zu deiner Play-Games-Spielerkennung gespeichert. Andere Teilnehmende sehen dort deinen Play-Games-Spielernamen, dein Play-Games-Profilbild und deine Punktzahl. Beides verwaltest du selbst in deinem Google-Konto; wir haben darauf keinen Einfluss und speichern die Punktzahlen nicht bei uns.

Die Tageswertung speichert dazu, wie viele Punkte am laufenden Tag gesammelt wurden, das beste Tagesergebnis und den Zeitpunkt des letzten Tageswechsels. Ein Anzeigename und eine Avatar-Fee lassen sich in den Einstellungen hinterlegen; beides wird nur lokal gespeichert und erscheint allein auf diesem Gerät. In der Bestenliste steht nicht dieser Name, sondern dein Play-Games-Spielername.

Eine Ausnahme, die wir offen nennen wollen: Android sichert App-Daten auf Wunsch in deinem eigenen Google-Konto („Automatische Datensicherung"), und Fairydoku nimmt daran teil. Dadurch findest du deinen Spielstand auf einem neuen Telefon wieder. Diese Sicherung liegt in deinem Konto, nicht bei uns — wir haben darauf keinen Zugriff. Abschalten kannst du sie in den Android-Einstellungen unter „Sicherung" bzw. „Google – Datensicherung".
```

### Abschnitt 5 — neu einfügen

```
5. Google Play Games
Für die Bestenliste nutzen wir Google Play Games Services, einen Dienst der Google Ireland Limited, Gordon House, Barrow Street, Dublin 4, Irland.

Verarbeitet werden dabei: deine Play-Games-Spielerkennung, dein Play-Games-Spielername, dein Play-Games-Profilbild, die übermittelten Punktzahlen sowie technische Angaben, die für den Betrieb des Dienstes erforderlich sind.

Rechtsgrundlage ist deine Einwilligung (Art. 6 Abs. 1 lit. a DSGVO). Du erteilst sie, indem du der Play-Games-Anmeldung zustimmst. Du kannst sie jederzeit mit Wirkung für die Zukunft widerrufen: in den Play-Games-Einstellungen deines Google-Kontos, indem du die Verknüpfung mit Fairydoku aufhebst. Danach wird keine Punktzahl mehr übermittelt; die App bleibt spielbar.

Ohne Einwilligung findet keine Übertragung statt. Weitere Informationen findest du in der Datenschutzerklärung von Google.
```

### Abschnitt 8 (bisher 7) — ersetzen

```
8. Empfänger und Datenübermittlung in Drittländer
Empfänger der oben genannten Daten ist Google — als Anbieter der Werbung (AdMob) und als Anbieter der Bestenliste (Play Games Services). Dabei kann es zu einer Übermittlung von Daten in Länder außerhalb der EU/des EWR (insbesondere USA) kommen. Google stützt solche Übermittlungen auf geeignete Garantien (z. B. EU-Standardvertragsklauseln bzw. das EU-US Data Privacy Framework).
```

### Abschnitt 9 (bisher 8) — ersetzen

```
9. Speicherdauer
Wir selbst speichern keine personenbezogenen Daten. Die Tagesbestenliste bei Play Games wird von Google turnusmäßig zurückgesetzt. Im Übrigen richtet sich die Speicherdauer der durch Google verarbeiteten Daten nach dessen Datenschutzbestimmungen.
```

### Abschnitt 10 (bisher 9) — Absatz anhängen

```
Deine bei Play Games gespeicherten Spieldaten kannst du selbst löschen: In den Play-Games-Einstellungen deines Google-Kontos lässt sich der Spielstand einzelner Spiele entfernen. Da diese Daten nicht bei uns liegen, ist das der schnellste Weg — eine Nachricht an uns geht denselben Weg, nur langsamer.
```

### Abschnitt 11 (bisher 10) — Absatz anhängen

```
Die Teilnahme an der Bestenliste beendest du unabhängig davon in den Play-Games-Einstellungen deines Google-Kontos, indem du die Verknüpfung mit Fairydoku aufhebst.
```

### Stand-Zeile

```
Stand: <Monat Jahr der Umstellung>
```

---

## 3. AGB — für `ui/GameCopy.kt`

**Ein neuer § 7 kommt dazu**; die bisherigen §§ 7 bis 12 werden zu 8 bis 13. Der
Verweis auf § 5 im neuen Paragrafen stimmt dann noch, der auf „§ 10 Datenschutz"
in der Überschrift wird zu § 11.

### § 6 — ersetzen

```
§ 6 Spielstand, Tageswertung und Bestenliste
Dein Punktestand, deine Tageswertung und deine bisherigen Bestleistungen werden lokal auf deinem Gerät gespeichert.

Nimmst du an der Bestenliste teil, wird deine Tagespunktzahl an Google Play Games übertragen und ist dort für andere Teilnehmende sichtbar — zusammen mit deinem Play-Games-Spielernamen und deinem Play-Games-Profilbild. Die Teilnahme ist freiwillig und jederzeit beendbar; ohne sie bleibt die App vollständig spielbar.

Die Tageswertung sammelt Punkte bis zu einem festen täglichen Stichtag. Danach verfallen die gesammelten Punkte, und es wird eine Belohnung in virtuellen Spielhilfen gutgeschrieben. Ein Anspruch auf den Erhalt gesammelter Punkte über den Stichtag hinaus besteht nicht. Der Anbieter kann Zeitpunkt des Stichtags, Punkteberechnung und Belohnungsstufen anpassen.

Der Stichtag der App und der Zurücksetzungszeitpunkt der Bestenliste bei Google können auseinanderfallen; ein Anspruch auf eine bestimmte Zuordnung von Punkten zu einem Tag besteht nicht.

Löschst du die App oder die App-Daten oder hebst du die Verknüpfung mit Play Games auf, gehen Spielstand, Tageswertung, Bestleistungen und Platzierung verloren; eine Wiederherstellung durch den Anbieter ist nicht möglich.
```

### § 7 — neu

```
§ 7 Regeln der Bestenliste
Die Bestenliste soll das tatsächliche Spielgeschehen abbilden. Nicht gestattet sind insbesondere:
• das Übermitteln von Punktzahlen, die nicht durch reguläres Spielen entstanden sind
• der Einsatz von Hilfsprogrammen, das Verändern des Spielstands oder der App
• das Verstellen der Geräte-Uhrzeit, um Tageswertungen mehrfach abzuschließen
• die Nutzung mehrerer Spielerkonten mit dem Ziel, die Bestenliste zu beeinflussen

Bei begründetem Verdacht auf einen Verstoß darf der Anbieter einzelne Punktzahlen aus der Wertung nehmen, eine Platzierung zurücksetzen oder die betreffende Spielerkennung dauerhaft von der Bestenliste ausschließen. Ein Anspruch auf Teilnahme an der Bestenliste besteht nicht.

Belohnungen aus der Tageswertung sind virtuelle Spielelemente im Sinne von § 5. Sie haben keinen Geldwert und werden bei einem Ausschluss ersatzlos entzogen.
```

### § 9 (bisher § 8) — Absatz anhängen

```
Die Bestenliste zeigt Spielernamen und Profilbilder anderer Teilnehmender. Diese stammen aus deren Google-Konten und werden von Google verwaltet und moderiert; der Anbieter hat auf ihre Auswahl keinen Einfluss und kann sie nicht ändern.
```

---

## 4. Texte in der App

Kurz gehalten: Jeder Satz wird hundertmal gelesen.

### Beim ersten Start, wenn Play Games verfügbar ist

```
Titel:   Spielst du mit?
Text:    Deine Tagespunkte können in einer Bestenliste stehen — mit deinem
         Play-Games-Namen, nicht mit dem, den du hier vergibst.
         Ohne Anmeldung spielst du genauso weiter, nur für dich.
Knöpfe:  [ Anmelden ]  [ Später ]
```

### In den Einstellungen

| Zustand | Text |
| --- | --- |
| angemeldet | `Play Games · angemeldet als {Name}` mit dem Zusatz `Bestenliste ansehen` |
| nicht angemeldet | `Play Games · nicht angemeldet` mit dem Zusatz `Anmelden und mitspielen` |
| nicht verfügbar | Eintrag ganz weglassen — ein Menüpunkt, der nichts tut, verwirrt mehr, als er hilft |
| Verknüpfung lösen | `Teilnahme beenden` — führt in die Play-Games-Einstellungen des Kontos, wir lösen sie nicht selbst |

### Beim Anzeigenamen (bleibt im Spiel, wird aber eingeordnet)

```
Dein Name im Feenreich — er steht in deiner eigenen Tageswertung.
In der Bestenliste steht dein Play-Games-Name.
```

### Meldungen

| Fall | Text |
| --- | --- |
| Punktzahl eingereicht | `🏆 {Punkte} Punkte in der Bestenliste!` |
| kein Netz | `Keine Verbindung — deine Punkte werden später eingereicht.` |
| Anmeldung abgelehnt | `Alles gut. Du spielst weiter für dich.` |
| Play Games nicht verfügbar | *(nichts anzeigen)* |

Die Zeile fürs Fehlen des Netzes verspricht nur, was die App auch hält: Die
Punktzahl steht lokal und geht beim nächsten gelungenen Einreichen mit.

---

## 5. Store-Eintrag

### Kurzbeschreibung (deutsch, 80 Zeichen)

```
Feen-Logikrätsel im Nachtwald — mit Tagesbestenliste. Ohne Zeitdruck.
```

### Vollbeschreibung — Absatz zum Einfügen (deutsch)

Hinter den Absatz über die Tageswertung:

```
🏆 DIE TAGESBESTENLISTE
Deine Tagespunkte zählen für die Bestenliste des Tages. Sie läuft über Google
Play Games — mit dem Namen und dem Bild, die dort in deinem Konto stehen. Die
Teilnahme ist freiwillig: Ohne Anmeldung spielst du genauso weiter, nur für
dich allein.
```

### Kurzbeschreibung (englisch, 80 Zeichen)

```
Fairy logic puzzles in a night forest — with a daily leaderboard. No timer.
```

### Vollbeschreibung — Absatz zum Einfügen (englisch)

```
🏆 THE DAILY LEADERBOARD
Your points for the day count towards that day's leaderboard, run through
Google Play Games — under the name and picture from your account there. Taking
part is optional: without signing in you play exactly the same, just for
yourself.
```

### Versionshinweise (deutsch, max. 500 Zeichen)

```
Neu: die Tagesbestenliste.

Deine Punkte des Tages zählen jetzt für eine Bestenliste über Google Play
Games. Anmelden, mitspielen — oder es lassen: Ohne Anmeldung läuft alles
weiter wie bisher, nur für dich.

Dein Name im Feenreich bleibt, wo er war. In der Bestenliste steht dein
Play-Games-Name.
```

### Versionshinweise (englisch)

```
New: the daily leaderboard.

Your points for the day now count towards a leaderboard run through Google
Play Games. Sign in and join — or don't: without signing in everything works
as before, just for you.

Your name in the fairy realm stays where it was. The leaderboard shows your
Play Games name.
```

### Testerhinweise

```
Was neu ist
Die Tagesbestenliste über Google Play Games.

Worauf ihr besonders achten könnt
• Die Anmeldung. Sie kommt beim ersten Start, wenn euer Konto Play Games
  erlaubt. Lehnt sie einmal ab: Das Spiel muss vollständig weiterlaufen.
• Die Punkte. Nach einem geschafften Level sollten sie in der Bestenliste
  ankommen. Ohne Netz nicht — dann aber später, nicht gar nicht.
• Die zwei Namen. In den Einstellungen vergebt ihr einen Namen fürs Feenreich;
  in der Bestenliste steht der aus eurem Google-Konto. Wirkt das verständlich
  oder wie ein Fehler?
• Der Tageswechsel. Unsere Tageswertung wechselt um vier Uhr früh, Googles
  Bestenliste zu ihrer eigenen Zeit. Sagt Bescheid, wenn euch das
  durcheinanderbringt.

Was wir schon wissen
Wer nicht als Testender für die Play Games-Dienste eingetragen ist, sieht gar
keine Anmeldung. Sagt Bescheid, dann tragen wir euer Konto nach.
```

---

## 6. Datensicherheitsformular

Ersetzt die heutige Fassung in
`storepaket/play-store/datensicherheit-und-einstufung.md`, sobald die Anbindung
draußen ist.

| Datenart | Erhoben | Geteilt | Zweck | Pflicht? |
| --- | --- | --- | --- | --- |
| Geräte- oder andere IDs | Ja | Ja (Google) | Werbung, Betrugsvermeidung | optional |
| App-Interaktionen | Ja | Ja (Google) | Werbung, App-Funktionalität | optional |
| Nutzer-ID | Ja | Ja (Google) | App-Funktionalität — die Play-Games-Spielerkennung | optional |
| Spielaktivität (Punktzahlen) | Ja | Ja (Google) | App-Funktionalität | optional |

**Nicht** anzukreuzen: Name, E-Mail, Anschrift, Telefonnummer, Standort,
Kontakte, Fotos, Dateien, Kalender, Gesundheitsdaten, Zahlungsdaten,
Sprachaufnahmen, Nachrichten.

**Zum Namen:** Der in der App vergebene Anzeigename bleibt auf dem Gerät und
wird nicht übertragen — das Feld bleibt leer. Der in der Bestenliste sichtbare
Name ist der des Google-Kontos; erhoben wird er von Google, nicht von uns.
Deshalb steht dort „Nutzer-ID" und nicht „Name".

**Nutzergenerierte Inhalte:** nein, solange der frei getippte Name das Gerät
nicht verlässt. Genau deshalb bleibt er lokal — sonst entstünde eine
Moderationspflicht für Namen, die wir weder sehen noch ändern können.

| Frage | Antwort |
| --- | --- |
| Verschlüsselung bei der Übertragung | Ja |
| Können Nutzer die Löschung verlangen? | **Ja** — über die Play-Games-Einstellungen des Google-Kontos, zusätzlich über die Kontaktadresse |

**IARC-Fragebogen:** „Nutzer können miteinander kommunizieren" bleibt **Nein**
— eine Bestenliste ist kein Chat. „Nutzergenerierte Inhalte" wird **Ja**, weil
fremde Spielernamen sichtbar werden.

---

## 7. Was offen bleibt

- **Der Stichtag.** Vier Uhr früh in der App gegen Googles eigenen
  Zurücksetzungszeitpunkt. Entscheiden, ob man das erklärt oder die Einreichung
  danach ausrichtet.
- **Der doppelte Name.** Hier bleibt der eigene Anzeigename erhalten und wird
  eingeordnet („in der Bestenliste steht dein Play-Games-Name"). Die Alternative
  wäre, ihn ganz zu streichen — das spart eine Erklärung und nimmt dem Spiel
  eine Kleinigkeit.
- **Die Rechtsgrundlage für Play Games.** Oben steht Einwilligung, weil die
  Anmeldung freiwillig und ablehnbar ist. Ob berechtigtes Interesse tragfähiger
  wäre, sollte mitgeprüft werden.
- **Die Zielgruppe ab 13.** Die Einstufung im Store lautet „ab 13", die
  Inhaltseinstufung bleibt „ab 0". Ordnet Google die App trotz ihrer Gestaltung
  als kindgerichtet ein, ist eine Bestenliste mit fremden Anzeigenamen neu zu
  bewerten. Vorab sicher sagen lässt sich das nicht.
- **Die anwaltliche Prüfung.** Bisher wurde bewusst darauf verzichtet. Mit einer
  Bestenliste, auf der fremde Namen erscheinen, und einer Zielgruppe ab 13 ist
  das eine andere Lage als bei einer App, die nichts überträgt.

---

*Diese Datei löst `RECHTSTEXTE-RANGLISTE.md` ab (angelegt am 6. August 2026,
gelöscht am 8. September). Dort standen dieselben Abschnitte als Entwurf, aber
nur die rechtlichen — Store, Oberfläche und Einrichtung fehlten, und die
Nummerierung passte nicht mehr zu der in `GameCopy.kt`.*
