#!/usr/bin/env python3
"""Setzt aus den gerechneten Bildern den Werbefilm zusammen.

Die Aufnahme steht in `app/src/test/java/ug/humb/fairydoku/film/WerbefilmTest.kt`
— sie zeichnet die laufende App Bild fuer Bild, ohne Telefon und ohne Emulator:

    ./gradlew testDebugUnitTest --tests '*WerbefilmTest*' -Dwerbefilm=ja
    python3 werkzeuge/werbefilm.py

Hier kommt nur noch dazu, was kein Bildschirm hergibt: die Schrift, die sagt,
was gerade passiert, ein Schlussbild und der Ton. **Gesprochen wird nichts.**
Die Stimme aus der ersten Fassung ist am 9. September 2026 herausgeflogen — sie
passte zu einem Buehnen-Oger, nicht zu einem Feenwald, und in den sozialen
Netzen laeuft der Ton bei den meisten ohnehin nicht mit.

Der Ton kommt aus dem Spiel selbst: die Waldmusik als Bett, das Kichern beim
Setzen, der Jubel beim geloesten Level. Damit bleibt die Rechtefrage
geschlossen — es ist dieselbe ElevenLabs-Lizenz wie in der App.
"""

import json
import subprocess
import sys
from pathlib import Path

WURZEL = Path(__file__).resolve().parent.parent
BILDER = WURZEL / "app" / "build" / "werbefilm" / "bilder"
MARKEN = WURZEL / "app" / "build" / "werbefilm" / "marken.txt"
KLANG = WURZEL / "app" / "src" / "main" / "res" / "raw"
SCHRIFT_TITEL = WURZEL / "app" / "src" / "main" / "res" / "font" / "cinzel_decorative_black.ttf"
SCHRIFT_TEXT = WURZEL / "app" / "src" / "main" / "res" / "font" / "quicksand_variable.ttf"
SCHRIFT_ZEILE = WURZEL / "app" / "src" / "main" / "res" / "font" / "cinzel_decorative_bold.ttf"
ZIEL = WURZEL / "app" / "build" / "werbefilm" / "Fairydoku-Werbefilm.mp4"

FPS = 30
BREITE, HOEHE = 1080, 1920
GOLD, HELL = "0xFFD76B", "0xF2EFFA"

# Was in welcher Szene unten steht. Der Schluessel ist die Marke aus dem Test —
# so wandert die Schrift mit, wenn sich der Ablauf aendert.
TEXTE = {
    "brett": "Sechs Feen suchen ihren Platz",
    "kreuze": "Kurz tippen: hier wohnt keine",
    "fee": "Halten: hier wohnt eine",
    "kreis-an": "Der Feenkreis brennt",
    "kreis-wirkt": "Jede Fee kreuzt selbst an,\nwas sie ausschließt",
    "loesen": "Eine je Reihe, Spalte und Zone —\nund keine berührt die andere",
    "geschafft": "Gelöst. Ohne Uhr, ohne Eile",
    "grosses-gitter": "Alle zwei Level wächst der Wald",
}

# Sechs Kichern liegen im Spiel, und im Spiel wuerfelt es sie. Im Film gehen
# sie der Reihe nach durch — immer dasselbe klingt nach Schleife, und genau das
# ist beim ersten Ansehen aufgefallen.
KICHERN = [f"fairy_giggle_{i}.mp3" for i in range(1, 7)]


def lauf(befehl):
    ergebnis = subprocess.run(befehl, shell=True, capture_output=True, text=True)
    if ergebnis.returncode:
        print(ergebnis.stderr[-1500:], file=sys.stderr)
        raise SystemExit(f"Abgebrochen: {befehl[:90]}")


def marken():
    if not MARKEN.exists():
        raise SystemExit("Keine Marken — erst den Test mit -Dwerbefilm=ja laufen lassen.")
    paare = []
    for zeile in MARKEN.read_text(encoding="utf-8").splitlines():
        if not zeile.strip():
            continue
        bild, name = zeile.split("\t")
        paare.append((name, int(bild)))
    return paare


