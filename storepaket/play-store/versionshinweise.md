# Fairydoku — Versionshinweise und Testerhinweise

Drei Felder, die der Play Store verlangt und die leicht verwechselt werden.
Alle Texte hier stehen fertig zum Kopieren, mit Googles Grenzen daneben.

---

## 1. Versionshinweise · „Was ist neu?"

**Wo:** Beim Erstellen eines Releases, je Sprache. **Grenze: 500 Zeichen.**
**Wer liest es:** alle, im Store unter „Neuigkeiten".

Bei einer ersten Fassung gibt es nichts Neues zu melden — deshalb steht hier,
was die App überhaupt ist. Ab der zweiten Fassung wird daraus eine Liste der
Änderungen.

```
Die erste Fassung von Fairydoku.

Setze Feen auf ein Waldgitter: eine je Reihe, eine je Spalte, eine je
leuchtender Zone — und keine zwei berühren sich, auch nicht über Eck.

Das Gitter wächst alle zwei Level, bis acht Feen nebeneinander wohnen.
Eine Uhr läuft nicht: Du kannst überlegen, so lange du magst.

Feenstaub und Irrlicht helfen, wenn es klemmt. Beide wachsen von selbst
nach — nichts muss gekauft werden.
```

---

## 2. Hinweise für die Tester

**Wo:** in der Testspur, beim Einladen der Tester.
**Wer liest es:** nur die Testrunde.

```
Was diese Fassung ist
Eine Testfassung. Sie ist in allem gleich wie die spätere Veröffentlichung,
mit einer Ausnahme: Die Werbung zeigt Googles Beispielanzeigen statt echter.
Ihr dürft sie also gefahrlos anschauen und antippen.

Worauf ihr besonders achten könnt
• Die Klänge. Sie sind diese Woche mehrfach geändert worden — vor allem der
  Ton beim Setzen des ✕, den man dutzendfach je Level hört.
• Der Levelpfad. Beim Wiederholen eines früheren Levels sollte die Karte
  dorthin springen, wo ihr steht — nicht zum weitesten Level.
• Die Werbung. Kommt keine Anzeige, muss das Spiel trotzdem weiterlaufen.
  Wenn es hängenbleibt, ist das ein Fehler und kein Wartezustand.
• Die Klang-Einstellungen. Neben jedem Regler steht ein Lautsprecher zum
  Stummschalten.

Was wir schon wissen
• Auf Tablets ist die App benutzbar, aber nicht angepasst — das Brett bleibt
  in der Mitte stehen. Das ist bekannt und kein Fehlerbericht wert.
• Das Querformat hat noch niemand gesehen.

Sonstiges
Kein Konto, keine Anmeldung, keine Käufe. Offline spielbar; nur die Werbung
braucht Netz.

Was ihr meldet
Was passiert ist, was ihr erwartet habt, und auf welchem Gerät. Ein
Bildschirmfoto sagt mehr als eine Beschreibung.
```

---

## 2b. Testerhinweise zur zweiten Runde (Fassung 1.5.1, Nummer 55)

```
Was sich geändert hat

Vier Sachen aus eurer ersten Rückmeldung sind behoben. Bitte schaut euch
besonders diese an:

• Wärme und Akku. Das Spiel hat im Hintergrund durchgehend gerechnet, obwohl
  sich auf dem Brett nichts bewegt — sechzigmal pro Sekunde. Das ist raus.
  Wenn das Gerät jetzt noch heiß wird, sagt bitte Bescheid, dann steckt mehr
  dahinter.
• Tippen. Ton und Bild sollten mitkommen, auch wenn ihr schnell setzt. Das
  hing vermutlich an derselben Ursache.
• Karte und zurück. Auf das Level zu tippen, das gerade läuft, bringt euch
  jetzt dorthin zurück, statt es neu zu starten. Eure gesetzten ✕ bleiben
  stehen, die Punkte auch.
• Bildschirm. Er bleibt an, solange ein Rätsel offen ist. Auf der Karte und
  in den Menüs dunkelt er wie gewohnt ab.
• Punkte. Sie werden erst gutgeschrieben, wenn ihr den Gewinn-Dialog
  schließt — dann zählt die Zahl sichtbar hoch. Vorher stand sie schon auf
  dem neuen Wert, während der Dialog noch etwas versprach.

Was weiter gilt

Die Werbung zeigt Googles Beispielanzeigen, nicht echte. Ihr dürft sie
gefahrlos anschauen und antippen.

Auf Tablets ist die App benutzbar, aber nicht angepasst. Das ist bekannt.

Noch offen

Die Schwierigkeit der Level ist erzeugt, nicht entworfen — dass Level 5
schwerer war als alles bis 20, ist bekannt und noch nicht geändert.

Was ihr meldet

Was passiert ist, was ihr erwartet habt, und auf welchem Gerät.
```

