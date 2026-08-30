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

**Kurz tippen** setzt ✕ (Merkzeichen „hier keine Fee") und nimmt es wieder weg.
**Gedrückt halten** setzt die Fee — aus dem leeren Feld wie aus dem Merkzeichen;
ein kurzes Rütteln meldet, dass sie sitzt. Auf einer Fee räumen beide Gesten das
Feld.

Das Merkzeichen liegt auf der schnellsten Geste, weil es der weitaus häufigere
Zug ist: Beim Ausschließen arbeitet man sich durch viele Felder, bevor überhaupt
eine Fee gesetzt wird. Und weil das Brett keinen Doppeltipp kennt, erscheint das
✕ ohne jede Wartezeit — bei einem Doppeltipp müsste jeder einzelne Tipp erst
abwarten, ob noch einer folgt.

Eine Fee, die beim Setzen kollidiert, kostet einen von drei Versuchen. Sind alle
drei verbraucht, ist das Level verloren — und erst das kostet eines der fünf
Wald-Leben.

**Eine Spieluhr gibt es nicht.** Bis zum 28. August lief ein Countdown, dessen
Ablauf das Level ebenfalls beendete. Er ist ersatzlos gestrichen: Ein Rätsel,
das vom Nachdenken lebt, straft mit einer Uhr genau das ab, wofür es gemacht
ist. Geblieben sind die drei Versuche als einziges Ende.

## Die Hilfen

Zwei, und beide zeigen nur, was ohnehin beweisbar ist:

- **✨ Feenstaub** setzt eine Fee auf ein garantiert sicheres Feld, das zwei
  Sekunden golden nachleuchtet.
- **🔮 Irrlicht** deckt umgekehrt ein Feld auf, auf dem keine Fee sitzt.

Beide Vorräte fassen **drei** Stück und gehören dem Spieler, nicht dem Level —
sie gehen in das nächste Level mit und überleben den Neustart der App. Ein
verbrauchtes Stück wächst in **zwei Stunden** nach; ist der Vorrat voll, ruht
das Nachwachsen. Es läuft über einen gespeicherten Zeitpunkt und holt deshalb
auch nach, was während geschlossener App fällig geworden wäre
(`RegeneratingSupply`, geteilt mit den Wald-Leben).

Früher standen hier drei Fähigkeiten. Der **Natur-Schild** nahm dem Fehler die
Folge und die **Zeiten-Blüte** dem Countdown den Druck; zusammen machten sie das
Rätsel beliebig. Die Zeiten-Blüte hat sich mit dem Countdown ohnehin erledigt.

## Klang

| Ereignis | Klang | Herkunft |
| --- | --- | --- |
| Fee richtig gesetzt | ein Kichern, sechs verschiedene, gewürfelt | Aufnahme |
| Fee falsch gesetzt | erschrockener Aufschrei | Aufnahme |
| Merkzeichen | ein Glockenspiel-Ton in A, 900 ms | Aufnahme · eigens erzeugt |
| Rücknahme — Fee **und** Merkzeichen | ein hohes Nachklingen, 350 ms | Aufnahme · 13,0 s |
| Rätsel gelöst | der Höhepunkt, 2,5 s | Aufnahme · 3,2 s |
| Nächstes Level beginnt | nichts — die Waldmusik läuft weiter | — |
| Spielende | absteigende Molltonfolge | berechnet |
| Hintergrund | ruhige Waldschleife, über Brett und Karte dieselbe | Aufnahme |

Die drei mit einer Zeitangabe stammen aus **einem einzigen zwanzig Sekunden
langen Stück** („Neues Level im Feenwald", `Audio/`); die Zahl sagt, an welcher
Sekunde der Ausschnitt beginnt. Vorher war der Jubel gerechnet, in C-Dur aus der
eigenen Pentatonik, und stand ohne Zusammenhang neben den Aufnahmen — beim
Spielen fiel genau das auf: „alles ein bisschen auseinandergerissen, keine
Konstante in der Musik".

**Der Levelbeginn ist stumm.** Er hatte am 29. und 30. August kurz einen eigenen
Klang, erst gerechnet, dann als Ausschnitt. Beide sind wieder weg: Die Waldmusik
läuft ohnehin durch, und ein Anfang braucht keine Ansage. Nur das Gewinnen
bekommt eine — und dafür tritt die Musik dann auch beiseite.

**Zwölf Aufnahmen liegen bei** (`res/raw/`) — die Waldschleife, der
Schreckenslaut, die sechs Kicherlaute und die drei Ausschnitte aus dem
Levelstück. Alle mit ElevenLabs
unter bezahltem Tarif erzeugt; die Originale samt Zeitstempel liegen unter
`Audio/`. Die kurzen laufen über `SoundPool`, die Waldschleife über
`MusicDecoder` — der packt die MP3 einmal nach rohem PCM aus, weil MP3 am
Schleifenende sonst eine hörbare Naht setzt.

Die sechs Kicherlaute sind **auf einen gemeinsamen Pegel gebracht** (rund
−26 dB RMS). Im Original lagen zwischen dem leisesten und dem lautesten
vierzehn Dezibel; roh eingebaut hätte jeder zweite Zug erschreckt. Eine
Zuordnung zur Feenart gibt es nicht — es wird gewürfelt.

Feenstaub und Irrlicht haben **keinen eigenen Klang**. Sie sind keine eigenen
Ereignisse, sondern eine andere Art, denselben Zug zu tun: Der Feenstaub setzt
eine Fee, also kichert sie; das Irrlicht setzt ein ✕, also tickt es. Ein eigener
Klang ließe die Hilfe nach Belohnung klingen statt nach Zug. Bis zum 30. August
gab es für den Feenstaub eine Funkenkaskade — sie steht noch in `FairySounds`,
wird aber nicht mehr eingehängt.

**An zwei Stellen tritt die Musik beiseite**: unter dem Jubel und unter dem
Levelbeginn. Beide sagen etwas — „geschafft", „ein neuer Wald" —, und darunter
lief bis zum 30. August unbeirrt die Fläche weiter, die man ohnehin die ganze
Zeit hört. Weggeblendet statt angehalten, schnell hinein und langsam wieder
heraus (180 ms gegen 700), damit nichts knackt und die Rückkehr nicht wie ein
Einschalten wirkt.

**Alles andere wird beim Start berechnet** (`audio/Synth.kt`,
`audio/FairySounds.kt`, `audio/FairyChimes.kt`). Für Instrumente ist Synthese
ideal: Sie kostet keinen Speicherplatz, und jede Tonhöhe ist über eine Zahl
änderbar. Beim Setzen einer Fee ist sie sogar das bessere Mittel — die zehn
Eigentöne stammen aus einer Tonleiter, es kann also nichts schief klingen, egal
wie schnell gesetzt wird.

Beim Setzen einer Fee ist es trotzdem wieder eine Aufnahme. Die Reihenfolge
dieser Kehrtwenden gehört zur Geschichte: Kichern (bis 5. August) → berechneter
Eigenton je Fee (bis 29. August) → wieder Kichern. Den Ausschlag gab jedes Mal
das Spielen, nicht die Theorie. [FairyChimes] bleibt samt Tests im Projekt,
falls die dritte Runde zurückführt.

*Beide Aufnahmen waren im August einmal draußen — es war unklar, wie ihre
Rechtelage zu belegen wäre. Der Tarif ist inzwischen belegt
(`storepaket/pruefbericht.md`), und die Testrunde wollte sie ohnehin zurück.*

**Gesprochen wird nichts mehr.** Bis zum 29. August folgte dem Jubel ein
Lobsatz aus der Sprachausgabe des Geräts — „Level 4 geschafft!", eine knappe
Sekunde nach dem Jubel und selbst zwei lang. Beim Weiterspielen war er im Weg,
und wer mehrere Level hintereinander schafft, hört ihn immer wieder. Mit ihm ist
`FairyVoice.kt` verschwunden und damit die einzige Stelle, an der die App eine
Systemkomponente ansprach, die auf jedem Gerät anders klingt oder ganz fehlt.

Der Regler heißt weiter **Feenstimme** und regelt, wie laut die Fee beim Setzen
kichert. Er sitzt bewusst nicht auf dem Klang-Regler: Der Laut ertönt bei jedem
Zug und ist damit das, was man am ehesten leiser haben will, ohne Tick und Jubel
mit zu dämpfen.

### Lautstärke

Das Lautsprecher-Zeichen oben rechts öffnet die Klang-Einstellungen: **Musik,
Klänge und Feenstimme sind getrennt regelbar**. Ein
Regler auf null ist zugleich der Stummschalter — dafür braucht es keinen
zweiten Bedienweg, und das Zeichen am Rand zeigt an, wenn alles stumm ist.

Musik und Klänge getrennt zu regeln hat einen Grund: Die Musik läuft
ununterbrochen und stört beim Nachdenken schneller, die Klänge sind Rückmeldung
auf eigene Züge und dürfen lauter bleiben. Deshalb ist die Musik auch
voreingestellt leiser (70 % gegen 90 %).

Neben jedem Regler steht seit dem 30. August ein **Lautsprecher-Schalter**. Der
Regler auf null war zwar immer schon der Stummschalter, aber man musste ihn
dorthin *ziehen* — und wer nur schnell die Musik ausmachen will, während er
nachdenkt, will nicht zielen müssen. Zurück führt derselbe Schalter, und zwar
auf die zuletzt eingestellte Lautstärke: Wer bei 30 % hörte, hört danach wieder
30 % und nicht plötzlich 70.

Die Einstellung wird gespeichert. Wer früher die alten Ein/Aus-Schalter benutzt
hat, dessen „aus" wird beim ersten Start als Lautstärke null übernommen.

### Klänge anhören, ohne die App zu starten

```powershell
.\gradlew.bat testDebugUnitTest --tests "*SoundRenderTest*"
```

Schreibt alle **berechneten** Klänge als WAV nach `app/build/sounds/`. Der
schnellste Weg, eine Änderung an der Synthese zu beurteilen. Dieselben Tests
prüfen auch, dass kein Klang stumm ist, keiner übersteuert und die Musikschleife
ohne hörbaren Sprung schließt. Die beiden Aufnahmen sind davon nicht betroffen —
die liegen als MP3 vor und lassen sich direkt anhören.

## Das Spielbrett

Das Gitter liegt auf einer **Moos-Matte** und besteht aus Steinplatten mit
Moosflecken. Jede Platte ist mit der Farbe ihrer Zone leicht eingefärbt — nur zu
einem Zehntel: Kenntlich wird eine Zone über ihre **leuchtenden Ränder**, nicht
über die Fläche. Auf voll eingefärbten Flächen stünden acht Neontöne
gleichzeitig im Bild und nähmen den Feen die Aufmerksamkeit.

Wo zwei Zonengrenzen aufeinandertreffen, ist die Ecke rund. Dadurch erscheint
eine Zone als zusammenhängender, abgerundeter Block statt als Ansammlung
quadratischer Felder — genau die Wirkung aus der Vorlage.

Die Markierung „hier sitzt keine Fee" ist ein goldgelbes ✕ mit Gold-Glow. Der
Schein ist nicht nur Zierrat: Auf der graugrünen Steinplatte wirkte ein flaches
Gelb blass.

## Die Feen

In jeder Waldzone lebt eine eigene Fee — auf einem Brett sind also bis zu acht
verschiedene gleichzeitig zu sehen, und die Zonen sind auf einen Blick
auseinanderzuhalten.

| | | | | |
| --- | --- | --- | --- | --- |
| Viridis, die Waldfee | Nebula, die Staubfee | Salta, die Hüpffee | Aura, die Strahlfee | Nixie, die Frostfee |
| Zephyr, die Windfee | Ignis, die Funkenfee | Terra, die Kristallfee | Chrono, die Pendelfee | Trixie, die Chaosfee |

Zehn Feen, aber höchstens acht Zonen: Wäre die Zuordnung fest, blieben zwei Feen
für immer unsichtbar — auf den 4×4-Brettern der ersten Level sogar sechs. Deshalb
dreht sich der Reigen mit jedem Level um einen Platz weiter
(`GameState.speciesForZone`). Der Schritt ist teilerfremd zur Zahl der Feen, und
daran hängen beide Zusagen: Auf einem Brett trägt keine Zone dieselbe Fee wie eine
andere, und über zehn Level kommt in jeder Zone jede Fee genau einmal vor.

**Die Bilder** liegen als freigestellte Pixel-Art in `res/drawable-nodpi/`, alle
in einem 256×256-Feld auf gemeinsamer Grundlinie — dadurch stehen die Figuren
auf dem Brett gleich hoch, obwohl die Vorlagen unterschiedlich groß waren. Die
Vorlage mit allen zehn liegt unter `Bilder/feen.jpg`.

Freigestellt wurden sie durch eine Flutung des Hintergrunds vom Bildrand her,
nicht durch einen Weiß-Schwellwert: Nur so bleiben helle Flächen *innerhalb*
einer Figur erhalten — Auras weißes Kleid, Nixies Haar, Trixies karierter
Flügel. Jede Figur wird zudem einzeln vom eigenen Saatpunkt aus eingesammelt,
weil sich die Vorlagen im Sammelbild überlappen.

Damit sich Fee und Zonenfarbe nicht beißen, tragen die zwei Ringe des Scheins
verschiedene Rollen: der **äußere Hof die Zonenfarbe** — an ihr hängt die
Lesbarkeit des Rätsels —, der **innere Kern den Eigenton der Fee**. Die Harmonie
entsteht durch Rahmung statt durch Übereinstimmung.

Tests halten fest, dass zu jeder Fee ein Bild vorliegt, keine verwaisten Bilder
herumliegen und jede Fee einen eigenen Schein hat — ein fehlendes Bild fiele
sonst erst im laufenden Spiel als leeres Feld auf.

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
│   ├── GameEngine.kt                FairydokuEngine: Züge, Feenstaub, Punkte
│   └── GameViewModel.kt             Zustandsverwaltung, Persistenz, Klangausgabe
├── art/
│   └── FairySprites.kt              Der Eigenton jeder Fee (Android-frei)
├── audio/
│   ├── Synth.kt                     Tonerzeugung (reines Kotlin, testbar)
│   ├── FairySounds.kt               Die Klänge des Waldes
│   ├── SoundEvent.kt                Welcher Spielzug wie klingt
│   └── FairyAudio.kt                Wiedergabe, Musikschleife, Schalter
├── data/
│   └── GamePreferences.kt           DataStore: Highscore, Partien, Tonschalter
└── ui/
    ├── GameCopy.kt                  Alle Texte der Oberfläche
    ├── theme/                       Design-Tokens, Schriften, Farbschema
    ├── sprites/
    │   ├── FairyArt.kt              Zuordnung Fee → Bildressource
    │   └── FairySpriteCache.kt      Bilder einmal laden und behalten
    ├── components/
    │   ├── SoundSettingsOverlay.kt  Regler für Musik, Klänge und Stimme
    │   ├── FairydokuBoard.kt        Brett: Steinplatten, Zonenränder, Feen
    │   ├── PowerUpBar.kt            Der Feenstaub-Knopf
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

- **Schriftbild des Titels.** Cinzel Decorative enthält nur Versalien, „Fairydoku"
  erscheint daher als „FAIRYDOKU".
- **Die Feen bewegen sich nur als Ganzes** (Schweben, Erscheinen). Für
  Leerlauf-Animationen — Nebulas rieselnder Staub, Ignis' züngelnde Flügel,
  Trixies Würfel — bräuchte es je Fee ein zweites Bild; die Zeichenschicht ist
  darauf vorbereitet.
- Die beiden Emoji in der Titelzeile und im „Level up"-Overlay sind noch
  Platzhalter.
- Hintergrund-Illustration fehlt noch.
- **Jubel und Effektklänge sind synthetisch.** Waldmusik und Schreckenslaut
  sind Aufnahmen; wer auch den Rest aufgenommen haben will, ersetzt die
  entsprechenden Zweige in `FairyAudio` durch weitere Clips — die Schnittstelle
  dafür ist `SoundEvent` und bleibt gleich.

## Balance an einem Ort

Alle Stellschrauben stehen als Konstanten in `GameState.Companion` und
`FairydokuEngine` — die Werte stammen aus dem Handoff:

| Schraube | Aktuell |
| --- | --- |
| Gittergröße | 4×4, wächst alle zwei Level, Maximum 8×8 ab Level 9 |
| Versuche je Level | 3 |
| Wald-Leben | 5, nachwachsend alle zwei Stunden |
| Feenstaub, Irrlicht | je 3 Stück, nachwachsend alle zwei Stunden |
| Punkte je Level | 175 × Gittergröße + 300 |
| Feen-Arten | zehn, je Zone eine andere; über zehn Level jede einmal je Zone |

Zu den Punkten: In der Formel steht noch `POINTS_PER_CELL × Größe +
Restsekunden × POINTS_PER_SECOND`. Seit die Spieluhr weg ist, zählt
`remainingMillis` nicht mehr herunter, der Zeitanteil ist also für jede
Gittergröße derselbe — herausgerechnet ergibt das die Zeile oben. Die Formel
umzuschreiben würde am Ergebnis nichts ändern und wartet deshalb auf einen
ruhigeren Zeitpunkt als die Woche vor der Veröffentlichung.

## Technischer Stand

| | |
| --- | --- |
| minSdk | 26 (Android 8.0) — Voraussetzung für die Variable Font |
| compileSdk / targetSdk | 36 |
| Kotlin | 2.0.21 · AGP 8.13.2 · Gradle 8.13 · JDK 17 |

`targetSdk 36` ist keine Kür: Google Play verlangt ihn ab dem **31. August
2026** für neue Apps. Damit gilt auch, dass Android 16 auf großen Bildschirmen
die Festlegung auf Hochformat nicht mehr beachtet — deshalb steht das Brett in
einer Box, die nie höher wird als der Platz, der ihr bleibt.

**Offen vor einer Veröffentlichung:** siehe `STAND.md` — nichts davon ist
Programmierarbeit.
