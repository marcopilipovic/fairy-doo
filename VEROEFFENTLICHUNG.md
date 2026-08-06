# Veröffentlichung im Play Store

Diese Datei sammelt, was für die Veröffentlichung nötig ist und was bereits
erledigt wurde. Sie liegt bewusst im Repo — der Schlüssel selbst und seine
Passwörter dagegen ausdrücklich **nicht**.

---

## ⚠️ Der Signierschlüssel — das Wichtigste zuerst

Der Upload-Schlüssel ist angelegt:

```
fairydoku-upload.keystore     der Schlüssel selbst
keystore.properties           Alias und Passwörter
```

Beide Dateien liegen im Projektordner und sind über `.gitignore` von Git
ausgeschlossen. Sie sind **nirgendwo sonst gespeichert**.

**Gehen sie verloren, lässt sich die App im Play Store nie wieder
aktualisieren.** Der Eintrag wäre dann tot — mit allen Bewertungen,
Installationen und Ranglisten. Es gibt dafür keine Wiederherstellung, auch
nicht durch Google.

### Sicherungen

- ✅ **USB-Stick** — am 6. August 2026 kopiert, Größe geprüft (4.302 Bytes)
- ⬜ **Zweiter Ort** — noch offen. Am einfachsten der Passwortmanager, wenn er
  Dateianhänge kann; sonst ein zweiter Stick, der woanders liegt.

Zwei Sicherungen an *unabhängigen* Orten sind der Sinn der Sache — nicht zweimal
derselbe Rechner, nicht zweimal dieselbe Cloud.

### Weiter zu tun

1. Das Passwort zusätzlich im Passwortmanager ablegen — getrennt von der Datei.
2. In der Play Console **Play App Signing** aktivieren (Standard bei neuen
   Apps). Google verwahrt dann den eigentlichen Verteilschlüssel; dieser hier
   ist nur der Upload-Schlüssel. Geht er trotzdem verloren, kann Google einen
   Austausch anbieten — aber verlassen sollte man sich darauf nicht.

### Fingerabdrücke

Für die Play Console und für Dienste, die eine Schlüsselbindung brauchen
(etwa Play Games oder Firebase):

```
SHA-1:   B4:10:3C:F8:E6:61:20:0D:19:1F:28:76:E5:05:DE:75:2A:13:8B:6B
SHA-256: 75:F9:9F:44:00:85:1D:42:96:C2:3D:90:AD:1D:E9:B8:4B:1D:5C:8D:1B:29:3B:B9:A2:0F:7B:1D:D8:7D:3E:F4
Gültig bis: 22. Dezember 2053
```

---

## Bauen

```bash
export JAVA_HOME=~/.jdks/jdk-17.0.20+8
export ANDROID_HOME=~/Android/Sdk

./gradlew bundleRelease      # app-release.aab — das lädst du hoch
./gradlew assembleRelease    # app-release.apk — zum Ausprobieren auf dem Gerät
./gradlew testDebugUnitTest  # Tests
```

Ergebnisse liegen in `app/build/outputs/`.

**Hochgeladen wird die `.aab`**, nicht die APK. Google baut daraus für jedes
Gerät eine passende, kleinere Fassung.

### Nach jedem Upload

`app/build/outputs/mapping/release/mapping.txt` in der Play Console
hinterlegen. Ohne diese Datei sind Absturzberichte unlesbar, weil der
Release-Build die Namen verschleiert.

---

## Stand der Vorbereitung

| | Punkt | Stand |
|---|---|---|
| 1 | Signierschlüssel | ✅ angelegt, eine Sicherung auf USB — zweite fehlt |
| 2 | Signierter Release-Build (AAB + APK) | ✅ baut durch, 5,5 MB APK |
| 3 | Entwicklerkonto bei Google (einmalig ~25 USD) | ⬜ **nur Nataly** |
| 4 | Einwilligungswerkzeug für Werbung (UMP) | ✅ eingebaut, Widerruf in den Einstellungen |
| 5 | Datenschutzerklärung unter einer Web-Adresse | ✅ Seite wird erzeugt — **hochladen fehlt** |
| 6 | Platzhalter in den Rechtstexten ausfüllen | ⬜ **nur Nataly** |
| 7 | Store-Eintrag: Symbol, Bilder, Beschreibung, Fragebögen | ⬜ offen |
| 8 | **Echte AdMob-Anzeigen-ID** statt der Test-ID | ⬜ **nur Nataly** |

