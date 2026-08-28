# Stand: 28. August 2026

Diese Datei ist der Einstieg für jede neue Sitzung in diesem Ordner. Sie sagt,
wo das Projekt steht und was noch fehlt — damit niemand aus Gesprächsresten
rekonstruieren muss, was längst getan ist.

**Fairydoku geht als erstes der Spiele in den Play Store.**

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

**Klang und Musik.** Die zehn Feentöne und alle Effekte werden im Spiel
gerechnet; ihre Schleifen schließen sich ohne Naht, weil `Synth.mixLooping` den
Überhang nach vorn faltet.

Die Waldmusik und der Schreckenslaut sind seit dem 28. August wieder
Aufnahmen — die berechneten Fassungen haben der Testrunde nicht gefallen.
Dieselbe Musik liegt über beiden Bildschirmen und läuft beim Wechsel
durch. Erzeugt mit ElevenLabs am 1. August 2026 unter bezahltem Tarif;
der Beleg dazu ist im `pruefbericht.md` noch als offener Punkt vermerkt.

**Werbung.** Nur freiwillige Videos für Feenstaub, Irrlicht oder ein Leben, ab
Level 3. Keine Banner. Das Werbe-SDK startet erst beim ersten Tippen auf einen
Werbe-Knopf; Googles Einwilligungswerkzeug (UMP) läuft davor. Die Spieluhr
steht währenddessen still — nachgemessen.

**Rechtstexte.** Echte Angaben (App HUMB UG, Parkstraße 9, HRB 208491),
Zielgruppe ab 13 Jahren, keine Platzhalter. Als Webseite in
`storepaket/webseite/`.

**Store-Paket.** `storepaket/` enthält APK, fünf Bildschirmfotos, Symbol 512,
Feature-Grafik, alle Texte, die Antworten für Datensicherheit und
Alterseinstufung sowie `pruefbericht.md`.

---

## Offen — nichts davon ist Programmierarbeit

1. **Echte AdMob-Kennungen.** Es stehen noch Googles Test-IDs im Code:
   `AndroidManifest.xml` (App-ID) und `RewardedAdManager.AD_UNIT_ID`. Ein
   Kollege beschafft sie. Damit darf nicht veröffentlicht werden.
2. **Einwilligungsnachricht im AdMob-Konto** anlegen und veröffentlichen
   (EU-Einstellungen). Sonst zeigt die App den Dialog, bekommt aber keinen
   Inhalt.
3. **Datenschutz-Seite ins Netz stellen.** Google ruft die Adresse beim
   Einreichen ab. Fertige Datei: `storepaket/webseite/rechtstexte.html`.
4. **Einmal auf einem echten Telefon durchspielen.** Bisher alles im Emulator
   geprüft, und seit der Zusammenführung ist noch gar nichts auf einem Gerät
   gelaufen. Besonders anzusehen: die Tageswertung im Spielverlauf und das
   Querformat — Android 16 achtet auf großen Bildschirmen nicht mehr auf die
   Festlegung auf Hochformat.
5. **Das fünfte Bildschirmfoto.** Die Reihe springt von 4 auf 6, weil die
   Tageswertung auf der alten Linie fehlte. Jetzt ist sie wieder da und kann
   aufgenommen werden. Nicht zwingend — Google verlangt mindestens zwei.

---

## Zwei Dinge, die man wissen muss

**Der Signierschlüssel liegt nur auf diesem PC** (`keystore/fairydoku-release.jks`,
absichtlich nicht im Repository). Geht er verloren, lässt sich die App im Play
Store nie wieder aktualisieren. Er gehört an einen zweiten Ort gesichert.

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
