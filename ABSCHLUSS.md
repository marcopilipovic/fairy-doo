# Fairydoku — Abschluss

**Stand: 29. September 2026. Das Spiel ist im Play Store.**

**Nachtrag vom selben Tag:** Einen Tag nach der Freigabe kam aus der Runde „der
Feenkreis macht irgendwie nichts". Der Knopf war bei leerem Vorrat bedienbar und
tat nichts — seit der 1.5.8, in jeder Fassung. Es waren **zwei** Stellen, nicht eine — der
Feenkreis und der Werbe-Knopf im Verloren-Dialog, der eine leere Handlung
mitbekam. Behoben in der **1.5.13 als Nummer 67**; die Regel steht jetzt einmal
als `knopfBedienbar` statt dreimal von Hand, und der geteilte goldene Knopf
kennt endlich einen gesperrten Zustand. Alle 75 Bedienelemente sind daraufhin
durchgesehen, die Befunde stehen als Regeln 205 bis 207 in `~/kisten`.

Diese Datei ist der Schlussstein. Sie sagt, was gebaut wurde, wo es liegt, was
bewusst offen geblieben ist und woran man merkt, dass doch noch etwas zu tun
ist. Wer nach einer Pause hier wieder einsteigt, liest zuerst diese Datei und
dann `STAND.md`.

---

## Was es ist

Ein ruhiges Logikrätsel für Android. Man setzt Feen auf ein Waldgitter: eine je
Reihe, eine je Spalte, eine je leuchtender Zone — und keine zwei berühren sich,
auch nicht über Eck. Das Gitter beginnt bei 4×4 und wächst alle zwei Level, bis
ab Level 9 acht Feen nebeneinander wohnen.

Es läuft keine Uhr. Man kann überlegen, so lange man mag, weggehen und
wiederkommen; das Rätsel wartet. Nur ein verlorenes Level kostet eines von fünf
Leben, und die wachsen von selbst nach.

Kostenlos, ohne Käufe, ohne Anmeldung, ohne Banner. Werbung läuft nur, wenn
jemand selbst ein Video startet, um einen Helfer oder ein Leben zu bekommen.

---

## Die Eckdaten

| | |
| --- | --- |
| Paket | `ug.humb.fairydoku` |
| Veröffentlicht | **29. September 2026** |
| Fassung im Store | 1.5.13, Nummer 67 |
| Ziel-API | 36 (Android 16), mindestens Android 8 |
| Anbieter | App HUMB UG (haftungsbeschränkt), Parkstraße 9, 31188 Holle |
| Umfang | 63 Kotlin-Dateien, rund 16.200 Zeilen |
| Prüfungen | 156 in der Debug-Fassung, 149 in beiden Release-Fassungen, alle grün |
| Einbuchungen | 246, vom 31. Juli bis zum 29. September 2026 |

---

## Der Weg dahin

- **31. Juli** — erste Einbuchung.
- **18. August** — die Reihenfolge kippt: das Feenspiel geht zuerst raus, nicht
  die Hundespiele.
- **30. August** — Signierschlüssel gesichert an drei Orten; Markenrecherche
  ohne Treffer.
- **31. August** — der Paketname wird auf `ug.humb.fairydoku` umgestellt. Der
  letzte Tag, an dem das ging: Nach dem ersten Upload liegt er für immer fest.
- **8. September** — die Einwilligungsnachricht im AdMob-Konto wird
  veröffentlicht.
- **20. September** — die Fassung für den offenen Test, 1.5.11 als Nummer 65.
- **28. September** — Kontrolle vor der Veröffentlichung, zu dritt mit zwei
  Nachbarsitzungen. Ergebnis: 1.5.12 als Nummer 66.
- **29. September** — Google gibt frei. Das erste Spiel des Hauses im Store.

---

## Was im Spiel steckt

**Zehn Feen**, jede mit eigenem Namen und eigener Farbe, als Vektorzeichnungen.
Über zehn Level durchläuft jede einmal jede Waldzone.

**Drei Helfer**, alle kostenlos und von selbst nachwachsend:

- **Feenstaub** setzt eine Fee auf ein garantiert sicheres Feld (drei Stück, zwei Stunden)
- **Irrlicht** deckt umgekehrt ein Feld auf, auf dem keine sitzt (drei Stück, zwei Stunden)
- **Feenkreis** — eine halbe Minute lang kreuzt jede gesetzte Fee selbst an, was sie ausschließt (zwei Stück, drei Stunden)

**Fünf Leben**, eines wächst alle zwei Stunden nach. Drei Versuche je Level; ein
Versuch geht nur verloren, wenn eine Fee wirklich im Konflikt steht.

**Klang.** Eine Waldschleife liegt über allem, sechs verschiedene Kicherlaute
wechseln sich ab, wenn eine Fee richtig sitzt. Musik, Geräusche und Feenstimmen
lassen sich getrennt regeln.

**Eine Tageswertung**, die bis zu einem festen Stichtag Punkte sammelt und dann
in Helfern belohnt — der Grund, am nächsten Tag wiederzukommen.

---

## Wo was liegt

| Was | Wo |
| --- | --- |
| Quelltext | `/home/nataly/fairy-doo`, Zweig `main` |
| Ferne | `github.com/marcopilipovic/fairy-doo` (öffentlich) |
| Fertige Dateien | `storepaket/app/` — AAB und APK, scharf und mit Testanzeigen |
| Store-Texte | `storepaket/play-store/` |
| Rechtstexte | `storepaket/webseite/seiten/` deutsch, `englisch/` englisch |
| Bildschirmfotos | `storepaket/play-store/bildschirmfotos/` — gerechnet, nicht aufgenommen |
| Der laufende Stand | `STAND.md` |
| Wie man veröffentlicht | `VEROEFFENTLICHUNG.md` |

