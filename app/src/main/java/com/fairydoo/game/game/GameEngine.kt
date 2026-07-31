package com.fairydoo.game.game

import com.fairydoo.game.game.model.CellMark
import com.fairydoo.game.game.model.FairydokuRules
import com.fairydoo.game.game.model.Pos
import com.fairydoo.game.game.model.PuzzleGenerator
import kotlin.random.Random

/**
 * Die Spielregeln — die einzige Stelle, die weiß, *was* Fairy Doo ist.
 *
 * Reine Funktionen ohne Android-Abhängigkeiten: mit normalen JVM-Unit-Tests
 * prüfbar, und UI, ViewModel und Persistenz bleiben davon unberührt.
 */
interface GameEngine {

    /** Frischer Startzustand für eine neue Partie. */
    fun newGame(level: Int = 1): GameState

    /**
     * Zeitschritt. Wird nur aufgerufen, solange der Zustand
     * [GameStatus.Running] ist.
     */
    fun tick(state: GameState, deltaMillis: Long): GameState

    /** Verarbeitet eine Spielereingabe. */
    fun onInput(state: GameState, input: GameInput): GameState
}

/** Spielereingaben. Die UI übersetzt Gesten in diese Ereignisse. */
sealed interface GameInput {
    /** Tippen auf ein Feld — schaltet leer → Fee → Merkzeichen → leer. */
    data class TapCell(val pos: Pos) : GameInput

    /** Eine Magie-Fähigkeit einsetzen. */
    data class UsePowerUp(val powerUp: PowerUp) : GameInput

    /** Nach gelöstem Rätsel weiter in den dichteren Wald. */
    data object NextLevel : GameInput
}

/**
 * Fairydoku: Feen auf einem Zonen-Gitter platzieren.
 *
 * Endlos-Modus — der Wald wird mit jedem Level dichter (siehe
 * [GameState.sizeForLevel]). Vorbei ist es, wenn die Zeit abläuft oder
 * [GameState.MAX_MISTAKES] Fehler zusammenkommen.
 */