### Die Rechtstext-Seite

`./gradlew testDebugUnitTest` erzeugt sie nach `app/build/rechtstexte/index.html`
— aus derselben Quelle, aus der auch die App ihre Texte bezieht. Beide können
damit nicht auseinanderlaufen.

Daneben entsteht `offene-platzhalter.txt` mit allem, was noch auszufüllen ist.

Die Datei muss irgendwo öffentlich erreichbar liegen; die Adresse trägst du in
der Play Console ein. Es genügt eine einzelne Datei — sie braucht kein
JavaScript und lädt nichts nach.

### Werbung: noch die Test-ID im Code

`app/src/main/java/com/fairydoo/game/ads/RewardedAdManager.kt` benutzt bislang
Googles offizielle **Test**-Anzeigen-ID, und im `AndroidManifest.xml` steht die
Test-App-ID. Beides muss vor der Veröffentlichung durch die echten Werte aus
einem AdMob-Konto ersetzt werden — sonst verdient die App nichts, und Google
beanstandet Testanzeigen in einer veröffentlichten App.

### Was nur Nataly erledigen kann

**Entwicklerkonto.** play.google.com/console, einmalig etwa 25 US-Dollar.
Die Identitätsprüfung dauert je nach Andrang ein paar Tage — das lohnt sich
früh anzustoßen.

**Die Platzhalter in `app/src/main/java/com/fairydoo/game/ui/GameCopy.kt`:**
`[Firmenname / Rechtsform]`, `[Straße und Hausnummer]`, `[PLZ und Ort]`,
`[E-Mail-Adresse]`, `[Name der Geschäftsführung]`, `[Monat Jahr]`.
Sie stehen in Impressum, AGB und Datenschutzerklärung.

---

## Angaben für den Store-Eintrag

Ergibt sich aus dem, was besprochen wurde:

- **Kategorie:** Puzzle — nicht „Familie"
- **Zielgruppe:** ab 13 Jahren; **nicht** ins Programm „Designed for Families"
- **Inhaltseinstufung:** kommt über den Fragebogen von allein auf die niedrigste
  Stufe — das Spiel enthält nichts Bedenkliches
- **Beschreibung:** nirgends „für Kinder" schreiben. Sonst kann Google die App
  trotz der Angabe „ab 13" als kindgerichtet einstufen, und die
  Familienrichtlinien greifen doch.
- **Datensicherheitsformular:** muss zur Datenschutzerklärung passen.
  Widersprüche zwischen beiden sind ein häufiger Ablehnungsgrund.

---

## Auslieferung

Nicht direkt in die Produktion, sondern der Reihe nach:

1. **Interner Test** — bis zu 100 Testende, keine Prüfung durch Google,
   Aktualisierung in Minuten
2. **Pre-Launch-Report abwarten** — Google lässt die App automatisch auf echten
   Geräten laufen und meldet Abstürze, bevor irgendwer sie sieht
3. **Geschlossener Test**
4. **Stufenweise Freigabe** — mit 5 % anfangen, Android Vitals beobachten,
   dann erhöhen

Der Grund: Eine schlechte Bewertung aus der Startwoche bleibt jahrelang stehen,
auch wenn der Fehler in einer Stunde behoben ist.

---

## Später, nicht jetzt

- **Online-Rangliste** (Firebase, nicht Play Games — siehe
  `RECHTSTEXTE-RANGLISTE.md`). Erst wenn es Spielerinnen gibt.
- **Ligen.** Brauchen 25–30 Aktive pro Gruppe, sonst wirken sie leer.
- **iOS.** Die Spiellogik ließe sich übernehmen, die Oberfläche wäre neu —
  Kotlin mit Compose läuft nicht auf dem iPhone.
