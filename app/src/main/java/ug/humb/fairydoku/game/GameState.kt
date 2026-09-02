package ug.humb.fairydoku.game

import ug.humb.fairydoku.game.model.CellMark
import ug.humb.fairydoku.game.model.Pos
import ug.humb.fairydoku.game.model.Puzzle
import ug.humb.fairydoku.game.model.PuzzleGenerator

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


/**
 * Warum die Partie endete.
 *
 * Nur noch ein Grund. Daneben stand bis zum 30. August `TimeUp` — das Ende
 * durch die abgelaufene Spieluhr. Die Uhr ist am 28. August gestrichen worden,
 * der Grund blieb zwei Tage länger stehen und mit ihm ein Satz in der
 * Oberfläche („Die Zeit ist verronnen"), den kein Spieler je zu sehen bekommen
 * konnte.
 *
 * Die Aufzählung bleibt trotzdem eine: Sie benennt, *warum* ein Level verloren
 * ist, und das ist eine Information, die die Oberfläche braucht — auch wenn es
 * im Moment nur eine Antwort darauf gibt.
 */
enum class GameOverReason {
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

    /** Nach jedem Tippen: die berührte Zone und die Fee, die dort lebt. */
    data class Zone(val regionIndex: Int, val species: FairySpecies) : StatusMessage

    data object MistakeMade : StatusMessage
    data object FairyDustUsed : StatusMessage
    data object IrrlichtUsed : StatusMessage

    /** Kein Feenstaub mehr da — mit der Zeit bis zum nächsten. */
    data class NoFairyDust(val nextInMillis: Long) : StatusMessage

    /** Kein Irrlicht mehr da — mit der Zeit bis zum nächsten. */
    data class NoIrrlicht(val nextInMillis: Long) : StatusMessage
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

    /**
     * Felder, die aus einer Hilfe stammen und damit beweisbar richtig sind —
     * die Feen des Feenstaubs, die Kreuze des Irrlichts.
     *
     * Sie werden gebraucht, weil eine solche Fee sonst rot markiert wird,
     * sobald eine falsch gesetzte Fee des Spielers mit ihr in dieselbe Reihe
     * gerät. Rot heißt im Spiel „hier stimmt etwas nicht" — und das ist bei
     * genau diesem Feld die Unwahrheit. Mirco Lehnhoff am 1. September 2026:
     * „dann sind alle Feen rot hinterlegt."
     *
     * Wer die Hilfe bezahlt hat, soll ihr auch trauen koennen. Das Rot gehoert
     * auf die Feen, die weg muessen.
     */
    val certain: Set<Pos> = emptySet(),
    /** Zuletzt per Feenstaub aufgedecktes Feld; pulsiert kurz golden. */
    val hintCell: Pos? = null,
    val hintPulseMillis: Long = 0L,
    val lives: Int = MAX_LIVES,
    /**
     * Wie viel Feenstaub gerade vorrätig ist.
     *
     * Der Vorrat gehört nicht zum Level, sondern zum Spieler: Er wird beim
     * Start eines Levels aus dem gespeicherten Stand übernommen und wächst über
     * die Zeit nach ([FairyDustSupply]). Ein levelweiser Vorrat hätte sich bei
     * jedem Neustart zurückgesetzt, und das Nachwachsen wäre bedeutungslos.
     */
    val fairyDust: Int = FairyDustSupply.max,
    /**
     * Wie viel Irrlicht gerade vorrätig ist.
     *
     * Genau wie [fairyDust] ein Vorrat des Spielers statt des Levels — siehe
     * dort für die Begründung.
     */
    val irrlicht: Int = IrrlichtSupply.max,

    /**
     * Der Feenkreis-Vorrat und die Restzeit eines laufenden Kreises.
     *
     * Solange [feenkreisMillis] über null steht, kreuzt jede gesetzte Fee die
     * Felder an, die sie ausschliesst — siehe [FeenkreisSupply].
     */
    val feenkreis: Int = FeenkreisSupply.max,
    val feenkreisMillis: Long = 0L,
    val remainingMillis: Long = 0L,
    val roundDurationMillis: Long = 0L,
    val statusMessage: StatusMessage = StatusMessage.Hint,
    val overReason: GameOverReason? = null,
) {
    val isActive: Boolean get() = status == GameStatus.Running

    val remainingSeconds: Int get() = (remainingMillis / 1000).toInt()

    /** Die Fee, die auf diesem Feld erscheint — die ihrer Waldzone. */
    fun speciesAt(pos: Pos): FairySpecies? =
        puzzle?.let { speciesForZone(level, it.regionAt(pos)) }

    /** Alle vom Spieler gesetzten Feen. */
    val fairies: Set<Pos>
        get() = marks.filterValues { it == CellMark.Fairy }.keys

    val placedFairies: Int get() = fairies.size

    val boardSize: Int get() = puzzle?.size ?: 0

    fun markAt(pos: Pos): CellMark = marks[pos] ?: CellMark.Empty

    /** Fortschritt im aktuellen Rätsel, 0f..1f — speist den Goldbalken. */
    val levelProgress: Float
        get() {
            val total = boardSize
            if (total == 0) return 0f
            return (placedFairies.toFloat() / total).coerceIn(0f, 1f)
        }

    companion object {
        const val MAX_LIVES = 3


        /** Wie lange ein aufgedecktes Feld nachleuchtet. */
        const val HINT_PULSE_MILLIS = 2_000L

        /** Wie lange ein Feenkreis wirkt — eine halbe Minute. */
        const val FEENKREIS_MILLIS = 30_000L

        /** Der Wald wird dichter: alle zwei Level ein Feld mehr, bis 8×8. */
        fun sizeForLevel(level: Int): Int =
            (PuzzleGenerator.MIN_SIZE + (level - 1) / 2).coerceAtMost(MAX_SIZE)

        /** Größere Gitter brauchen mehr Zeit. */
        fun durationForLevel(level: Int): Long =
            (BASE_SECONDS + sizeForLevel(level) * SECONDS_PER_CELL) * 1000L

        /**
         * Welche Fee in einer Waldzone lebt.
         *
         * Zehn Feen, aber höchstens acht Zonen: Wäre die Zuordnung fest,
         * blieben zwei Feen für immer unsichtbar — auf den 4×4-Brettern der
         * ersten Level sogar sechs. Deshalb dreht sich der Reigen mit jedem
         * Level um einen Platz weiter.
         *
         * [ZONE_STRIDE] ist teilerfremd zur Zahl der Feen. Daran hängen beide
         * Zusagen: Auf einem Brett trägt keine Zone dieselbe Fee wie eine
         * andere, und über zehn Level kommt in jeder Zone jede Fee genau einmal
         * vor. Der Schritt von drei sorgt zusätzlich dafür, dass benachbarte
         * Zonennummern weit auseinanderliegende — also gut unterscheidbare —
         * Feen bekommen.
         */
        fun speciesForZone(level: Int, regionIndex: Int): FairySpecies =
            FairySpecies.entries[
                (regionIndex * ZONE_STRIDE + (level - 1)).mod(FairySpecies.entries.size),
            ]

        /** Die Feen, die auf dem Brett dieses Levels zu sehen sind. */
        fun speciesOnBoard(level: Int): List<FairySpecies> =
            (0 until sizeForLevel(level)).map { speciesForZone(level, it) }

        /** Teilerfremd zur Zahl der Feen — siehe [speciesForZone]. */
        const val ZONE_STRIDE = 3

        const val MAX_SIZE = 8
        private const val BASE_SECONDS = 60L
        private const val SECONDS_PER_CELL = 15L
    }
}
