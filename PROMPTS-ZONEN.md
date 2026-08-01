# Fairydoku — Prompts für die zehn Gebietskacheln

Zehn Bilder, je eines für ein Waldgebiet des Spielbretts. Jedes wird als
**nahtlos wiederholbare Kachel** über die Felder einer Zone gelegt.

Der Ablauf: Kopiere den **gemeinsamen Kopf** vor jeden Einzelprompt, erzeuge das
Bild, prüfe es nach der Anleitung ganz unten, lege es unter dem angegebenen
Namen ab. Dann sage mir Bescheid — das Eintragen ist eine Zeile je Gebiet.

Du musst nicht alle zehn auf einmal liefern. Gebiete ohne Bild behalten das
gezeichnete Motiv, das Brett sieht dazwischen nicht unfertig aus.

---

## Der gemeinsame Kopf

Dieser Teil entscheidet über brauchbar oder unbrauchbar. Er gehört **vor jeden**
Einzelprompt:

```
Seamless tileable texture for a children's puzzle game set in a magical forest.
Hand-painted storybook illustration style, soft brush texture, muted and
slightly desaturated colours — gentle and cosy, never neon or glossy.

STRICT REQUIREMENTS:
- Seamlessly tileable: the left edge must continue into the right edge, the top
  into the bottom, with no visible seam.
- Perfectly even, flat lighting across the whole image. No vignette, no glow,
  no light source, no directional drop shadows. Any brightness gradient will
  show up as a grid when the tile repeats.
- Straight top-down view, completely flat, no perspective, no horizon, no
  3D depth.
- An allover pattern of even density — no single large motif, no focal point,
  nothing centred. Every part of the image should look equally interesting.
- Medium contrast. Game pieces are drawn on top of this texture and must stay
  readable.
- Square, exactly 1024 x 1024 pixels.
- No text, no letters, no numbers, no frame, no border, no watermark.
```

**Warum das so streng ist:** Eine Kachel deckt vier Spielfelder je Kante ab und
wiederholt sich danach. Ein heller Fleck in der Bildmitte wird dadurch zu einem
regelmäßigen Punktraster über das ganze Gebiet — auch wenn er im Einzelbild
hübsch aussah. Dasselbe gilt für jeden Schatten, der aus einer Richtung fällt.

---

## Die zehn Gebiete

### 1 — Goldene Lichtung

Eine sonnenbeschienene Lichtung, hell und freundlich. Das hellste Gebiet des
Bretts.

```
A sunlit forest clearing floor in warm cream and pale gold (#FDF6E3 base tone),
covered with fine dry grass, tiny four-pointed light sparkles, small delicate
fern fronds and scattered pale seeds. Soft golden-brown accents (#D9B46A).
```

Datei: `zone_goldene_lichtung.png`

### 2 — Sonnengarten

Ein Beet aus Sonnenblumen von oben gesehen.

```
A dense bed of sunflowers seen from directly above, warm saturated yellow
(#F6C445 base tone), overlapping round flower heads of slightly different
sizes, dark amber centres and petals (#9E6B00), a few green leaves filling the
gaps between blossoms.
```

Datei: `zone_sonnengarten.png`

### 3 — Tannenhain

Der dunkle Nadelwaldboden. Das dunkelste grüne Gebiet.

```
A dense forest floor of fallen pine needles in deep dark green (#1B4332 base
tone), layered needle clusters pointing in many directions, a few small closed
pine cones, patches of moss in lighter fir green (#40916C).
```

Datei: `zone_tannenhain.png`

### 4 — Dornenranke

Ein Dickicht aus dornigen Ranken.

```
A thicket of twisting bramble vines on emerald green (#00A86B base tone),
curving woody stems crossing over each other, sharp small thorns along the
stems, a few pointed leaves, deep emerald shadows (#004B23).
```

Datei: `zone_dornenranke.png`

### 5 — Herbstboden

Herabgefallenes Laub, dicht übereinander.

```
A carpet of fallen autumn leaves in rust orange (#C05621 base tone),
overlapping maple and oak leaves of varying sizes and angles, visible leaf
veins and stems, deep chestnut brown shadows between the leaves (#5C2000).
```

Datei: `zone_herbstboden.png`

### 6 — Feigenhain

Feigen an Zweigen, dazu die schräge Schraffur der Vorlage.

```
Stylised fig fruits on short branches over a rich violet ground (#6B3074 base
tone), teardrop-shaped figs with small leaves, arranged over a subtle diagonal
45-degree hatching pattern, soft lilac highlights on the fruit (#B86BB3).
```

Datei: `zone_feigenhain.png`

### 7 — Kristallader

Marmorierter Stein mit Kristalladern. Nach der Goldenen Lichtung das hellste
Gebiet.

```
Polished pale marble stone in cool grey-white (#E2E8F0 base tone), threaded
with fine crystalline veins and faceted geometric fracture lines in slate grey
(#64748B), subtle mineral speckle, smooth and cool.
```