**Der Signierschlüssel** liegt als `fairydoku-upload.keystore` im Ordner, ist
durch `.gitignore` geschützt und an drei Orten gesichert: diese Maschine, ein
USB-Stick, ein Ausdruck. **Geht er verloren, lässt sich die App nie wieder
aktualisieren.** Das ist die einzige Sache in diesem Projekt, die sich nicht
reparieren lässt.

---

## Zwei Eigenheiten, die man kennen muss

**Die Bildschirmfotos werden gerechnet, nicht aufgenommen.** Ein Test zeichnet
die Oberfläche und schreibt fünf PNG-Dateien. Das ist bequem — aber sie
veralten lautlos, weil niemand sie ansieht. Am 28. September fiel auf, dass das
Bild der Levelkarte seit dem 20. eine Zeile zeigte, die es nicht mehr gab.
**Wer einen Text im Spiel ändert, rechnet die Bilder neu:**

```
./gradlew testDebugUnitTest --tests '*BildschirmfotosTest*' -Dwerbefilm=ja
```

**Die Rechtstexte stehen im Quelltext**, in `GameCopy.legalBody`. Von dort
entstehen die Webseiten automatisch. Wer einen Rechtstext ändert, ändert ihn
dort und **nirgends sonst** — und braucht danach einen neuen Bau, weil er in der
App steckt.

---

## Was bewusst offen geblieben ist

Nichts davon hält etwas auf. Es ist die Liste für den Tag, an dem wieder Zeit
ist.

1. **Die Landingpage sagt „Coming soon to Google Play".** Das stimmt seit dem
   29. September nicht mehr. Liegt in Mircos CMS.
2. **Die veröffentlichten Rechtstexte im Netz** tragen die zwei Korrekturen vom
   28. September noch nicht — den Feenkreis in § 5 und die Herkunft der Töne auf
   der Lizenzseite. In der App stehen sie.
3. **Das Store-Bild an Position 2** ist neu gerechnet und liegt bereit, aber
   noch nicht in der Console getauscht.
4. **Die eigenen Geräte im AdMob-Konto** unter *Einstellungen → Testgeräte*.
   Solange das nicht steht, darf niemand aus dem Team auf die eigene Werbung
   tippen — das zählt als ungültiger Traffic und ist der häufigste Weg, ein
   AdMob-Konto zu verlieren.
5. **Eine englische Fassung der App.** Der Store-Eintrag ist zweisprachig, die
   App selbst nur deutsch. Solange das so ist, bringt eine weite Länderauswahl
   vor allem Bewertungen von Leuten, die nichts verstehen.
6. **Ein echtes Querformat** (Brett links, Bedienung rechts). Bis dahin bleibt
   die Festlegung aufs Hochformat stehen, und Googles vierte Mahnung bleibt
   bestehen. Sie ist eine Empfehlung, kein Hindernis.
7. **Play Games** ist bewusst nicht eingebunden. Die fertigen Texte liegen als
   Vorrat in `PLAY-GAMES-START.md`. Bei Fairydoku ohne Nutzen; interessant wird
   es erst bei Switchit, wegen des Ladens.

---

## Woran man merkt, dass doch etwas zu tun ist

Vier Dinge, und nur diese vier, rechtfertigen ein neues Projekt:

**Google lehnt etwas ab oder mahnt.** Dann zählt der Wortlaut — ohne ihn ist
jede Antwort geraten.

**Ein Knopf tut nichts, ein Ablauf klemmt.** So kam der Feenkreis-Fehler herein,
und er zeigt die Lücke: `GameViewModel` hat **keine einzige Prüfung**. Der
Spielkern ist dicht geprüft — der Feenkreis allein mit neun Prüfungen —, aber
alle sprechen mit der Maschine statt mit dem Knopf. Wer hier etwas sucht, sucht
zuerst in der Schicht zwischen Bedienung und Kern.

**Die App stürzt bei jemandem ab.** Der Absturzbericht steht in der Play
Console und ist lesbar, weil die Zuordnungsdatei im Bundle liegt
(`BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`). Sie muss
nicht getrennt hochgeladen werden.

**Die Werbung bringt weiter kein Leben.** Die Frage war beim Abschluss noch
offen: Am 25. September meldete das eigene Telefon „Werbung läuft" ohne
Belohnung. Die Einwilligungsnachricht war zu dem Zeitpunkt seit zwei Wochen
veröffentlicht, also lag es nicht daran. Wahrscheinlichste Erklärung ist eine
frische Anzeigeneinheit ohne Verkehr — das gibt sich von selbst, sobald echte
Leute spielen.

**Nachsehen lässt sich das ohne jedes Risiko**, denn es erfordert kein Antippen:
Im AdMob-Konto stehen unter Berichte drei Zahlen für die Anzeigeneinheit.

| Was dort steht | Was es heißt |
| --- | --- |
| Anfragen 0 | Die App fragt gar nicht — es hängt an der Einwilligung |
| Anfragen > 0, Impressionen 0 | Sie fragt, Google hat nichts — frische Einheit ohne Verkehr |
| Impressionen > 0 | Es läuft |

---

## Wie man wieder anfängt

```
cd /home/nataly/fairy-doo
export JAVA_HOME=/home/nataly/.jdks/jdk-17.0.20+8
./gradlew test                              # alle Prüfungen
./gradlew bundleRelease assembleRelease     # scharf, echte Werbung
./gradlew bundleReleaseTest                 # mit Googles Testanzeigen
```

Die Nummer muss steigen, spurübergreifend. Zuletzt hochgeladen: **66**.

---

*Gebaut zwischen dem 31. Juli und dem 29. September 2026 von Nataly Pilipovic
und Claude, für die App HUMB UG.*
