# Fairydoku — alles für die Veröffentlichung

Stand: 28. August 2026. Alles in diesem Ordner ist fertig zum Verwenden, außer
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

> **Anmerkung zum ersten Bild.** Nach der Umbenennung der Waldfee zeigte es
> unten einen Namen, den die App nicht mehr kennt. Ersetzt wurde nur diese eine
> Zeile, in derselben Schrift, Größe und Farbe, die die App dafür benutzt. Das
> Bild zeigt, was die App heute anzeigt — beim nächsten Emulator-Durchgang
> gehört es trotzdem frisch aufgenommen.

## Für die Webseite

`webseite/`

| Datei | Wofür |
| --- | --- |
| `rechtstexte.html` | fertige Seite, hell und dunkel, ohne fremde Abhängigkeiten |
| `rechtstexte.md` | dieselben Texte als Markdown, falls die Seite anders gebaut wird |

Die Seite enthält Impressum, AGB und Datenschutzerklärung mit den echten
Angaben der App HUMB UG.

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

`Fairydoku-2026-08-26-debug.apk` — zum Ausprobieren auf einem echten Telefon.
Debug-Fassung, deshalb 22 MB statt gut 3; sie ist unverkleinert und nicht
verschleiert.

**Nicht diese Datei hochladen.** Der Play Store will ein signiertes App-Bundle
(`.aab`) — siehe unten.

---

## Was seit dem 19. August dazugekommen ist

| Was | Warum |
| --- | --- |
| Haftungsklausel aus den AGB entfernt | auf Weisung; §§ 11–13 wurden zu 10–12 |
| Rechtstexte gegliedert | Überschriften und Aufzählungen; Seite und App aus einer Quelle |
| Waldfee heißt Viridis statt Flora | „Flora" ist zugleich eine Fee bei Disney und bei Winx Club |
| Store-Text berichtigt | das Gitter wächst alle **zwei** Level, nicht mit jedem |
| Ziel-API 36 | ab 31.08.2026 nimmt Google nichts Niedrigeres mehr an |
| Werkzeugkette angehoben | AGP 8.13.2, Gradle 8.13 |
| Brett auf flache Fenster vorbereitet | Android 16 erzwingt auf großen Bildschirmen kein Hochformat mehr |
| **Die zwei Linien zusammengeführt** | siehe unten — dabei kamen vier Fehler ans Licht |

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

Die vollständige Prüfung aller Inhalte steht in `pruefbericht.md` (19.08.,
Rechte und Stabilität) — die Markenlage und die Store-Texte sind am 25.08.
getrennt geprüft worden.

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

### 4. Der Signierschlüssel

Er liegt nur auf einem Rechner und ist absichtlich nicht im Repository. Zwei
Dinge sind offen: ihn an einen zweiten Ort zu sichern, und zu klären, welcher
der beiden vorhandenen der richtige ist — auf dem einen Rechner liegt
`fairydoku-upload.keystore`, auf dem anderen wird `fairydoku-release.jks`
genannt.

Zur Beruhigung: Neue Apps nutzen Play App Signing. Google verwahrt den
eigentlichen Signaturschlüssel, ihr signiert nur mit einem *Upload*-Schlüssel.
Geht der verloren, lässt er sich über den Play-Support zurücksetzen — lästig,
aber nicht endgültig.

### 5. Das App-Bundle

Sobald 1, 2 und 4 stehen, mit angelegter `keystore.properties`:

```
gradlew.bat bundleRelease
```

Das Ergebnis liegt unter `app/build/outputs/bundle/release/`. **Das** ist die
Datei, die hochgeladen wird.

### 6. Einmal auf einem echten Telefon durchspielen

Bisher ist alles im Emulator geprüft. Seit der Umstellung auf Ziel-API 36
lohnt ein gezielter Blick auf das Querformat: Android 16 achtet auf großen
Bildschirmen nicht mehr auf die Festlegung auf Hochformat. Das Brett ist darauf
vorbereitet, gesehen hat es dort aber noch niemand.

### 7. Zwei Dinge, die eine Viertelstunde kosten

- **Markenrecherche.** „Fairydoku" liefert bei offener Suche keine Treffer.
  Eine belastbare Auskunft gibt es kostenlos im
  [DPMAregister](https://register.dpma.de/DPMAregister/marke/einsteiger) und
  bei [TMview](https://www.tmdn.org/tmview/) — Klasse 9 und 41.
- **Entwicklername prüfen.** Google zeigt bei verifizierten Konten Name und
  Anschrift im Eintrag an. Sie müssen mit dem Impressum übereinstimmen: App
  HUMB UG (haftungsbeschränkt), Parkstraße 9, 31188 Holle.

### 8. Eine rechtliche Freigabe der Texte

Sie sind vollständig und beschreiben die App wahrheitsgemäß, aber niemand mit
Rechtskenntnis hat sie gelesen.
