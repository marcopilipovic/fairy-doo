# Grafiken für den Store-Eintrag

## Feature-Grafik

`feature-grafik-1024x500.png` — das Querformat-Bild, das im Play Store ganz
oben steht. Pflichtangabe.

Erzeugt von `FeatureGrafik.java` aus denselben Farben und Schriften wie das
Spiel (`Color.kt`, `app/src/main/res/font/`). Neu bauen:

```bash
java -Djava.awt.headless=true store/FeatureGrafik.java \
     app/src/main/res/font store/feature-grafik-1024x500.png
```

Warum ein Programm statt einer Bilddatei: Ändert sich die Farbwelt des Spiels,
lässt sich der Banner in einer Sekunde nachziehen, statt ihn nachzumalen.

### Gestaltungsentscheidungen

**Nur „FAIRYDOKU", kein Untertitel mit dem vollen Store-Namen.** Der Name steht
im Store ohnehin daneben, und die Grafik wird je nach Ansicht an den Rändern
beschnitten. Deshalb liegt alles Wichtige in der Mitte, und die Tannen stehen
am Rand, wo ein Anschnitt nicht stört.

**Die Leuchtpilze sind Absicht.** Ohne sie wäre es ein beliebiger Nachthimmel
mit Tannen; mit ihnen erkennt man den Feenpfad wieder.

**Kein Bildschirmfoto im Banner.** Ein abfotografiertes Handy in der
Feature-Grafik wirkt bei kleiner Darstellung matschig — die Bildschirmfotos
stehen ohnehin direkt darunter.

## Noch offen

**App-Symbol 512 × 512.** Liegt in der App als adaptives Symbol vor, muss für
den Store aber getrennt als PNG hochgeladen werden.
