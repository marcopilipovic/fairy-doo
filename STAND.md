# Stand: 29. August 2026

Diese Datei ist der Einstieg für jede neue Sitzung in diesem Ordner. Sie sagt,
wo das Projekt steht und was noch fehlt — damit niemand aus Gesprächsresten
rekonstruieren muss, was längst getan ist.

**Fairydoku geht als erstes der Spiele in den Play Store.**

**Der Paketname ist `ug.humb.fairydoku`** — Umkehr der Firmendomain, wie bei
den übrigen Apps des Hauses. Er ist am 31. August von `com.fairydoo.game`
umgestellt worden, dem letzten Tag, an dem das ging: Nach dem ersten Upload
liegt er für immer fest. Der alte behauptete die Domain fairydoo.com und trug
noch die frühere Schreibweise ohne k-u.

**Es gibt nur noch `main`, und alles liegt auf GitHub.** Bis zum 28. August
liefen zwei Fassungen nebeneinander, die sich am 6. August getrennt hatten;
`feature/neue-feen-symbol-und-musik` ist eingearbeitet und gelöscht. Wer hier
einen zweiten Zweig anlegt, sollte ihn zügig wieder zurückführen — die drei
Wochen Trennung haben dieselbe Arbeit zweimal entstehen lassen.

---

## Fertig und geprüft

**Die Feen.** Zehn Vektorzeichnungen aus dem Handoff „Feen schlicht"
(`Bilder/feen-schlicht/`), jede mit eigener Farbe. Die alten Pixelbilder und
sämtliche 🧚-Emoji sind raus — auch in Titelzeile, Hinweiszeile, Anleitung und
Overlays.

**Das App-Symbol.** Salta in Gold, buchstäblich dieselbe Zeichnung wie im Spiel
(`mipmap-anydpi-v26/ic_launcher_foreground.xml` übernimmt den Inhalt aus
`drawable/fairy_salta.xml`). Reiner Vektor, keine Dichtestufen mehr.

**Klang und Musik.** Nur noch das Spielende wird im Spiel gerechnet; ihre Schleifen schließen sich ohne Naht, weil
`Synth.mixLooping` den Überhang nach vorn faltet.

Sitzt eine Fee richtig, **kichert sie** — eine von sechs Aufnahmen, gewürfelt,
ohne Zuordnung zur Art. Beginnt ein Level, schwillt ein eigens dafür erzeugtes
Stück an (`level_start.mp3`, 5,4 s aus einer 20-Sekunden-Vorlage). Feenstaub und
Irrlicht haben **keinen eigenen Klang**: Sie sind eine andere Art, denselben Zug
zu tun, also klingen sie wie er.

Unter dem Jubel und unter dem Levelbeginn **tritt die Musik beiseite** — an
diesen zwei Stellen soll man nur den Klang hören. Dazwischen lag vom 5. bis zum 29. August ein
berechneter Eigenton je Fee. Beide Kehrtwenden kamen aus dem Spielen, nicht aus
der Theorie; `FairyChimes` bleibt samt Tests im Projekt, falls es ein drittes
Mal zurückgeht.

**Gesprochen wird nichts.** Bis zum 29. August folgte dem Jubel nach einem
gelösten Level ein Lobsatz aus der Sprachausgabe des Geräts. Er ist beim Spielen
als störend aufgefallen und ersatzlos entfallen; `FairyVoice.kt` ist damit
gelöscht. Der Regler „Feenstimme" bleibt — er regelt, wie laut die Fee beim
Setzen kichert.

Die Waldmusik und der Schreckenslaut sind seit dem 28. August wieder
Aufnahmen — die berechneten Fassungen haben der Testrunde nicht gefallen.
Dieselbe Musik liegt über beiden Bildschirmen und läuft beim Wechsel durch.
Erzeugt mit ElevenLabs am 1. August 2026 unter dem Tarif *Starter*, der die
gewerbliche Lizenz für Sprache und Musik ausdrücklich einschließt. **Die
Rechtefrage ist damit erledigt** — Einzelheiten im `pruefbericht.md`.

**Keine Spieluhr.** Der Countdown je Level ist am 28. August ersatzlos
gestrichen. Ein Level endet seither nur noch durch drei verbrauchte Versuche.
Ein Rätsel, das vom Nachdenken lebt, soll nicht genau dafür bestraft werden.

**Werbung.** Nur freiwillige Videos für Feenstaub, Irrlicht oder ein Leben, und
erst nach den ersten drei Leveln (`ADS_UNLOCK_AFTER_LEVEL`); davor tritt an ihre
Stelle ein Geschenk. Keine Banner. Das Werbe-SDK startet erst beim ersten Tippen auf einen
Werbe-Knopf; Googles Einwilligungswerkzeug (UMP) läuft davor. Bleibt eine
Anzeige aus — kein Netz, kein Vorrat, abgelehnte Einwilligung —, meldet der
`RewardedAdManager` das in jedem Fall zurück; ein Wachhund nach zwölf Sekunden
fängt auch den Fall ab, in dem Google gar nichts sagt. Vorher konnte das Spiel
dabei stehenbleiben und ließ sich nur durch einen Neustart lösen.

