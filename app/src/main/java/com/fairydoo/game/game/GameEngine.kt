package com.fairydoo.game.game

/**
 * Die Spielregeln — die einzige Stelle, die weiß, *was* Fairy Doo eigentlich ist.
 *
 * Alles andere (ViewModel, UI, Persistenz) ist mechanik-neutral und bleibt
 * unverändert, wenn die echte Puzzle-Mechanik hier einzieht. Reine Funktionen,
 * keine Android-Abhängigkeiten — dadurch mit normalen JVM-Unit-Tests prüfbar.
 */
interface GameEngine {

    /** Frischer Startzustand für eine neue Partie. */
    fun newGame(level: Int = 1): GameState

    /**
     * Zeitschritt. [deltaMillis] ist die seit dem letzten Frame vergangene Zeit.
     * Wird nur aufgerufen, während der Zustand [GameStatus.Running] ist.
     */
    fun tick(state: GameState, deltaMillis: Long): GameState

    /** Verarbeitet eine Spielereingabe. */
    fun onInput(state: GameState, input: GameInput): GameState
}

/**
 * Spielereingaben. Für die echte Mechanik hier ergänzen (z. B. `Swipe`,
 * `SelectTile`, `Undo`) — die UI übersetzt Gesten in diese Ereignisse.
 */
sealed interface GameInput {
    /** Tap auf das Spielfeld, Koordinaten normiert auf 0f..1f. */
    data class Tap(val x: Float, val y: Float) : GameInput
}

/**
 * Platzhalter-Regeln, damit das Gerüst von Anfang an spielbar und testbar ist:
 * 60 Sekunden Zeit, jeder Tap gibt Punkte, danach Ergebnis.
 *
 * Wird durch die echte Puzzle-Mechanik ersetzt.
 */
class PlaceholderEngine : GameEngine {

    override fun newGame(level: Int): GameState = GameState(
        status = GameStatus.Running,
        level = level,
        remainingMillis = GameState.ROUND_DURATION_MILLIS,
    )

    override fun tick(state: GameState, deltaMillis: Long): GameState {
        if (state.status != GameStatus.Running) return state

        val remaining = (state.remainingMillis - deltaMillis).coerceAtLeast(0L)
        return state.copy(
            elapsedMillis = state.elapsedMillis + deltaMillis,
            remainingMillis = remaining,
            status = if (remaining == 0L) GameStatus.Finished else state.status,
        )
    }

    override fun onInput(state: GameState, input: GameInput): GameState {
        if (state.status != GameStatus.Running) return state

        return when (input) {
            is GameInput.Tap -> state.copy(
                score = state.score + POINTS_PER_TAP,
                moves = state.moves + 1,
            )
        }
    }

    private companion object {
        const val POINTS_PER_TAP = 10
    }
}
