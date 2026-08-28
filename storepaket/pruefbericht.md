# Fairydoku — Prüfbericht vor der Veröffentlichung

Stand: 19. August 2026, geprüft an der Release-APK (versionName 0.1.0,
3.327.731 Byte) auf einem Pixel-5-Emulator mit Android 15.

Drei Fragen: Können die Rechte an Mitgeliefertem Ärger machen? Kann der Store
Ärger machen? Und kann die App auf fremden Telefonen Schaden anrichten?

---

## 1. Risiken für die Spieler

**Kurz: keine.** Im Einzelnen geprüft:

### Rechte, die die App anfordert

Die App selbst fordert zwei an:

| Recht | Wofür |
| --- | --- |
| `INTERNET` | nur für die Werbeanzeige |
| `ACCESS_NETWORK_STATE` | ob überhaupt eine Verbindung besteht |

Das Werbe-SDK bringt weitere mit, die im fertigen Paket landen: `AD_ID`,
`ACCESS_ADSERVICES_AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`,
`ACCESS_ADSERVICES_TOPICS`, `WAKE_LOCK`, `FOREGROUND_SERVICE`.

**Keines davon ist ein gefährliches Recht.** Android fragt bei keinem einzigen
nach — es erscheint kein Dialog, weil keines nachfragepflichtig ist. Und es
fehlt vollständig, was üblicherweise Sorgen macht: **kein** Zugriff auf Kamera,
Mikrofon, Standort, Kontakte, Kalender, Telefon, SMS, Dateien oder Fotos.

Zu `ACCESS_ADSERVICES_TOPICS`: Das ist Googles Themen-Schnittstelle für
interessenbasierte Werbung. Das SDK meldet sie an, die App nutzt sie nicht —
jede Anzeigenanfrage ist ausdrücklich als nicht personalisiert markiert. Die
Anmeldung des Rechts allein löst nichts aus.

### Was die App auf dem Gerät anfasst

Nachgesehen im gesamten Quelltext: **kein einziger Zugriff außerhalb des
eigenen App-Bereichs.** Kein `getExternalStorage`, kein `/sdcard`, kein
`MediaStore`. Geschrieben wird nur in zwei Verzeichnisse, die Android der App
allein zuweist und beim Deinstallieren restlos entfernt:

| Ort | Inhalt | Größe |
| --- | --- | --- |
| `files/datastore/` | Spielstand, Name, Avatar, Vorräte | 32 KB |
| `cache/sounds-v6/` | berechnete Klänge und Musik | rund 7 MB |

Die 7 MB im Zwischenspeicher sind der Preis dafür, dass Musik und Klänge nicht
als Dateien mitgeliefert werden: Sie werden beim ersten Start gerechnet und
danach dort abgelegt. Android darf diesen Ordner jederzeit löschen; die App
rechnet dann neu. Er wächst nicht — ältere Fassungen werden beim Start
entfernt.

**Die App belegt damit rund 10 MB.** Zum Vergleich: Die Installationsdatei
allein ist 3,3 MB.

### Datensicherung

Eine Sache, die man wissen sollte: Fairydoku nimmt an Androids automatischer
Datensicherung teil. Der Spielstand liegt dadurch auch im Google-Konto des
Spielers und überlebt einen Telefonwechsel. Das ist gewollt, betrifft nur sein
eigenes Konto, und die Datenschutzerklärung nennt es jetzt ausdrücklich samt
Abschaltweg. (Bis heute stand dort das Gegenteil — behoben.)

### Stabilität

- **Kein einziger Absturz** über die gesamte Testerei: elf Level durchgespielt,
  Werbung mehrfach, Einwilligung erteilt und zurückgenommen, Bildschirm- und
  Levelwechsel, App-Neustarts.
- **Vollständig offline lauffähig.** Frisch installiert ohne Netz gestartet:
  Anleitung, Levelkarte, Spiel, Klang und Musik funktionieren. Nur die Werbung
  entfällt, und der Knopf sagt das.
- Der Release-Build ist **nicht debugfähig**, verkleinert, verschleiert und
  signiert.

### Batterie

Die Spieluhr läuft mit 16-ms-Schritten, aber nur während eines laufenden
Levels. Beim Wechsel in den Hintergrund halten Uhr und Musik an, seit heute
auch während einer Werbeanzeige. Keine Hintergrunddienste, keine Weckzeiten,
keine Standortabfragen.

---

## 2. Urheberrecht

**Nach dem heutigen Durchgang: nichts Fremdes mehr im Spiel, außer zwei
Schriften unter freier Lizenz.**

| Was | Herkunft | Bewertung |
| --- | --- | --- |
| Die zehn Feen | Handoff „Feen schlicht", eigene Arbeit | eigen |
| App-Symbol, Store-Bilder | aus derselben Zeichnung erzeugt | eigen |
| Musik (zwei Stücke) | im Spiel berechnet | eigen, keine Aufnahme |
| Alle Klänge, zehn Feentöne | im Spiel berechnet | eigen, keine Aufnahme |
| Cinzel Decorative, Quicksand | Google Fonts | **SIL OFL 1.1** |
| Bibliotheken | AndroidX, Google, JetBrains | Apache 2.0 / Play-Bedingungen |