---

## 2c. Testerhinweise zur vierten Runde (Fassung 1.5.6, Nummer 60)

```
Vier Punkte aus eurer Rückmeldung sind eingebaut.

Die Anleitung ist kürzer geworden
Beim allerersten Start stehen jetzt zwei Bildschirme statt fünf: das Ziel und
wie man setzt. Die Erklärung zu den Leben kommt beim ersten verbrauchten
Versuch, die zu den Helferlein zu Beginn von Level 2. Jede genau einmal. Wer
alles am Stück will, findet es weiter unter dem Fragezeichen.

Wer schon gespielt hat, sieht davon nichts mehr. Zum Prüfen die App
löschen und neu holen, oder unter Einstellungen die App-Daten löschen.

Die Hilfe sagt nicht mehr die Unwahrheit
Setzt der Feenstaub eine Fee und eine eurer eigenen Feen steht in derselben
Reihe, wurde bisher alles rot markiert, auch die neue. Dabei ist gerade die
garantiert richtig. Was aus einer Hilfe kommt, wird jetzt nie mehr rot. Das
Rot bleibt auf den Feen, die weg müssen.

Man sieht, wo die Hilfe gelandet ist
Von dem neuen Feld laufen zwei goldene Ringe nach außen.

Ein Satz stimmt jetzt
„Sicher hast du schon:" statt „Sicher ist dir schon:".

Der Feenkreis — der dritte Helfer
Neben Feenstaub und Irrlicht gibt es jetzt einen dritten Knopf. Wer ihn
antippt, hat dreißig Sekunden lang einen brennenden Feenkreis: Jede Fee, die
ihr in dieser Zeit setzt, kreuzt selbst an, welche Felder sie ausschließt —
Reihe, Spalte, Zone und die Nachbarfelder.

Er nimmt euch das Tippen ab, nicht das Nachdenken. Und er garantiert nichts:
Setzt ihr eine Fee falsch, sind seine Kreuze auch falsch. Zwei Stück im
Vorrat, drei Stunden bis einer nachwächst.

Ein Knopf zum Leeren des Bretts
Oben links, zwischen Anleitung und Klang. Er nimmt alle eigenen Zeichen weg;
das Level und die Versuche bleiben. Was aus Feenstaub oder Irrlicht stammt,
bleibt ebenfalls stehen — dafür habt ihr bezahlt.

Auch das Tippen rüttelt jetzt
Bisher gab es Haptik nur beim Halten. Ein ✕ zu setzen war die häufigste Geste
im Spiel und die einzige ohne Antwort im Finger.

Das Spielbrett nutzt die volle Breite
Es war auf eine feste Größe gedeckelt, die noch aus der Entwurfsvorlage
stammte. Bei großen Gittern waren die Felder dadurch kleiner als der
Richtwert, den Android für Tippziele empfiehlt.

Der Bildschirm bleibt drei Minuten wach
Vorher blieb er an, solange ein Rätsel offen war — auch bei einem Handy, das
auf dem Tisch lag. Jede Berührung schenkt jetzt drei Minuten, danach dunkelt
das Gerät ab wie sonst.

Die Punkte sind jetzt eine Zahl
Im Rätsel steht dieselbe Zahl wie auf der Karte: die Punkte des heutigen
Tages. Vorher stand dort der laufende Lauf, und weil beide Zahlen „Punkte"
hießen, sah es nach einem Fehler aus. Der Lauf wird weiter mitgezählt, er
füttert den Bestwert, ist aber nicht mehr zu sehen.

Das Spielbrett ist größer
Es war auf eine feste Breite gedeckelt, die noch aus der Entwurfsvorlage
stammte. Auf jedem Handy, das breiter ist, lag daneben Platz brach — und je
größer das Gitter, desto kleiner wurden die Felder darin. Bei 8×8 lagen sie
unter dem Richtwert, den Android für ein Tippziel empfiehlt. Jetzt wächst das
Brett mit, bis der Platz aufgebraucht ist.

Auf großen Gittern sollte sich das deutlich anfühlen. Auf Tablets besonders.

Was weiter offen ist
• Tablets sind benutzbar, aber nicht angepasst.
• Level 5 ist schwerer als die Level danach. Bekannt.

Worauf ihr besonders achten könnt
Wird das Gerät noch warm? In der letzten Fassung ist eine Dauerschleife
herausgeflogen, die durchgehend gerechnet hat. Wenn es jetzt immer noch
heiß wird, steckt mehr dahinter und wir müssen weitersuchen.
```

