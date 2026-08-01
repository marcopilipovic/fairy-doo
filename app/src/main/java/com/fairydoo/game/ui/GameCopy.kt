package com.fairydoo.game.ui

import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameOverReason
import com.fairydoo.game.game.PowerUp
import com.fairydoo.game.game.StatusMessage

/**
 * Alle Texte der Oberfläche an einem Ort — wörtlich aus dem Handoff.
 *
 * Die Engine meldet nur, *was* geschehen ist ([StatusMessage]); formuliert wird
 * es hier. So lässt sich die Ansprache ändern, ohne die Spielregeln anzufassen,
 * und eine spätere Übersetzung hat genau eine Anlaufstelle.
 */
object GameCopy {

    /** Die Waldzonen, in der Reihenfolge der Zonenfarben. */
    val zoneNames = listOf(
        "Mondlicht-Lichtung",
        "Pilzkreis",
        "Flussbett",
        "Glühwürmchen-Hain",
        "Nebelmoor",
        "Sternenwiese",
        "Wurzelhöhle",
        "Elfentor",
    )

    fun zoneName(index: Int): String = zoneNames[index % zoneNames.size]

    fun statusText(message: StatusMessage): String = when (message) {
        StatusMessage.Hint -> "Tippe ein Feld: leer → ✕ → 🧚"
        // Knapp gehalten: Das ist die mit Abstand längste Meldung, und je
        // kürzer sie ist, desto seltener bricht sie auf zwei Zeilen um.
        is StatusMessage.Zone ->
            "${zoneName(message.regionIndex)} · ${fairyIntroduction(message.species)}"
        StatusMessage.MistakeMade -> "⚡ Die Zauberkräfte stören sich! (−1 Leben)"
        StatusMessage.ShieldSaved -> "🍃 Der Natur-Schild hat dich beschützt!"
        StatusMessage.ShieldActivated -> "🍃 Natur-Schild aktiviert!"
        StatusMessage.ShieldAlreadyActive -> "Der Schild leuchtet bereits."
        StatusMessage.FairyDustUsed -> "✨ Ein Irrlicht zeigt dir ein sicheres Feld!"
        StatusMessage.TimeFrozen -> "🌸 Die Zeit steht still… (12 s)"
        is StatusMessage.Exhausted -> when (message.powerUp) {
            PowerUp.FairyDust -> "Kein Feenstaub mehr übrig…"
            PowerUp.NatureShield -> "Keine Schutzblätter mehr…"
            PowerUp.TimeBlossom -> "Keine Zeiten-Blüten mehr…"
        }
    }

    fun gameOverReason(reason: GameOverReason?): String = when (reason) {
        GameOverReason.TimeUp -> "Die Zeit ist verronnen – der Wald schläft ein."
        GameOverReason.TooManyConflicts -> "Zu viele Zauberkräfte sind kollidiert."
        null -> ""
    }

    /** Die Wesensart jeder Fee — Beiname, nicht Eigenname. */
    fun fairyTitle(species: FairySpecies): String = when (species) {
        FairySpecies.Flora -> "Waldfee"
        FairySpecies.Nebula -> "Staubfee"
        FairySpecies.Salta -> "Hüpffee"
        FairySpecies.Aura -> "Strahlfee"
        FairySpecies.Nixie -> "Frostfee"
        FairySpecies.Zephyr -> "Windfee"
        FairySpecies.Ignis -> "Funkenfee"
        FairySpecies.Terra -> "Kristallfee"
        FairySpecies.Chrono -> "Pendelfee"
        FairySpecies.Trixie -> "Chaosfee"
    }

    /** „Nixie, die Frostfee" */
    fun fairyIntroduction(species: FairySpecies): String =
        "${species.displayName}, die ${fairyTitle(species)}"

    /** „3 / 5 Feen platziert" */
    fun progressText(placed: Int, total: Int): String = "$placed / $total Feen platziert"

    /**
     * „Der Wald wird dichter: 5×5-Gitter — Nixie und Salta warten schon…"
     *
     * Kündigt die Neuzugänge an statt einer Feen-Art: Seit in jeder Zone eine
     * andere Fee lebt, ist das die Information, auf die man sich freut.
     */
    fun nextLevelTeaser(nextSize: Int, newcomers: List<FairySpecies>): String {
        val grid = "$nextSize×$nextSize-Gitter"
        if (newcomers.isEmpty()) {
            return "Der Wald wird dichter: ein neues $grid erwartet dich…"
        }

        val shown = newcomers.take(MAX_TEASER_NAMES).map { it.displayName }
        val hidden = newcomers.size - shown.size
        val names = enumerate(if (hidden > 0) shown + "$hidden weitere" else shown)
        val verb = if (newcomers.size == 1) "wartet" else "warten"
        return "Der Wald wird dichter: $grid — $names $verb schon…"
    }

    /** „Flora" · „Flora und Nixie" · „Flora, Nixie und Chrono" */
    private fun enumerate(names: List<String>): String = when (names.size) {
        0 -> ""
        1 -> names.first()
        else -> names.dropLast(1).joinToString(", ") + " und " + names.last()
    }

    private const val MAX_TEASER_NAMES = 3

    /** Formatiert die Restzeit als m:ss. */
    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}
