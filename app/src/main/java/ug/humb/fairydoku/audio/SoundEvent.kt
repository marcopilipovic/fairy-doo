package ug.humb.fairydoku.audio

import ug.humb.fairydoku.game.FairySpecies
import ug.humb.fairydoku.game.GameState
import ug.humb.fairydoku.game.GameStatus
import ug.humb.fairydoku.game.StatusMessage
import ug.humb.fairydoku.game.model.CellMark

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

    /** Rätsel gelöst — der Jubel. */
    data object LevelComplete : SoundEvent

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
        //
        // Zu hören ist dabei nichts. Vom 29. bis zum 30. August lag hier ein
        // eigener Klang; er ist wieder weg, weil er nichts hinzufügte: Die
        // Waldmusik läuft ohnehin weiter, und ein Anfang braucht keine Ansage.
        // Nur das Gewinnen bekommt eine.
        val frischesBrett = previous.puzzle != null &&
            next.puzzle != null &&
            previous.puzzle !== next.puzzle
        if (frischesBrett) return emptyList()

        val events = mutableListOf<SoundEvent>()

        // Eine Hilfe klingt wie der Zug, den sie tut.
        //
        // Hier stand bis zum 30. August ein eigener Klang für den Feenstaub,
        // der den Zug ersetzte: Statt der Fee war ein Funkeln zu hören. Das
        // Irrlicht hatte nie einen — es setzt sein ✕, und man hört den Tick wie
        // bei jedem Merkzeichen. Genau das ist jetzt auch beim Feenstaub so:
        // Er setzt eine Fee, also kichert sie.
        //
        // Der Grund ist nicht Sparsamkeit. Eine Hilfe ist kein eigenes
        // Ereignis, sondern eine andere Art, denselben Zug zu tun — und wenn
        // sie anders klingt, klingt sie nach Belohnung statt nach Zug.
        val levelSolved = previous.status != GameStatus.LevelComplete &&
            next.status == GameStatus.LevelComplete
        val lost = previous.status != GameStatus.GameOver &&
            next.status == GameStatus.GameOver

        if (!levelSolved) {
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
                // Jedes Wegnehmen klingt, nicht nur das einer Fee.
                //
                // Bis zum 30. August stand hier `if (before == CellMark.Fairy)`
                // — ein Merkzeichen wieder abzuräumen blieb also stumm. Beim
                // Spielen fällt genau das auf: Das Setzen des ✕ antwortet, das
                // Wegnehmen nicht. Eine Geste, die in eine Richtung Rückmeldung
                // gibt und in die andere nicht, fühlt sich an, als hätte der
                // zweite Tipp nicht gezählt.
                CellMark.Empty -> events += SoundEvent.Undo
            }
        }
        return events
    }
}
