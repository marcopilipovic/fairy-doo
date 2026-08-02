package com.fairydoo.game.game

import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.FairydokuRules
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.game.model.PuzzleGenerator
import kotlin.random.Random

/**
 * Die Spielregeln — die einzige Stelle, die weiß, *was* Fairydoku ist.
 *
 * Reine Funktionen ohne Android-Abhängigkeiten: mit normalen JVM-Unit-Tests
 * prüfbar, und UI, ViewModel und Persistenz bleiben davon unberührt.
 */
interface GameEngine {

    /** Frischer Startzustand — beginnt im Willkommens-Overlay. */
    fun newGame(level: Int = 1): GameState

    /** Zeitschritt. Wirkt nur, solange der Zustand [GameStatus.Running] ist. */
    fun tick(state: GameState, deltaMillis: Long): GameState

    /** Verarbeitet eine Spielereingabe. */
    fun onInput(state: GameState, input: GameInput): GameState
}

/** Spielereingaben. Die UI übersetzt Gesten in diese Ereignisse. */
sealed interface GameInput {
    /** Kurz tippen — setzt oder entfernt das Merkzeichen, nimmt eine Fee weg. */
    data class TapCell(val pos: Pos) : GameInput

    /** Gedrückt halten — setzt die Fee, oder nimmt sie weg. */
    data class HoldCell(val pos: Pos) : GameInput

    /** Feenstaub einsetzen — deckt ein sicheres Feld auf. */
    data object UseFairyDust : GameInput

    /** „Den Wald betreten" — beendet das Willkommens-Overlay. */
    data object Begin : GameInput

    /** „Tiefer in den Wald" — nach gelöstem Rätsel. */
    data object NextLevel : GameInput
}

/**
 * Fairydoku: Feen auf einem Zonen-Gitter platzieren.
 *
 * Endlos-Modus — der Wald wird mit jedem zweiten Level dichter, und die Feen-Art
 * wechselt. Vorbei ist es, wenn die Zeit abläuft oder alle Leben verbraucht sind.
 */
