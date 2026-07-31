package com.fairydoo.game.game

import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.game.model.Puzzle
import com.fairydoo.game.game.model.PuzzleGenerator

/** Lebenszyklus einer Partie. */
enum class GameStatus {
    /** Willkommens-Overlay vor dem ersten Zug. */
    Intro,

    /** Läuft, Uhr tickt, Eingaben werden verarbeitet. */
    Running,

    /** Pausiert (App im Hintergrund). Uhr steht. */
    Paused,

    /** Rätsel gelöst — „Level up", wartet auf „weiter". */
    LevelComplete,

    /** Zeit abgelaufen oder alle Leben verloren. */
    GameOver,
}

/** Die drei Magie-Fähigkeiten aus dem Feenreich. */
enum class PowerUp {
    /** Feenstaub: deckt ein garantiert sicheres Feld auf. */
    FairyDust,

    /** Natur-Schild: fängt den nächsten Fehler ab. */
    NatureShield,

    /** Zeiten-Blüte: friert die Uhr eine Weile ein. */
    TimeBlossom,
}

/**
 * Die Feen-Arten wechseln mit jedem Level durch. Sie ändern nichts an den
 * Regeln — sie geben dem Fortschritt ein Gesicht.
 */
enum class FairySpecies(val displayName: String) {
    Blossom("Blütenfeen"),
    Water("Wasserfeen"),
    Fire("Feuerfeen"),
    Star("Sternenfeen"),
}

/** Warum die Partie endete. */
enum class GameOverReason {
    TimeUp,
    TooManyConflicts,
}

/**
 * Rückmeldung an den Spieler unter dem Brett.
 *
 * Die Engine meldet, *was* geschehen ist, nicht wie es formuliert wird — die
 * Texte stehen in der UI. So bleibt die Spiellogik frei von Copy, und die
 * Formulierungen lassen sich ändern oder übersetzen, ohne die Regeln
 * anzufassen.
 */
sealed interface StatusMessage {
    /** Voreinstellung: die Bedienhilfe. */
    data object Hint : StatusMessage

    /** Nach jedem Tippen: der Name der berührten Zone. */
    data class Zone(val regionIndex: Int) : StatusMessage

    data object MistakeMade : StatusMessage
    data object ShieldSaved : StatusMessage
    data object ShieldActivated : StatusMessage
    data object ShieldAlreadyActive : StatusMessage
    data object FairyDustUsed : StatusMessage
    data object TimeFrozen : StatusMessage
    data class Exhausted(val powerUp: PowerUp) : StatusMessage
}

/**
 * Vollständiger Zustand einer Partie.
 *
 * Unveränderlich: Die Engine erzeugt aus altem Zustand + Ereignis einen neuen.
 * Das macht Undo, Replay und Tests trivial.
 */
data class GameState(
    val status: GameStatus = GameStatus.Intro,
    val level: Int = 1,
    val score: Int = 0,
    /** Punkte des zuletzt abgeschlossenen Levels — für das „Level up"-Overlay. */
    val gained: Int = 0,
    val puzzle: Puzzle? = null,
    val marks: Map<Pos, CellMark> = emptyMap(),
    val conflicts: Set<Pos> = emptySet(),
    /** Zuletzt per Feenstaub aufgedecktes Feld; pulsiert kurz golden. */
    val hintCell: Pos? = null,
    val hintPulseMillis: Long = 0L,
    val lives: Int = MAX_LIVES,
    val shieldActive: Boolean = false,
    val powerUps: Map<PowerUp, Int> = STARTING_POWER_UPS,
    val remainingMillis: Long = 0L,
    val roundDurationMillis: Long = 0L,
    /** Restlaufzeit der Zeiten-Blüte; solange > 0, steht die Uhr still. */
    val freezeMillis: Long = 0L,
    val statusMessage: StatusMessage = StatusMessage.Hint,
    val overReason: GameOverReason? = null,
) {
    val isActive: Boolean get() = status == GameStatus.Running

    val timeFrozen: Boolean get() = freezeMillis > 0L

    val remainingSeconds: Int get() = (remainingMillis / 1000).toInt()

    val species: FairySpecies get() = speciesForLevel(level)

    /** Alle vom Spieler gesetzten Feen. */
    val fairies: Set<Pos>
        get() = marks.filterValues { it == CellMark.Fairy }.keys

    val placedFairies: Int get() = fairies.size

    val boardSize: Int get() = puzzle?.size ?: 0

    fun markAt(pos: Pos): CellMark = marks[pos] ?: CellMark.Empty

    fun powerUpCount(powerUp: PowerUp): Int = powerUps[powerUp] ?: 0

    /** Fortschritt im aktuellen Rätsel, 0f..1f — speist den Goldbalken. */
    val levelProgress: Float
        get() {
            val total = boardSize
            if (total == 0) return 0f
            return (placedFairies.toFloat() / total).coerceIn(0f, 1f)
        }

    companion object {
        const val MAX_LIVES = 3

        val STARTING_POWER_UPS: Map<PowerUp, Int> = mapOf(
            PowerUp.FairyDust to 3,
            PowerUp.NatureShield to 1,
            PowerUp.TimeBlossom to 2,
        )

        /** Wie lange die Zeiten-Blüte die Uhr anhält. */
        const val FREEZE_DURATION_MILLIS = 12_000L

        /** Wie lange ein aufgedecktes Feld nachleuchtet. */
        const val HINT_PULSE_MILLIS = 2_000L

        /** Der Wald wird dichter: alle zwei Level ein Feld mehr, bis 8×8. */
        fun sizeForLevel(level: Int): Int =
            (PuzzleGenerator.MIN_SIZE + (level - 1) / 2).coerceAtMost(MAX_SIZE)

        /** Größere Gitter brauchen mehr Zeit. */
        fun durationForLevel(level: Int): Long =
            (BASE_SECONDS + sizeForLevel(level) * SECONDS_PER_CELL) * 1000L

        fun speciesForLevel(level: Int): FairySpecies =
            FairySpecies.entries[(level - 1).coerceAtLeast(0) % FairySpecies.entries.size]

        const val MAX_SIZE = 8
        private const val BASE_SECONDS = 60L
        private const val SECONDS_PER_CELL = 15L
    }
}
