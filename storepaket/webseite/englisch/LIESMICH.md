# Die englischen Rechtstexte

**Abgezogen von der Webseite am 8. September 2026** — als HTML, so wie die
Seiten ausgeliefert werden:

```
https://fairydoku.sites.humb.ug/en/impressum
https://fairydoku.sites.humb.ug/en/nutzungsbedingungen
https://fairydoku.sites.humb.ug/en/datenschutz     ← die für den englischen Store-Eintrag
https://fairydoku.sites.humb.ug/en/lizenzen
```

**Warum sie hier liegen:** Sie lagen sonst nirgends. Die deutschen Seiten
entstehen aus `ui/GameCopy.kt` — ein Test erzeugt sie, und ein zweiter hält sie
mit der App gleich. Die englischen entstehen nirgends im Projekt; sie sind
außerhalb geschrieben worden und existierten allein auf dem Server.

Das ist die Sorte Lücke, die erst auffällt, wenn sie weh tut: Ändert sich ein
deutscher Abschnitt, ändert der Test die deutsche Seite mit — die englische
bleibt stehen und behauptet weiter das Alte. Und wer sie ändern will, muss
erst jemanden finden, der weiß, wo sie herkommt.

**Bis das behoben ist, gilt:** Wer die Rechtstexte ändert, ändert diese vier
Seiten von Hand mit. Die englischen Fassungen der Play-Games-Abschnitte stehen
fertig in `PLAY-GAMES-START.md`.

Beide Fassungen tragen den Vorbehalt, der dabei wichtig ist: *„In case of any
discrepancy, the German version governs."* Die englische Seite ist eine
Übersetzung zur Bequemlichkeit, nicht die maßgebliche Fassung.

**Sauber wäre**, sie genauso zu erzeugen wie die deutschen: eine englische
Fassung von `GameCopy.legalBody`, aus der derselbe Test beide Sprachen
ausgibt. Dann kann keine der beiden zurückbleiben. Das ist ein eigenes Stück
Arbeit und steht noch aus.
