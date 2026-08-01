# Fairydoku — Kontext für Grafik- und Sound-KIs

Diese Datei ist zum Kopieren gedacht. Stell **Teil 1** jedem Auftrag voran und
häng den passenden Abschnitt aus Teil 2 oder 3 an.

---

## Teil 1 — Der Kern (immer voranstellen)

> **Fairydoku** ist ein Logik-Puzzlespiel für Android im Feen-Thema, eine
> Mischung aus Sudoku und dem „Queens"-Puzzle.
>
> Der Spieler ist Hüter:in eines magischen Nachtwaldes. Auf einem moosbewachsenen
> Steingitter, das in leuchtend umrandete Zonen unterteilt ist, platziert er
> Feen: genau eine je Reihe, je Spalte und je Zone — und keine zwei dürfen sich
> berühren, auch nicht diagonal, sonst stören sich ihre Zauberkräfte.
>
> **Stimmung:** biolumineszenter Nachtwald, tiefblau-violett, warm und
> freundlich statt bedrohlich. Glühwürmchen, leuchtende Pilze, goldener Schimmer.
> Verspielt und niedlich, für ein Casual-Publikum jeden Alters — nicht düster,
> nicht kitschig-barock.
>
> **Grundfarben:** Hintergrund Tiefblau `#0A0E21` bis Violett `#1B1440`.
> Leitfarbe ist Gold (`#FFD76B`, hell `#FFE9A8`). Akzente: Blattgrün `#7DFF9E`,
> Blütenrosa `#FF9ECF`. Das Spielbrett besteht aus moosigen Steinen in
> Dunkelgrün (`#4A5D3F`, `#35452E`, `#55684A`, `#3B4C33`).

---

## Teil 2 — Grafik

### 2a) Feen-Sprites (das Wichtigste)

> Erzeuge ein **Pixel-Art-Sprite, 32×32 Pixel**, für ein Handyspiel.
>
> **Technische Vorgaben, zwingend:**
> - exakt 32×32 Pixel, keine Zwischentöne durch Weichzeichnen, keine
>   Kantenglättung — harte Pixelkanten
> - transparenter Hintergrund (PNG mit Alpha)
> - mindestens 2 Pixel Abstand zu allen vier Rändern
> - begrenzte Palette: höchstens 12 Farben, davon eine dunkle Kontur
> - die Figur muss vor **dunkelgrünem Moos** (`#3B4C33`) klar lesbar sein — also
>   helle Flächen und kräftige Kontur, keine dunkelgrünen Hauptfarben
> - Frontalansicht, stehend/schwebend, symmetrisch aufgebaut
> - die Figur füllt etwa 26 der 32 Pixel Höhe
>
> **Gemeinsame Silhouette aller Feen** (damit sie als ein Set wirken): schmaler
> Körper, runder Kopf mit sichtbaren Augen, zwei Fühler nach oben, ein
> Flügelpaar links und rechts, zwei kurze Beine.
>
> **Zusätzlich brauche ich 2 Einzelbilder** derselben Figur für eine
> Leerlauf-Animation: einmal mit weit geöffneten Flügeln, einmal mit angelegten.

**Die zehn Charaktere** (jeweils an den Block oben anhängen):

| Fee | Beschreibung für den Prompt |
| --- | --- |
| **Flora**, die Waldfee | Große orange-gelbe Monarchenfalter-Flügel, grünes Blätterkleid, braunes Haar mit Fühlern, hält einen kleinen Blätterzweig. |
| **Nebula**, die Staubfee | Violette Nachtfalter-Flügel mit winzigen weißen Sternenpunkten, dunkelblaues Stoffkleid, hält eine gläserne Fiole, aus der rosa Glitzerpunkte rieseln. |
| **Salta**, die Hüpffee | Kurze, runde Bienenflügel steil nach oben, hellgelbes Kleidchen, freche hochgebundene Zöpfe, angewinkelte Arme wie vor einem Sprung. |
| **Aura**, die Strahlfee | Vier spitze Libellenflügel in gleißendem Weiß-Blau, fast durchsichtig, reinweißes Kleid mit goldenen Rändern, hält einen leuchtenden Stern vor der Brust. |
| **Nixie**, die Frostfee | Eckige, kristalline Flügel wie geschliffene Eisschollen in Hellblau und Cyan, schneeweißes Haar, eisblaues Kleid, kleine weiße Atemwölkchen. |
| **Zephyr**, die Windfee | Sehr schmale, lange Flügel in zartem Mintgrün, langes Haar waagerecht nach hinten wehend, Kleidung aus wirbelnden hellblauen Stoffbändern. |
| **Ignis**, die Funkenfee | Zackige Flügel wie züngelnde Flammen in Feuerrot und Orange, kurzes stachliges feuerrotes Haar, Kleid aus aschgrauen Blättern mit glühenden Rändern. |
| **Terra**, die Kristallfee | Breite, schwere Flügel aus smaragdgrünen Edelsteinfacetten, erdbraunes Kleid mit Rindenstruktur, kleine Krone aus Rohkristallen. Steht fest am Boden, schwebt nicht. |
| **Chrono**, die Pendelfee | Flügel in Form geschwungener goldener Uhrzeiger, elegantes lila Kleid, kleine Taschenuhr an der Hüfte, streng hochgestecktes Haar. |
| **Trixie**, die Chaosfee | Bewusst asymmetrisch: linker Flügel rosa kariert, rechter neongrün gepunktet, zweifarbige Narrenkappe, jongliert mit zwei bunten Würfeln. |

