package com.fairydoo.game.ui

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
        is StatusMessage.Zone -> "Zone: ${zoneName(message.regionIndex)}"
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

    /** „Der Wald wird dichter: 5×5-Gitter mit Wasserfeen erwartet dich…" */
    fun nextLevelTeaser(nextSize: Int, nextSpecies: String): String =
        "Der Wald wird dichter: $nextSize×$nextSize-Gitter mit $nextSpecies erwartet dich…"

    /** Formatiert die Restzeit als m:ss. */
    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}
