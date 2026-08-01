package com.fairydoo.game.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.fairydoo.game.R

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
 * @param texture das gezeichnete Motiv
 * @param name der Ort, den die Zone darstellt
 * @param image eine gemalte Kachel, die Farbe und Motiv ersetzt — siehe unten
 */
data class ZoneStyle(
    val fill: Color,
    val ink: Color,
    val texture: ZoneTexture,
    val name: String,
    @DrawableRes val image: Int? = null,
) {
    /**
     * Ob dieses Gebiet als gemalte Kachel erscheint statt als gezeichnetes
     * Motiv.
     *
     * Beides steht nebeneinander, weil die Kacheln nach und nach entstehen: Ein
     * Gebiet ohne Bild sieht aus wie bisher, eines mit Bild trägt die Kachel.
     * So lässt sich Zone für Zone austauschen, ohne dass das Brett dazwischen
     * unfertig aussieht — und wenn sich eine Kachel als unbrauchbar erweist,
     * genügt es, `image` wieder zu entfernen.
     *
     * [fill] bleibt in jedem Fall gesetzt: Der Schein der Fee nimmt seine Farbe
     * daher, und sollte eine Bilddatei fehlen, ist es der Rückfall.
     */
    val hasImage: Boolean get() = image != null
}

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
        image = R.drawable.zone_goldene_lichtung,
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
 * Die Hecke, die zwei Gebiete voneinander trennt.
 *
 * Für alle Gebiete dieselbe Farbe: Welche Zone hinter einer Grenze liegt,
 * beantwortet die Fläche — die Hecke sagt nur, *dass* dort eine Grenze
 * verläuft. Bunte Ränder zwängen das Auge, zehn Farbtöne gleichzeitig an
 * Kanten auseinanderzuhalten.
 *
 * Zuvor war es eine gezogene elfenbeinfarbene Linie: in einem Wald ein
 * Fremdkörper, der die Gebiete auseinanderschnitt, statt sie zu begrenzen. Ein
 * gedämpftes Waldgrün gehört dorthin, wo es liegt.
 */
val HedgeGreen = Color(0xFF3F5B3C)

/**
 * Die Lichtseite der Blätter.
 *
 * Sie macht aus dem grünen Band erst Laub — und trägt darüber hinaus eine
 * Aufgabe, die ihr erst das Messen zugewiesen hat: Auf dem Tannenhain, einem
 * dunklen Waldgrün, hat weder die Hecke noch ihr Saum genug Kontrast; gemessen
 * 1,46 und 1,48 zu 1. Beide sind zu nah an der Fläche. Ein deutlich helleres
 * Laubgrün löst das, ohne die Hecke aufdringlich zu machen: Auf den hellen
 * Gebieten fällt weiterhin der dunkle Grundton auf, auf den dunklen dieses hier.
 */
val HedgeLight = Color(0xFF8CAB74)

/**
 * Der dunkle Saum unter der Hecke.
 *
 * Nicht Kosmetik: Die Zonenregel ist der Kern des Rätsels, und zwei Gebiete
 * müssen auch dann getrennt bleiben, wenn die Hecke selbst in einer der beiden
 * Flächen aufginge — auf dem Tannenhain etwa, der fast dieselbe Farbe hat.
 * Dasselbe Mittel, mit dem man Schrift über wechselnden Bildern lesbar hält.
 */
val HedgeShade = Color(0xC7121A10)

/** Die Fuge zwischen zwei Feldern derselben Zone. */
val CellSeam = Color(0x24000000)

/**
 * Die Rinde der Zweige, mit denen ein Feld als „hier keine Fee" markiert wird.
 *
 * Vorher war das ein weißes Kreuz — neben einer gemalten Kachel wirkte es wie
 * aus einem anderen Spiel. Die Kreuzform bleibt, weil sie ohne Erklärung
 * verstanden wird; nur das Material ist jetzt Holz.
 */
val TwigBark = Color(0xFF7A5433)

/**
 * Die dunkle Kontur der Zweige.
 *
 * Sie trägt das Zeichen auf den hellen Gebieten — auf der Goldenen Lichtung und
 * der Kristallader wäre Rindenbraun allein zu blass.
 */
val TwigShade = Color(0xF22A1C10)

/**
 * Das Glanzlicht auf der Oberseite der Zweige.
 *
 * Es trägt das Zeichen auf den dunklen Gebieten: Auf dem nächtlichen Himmelstor
 * verschwände die Rinde beinahe, das Glanzlicht bleibt sichtbar. Zusammen mit
 * der Kontur hat die Markierung dadurch auf allen zehn Flächen dasselbe
 * Gewicht — dasselbe Prinzip wie bei der Hecke.
 */
val TwigLight = Color(0xFFC9A570)

/**
 * Feine Körnung über jeder Zonenfläche.
 *
 * Ohne sie liegen zehn satte Farben als glatte Blöcke nebeneinander, und das
 * Brett wirkt plakativ statt gemalt. Die Körnung nimmt den Flächen den Lack,
 * ohne ihre Farbe zu verändern — der Unterschied zwischen bedrucktem Papier und
 * lackiertem Blech.
 */
val ZoneGrain = Color(0x0E000000)
