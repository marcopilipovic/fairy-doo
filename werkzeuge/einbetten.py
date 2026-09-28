"""Setzt Bilder und Texte in die Store-Vorlage ein.

Die Bilder werden als data:-URI eingebettet, weil die veröffentlichte Seite
keine fremden Hosts erreichen darf. Die Texte kommen aus texte.md — derselben
Datei, aus der auch die App und die PDFs gespeist werden; hier wird nichts
zweitgepflegt.
"""
import base64
import html
import pathlib
import re

HIER = pathlib.Path(__file__).parent
REPO = HIER.parent


# Die Bilder liegen dort, wo sie auch für den Store liegen — nicht daneben.
# Bis zum 28. September zeigte dieses Werkzeug auf JPG-Dateien im eigenen
# Ordner, die es seit der Umstellung auf gerechnete Bildschirmfotos nicht mehr
# gibt; es wäre beim ersten Aufruf gescheitert.
LADEN = REPO / "storepaket" / "play-store"


def uri(name, typ):
    roh = (LADEN / name).read_bytes()
    return f"data:{typ};base64," + base64.b64encode(roh).decode("ascii")


bloecke = re.findall(
    r"```\n(.*?)\n```",
    (REPO / "storepaket/play-store/texte.md").read_text(encoding="utf-8"),
    re.S,
)
name, kurz, kurz_alt, lang = bloecke[:4]

ersatz = {
    "{{SYMBOL}}": uri("symbol-512x512.png", "image/png"),
    "{{FEATURE}}": uri("feature-grafik-1024x500.png", "image/png"),
    "{{FOTO1}}": uri("bildschirmfotos/1-Spielbrett.png", "image/png"),
    "{{FOTO2}}": uri("bildschirmfotos/2-Feenpfad.png", "image/png"),
    "{{FOTO3}}": uri("bildschirmfotos/3-Feenkreis.png", "image/png"),
    "{{FOTO4}}": uri("bildschirmfotos/4-Grosses-Gitter.png", "image/png"),
    "{{FOTO5}}": uri("bildschirmfotos/5-Level-geschafft.png", "image/png"),
    "{{KURZ}}": html.escape(kurz),
    "{{KURZ_ALT}}": html.escape(kurz_alt),
    "{{LANG}}": html.escape(lang),
}

seite = (HIER / "vorlage.html").read_text(encoding="utf-8")
for marke, wert in ersatz.items():
    if marke not in seite:
        raise SystemExit(f"Marke fehlt in der Vorlage: {marke}")
    seite = seite.replace(marke, wert)

ziel = HIER / "Fairydoku-Storevorlage.html"
ziel.write_text(seite, encoding="utf-8")

print(f"  {ziel.name}  {len(seite) / 1024 / 1024:.2f} MB")
print(f"  Zeichen: Name {len(name)}, kurz {len(kurz)}, "
      f"Alternative {len(kurz_alt)}, lang {len(lang)}")
