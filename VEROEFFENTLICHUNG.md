# Veröffentlichung im Play Store

Das Handbuch zum Ausliefern: der Schlüssel, die Baubefehle, die Angaben für den
Eintrag und die Reihenfolge, in der eine Fassung nach draußen geht.

**Was noch offen ist, steht nicht hier, sondern in `STAND.md`.** Diese Datei
führte bis zum 8. September eine zweite Liste davon — sie war an sechs Stellen
überholt und behauptete unter anderem, die Datenschutzseite sei noch nicht
hochgeladen und im Code stünden noch Testkennungen. Zwei Listen desselben
Inhalts laufen auseinander, und man merkt es beim Falschen.

Aufgeteilt ist es so:

| Frage | Datei |
| --- | --- |
| Was fehlt noch? | `STAND.md` |
| Wie liefere ich aus? | diese Datei |
| Was liegt im weitergereichten Paket? | `storepaket/LIESMICH.md` |
| Was wurde vor der Veröffentlichung geprüft? | `storepaket/pruefbericht.md` |

---

## ⚠️ Der Signierschlüssel — das Wichtigste zuerst

```
fairydoku-upload.keystore     der Schlüssel selbst, 4302 Byte
keystore.properties           Alias und die zwei Kennwörter
```

Beide liegen im Projektordner und sind über `.gitignore` von Git ausgeschlossen.
**Gehen sie verloren, lässt sich die App im Play Store nicht mehr
aktualisieren** — mit allen Bewertungen und Installationen.

**Gesichert am 30. August 2026 an drei Orten:** dieser Rechner, ein USB-Stick
und ein Ausdruck. Das Druckblatt schreibt den Schlüssel als 80 Zeilen Text um,
jede mit eigener Prüfsumme, dazu Fingerabdruck und Rückweg — erzeugt von
`/home/nataly/schluessel-sicherung/bauen.py`, und die Rückrechnung ist
nachgeprüft: Byte für Byte identisch.

Beim ersten Upload **Play App Signing** aktivieren (Standard bei neuen Apps).
Google verwahrt dann den eigentlichen Verteilschlüssel; dieser hier ist nur der
Upload-Schlüssel und ließe sich im Notfall über den Support austauschen.

### Fingerabdrücke

Für die Play Console und für Dienste, die eine Schlüsselbindung brauchen:

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

