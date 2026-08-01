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
    /** Tippen auf ein Feld — schaltet leer → Merkzeichen → Fee → leer. */
    data class TapCell(val pos: Pos) : GameInput

    /** Eine Magie-Fähigkeit einsetzen. */
    data class UsePowerUp(val powerUp: PowerUp) : GameInput

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
    )

    override fun tick(state: GameState, deltaMillis: Long): GameState {
        if (state.status != GameStatus.Running) return state

        // Das Nachleuchten eines Hinweises läuft unabhängig von der Spieluhr ab.
        val pulse = (state.hintPulseMillis - deltaMillis).coerceAtLeast(0L)
        val withPulse = state.copy(
            hintPulseMillis = pulse,
            hintCell = if (pulse == 0L) null else state.hintCell,
        )

        // Die Zeiten-Blüte hält die Uhr an, statt sie nur zu bremsen.
        if (withPulse.timeFrozen) {
            return withPulse.copy(
                freezeMillis = (withPulse.freezeMillis - deltaMillis).coerceAtLeast(0L),
            )
        }

        val remaining = (withPulse.remainingMillis - deltaMillis).coerceAtLeast(0L)
        return withPulse.copy(
            remainingMillis = remaining,
            status = if (remaining == 0L) GameStatus.GameOver else withPulse.status,
            overReason = if (remaining == 0L) GameOverReason.TimeUp else withPulse.overReason,
        )
    }

    override fun onInput(state: GameState, input: GameInput): GameState = when (input) {
        is GameInput.TapCell -> onTapCell(state, input.pos)
        is GameInput.UsePowerUp -> onUsePowerUp(state, input.powerUp)
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
     * Schaltet ein Feld weiter: leer → Merkzeichen → Fee → leer.
     *
     * Das Merkzeichen kommt vor der Fee, weil es der häufigere Zug ist: Beim
     * Ausschließen von Feldern arbeitet man sich durch viele Merkzeichen, bevor
     * eine Fee gesetzt wird.
     */
    private fun onTapCell(state: GameState, pos: Pos): GameState {
        val puzzle = state.puzzle ?: return state
        if (state.status != GameStatus.Running) return state
        if (!puzzle.contains(pos)) return state

        val marks = state.marks.toMutableMap()
        val wasFairy = state.markAt(pos) == CellMark.Fairy

        when (state.markAt(pos)) {
            CellMark.Empty -> marks[pos] = CellMark.Warded
            CellMark.Warded -> marks[pos] = CellMark.Fairy
            CellMark.Fairy -> marks.remove(pos)
        }

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
            next = if (next.shieldActive) {
                next.copy(shieldActive = false, statusMessage = StatusMessage.ShieldSaved)
            } else {
                val lives = next.lives - 1
                next.copy(
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
        }

        return checkWin(next)
    }

    private fun onUsePowerUp(state: GameState, powerUp: PowerUp): GameState {
        if (state.status != GameStatus.Running) return state

        // Der Schild leuchtet schon — nicht noch einen verbrauchen.
        if (powerUp == PowerUp.NatureShield && state.shieldActive) {
            return state.copy(statusMessage = StatusMessage.ShieldAlreadyActive)
        }
        if (state.powerUpCount(powerUp) <= 0) {
            return state.copy(statusMessage = StatusMessage.Exhausted(powerUp))
        }

        val used = state.copy(
            powerUps = state.powerUps + (powerUp to state.powerUpCount(powerUp) - 1),
        )

        return when (powerUp) {
            PowerUp.FairyDust -> revealSafeCell(used)
            PowerUp.NatureShield -> used.copy(
                shieldActive = true,
                statusMessage = StatusMessage.ShieldActivated,
            )
            PowerUp.TimeBlossom -> used.copy(
                freezeMillis = GameState.FREEZE_DURATION_MILLIS,
                statusMessage = StatusMessage.TimeFrozen,
            )
        }
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
        return buildLevel(state, state.level + 1, GameStatus.Running)
    }

    /** Baut ein Level auf; [previous] liefert Punktestand, Leben und Vorräte. */
    private fun buildLevel(previous: GameState, level: Int, status: GameStatus): GameState {
        val duration = GameState.durationForLevel(level)
        val isFirst = level <= 1

        return GameState(
            status = status,
            level = level,
            score = if (isFirst) 0 else previous.score,
            gained = 0,
            puzzle = PuzzleGenerator.generate(GameState.sizeForLevel(level), random),
            lives = if (isFirst) GameState.MAX_LIVES else previous.lives,
            powerUps = if (isFirst) {
                GameState.STARTING_POWER_UPS
            } else {
                restock(previous.powerUps, previous.level)
            },
            remainingMillis = duration,
            roundDurationMillis = duration,
            statusMessage = StatusMessage.Hint,
        )
    }

    /**
     * Nachschub nach jedem Level: Feenstaub und Zeiten-Blüte immer, der
     * Natur-Schild nur jedes zweite Level — er ist die stärkste Fähigkeit.
     */
    private fun restock(powerUps: Map<PowerUp, Int>, completedLevel: Int): Map<PowerUp, Int> {
        fun countOf(powerUp: PowerUp) = powerUps[powerUp] ?: 0
        return mapOf(
            PowerUp.FairyDust to countOf(PowerUp.FairyDust) + 1,
            PowerUp.TimeBlossom to countOf(PowerUp.TimeBlossom) + 1,
            PowerUp.NatureShield to countOf(PowerUp.NatureShield) +
                if (completedLevel % 2 == 0) 1 else 0,
        )
    }

    private companion object {
        const val POINTS_PER_CELL = 100
        const val POINTS_PER_SECOND = 5
    }
}