def schrift(text, von, bis, groesse=44):
    """Eine Zeile, die ins Bild kommt und wieder geht.

    Kein Kasten mehr. Ein Balken unter der Schrift macht aus einem Film eine
    Bedienungsanleitung — und er deckt genau das zu, was man sehen soll. Statt
    dessen dieselbe geschwungene Schrift wie im Titel des Spiels, in Goldcreme,
    mit einem weichen dunklen Schatten darunter. Der Schatten ist das, was sie
    ueber jedem Untergrund lesbar macht, ohne etwas zu verdecken.

    Dazu steigt die Zeile beim Erscheinen ein Stueck auf und faellt beim Gehen
    wieder zurueck — zwanzig Bildpunkte, kaum bewusst zu bemerken. Genau
    deshalb wirkt sie gesetzt statt eingeblendet.
    """
    t = text.replace("'", "’").replace(":", r"\:").replace("%", r"\%")
    ein, aus = 0.5, 0.45
    alpha = (f"if(lt(t,{von}),0,if(lt(t,{von + ein}),(t-{von})/{ein},"
             f"if(lt(t,{bis - aus}),1,if(lt(t,{bis}),({bis}-t)/{aus},0))))")
    steigen = f"h*0.605-18*min(1,max(0,(t-{von})/{ein}))"
    return (f"drawtext=fontfile='{SCHRIFT_ZEILE}':text='{t}':fontcolor=0xFFE9A8"
            f":fontsize={groesse}:x=(w-text_w)/2:y='{steigen}':line_spacing=22"
            f":shadowcolor=0x05060F@0.85:shadowx=0:shadowy=5"
            f":borderw=4:bordercolor=0x05060F@0.55:alpha='{alpha}'")


