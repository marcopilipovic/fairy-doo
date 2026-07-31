# Fairydoku

Android-Spiel für den magischen Wald — natives Kotlin mit Jetpack Compose.

Du bist Hüter eines Feenreichs und setzt Feen auf ein Zonen-Gitter, ohne dass
sich ihre Zauberkräfte stören. Endlos-Modus: Mit jedem Level wird der Wald
dichter.

## Die Regeln

Auf jedem Brett gilt:

- genau **eine Fee je Zeile**,
- genau **eine Fee je Spalte**,
- genau **eine Fee je Waldzone** (die farbigen Bereiche),
- **keine zwei Feen berühren sich** — auch nicht diagonal.

Ein Tipp auf ein Feld schaltet weiter: leer → Fee → Merkzeichen → leer. Das
Merkzeichen ist reine Notizhilfe („hier sitzt sicher keine“).

Eine Fee, die beim Setzen mit einer anderen kollidiert, kostet einen Fehler.
Nach drei Fehlern oder abgelaufener Zeit ist der Lauf vorbei.

## Die Magie-Fähigkeiten

| Fähigkeit | Wirkung |
| --- | --- |
| **Feenstaub** | Setzt eine Fee auf ein garantiert sicheres Feld. Bringt keine Punkte. |
| **Natur-Schild** | Fängt den nächsten Fehler ab und verbraucht sich dabei. |
| **Zeiten-Blüte** | Lässt die Uhr 12 Sekunden lang halb so schnell laufen. |

Nach jedem Level wird eine Fähigkeit aufgefüllt, der Reihe nach.

## Bauen und starten

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

.\gradlew.bat assembleDebug        # APK bauen
.\gradlew.bat testDebugUnitTest    # Unit-Tests
.\gradlew.bat installDebug         # auf verbundenes Gerät/Emulator
```

## Aufbau

```
app/src/main/java/com/fairydoo/game/
├── MainActivity.kt                  Einstiegspunkt, Theme + Navigation
├── game/
│   ├── model/
│   │   ├── Puzzle.kt                Gitter, Zonen und die Regeln (FairydokuRules)
│   │   └── PuzzleGenerator.kt       Erzeugt Rätsel mit eindeutiger Lösung
│   ├── GameState.kt                 Unveränderlicher Partie-Zustand
│   ├── GameEngine.kt                FairydokuEngine: Züge, Fähigkeiten, Punkte
│   └── GameViewModel.kt             Spieluhr, Zustandsverwaltung, Persistenz
├── data/
│   └── GamePreferences.kt           DataStore: Highscore, Partien, Einstellungen
└── ui/
    ├── theme/                       Farben, Typografie, Material-Theme
    ├── navigation/                  Alle Ziele an einer Stelle
    ├── components/
    │   ├── FairydokuBoard.kt        Canvas-Zeichenschicht des Bretts
    │   └── PowerUpBar.kt            Die drei Fähigkeiten
    └── screens/                     Home, Game, Settings
```

### Drei Prinzipien, die das Projekt trägt

**Die Regeln kennt nur die Engine.** `FairydokuRules` und `FairydokuEngine` sind
reine Logik ohne Android-Abhängigkeiten: alter Zustand + Ereignis → neuer
Zustand. Deshalb laufen sie als schnelle JVM-Tests, und UI, ViewModel und
Persistenz bleiben von Regeländerungen unberührt.

**Jedes Rätsel hat genau eine Lösung.** Der Generator würfelt nicht nur, bis es
passt — er schließt alternative Lösungen gezielt aus, indem er einzelne Felder
in andere Zonen umhängt (`enforceUniqueness`). Ohne das hatte ab 7×7 praktisch
kein Brett mehr eine eindeutige Lösung, und der Feenstaub hätte Felder
aufgedeckt, die zu einer *anderen* Lösung gehören als der, die der Spieler
gerade baut.

**Screens holen Daten, Contents stellen dar.** Jeder Screen ist in ein
`XyzScreen` (Datenzugriff) und ein privates `XyzContent` (reine Darstellung)
geteilt. Nur deshalb funktionieren die `@Preview`-Funktionen ohne laufende App.

## Wenn das Design kommt

Der Anschluss ist auf wenige Stellen begrenzt:

| Was | Wo |
| --- | --- |
| Farben, Zonenfarben | `ui/theme/Color.kt` (`RegionColors` für die Waldzonen) |
| Schrift | TTF/OTF nach `res/font/`, dann `GameFontFamily` in `ui/theme/Type.kt` |
| Feen-Darstellung | `drawFairy` in `ui/components/FairydokuBoard.kt` |
| Fähigkeiten-Symbole | `ui/components/PowerUpBar.kt` |
| Launcher-Icon | `res/drawable/ic_launcher_foreground.xml` |

Material You / Dynamic Color ist bewusst **aus**: Das Spiel soll auf jedem Gerät
gleich aussehen und nicht die Systemfarben des Nutzers übernehmen.

## Balance an einem Ort

Alle Stellschrauben stehen als Konstanten in `GameState.Companion` und
`FairydokuEngine`:

| Schraube | Aktuell |
| --- | --- |
| Gittergröße | 4×4, wächst alle zwei Level, Maximum 9×9 |
| Zeit je Level | 60 s, +25 s je Größenstufe |
| Erlaubte Fehler | 3 |
| Startvorrat | 3× Feenstaub, 1× Natur-Schild, 2× Zeiten-Blüte |
| Punkte | 50 je selbst gesetzter Fee × Level, +5 je Restsekunde, +200 × Level |

## Technischer Stand

| | |
| --- | --- |
| minSdk | 26 (Android 8.0) |
| compileSdk / targetSdk | 35 |
| Kotlin | 2.0.21 · AGP 8.7.3 · Gradle 8.9 · JDK 21 |

**Offen vor einer Veröffentlichung:** Google Play verlangt fortlaufend neuere
`targetSdk`-Stände; ein Upgrade auf AGP 8.9+/`targetSdk 36` ist ein eigener
Schritt. Ebenfalls offen: Signierschlüssel für Release-Builds
(`keystore.properties` ist in `.gitignore` vorgesehen), Sound und Animationen.
