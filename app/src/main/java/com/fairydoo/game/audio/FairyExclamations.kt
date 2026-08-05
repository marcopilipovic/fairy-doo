package com.fairydoo.game.audio

import com.fairydoo.game.game.FairySpecies

/**
 * Der kurze Ausruf, den eine Fee von sich gibt, sobald sie richtig sitzt.
 *
 * Jede Art hat ihre eigene Tonhöhe und ihr eigenes Sprechtempo, damit die
 * Systemstimme des Geräts nicht bei jeder Fee gleich klingt — die Werte sind
 * Vielfache der neutralen Sprechstimme (1.0), nicht absolute Werte.
 */
object FairyExclamations {

    data class Line(val text: String, val pitch: Float, val rate: Float)

    private val lines: Map<FairySpecies, Line> = mapOf(
        FairySpecies.Flora to Line("Juhuu!", 1.8f, 1.1f),
        FairySpecies.Nebula to Line("Huch!", 1.4f, 1.15f),
        FairySpecies.Salta to Line("Jippie!", 1.9f, 1.25f),
        FairySpecies.Aura to Line("Aah!", 1.5f, 0.85f),
        FairySpecies.Nixie to Line("Brrr!", 1.3f, 0.95f),
        FairySpecies.Zephyr to Line("Huii!", 1.7f, 1.0f),
        FairySpecies.Ignis to Line("Haha!", 1.85f, 1.2f),
        FairySpecies.Terra to Line("Ooh!", 1.1f, 0.85f),
        FairySpecies.Chrono to Line("Tick!", 1.5f, 1.3f),
        FairySpecies.Trixie to Line("Hihi!", 1.75f, 1.1f),
    )

    fun of(species: FairySpecies): Line =
        requireNotNull(lines[species]) { "Keine Feenstimme für $species hinterlegt" }
}