class FairydokuEngine(
    private val random: Random = Random.Default,
) : GameEngine {

    override fun newGame(level: Int): GameState {
        val duration = GameState.durationForLevel(level)
        return GameState(
            status = GameStatus.Running,
            level = level,
            puzzle = PuzzleGenerator.generate(GameState.sizeForLevel(level), random),
            remainingMillis = duration,
            roundDurationMillis = duration,
        )
    }

    override fun tick(state: GameState, deltaMillis: Long): GameState {
        if (state.status != GameStatus.Running) return state

        // Die Zeiten-Blüte halbiert das Tempo der Uhr, solange sie blüht.
        val effectiveDelta = if (state.slowMotionActive) deltaMillis / 2 else deltaMillis
        val remaining = (state.remainingMillis - effectiveDelta).coerceAtLeast(0L)

        return state.copy(
            remainingMillis = remaining,
            slowMotionMillis = (state.slowMotionMillis - deltaMillis).coerceAtLeast(0L),
            status = if (remaining == 0L) GameStatus.GameOver else state.status,
        )
    }

    override fun onInput(state: GameState, input: GameInput): GameState = when (input) {
        is GameInput.TapCell -> onTapCell(state, input.pos)
        is GameInput.UsePowerUp -> onUsePowerUp(state, input.powerUp)
        GameInput.NextLevel -> onNextLevel(state)
    }

    /**
     * Schaltet ein Feld weiter: leer → Fee → Merkzeichen → leer.
     *
     * Das Merkzeichen ist reine Notizhilfe für den Spieler („hier sitzt sicher
     * keine“) und wird von den Regeln nicht beachtet.
     */
    private fun onTapCell(state: GameState, pos: Pos): GameState {
        val puzzle = state.puzzle ?: return state
        if (state.status != GameStatus.Running) return state
        if (!puzzle.contains(pos)) return state
        // Aufgedeckte Felder sind gesetzt und lassen sich nicht wegtippen.
        if (pos in state.revealed) return state

        val marks = state.marks.toMutableMap()
        val wasFairy = state.markAt(pos) == CellMark.Fairy

        when (state.markAt(pos)) {
            CellMark.Empty -> marks[pos] = CellMark.Fairy
            CellMark.Fairy -> marks[pos] = CellMark.Warded
            CellMark.Warded -> marks.remove(pos)
        }

        val fairies = marks.filterValues { it == CellMark.Fairy }.keys
        val conflicts = FairydokuRules.conflicts(puzzle, fairies)

        // Ein Fehler entsteht nur beim *Setzen* einer Fee, die sofort mit einer
        // anderen kollidiert — Wegnehmen und Merkzeichen kosten nie etwas.
        val causedMistake = !wasFairy && pos in conflicts

        var next = state.copy(
            marks = marks,
            conflicts = conflicts,
        )

        if (causedMistake) {
            next = if (next.shieldActive) {
                // Der Natur-Schild fängt genau einen Fehler ab und verbraucht sich.
                next.copy(shieldActive = false)
            } else {
                val mistakes = next.mistakes + 1
                next.copy(
                    mistakes = mistakes,
                    status = if (mistakes >= GameState.MAX_MISTAKES) {
                        GameStatus.GameOver
                    } else {
                        next.status
                    },
                )
            }
        }

        if (next.status == GameStatus.Running && FairydokuRules.isSolved(puzzle, fairies)) {
            next = completeLevel(next)
        }

        return next
    }

    private fun onUsePowerUp(state: GameState, powerUp: PowerUp): GameState {
        if (state.status != GameStatus.Running) return state
        if (state.powerUpCount(powerUp) <= 0) return state

        val used = state.copy(
            powerUps = state.powerUps + (powerUp to state.powerUpCount(powerUp) - 1),
        )

        return when (powerUp) {
            PowerUp.FairyDust -> revealSafeCell(used)
            PowerUp.NatureShield -> used.copy(shieldActive = true)
            PowerUp.TimeBlossom -> used.copy(
                slowMotionMillis = GameState.SLOW_MOTION_DURATION_MILLIS,
            )
        }
    }

    /**
     * Der Feenstaub setzt eine Fee auf ein Lösungsfeld, das noch frei ist.
     *
     * Er räumt dabei falsch gesetzte Feen weg, die dem Hinweis im Weg stehen —
     * sonst stünde der Spieler nach dem Hinweis vor einem Brett, das die Regeln
     * verletzt, ohne zu wissen, warum.
     */
    private fun revealSafeCell(state: GameState): GameState {
        val puzzle = state.puzzle ?: return state

        val target = puzzle.solution
            .filter { it !in state.fairies }
            .minByOrNull { it.row * puzzle.size + it.col }
            ?: return state

        val marks = state.marks.toMutableMap()
        marks.keys
            .filter { marks[it] == CellMark.Fairy && it !in puzzle.solution }
            .filter { FairydokuRules.touches(it, target) || it.row == target.row || it.col == target.col }
            .forEach { marks.remove(it) }
        marks[target] = CellMark.Fairy

        val fairies = marks.filterValues { it == CellMark.Fairy }.keys
        var next = state.copy(
            marks = marks,
            revealed = state.revealed + target,
            conflicts = FairydokuRules.conflicts(puzzle, fairies),
        )

        if (FairydokuRules.isSolved(puzzle, fairies)) next = completeLevel(next)
        return next
    }

    /** Rätsel gelöst: Punkte gutschreiben und auf „weiter“ warten. */
    private fun completeLevel(state: GameState): GameState {
        val size = state.puzzle?.size ?: 0
        // Selbst gesetzte Feen zählen; aufgedeckte nicht, sonst würde der
        // Feenstaub Punkte schenken.
        val earned = (size - state.revealed.size).coerceAtLeast(0) * POINTS_PER_FAIRY * state.level
        val timeBonus = state.remainingSeconds * POINTS_PER_SECOND
        val levelBonus = LEVEL_BONUS * state.level

        return state.copy(
            status = GameStatus.LevelComplete,
            score = state.score + earned + timeBonus + levelBonus,
        )
    }

    /**
     * Nächstes Level: frisches Rätsel, frische Uhr. Punktestand, Fehler und
     * Vorräte wandern mit — der Endlos-Modus ist ein Lauf, keine Serie
     * unabhängiger Runden.
     */
    private fun onNextLevel(state: GameState): GameState {
        if (state.status != GameStatus.LevelComplete) return state

        val level = state.level + 1
        val duration = GameState.durationForLevel(level)

        return state.copy(
            status = GameStatus.Running,
            level = level,
            puzzle = PuzzleGenerator.generate(GameState.sizeForLevel(level), random),
            marks = emptyMap(),
            conflicts = emptySet(),
            revealed = emptySet(),
            remainingMillis = duration,
            roundDurationMillis = duration,
            slowMotionMillis = 0L,
            powerUps = restock(state.powerUps, level),
        )
    }

    /** Nachschub: pro Level eine Fähigkeit auffüllen, der Reihe nach. */
    private fun restock(powerUps: Map<PowerUp, Int>, level: Int): Map<PowerUp, Int> {
        val reward = PowerUp.entries[level % PowerUp.entries.size]
        return powerUps + (reward to (powerUps[reward] ?: 0) + 1)
    }

    private companion object {
        const val POINTS_PER_FAIRY = 50
        const val POINTS_PER_SECOND = 5
        const val LEVEL_BONUS = 200
    }
}
