# Fairydoku — alles für die Veröffentlichung

Stand: 19. August 2026. Alles in diesem Ordner ist fertig zum Verwenden, außer
dem, was unter „Was noch fehlt" steht.

---

## Für den Play Store

`play-store/`

| Datei | Wofür | Googles Anforderung |
| --- | --- | --- |
| `symbol-512x512.png` | App-Symbol im Store | 512 × 512 PNG ✓ |
| `feature-grafik-1024x500.png` | Kopfbild des Eintrags | 1024 × 500 ✓ |
| `bildschirmfotos/` | fünf Bilder, in der Reihenfolge der Dateinamen hochladen | mind. 2, höchstens 8 ✓ |
| `texte.md` | Name, Kurz- und Vollbeschreibung, Kategorie | — |
| `datensicherheit-und-einstufung.md` | Vorschläge für die beiden Fragebögen | — |

Die Bildschirmfotos sind 1080 × 2090, aus der Release-APK auf einem Pixel-5-
Emulator aufgenommen, ohne Statusleiste und Navigationsleiste. Die ersten
beiden erscheinen in der Suchliste, oft ohne dass jemand den Eintrag öffnet —
deshalb stehen Spielbrett und Feenpfad vorn.

## Für die Webseite

`webseite/`

| Datei | Wofür |
| --- | --- |
| `rechtstexte.html` | fertige Seite, hell und dunkel, ohne fremde Abhängigkeiten |
| `rechtstexte.md` | dieselben Texte als Markdown, falls die Seite anders gebaut wird |

Die Seite enthält Impressum, AGB und Datenschutzerklärung mit den echten
Angaben der App HUMB UG.

**Die Adresse dieser Seite gehört in den Play-Store-Eintrag**, und sie muss
erreichbar sein, bevor die App eingereicht wird — Google ruft sie ab und lehnt
sonst ab.

### Wichtig: nicht von Hand ändern

Die Texte stammen aus dem App-Quelltext (`GameCopy.legalBody`). Ein Unit-Test
schreibt sie heraus, daraus entsteht diese Seite. Zwei Fassungen, die von Hand
gepflegt werden, laufen früher oder später auseinander — meist unbemerkt, weil
niemand beide nebeneinander liest.

Wer die Texte ändern will, ändert sie **im Code**. Danach:

```
gradlew.bat testDebugUnitTest --tests "*RechtstexteExportTest*"
```

Der Test prüft dabei auch, dass keine Platzhalter übrig sind — ein
`[Firmenname]` im veröffentlichten Impressum wäre ein Abmahngrund.

## Die App

`Fairydoku-2026-08-19.apk` — signierter Release-Build, 3,2 MB.

Zum Ausprobieren auf einem echten Telefon. **Nicht diese Datei hochladen**: Der
Play Store will ein App-Bundle (`.aab`), und vorher müssen die echten
AdMob-Kennungen drin sein.

---

## Was noch fehlt

1. **Die echten AdMob-Kennungen.** Im Code stehen noch Googles Test-IDs, an
   zwei Stellen:
   - `app/src/main/AndroidManifest.xml` — die App-ID (mit Tilde)
   - `app/src/main/java/com/fairydoo/game/ads/RewardedAdManager.kt` — die
     Anzeigenblock-ID (mit Schrägstrich)

   Mit Test-IDs darf die App nicht veröffentlicht werden.

2. **Die Einwilligungsnachricht im AdMob-Konto.** Die App fragt die
   Einwilligung über Googles User Messaging Platform ab. Damit dort etwas
   erscheint, muss im AdMob-Konto unter den EU-Einstellungen eine Nachricht
   angelegt und veröffentlicht sein.

3. **Eine rechtliche Freigabe der Texte.** Sie sind vollständig und beschreiben
   die App wahrheitsgemäß, aber niemand mit Rechtskenntnis hat sie gelesen.

4. **Das App-Bundle.** Sobald 1 und 2 stehen:

   ```
   gradlew.bat bundleRelease
   ```

   Das Ergebnis liegt dann unter `app/build/outputs/bundle/release/`.