**Nachtrag vom 28. August:** Zwei Aufnahmen sind zurück in der App — die
Waldmusik (`ambient_forest.mp3`, 821 KB) und der Schreckenslaut
(`fairy_startled.mp3`, 6 KB). Der Rest wird weiterhin berechnet: alle übrigen
Effekte und die zehn Feentöne.

Beide waren im August entfernt worden, weil hier stand, ihre Rechtelage sei
nicht zu belegen. Das war zu vorsichtig formuliert — es war eine offene Frage,
keine Absage. Erzeugt wurden sie am **1. August 2026 zwischen 5:56 und 12:53
Uhr** mit ElevenLabs, und zwar unter einem bezahlten Tarif; die Zeitstempel
stehen in den Dateinamen unter `Audio/`.

⬜ **Nachzutragen:** die Abo-Bestätigung für den 1. August 2026 — Tarifname und
Zeitraum. Erst damit ist die Herkunft belegt statt erinnert. Solange das fehlt,
gilt dieser Punkt als offen.

Zurückgeholt wurden sie, weil die Testrunde die berechnete Musik nicht mochte.
Die Waldschleife ist dabei von 194 kbit/s Stereo auf 112 kbit/s Mono neu
kodiert worden — 821 statt 1.423 KB, und der Abspieler rechnet sie ohnehin auf
einen Kanal herunter.

**Die Schriftlizenz** verlangt, dass Copyright-Vermerk und Lizenzhinweis
mitgeliefert werden. Beides steht jetzt im Impressum. Die OFL erlaubt das
Einbetten in Anwendungen ausdrücklich, auch kommerziell.

Was nicht schützbar ist und deshalb auch keine Gefahr darstellt: die
Spielregel. „Eine Fee je Reihe, Spalte und Zone" ist ein bekanntes
Rätselprinzip; Spielregeln sind nicht urheberrechtlich geschützt. Geschützt ist
die konkrete Ausgestaltung — Figuren, Bilder, Texte, Klänge, Quelltext — und
die ist eigene Arbeit.

---

## 3. Play-Store-Richtlinien

### Was passt

- **Ziel-API 35** — erfüllt die aktuelle Anforderung für neue Apps.
- **Werbung**: ausschließlich freiwillige Videoanzeigen. Keine Banner, keine
  Unterbrechungen, nichts, was von selbst erscheint. Nichts davon ist eine
  Richtlinienfrage; es ist die zurückhaltendste Form, die es gibt.
- **Einwilligung**: Googles zertifiziertes Werkzeug ist eingebunden und wird vor
  der ersten Anzeige gezeigt. Zurücknehmen geht in den App-Einstellungen.
- **Inhaltsfreigabe „G"**: Glücksspiel-, Gewalt- und sexuelle Werbeinhalte sind
  auf Anzeigenebene ausgeschlossen.
- **Zielgruppe ab 13**, ausdrücklich als *nicht* an Kinder gerichtet angemeldet
  — im Code, in den AGB und in der Datenschutzerklärung übereinstimmend.
- **Rechtstexte** vollständig, ohne Platzhalter, mit erreichbarer Webseite.
- Keine Käufe im Spiel, keine Nutzerkonten, keine nutzergenerierten Inhalte,
  keine Kommunikation zwischen Spielern — damit entfallen die Richtlinien, an
  denen die meisten Apps hängenbleiben.

### Was noch offen ist

1. **Die Test-Kennungen von Google stehen noch im Code.** App-ID im
   `AndroidManifest.xml`, Anzeigenblock-ID im `RewardedAdManager`. Damit darf
   nicht veröffentlicht werden.

2. **Im AdMob-Konto muss eine Einwilligungsnachricht angelegt und
   veröffentlicht sein**, unter den EU-Einstellungen. Sonst zeigt die App den
   Dialog an, bekommt aber keinen Inhalt dafür.

3. **Die Datenschutz-Seite muss unter der angegebenen Adresse erreichbar sein**,
   bevor eingereicht wird. Google ruft sie ab.

---

## Was heute behoben wurde

| Befund | Folge, wenn ungefixt |
| --- | --- |
| Fremde Tondatei ohne belegbare Rechte | Urheberrechtliches Restrisiko |
| Datenschutzerklärung verschwieg die Cloud-Sicherung | Falsche Angabe gegenüber Nutzern und im Fragebogen |
| Funkloch schaltete Werbung dauerhaft ab | Verlorene Einnahmen |
| Schriftlizenz nicht mitgeliefert | Verstoß gegen die OFL |
| Uhr lief während der Werbung weiter | Spieler verliert das Level, während er Werbung ansieht |

---

## Bewertung

Aus meiner Sicht ist die App aus Nutzersicht unbedenklich und aus Rechtesicht
sauber. Was bleibt, sind drei Handgriffe im AdMob-Konto und auf der Webseite —
keine Programmierarbeit.

Ein Vorbehalt, der genannt gehört: Ich habe die App geprüft, nicht juristisch
begutachtet. Die Aussagen zu Urheberrecht und Store-Richtlinien beruhen darauf,
was im Projekt nachweisbar ist — nicht auf einer Rechtsberatung.
