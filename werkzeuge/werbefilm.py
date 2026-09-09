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
# Nicht jede Szene braucht eine Zeile. Das Brett am Anfang und das Lösen am
# Ende zeigen sich selbst; wo staendig Schrift steht, liest man keine mehr.
TEXTE = {
    "kreuze": "Kurz tippen: keine Fee",
    "fee": "Halten: hier wohnt eine",
    "kreis-wirkt": "Der Feenkreis\nkreuzt selbst an",
    "geschafft": "Gelöst — ohne Uhr",
    "grosses-gitter": "Alle zwei Level\nwächst der Wald",
}

# Der Aufpopper: erst zu gross, dann eine Spur zu klein, dann sitzt es. Drei
# Bilder je Stufe, zusammen ein Zehntel Sekunde. ffmpeg kann die Schriftgroesse
# nicht ueber die Zeit rechnen (drawtext kennt hier kein `eval`), deshalb liegt
# jede Stufe als eigenes Bild vor und wird nacheinander eingeblendet.
STUFEN = (1.18, 0.94, 1.0)
STUFEN_BILDER = 3
GROESSE = 74

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


def zeilenbild(text, groesse, ziel):
    """Malt eine Zeile auf durchsichtigen Grund und legt sie als PNG ab."""
    t = text.replace("'", "’").replace(":", r"\:").replace("%", r"\%")
    hoehe = int(groesse * 3.4)
    lauf(
        f"ffmpeg -v error -y -f lavfi -i \"color=c=black@0:s={BREITE}x{hoehe}:d=1,format=rgba\" "
        f"-vf \"drawtext=fontfile='{SCHRIFT_ZEILE}':text='{t}':fontcolor=0xFFE9A8"
        f":fontsize={groesse}:x=(w-text_w)/2:y=(h-text_h)/2:line_spacing={int(groesse * 0.45)}"
        f":shadowcolor=0x05060F@0.9:shadowx=0:shadowy=6:borderw=5:bordercolor=0x05060F@0.6\" "
        f"-frames:v 1 {ziel}"
    )
    return hoehe


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

    # Jede Zeile kommt als eigenes Bild ins Spiel — einmal je Stufe des
    # Aufpoppers. Ueberlagert wird mittig, ueber allem, was das Spiel zeigt.
    bilderordner = ZIEL.parent / "zeilen"
    bilderordner.mkdir(exist_ok=True)
    eingaben, ueberlagerungen = [], []
    strom = 2  # 0 = Hintergrund, 1 = Bildfolge
    ketten = ""
    for (name, von), (_, bis) in zip(szenen, szenen[1:]):
        if name not in TEXTE:
            continue
        beginn = von / FPS + 0.12
        ende = bis / FPS - 0.15
        stufen_dauer = STUFEN_BILDER / FPS
        for i, faktor in enumerate(STUFEN):
            datei = bilderordner / f"{name}-{i}.png"
            zeilenbild(TEXTE[name], int(GROESSE * faktor), datei)
            # Das Standbild muss den ganzen Film ueber bereitstehen: Der
            # Ueberlagerer nimmt sein Bild zur Zeit des Hauptstroms, und ein
            # Standbild, das vorher endet, ist im Fenster einfach nicht da.
            # Sichtbar wird es erst durch `enable`.
            eingaben.append(f"-loop 1 -t {spielzeit:.2f} -i {datei}")
            ab = beginn + i * stufen_dauer
            bis_hier = (beginn + (i + 1) * stufen_dauer) if i < len(STUFEN) - 1 else ende
            # Die letzte Stufe bleibt stehen und geht weich wieder weg.
            if i == len(STUFEN) - 1:
                ketten += (f"[{strom}:v]format=rgba,fade=t=in:st={ab:.2f}:d=0.12:alpha=1,"
                           f"fade=t=out:st={max(0.2, ende - 0.35):.2f}:d=0.35:alpha=1[z{strom}];")
                ueberlagerungen.append((strom, ab, bis_hier))
            else:
                ketten += f"[{strom}:v]format=rgba[z{strom}];"
                ueberlagerungen.append((strom, ab, bis_hier))
            strom += 1

    # Die Kette der Ueberlagerungen: mittig, jede nur in ihrem Zeitfenster.
    vorher = "[gelegt]"
    schritte = []
    for nummer, (idx, ab, bis_hier) in enumerate(ueberlagerungen):
        marke_aus = f"[u{nummer}]" if nummer < len(ueberlagerungen) - 1 else "[v]"
        schritte.append(
            f"{vorher}[z{idx}]overlay=(W-w)/2:(H-h)/2-60"
            f":enable='between(t,{ab:.2f},{bis_hier:.2f})'"
            + marke_aus
        )
        vorher = marke_aus
    if not schritte:
        schritte = ["[gelegt]format=yuv420p[v]"]
        filter_video = aufbau + ";" + ";".join(schritte)
    else:
        filter_video = aufbau + ";" + ketten + ";".join(schritte)
        # Am Ende noch das Format fuer den Kodierer.
        filter_video = filter_video.replace(marke_aus, "[vmix]") + ";[vmix]format=yuv420p[v]"

    # Ton: Musik unter allem, Kichern bei den Feen, Jubel beim Gewinn.
    quellen = [f"-i {KLANG / 'ambient_forest.mp3'}"]
    ton0 = strom  # die Tonspuren kommen hinter den Zeilenbildern
    mische = [f"[{ton0}:a]atrim=0:{spielzeit + 4:.2f},volume=0.5,"
              f"afade=t=in:st=0:d=2,afade=t=out:st={spielzeit + 1.5:.2f}:d=2.5[m]"]
    namen = ["[m]"]
    n = ton0 + 1
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
         + " ".join(eingaben) + " "
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
