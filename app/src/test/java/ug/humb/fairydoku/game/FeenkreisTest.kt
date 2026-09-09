package ug.humb.fairydoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ug.humb.fairydoku.game.model.CellMark
import ug.humb.fairydoku.game.model.FairydokuRules
import ug.humb.fairydoku.game.model.Pos

/**
 * Der Feenkreis — der dritte Helfer, und der einzige, der nichts verrät.
 *
 * Feenstaub und Irrlicht kennen die Lösung und dürfen sie zeigen. Der Feenkreis
 * kennt nur die Züge des Spielers: Solange er brennt, kreuzt jede gesetzte Fee
 * selbst an, welche Felder sie ausschließt. Setzt man sie falsch, sind die
 * Kreuze auch falsch — und genau das muss so bleiben, sonst wäre er die dritte
 * Lösungshilfe statt einer Schreibhilfe.
 *
 * Geprüft wird beides: der Vorrat (zwei Stück, drei Stunden) und das Ankreuzen.
 */
class FeenkreisTest {

    private val engine = FairydokuEngine()

    private fun startedGame(level: Int = 1): GameState =
        engine.onInput(engine.newGame(level), GameInput.Begin)

    @Test
    fun `zwei Stueck, drei Stunden je Nachwuchs`() {
        // Knapper als Feenstaub und Irrlicht (drei Stück, zwei Stunden), weil
        // er auf ein ganzes Rätsel wirkt statt auf ein einzelnes Feld.
        assertEquals(2, FeenkreisSupply.max)
        assertEquals(3 * 60 * 60_000L, FeenkreisSupply.intervalMillis)
    }

    @Test
    fun `einsetzen kostet ein Stueck und startet die halbe Minute`() {
        val vorher = startedGame()

        val nachher = engine.onInput(vorher, GameInput.UseFeenkreis)

        assertEquals(vorher.feenkreis - 1, nachher.feenkreis)
        assertEquals(GameState.FEENKREIS_MILLIS, nachher.feenkreisMillis)
    }

    @Test
    fun `ein brennender Kreis laesst sich nicht nachlegen`() {
        // Sonst verpufft der zweite Vorrat im ersten Kreis, ohne dass jemand
        // etwas davon hat.
        val brennt = engine.onInput(startedGame(), GameInput.UseFeenkreis)

        val nochmal = engine.onInput(brennt, GameInput.UseFeenkreis)

        assertEquals(brennt.feenkreis, nochmal.feenkreis)
        assertEquals(brennt.feenkreisMillis, nochmal.feenkreisMillis)
    }

    @Test
    fun `ohne Vorrat passiert nichts`() {
        val leer = startedGame().copy(feenkreis = 0)

        assertEquals(leer, engine.onInput(leer, GameInput.UseFeenkreis))
    }

    @Test
    fun `die Zeit laeuft mit der Uhr ab`() {
        val brennt = engine.onInput(startedGame(), GameInput.UseFeenkreis)

        val spaeter = engine.tick(brennt, 10_000L)
        assertEquals(GameState.FEENKREIS_MILLIS - 10_000L, spaeter.feenkreisMillis)

        // Und er bleibt bei null stehen, statt ins Minus zu laufen.
        val abgelaufen = engine.tick(spaeter, GameState.FEENKREIS_MILLIS)
        assertEquals(0L, abgelaufen.feenkreisMillis)
    }

    @Test
    fun `waehrend er brennt kreuzt eine gesetzte Fee selbst an`() {
        val brennt = engine.onInput(startedGame(), GameInput.UseFeenkreis)
        val puzzle = requireNotNull(brennt.puzzle)
        val pos = puzzle.solution.first()

        val gesetzt = engine.onInput(brennt, GameInput.HoldCell(pos))

        assertEquals(CellMark.Fairy, gesetzt.marks[pos])
        val ausgeschlossen = FairydokuRules.forbidden(puzzle, setOf(pos))
        assertTrue("Der Kreis muss mehr als ein Feld ausschliessen", ausgeschlossen.size > 1)
        for (feld in ausgeschlossen) {
            assertEquals("Feld $feld sollte angekreuzt sein", CellMark.Warded, gesetzt.marks[feld])
        }
    }

    @Test
    fun `ohne brennenden Kreis kreuzt nichts von selbst an`() {
        val ruhig = startedGame()
        val puzzle = requireNotNull(ruhig.puzzle)
        val pos = puzzle.solution.first()

        val gesetzt = engine.onInput(ruhig, GameInput.HoldCell(pos))

        assertEquals(CellMark.Fairy, gesetzt.marks[pos])
        assertEquals("Nur die Fee selbst steht auf dem Brett", 1, gesetzt.marks.size)
    }

    @Test
    fun `was schon auf dem Brett steht bleibt stehen`() {
        // Der Kreis füllt Lücken, er räumt nicht auf: Eine bereits gesetzte Fee
        // darf er nicht in ein Kreuz verwandeln, sonst nimmt er dem Spieler
        // seine eigene Arbeit weg.
        val brennt = engine.onInput(startedGame(), GameInput.UseFeenkreis)
        val puzzle = requireNotNull(brennt.puzzle)
        val ersteFee = puzzle.solution.first()
        val zweiteFee = puzzle.solution.elementAt(1)

        val beide = engine
            .onInput(brennt, GameInput.HoldCell(zweiteFee))
            .let { engine.onInput(it, GameInput.HoldCell(ersteFee)) }

        assertEquals(CellMark.Fairy, beide.marks[zweiteFee])
        assertEquals(CellMark.Fairy, beide.marks[ersteFee])
    }

    @Test
    fun `der Kreis garantiert nichts - eine falsche Fee kreuzt falsch an`() {
        // Der Unterschied zum Feenstaub, und der Grund, warum er billiger sein
        // darf: Er kennt die Lösung nicht, sondern nur den Zug.
        val brennt = engine.onInput(startedGame(), GameInput.UseFeenkreis)
        val puzzle = requireNotNull(brennt.puzzle)
        val loesung = puzzle.solution.toSet()
        val falsch = (0 until puzzle.size).flatMap { r -> (0 until puzzle.size).map { c -> Pos(r, c) } }
            .first { it !in loesung && FairydokuRules.forbidden(puzzle, setOf(it)).any { f -> f in loesung } }

        val gesetzt = engine.onInput(brennt, GameInput.HoldCell(falsch))

        // Mindestens ein Feld der echten Lösung ist jetzt angekreuzt — der Kreis
        // hat die Behauptung des Spielers übernommen, nicht die Wahrheit.
        val ausgeschlossen = FairydokuRules.forbidden(puzzle, setOf(falsch))
        val getroffen = ausgeschlossen.filter { it in loesung }
        assertNotEquals(0, getroffen.size)
        for (feld in getroffen) assertEquals(CellMark.Warded, gesetzt.marks[feld])
    }
}
