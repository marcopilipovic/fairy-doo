# Landingpage: zwei Stellen, die nicht mehr stimmen

Stand: 9. September 2026. Die Seiten liegen im CMS unter *Seiten → fairydoku*,
je einmal DE und EN, und werden als roher JSON-Inhalt bearbeitet
(`sectionsJson`).

Die Seite ist aus dem Material vom Anfang September erzeugt worden. Zwei
Aussagen darin sind seither überholt — beide betreffen die Anleitung und die
Helfer.

---

## 1. Die Anleitung hat keine fünf Bilder mehr

**Wo:** Abschnitt `rules`, Schlüssel `sub`.

Seit Fassung 1.5.2 stehen beim allerersten Start **zwei** Bildschirme statt
fünf; alles Weitere erscheint erst, wenn es zum ersten Mal etwas bedeutet — die
Leben beim ersten verbrauchten Versuch, die Helfer zu Beginn von Level 2. Der
Grund kam aus der Testrunde: „Kinder lesen nicht!"

**Englisch — alt:**

```
"sub": "That's all there is to learn. The tutorial explains it in five pictures, and after that you've got it."
```

**Englisch — neu:**

```
"sub": "That's all there is to learn. The first time you play, two screens show you how; everything else turns up the moment it matters."
```

**Deutsch — neu:**

```
"sub": "Mehr gibt es nicht zu lernen. Beim ersten Mal erklären es zwei Bildschirme; alles Weitere taucht auf, sobald es das erste Mal etwas bedeutet."
```

---

## 2. Der dritte Helfer fehlt

**Wo:** Abschnitt `help`, Schlüssel `body`.

Seit Fassung 1.5.6 gibt es neben Feenstaub und Irrlicht den **Feenkreis**: eine
halbe Minute lang kreuzt jede gesetzte Fee selbst an, welche Felder sie
ausschließt. Zwei Stück im Vorrat, drei Stunden je Nachwuchs — nachgezählt in
`RegeneratingSupply.kt`.

Er gehört in denselben Textblock, nicht in einen neuen Schlüssel: Was die
Vorlage nicht kennt, zeigt sie nicht an.

**Englisch — neu:**

```
"body": "Fairy Dust places a fairy on a tile that is guaranteed safe. The Will-o'-the-Wisp, conversely, reveals a tile where none sits. You can hold three of each, and used ones regrow on their own in two hours. The Fairy Ring is the third: for half a minute, every fairy you place marks the tiles she rules out herself — two in stock, three hours to grow back. Nothing to buy, nothing to unlock."
```

**Deutsch — neu:**

```
"body": "Der Feenstaub setzt eine Fee auf ein Feld, das garantiert sicher ist. Das Irrlicht deckt umgekehrt ein Feld auf, auf dem keine sitzt. Von beiden hast du drei, und Verbrauchtes wächst in zwei Stunden von selbst nach. Der Feenkreis ist der dritte: Eine halbe Minute lang kreuzt jede Fee, die du setzt, selbst an, welche Felder sie ausschließt — zwei im Vorrat, drei Stunden je Nachwuchs. Nichts davon wird gekauft, nichts freigeschaltet."
```

---

## Was geprüft ist und stimmt

- **Die Leben:** fünf, eines wächst in zwei Stunden nach (`GlobalLives`).
- **Das Gitter:** 4×4 zu Beginn, alle zwei Level eine Reihe und eine Spalte
  mehr, ab Level 9 acht Feen.
- **Die drei Versuche**, die Aussage „keine Uhr", die zehn Feen mit eigenem
  Namen und eigener Farbe.
- **Der Punkt zur Werbung und zum Spielstand** im Abschnitt darunter — deckt
  sich mit dem, was Datenschutzerklärung und Datensicherheitsformular sagen.
- **„Coming soon to Google Play"** stimmt bis zum Tag der Veröffentlichung. Dann
  gehört es geändert, hier wie im Werbevideo.

## Hinweis zum Bearbeiten

Es ist rohes JSON: Ein fehlendes Komma oder ein nicht geschütztes
Anführungszeichen zerlegt die Seite. Am sichersten ist, nur den Text zwischen
den Anführungszeichen zu ersetzen und die Zeile im Übrigen stehen zu lassen —
und nach dem Speichern die Seite einmal aufzurufen.
