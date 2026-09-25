# Fairydoku 1.5.11 (Nummer 65) — für den offenen Test

Gebaut am 20. September 2026 aus `main`.

| Datei | Wofür |
| --- | --- |
| `Fairydoku-1.5.11-65.aab` | **das hier hochladen** — Veröffentlichungsfassung, echte Werbung |
| `Fairydoku-1.5.11-65.apk` | dieselbe Fassung zum Ausprobieren am Telefon |
| `Fairydoku-1.5.11-65-TEST.aab` | dasselbe mit Googles Testanzeigen, falls doch noch eine geschlossene Runde dazwischenkommt |
| `Fairydoku-1.5.11-65-TEST.apk` | dazu die APK |

Paket `ug.humb.fairydoku`, versionCode **65**, versionName **1.5.11**,
Ziel-API 36, mindestens Android 8. Signiert mit dem Upload-Schlüssel,
SHA-256 `75:F9:9F:44:00:85:1D:42:96:C2:3D:90:AD:1D:E9:B8:4B:1D:5C:8D:1B:29:3B:B9:A2:0F:7B:1D:D8:7D:3E:F4`.

Beide tragen dieselbe Nummer 65 — hochladen lässt sich nur eine davon.

---

## 1. Warum diesmal die Fassung mit der echten Werbung

In den geschlossenen Runden war die Testfassung richtig: Googles
Beispielanzeigen, auf die das Team beliebig tippen durfte. **Im offenen Test ist
die App öffentlich**, und Testanzeigen in einer ausgelieferten App sind genau
das, was Googles Regeln untersagen. Davon abgesehen prüft niemand die Werbung,
wenn keine echte läuft.

**Dafür gehören eure Geräte ins AdMob-Konto**, unter *Einstellungen →
Testgeräte*. Dann sehen sie weiterhin Beispielanzeigen und dürfen tippen; alle
anderen sehen echte. Ohne diesen Eintrag erzeugt jeder eigene Tipp „ungültigen
Traffic" — der häufigste Weg, ein AdMob-Konto zu verlieren.

**Und die Einwilligungsnachricht muss stehen.** Im AdMob-Konto unter
*Datenschutz und Meldungen → DSGVO* eine Nachricht anlegen **und
veröffentlichen**. Fehlt sie, liefert Google im EWR kein Formular aus, die App
bekommt keine Einwilligung — und damit kommt gar keine Anzeige. Der Knopf
verspricht dann ein Belohnungsvideo, das nie erscheint.

Die APKs sind zum Ausprobieren am Telefon; hochgeladen wird immer die `.aab`.

**Die Zuordnungsdatei musst du nicht getrennt hochladen.** Sie liegt im Bundle
unter `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`; die
Play Console nimmt sie von dort.

---

## 2. Was seit der 1.5.6 dazugekommen ist

Die 1.5.6 als Nummer 60 liegt in der Testspur. Seither:

- **Das Namensfeld ließ sich nicht bedienen** (1.5.7). Es sprang bei jedem
  Buchstaben zurück; gemeldet aus der Runde, am selben Tag behoben.
- **Die Anleitung kennt den Feenkreis** (1.5.8). Sie zeigte zwei Helfer, die
  Leiste im Spiel drei.
- **Die Levelkarte wuchs nicht mit** (1.5.10). Gemeldet von einem Samsung S21:
  „Da ist die Karte klein." Es lag nicht am Gerät, sondern an seiner
  Einstellung — Samsungs Bildschirmzoom ändert die Dichte, und derselbe
  Bildschirm ist dann 411 dp breit statt 360. Die Levelkreise standen mit
  54 dp fest und wirkten darin verloren. Jetzt wächst die Karte mit dem Platz,
  wie das Spielbrett es seit dem 30. August tut.
- **Drei Meldungen der Play Console behoben** (1.5.9): veraltetes
  `androidx.fragment` aus Googles Werbe-SDK, veraltete Fenster-Schnittstellen
  für die randlose Anzeige — und der eigentliche Fund: **In flachen Fenstern
  verschwand das Spielbrett.** Auf einem Tablet im Querformat war es nicht mehr
  da. Seit Ziel-API 36 achtet Android auf großen Bildschirmen nicht mehr auf
  die Festlegung aufs Hochformat.
- **Das Spiel schwieg, wenn keine Anzeige kam** (1.5.11). Aus der Runde
  gemeldet: „Es läuft keine Werbung, da steht dann halt nur geschrieben,
  Werbung läuft und es wird kein Leben aufgefüllt." Am Spiel lag es nicht —
  es fragt korrekt an und bekommt nichts zurück (siehe Einwilligungsnachricht,
  Abschnitt 1). Aber es sagte davon nichts. Jetzt steht im Verloren-Dialog,
  wann von selbst ein Leben nachwächst, und wenn die Anfrage ins Leere läuft:
  „Gerade kommt keine Anzeige — versuch es später noch einmal."

**Die Texte für die Spur** stehen in `texte/versionshinweise.md`, Abschnitt
**2g** auf deutsch und **2h** auf englisch.

---

## 3. Was vorher noch zu tun ist

- **Die Einwilligungsnachricht** (siehe oben) — ohne sie keine Werbung im EWR.
- **Die Händlererklärung** in der Play Console (DSA). Als App HUMB UG seid ihr
  Händler; Name, Anschrift und E-Mail erscheinen dann öffentlich im Eintrag und
  müssen mit dem Impressum übereinstimmen.
- ~~Die Bildschirmfotos.~~ **Erledigt am 11. September.** Die fünf sind neu und
  zeigen den heutigen Stand: das mitgewachsene Brett, alle drei Helfer, den
  brennenden Feenkreis, ein 8×8-Gitter und den Gewinn-Dialog. Sie werden
  gerechnet statt aufgenommen — ändert sich ein Bildschirm, laufen sie neu
  durch.

## 4. Was in diesem Paket sonst liegt

`texte/` — der Store-Eintrag deutsch und englisch, Versions- und
Testerhinweise, die Antworten für Datensicherheit und Alterseinstufung, dazu
`PLAY-GAMES-START.md` als Vorrat (nicht eintragen, siehe Datei).

`webseite/` — die vier Rechtstext-Seiten deutsch, dieselben englisch.

`store-grafik/` — Symbol, Feature-Grafik, die alten Bildschirmfotos.
