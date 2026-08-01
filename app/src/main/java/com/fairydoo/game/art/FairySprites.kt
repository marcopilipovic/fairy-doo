package com.fairydoo.game.art

import com.fairydoo.game.game.FairySpecies

/**
 * Der Eigenton jeder Fee — der innere Schein, den sie um sich verbreitet.
 *
 * Als schlichte ARGB-Zahl statt als `Color`: Dasselbe Motiv wie bei
 * `FairyClips.GIGGLE_COUNT` — so kann der Vorschau-Test damit rechnen, ohne
 * dass ein Android-Grafikstapel vorhanden sein muss. Die UI wickelt den Wert in
 * `Color(...)`.
 */
val FairySpecies.glowArgb: Int
    get() = when (this) {
        FairySpecies.Flora -> 0xE68CFFA8.toInt()
        FairySpecies.Nebula -> 0xE6B08BFF.toInt()
        FairySpecies.Salta -> 0xE6FFF3A0.toInt()
        FairySpecies.Aura -> 0xE6DDF2FF.toInt()
        FairySpecies.Nixie -> 0xE68CFFF6.toInt()
        FairySpecies.Zephyr -> 0xE6C8FFE8.toInt()
        FairySpecies.Ignis -> 0xE6FF8A4A.toInt()
        FairySpecies.Terra -> 0xE68FE0B0.toInt()
        FairySpecies.Chrono -> 0xE6C89BFF.toInt()
        FairySpecies.Trixie -> 0xE6FF8AD8.toInt()
    }

/**
 * Die Gestalt jeder Fee.
 *
 * Noch zeigen neun Einträge auf [FloraSprite] — die übrigen Charaktere
 * entstehen, sobald Flora abgenommen ist. Bis dahin ist das Spiel vollständig
 * spielbar, und der Unterschied zwischen den Zonen trägt schon über den
 * Eigenton.
 */
val FairySpecies.sprite: PixelSprite
    get() = when (this) {
        FairySpecies.Flora -> FloraSprite
        else -> FloraSprite
    }

/** Alle Sprites, für Vorschau und Prüfung. */
val allFairySprites: Map<FairySpecies, PixelSprite>
    get() = FairySpecies.entries.associateWith { it.sprite }
