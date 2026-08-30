# Fairydoku — alles für die Veröffentlichung

Stand: 30. August 2026. Alles in diesem Ordner ist fertig zum Verwenden, außer
dem, was unter „Was noch fehlt" steht.

**Es gibt nur noch eine Fassung des Spiels.** Bis zum 28. August liefen zwei
Linien nebeneinander; sie sind zusammengeführt, der zweite Zweig ist gelöscht.
Alles liegt auf `main`.

---

## Für den Play Store

`play-store/`

| Datei | Wofür | Googles Anforderung |
| --- | --- | --- |
| `symbol-512x512.png` | App-Symbol im Store | 512 × 512 PNG ✓ |
| `feature-grafik-1024x500.png` | Kopfbild des Eintrags | 1024 × 500 ✓ |
| `bildschirmfotos/` | fünf Bilder, in der Reihenfolge der Dateinamen hochladen | mind. 2, höchstens 8 ✓ |
| `texte.md` | Name, Kurz- und Vollbeschreibung, Kategorie | — |
| `datensicherheit-und-einstufung.md` | Vorschläge für die beiden Fragebögen | — |

Die Bildschirmfotos sind 1080 × 2090, aus der Release-APK auf einem Pixel-5-
Emulator aufgenommen, ohne Statusleiste und Navigationsleiste. Die ersten
beiden erscheinen in der Suchliste, oft ohne dass jemand den Eintrag öffnet —
deshalb stehen Spielbrett und Feenpfad vorn.

> **Anmerkung zu den Bildern.** Zwei Stellen sind nachträglich bearbeitet, beide
> aus demselben Grund: Die Aufnahmen zeigten etwas, das die App nicht mehr
> zeigt.
>
> 1. Auf Bild 1 stand unten der alte Name der Waldfee. Ersetzt wurde nur diese
>    eine Zeile, in derselben Schrift, Größe und Farbe.
> 2. Auf den Bildern 1, 3 und 4 lief noch die Spieluhr. Sie ist herausgenommen
>    und die Blätterzeile wieder mittig gesetzt — genau so, wie die App sie seit
>    dem 28. August zeichnet.
>
> Beim nächsten Emulator-Durchgang gehören die drei trotzdem frisch aufgenommen.

## Für die Webseite

`webseite/`

| Datei | Wofür |
| --- | --- |
| `rechtstexte.html` | fertige Seite, hell und dunkel, ohne fremde Abhängigkeiten |
| `rechtstexte.md` | dieselben Texte als Markdown, falls die Seite anders gebaut wird |

Die Seite enthält vier Teile — Impressum, AGB, Datenschutzerklärung und
Lizenzen — mit den echten Angaben der App HUMB UG. Die Lizenzseite führt die
SIL Open Font License 1.1 und die Apache License 2.0 im englischen Wortlaut;
beide verlangen genau das, wenn man Schriften oder Bibliotheken mitliefert.

**Die Adresse dieser Seite gehört in den Play-Store-Eintrag**, und sie muss
erreichbar sein, bevor die App eingereicht wird — Google ruft sie ab und lehnt
sonst ab.

### Wichtig: nicht von Hand ändern

Die Texte stammen aus dem App-Quelltext (`GameCopy.legalBody`). Ein Unit-Test
erzeugt daraus sowohl den Rohtext als auch diese Seite. Zwei Fassungen, die von
Hand gepflegt werden, laufen früher oder später auseinander — meist unbemerkt,
weil niemand beide nebeneinander liest. Genau das war hier schon passiert.

Wer die Texte ändern will, ändert sie **im Code**. Danach:

```
gradlew.bat testDebugUnitTest --tests "*RechtstexteExportTest*"
```

Das Ergebnis liegt in `app/build/rechtstexte/` und wird von dort hierher
kopiert. Der Test prüft dabei mit, dass keine Platzhalter übrig sind, dass die
Haftungsklausel nicht zurückkehrt und dass die Paragrafen lückenlos
durchlaufen.

## Die App

