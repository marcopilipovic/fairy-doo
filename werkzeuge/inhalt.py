"""Baut die ausgezeichneten Textdateien für PdfSatz aus den echten Quellen.

Die Rechtstexte kommen aus dem, was der Ausgabetest der App herausschreibt;
die Spielbeschreibung aus texte.md. Beides also aus derselben Stelle, aus der
auch App und Webseite gespeist werden — nichts wird hier zweitgepflegt.
"""
import re
import pathlib

REPO = pathlib.Path(__file__).resolve().parent.parent
ZIEL = pathlib.Path(__file__).parent

PARA = re.compile(r"^§ \d+\s+\S.*$")
NUM = re.compile(r"^\d+\.\s+\S.*$")


def ist_ueberschrift(zeile, erste):
    """Dieselbe Regel wie LegalText.kt in der App."""
    if PARA.match(zeile) or NUM.match(zeile):
        return True
    if not erste:
        return False
    if len(zeile) > 60:
        return False
    return zeile[-1] not in ".:!?,;"


def zerlege(text):
    """Zerlegt in (Art, Zeilen): H Überschrift, B Aufzählung, P Absatz.

    Ein Aufzählungspunkt darf über mehrere Zeilen gehen — im Quelltext sind die
    Zeilen umbrochen, und die Folgezeilen beginnen ohne Punktzeichen. Früher
    verlangte die Prüfung, dass *jede* Zeile mit dem Punkt beginnt; dadurch
    fielen sechs Punkte zu einem Fließtext zusammen und lasen sich wie ein
    Satz ohne Interpunktion.
    """
    raus = []
    for absatz in re.split(r"\n\s*\n", text.strip()):
        zeilen = [z.strip() for z in absatz.split("\n") if z.strip()]
        offen, punkte = [], []

        def schliesseAbsatz():
            if offen:
                raus.append(("P", list(offen)))
                offen.clear()

        def schliessePunkte():
            if punkte:
                raus.append(("B", list(punkte)))
                punkte.clear()

        for i, zeile in enumerate(zeilen):
            if zeile.startswith("• "):
                schliesseAbsatz()
                punkte.append(zeile[2:])
            elif punkte:
                # Fortsetzung des laufenden Punktes.
                punkte[-1] += " " + zeile
            elif ist_ueberschrift(zeile, i == 0):
                schliesseAbsatz()
                raus.append(("H", [zeile]))
            else:
                offen.append(zeile)

        schliesseAbsatz()
        schliessePunkte()
    return raus


def rechtstexte():
    z = [
        "T:Fairydoku — Rechtstexte",
        "P:Impressum, Nutzungsbedingungen, Datenschutzerklärung und die Lizenzen der "
        "fremden Bestandteile. Wortgleich mit dem, was in der App steht — beide "
        "entstehen aus derselben Stelle im Quelltext.",
        "S:",
    ]
    for datei, titel in [
        ("impressum", "Impressum"),
        ("agb", "Nutzungsbedingungen (AGB)"),
        ("datenschutz", "Datenschutzerklärung"),
        ("lizenzen", "Lizenzen"),
    ]:
        z.append(f"H1:{titel}")
        text = (REPO / "app/build/rechtstexte" / f"{datei}.txt").read_text()
        for art, inhalt in zerlege(text):
            if art == "H":
                z.append(f"H2:{inhalt[0]}")
            elif art == "B":
                z += [f"B:{b}" for b in inhalt]
            elif len(inhalt) == 1:
                z.append(f"P:{inhalt[0]}")
            else:
                # Anschriften: jede Zeile für sich, sonst laufen sie zusammen.
                z += [f"L:{l}" for l in inhalt[:-1]]
                z.append(f"P:{inhalt[-1]}")
        z.append("S:")
    (ZIEL / "rechtstexte.txt").write_text("\n".join(z), encoding="utf-8")
    return len(z)


def spielbeschreibung():
    md = (REPO / "storepaket/play-store/texte.md").read_text()
    name, kurz, alt, voll = re.findall(r"```\n(.*?)\n```", md, re.S)[:4]

    z = [
        "T:Fairydoku — Spielbeschreibung",
        "P:Die Texte für den Google-Play-Eintrag. In Klammern jeweils die Länge und "
        "Googles Grenze.",
        "S:",
        "H1:App-Name", f"P:{name}", f"P:({len(name)} von höchstens 30 Zeichen)", "S:",
        "H1:Kurzbeschreibung",
        "P:Erscheint in der Suchliste direkt unter dem Namen — der Satz, der entscheidet, "
        "ob jemand den Eintrag überhaupt öffnet.",
        f"P:{kurz}", f"P:({len(kurz)} von höchstens 80 Zeichen)",
        "H2:Alternative", f"P:{alt}", f"P:({len(alt)} Zeichen)", "S:",
        "H1:Vollständige Beschreibung",
        f"P:({len(voll)} von höchstens 4.000 Zeichen)", "S:",
    ]
    for art, inhalt in zerlege(voll):
        if art == "B":
            z += [f"B:{b}" for b in inhalt]
        elif art == "H" or (len(inhalt) == 1 and inhalt[0].isupper()):
            z.append(f"H2:{inhalt[0]}")
        else:
            z.append("P:" + " ".join(inhalt))
    (ZIEL / "spielbeschreibung.txt").write_text("\n".join(z), encoding="utf-8")
    return len(z)


if __name__ == "__main__":
    print(f"  rechtstexte.txt        {rechtstexte()} Zeilen")
    print(f"  spielbeschreibung.txt  {spielbeschreibung()} Zeilen")
