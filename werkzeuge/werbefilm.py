#!/usr/bin/env python3
"""Setzt aus den gerechneten Bildern den Werbefilm zusammen.

Die Aufnahme steht in `app/src/test/java/ug/humb/fairydoku/film/WerbefilmTest.kt`
— sie zeichnet die laufende App Bild fuer Bild, ohne Telefon und ohne Emulator:

    ./gradlew testDebugUnitTest --tests '*WerbefilmTest*' -Dwerbefilm=ja
    python3 werkzeuge/werbefilm.py

**Der Aufbau, und warum er dreimal umgeworfen wurde.** Zuerst lief die Schrift
in einem Balken unter dem Bild — das sieht aus wie eine Bedienungsanleitung.
Dann lag sie auf dem Spielfeld — und deckte sekundenlang genau das zu, was sie
erklaerte. Jetzt bekommt sie ihren eigenen Augenblick: **Zwischentitel**, wie im
Stummfilm. Eine Karte sagt, was gleich passiert, dann zeigt es das Spiel — ohne
ein Wort im Bild.

Das loest drei Dinge auf einmal: Das Brett ist nie verdeckt, die Schrift steht
nur so lange, wie man zum Lesen braucht, und der Film bekommt einen Takt statt
eines Dauerlaufs.

**Gesprochen wird nichts.** Der Ton kommt aus dem Spiel selbst: Waldmusik als
Bett, das Kichern beim Setzen, der Jubel beim geloesten Level. Damit bleibt die
Rechtefrage geschlossen — dieselbe ElevenLabs-Lizenz wie in der App.
"""

import subprocess
import sys
from pathlib import Path

WURZEL = Path(__file__).resolve().parent.parent
BILDER = WURZEL / "app" / "build" / "werbefilm" / "bilder"
MARKEN = WURZEL / "app" / "build" / "werbefilm" / "marken.txt"
KLANG = WURZEL / "app" / "src" / "main" / "res" / "raw"
SCHRIFT_TITEL = WURZEL / "app" / "src" / "main" / "res" / "font" / "cinzel_decorative_black.ttf"
SCHRIFT_ZEILE = WURZEL / "app" / "src" / "main" / "res" / "font" / "cinzel_decorative_bold.ttf"
ARBEIT = WURZEL / "app" / "build" / "werbefilm" / "teile"
ZIEL = WURZEL / "app" / "build" / "werbefilm" / "Fairydoku-Werbefilm.mp4"

FPS = 30
BREITE, HOEHE = 1080, 1920
GOLD, CREME = "0xFFD76B", "0xFFE9A8"
BLENDE = 0.45

# Der Ablauf: Karten und Szenen im Wechsel. Eine Szene nennt ihre Marken aus
# dem Test — von der ersten bis vor die naechste.
ABLAUF = [
    ("karte", "Ein Feenwald\nvoller Logik", 2.0),
    ("szene", "brett", "kreuze"),
    ("karte", "Kurz tippen:\nhier wohnt keine", 1.8),
    ("szene", "kreuze", "fee"),
    ("karte", "Halten:\nhier wohnt eine", 1.8),
    ("szene", "fee", "kreis-an"),
    ("karte", "Der Feenkreis\nkreuzt selbst an", 2.0),
    ("szene", "kreis-an", "loesen"),
    ("szene", "loesen", "grosses-gitter", 5.0),
    ("karte", "Keine Uhr.\nKeine Käufe.", 1.8),
    ("szene", "grosses-gitter", "ende"),
]

KICHERN = [f"fairy_giggle_{i}.mp3" for i in range(1, 7)]


def lauf(befehl):
    ergebnis = subprocess.run(befehl, shell=True, capture_output=True, text=True)
    if ergebnis.returncode:
        print(ergebnis.stderr[-1200:], file=sys.stderr)
        raise SystemExit(f"Abgebrochen: {befehl[:100]}")


def marken():
    if not MARKEN.exists():
        raise SystemExit("Keine Marken — erst den Test mit -Dwerbefilm=ja laufen lassen.")
    liste = []
    for zeile in MARKEN.read_text(encoding="utf-8").splitlines():
        if zeile.strip():
            bild, name = zeile.split("\t")
            liste.append((name, int(bild)))
    return liste


