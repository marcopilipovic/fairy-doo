# Fairydoku 1.5.12 (Nummer 66) — für die Veröffentlichung

Gebaut am 28. September 2026 aus `main`.

| Datei | Wofür |
| --- | --- |
| `Fairydoku-1.5.12-66.aab` | **das hier hochladen** — Veröffentlichungsfassung, echte Werbung |
| `Fairydoku-1.5.12-66.apk` | dieselbe Fassung zum Ausprobieren am Telefon |
| `Fairydoku-1.5.12-66-TEST.aab` | dasselbe mit Googles Testanzeigen, falls doch noch eine geschlossene Runde dazwischenkommt |
| `Fairydoku-1.5.12-66-TEST.apk` | dazu die APK |

Paket `ug.humb.fairydoku`, versionCode **66**, versionName **1.5.12**,
Ziel-API 36, mindestens Android 8. Signiert mit dem Upload-Schlüssel,
SHA-256 `75:F9:9F:44:00:85:1D:42:96:C2:3D:90:AD:1D:E9:B8:4B:1D:5C:8D:1B:29:3B:B9:A2:0F:7B:1D:D8:7D:3E:F4`.

Nachgesehen im fertigen Bundle: die echten Kennungen
`ca-app-pub-5051364478140655~5511669323` und `.../4643626005` stehen drin,
Googles Testkennungen nicht.

**Die Zuordnungsdatei musst du nicht getrennt hochladen.** Sie liegt im Bundle
unter `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`.

---

## 1. Warum 66 und nicht die 65 aus dem offenen Test

**Am Spiel hat sich nichts geändert.** Zwischen der Fassung, die den offenen
Test durchlaufen hat (1.5.11, Nummer 65, 20. September), und heute liegt keine
einzige Änderung am Programm.

Geändert haben sich **zwei Rechtstexte, die in der App stecken** — und weil sie
darin stecken, braucht es einen neuen Bau:

- **§ 5 der Nutzungsbedingungen** nannte als Spielhilfen nur „Feenstaub" und
  „Irrlicht". Der Feenkreis kam Anfang September dazu und fehlte. Durch das
  „wie" davor war der Satz nie falsch, nur unvollständig.
- **Die Lizenzseite** sagte, Fairydoku benutze „zwei Schriften und mehrere
  Programmbibliotheken". Seit Ende August stecken acht Tonaufnahmen darin — die
  Waldmusik, der Schreckenslaut, die sechs Kicherlaute. Sie sind mit ElevenLabs
  unter einem bezahlten Tarif erzeugt, der die gewerbliche Nutzung ausdrücklich
  einschließt (belegt in `pruefbericht.md`), und **nicht** nennungspflichtig.
  Die Aufzählung stimmte trotzdem nicht mehr.

Nummer 65 ließe sich ohnehin nicht erneut hochladen — Google nimmt je Paket nur
steigende Nummern, spurübergreifend.

**Die Texte für die Spur** stehen in `texte/versionshinweise.md`, Abschnitt
**2i** auf deutsch und **2j** auf englisch.

---

## 2. Was in der Play Console noch zu setzen ist

Das kann niemand aus dem Projekt heraus erledigen:

- **Die Händlererklärung nach dem DSA.** Als App HUMB UG seid ihr Händler; Name,
  Anschrift und E-Mail erscheinen danach öffentlich im Eintrag und müssen mit
  dem Impressum übereinstimmen.
- **Künstlich erzeugtes Material angeben.** Die acht Tonaufnahmen stammen aus
  ElevenLabs. Die Console fragt danach; es ist ein Haken, keine Datei.
- **Die Länderauswahl.** Die App spricht deutsch, der Store-Eintrag zusätzlich
  englisch. Weiter zu öffnen, als die App spricht, bringt Einträge, die niemand
  versteht.
- **Die eigenen Geräte als Testgeräte im AdMob-Konto** (Einstellungen →
  Testgeräte). Ab der Produktion laufen echte Anzeigen; wer ohne diesen Eintrag
  auf die eigene Werbung tippt, erzeugt „ungültigen Traffic" — der häufigste
  Weg, ein AdMob-Konto zu verlieren.

**Erledigt und nachgewiesen:** Die Einwilligungsnachricht im AdMob-Konto ist
seit dem 8. September veröffentlicht (*Datenschutz und Mitteilungen →
Europäische Verordnungen*, App `fairydoku`, Status **Veröffentlicht**).

## 3. Was in diesem Paket sonst liegt

`texte/` — der Store-Eintrag deutsch und englisch, Versions- und
Testerhinweise, die Antworten für Datensicherheit und Alterseinstufung, dazu
`PLAY-GAMES-START.md` als Vorrat (nicht eintragen, siehe Datei).

`webseite/` — die vier Rechtstext-Seiten deutsch, dieselben englisch.
**Die veröffentlichte Webseite trägt die beiden Korrekturen aus Abschnitt 1
noch nicht** — sie liegt in Mircos CMS und muss dort nachgezogen werden.

`store-grafik/` — Symbol, Feature-Grafik, die fünf Bildschirmfotos.
