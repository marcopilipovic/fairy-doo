# 10 Feen — schlichte Variante

Reduzierte Feen-Sprites als **SVG**: flache Farbflächen, keine Fellstruktur, keine
Flügelmuster. Gedacht als Spielsteine (klein lesbar) und Sammelbilder (groß).

Vorschau: `Feen schlicht.dc.html` im Browser öffnen.

## Dateien

```
feen_schlicht/            10 SVGs, je eine Zonenfarbe
Feen schlicht.dc.html     Übersichtsseite (Prototyp)
support.js                Laufzeit des Prototyps — nicht in die App übernehmen
```

## Aufbau je Fee

- **viewBox** `0 0 120 164`, Seitenverhältnis 1 : 1,367
- Empfohlene Größen: **Spielstein 38 × 52 px**, **Sammelbild 100 × 137 px**
- Alle Konturen `#4a3326`, Strichbreite **2.2** — bei Skalierung nicht mitskalieren lassen,
  sonst wirkt der Spielstein zu fett; für < 40 px Breite Strichbreite auf 3 erhöhen
- Haut `#f7dcc4`, Beine `#e8c1a2` (Strichbreite 7), Arme `#f7dcc4` (Strichbreite 7)
- Flügel: 4 Ellipsen in der Aufhell-Farbe, oben `fill-opacity .85`, unten `.7`
- Kleid: Dreieck in der Hauptfarbe, Saum als Bogenband in der Dunkelfarbe
- Gesicht: zwei Punkte (r 2.3) + Mundbogen, in `#4a3326` — bewusst minimal

## Palette

| Zone | Datei | Haupt | Dunkel (Saum) | Hell (Flügel) | Haar |
|---|---|---|---|---|---|
| Teal | `fee_teal.svg` | `#2f9c9c` | `#1f7a7a` | `#7fd6d0` | `#3c2a20` |
| Gold | `fee_gold.svg` | `#e8b93a` | `#bf8f1e` | `#ffe08a` | `#a9763f` |
| Grün | `fee_green.svg` | `#6faa4f` | `#4d8036` | `#bce39b` | `#6b4a2e` |
| Orange | `fee_orange.svg` | `#e28a42` | `#b96524` | `#ffc48e` | `#c98a4a` |
| Pink | `fee_pink.svg` | `#e37a9c` | `#bb5678` | `#ffb9cf` | `#4a3226` |
| Lila | `fee_purple.svg` | `#8a6ac4` | `#664a9c` | `#c9b4ec` | `#5a3f6a` |
| Rosé | `fee_rose.svg` | `#d4657a` | `#a94257` | `#f5aab6` | `#8a4a4a` |
| Blau | `fee_blue.svg` | `#4a8ac4` | `#2f6699` | `#a5cdec` | `#3f4a6a` |
| Eisblau | `fee_icy.svg` | `#8ac7d8` | `#5e9cb0` | `#d3eef5` | `#7a6a5a` |
| Marine | `fee_navy.svg` | `#46579c` | `#2c3a72` | `#93a2d8` | `#2e2a3a` |

## Hinweise zur Verwendung

- Die SVGs haben **keinen** eigenen Hintergrund und keinen Glow. Ein Glow gehört in die App
  (z. B. `filter: drop-shadow(0 0 8px <Hauptfarbe>)`), damit er zum Spielfeld passt.
- Farbwechsel ist trivial: pro Datei kommen nur die vier Palettenwerte vor.
- Für Animation (Flügelschlag) die beiden oberen Ellipsen um ihren jeweiligen Mittelpunkt
  auf `scaleX(.86 → 1)` bei 2.2 s wechseln lassen — die unteren gegenläufig.
