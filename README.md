# Fairydoku

Android-Spiel für den magischen Wald — natives Kotlin mit Jetpack Compose.

Du bist Hüter:in eines Feenreichs und setzt Feen auf ein moosbewachsenes
Zonen-Gitter, ohne dass sich ihre Zauberkräfte stören. Endlos-Modus: Mit jedem
Level wird der Wald dichter, und die Feen-Art wechselt.

Die Oberfläche folgt dem Design-Handoff „Fairydoku – Feen-Logikpuzzle"
(`Fairydoku Feen-Spiel Design.zip`).

## Die Regeln

- genau **eine Fee je Reihe**,
- genau **eine Fee je Spalte**,
- genau **eine Fee je Waldzone** (die farbig umrandeten Bereiche),
- **keine zwei Feen berühren sich** — auch nicht diagonal.

Ein Tipp auf ein Feld schaltet weiter: leer → ✕ (Merkzeichen „hier keine Fee")
→ 🧚 Fee → leer. Das Merkzeichen kommt zuerst, weil es der häufigere Zug ist.

Eine Fee, die beim Setzen kollidiert, kostet ein Leben. Drei Leben, dann ist der
Lauf vorbei — ebenso, wenn die Zeit abläuft.

## Die Magie-Fähigkeiten

| Fähigkeit | Wirkung |
| --- | --- |
| ✨ **Feenstaub** | Setzt eine Fee auf ein garantiert sicheres Feld; es leuchtet 2 s golden nach. |
| 🍃 **Natur-Schild** | Fängt den nächsten Fehler ab und verbraucht sich dabei. |
| 🌸 **Zeiten-Blüte** | Hält die Uhr 12 Sekunden lang an. |

Nach jedem Level kommen Feenstaub und Zeiten-Blüte dazu, der Natur-Schild nur
jedes zweite Level.

## Klang

| Ereignis | Klang | Herkunft |
| --- | --- | --- |
| Fee richtig gesetzt | Kichern in sechs Varianten | Aufnahme |
| Fee falsch gesetzt | erschrockener Aufschrei | Aufnahme |
| Merkzeichen / Rücknahme | trockener Tick / kurzes Abwärts-Wispern | berechnet |
| Fähigkeit eingesetzt | Funkenkaskade, Schild-Zweiklang, schwebender Ton | berechnet |
| Rätsel gelöst | Glockenjubel und eine lobende Feenstimme | berechnet + Sprachausgabe |
| Spielende | absteigende Molltonfolge | berechnet |
| Hintergrund | Ambient-Schleife aus vier Akkorden mit Glockentönen | berechnet |

**Die Feenstimmen sind echte Aufnahmen** (`res/raw/fairy_giggle_1..6.mp3`,
`fairy_startled.mp3`), abgespielt über `SoundPool` — es dekodiert MP3 selbst,
hält die Clips im Speicher und mischt mehrere gleichzeitig, sodass sich beim
schnellen Setzen mehrerer Feen die Stimmen überlagern statt abzuschneiden. Die
Originale liegen unter `Audio/`.

**Alles andere wird beim Start berechnet** (`audio/Synth.kt`,
`audio/FairySounds.kt`). Für Instrumente und Ambiente ist Synthese ideal: Sie
kostet keinen Speicherplatz und jede Tonhöhe ist über eine Zahl änderbar. Für
eine Stimme ist sie es nicht — deren Klangfarbe lässt sich aus Sinustönen nicht
überzeugend bauen, und genau deshalb sind Kichern und Aufschrei Aufnahmen.

Die **Lobstimme** nutzt die Sprachausgabe des Geräts, nicht aufgenommene Sprache:
Nur so kann das Lob den Spielstand nennen („Level 4 geschafft"). Fehlt eine
deutsche Stimme, bleibt sie still — das Spiel funktioniert auch ohne.

Musik, Klänge und Stimme lassen sich einzeln über die drei Schalter oben rechts
abstellen; die Einstellung wird gespeichert.

### Klänge anhören, ohne die App zu starten

```powershell
.\gradlew.bat testDebugUnitTest --tests "*SoundRenderTest*"
```

Schreibt alle **berechneten** Klänge als WAV nach `app/build/sounds/`. Der
schnellste Weg, eine Änderung an der Synthese zu beurteilen. Dieselben Tests
prüfen auch, dass kein Klang stumm ist, keiner übersteuert und die Musikschleife
ohne hörbaren Sprung schließt. Die aufgenommenen Feenstimmen sind davon nicht
betroffen — die liegen als MP3 vor und lassen sich direkt anhören.

## Die Feen

In jeder Waldzone lebt eine eigene Fee — auf einem Brett sind also bis zu acht
verschiedene gleichzeitig zu sehen, und die Zonen sind auf einen Blick
auseinanderzuhalten.

| | | | | |
| --- | --- | --- | --- | --- |
| Flora, die Waldfee | Nebula, die Staubfee | Salta, die Hüpffee | Aura, die Strahlfee | Nixie, die Frostfee |
| Zephyr, die Windfee | Ignis, die Funkenfee | Terra, die Kristallfee | Chrono, die Pendelfee | Trixie, die Chaosfee |

Zehn Feen, aber höchstens acht Zonen: Wäre die Zuordnung fest, blieben zwei Feen
für immer unsichtbar — auf den 4×4-Brettern der ersten Level sogar sechs. Deshalb
dreht sich der Reigen mit jedem Level um einen Platz weiter
(`GameState.speciesForZone`). Der Schritt ist teilerfremd zur Zahl der Feen, und
daran hängen beide Zusagen: Auf einem Brett trägt keine Zone dieselbe Fee wie eine
andere, und über zehn Level kommt in jeder Zone jede Fee genau einmal vor.

**Die Sprites stehen im Code**, nicht als Bilddateien: 32×32-Raster aus
Zeichenketten plus Farbpalette (`art/`). Das hält den Versionsverlauf lesbar und
macht jedes einzelne Pixel nachträglich änderbar.

Damit sich Fee und Zonenfarbe nicht beißen, tragen die zwei Ringe des Scheins
verschiedene Rollen: der **äußere Hof die Zonenfarbe** — an ihr hängt die
Lesbarkeit des Rätsels —, der **innere Kern den Eigenton der Fee**. Die Harmonie
entsteht durch Rahmung statt durch Übereinstimmung.

### Sprites anschauen, ohne die App zu starten

```powershell
.\gradlew.bat testDebugUnitTest --tests "*SpriteRenderTest*"
```

Schreibt nach `app/build/sprites/`: jede Fee einzeln, einen Filmstreifen ihrer
Einzelbilder, einen Kontaktbogen aller zehn und Brett-Ansichten der Level 1–3 mit
echten Rätseln. Dieselben Tests prüfen Rasterformat, Sichtbarkeit, Bewegung,
Randabstand und den Kontrast zum Moosgrund.

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
├── MainActivity.kt                  Einstiegspunkt; ein Bildschirm, keine Navigation
├── game/
│   ├── model/
│   │   ├── Puzzle.kt                Gitter, Zonen und die Regeln (FairydokuRules)
│   │   └── PuzzleGenerator.kt       Erzeugt Rätsel mit eindeutiger Lösung
│   ├── GameState.kt                 Unveränderlicher Partie-Zustand
│   ├── GameEngine.kt                FairydokuEngine: Züge, Fähigkeiten, Punkte
│   └── GameViewModel.kt             Spieluhr, Zustandsverwaltung, Persistenz
├── art/
│   ├── PixelSprite.kt               Sprite-Format, Palette, Prüfung
│   ├── FloraSprite.kt               Die Raster je Fee, eine Datei pro Charakter
│   └── FairySprites.kt              Zuordnung Fee → Gestalt und Eigenton
├── audio/
│   ├── Synth.kt                     Tonerzeugung (reines Kotlin, testbar)
│   ├── FairySounds.kt               Die Klänge des Waldes
│   ├── SoundEvent.kt                Welcher Spielzug wie klingt
│   ├── FairyVoice.kt                Lobstimme über die Sprachausgabe
│   └── FairyAudio.kt                Wiedergabe, Musikschleife, Schalter
├── data/
│   └── GamePreferences.kt           DataStore: Highscore, Partien, Tonschalter
└── ui/
    ├── GameCopy.kt                  Alle Texte und Zonennamen
    ├── theme/                       Design-Tokens, Schriften, Farbschema
    ├── components/
    │   ├── FairydokuBoard.kt        Brett: Moosfelder, Zonenränder, Feen
    │   ├── PowerUpBar.kt            Die drei Fähigkeiten
    │   ├── Overlays.kt              Willkommen, Level up, Spielende
    │   └── Fireflies.kt             Glühwürmchen-Schleier
    └── screens/GameScreen.kt        Setzt alles zusammen
```

### Vier Prinzipien, die das Projekt trägt

**Die Regeln kennt nur die Engine.** `FairydokuRules` und `FairydokuEngine` sind
reine Logik ohne Android-Abhängigkeiten: alter Zustand + Ereignis → neuer
Zustand. Deshalb laufen sie als schnelle JVM-Tests, und UI, ViewModel und
Persistenz bleiben von Regeländerungen unberührt.

**Jedes Rätsel hat genau eine Lösung.** Der Generator würfelt nicht nur, bis es
passt — er schließt alternative Lösungen gezielt aus, indem er einzelne Felder in
andere Zonen umhängt (`enforceUniqueness`). Ohne das hatte ab 7×7 praktisch kein
Brett mehr eine eindeutige Lösung, und der Feenstaub hätte Felder aufgedeckt, die
zu einer *anderen* Lösung gehören als der, die der Spieler gerade baut. Der
Prototyp aus dem Handoff garantiert nur „mindestens eine Lösung"; das ist die
eine Stelle, an der die Umsetzung bewusst über die Vorlage hinausgeht.

**Die Engine formuliert nicht.** Sie meldet, *was* geschehen ist
(`StatusMessage`), nicht wie es heißt. Die Texte stehen in `ui/GameCopy.kt` — so
lässt sich die Ansprache ändern oder übersetzen, ohne die Regeln anzufassen.

**Ein Bildschirm, alles andere sind Overlays.** So sieht es das Handoff vor;
deshalb gibt es keinen Navigations-Graphen.

## Design-Anschluss

| Was | Wo |
| --- | --- |
| Farben, Zonenpalette | `ui/theme/Color.kt` |
| Schriften | `res/font/`, gebunden in `ui/theme/Type.kt` |
| Texte, Zonennamen | `ui/GameCopy.kt` |
| Feen-Darstellung | `FairyGlyph` in `ui/components/FairydokuBoard.kt` |
| Launcher-Icon | `res/drawable/ic_launcher_foreground.xml` |

Material You / Dynamic Color ist bewusst **aus**, und es gibt nur ein dunkles
Schema: Der Nachtwald ist die Identität des Spiels, und die Lesbarkeit des
Bretts hängt an den Zonenfarben.

### Offen gegenüber dem Handoff

- **Feen-Sprites.** Die Feen sind Emoji-Platzhalter, wie im Prototyp. Das
  Handoff nennt vier Sprite-Sets als noch zu produzieren.
- **Feen-Arten unterscheiden sich nur am Schein.** Der Prototyp färbt das Emoji
  per CSS-`hue-rotate` um; ein Farbfilter auf Text ist in Compose erst ab API 31
  möglich. Bis die Sprites da sind, tragen die Arten deshalb dieselbe Figur mit
  artspezifischem Glow (rosa/blau/orange/gold).
- **Schriftbild des Titels.** Cinzel Decorative enthält nur Versalien, „Fairydoku"
  erscheint daher als „FAIRYDOKU".
- Hintergrund-Illustration fehlt noch.
- **Jubel und Fähigkeiten sind noch synthetisch.** Die Feenstimmen sind
  inzwischen Aufnahmen; wer auch den Rest aufgenommen haben will, ersetzt die
  entsprechenden Zweige in `FairyAudio` durch weitere Clips — die Schnittstelle
  dafür ist `SoundEvent` und bleibt gleich.

## Balance an einem Ort

Alle Stellschrauben stehen als Konstanten in `GameState.Companion` und
`FairydokuEngine` — die Werte stammen aus dem Handoff:

| Schraube | Aktuell |
| --- | --- |
| Gittergröße | 4×4, wächst alle zwei Level, Maximum 8×8 |
| Zeit je Level | 60 s + 15 s je Gitterfeld |
| Leben | 3 |
| Startvorrat | 3× Feenstaub, 1× Natur-Schild, 2× Zeiten-Blüte |
| Punkte je Level | 100 × Gittergröße + 5 je Restsekunde |
| Feen-Arten | Blüten → Wasser → Feuer → Sternen, dann von vorn |

## Technischer Stand

| | |
| --- | --- |
| minSdk | 26 (Android 8.0) — Voraussetzung für die Variable Font |
| compileSdk / targetSdk | 35 |
| Kotlin | 2.0.21 · AGP 8.7.3 · Gradle 8.9 · JDK 21 |

**Offen vor einer Veröffentlichung:** Google Play verlangt fortlaufend neuere
`targetSdk`-Stände; ein Upgrade auf AGP 8.9+/`targetSdk 36` ist ein eigener
Schritt. Ebenfalls offen: Signierschlüssel für Release-Builds
(`keystore.properties` ist in `.gitignore` vorgesehen).