**Rechtstexte.** Vier Seiten — Impressum, AGB, Datenschutz, Lizenzen —, echte
Angaben (App HUMB UG, Parkstraße 9, HRB 208491), Zielgruppe ab 13 Jahren, keine
Platzhalter. Sie stehen einmal in `ui/GameCopy.kt` und werden von dort in die
App, auf die Webseite (`storepaket/webseite/`) und in die PDFs ausgegeben; ein
Test hält beide Ausgaben mit der App gleich. Die Lizenzseite trägt OFL 1.1 und
Apache 2.0 im Wortlaut — beide verlangen das.

**Store-Paket.** `storepaket/` enthält APK, fünf Bildschirmfotos, Symbol 512,
Feature-Grafik, alle Texte, die Antworten für Datensicherheit und
Alterseinstufung sowie `pruefbericht.md`. Auf drei der Fotos stand noch die
Spieluhr; sie ist am 29. August herausgenommen und die Blätterzeile wieder
mittig gesetzt worden, damit die Bilder zeigen, was die App zeigt.

---

## Offen — nichts davon ist Programmierarbeit

1. ~~**Echte AdMob-Kennungen.**~~ **Erledigt am 31. August.** Sie stehen in
   `app/build.gradle.kts`, und zwar je Bauart verschieden: Die Release-Fassung
   trägt die echten, die Debug-Fassung behält Googles Testkennungen. Wer auf
   eigene echte Anzeigen tippt, erzeugt „ungültigen Traffic" — der häufigste
   Weg, ein AdMob-Konto zu verlieren.
2. **Einwilligungsnachricht im AdMob-Konto** anlegen und veröffentlichen
   (EU-Einstellungen). Sonst zeigt die App den Dialog, bekommt aber keinen
   Inhalt.
3. ~~**Datenschutz-Seite ins Netz.**~~ **Erledigt am 31. August.** Sie steht
   unter `fairydoku.sites.humb.ug` mit vier getrennten Adressen; für den Store
   zählt `/de/datenschutz`. Alle vier geprüft und wortgleich mit der Quelle.
   Ändern sich die Texte, liegt jede Seite einzeln in
   `storepaket/webseite/seiten/`.

4. **Einmal in Ruhe auf einem echten Telefon durchspielen.** Besonders anzusehen: die
   Tageswertung im Spielverlauf und das Querformat — Android 16 achtet auf
   großen Bildschirmen nicht mehr auf die Festlegung auf Hochformat. Die
   Testrunde hat inzwischen das meiste davon abgedeckt; von dort kamen die
   Einrückung der Rechtstexte, die stehengebliebene Werbung und der Wunsch,
   die Uhr und die berechnete Musik loszuwerden.
5. ~~**Markenrecherche.**~~ **Erledigt am 30. August.** TMview auf „fairydoku":
   keine Treffer, und TMview führt auch die nationalen Register. Einzelheiten
   im `pruefbericht.md`.
6. ~~**Signierschlüssel sichern.**~~ **Erledigt am 30. August.** Drei Orte:
   diese Maschine, ein USB-Stick, ein Ausdruck.

**Es bleiben also drei Dinge, und zwei davon gehören demselben Konto:** die
echten AdMob-Kennungen samt Einwilligungsnachricht, und die Datenschutz-Seite
unter ihrer Adresse. Der Rest ist Spielen.

---

## Zwei Dinge, die man wissen muss

**Der Signierschlüssel liegt nur auf diesem PC.** Zwei Dateien im Projektordner,
beide absichtlich nicht im Repository (`.gitignore` Zeile 16 und 17):

| Datei | Was drin steht |
| --- | --- |
| `fairydoku-upload.keystore` | der Schlüssel selbst, 4302 Byte |
| `keystore.properties` | die zwei Kennwörter und der Alias |

**Gesichert am 30. August 2026** an drei Orten: hier, auf einem USB-Stick und
als Ausdruck. Das Druckblatt schreibt den Schlüssel als 80 Zeilen Text um, jede
mit eigener Prüfsumme, dazu Fingerabdruck und Rückweg — erzeugt von
`/home/nataly/schluessel-sicherung/bauen.py`, und die Rückrechnung ist
nachgeprüft: Byte für Byte identisch.

Vorher lag er **einmal**, und zwar auf dieser Maschine. Die ist eine virtuelle;
was hier steht, steht nicht auf einem Rechner, den jemand in der Hand hält.

**Auf die anwaltliche Prüfung der Rechtstexte wurde bewusst verzichtet.** Als
Rechtsgrundlage für die Werbung steht die Einwilligung (Art. 6 Abs. 1 lit. a
DSGVO) — passend dazu, dass vor der ersten Anzeige Googles Einwilligungswerkzeug
läuft und ohne Einwilligung nichts angefragt wird. Ungeprüft ist damit nicht
mehr die Einordnung, sondern nur noch, ob die Texte im Übrigen vollständig sind.

*(Hier stand bis zum 25. August das berechtigte Interesse nach lit. f. Das war
schon damals falsch — die Datenschutzerklärung selbst nennt seit jeher die
Einwilligung. Wer diese Datei als Einstieg las, trug den Fehler weiter.)*

---

## Wo was steht

| Frage | Datei |
| --- | --- |
| Wie das Spiel funktioniert und warum | `README.md` |
| Was vor der Veröffentlichung geprüft wurde | `storepaket/pruefbericht.md` |
| Texte und Grafiken für den Store | `storepaket/play-store/` |
| Rechtstexte für die Webseite | `storepaket/webseite/` |
| Warum eine Stelle im Code so ist, wie sie ist | der Kommentar dort — die
  Begründungen stehen im Quelltext, nicht in Extra-Dokumenten |
