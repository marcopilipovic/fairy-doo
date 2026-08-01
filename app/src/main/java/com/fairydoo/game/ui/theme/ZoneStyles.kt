package com.fairydoo.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Das Motiv, das über der Fläche einer Zone liegt.
 *
 * Jede Zone trägt zwei voneinander unabhängige Merkmale: ihre Farbe und ihr
 * Motiv. Wer Farben schlecht oder gar nicht unterscheidet, liest die Zone am
 * Motiv ab; wer sie gut unterscheidet, nimmt das Motiv kaum wahr. Keines der
 * beiden ist auf das andere angewiesen — und genau das ist der Zweck.
 *
 * Die Motive sind gegenständlich statt geometrisch, weil sie den Ort erzählen
 * sollen, an dem die Fee wohnt: Ein Nadelzweig sagt „Tannenhain", eine Welle
 * sagt „Fluss". Ein Streifenmuster sagt nur „Zone drei".
 */
enum class ZoneTexture {
    /** Funkelnde Sterne über der offenen Lichtung. */
    Sparkles,

    /** Beeren an rankenden Zweigen. */
    Berries,

    /** Herabgefallenes Herbstlaub. */
    FallenLeaves,

    /** Fließende Wellen. */
    Waves,

    /** Dornige Bögen, die sich ineinander schlingen. */
    Thorns,

    /** Aufgebrochene Kristallzellen. */
    CrystalCells,

    /** Strahlende Sonnenblumen. */
    Sunflowers,

    /** Nadelzweige eines dichten Tanns. */
    PineNeedles,

    /** Weiches, gesprenkeltes Moos. */
    Speckles,

    /** Sternbilder mit ihren Verbindungslinien. */
    Constellations,
}

/**
 * Wie eine Waldzone aussieht — Fläche, Motiv und Name gehören zusammen.
 *
 * @param fill die deckende Grundfarbe des Gebiets
 * @param ink der Ton, in dem das Motiv daraufliegt
 * @param texture das Motiv selbst
 * @param name der Ort, den die Zone darstellt
 */
data class ZoneStyle(
    val fill: Color,
    val ink: Color,
    val texture: ZoneTexture,
    val name: String,
)

/**
 * Die zehn Gebiete des Feenreichs.
 *
 * Zehn, weil zehn Feen im Wald leben und das Gitter auf 10×10 wachsen kann —
 * so bekommt jede Fee ein eigenes Zuhause, und keine Zone muss sich eine Farbe
 * mit einer anderen teilen.
 *
 * Die Farbtöne sind über den ganzen Farbkreis verteilt statt nach Geschmack
 * gewählt: Zwischen je zwei Gebieten liegt ein spürbarer Sprung im Farbton, und
 * wo zwei sich näherkommen — Goldlaub und Abendrot, Dornenranken und
 * Tannenhain — trennt sie ein deutlicher Helligkeitsunterschied.
 *
 * Der Motiv-Ton ist immer eine dunklere oder hellere Verwandte der Fläche, nie
 * eine Fremdfarbe: Das Motiv soll die Zone strukturieren, nicht eine zweite
 * Farbe in sie hineintragen.
 */
val ZoneStyles: List<ZoneStyle> = listOf(
    ZoneStyle(
        fill = Color(0xFFF2E8C9),
        ink = Color(0x66A08A4E),
        texture = ZoneTexture.Sparkles,
        name = "Helle Wiese",
    ),
    ZoneStyle(
        fill = Color(0xFF9B79C9),
        ink = Color(0x805B3E8C),
        texture = ZoneTexture.Berries,
        name = "Waldbeeren",
    ),
    ZoneStyle(
        fill = Color(0xFFB25E2E),
        ink = Color(0x80703518),
        texture = ZoneTexture.FallenLeaves,
        name = "Goldlaub",
    ),
    ZoneStyle(
        fill = Color(0xFF7FD1D8),
        ink = Color(0x803A9BA6),
        texture = ZoneTexture.Waves,
        name = "Flussquelle",
    ),
    ZoneStyle(
        fill = Color(0xFF3D9970),
        ink = Color(0x8C1E5C43),
        texture = ZoneTexture.Thorns,
        name = "Dornenranken",
    ),
    ZoneStyle(
        // Karmesin statt des Lachstons der Vorlage: Der lag im Farbton nur
        // sechzehn Grad neben Goldlaub und war bei ähnlicher Helligkeit kaum
        // davon zu trennen. Das bläuliche Rot rückt beide auseinander, ohne
        // die Kristallhöhle aus dem warmen Teil der Palette zu nehmen.
        fill = Color(0xFFCE4257),
        ink = Color(0x808A2438),
        texture = ZoneTexture.CrystalCells,
        name = "Kristallhöhle",
    ),
    ZoneStyle(
        fill = Color(0xFFE8A317),
        ink = Color(0x8C9E6809),
        texture = ZoneTexture.Sunflowers,
        name = "Abendrot",
    ),
    ZoneStyle(
        fill = Color(0xFF2E4A38),
        ink = Color(0x8C7FB894),
        texture = ZoneTexture.PineNeedles,
        name = "Tannenhain",
    ),
    ZoneStyle(
        fill = Color(0xFFC6CBC0),
        ink = Color(0x707C8676),
        texture = ZoneTexture.Speckles,
        name = "Silbermoos",
    ),
    ZoneStyle(
        fill = Color(0xFF3E3F7A),
        ink = Color(0x8CAFB4E8),
        texture = ZoneTexture.Constellations,
        name = "Himmelstor",
    ),
)

/**
 * Die Außenlinie einer Zone — cremeweiß statt bunt.
 *
 * Früher trug die Grenze die Zonenfarbe. Das doppelte die Information, die
 * ohnehin in der Fläche steckt, und zwang das Auge, zehn leuchtende Farbtöne
 * gleichzeitig an Kanten auseinanderzuhalten. Eine einheitliche helle Linie
 * sagt nur noch „hier endet eine Zone"; *welche* Zone es ist, beantwortet die
 * Füllung.
 */
val ZoneBorder = Color(0xFFE8E4DA)

/**
 * Der dunkle Saum an der Innenseite der hellen Grenzlinie.
 *
 * Eine helle Linie allein reicht nicht: Auf der Hellen Wiese, die selbst fast
 * cremefarben ist, verschwände sie vollständig — gemessen ein Kontrast von
 * 1,04 zu 1. Mit dem Saum trägt jede Grenze beides in sich, hell und dunkel,
 * und mindestens eines davon hebt sich von jeder der zehn Flächen ab. Dasselbe
 * Mittel, mit dem man Schrift über wechselnden Bildern lesbar hält.
 */
val ZoneBorderShade = Color(0xE01A1A14)

/** Die Fuge zwischen zwei Feldern derselben Zone. */
val CellSeam = Color(0x33000000)
