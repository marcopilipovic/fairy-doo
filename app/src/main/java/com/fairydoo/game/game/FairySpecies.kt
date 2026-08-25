package com.fairydoo.game.game

/**
 * Die zehn Feen des Waldes.
 *
 * Sie ändern nichts an den Regeln — sie geben den *Zonen* ein Gesicht: In jeder
 * Waldzone lebt genau eine Fee (siehe [GameState.speciesForZone]). Früher hing
 * die Art am Level und alle Feen eines Bretts sahen gleich aus; seit die Zone
 * entscheidet, sitzen bis zu acht verschiedene Feen gleichzeitig auf dem Brett
 * und machen die Zonen auf einen Blick unterscheidbar.
 *
 * Hier steht nur die Identität. Gestalt und Farben liegen in
 * `com.fairydoo.game.art`, die Beinamen in `ui/GameCopy.kt` — die Spiellogik
 * bleibt frei von Darstellung und Formulierung.
 *
 * [displayName] ist ausgeschrieben statt aus `name` abgeleitet: Der
 * Release-Build läuft mit R8, und der Name einer Figur soll nicht daran hängen,
 * ob ein Enum-Feldname die Verschleierung übersteht.
 */
enum class FairySpecies(val displayName: String) {
    Viridis("Viridis"),
    Nebula("Nebula"),
    Salta("Salta"),
    Aura("Aura"),
    Nixie("Nixie"),
    Zephyr("Zephyr"),
    Ignis("Ignis"),
    Terra("Terra"),
    Chrono("Chrono"),
    Trixie("Trixie"),
}
