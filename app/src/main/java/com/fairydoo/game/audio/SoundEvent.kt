package com.fairydoo.game.audio

import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.GameStatus
import com.fairydoo.game.game.StatusMessage
import com.fairydoo.game.game.model.CellMark

/** Was im Wald zu hören ist. */
sealed interface SoundEvent {
    /** Eine Fee wurde gesetzt und sitzt richtig — sie kichert. */
    data class FairyPlaced(val variant: Int) : SoundEvent

    /** Eine Fee wurde falsch gesetzt und erschrickt. */
    data object FairyStartled : SoundEvent

    /** Ein Merkzeichen wurde gesetzt. */
    data object Ward : SoundEvent

    /** Eine Fee wurde wieder weggenommen. */
    data object Undo : SoundEvent

    /** Der Feenstaub wurde eingesetzt. */
    data object FairyDustUsed : SoundEvent

    /** Rätsel gelöst — Jubel und Lob. */
    data object LevelComplete : SoundEvent

    data object GameOver : SoundEvent
}

/**
 * Leitet aus zwei aufeinanderfolgenden Spielzuständen ab, was zu hören ist.
 *
 * Bewusst hier statt in der Engine: Die Regeln sollen nichts über Klang wissen.
 * Und bewusst als reine Funktion statt verstreut in der UI — so ist in einem
 * Unit-Test prüfbar, dass ein falsch gesetzter Zug wirklich den Schreck auslöst
 * und nicht das Kichern.
 */
object SoundEvents {

    fun diff(previous: GameState, next: GameState): List<SoundEvent> {
        val events = mutableListOf<SoundEvent>()

        // Der Feenstaub zuerst: Sein Klang ersetzt den des Zuges, den er
        // auslöst — er setzt ja selbst eine Fee.
        val usedDust = next.fairyDust < previous.fairyDust
        if (usedDust) events += SoundEvent.FairyDustUsed

        val levelSolved = previous.status != GameStatus.LevelComplete &&
            next.status == GameStatus.LevelComplete
        val lost = previous.status != GameStatus.GameOver &&
            next.status == GameStatus.GameOver

        if (!usedDust && !levelSolved) {
            events += markChangeEvents(previous, next)
        }

        // Der Jubel ersetzt das Kichern der letzten Fee — beides zugleich wäre
        // ein Durcheinander.
        if (levelSolved) events += SoundEvent.LevelComplete
        if (lost) events += SoundEvent.GameOver

        return events
    }

    private fun markChangeEvents(previous: GameState, next: GameState): List<SoundEvent> {
        val puzzle = next.puzzle ?: return emptyList()
        val events = mutableListOf<SoundEvent>()

        for (pos in puzzle.allPositions) {
            val before = previous.markAt(pos)
            val after = next.markAt(pos)
            if (before == after) continue

            when (after) {
                CellMark.Fairy -> events += when {
                    pos in next.conflicts -> SoundEvent.FairyStartled
                    // Die Variante wechselt mit Feld und Spielstand, damit
                    // dieselbe Fee nicht bei jedem Zug identisch klingt.
                    else -> SoundEvent.FairyPlaced(
                        variant = (pos.row * 3 + pos.col + next.placedFairies) %
                            FairyClips.GIGGLE_COUNT,
                    )
                }

                CellMark.Warded -> events += SoundEvent.Ward
                CellMark.Empty -> if (before == CellMark.Fairy) events += SoundEvent.Undo
            }
        }
        return events
    }
}
