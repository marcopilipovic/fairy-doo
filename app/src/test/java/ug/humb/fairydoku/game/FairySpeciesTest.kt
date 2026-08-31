package ug.humb.fairydoku.game

import ug.humb.fairydoku.game.model.PuzzleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Die Zuordnung „welche Fee lebt in welcher Zone" ist reine Zahlenarbeit und
 * deshalb prüfbar — anders als die Frage, ob eine Fee hübsch aussieht.
 */
class FairySpeciesTest {

    @Test
    fun `es gibt zehn Feen mit eigenen Namen`() {
        assertEquals(10, FairySpecies.entries.size)
        assertEquals(
            "Zwei Feen tragen denselben Namen",
            10,
            FairySpecies.entries.map { it.displayName }.toSet().size,
        )
    }

    @Test
    fun `auf einem Brett bekommt jede Zone eine eigene Fee`() {
        // Zwei Zonen mit derselben Fee wären auf dem Brett nicht zu
        // unterscheiden — und genau die Unterscheidbarkeit ist der Sinn der
        // zehn Charaktere.
        for (level in 1..40) {
            val feen = GameState.speciesOnBoard(level)
            assertEquals("Level $level: $feen", feen.size, feen.toSet().size)
        }
    }

    @Test
    fun `die Zuordnung ist innerhalb eines Levels stabil`() {
        val puzzle = PuzzleGenerator.generate(6, Random(42))
        val level = 5
        val gesehen = mutableMapOf<Int, FairySpecies>()

        for (pos in puzzle.allPositions) {
            val zone = puzzle.regionAt(pos)
            val fee = GameState.speciesForZone(level, zone)
            assertEquals("Zone $zone wechselte die Fee", gesehen.getOrPut(zone) { fee }, fee)
        }
    }

    @Test
    fun `ueber zehn Level kommt jede Fee vor`() {
        // Schon auf den kleinen 4x4-Brettern der ersten Level — sonst bliebe
        // ein Teil der Figuren für Neueinsteiger unsichtbar.
        val gesehen = (1..10).flatMap { GameState.speciesOnBoard(it) }.toSet()

        assertEquals(FairySpecies.entries.toSet(), gesehen)
    }

    @Test
    fun `in jeder Zone kommt ueber zehn Level jede Fee vor`() {
        for (zone in 0 until GameState.MAX_SIZE) {
            val gesehen = (1..10).map { GameState.speciesForZone(it, zone) }.toSet()
            assertEquals("Zone $zone", FairySpecies.entries.toSet(), gesehen)
        }
    }

    @Test
    fun `der Schritt ist teilerfremd zur Zahl der Feen`() {
        // Beide Zusagen oben hängen daran; ein späterer elfter Charakter oder
        // ein anderer Schritt darf nicht stillschweigend eine davon brechen.
        var a = GameState.ZONE_STRIDE
        var b = FairySpecies.entries.size
        while (b != 0) {
            val rest = a % b
            a = b
            b = rest
        }

        assertEquals("Schritt und Feenzahl haben einen gemeinsamen Teiler", 1, a)
    }

    @Test
    fun `benachbarte Zonen bekommen weit auseinanderliegende Feen`() {
        // Der Schritt von drei sorgt dafür, dass zwei nebeneinanderliegende
        // Zonennummern nicht zwei ähnlich gestaltete Nachbarn im Enum erhalten.
        for (level in 1..10) {
            for (zone in 0 until GameState.MAX_SIZE - 1) {
                val hier = GameState.speciesForZone(level, zone).ordinal
                val dort = GameState.speciesForZone(level, zone + 1).ordinal
                val abstand = minOf(
                    (hier - dort).mod(FairySpecies.entries.size),
                    (dort - hier).mod(FairySpecies.entries.size),
                )
                assertTrue("Level $level, Zonen $zone/${zone + 1}: Abstand $abstand", abstand >= 3)
            }
        }
    }
}