---

## 2d. Tester notes, English (version 1.5.6, build 60)

```
The fairy ring — a third helper
Next to fairy dust and the will-o'-the-wisp there is now a third button. Tap
it and you get thirty seconds of a burning fairy ring: every fairy you place
during that time marks the squares she rules out herself — row, column, glade
and the neighbouring squares.

It saves you the tapping, not the thinking. And it guarantees nothing: place
a fairy wrongly and its crosses are wrong too. Two in stock, three hours for
one to grow back.

A button to clear the board
Top left, between the tutorial and the sound menu. It removes all your own
marks; the level and your attempts stay. Anything that came from fairy dust
or a will-o'-the-wisp stays as well — you paid for that.

Tapping now gives haptic feedback too
Until now there was only feedback on press-and-hold. Placing an X is the most
frequent gesture in the game and was the only one with no answer in the
finger.

The board uses the full width
It was capped at a fixed size left over from the design template. On large
grids the squares were smaller than the minimum Android recommends for a
touch target.

The screen stays awake for three minutes
Before, it stayed on for as long as a puzzle was open — including a phone
lying on the table. Every touch now buys three minutes, after which the
device dims as usual.

Points are one number now
The puzzle screen shows the same figure as the map: today's points. Before it
showed the current run, and since both were called "points", it looked like a
bug.

Please note
The shorter tutorial only appears on a fresh install. If you have played
before, delete the app or clear its data to see it.

Advertising shows Google's sample ads, not real ones. You may tap them
freely.

Known and still open
• Tablets are usable but not specifically adapted.
• Level 5 is harder than the levels after it. The puzzles are generated, not
  designed, so difficulty varies within a grid size.
• The day rolls over at 4 a.m. on purpose: anyone playing at half past eleven
  should not lose their score mid-session.

What to report
What happened, what you expected, and on which device.
```

---

## 3. Anweisungen für Rezensenten

**Wo:** App-Inhalte → „Zugriff auf App".
**Wer liest es:** Googles Prüfer.

Die App braucht keine Anmeldedaten, deshalb genügt die Auswahl **„Alle
Funktionen sind ohne besonderen Zugriff verfügbar"**. Wer trotzdem etwas
schreiben will:

```
Die App benötigt keine Anmeldung und keine Zugangsdaten. Alle Funktionen
sind sofort verfügbar.

Werbung erscheint ausschließlich als freiwillige Videoanzeige, die der
Spieler selbst startet, um eine Spielhilfe zu erhalten. Es gibt keine
Banner und keine Anzeigen, die von selbst erscheinen. Vor der ersten
Anzeige läuft Googles Einwilligungswerkzeug (UMP).

Ab dem vierten Level erscheinen die Werbe-Knöpfe; davor tritt an ihre
Stelle ein Geschenk.
```

---

## Was in diese Felder *nicht* gehört

**Keine Versprechen für die Zukunft.** „Bald mit Online-Rangliste" in den
Versionshinweisen ist eine Zusage, die Google als Teil der Beschreibung liest.

**Keine Preise oder Werbeaussagen.** „Jetzt gratis!" oder „Bestes Rätselspiel"
sind in diesem Feld unerwünscht.

**Keine Bitte um Bewertungen.** Auch nicht freundlich formuliert.
