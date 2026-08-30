package com.fairydoo.game.audio

import com.fairydoo.game.game.FairySpecies
import com.fairydoo.game.game.GameState
import com.fairydoo.game.game.GameStatus
import com.fairydoo.game.game.StatusMessage
import com.fairydoo.game.game.model.CellMark

/** Was im Wald zu hören ist. */
sealed interface SoundEvent {
    /** Eine Fee wurde gesetzt und sitzt richtig — sie ruft ihren Ausruf. */
    data class FairyPlaced(val species: FairySpecies) : SoundEvent

    /** Eine Fee wurde falsch gesetzt und erschrickt. */
    data object FairyStartled : SoundEvent

    /** Ein Merkzeichen wurde gesetzt. */
    data object Ward : SoundEvent

    /** Eine Fee wurde wieder weggenommen. */
    data object Undo : SoundEvent

    /** Der Feenstaub wurde eingesetzt. */
    data object FairyDustUsed : SoundEvent

    /** Rätsel gelöst — der Jubel. */
    data object LevelComplete : SoundEvent

    /** Das nächste Rätsel liegt bereit — derselbe Jubel, heller und ganz leise. */
    data object LevelStart : SoundEvent

    data object GameOver : SoundEvent
}

/**
 * Leitet aus zwei aufeinanderfolgenden Spielzuständen ab, was zu hören ist.
 *
 * Bewusst hier statt in der Engine: Die Regeln sollen nichts über Klang wissen.
 * Und bewusst als reine Funktion statt verstreut in der UI — so ist in einem
 * Unit-Test prüfbar, dass ein falsch gesetzter Zug wirklich den Schreck auslöst
 * und nicht den Ausruf.
 */
object SoundEvents {

    fun diff(previous: GameState, next: GameState): List<SoundEvent> {
        // Ein frisches Brett ist kein Zug.
        //
        // Beim Wechsel ins nächste Level — und ebenso beim Neustart eines
        // verlorenen — wird aus jeder gesetzten Fee ein leeres Feld.
        // [markChangeEvents] las daraus für jede einzelne eine Rücknahme: bei
        // einem gelösten 8×8-Brett acht Abwärts-Wispern auf einen Schlag. Genau
        // das war das Geräusch beim Levelwechsel.
        //
        // Erkannt wird es am Rätsel selbst, nicht an der Levelnummer: Ein
        // Neustart nach drei verbrauchten Versuchen behält die Nummer, legt aber
        // ebenso ein neues Brett hin. Verglichen wird die Kennung, nicht der
        // Inhalt — zwei Level können zufällig dieselbe Lösung tragen.
        val frischesBrett = previous.puzzle != null &&
            next.puzzle != null &&
            previous.puzzle !== next.puzzle
        if (frischesBrett) return listOf(SoundEvent.LevelStart)

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
                    else -> SoundEvent.FairyPlaced(species = next.speciesAt(pos) ?: continue)
                }

                CellMark.Warded -> events += SoundEvent.Ward
                CellMark.Empty -> if (before == CellMark.Fairy) events += SoundEvent.Undo
            }
        }
        return events
    }
}