./gradlew testDebugUnitTest    # Tests, erzeugt nebenbei die Rechtstext-Seite
./gradlew bundleReleaseTest    # .aab für die Testspuren, mit Googles Testwerbung
./gradlew bundleRelease        # .aab für die Veröffentlichung, echte Kennungen
./gradlew assembleReleaseTest  # dieselbe Testfassung als APK fürs Telefon
```

Ergebnisse liegen unter `app/build/outputs/`. **Hochgeladen wird die `.aab`**,
nicht die APK — Google baut daraus für jedes Gerät eine passende, kleinere
Fassung.

**Es gibt zwei Bauarten, die im Store landen können.** Beide tragen denselben
Paketnamen `ug.humb.fairydoku`, sind verkleinert, verschleiert und mit demselben
Schlüssel signiert; sie unterscheiden sich allein in der Werbung:

| | Werbung | wofür |
| --- | --- | --- |
| `releaseTest` | Googles Testanzeigen | interne und geschlossene Tests |
| `release` | die echten Kennungen | die Veröffentlichung |

Der Sinn: Wer auf eine echte Anzeige tippt, erzeugt „ungültigen Traffic" — der
häufigste Weg, ein AdMob-Konto zu verlieren. Auf die Testfassung darf die
Testrunde tippen, so oft sie will.

**Die Nummer muss steigen**, und zwar spurübergreifend. Zuletzt gebaut:
versionCode 61, versionName 1.5.7.

### Nach dem Upload

Nichts. Die Zuordnungsdatei, ohne die Absturzberichte unlesbar bleiben, liegt im
Bundle selbst unter
`BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map` — nachgesehen
am 8. September 2026 im Bundle der 1.5.7. Nur wer eine APK statt eines Bundles
hochlädt, muss `app/build/outputs/mapping/<Bauart>/mapping.txt` von Hand
hinterlegen.

### Die Rechtstext-Seite

`./gradlew testDebugUnitTest` erzeugt sie nach `app/build/rechtstexte/` — aus
derselben Quelle, aus der die App ihre Texte bezieht (`ui/GameCopy.kt`). Beide
können damit nicht auseinanderlaufen; ein Test hält sie gleich. Die Seiten
liegen unter `storepaket/webseite/seiten/` und stehen im Netz:

```
https://fairydoku.sites.humb.ug/de/impressum
https://fairydoku.sites.humb.ug/de/nutzungsbedingungen
https://fairydoku.sites.humb.ug/de/datenschutz     ← die für den Store
https://fairydoku.sites.humb.ug/de/lizenzen
```

Für den Eintrag zählt `/de/datenschutz` — nicht die Startseite. Google ruft
genau diese Adresse ab.

---

## Angaben für den Store-Eintrag

- **Konto:** das Organisationskonto der App HUMB UG, nicht ein Privatkonto.
  Dafür ist eine D-U-N-S-Nummer nötig. Google zeigt bei verifizierten Konten
  Name und Anschrift im Eintrag an; sie müssen mit dem Impressum
  übereinstimmen — App HUMB UG (haftungsbeschränkt), Parkstraße 9, 31188 Holle.
- **Kategorie:** Puzzle — nicht „Familie"
- **Zielgruppe:** ab 13 Jahren; **nicht** ins Programm „Designed for Families"
- **Inhaltseinstufung:** kommt über den Fragebogen von allein auf die niedrigste
  Stufe — das Spiel enthält nichts Bedenkliches
- **Beschreibung:** nirgends „für Kinder" schreiben. Sonst kann Google die App
  trotz der Angabe „ab 13" als kindgerichtet einstufen, und die
  Familienrichtlinien greifen doch.
- **Datensicherheitsformular:** muss zur Datenschutzerklärung passen.
  Widersprüche zwischen beiden sind ein häufiger Ablehnungsgrund.
- **Play Games-Dienste:** nein, nicht zu dieser Veröffentlichung. Begründung in
  `STAND.md`.

Texte, Bilder und die Antworten für beide Fragebögen liegen fertig in
`storepaket/play-store/`.

---

## Auslieferung

Nicht direkt in die Produktion, sondern der Reihe nach:

1. **Interner Test** — bis zu 100 Testende, keine Prüfung durch Google,
   Aktualisierung in Minuten
2. **Pre-Launch-Report abwarten** — Google lässt die App automatisch auf echten
   Geräten laufen und meldet Abstürze, bevor irgendwer sie sieht
3. **Stufenweise Freigabe** — mit 5 % anfangen, Android Vitals beobachten, dann
   erhöhen

Der Grund: Eine schlechte Bewertung aus der Startwoche bleibt jahrelang stehen,
auch wenn der Fehler in einer Stunde behoben ist.

*Der geschlossene Test dazwischen entfällt.* Er ist die Auflage für
Privatkonten — zwölf Testende über vierzehn Tage; für das Organisationskonto
gilt sie nicht.

---

## Was sich nur auf einem echten Telefon prüfen lässt

| | Was | Warum |
| --- | --- | --- |
| ⬜ | **Tagesabschluss** | Kommt das Overlay nach vier Uhr früh? Kommt der Feenstaub im Vorrat an? Erscheint es nur einmal? |
| ⬜ | **Werbung** | Ab dem vierten Level. Kommt die Belohnung an? |
| ⬜ | **Einwilligungsdialog** | Nur bei frischer Installation und nur in der EU. Ablehnen muss sauber funktionieren. |
| ⬜ | **Neustart** | Steht der Stand nach vollständigem Schließen noch? |
| ⬜ | **Querformat** | Android 16 erzwingt auf großen Bildschirmen kein Hochformat mehr. Das Brett ist vorbereitet, gesehen hat es dort noch niemand. |
| ✅ | **Release-Fassung** | Läuft. Die Testrunde spielt seit dem 28. August signierte Fassungen. |
| ✅ | **Klang insgesamt** | Gehört — und daraufhin sind die berechnete Musik und der Schreckenslaut wieder durch die Aufnahmen ersetzt. |

---

## Später, nicht jetzt

- **Illustrierte Pilze** — im Spielbildschirm stehen noch Emoji als Platzhalter
- **Feentitel aus Bausteinen** statt des freien Namensfelds
- **Online-Bestenliste über Play Games.** Erst wenn es Spielerinnen gibt: Eine
  Bestenliste ohne Spieler zeigt eine leere Liste. Sämtliche Texte dafür liegen
  fertig in `PLAY-GAMES-START.md` — Rechtstexte, Store, Oberfläche, Einrichtung
  in der Console, beide Fragebögen. *(Hier stand bis zum 8. September
  „Firebase, nicht Play Games". Play Games kostet nichts und braucht keinen
  eigenen Server; Firebase beides.)*
- **Ligen.** Brauchen 25–30 Aktive je Gruppe, sonst wirken sie leer.
- **iOS.** Die Spiellogik ließe sich übernehmen, die Oberfläche wäre neu —
  Kotlin mit Compose läuft nicht auf dem iPhone.