**Im Repository liegt bewusst keine APK.** Eine mitgelieferte Datei ist nach
zwei Änderungen veraltet, und niemand sieht ihr das an. Im weitergereichten
Paket liegt trotzdem eine unter `app/` — sonst müsste der Kollege erst bauen
können. Was dort liegt, ist der Stand vom 29. August:

| Datei | Wofür |
| --- | --- |
| `Fairydoku-0.9.4-35.apk` | zum Ausprobieren auf einem Telefon |
| `Fairydoku-0.9.4-35.aab` | die Bauform, die der Store haben will |

**Beide tragen noch Googles Testkennungen für die Werbung** (siehe „Was noch
fehlt", Punkt 1). Sie zeigen, wie die App aussieht und läuft — hochgeladen wird
erst eine Fassung mit den echten Kennungen.

Selbst bauen geht in einer Minute:

```
gradlew.bat assembleRelease     → app/build/outputs/apk/release/  (rund 4 MB)
gradlew.bat bundleRelease       → app/build/outputs/bundle/release/  (rund 7 MB)
```

Die **APK** ist zum Ausprobieren auf einem Telefon. Das **App Bundle** (`.aab`)
ist das, was der Play Store haben will — die APK dort hochzuladen geht nicht.

Beides ist bereits signiert, sofern `keystore.properties` im Projektordner
liegt. Der Stand vom 30. August: versionCode 35, versionName 0.9.4.

---

## Was seit dem 19. August dazugekommen ist

| Was | Warum |
| --- | --- |
| Haftungsklausel aus den AGB entfernt | auf Weisung; §§ 11–13 wurden zu 10–12 |
| Rechtstexte gegliedert | Überschriften und Aufzählungen; Seite und App aus einer Quelle |
| Vierte Rechtliches-Seite: Lizenzen | OFL 1.1 und Apache 2.0 verlangen, dass ihr Text mitgeliefert wird |
| Waldfee heißt Viridis statt Flora | „Flora" ist zugleich eine Fee bei Disney und bei Winx Club |
| Store-Text berichtigt | das Gitter wächst alle **zwei** Level, nicht mit jedem |
| Ziel-API 36 | ab 31.08.2026 nimmt Google nichts Niedrigeres mehr an |
| Werkzeugkette angehoben | AGP 8.13.2, Gradle 8.13 |
| Brett auf flache Fenster vorbereitet | Android 16 erzwingt auf großen Bildschirmen kein Hochformat mehr |
| **Die zwei Linien zusammengeführt** | siehe unten — dabei kamen vier Fehler ans Licht |
| **Die Spieluhr ist raus** | Sie bestrafte das Nachdenken, für das das Spiel gemacht ist |
| Werbung kann das Spiel nicht mehr einfrieren | Kam keine Anzeige, blieb das Spiel stehen — nur ein Neustart half |
| Waldmusik und Schreckenslaut sind wieder Aufnahmen | Die berechneten Fassungen gefielen der Testrunde nicht |

### Was die Zusammenführung ans Licht brachte

Beide Linien waren unvollständig, jede auf ihre Art. Vier Dinge waren falsch
und sind es jetzt nicht mehr:

- **Die Tageswertung fehlte.** Store-Text, AGB § 6 und Datenschutzerklärung
  Abschnitt 4 beschrieben sie — gebaut war sie auf dieser Linie nie. Das
  übersprungene fünfte Bildschirmfoto war der Fingerabdruck davon.
- **Die Musik war verstummt.** Auf der anderen Linie las der Abspieler eine
  Tondatei, die es nach dem Entfernen der Aufnahme nicht mehr gab.
- **„Der Wald wird dichter"** erschien bei jedem Level, obwohl das Gitter nur
  jedes zweite Mal wächst — derselbe Fehler wie im Store-Text.
- **Vier Statusmeldungen waren zu lang** und brachen auf dem Gerät ab, unter
  anderem „✨ Der Feenstaub zeigt dir ein sicheres Feld!" mit 44 Zeichen.

Die vollständige Prüfung aller Inhalte steht in `pruefbericht.md`.

---

## Was noch fehlt

Nichts davon ist Programmierarbeit.

### 1. Die echten AdMob-Kennungen

Im Code stehen noch Googles Test-IDs, an zwei Stellen:

- `app/src/main/AndroidManifest.xml` — die App-ID (mit Tilde)
- `app/src/main/java/com/fairydoo/game/ads/RewardedAdManager.kt` — die
  Anzeigenblock-ID (mit Schrägstrich)

Beide beginnen heute mit `ca-app-pub-3940256099942544`; das ist Googles
öffentliche Testkennung. **Mit ihr darf die App nicht veröffentlicht werden.**

### 2. Die Einwilligungsnachricht im AdMob-Konto

Die App fragt die Einwilligung über Googles User Messaging Platform ab. Damit
dort etwas erscheint, muss im AdMob-Konto unter den EU-Einstellungen eine
Nachricht angelegt **und veröffentlicht** sein. Fehlt sie, zeigt die App den
Dialog, bekommt aber keinen Inhalt dafür.

### 3. Die Datenschutz-Seite ins Netz

`webseite/rechtstexte.html` braucht nur einen öffentlich erreichbaren Ort.
Vorgesehen ist `https://humb.ug/fairydoku/rechtstexte`.

### 4. Der Signierschlüssel sichern

Die Frage, welcher der richtige ist, ist beantwortet: Es ist
`fairydoku-upload.keystore` im Projektordner, RSA 4096 Bit, gültig bis zum
22. Dezember 2053. Von `fairydoku-release.jks` gibt es keine Spur; der Name
stammt aus einer Notiz, nicht aus dem Projekt.

Zu sichern sind **zwei** Dateien, und nur zusammen nützen sie etwas:

- `fairydoku-upload.keystore` — der Schlüssel
- `keystore.properties` — die zwei Kennwörter und der Alias

Beide sind absichtlich nicht im Repository.

Zur Beruhigung: Neue Apps nutzen Play App Signing. Google verwahrt den
eigentlichen Signaturschlüssel, ihr signiert nur mit einem *Upload*-Schlüssel.
Geht der verloren, lässt er sich über den Play-Support zurücksetzen — lästig,
aber nicht endgültig.

### 5. Das App-Bundle bauen

Sobald Punkt 1 erledigt ist:

```
gradlew.bat bundleRelease
```

Das Ergebnis liegt unter `app/build/outputs/bundle/release/`. **Das** ist die
Datei, die hochgeladen wird. Sie ist am 29. August zuletzt fehlerfrei gebaut
worden — signiert, verkleinert, nicht debugfähig.

### 6. Einmal in Ruhe auf einem echten Telefon durchspielen

Die Testrunde hat inzwischen einiges abgedeckt; von dort kamen die Einrückung
der Rechtstexte, die stehengebliebene Werbung und der Wunsch, die Uhr und die
berechnete Musik loszuwerden. Nicht angesehen hat bisher jemand das
**Querformat**: Android 16 achtet auf großen Bildschirmen nicht mehr auf die
Festlegung auf Hochformat. Das Brett ist darauf vorbereitet, gesehen hat es dort
aber noch niemand.

### 7. Zwei Dinge, die eine Viertelstunde kosten

- ~~Markenrecherche~~ — **erledigt am 30. August 2026.** TMview auf
  „fairydoku": keine Zeilen gefunden. TMview führt auch die nationalen
  Register, das DPMA eingeschlossen. Einzelheiten im `pruefbericht.md`.
- **Entwicklername prüfen.** Google zeigt bei verifizierten Konten Name und
  Anschrift im Eintrag an. Sie müssen mit dem Impressum übereinstimmen: App
  HUMB UG (haftungsbeschränkt), Parkstraße 9, 31188 Holle.

### 8. Eine rechtliche Freigabe der Texte

Sie sind vollständig und beschreiben die App wahrheitsgemäß, aber niemand mit
Rechtskenntnis hat sie gelesen.
