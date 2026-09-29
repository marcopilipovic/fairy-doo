# Fairydoku 1.5.13 (Nummer 67) — der tote Feenkreis-Knopf

Gebaut am 29. September 2026 aus `main`.

| Datei | Wofür |
| --- | --- |
| `Fairydoku-1.5.13-67.aab` | **das hier hochladen** — Veröffentlichungsfassung, echte Werbung |
| `Fairydoku-1.5.13-67.apk` | dieselbe Fassung zum Ausprobieren am Telefon |
| `Fairydoku-1.5.13-67-TEST.aab` | dasselbe mit Googles Testanzeigen, falls doch noch eine geschlossene Runde dazwischenkommt |
| `Fairydoku-1.5.13-67-TEST.apk` | dazu die APK |

Paket `ug.humb.fairydoku`, versionCode **67**, versionName **1.5.13**,
Ziel-API 36, mindestens Android 8. Signiert mit dem Upload-Schlüssel,
SHA-256 `75:F9:9F:44:00:85:1D:42:96:C2:3D:90:AD:1D:E9:B8:4B:1D:5C:8D:1B:29:3B:B9:A2:0F:7B:1D:D8:7D:3E:F4`.

Nachgesehen im fertigen Bundle: die echten Kennungen
`ca-app-pub-5051364478140655~5511669323` und `.../4643626005` stehen drin,
Googles Testkennungen nicht.

**Die Zuordnungsdatei musst du nicht getrennt hochladen.** Sie liegt im Bundle
unter `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`.

---

## 1. Warum 67 — zwei tote Knöpfe

**Am 29. September, einen Tag nach der Freigabe, kam aus der Runde: „Der
Feenkreis macht irgendwie nichts."** Er machte wirklich nichts. Und es waren
zwei Stellen, nicht eine.

**Der Feenkreis-Knopf.** Feenstaub und Irrlicht führen bei leerem Vorrat immer
irgendwohin — in den ersten Leveln zu einem Geschenk, danach zu einem Video.
Der Feenkreis hat absichtlich keinen Geschenk-Weg; nur trug sein Knopf
dieselbe Zeile wie die beiden anderen. Damit sah er bei leerem Vorrat
bedienbar aus, und die Sperre dahinter wies den Druck ohne ein Wort ab. Der
Vorrat ist zwei Stück bei drei Stunden Nachwuchs — wer ihn zweimal ausgab,
hatte danach stundenlang einen toten Knopf.

**Der Werbe-Knopf im Verloren-Dialog.** Dort stand `onClick = if (verfügbar)
onWatchAd else ({})` — ein voll leuchtender Knopf mit einer **leeren
Handlung**. Darauf stand „Werbung lädt…", und es lud nie etwas. Das ist
vermutlich, was im Test schon einmal als „Werbung läuft, es passiert nichts"
ankam; wir hatten damals nur einen Hinweistext nachgerüstet.

**Die Ursache war dieselbe:** Der geteilte goldene Knopf konnte gar nicht
blass — er hatte keinen gesperrten Zustand. Wer ihn nicht drückbar brauchte,
musste ihm eine leere Handlung geben.

Beides behoben. Die Regel steht jetzt einmal statt dreimal von Hand, heißt
`knopfBedienbar` und hat sieben eigene Prüfungen; der goldene Knopf kennt
`enabled` und wird blass. Die gesperrten Zustände sind gezeichnet und
angesehen worden — die Bilder liegen in `app/build/knopfproben/`.

**Nachgeprüft wurden alle 75 Bedienelemente in zehn Dateien.** Sonst nichts
gefunden: Die Levelknoten sperren bei null Leben, der Werbe-Knopf auf der
Levelkarte konnte es von Anfang an richtig, der Geschenk-Weg führt immer zu
einer Wirkung, und der Punkt „Datenschutz-Einstellungen" erscheint nur, wenn
das Formular auch da ist. Leere Handlungen gibt es im ganzen Quelltext keine
mehr.

---

## 1b. Was davor in der 66 steckte

### Warum 66 und nicht die 65 aus dem offenen Test

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
