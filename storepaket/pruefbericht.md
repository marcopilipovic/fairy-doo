# Fairydoku — Prüfbericht vor der Veröffentlichung

Stand: 29. August 2026, geprüft an der Release-APK (versionName 0.7.1,
versionCode 18, 4.175.734 Byte) auf einem Pixel-5-Emulator mit Android 15 und,
seit der Zusammenführung, laufend auf den Telefonen der Testrunde. Die erste
Fassung dieses Berichts stammt vom 19. August; was seither dazukam, steht unten
in der Tabelle.

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
`ACCESS_ADSERVICES_TOPICS`, `WAKE_LOCK`, `FOREGROUND_SERVICE`. Dazu kommt
`com.fairydoo.game.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` — die meldet die
App sich selbst; AndroidX benutzt sie, um interne Empfänger gegen fremde Apps
abzuriegeln. Sie ist eine Schutzmaßnahme, kein Zugriff.

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

**Die App belegt damit rund 11 MB.** Zum Vergleich: Die Installationsdatei
allein ist 4,1 MB — gut ein Fünftel davon ist die Waldmusik.

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
  signiert. Nachgeprüft am fertigen Paket:

| | |
| --- | --- |
| Signatur | gültig, ein Unterzeichner, Schema v2 |
| Schlüssel | RSA 4096 Bit, SHA384withRSA |
| Gültig bis | 22. Dezember 2053 — Google verlangt mindestens 2033 |
| SHA-256 | `75f99f…d87d3ef4` |

### Batterie

Seit dem 28. August läuft überhaupt keine Uhr mehr: Der Countdown je Level ist
gestrichen, und mit ihm der Takt, der bis dahin 16-ms-weise weiterlief. Was
bleibt, ist die Musik, und die hält beim Wechsel in den Hintergrund an — auch
während einer Werbeanzeige. Keine Hintergrunddienste, keine Weckzeiten, keine
Standortabfragen.

---

## 2. Urheberrecht

**Nach dem heutigen Durchgang: nichts Fremdes mehr im Spiel, außer zwei
Schriften unter freier Lizenz.**

| Was | Herkunft | Bewertung |
| --- | --- | --- |
| Die zehn Feen | Handoff „Feen schlicht", eigene Arbeit | eigen |
| App-Symbol, Store-Bilder | aus derselben Zeichnung erzeugt | eigen |
| Waldmusik, Schreckenslaut, sechs Kicherlaute, vier Klänge im Spielverlauf | ElevenLabs, Tarif Starter | lizenziert, gewerblich |
| Alle übrigen Klänge | im Spiel berechnet | eigen, keine Aufnahme |
| Cinzel Decorative, Quicksand | Google Fonts | **SIL OFL 1.1** |
| Bibliotheken | AndroidX, Google, JetBrains | Apache 2.0 / Play-Bedingungen |

**Nachtrag vom 28. und 29. August:** Acht Aufnahmen sind zurück in der App — die
Waldmusik (`ambient_forest.mp3`, 821 KB), der Schreckenslaut
(`fairy_startled.mp3`, 6 KB), die sechs Kicherlaute der Feen (je 6 KB) und der
drei Ausschnitte aus einem zwanzig Sekunden langen Stück
(`level_complete.mp3` 35 KB, `ward.mp3` 3 KB, `undo.mp3` 5 KB). Nur das
Spielende wird noch berechnet.

Acht der neun stammen vom 1. August und damit aus demselben Vorgang; der
Levelbeginn ist am **30. August 2026** dazugekommen, unter demselben Konto und
demselben Tarif. Die Vorlage dazu liegt ungekürzt unter `Audio/`.

Beide waren im August entfernt worden, weil hier stand, ihre Rechtelage sei
nicht zu belegen. Das war zu vorsichtig formuliert — es war eine offene Frage,
keine Absage. Erzeugt wurden sie am **1. August 2026 zwischen 5:56 und 12:53
Uhr** mit ElevenLabs, und zwar unter einem bezahlten Tarif; die Zeitstempel
stehen in den Dateinamen unter `Audio/`.

**Der Tarif ist belegt (29. August 2026).** Im ElevenLabs-Konto steht *Starter*
als aktueller Plan, 6 USD im Monat. Sein Leistungsumfang nennt ausdrücklich:

> Kommerzielle Lizenz für Sprache und Musik

Damit ist die Nutzung in einer verkauften oder werbefinanzierten App gedeckt —
und zwar für beides, was hier verwendet wird: die Musik und den Laut.

✅ **Auch die Reihenfolge stimmt.** Das Abonnement wurde abgeschlossen und
*danach* wurden die Klänge erzeugt — nicht umgekehrt, und zu keinem Zeitpunkt
mit einer kostenlosen Testfassung. So von Nataly bestätigt, deren Mann das Konto
führt.

