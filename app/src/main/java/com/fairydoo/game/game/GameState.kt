package com.fairydoo.game.game

/** Lebenszyklus einer Partie. */
enum class GameStatus {
    /** Noch nicht gestartet — Startbildschirm des Spielfelds. */
    Idle,

    /** Läuft, Uhr tickt, Eingaben werden verarbeitet. */
    Running,

    /** Pausiert (App im Hintergrund oder Pause-Knopf). Uhr steht. */
    Paused,

    /** Partie beendet — Ergebnis wird gezeigt. */
    Finished,
}

/**
 * Vollständiger Zustand einer Partie.
 *
 * Bewusst unveränderlich: Die Engine erzeugt aus altem Zustand + Ereignis einen
 * neuen. Das macht Undo, Replay und Tests trivial — alles, was ein Puzzlespiel
 * früher oder später braucht.
 */
data class GameState(
    val status: GameStatus = GameStatus.Idle,
    val score: Int = 0,
    val moves: Int = 0,
    val level: Int = 1,
    val elapsedMillis: Long = 0L,
    val remainingMillis: Long = ROUND_DURATION_MILLIS,
) {
    val isActive: Boolean get() = status == GameStatus.Running

    val remainingSeconds: Int get() = ((remainingMillis + 999) / 1000).toInt()

    companion object {
        const val ROUND_DURATION_MILLIS: Long = 60_000L
    }
}
