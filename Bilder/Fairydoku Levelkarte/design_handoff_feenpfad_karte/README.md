# Handoff: Feenpfad – Levelkarte mit Parallax-Waldszene

## Overview
Die Levelauswahl von Fairydoku ("Der Feenpfad"): eine vertikal scrollbare Karte, auf der die Level als
runde Steine entlang eines gepunkteten Pfades liegen. Hinter dem Pfad liegt eine mehrschichtige
Waldszene, die beim Scrollen mit unterschiedlichen Geschwindigkeiten mitläuft (Parallaxe) und dadurch
Tiefe erzeugt. Diese Übergabe beschreibt ausschließlich die Karte samt Szenerie – nicht das Puzzle-Board.

## About the Design Files
Die Datei in diesem Bundle ist eine **Design-Referenz in HTML** – ein Prototyp, der Aussehen und
Verhalten zeigt, **kein produktionsreifer Code zum Kopieren**. Aufgabe ist, dieses Design in der
bestehenden Umgebung des Zielprojekts nachzubauen (React, Vue, SwiftUI, Flutter, native …) mit den dort
etablierten Patterns und Libraries. Existiert noch keine Umgebung, das für das Projekt am besten
geeignete Framework wählen und die Designs dort umsetzen.

Im Prototyp sind alle Ebenen absolut positionierte DOM-Elemente mit Inline-Styles; die Silhouetten
(Tannen, Pilze, Steine) sind Inline-SVG-Pfade. In einer Engine/Native-Umgebung sind dieselben Ebenen
sinnvoller als Sprites/Shapes mit Scroll-Offset-Faktor umzusetzen.

## Fidelity
**High-fidelity.** Farben, Größen, Kurven, Deckkräfte und Parallaxe-Faktoren sind final und exakt
dokumentiert. Die Szene sollte pixelnah nachgebaut werden.

## Screens / Views

### Screen: Levelkarte "Der Feenpfad"
**Purpose:** Level auswählen; Fortschritt und noch gesperrte Level erkennen.

**Layout (Prototyp-Maße, Design-Breite 430 px Viewport):**
- Kopfbereich (nicht Teil dieser Übergabe): Herzen, SCORE-Chip, Icons ❔ 🏆, Titel "✦ Der Feenpfad ✦",
  Hinweiszeile.
- Rahmen um die Karte: padding 10 px, border-radius 26 px, border 2 px rgba(140,190,90,.4),
  Hintergrund: radial 25%/20% #4d6338 + radial 80%/75% #3c5230 + linear 160° #42562f → #2c3d20 60% → #22301a,
  box-shadow: 0 12px 36px rgba(0,0,0,.6), 0 0 40px rgba(110,170,80,.18),
  inset 0 2px 8px rgba(210,255,160,.18), inset 0 -6px 14px rgba(0,0,0,.4).
- **Scroll-Viewport:** width 374 px, height 470 px, overflow-y auto, border-radius 18 px,
  box-shadow inset 0 0 34px rgba(0,0,0,.8).
- **Inner-Canvas:** position relative, width 374 px, height = maxN·92 + 80 px, overflow hidden.
  maxN = max(freigeschaltetes Level + 3, 12).

**Pfad & Levelsteine:**
- Knotenposition i (1-basiert): x = round(187 + 118·sin(i·1.05)), y = 50 + (i−1)·92.
- Pfad: zwei SVG-Pfade über dieselben Punkte (Polyline, `L`-Segmente):
  1. stroke rgba(255,233,168,.35), width 5, linecap round, dasharray "1 14" (Punktkette)
  2. stroke rgba(255,233,168,.12), width 11, linecap round (weicher Schein darunter)