Damit ist der Punkt erledigt. Die Aufnahmen sind unter einem Tarif entstanden,
der die gewerbliche Nutzung ausdrücklich einschließt.

Wer es je genauer braucht — etwa weil jemand von außen fragt —, findet den
Beleg im Zahlungsverlauf des ElevenLabs-Kontos: eine Rechnung, deren Zeitraum
den 1. August 2026 einschließt.

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

- **Ziel-API 36** — Google Play verlangt sie ab dem 31. August 2026 für neue
  Apps. Der Schritt ist gemacht, samt der Folge daraus: Android 16 beachtet auf
  großen Bildschirmen die Festlegung auf Hochformat nicht mehr, das Brett rechnet
  seine Größe deshalb aus dem Platz, der ihm bleibt.
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

## Was behoben wurde

**Am 19. August, beim ersten Durchgang:**

| Befund | Folge, wenn ungefixt |
| --- | --- |
| Fremde Tondatei ohne belegbare Rechte | Urheberrechtliches Restrisiko |
| Datenschutzerklärung verschwieg die Cloud-Sicherung | Falsche Angabe gegenüber Nutzern und im Fragebogen |
| Funkloch schaltete Werbung dauerhaft ab | Verlorene Einnahmen |
| Schriftlizenz nicht mitgeliefert | Verstoß gegen die OFL |
| Uhr lief während der Werbung weiter | Spieler verliert das Level, während er Werbung ansieht |

**Seither:**

| Befund | Folge, wenn ungefixt | Wann |
| --- | --- | --- |
| Eine Anzeige, die nicht kam, meldete sich nicht zurück — das Spiel blieb stehen | Nur noch mit einem Neustart der App zu lösen | 28. Aug. |
| `targetSdk` stand auf 35 | Google Play nimmt ab dem 31. August 2026 keine neue App mehr an | 25. Aug. |
| Die Lizenzen der fremden Bestandteile fehlten in der App | Verstoß gegen OFL 1.1 und Apache 2.0 — jetzt eine eigene Seite im Wortlaut | 28. Aug. |
| Eine Fee hieß „Flora" | Eingetragene Marke in mehreren Klassen; heißt jetzt Viridis | 25. Aug. |
| Die AGB trugen eine Haftungsklausel | Gegenüber Verbrauchern in Teilen unwirksam, und für ein kostenloses Spiel überflüssig | 24. Aug. |
| Die Rechtstexte liefen in der App ohne Absätze durch | Unlesbar auf dem Telefon — jetzt aus derselben Quelle gegliedert wie die Webseite | 24. Aug. |
| Zwei Fassungen des Spiels liefen drei Wochen nebeneinander | Dieselbe Arbeit zweimal; jetzt gibt es nur noch `main` | 28. Aug. |
| Die berechnete Musik gefiel der Testrunde nicht | — kein Fehler, aber der Grund, die zwei Aufnahmen zurückzuholen | 28. Aug. |
| Die Spieluhr | Sie bestrafte das Nachdenken, für das das Spiel gemacht ist; ersatzlos gestrichen | 28. Aug. |
| Drei Bildschirmfotos zeigten die Uhr noch | Bilder, die etwas zeigen, was es nicht gibt | 29. Aug. |
| Der gesprochene Lobsatz nach jedem Level | Er stand dem Weiterspielen im Weg; mit ihm ist die Sprachausgabe ganz aus der App verschwunden | 29. Aug. |

---

## Bewertung

Aus meiner Sicht ist die App aus Nutzersicht unbedenklich und aus Rechtesicht
sauber. Am Quelltext und an den Texten ist nichts mehr zu tun; was bleibt, sind
fünf Handgriffe außerhalb des Projekts:

1. Echte AdMob-Kennungen eintragen (Manifest und `RewardedAdManager`).
2. Einwilligungsnachricht im AdMob-Konto anlegen und veröffentlichen.
3. Datenschutz-Seite unter der angegebenen Adresse erreichbar machen.
4. Signierschlüssel und `keystore.properties` an einem zweiten Ort sichern.
5. Eine Viertelstunde Markenrecherche bei DPMAregister und TMview.

Nur der vierte Punkt ist unwiederbringlich, wenn er ausbleibt. Die anderen vier
lassen sich jederzeit nachholen — der erste allerdings *muss* vor dem Einreichen
erledigt sein, sonst läuft die App mit Googles Testanzeigen.

Ein Vorbehalt, der genannt gehört: Ich habe die App geprüft, nicht juristisch
begutachtet. Die Aussagen zu Urheberrecht und Store-Richtlinien beruhen darauf,
was im Projekt nachweisbar ist — nicht auf einer Rechtsberatung.
