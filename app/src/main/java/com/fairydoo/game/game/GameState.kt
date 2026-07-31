package com.fairydoo.game.game

import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.game.model.Puzzle
import com.fairydoo.game.game.model.PuzzleGenerator

/** Lebenszyklus einer Partie. */
enum class GameStatus {
    /** Noch nicht gestartet. */
    Idle,

    /** Läuft, Uhr tickt, Eingaben werden verarbeitet. */
    Running,

    /** Pausiert (App im Hintergrund oder Pause-Knopf). Uhr steht. */
    Paused,

    /** Rätsel gelöst — Zwischenstand, wartet auf „weiter“. */
    LevelComplete,

    /** Zeit abgelaufen oder zu viele Fehler. */
    GameOver,
}

/** Die drei Magie-Fähigkeiten aus dem Feenreich. */
enum class PowerUp {
    /** Feenstaub: deckt ein garantiert sicheres Feld auf. */
    FairyDust,

    /** Natur-Schild: fängt den nächsten Fehler ab. */
    NatureShield,

    /** Zeiten-Blüte: lässt die Uhr eine Weile langsamer laufen. */
    TimeBlossom,
}

/**
 * Vollständiger Zustand einer Partie.
 *
 * Unveränderlich: Die Engine erzeugt aus altem Zustand + Ereignis einen neuen.
 * Das macht Undo, Replay und Tests trivial.
 */
data class GameState(
    val status: GameStatus = GameStatus.Idle,
    val level: Int = 1,
    val score: Int = 0,
    val puzzle: Puzzle? = null,
    val marks: Map<Pos, CellMark> = emptyMap(),
    val conflicts: Set<Pos> = emptySet(),
    /** Per Feenstaub aufgedeckte Felder — zählen nicht als eigene Leistung. */
    val revealed: Set<Pos> = emptySet(),
    val mistakes: Int = 0,
    val shieldActive: Boolean = false,
    val powerUps: Map<PowerUp, Int> = STARTING_POWER_UPS,
    val remainingMillis: Long = 0L,
    val roundDurationMillis: Long = 0L,
    /** Restlaufzeit der Zeiten-Blüte; solange > 0, vergeht die Zeit halb so schnell. */
    val slowMotionMillis: Long = 0L,
) {
    val isActive: Boolean get() = status == GameStatus.Running

    val slowMotionActive: Boolean get() = slowMotionMillis > 0L

    val remainingSeconds: Int get() = ((remainingMillis + 999) / 1000).toInt()

    /** Alle vom Spieler gesetzten Feen. */
    val fairies: Set<Pos>
        get() = marks.filterValues { it == CellMark.Fairy }.keys

    /** Wie viele Feen noch fehlen. */
    val remainingFairies: Int
        get() = (puzzle?.size ?: 0) - fairies.size

    val mistakesLeft: Int get() = (MAX_MISTAKES - mistakes).coerceAtLeast(0)

    fun markAt(pos: Pos): CellMark = marks[pos] ?: CellMark.Empty

    fun powerUpCount(powerUp: PowerUp): Int = powerUps[powerUp] ?: 0

    /** Fortschritt im aktuellen Rätsel, 0f..1f — speist den Level-Balken. */
    val levelProgress: Float
        get() {
            val total = puzzle?.size ?: return 0f
            if (total == 0) return 0f
            val correct = fairies.count { it !in conflicts }
            return (correct.toFloat() / total).coerceIn(0f, 1f)
        }

    companion object {
        const val MAX_MISTAKES = 3

        val STARTING_POWER_UPS: Map<PowerUp, Int> = mapOf(
            PowerUp.FairyDust to 3,
            PowerUp.NatureShield to 1,
            PowerUp.TimeBlossom to 2,
        )

        /** Dauer der Zeitlupe, die eine Zeiten-Blüte auslöst. */
        const val SLOW_MOTION_DURATION_MILLIS = 12_000L

        /**
         * Der Wald wird dichter: Alle zwei Level wächst das Gitter, bis 9×9.
         * Darüber hinaus würden die Felder auf einem Telefon zu klein.
         */
        fun sizeForLevel(level: Int): Int =
            (PuzzleGenerator.MIN_SIZE + (level - 1) / 2).coerceAtMost(MAX_SIZE)

        /** Größere Gitter brauchen mehr Zeit. */
        fun durationForLevel(level: Int): Long {
            val size = sizeForLevel(level)
            return (BASE_SECONDS + (size - PuzzleGenerator.MIN_SIZE) * SECONDS_PER_STEP) * 1000L
        }

        private const val MAX_SIZE = 9
        private const val BASE_SECONDS = 60L
        private const val SECONDS_PER_STEP = 25L
    }
}
