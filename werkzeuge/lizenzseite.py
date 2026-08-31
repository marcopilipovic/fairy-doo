"""Baut den Text der Lizenzseite aus den Originalquellen.

Die Copyright-Vermerke werden aus den Schriftdateien selbst gelesen, die
Lizenztexte von den Seiten der Lizenzgeber geholt. Nichts davon wird hier
abgetippt — bei einer Lizenz zählt jedes Wort, und Abtippen ist die
zuverlässigste Art, eines zu verlieren.

Umbrochen wird neu: Die Originale sind auf 70 Zeichen hart umbrochen, was auf
einem Telefon zu Treppen führt. Der Wortlaut bleibt dabei unangetastet, nur
die Zeilenenden fallen weg.
"""
import re
import struct
import pathlib

HIER = pathlib.Path(__file__).parent
SCHRIFTEN = HIER.parent / "app/src/main/res/font"


def copyright_aus(ttf):
    """Liest nameID 0 (Copyright) aus einer TrueType-Datei."""
    d = pathlib.Path(ttf).read_bytes()
    for i in range(struct.unpack(">H", d[4:6])[0]):
        off = 12 + i * 16
        if d[off:off + 4] == b"name":
            toff = struct.unpack(">I", d[off + 8:off + 12])[0]
            break
    else:
        return ""
    _, count, soff = struct.unpack(">HHH", d[toff:toff + 6])
    beste = ""
    for i in range(count):
        r = toff + 6 + i * 12
        pid, _, _, nid, ln, o = struct.unpack(">HHHHHH", d[r:r + 12])
        if nid != 0:
            continue
        roh = d[toff + soff + o: toff + soff + o + ln]
        try:
            s = roh.decode("utf-16-be") if pid == 3 else roh.decode("latin-1")
        except Exception:
            continue
        if len(s) > len(beste):
            beste = s
    return " ".join(beste.split())


# Ein nummerierter Abschnitt, dessen Überschrift und Rumpf in derselben Zeile
# stehen: "4. Redistribution. You may reproduce and distribute ..."
NUMMERIERT = re.compile(r"^(\d+\.\s+[A-Z][^.]{0,60}\.)\s+(.+)$", re.S)


def reflow(text):
    """Leerzeilen trennen Absätze; innerhalb eines Absatzes fällt der Umbruch weg."""
    raus = []
    for block in re.split(r"\n\s*\n", text.strip()):
        zeilen = [z.strip() for z in block.split("\n") if z.strip()]
        # Die Trennlinien der OFL tragen nichts und würden als Überschrift
        # gelesen — sie bestehen nur aus Bindestrichen.
        zeilen = [z for z in zeilen if set(z) != {"-"}]
        if not zeilen:
            continue

        # Steht die Überschrift allein in der ersten Zeile, bleibt sie dort.
        #
        # Erkannt wird sie an der Großschreibung, nicht an der Länge: PREAMBLE,
        # DEFINITIONS, TERMINATION. Eine Längenregel griff auch bei Absätzen,
        # deren erste Zeile zufällig kurz ausfiel — dann stand von der
        # Apache-Definition „"You" (or "Your") shall mean an individual" nur
        # die halbe Zeile als Überschrift da und der Rest daneben.
        kopf = None
        if len(zeilen) > 1 and zeilen[0] == zeilen[0].upper() and any(c.isalpha() for c in zeilen[0]):
            kopf, zeilen = zeilen[0], zeilen[1:]

        absatz = " ".join(zeilen)
        m = NUMMERIERT.match(absatz)
        if m:
            raus.append(m.group(1))
            raus.append(m.group(2))
        else:
            if kopf:
                raus.append(kopf)
            raus.append(absatz)
        if kopf and m:
            raus.insert(-2, kopf)
    return "\n\n".join(raus)


ofl_roh = (HIER / "ofl-1.1.txt").read_text(encoding="utf-8")
# Die ersten Zeilen der Datei sind eine Vorlage zum Ausfüllen
# ("Copyright (c) <dates>, <Copyright Holder>"). Unsere echten Vermerke stehen
# oben auf der Seite; hier beginnt die Lizenz selbst.
ofl = reflow(ofl_roh[ofl_roh.index("SIL OPEN FONT LICENSE Version 1.1"):])
apache = reflow((HIER / "apache-2.0.txt").read_text(encoding="utf-8"))

cinzel = copyright_aus(SCHRIFTEN / "cinzel_decorative_bold.ttf")
quicksand = copyright_aus(SCHRIFTEN / "quicksand_variable.ttf")

# Die deutschen Absätze stehen bewusst als je eine lange Zeile.
#
# Ein Zeilenumbruch in der Quelle bleibt ein Zeilenumbruch auf dem Telefon:
# LegalText hält die Zeilen eines Absatzes auseinander, damit Anschriften nicht
# zusammenlaufen. Hart umbrochener Fließtext sah dadurch aus wie ein Gedicht —
# „mehrere" allein auf einer Zeile, „nicht die" auf der nächsten.
seite = f"""Fairydoku benutzt fremde Bestandteile: zwei Schriften und mehrere Programmbibliotheken. Deren Urheber erlauben das ausdrücklich — sie verlangen aber, dass ihr Lizenztext mitgeliefert wird. Genau dafür ist diese Seite da.

Die Lizenzen stehen im englischen Original. Eine Übersetzung wäre nicht die Lizenz, sondern eine Nacherzählung davon.

Schriften
{cinzel}
{quicksand}

Beide stehen unter der SIL Open Font License, Version 1.1. Ihr vollständiger Text folgt.

{ofl}

Programmbibliotheken
Oberfläche, Bewegungsabläufe und Datenhaltung stützen sich auf freie Bibliotheken von Google und JetBrains: AndroidX, Jetpack Compose, DataStore und kotlinx.serialization. Sie stehen unter der Apache License, Version 2.0. Ihr vollständiger Text folgt.

Nicht darunter fallen Googles Werbebausteine — Google Mobile Ads und die User Messaging Platform, über die das Belohnungsvideo und die Einwilligungsabfrage laufen. Sie sind nicht quelloffen und werden unter Googles eigenen Bedingungen bereitgestellt, die keine Weitergabe eines Lizenztexts verlangen.

{apache}"""

(HIER / "lizenzseite.txt").write_text(seite, encoding="utf-8")
print(f"  Copyright Cinzel:    {cinzel}")
print(f"  Copyright Quicksand: {quicksand}")
print(f"  Seitentext:          {len(seite)} Zeichen, {seite.count(chr(10)*2) + 1} Absätze")