Datei: `zone_kristallader.png`

### 8 — Flusslauf

Klares, fließendes Wasser von oben.

```
Clear shallow flowing water seen from above, bright aquamarine cyan (#38BDF8
base tone), gentle sinusoidal ripple lines running horizontally, soft
refractions and a hint of pebbles beneath the surface, deeper ocean blue in the
troughs (#0369A1).
```

Datei: `zone_flusslauf.png`

### 9 — Himmelstor

Der Nachthimmel als Boden — das dunkelste Gebiet des Bretts.

```
A deep indigo night sky (#1E1B4B base tone) filled with constellations: small
bright stars connected by thin faint lines into star patterns, scattered
smaller stars and fine cosmic dust, pale blue-lavender starlight (#A5B4FC).
```

Datei: `zone_himmelstor.png`

### 10 — Erdreich

Aufgesprungener Lehmboden.

```
Dry cracked clay earth in warm terracotta coral (#E05A47 base tone), an
irregular network of cracks forming uneven cells of different sizes, slightly
raised plate edges, deep red-brown in the fissures (#7A1C10).
```

Datei: `zone_erdreich.png`

---

## Negativ-Prompt

Falls die KI ein Feld dafür hat, hier zum Mitkopieren:

```
text, letters, numbers, watermark, signature, frame, border, vignette,
perspective, horizon, 3D render, glossy, shiny, neon, oversaturated,
single centred object, focal point, drop shadow, directional lighting,
visible seams, characters, people, animals
```

---

## Wohin die Bilder gehören

```
app/src/main/res/drawable-nodpi/zone_<name>.png
```

Die Namen stehen oben bei jedem Gebiet. Sie müssen genau so lauten:
kleingeschrieben, ohne Umlaute, Leerzeichen als Unterstrich — das ist keine
Pedanterie, sondern eine Regel von Android für Ressourcennamen. Ein Test im
Projekt prüft, dass zu jedem eingetragenen Gebiet auch eine Datei liegt und
umgekehrt keine Datei verwaist.

`drawable-nodpi` heißt: Android skaliert das Bild nicht je nach Gerätedichte
vor. Genau das ist hier nötig — sonst würde aus einer nahtlosen Kachel eine mit
weichen Rändern.

**Die 1024 Bildpunkte sind keine Empfehlung, sondern eine Bedingung.** Die App
lädt die Kachel halbiert auf 512 und teilt sie auf vier Felder je Kante auf —
das geht nur glatt auf, wenn die Kantenlänge durch acht teilbar ist. Bei einer
krummen Größe fehlten am Kachelende ein paar Bildpunkte, und an jeder vierten
Feldgrenze liefe eine feine Kante durchs Bild: der einzige Ort, an dem eine
nahtlose Kachel doch eine Naht bekäme. 1024, 2048 oder 512 sind sicher.

---

## Wie du prüfst, ob eine Kachel wirklich nahtlos ist

Das ist der Schritt, den man nicht überspringen sollte. Bild-KIs behaupten
Kachelbarkeit zuverlässig und liefern sie unzuverlässig — auch wenn im Prompt
„seamless" steht, sitzt oft eine sichtbare Naht im Bild.

**Der schnellste Test:** Lege das Bild viermal als 2×2-Block nebeneinander. Wenn
in der Mitte des Blocks ein Kreuz sichtbar wird, ist die Kachel nicht nahtlos.

- In jedem Bildbearbeitungsprogramm mit Ebenen (auch Paint.NET oder GIMP):
  Leinwand auf die doppelte Größe, Bild viermal einfügen.
- Online geht es schneller: Suche nach „seamless texture checker" — solche
  Seiten zeigen die Kachelung direkt an.

**Der zweite Blick gilt der Helligkeit:** Auch eine technisch nahtlose Kachel
verrät sich, wenn sie in der Mitte heller ist als am Rand. Beim Wiederholen
entsteht daraus ein Punktraster. Kneife die Augen zusammen oder verkleinere das
Bild stark — wenn ein heller Fleck übrig bleibt, taugt die Kachel nicht.

Wenn eine Kachel nicht passt: Schick sie mir trotzdem. Ein leichter
Helligkeitsverlauf lässt sich rechnerisch herausnehmen, und für kleinere Nähte
gibt es Mittel. Nur eine Kachel mit einem großen Motiv in der Mitte ist nicht
zu retten.

---

## Was danach passiert

Für jedes gelieferte Bild trage ich eine Zeile in `ZoneStyles.kt` ein. Das
gezeichnete Motiv bleibt als Rückfall bestehen — falls sich eine Kachel im Spiel
doch als unruhig erweist, ist sie mit derselben einen Zeile wieder draußen.

Die Farbwerte der Gebiete bleiben in jedem Fall erhalten: Der Schein, den eine
Fee auf ihrem Feld verbreitet, nimmt seine Farbe von dort.