class FairydokuEngine(
    private val random: Random = Random.Default,
) : GameEngine {

    override fun newGame(level: Int): GameState = buildLevel(
        previous = GameState(),
        level = level,
        status = GameStatus.Intro,
        fresh = true,
    )

    override fun tick(state: GameState, deltaMillis: Long): GameState {
        if (state.status != GameStatus.Running) return state

        // Das Nachleuchten eines Hinweises läuft unabhängig von der Spieluhr ab.
        val pulse = (state.hintPulseMillis - deltaMillis).coerceAtLeast(0L)
        val withPulse = state.copy(
            hintPulseMillis = pulse,
            hintCell = if (pulse == 0L) null else state.hintCell,
        )

        val remaining = (withPulse.remainingMillis - deltaMillis).coerceAtLeast(0L)
        return withPulse.copy(
            remainingMillis = remaining,
            status = if (remaining == 0L) GameStatus.GameOver else withPulse.status,
            overReason = if (remaining == 0L) GameOverReason.TimeUp else withPulse.overReason,
        )
    }

    override fun onInput(state: GameState, input: GameInput): GameState = when (input) {
        is GameInput.TapCell -> onTapCell(state, input.pos)
        is GameInput.HoldCell -> onHoldCell(state, input.pos)
        GameInput.UseFairyDust -> onUseFairyDust(state)
        GameInput.Begin -> onBegin(state)
        GameInput.NextLevel -> onNextLevel(state)
    }

    private fun onBegin(state: GameState): GameState =
        if (state.status == GameStatus.Intro) {
            state.copy(status = GameStatus.Running)
        } else {
            state
        }

    /**
     * Kurz tippen: Merkzeichen setzen oder wieder wegnehmen.
     *
     * Das Merkzeichen liegt auf der schnellsten Geste, weil es der weitaus
     * häufigere Zug ist — beim Ausschließen arbeitet man sich durch viele
     * Felder, bevor überhaupt eine Fee gesetzt wird.
     *
     * Auf einer Fee räumt der Tipp ebenfalls ab. Sie kehrt damit nicht zum
     * Merkzeichen zurück, sondern zum leeren Feld: Wer eine Fee wegnimmt, hat
     * sich in aller Regel geirrt und will das Feld neu beurteilen.
     */
    private fun onTapCell(state: GameState, pos: Pos): GameState =
        setMark(state, pos) { current ->
            when (current) {
                CellMark.Empty -> CellMark.Warded
                CellMark.Warded, CellMark.Fairy -> CellMark.Empty
            }
        }

    /**
     * Gedrückt halten: die Fee setzen — oder wieder wegnehmen, wenn sie da ist.
     *
     * Auf einer Fee tut das Halten damit dasselbe wie der Tipp. Das ist
     * Absicht: Wer auf einer Fee verweilt, will sie loswerden, und ein
     * Wiedersetzen an derselben Stelle wäre nur verwirrend.
     */
    private fun onHoldCell(state: GameState, pos: Pos): GameState =
        setMark(state, pos) { current ->
            if (current == CellMark.Fairy) CellMark.Empty else CellMark.Fairy
        }

    /**
     * Setzt ein Feld auf den Wert, den [next] bestimmt, und zieht die Folgen.
     *
     * Beide Gesten unterscheiden sich nur in dieser einen Entscheidung —
     * Konflikte, Fehler und Siegprüfung sind für sie gleich und stehen deshalb
     * nur hier.
     */
    private fun setMark(
        state: GameState,
        pos: Pos,
        next: (CellMark) -> CellMark,
    ): GameState {
        val puzzle = state.puzzle ?: return state
        if (state.status != GameStatus.Running) return state
        if (!puzzle.contains(pos)) return state

        val current = state.markAt(pos)
        val target = next(current)
        if (target == current) return state

        val marks = state.marks.toMutableMap()
        val wasFairy = current == CellMark.Fairy

        if (target == CellMark.Empty) marks.remove(pos) else marks[pos] = target

        val fairies = marks.filterValues { it == CellMark.Fairy }.keys
        val conflicts = FairydokuRules.conflicts(puzzle, fairies)

        var next = state.copy(
            marks = marks,
            conflicts = conflicts,
            statusMessage = StatusMessage.Zone(
                regionIndex = puzzle.regionAt(pos),
                species = GameState.speciesForZone(state.level, puzzle.regionAt(pos)),
            ),
        )

        // Ein Fehler entsteht nur beim *Setzen* einer Fee, die sofort mit einer
        // anderen kollidiert — Wegnehmen und Merkzeichen kosten nie etwas.
        val causedMistake = !wasFairy && pos in conflicts
        if (causedMistake) {
            val lives = next.lives - 1
            next = next.copy(
                lives = lives.coerceAtLeast(0),
                statusMessage = StatusMessage.MistakeMade,
                status = if (lives <= 0) GameStatus.GameOver else next.status,
                overReason = if (lives <= 0) {
                    GameOverReason.TooManyConflicts
                } else {
                    next.overReason
                },
            )
        }

        return checkWin(next)
    }

    /**
     * Feenstaub einsetzen.
     *
     * Der Vorrat wird hier nur heruntergezählt; wann das verbrauchte Stück
     * nachwächst, entscheidet [FairyDustSupply] außerhalb der Spielregeln. Die
     * Engine kennt keine Uhrzeit — sonst ließe sie sich nicht ohne Android
     * prüfen.
     */
    private fun onUseFairyDust(state: GameState): GameState {
        if (state.status != GameStatus.Running) return state
        if (state.fairyDust <= 0) return state

        return revealSafeCell(state.copy(fairyDust = state.fairyDust - 1))
    }

    /** Der Feenstaub setzt eine Fee auf ein Lösungsfeld, das noch frei ist. */
    private fun revealSafeCell(state: GameState): GameState {
        val puzzle = state.puzzle ?: return state

        val target = puzzle.solution
            .filter { state.markAt(it) != CellMark.Fairy }
            .minByOrNull { it.row * puzzle.size + it.col }
            ?: return state

        val marks = state.marks.toMutableMap()
        marks[target] = CellMark.Fairy

        val fairies = marks.filterValues { it == CellMark.Fairy }.keys
        return checkWin(
            state.copy(
                marks = marks,
                conflicts = FairydokuRules.conflicts(puzzle, fairies),
                hintCell = target,
                hintPulseMillis = GameState.HINT_PULSE_MILLIS,
                statusMessage = StatusMessage.FairyDustUsed,
            ),
        )
    }

    /** Alle Feen gesetzt und keine im Konflikt: Level geschafft. */
    private fun checkWin(state: GameState): GameState {
        val puzzle = state.puzzle ?: return state
        if (state.status != GameStatus.Running) return state
        if (!FairydokuRules.isSolved(puzzle, state.fairies)) return state

        val gained = POINTS_PER_CELL * puzzle.size + state.remainingSeconds * POINTS_PER_SECOND
        return state.copy(
            status = GameStatus.LevelComplete,
            gained = gained,
            score = state.score + gained,
        )
    }

    /**
     * Nächstes Level: frisches Rätsel, frische Uhr, neue Feen-Art. Punktestand
     * und Leben wandern mit — der Endlos-Modus ist ein Lauf, keine Serie
     * unabhängiger Runden.
     */
    private fun onNextLevel(state: GameState): GameState {
        if (state.status != GameStatus.LevelComplete) return state
        return buildLevel(state, state.level + 1, GameStatus.Running, fresh = false)
    }

    /**
     * Baut ein Level auf.
     *
     * [fresh] entscheidet, ob Punktestand, Leben und Vorräte neu beginnen
     * (Levelauswahl, neuer Versuch) oder von [previous] mitwandern (Weiterzug
     * nach gelöstem Rätsel im selben Lauf).
     */
    private fun buildLevel(previous: GameState, level: Int, status: GameStatus, fresh: Boolean): GameState {
        val duration = GameState.durationForLevel(level)

        return GameState(
            status = status,
            level = level,
            score = if (fresh) 0 else previous.score,
            gained = 0,
            puzzle = PuzzleGenerator.generate(GameState.sizeForLevel(level), random),
            lives = if (fresh) GameState.MAX_LIVES else previous.lives,
            // Der Feenstaub wird nicht mehr je Level ausgeteilt: Er ist ein
            // Vorrat des Spielers, der über die Zeit nachwächst. Was noch da
            // ist, nimmt das nächste Level mit; den Anfangsstand setzt beim
            // Levelstart das ViewModel aus dem gespeicherten Stand.
            fairyDust = previous.fairyDust,
            remainingMillis = duration,
            roundDurationMillis = duration,
            statusMessage = StatusMessage.Hint,
        )
    }

    private companion object {
        const val POINTS_PER_CELL = 100
        const val POINTS_PER_SECOND = 5
    }
}