def main():
    bilder = sorted(BILDER.glob("*.png"))
    if not bilder:
        raise SystemExit("Keine Bilder — erst den Test mit -Dwerbefilm=ja laufen lassen.")
    anzahl = len(bilder)
    spielzeit = anzahl / FPS
    liste = marken() + [("ende", anzahl)]

    # Das Spielbild fuellt das ganze Bild — die Schrift liegt darauf.
    aufbau = f"[1:v]scale={BREITE}:{HOEHE}[gelegt]"

    # Fuer die Schrift zaehlen nur die Szenen. Die Marken der einzelnen Feen
    # sind Tonspuren-Marken; stuenden sie hier mit drin, endete eine Zeile in
    # dem Augenblick, in dem sie beginnt — und waere nie zu sehen.
    szenen = [(name, bild) for name, bild in liste if name in TEXTE or name == "ende"]
    texte = []
    for (name, von), (_, bis) in zip(szenen, szenen[1:]):
        if name not in TEXTE:
            continue
        texte.append(schrift(TEXTE[name], von / FPS + 0.15, bis / FPS - 0.1))
    filter_video = aufbau + ";[gelegt]" + ",".join(texte) + ",format=yuv420p[v]"

    # Ton: Musik unter allem, Kichern bei den Feen, Jubel beim Gewinn.
    quellen = [f"-i {KLANG / 'ambient_forest.mp3'}"]
    mische = [f"[2:a]atrim=0:{spielzeit + 4:.2f},volume=0.5,"
              f"afade=t=in:st=0:d=2,afade=t=out:st={spielzeit + 1.5:.2f}:d=2.5[m]"]
    namen = ["[m]"]
    n = 3
    kicher = 0
    for name, bild in liste:
        if name == "fee-gesetzt":
            quellen.append(f"-i {KLANG / KICHERN[kicher % len(KICHERN)]}")
            ms = int(bild / FPS * 1000) + 120
            # Jedes Kichern eine Spur anders laut — sechs gleich laute
            # hintereinander klingen wieder nach Wiederholung.
            laut = 0.62 + 0.06 * (kicher % 3)
            mische.append(f"[{n}:a]adelay={ms}|{ms},volume={laut:.2f}[k{n}]")
            namen.append(f"[k{n}]")
            n += 1
            kicher += 1
        if name == "geschafft":
            quellen.append(f"-i {KLANG / 'level_complete.mp3'}")
            ms = int(bild / FPS * 1000)
            mische.append(f"[{n}:a]adelay={ms}|{ms},volume=0.9[j{n}]")
            namen.append(f"[j{n}]")
            n += 1
    filter_ton = ";".join(mische) + ";" + "".join(namen) + \
        f"amix=inputs={len(namen)}:duration=first:dropout_transition=0:normalize=0[a]"

    hintergrund = (f"-f lavfi -t {spielzeit:.2f} -i \"gradients=s={BREITE}x{HOEHE}"
                   f":c0=0x0A0E21:c1=0x241A52:x0=0:y0=0:x1={BREITE}:y1={HOEHE}"
                   f":n=2:speed=0.004:r={FPS}\"")

    teil = ZIEL.parent / "teil-spiel.mp4"
    lauf(f"ffmpeg -v error -y {hintergrund} -framerate {FPS} -i {BILDER}/%05d.png "
         + " ".join(quellen)
         + f" -filter_complex \"{filter_video};{filter_ton}\" -map \"[v]\" -map \"[a]\" "
         f"-c:v libx264 -crf 20 -preset medium -pix_fmt yuv420p -r {FPS} "
         f"-c:a aac -b:a 160k -t {spielzeit:.2f} {teil}")

    # Das Schlussbild — dieselbe Schrift wie im Spiel.
    schluss = ZIEL.parent / "teil-schluss.mp4"
    dauer = 4.0
    karte = ",".join([
        (f"drawtext=fontfile='{SCHRIFT_TITEL}':text='FAIRYDOKU':fontcolor={GOLD}"
         f":fontsize=112:x=(w-text_w)/2:y=760:alpha='if(lt(t,0.6),t/0.6,1)'"),
        (f"drawtext=fontfile='{SCHRIFT_TEXT}':text='Ein Feenwald voller Logik'"
         f":fontcolor={HELL}:fontsize=52:x=(w-text_w)/2:y=930:alpha='if(lt(t,0.9),max(0,(t-0.3)/0.6),1)'"),
        (f"drawtext=fontfile='{SCHRIFT_TEXT}':text='Keine Uhr. Keine Käufe.'"
         f":fontcolor={GOLD}:fontsize=46:x=(w-text_w)/2:y=1090:alpha='if(lt(t,1.4),max(0,(t-0.8)/0.6),1)'"),
        "format=yuv420p",
    ])
    lauf(f"ffmpeg -v error -y -f lavfi -t {dauer} -i \"gradients=s={BREITE}x{HOEHE}"
         f":c0=0x0A0E21:c1=0x241A52:x0=0:y0=0:x1={BREITE}:y1={HOEHE}:n=2:speed=0.004:r={FPS}\" "
         f"-f lavfi -t {dauer} -i anullsrc=r=48000:cl=stereo "
         f"-vf \"{karte}\" -c:v libx264 -crf 20 -pix_fmt yuv420p -r {FPS} -c:a aac -b:a 160k {schluss}")

    lauf(f"ffmpeg -v error -y -i {teil} -i {schluss} -filter_complex "
         f"\"[0:v][1:v]xfade=transition=fade:duration=0.7:offset={spielzeit - 0.7:.2f}[v];"
         f"[0:a][1:a]acrossfade=d=0.7[a]\" -map \"[v]\" -map \"[a]\" "
         f"-c:v libx264 -crf 20 -preset medium -pix_fmt yuv420p -r {FPS} -c:a aac -b:a 160k "
         f"-movflags +faststart {ZIEL}")

    groesse = ZIEL.stat().st_size / 1_000_000
    print(f"Fertig: {ZIEL} — {anzahl} Bilder, {spielzeit + dauer - 0.7:.1f} s, {groesse:.1f} MB")


if __name__ == "__main__":
    main()
