# Werkzeuge

Kleine Programme, die beim Bauen des Store-Pakets geholfen haben. Sie gehören
nicht in die App und laufen von Hand, wenn man sie braucht.

Der gemeinsame Gedanke: **Nichts wird zweimal gepflegt.** Rechtstexte,
Spielbeschreibung und Lizenzen stehen an genau einer Stelle; alles andere —
Webseite, PDFs, Lizenzseite in der App — wird daraus erzeugt. Wer eine dieser
Dateien von Hand nachzieht, hat über kurz oder lang zwei Fassungen, die
auseinanderlaufen, und merkt es beim Falschen.

Alle Wege unten sind vom Projektordner aus gemeint.

---

## `lizenzseite.py` — die Lizenzseite der App

Baut den Text, der in der App unter „Lizenzen" steht.

Die Copyright-Vermerke werden aus den Schriftdateien selbst gelesen, die
Lizenztexte liegen daneben (`apache-2.0.txt`, `ofl-1.1.txt`) — im Wortlaut der
Lizenzgeber. Abgetippt wird nichts: Bei einer Lizenz zählt jedes Wort, und
Abtippen ist die zuverlässigste Art, eines zu verlieren.

```
python3 werkzeuge/lizenzseite.py
```

Nötig, wenn eine Abhängigkeit oder eine Schrift dazukommt.

---

## `inhalt.py` — Textdateien für den PDF-Satz

Bereitet die Rechtstexte und die Spielbeschreibung so auf, dass `PdfSatz` sie
setzen kann.

**Vorher muss der Ausgabetest gelaufen sein**, sonst fehlen die Eingaben:

```
./gradlew :app:testDebugUnitTest --tests "*RechtstexteExportTest*"
python3 werkzeuge/inhalt.py
```

Der Test schreibt die Rechtstexte nach `app/build/rechtstexte/` — direkt aus
`GameCopy.kt`, also aus derselben Quelle, aus der die App sie anzeigt.

---

## `PdfSatz.java` — Satz der PDFs

Setzt die vorbereiteten Textdateien zu PDFs. Braucht keine fremde Bibliothek,
schreibt das PDF selbst.

```
java -Djava.awt.headless=true werkzeuge/PdfSatz.java
```

Das `headless` muss sein — ohne Bildschirm bricht Javas Schriftvermessung sonst
ab.

---

## `einbetten.py` + `vorlage.html` — die Store-Vorschauseite

Setzt Bildschirmfotos und Texte in eine einzelne HTML-Datei. Die Bilder werden
als `data:`-Adresse eingebettet, weil die veröffentlichte Seite keine fremden
Server erreichen darf.

```
python3 werkzeuge/einbetten.py
```

Die Texte kommen aus `storepaket/play-store/texte.md`.

---

## `tonhoehe.py` — welche Töne in einem Klang stecken

Misst, welche Tonhöhen in einer WAV-Datei klingen. Entstanden bei der Suche nach
dem Klang fürs Setzen: Die Frage war, ob in einer Aufnahme überhaupt ein
Glöckchen steckt oder nur ein weicher Anschlag.

```
python3 werkzeuge/tonhoehe.py klang.wav
```

Ohne numpy, deshalb Goertzel — für sechzig Halbtöne genügt das.

---

## Was hier nicht liegt

`ffmpeg` und `ffprobe` wurden für die Klangarbeit gebraucht, sind aber fremde
Programme von je 76 MB und gehören nicht ins Projekt. Wer sie braucht, holt sie
sich neu.
