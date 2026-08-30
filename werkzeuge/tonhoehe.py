"""Bestimmt, welche Töne in einem Ausschnitt klingen.

Kein numpy vorhanden, deshalb Goertzel: Für jede in Frage kommende Tonhöhe wird
einzeln gemessen, wie viel Energie genau dort liegt. Das ist langsamer als eine
FFT, aber es genügt — wir fragen ja nur nach den 60 Halbtönen einer Tonleiter
und nicht nach dem ganzen Spektrum.
"""
import array
import math
import sys
import wave

NAMEN = ["C", "Cis", "D", "Dis", "E", "F", "Fis", "G", "Gis", "A", "Ais", "H"]


def lies(pfad):
    with wave.open(pfad, "rb") as w:
        assert w.getsampwidth() == 2, "nur 16 Bit"
        kanaele, rate = w.getnchannels(), w.getframerate()
        roh = array.array("h", w.readframes(w.getnframes()))
    if kanaele == 2:
        roh = array.array("h", [(roh[i] + roh[i + 1]) // 2 for i in range(0, len(roh), 2)])
    return roh, rate


def goertzel(proben, rate, hertz):
    """Energie bei genau einer Frequenz."""
    k = 2 * math.cos(2 * math.pi * hertz / rate)
    s1 = s2 = 0.0
    for p in proben:
        s0 = p + k * s1 - s2
        s2, s1 = s1, s0
    return math.sqrt(max(0.0, s1 * s1 + s2 * s2 - k * s1 * s2)) / len(proben)


def note(midi):
    return f"{NAMEN[midi % 12]}{midi // 12 - 1}"


pfad = sys.argv[1]
proben, rate = lies(pfad)
# Auf höchstens 0,4 s kürzen — länger bringt nichts und kostet nur Zeit.
proben = proben[: int(0.4 * rate)]

ergebnis = []
for midi in range(48, 108):            # C3 bis H7
    hz = 440.0 * 2 ** ((midi - 69) / 12)
    ergebnis.append((goertzel(proben, rate, hz), midi, hz))

ergebnis.sort(reverse=True)
staerkste = ergebnis[0][0]
print(f"  {pfad}")
for energie, midi, hz in ergebnis[:8]:
    balken = "█" * int(24 * energie / staerkste)
    print(f"    {note(midi):5s} {hz:7.1f} Hz  {balken}")
