# Fairy Doo

Android-Spiel (Casual/Puzzle) — natives Kotlin mit Jetpack Compose.

Der Stand ist ein **lauffähiges Gerüst**: Es startet, navigiert, spielt eine
Platzhalter-Runde und speichert den Highscore. Die eigentliche Puzzle-Mechanik
und das finale Design kommen noch.

## Bauen und starten

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

.\gradlew.bat assembleDebug        # APK bauen
.\gradlew.bat testDebugUnitTest    # Unit-Tests
.\gradlew.bat installDebug         # auf verbundenes Gerät/Emulator
```

Die Debug-APK liegt unter `app/build/outputs/apk/debug/`.

## Aufbau

```
app/src/main/java/com/fairydoo/game/
├── MainActivity.kt              Einstiegspunkt, baut Theme + Navigation auf
├── game/
│   ├── GameState.kt             Unveränderlicher Partie-Zustand
│   ├── GameEngine.kt            Die Spielregeln (aktuell PlaceholderEngine)
│   └── GameViewModel.kt         Spieluhr, Zustandsverwaltung, Persistenz
├── data/
│   └── GamePreferences.kt       DataStore: Highscore, Partien, Einstellungen
└── ui/
    ├── theme/                   Farben, Typografie, Material-Theme
    ├── navigation/              Alle Ziele an einer Stelle
    ├── components/GameBoard.kt  Canvas-Zeichenschicht des Spielfelds
    └── screens/                 Home, Game, Settings
```

### Zwei Prinzipien, die das Gerüst trägt

**Die Engine kennt die Regeln, sonst niemand.** `GameEngine` ist reine Logik
ohne Android-Abhängigkeiten: alter Zustand + Ereignis → neuer Zustand. Dadurch
ist sie mit schnellen JVM-Tests prüfbar, und die echte Mechanik lässt sich
einsetzen, ohne UI, ViewModel oder Persistenz anzufassen.

**Screens holen Daten, Contents stellen dar.** Jeder Screen ist in ein
`XyzScreen` (Datenzugriff) und ein privates `XyzContent` (reine Darstellung)
geteilt. Nur deshalb funktionieren die `@Preview`-Funktionen ohne laufende App.

## Wenn das Design kommt

Der Anschluss ist auf drei Dateien begrenzt:

| Was | Wo |
| --- | --- |
| Farben | `ui/theme/Color.kt` — Werte ersetzen, `Theme.kt` ordnet sie zu |
| Schrift | TTF/OTF nach `res/font/`, dann `GameFontFamily` in `ui/theme/Type.kt` |
| Größen/Abstände | direkt in den Screens (`.dp`-Werte) |
| Launcher-Icon | `res/drawable/ic_launcher_foreground.xml` + `res/values/colors.xml` |

Material You / Dynamic Color ist bewusst **aus**: Das Spiel soll auf jedem Gerät
gleich aussehen und nicht die Systemfarben des Nutzers übernehmen
(`ui/theme/Theme.kt`).

## Wenn die Spielmechanik kommt

1. `GameInput` um die echten Ereignisse erweitern (`Swipe`, `SelectTile`, …).
2. Neue `GameEngine`-Implementierung schreiben; `PlaceholderEngine` ersetzen.
3. `GameState` um Spielfelddaten erweitern.
4. `GameBoard` zeichnet den neuen Zustand.
5. Tests in `app/src/test/.../PlaceholderEngineTest.kt` mitziehen.

ViewModel, Navigation, Persistenz und Theme bleiben dabei unverändert.

## Technischer Stand

| | |
| --- | --- |
| minSdk | 26 (Android 8.0) |
| compileSdk / targetSdk | 35 |
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.7.3 |
| Gradle | 8.9 |
| JDK | 21 (Android Studio JBR) |

**Offen vor einer Veröffentlichung:** Google Play verlangt fortlaufend neuere
`targetSdk`-Stände. Ein Upgrade auf AGP 8.9+/`targetSdk 36` ist ein eigener,
bewusst separater Schritt — das Gerüst steht bewusst zuerst auf einer hier
verifizierten Kombination.

Ebenfalls offen: Signierschlüssel für Release-Builds (`keystore.properties` ist
in `.gitignore` vorgesehen), App-Icon im finalen Design, Sound.
