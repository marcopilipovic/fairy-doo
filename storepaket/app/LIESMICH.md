# Fairydoku 1.5.7 (Nummer 61) — zum Hochladen

Gebaut am 8. September 2026 aus `main`, Stand `203699d`.

| Datei | Wofür |
| --- | --- |
| `Fairydoku-1.5.7-61-TEST.aab` | **das hier in die Testspur hochladen** |
| `Fairydoku-1.5.7-61-TEST.apk` | dieselbe Fassung zum Ausprobieren am Telefon |

Paket `ug.humb.fairydoku`, versionCode **61**, versionName **1.5.7-test**,
Ziel-API 36, mindestens Android 8. Signiert mit dem Upload-Schlüssel,
SHA-256 `75:F9:9F:44:00:85:1D:42:96:C2:3D:90:AD:1D:E9:B8:4B:1D:5C:8D:1B:29:3B:B9:A2:0F:7B:1D:D8:7D:3E:F4`.

**Es ist die Testfassung.** Sie ist in allem gleich wie die spätere
Veröffentlichung — derselbe Paketname, verkleinert, verschleiert, mit
demselben Schlüssel signiert — bis auf die Werbung: Sie zeigt Googles
Testanzeigen. Darauf darf die Testrunde tippen, ohne dass es dem AdMob-Konto
als „ungültiger Traffic" angerechnet wird.

**Die Zuordnungsdatei musst du nicht getrennt hochladen.** Sie liegt im Bundle
selbst unter `BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`
(46 MB); die Play Console nimmt sie von dort. Abstürze sind damit lesbar.

## Was neu ist

Der eine Fehler, den diese Fassung behebt, kam am 8. September aus der Runde:
Das Feld für den Profilnamen sprang bei jedem Buchstaben zurück, eine Eingabe
war nicht möglich. Ursache und Behebung stehen im Verlauf unter „Das
Namensfeld sprang bei jedem Buchstaben zurück".

Alles Übrige ist der Stand von 1.5.6: der Feenkreis als dritter Helfer, der
Knopf zum Leeren des Bretts, das mitwachsende Brett, Haptik auch beim kurzen
Tippen, drei Minuten Bildschirmzeit statt dauerhaft an, und die Punktezahl im
Rätsel.

Die Texte für die Testspur — deutsch und englisch — stehen fertig zum Kopieren
in `../play-store/versionshinweise.md`, Abschnitte 2e und 2f.

## Was dabei nicht mitkommt

**Die fünf Bildschirmfotos in `../play-store/bildschirmfotos/` sind vom
29. August** und zeigen die App vor dem größeren Brett und vor dem dritten
Helfer. Für eine Testspur ist das gleichgültig — sie werden dort nicht
gezeigt. Vor der Veröffentlichung gehören sie neu aufgenommen.

**Die Einwilligungsnachricht im AdMob-Konto** ist weiterhin offen (EU-
Einstellungen, anlegen *und* veröffentlichen). Ohne sie erscheint der Dialog
leer. Für die Testfassung mit Googles Testanzeigen ist das nicht kritisch,
für die Veröffentlichung schon.
