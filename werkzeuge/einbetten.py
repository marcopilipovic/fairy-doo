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


def uri(name, typ):
    roh = (HIER / name).read_bytes()
    return f"data:{typ};base64," + base64.b64encode(roh).decode("ascii")


bloecke = re.findall(
    r"```\n(.*?)\n```",
    (REPO / "storepaket/play-store/texte.md").read_text(encoding="utf-8"),
    re.S,
)
name, kurz, kurz_alt, lang = bloecke[:4]

ersatz = {
    "{{SYMBOL}}": uri("symbol.png", "image/png"),
    "{{FEATURE}}": uri("feature.jpg", "image/jpeg"),
    "{{FOTO1}}": uri("1-Spielbrett.jpg", "image/jpeg"),
    "{{FOTO2}}": uri("2-Feenpfad.jpg", "image/jpeg"),
    "{{FOTO3}}": uri("3-Level-geschafft.jpg", "image/jpeg"),
    "{{FOTO4}}": uri("4-Grosses-Gitter.jpg", "image/jpeg"),
    "{{FOTO5}}": uri("5-Anleitung.jpg", "image/jpeg"),
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
