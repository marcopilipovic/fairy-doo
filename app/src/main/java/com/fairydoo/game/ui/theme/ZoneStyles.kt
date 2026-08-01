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
    /** Vierzackige Sterne zwischen kleinen Farnwedeln. */
    StarsAndFerns,

    /** Radiale Sonnenblumen mit Blütenkranz. */
    Sunflowers,

    /** Nadelzweige, senkrecht und schräg ineinander. */
    PineNeedles,

    /** Gewundene Ranken mit spitzen Dornen. */
    ThornVines,

    /** Überlappendes Ahorn- und Eichenlaub. */
    AutumnLeaves,

    /** Feigen an kurzen Zweigen über einer 45°-Schraffur. */
    FigsAndHatching,

    /** Kristallgitter und Marmoradern. */
    CrystalVeins,

    /** Sinusförmige Wasserlinien. */
    Waves,

    /** Sternbilder als Punkt-zu-Linie-Netz. */
    Constellations,

    /** Rissiger Lehmboden in Zellstruktur. */
    CrackedEarth,
}

/**
 * Wie eine Waldzone aussieht — Fläche, Motiv und Name gehören zusammen.
 *
 * @param fill die deckende Grundfarbe des Gebiets
 * @param ink der Ton des Motivs, samt seiner Deckkraft
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
 * Zehn, weil zehn Feen im Wald leben — so bekommt jede ein eigenes Zuhause, und
 * keine Zone muss sich eine Farbe mit einer anderen teilen.
 *
 * Farben und Deckkraft der Motive sind vorgegeben und hier unverändert
 * übernommen. Zwei Paare liegen im Farbton dicht beieinander — Goldene Lichtung
 * neben Sonnengarten, Tannenhain neben Dornenranke —, unterscheiden sich aber
 * deutlich in der Helligkeit; ein drittes, Herbstboden und Erdreich, ist in
 * beidem nah und wird allein von seinen Motiven getrennt: fallendes Laub gegen
 * rissigen Lehm. Der Test in `ZoneStylesTest` hält das fest, statt es zu
 * verschweigen.
 */
val ZoneStyles: List<ZoneStyle> = listOf(
    ZoneStyle(
        fill = Color(0xFFFDF6E3),
        ink = Color(0x66D9B46A),
        texture = ZoneTexture.StarsAndFerns,
        name = "Goldene Lichtung",
    ),
    ZoneStyle(
        fill = Color(0xFFF6C445),
        ink = Color(0x599E6B00),
        texture = ZoneTexture.Sunflowers,
        name = "Sonnengarten",
    ),
    ZoneStyle(
        fill = Color(0xFF1B4332),
        ink = Color(0x8040916C),
        texture = ZoneTexture.PineNeedles,
        name = "Tannenhain",
    ),
    ZoneStyle(
        fill = Color(0xFF00A86B),
        ink = Color(0x99004B23),
        texture = ZoneTexture.ThornVines,
        name = "Dornenranke",
    ),
    ZoneStyle(
        fill = Color(0xFFC05621),
        ink = Color(0x735C2000),
        texture = ZoneTexture.AutumnLeaves,
        name = "Herbstboden",
    ),
    ZoneStyle(
        fill = Color(0xFF6B3074),
        ink = Color(0x59B86BB3),
        texture = ZoneTexture.FigsAndHatching,
        name = "Feigenhain",
    ),
    ZoneStyle(
        fill = Color(0xFFE2E8F0),
        ink = Color(0x8064748B),
        texture = ZoneTexture.CrystalVeins,
        name = "Kristallader",
    ),
    ZoneStyle(
        fill = Color(0xFF38BDF8),
        ink = Color(0x660369A1),
        texture = ZoneTexture.Waves,
        name = "Flusslauf",
    ),
    ZoneStyle(
        fill = Color(0xFF1E1B4B),
        ink = Color(0xB3A5B4FC),
        texture = ZoneTexture.Constellations,
        name = "Himmelstor",
    ),
    ZoneStyle(
        fill = Color(0xFFE05A47),
        ink = Color(0x667A1C10),
        texture = ZoneTexture.CrackedEarth,
        name = "Erdreich",
    ),
)

/**
 * Die Außenlinie einer Zone.
 *
 * Für alle Gebiete dieselbe Farbe: Welche Zone hinter einer Grenze liegt,
 * beantwortet die Fläche — die Linie sagt nur, *dass* dort eine Grenze
 * verläuft. Bunte Ränder zwängen das Auge, zehn Farbtöne gleichzeitig an
 * Kanten auseinanderzuhalten.
 *
 * Gebrochenes Elfenbein statt Weiß: Reinweiß schnitt die Gebiete wie mit dem
 * Skalpell auseinander und ließ das Brett grell wirken.
 */
val ZoneBorder = Color(0xFFDCD3BE)

/**
 * Der dunkle Saum an der Innenseite der hellen Grenzlinie.
 *
 * Eine helle Linie allein reicht nicht: Auf der Goldenen Lichtung und der
 * Kristallader, die selbst fast weiß sind, verschwände sie vollständig. Mit dem
 * Saum trägt jede Grenze beides in sich, hell und dunkel, und mindestens eines
 * davon hebt sich von jeder der zehn Flächen ab. Dasselbe Mittel, mit dem man
 * Schrift über wechselnden Bildern lesbar hält.
 */
val ZoneBorderShade = Color(0xEB18140E)

/** Die Fuge zwischen zwei Feldern derselben Zone. */
val CellSeam = Color(0x24000000)

/**
 * Feine Körnung über jeder Zonenfläche.
 *
 * Ohne sie liegen zehn satte Farben als glatte Blöcke nebeneinander, und das
 * Brett wirkt plakativ statt gemalt. Die Körnung nimmt den Flächen den Lack,
 * ohne ihre Farbe zu verändern — der Unterschied zwischen bedrucktem Papier und
 * lackiertem Blech.
 */
val ZoneGrain = Color(0x0E000000)