- Levelstein: 54×54 px, border-radius 50 %, zentrierter Text, font-family Quicksand, font-weight 700.
  - **Erledigt:** Steintextur (siehe unten), border 3 px color-mix(in oklab, ZONENFARBE 80%, white),
    color #fff2c9, text-shadow 0 0 8px ZONENFARBE + 0 1px 2px rgba(0,0,0,.7),
    box-shadow 0 0 16px ZONENFARBE, inset 0 0 12px ZONENFARBE, 0 4px 10px rgba(0,0,0,.5), font-size 19 px.
  - **Aktuell:** Steintextur, border 3 px #fffbe8, box-shadow 0 0 24px rgba(255,215,107,.95),
    inset 0 0 14px rgba(255,215,107,.8), 0 4px 10px rgba(0,0,0,.5), Animation `glowPulse` 1.6 s infinite.
  - **Gesperrt:** radial-gradient(circle at 45% 45%, #313b34, #222a25 78%), border 2 px rgba(200,220,200,.18),
    color rgba(255,255,255,.4), inset 0 2px 5px rgba(0,0,0,.5), Label 🔒, font-size 17 px, cursor default.
  - Steintextur: radial(circle at 32% 28%, rgba(255,255,255,.18), transparent 45%),
    radial(ellipse 55% 45% at 70% 80%, rgba(60,90,45,.55), transparent 70%),
    radial(circle at 45% 50%, #6a7561, #48523f 78%).

## Die Parallax-Waldszene ("twilight")

Fünf Ebenen, alle absolut im Inner-Canvas, alle `pointer-events: none`.
Scroll-Position `sy` = scrollTop des Viewports, per rAF gedrosselt in den State geschrieben.
Jede Ebene: `transform: translate3d(0, sy·depth, 0)`, `will-change: transform`.
Ebenenhöhe = canvasHöhe + |maxScroll·depth| + 40 px (bei negativem depth zusätzlich `top: −pad`),
damit auch am Ende des Pfades keine leere Fläche entsteht. maxScroll = canvasHöhe − 470.

| Ebene | depth | Inhalt |
|---|---|---|
| deep | 0.78 | Grundverlauf, Lichtinseln, fernste Tannenreihe |
| far | 0.50 | helle Bodenrücken, Nebelbänke |
| mid | 0.26 | dunklere Bodenrücken, ferne Baumreihen, Moos-Pools |
| glow (nicht-parallax, depth 0) | – | Vignette, Textur, Requisiten, Lichtinseln + Schatten der Knoten |
| foreground | −0.20 | Unterholz, große dunkle Tannen, Seitenvignette |

**1. deep (0.78)**
- Grundverlauf: linear 180° #173b2b → rgba(#123025,.27) → rgba(#143426,.6) 34 % → #102a20 68 % → #0d241c.
- Lichtinseln: vier radiale Ellipsen (80%×18%) bei 46%/6 %, 56%/34 %, 42%/62 %, 58%/90 % in
  rgba(178,224,178,.13) / rgba(158,206,232,.11) / rgba(206,182,238,.11) / rgba(178,224,178,.1).
- Tannenfeld: spacing 40 px, scale 0.5–0.7, Farbe #31593f, opacity .7, blur 1.5 px, Einzug 0–16 px.

**2. far (0.50)**
- Bodenrücken: alle 214 px ein Element 560×240 px, left −128/−66 px alternierend,
  border-radius 50% 50% 0 0 / 100% 100% 0 0,
  Füllung linear 180° rgba(36,72,52,.55) → rgba(26,56,40,.7) 45 % → rgba(20,46,33,.8),
  inset 0 2px 0 rgba(214,240,206,.2) (Kantenlicht), blur 1 px.
- Nebelbänke: vier horizontale Streifen (Höhe 170–190 px) bei 8 %, 36 %, 64 %, 90 %,
  linear 180° transparent → rgba(196,226,208,.07)/rgba(186,214,232,.06)/rgba(210,196,236,.06) → transparent,
  blur 10–12 px.

**3. mid (0.26)**
- Bodenrücken: alle 152 px ein Element 520×220 px, left −84/−62 px alternierend, gleiche Rundung,
  Füllung linear 180° rgba(20,46,33,.85) → rgba(14,34,25,.95) 40 % → rgba(11,28,21,1),
  box-shadow inset 0 2px 0 rgba(206,238,196,.22), 0 −10px 26px rgba(3,10,7,.5).
- Ferne Baumreihen: pro Rückenband 7 Versuche, scale 0.30–0.46, Farbe #2f5b42, opacity .4, blur 1.2 px.
  **Wichtig:** nur außerhalb des Knotenkorridors – gültig ist x + Breite < 62 oder x > 312
  (die Ebene driftet vertikal, deshalb ist eine y-basierte Ausnahme wirkungslos).
- Moos-Pools: fünf Ellipsen 160–175×54–58 px, radial rgba(120–134, 172–190, 136–150, .12–.13), blur 6 px.
- Tannenfeld: spacing 58 px, scale 0.8–1.05, Farbe #1d4030, opacity .95, blur 0.5 px, Einzug 4–24 px.

**4. glow – nicht-parallax (depth 0, Koordinaten = Knotenkoordinaten)**
- Seitliche Abdunkelung: linear 90° rgba(4,10,8,.6) → transparent 28 % → transparent 72 % → rgba(4,10,8,.6).
- Korridorlicht: linear 90° transparent 22 % → rgba(206,232,196,.05) 50 % → transparent 78 %.
- Textur: radial-gradient(rgba(255,255,255,.9) .5px, transparent .5px), background-size 4×4 px, opacity .045.
- **Bodenrequisiten** (Pilze / Steinhäufchen): genau ein Objekt je Knotenpaar, y = Mitte zwischen zwei
  Knoten ±13 px, x auf der pfadabgewandten Seite (Mittelpunkt der beiden Knoten < 187 → x 232…328,
  sonst x 18…114). scale 0.72–1.22, opacity .78.
  - Pilz (viewBox 26×30): Stiel rect x10 y12 w6 h18 r3 #3b4a3f;
    Hut `M1 14C1 5.5 7 1 13 1C19 1 25 5.5 25 14C18.5 16.5 7.5 16.5 1 14Z`
    in #6d4a63 / #4c4a72 / #5c5a48 (rotierend);
    Lichtsaum `M3.5 8.5C6 4.5 9.5 2.6 13 2.6C16.5 2.6 20 4.5 22.5 8.5`,
    stroke rgba(255,232,196,.34), width 1.4, linecap round.
  - Steinhäufchen (viewBox 54×30): drei Ellipsen (14/22 r13×8, 39/23 r14×7, 26/13 r15×10) in #1e2e25;
    Lichtsaum `M13 8.5C17 4.6 24 3.6 30 5.4C34 6.6 37 9 38.6 12`, stroke rgba(198,226,196,.2), width 1.4.
  - Je Requisite zusätzlich: Bodenschatten (left/right 6 %, bottom −3 px, height 7 px,
    radial rgba(2,8,5,.55), blur 2 px) und ein sehr zarter warmer Schein
    (Overhang 40 % nach allen Seiten, radial rgba(255,226,180,.1) → transparent 68 %).
- **Lichtinsel je Knoten:** 190×190 px zentriert auf dem Knoten,
  radial rgba(255,238,186,.11) → rgba(255,238,186,.045) 45 % → transparent 70 %.
- **Bodenschatten je Knoten:** 76×20 px, 20 px unter dem Knotenmittelpunkt,
  radial rgba(2,8,5,.5) → transparent 72 %, blur 3 px.
- **Glitzer:** 13 kleine Punkte (2–4 px) in #fff6dd / #eafff2 / #f7ecff / #fff3cf mit
  box-shadow 0 0 7–12px 2–4px in der jeweiligen Farbe, Animation `twinkle` 2.4–4.6 s, versetzte Delays.

**5. foreground (−0.20)** – wird NACH den Knoten gezeichnet
- Unterholz: alle 152 px je Seite eine Kuppe 90–160×30–52 px, border-radius 52% 48% 0 0 / 100% 100% 0 0,
  rgba(6,18,13,.92), blur 1.6 px, Einzug −34 bis −60 px.
- Große Tannen: spacing 112 px, scale 1.3–1.75, Farbe #071410, opacity .9, blur 2.6 px.
  **Wichtig:** Einzug so klammern, dass die Innenkante außerhalb des Knotenbands bleibt
  (inset ≥ Breite − 50 px), sonst verdeckt eine Tanne die Levelbuttons.
- Seitenvignette: linear 90° rgba(4,10,8,.5) → transparent 24 % → transparent 76 % → rgba(4,10,8,.5).

**Tannen-Silhouette (viewBox 60×100, eine Farbe, keine Konturen):**
```
Stamm : rect x26.5 y72 w7 h28 rx2.5
unten : M30 40C41 58 49 71 57 87C42 82 18 82 3 87C11 71 19 58 30 40Z
mitte : M30 20C39 37 45 48 51 63C39 58 21 58 9 63C15 48 21 37 30 20Z
oben  : M30 3C37 17 41 27 46 41C36 36 24 36 14 41C19 27 23 17 30 3Z
```
Verteilung (`firField`): je Zeile links und rechts ein Baum, rechte Seite um spacing·0.5 versetzt,
zusätzlicher y-Jitter bis spacing·0.35, Einzug per Pseudorandom zwischen xMin und xMax
(Positionierung über `left: −inset` bzw. `right: −inset`).
Pseudorandom deterministisch: `abs((sin((i + seed) · 12.9898) · 43758.5453) % 1)`.

## Interactions & Behavior
- Tippen auf einen freigeschalteten Stein startet das Level; gesperrte Steine reagieren nicht.
- Scrollen: nur der Viewport scrollt; der scrollTop wird per requestAnimationFrame gedrosselt in den
  State geschrieben und speist die `translate3d` der fünf Ebenen. Kein Scroll-Listener pro Ebene.
- Animationen: `twinkle` (Opazität/Skalierung der Funken) und `glowPulse` (aktueller Levelstein),
  beide ease-in-out, infinite.
- Aktueller Levelstein sollte beim Öffnen im Viewport sichtbar sein.

## State Management
- `unlocked` (höchstes freigeschaltetes Level), `completed` (Liste erledigter Level).
- `mapScroll` (Zahl, rAF-gedrosselt) – speist ausschließlich die Parallaxe.
- Abgeleitet: maxN, Knotenpositionen, Ebeneninhalte. Die Ebeneninhalte sind deterministisch aus
  maxN + Seed berechnet und müssen zwischen Renders stabil bleiben (memoisieren).
- `scenery`: Variante der Szenerie, Werte `twilight` (Standard), `clearing`, `grove`, `dense`.
  Für die Umsetzung ist nur `twilight` relevant; die anderen sind ältere Explorationen.

## Design Tokens
**Waldtöne:** #173b2b, #143426, #102a20, #0d241c, #1e2e25, #1d4030, #2f5b42, #31593f, #071410, #050e0a
**Bodenrücken:** rgba(36,72,52,.55), rgba(26,56,40,.7), rgba(20,46,33,.85), rgba(14,34,25,.95), rgba(11,28,21,1)
**Kantenlicht:** rgba(206,238,196,.22), rgba(214,240,206,.2), rgba(198,226,196,.2)
**Nebel/Licht:** rgba(196,226,208,.07), rgba(186,214,232,.06), rgba(210,196,236,.06), rgba(206,232,196,.05)
**Warmes Licht:** #ffe9a8, #fff3cf, #fff6dd, rgba(255,238,186,.11), rgba(255,226,180,.1), rgba(255,232,196,.34)
**Pilzhüte:** #6d4a63, #4c4a72, #5c5a48 · **Stiel:** #3b4a3f
**Schatten:** rgba(2,8,5,.5), rgba(3,10,7,.5), rgba(4,10,8,.6)
**Radien:** 54 px (Levelstein, Kreis), 26 px (Rahmen), 18 px (Viewport), 50% 50% 0 0 / 100% 100% 0 0 (Rücken)
**Raster:** Knotenabstand 92 px, Rückenabstand 152 px (mid) / 214 px (far), Tannenabstand 40/58/112 px
**Parallaxe-Faktoren:** 0.78 / 0.50 / 0.26 / 0 / −0.20
**Typo:** Quicksand 700 (Levelnummern 19 px, gesperrt 17 px), Cinzel Decorative 700 (Titel 19 px)

## Assets
Keine Bilddateien. Alle Silhouetten (Tannen, Pilze, Steinhäufchen) sind Inline-SVG-Pfade, die
vollständig oben dokumentiert sind. Der Pfad ist ein SVG-Polyline-Paar. Alles andere sind
CSS-Gradienten. Fonts: Quicksand und Cinzel Decorative (Google Fonts).

## Files
- `Fairydoku.dc.html` – kompletter Prototyp. Die Levelkarte samt Szenerie liegt im Template im Block
  `phase === 'map'`; die Geometrie und alle Ebenenlisten werden in der Methode `mapVals()` erzeugt
  (`firField`, `twRidges`, `twFarRidges`, `twDistTrees`, `twGroundProps`, `twNodeGlows`,
  `twNodeShadows`, `twScrub`, `onMapScroll`).