def erste(liste, name, anzahl):
    """Das Bild, bei dem eine Marke zuerst steht. `ende` ist der Schluss."""
    if name == "ende":
        return anzahl
    for marke, bild in liste:
        if marke == name:
            return bild
    raise SystemExit(f"Marke {name} fehlt — Ablauf und Test passen nicht zusammen.")


def karte(text, dauer, ziel, gross=False):
    """Ein Zwischentitel: dunkler Grund, geschwungene Schrift, ruhig."""
    t = text.replace("'", "’").replace(":", r"\:").replace("%", r"\%")
    schrift = SCHRIFT_TITEL if gross else SCHRIFT_ZEILE
    groesse = 96 if gross else 78
    # Sanft auf und wieder ab, damit die Karte nicht schlaegt.
    alpha = f"if(lt(t,0.45),t/0.45,if(lt(t,{dauer - 0.45:.2f}),1,({dauer}-t)/0.45))"
    lauf(
        f"ffmpeg -v error -y -f lavfi -t {dauer} -i \"gradients=s={BREITE}x{HOEHE}"
        f":c0=0x090C1C:c1=0x1E1745:x0=0:y0=0:x1={BREITE}:y1={HOEHE}:n=2:speed=0.003:r={FPS}\" "
        f"-f lavfi -t {dauer} -i anullsrc=r=48000:cl=stereo "
        f"-vf \"drawtext=fontfile='{schrift}':text='{t}':fontcolor={CREME}:fontsize={groesse}"
        f":x=(w-text_w)/2:y=(h-text_h)/2:line_spacing=30:alpha='{alpha}',format=yuv420p\" "
        f"-c:v libx264 -crf 20 -pix_fmt yuv420p -r {FPS} -c:a aac -b:a 160k -shortest {ziel}"
    )


def schlusskarte(ziel, dauer):
    """Der Abspann: Name, Untertitel, und der Satz, der bleiben soll."""
    zeilen = [
        (SCHRIFT_TITEL, "FAIRYDOKU", 104, GOLD, 700, 0.2),
        (SCHRIFT_ZEILE, "Ein Feenwald voller Logik", 52, CREME, 880, 0.7),
        (SCHRIFT_ZEILE, "Bald im Google Play Store", 46, CREME, 1120, 1.3),
    ]
    male = []
    for schrift, text, groesse, farbe, y, ab in zeilen:
        t = text.replace("'", "’")
        alpha = (f"if(lt(t,{ab}),0,if(lt(t,{ab + 0.6}),(t-{ab})/0.6,"
                 f"if(lt(t,{dauer - 0.5:.2f}),1,({dauer}-t)/0.5)))")
        male.append(f"drawtext=fontfile='{schrift}':text='{t}':fontcolor={farbe}"
                    f":fontsize={groesse}:x=(w-text_w)/2:y={y}:alpha='{alpha}'")
    lauf(
        f"ffmpeg -v error -y -f lavfi -t {dauer} -i \"gradients=s={BREITE}x{HOEHE}"
        f":c0=0x090C1C:c1=0x1E1745:x0=0:y0=0:x1={BREITE}:y1={HOEHE}:n=2:speed=0.003:r={FPS}\" "
        f"-f lavfi -t {dauer} -i anullsrc=r=48000:cl=stereo "
        f"-vf \"{','.join(male)},format=yuv420p\" "
        f"-c:v libx264 -crf 20 -pix_fmt yuv420p -r {FPS} -c:a aac -b:a 160k -shortest {ziel}"
    )


def szene(von, bis, ziel):
    """Ein Stueck der Aufnahme — ohne ein Wort darauf."""
    anzahl = bis - von
    lauf(
        f"ffmpeg -v error -y -framerate {FPS} -start_number {von} -i {BILDER}/%05d.png "
        f"-f lavfi -t {anzahl / FPS:.2f} -i anullsrc=r=48000:cl=stereo "
        f"-frames:v {anzahl} -vf format=yuv420p "
        f"-c:v libx264 -crf 20 -preset medium -pix_fmt yuv420p -r {FPS} "
        f"-c:a aac -b:a 160k -shortest {ziel}"
    )
    return anzahl / FPS


