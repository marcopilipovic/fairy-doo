package com.fairydoo.game.art

import com.fairydoo.game.game.FairySpecies

/**
 * Der Eigenton jeder Fee — der innere Schein, den sie um sich verbreitet.
 *
 * Als schlichte ARGB-Zahl statt als `Color`: Dasselbe Motiv wie bei
 * `FairyClips.GIGGLE_COUNT` — so bleibt diese Datei frei von Android und kann
 * im JVM-Unit-Test verwendet werden. Die UI wickelt den Wert in `Color(...)`.
 *
 * Die Töne sind aus den Bildern abgeleitet und bewusst heller als die Figur
 * selbst: Der Schein soll sie vom dunklen Moos abheben, nicht nachfärben.
 */
val FairySpecies.glowArgb: Int
    get() = when (this) {
        FairySpecies.Flora -> 0xE6FFB25A.toInt() // Monarchfalter-Orange
        FairySpecies.Nebula -> 0xE6B08BFF.toInt() // Sternenviolett
        FairySpecies.Salta -> 0xE6FFD95A.toInt() // Bienengelb
        FairySpecies.Aura -> 0xE6DDF2FF.toInt() // Gleißendes Weißblau
        FairySpecies.Nixie -> 0xE68CFFF6.toInt() // Eiscyan
        FairySpecies.Zephyr -> 0xE6C8FFE8.toInt() // Windmint
        FairySpecies.Ignis -> 0xE6FF7A3C.toInt() // Flammenorange
        FairySpecies.Terra -> 0xE68FE0B0.toInt() // Smaragd über Rinde
        FairySpecies.Chrono -> 0xE6FFD98C.toInt() // Uhrwerkgold
        FairySpecies.Trixie -> 0xE6FF6BC8.toInt() // Narrenrosa
    }