**Englische Fassung** (viele Bild-KIs arbeiten damit zuverlässiger):

> 32x32 pixel art sprite, front view, transparent background, hard pixel edges,
> no anti-aliasing, limited palette of max 12 colors with dark outline, cute
> fairy character for a mobile puzzle game, readable against dark green moss
> background, 2px margin on all sides, slender body, round head with visible
> eyes, two antennae, one pair of wings, short legs.
> Character: *[Beschreibung aus der Tabelle einsetzen]*

### 2b) Hintergrund-Illustration

> Hochformat-Hintergrund für ein Handyspiel (Seitenverhältnis 9:19,5),
> biolumineszenter Nachtwald bei Nacht. Tiefblau-violette Farbtöne (`#0D1330`
> oben, `#1B1440` unten), leuchtende Pilze in Rosa und Cyan am unteren Rand,
> schwebende Glühwürmchen, weiches Mondlicht von oben.
> **Wichtig:** Die Bildmitte muss ruhig und dunkel bleiben — dort liegt das
> Spielbrett. Keine Figuren, kein Text, keine harten Kontraste in der Mitte.

### 2c) Symbole der drei Fähigkeiten

> Drei Symbole im selben Stil, je 64×64 Pixel, Pixel-Art, transparenter
> Hintergrund, für runde Schaltflächen auf dunkelblauem Grund:
> 1. **Feenstaub** — ein Glasfläschchen mit leuchtend goldenem Staub
> 2. **Natur-Schild** — ein leuchtendes grünes Blatt als Schutzschild
> 3. **Zeiten-Blüte** — eine rosa Blüte, deren Blütenblätter wie ein Zifferblatt
>    angeordnet sind

---

## Teil 3 — Sound

### 3a) Feenstimmen (die wichtigsten Klänge)

> Kurze Stimmaufnahmen einer **winzigen Fee** — hell, hoch, zart, freundlich.
> Wie ein sehr kleines Kind, aber luftiger und mit einem Hauch Glitzern.
> Mono, sauber geschnitten, ohne Hall-Fahne, jeweils **unter 1 Sekunde**.
>
> - **Kichern** (6 Varianten, damit es sich beim Spielen nicht wiederholt): ein
>   kurzes, perlendes „hi-hi-hi", zufrieden, verspielt. Wird abgespielt, wenn
>   der Spieler eine Fee richtig setzt.
> - **Erschrecken** (1 Aufnahme): ein kurzer, hoher Aufschrei, überrascht statt
>   ängstlich — die Fee wurde falsch platziert und ihre Zauberkräfte kollidieren.
>   Soll nicht wehtun, eher „hoppla!".

### 3b) Musik

> Ambient-Schleife für ein ruhiges Puzzlespiel im Feenwald, **nahtlos
> loopbar**, 30–60 Sekunden.
> Sehr zurückhaltend, keine Melodie im Vordergrund — der Spieler soll denken
> können. Weiche Flächenklänge, dazu vereinzelte Glockentöne und Harfenzupfer in
> einer pentatonischen Skala. Langsam, schwebend, warm.
> Stimmung: nächtlicher Wald, Glühwürmchen, Geborgenheit. Kein Beat, kein
> Schlagzeug, keine Spannung, kein Crescendo.

### 3c) Effektklänge

> Kurze Klänge für ein Feen-Puzzlespiel, hell und magisch, jeweils ohne langen
> Nachhall:
> - **Jubel** (1,5 s): aufsteigende Glockenfigur in Dur, endet auf der Oktave —
>   „Rätsel gelöst"
> - **Merkzeichen** (0,1 s): trockener, leiser Tick — der Spieler markiert ein
>   Feld als leer
> - **Rücknahme** (0,3 s): kurzes abfallendes Wispern
> - **Feenstaub** (0,5 s): aufsteigende Glitzerkaskade, wie rieselnder Sternenstaub
> - **Natur-Schild** (1 s): warmer, sich öffnender Zweiklang, beschützend
> - **Zeiten-Blüte** (1,4 s): schwebender, langsam pulsierender Ton — die Zeit
>   friert ein
> - **Spielende** (1,5 s): absteigende Molltonfolge, wehmütig statt dramatisch

---

## Was nicht passt (für Negativ-Prompts)

**Grafik:** fotorealistisch, 3D-Render, Aquarell, weichgezeichnet, Anti-Aliasing
bei Sprites, düster, gruselig, Horror, Anime-Fanservice, erwachsene Proportionen,
Text im Bild, Wasserzeichen, mehr als 32×32 Pixel bei Sprites.

**Sound:** tiefe Männerstimmen, Erwachsenenstimmen, Lachen mit Halloffnung,
Orchester-Pathos, Schlagzeug, elektronischer Beat, Dubstep, schrille oder
dissonante Klänge, alles über 2 Sekunden bei Effekten.

---

## Technische Eckdaten (falls die KI danach fragt)

| | |
| --- | --- |
| Plattform | Android, Hochformat, ein Bildschirm |
| Spielfeld | 4×4 bis 8×8 Felder, Brettbreite 352 dp |
| Zellgröße | 88 dp bei 4×4, 44 dp bei 8×8 |
| Sprite-Auflösung | 32×32 Pixel, ganzzahlig vergrößert |
| Schriften | Cinzel Decorative (Titel), Quicksand (Oberfläche) |
| Zonenfarben | `#FF6B8A` `#5BC8FF` `#7DFF9E` `#FFD76B` `#C58BFF` `#FF9A5B` `#6BFFF2` `#FF9ECF` |
| Audioformat | MP3 oder WAV, Mono, 44,1 kHz |