def main():
    bilder = sorted(BILDER.glob("*.png"))
    if not bilder:
        raise SystemExit("Keine Bilder — erst den Test mit -Dwerbefilm=ja laufen lassen.")
    anzahl = len(bilder)
    liste = marken()
    ARBEIT.mkdir(parents=True, exist_ok=True)

    # Teile bauen und dabei mitschreiben, wann im fertigen Film welches
    # Aufnahme-Bild liegt — daran haengt spaeter der Ton.
    teile, dauern, versatz = [], [], []
    uhr = 0.0
    for nummer, eintrag in enumerate(ABLAUF):
        datei = ARBEIT / f"{nummer:02d}.mp4"
        if eintrag[0] == "karte":
            _, text, dauer = eintrag
            karte(text, dauer, datei)
            dauern.append(dauer)
        else:
            von_name, bis_name = eintrag[1], eintrag[2]
            grenze = eintrag[3] if len(eintrag) > 3 else None
            von = erste(liste, von_name, anzahl)
            bis = erste(liste, bis_name, anzahl)
            if grenze:
                bis = min(bis, von + int(grenze * FPS))
            dauer = szene(von, bis, datei)
            dauern.append(dauer)
            versatz.append((von, bis, uhr))
        teile.append(datei)
        uhr += dauer - (BLENDE if nummer else 0)

    schluss = ARBEIT / "99-schluss.mp4"
    schlusskarte(schluss, 4.6)
    teile.append(schluss)
    dauern.append(4.6)

    # Alles mit weichen Blenden aneinander.
    eingaben = " ".join(f"-i {t}" for t in teile)
    ketten, vorher, laufzeit = [], "[0:v]", 0.0
    for i in range(1, len(teile)):
        laufzeit += dauern[i - 1] - (BLENDE if i > 1 else 0)
        ziel_marke = f"[b{i}]" if i < len(teile) - 1 else "[v]"
        ketten.append(
            f"{vorher}[{i}:v]xfade=transition=fade:duration={BLENDE}"
            f":offset={laufzeit - BLENDE:.2f}{ziel_marke}"
        )
        vorher = ziel_marke
    gesamt = laufzeit - BLENDE + dauern[-1]

    stumm = ARBEIT / "stumm.mp4"
    lauf(f"ffmpeg -v error -y {eingaben} -filter_complex \"{';'.join(ketten)}\" -map \"[v]\" "
         f"-c:v libx264 -crf 20 -preset medium -pix_fmt yuv420p -r {FPS} {stumm}")

    # Der Ton, auf der neuen Zeitrechnung: Kichern dort, wo im fertigen Film
    # eine Fee gesetzt wird, Jubel beim geloesten Level.
    def zeitpunkt(bild):
        for von, bis, beginn in versatz:
            if von <= bild < bis:
                return beginn + (bild - von) / FPS
        return None

    quellen = [f"-i {KLANG / 'ambient_forest.mp3'}"]
    mische = [f"[1:a]atrim=0:{gesamt + 2:.2f},volume=0.62,afade=t=in:st=0:d=2.5,"
              f"afade=t=out:st={gesamt - 3:.2f}:d=3[m]"]
    namen, n, kicher = ["[m]"], 2, 0
    for name, bild in liste:
        wann = zeitpunkt(bild)
        if wann is None:
            continue
        if name == "fee-gesetzt":
            quellen.append(f"-i {KLANG / KICHERN[kicher % len(KICHERN)]}")
            ms = int(wann * 1000) + 150
            laut = 0.40 + 0.05 * (kicher % 3)
            mische.append(f"[{n}:a]adelay={ms}|{ms},volume={laut:.2f}[k{n}]")
            namen.append(f"[k{n}]")
            n += 1
            kicher += 1
        elif name == "geschafft":
            quellen.append(f"-i {KLANG / 'level_complete.mp3'}")
            ms = int(wann * 1000)
            mische.append(f"[{n}:a]adelay={ms}|{ms},volume=0.75[j{n}]")
            namen.append(f"[j{n}]")
            n += 1
    ton = ";".join(mische) + ";" + "".join(namen) + \
        f"amix=inputs={len(namen)}:duration=first:dropout_transition=0:normalize=0[a]"

    lauf(f"ffmpeg -v error -y -i {stumm} " + " ".join(quellen) +
         f" -filter_complex \"{ton}\" -map 0:v -map \"[a]\" -c:v copy "
         f"-c:a aac -b:a 160k -movflags +faststart -t {gesamt:.2f} {ZIEL}")

    print(f"Fertig: {ZIEL} — {anzahl} Bilder Spiel, {gesamt:.1f} s, "
          f"{ZIEL.stat().st_size / 1_000_000:.1f} MB")


if __name__ == "__main__":
    main()
