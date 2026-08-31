package ug.humb.fairydoku.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Tageswertung: Punkte, die am Stichtag verfallen.
 *
 * Wie beim Feenstaub kommt die Zeit als Parameter herein. Damit lassen sich
 * genau die Fälle prüfen, die im Spiel am schwersten zu erzeugen wären: der
 * Sprung über vier Uhr morgens, ein Gerät, das zwei Wochen aus war, und der
 * Wechsel zwischen Winter- und Sommerzeit.
 */
class DailyCycleTest {

    private val hour = 60 * 60_000L
    private val day = DailyCycle.MILLIS_PER_DAY

    /** Mitteleuropäische Winterzeit, wie sie `TimeZone.getOffset` liefert. */
    private val cet = 1 * hour

    /** Sommerzeit — eine Stunde mehr. */
    private val cest = 2 * hour

    /** Donnerstag, 1. Januar 1970, 00:00 UTC ist der Ursprung aller Rechnungen. */
    private fun utc(days: Long, hours: Long = 0L, minutes: Long = 0L) =
        days * day + hours * hour + minutes * 60_000L

    // ---- Zyklusgrenzen ----

    @Test
    fun `der Tag wechselt um vier Uhr Ortszeit`() {
        // 03:59 Ortszeit gehört noch zum vorigen Tag, 04:01 zum neuen.
        val justBefore = utc(days = 10, hours = 3, minutes = 59) - cet
        val justAfter = utc(days = 10, hours = 4, minutes = 1) - cet

        assertEquals(9L, DailyCycle.cycleIdAt(justBefore, cet))
        assertEquals(10L, DailyCycle.cycleIdAt(justAfter, cet))
    }

    @Test
    fun `Mitternacht liegt mitten im Zyklus`() {
        // Der wichtigste Fall überhaupt: Wer um halb zwölf abends spielt, soll
        // um Mitternacht nicht seinen Punktestand verlieren.
        val beforeMidnight = utc(days = 10, hours = 23, minutes = 30) - cet
        val afterMidnight = utc(days = 11, hours = 0, minutes = 30) - cet

        assertEquals(
            DailyCycle.cycleIdAt(beforeMidnight, cet),
            DailyCycle.cycleIdAt(afterMidnight, cet),
        )
    }

    @Test
    fun `aufeinanderfolgende Kennungen liegen einen Tag auseinander`() {
        val now = utc(days = 100, hours = 12)

        assertEquals(
            DailyCycle.cycleIdAt(now, cet) + 1,
            DailyCycle.cycleIdAt(now + day, cet),
        )
    }

    @Test
    fun `der Zyklus endet genau dort wo der naechste beginnt`() {
        val now = utc(days = 20, hours = 15)
        val cycle = DailyCycle.cycleIdAt(now, cet)
        val endsAt = DailyCycle.endsAtMillis(cycle, cet)

        // Eine Millisekunde davor läuft der Zyklus noch, danach der nächste.
        assertEquals(cycle, DailyCycle.cycleIdAt(endsAt - 1, cet))
        assertEquals(cycle + 1, DailyCycle.cycleIdAt(endsAt, cet))
    }

    @Test
    fun `der Countdown zaehlt herunter und wird nie negativ`() {
        val now = utc(days = 20, hours = 15)
        val later = now + 3 * hour

        val first = DailyCycle.remainingSeconds(now, cet)
        val second = DailyCycle.remainingSeconds(later, cet)

        assertEquals(3 * 60 * 60, first - second)
        assertTrue(DailyCycle.remainingSeconds(later, cet) >= 0)
    }

    @Test
    fun `die Sommerzeit verschiebt den Stichtag mit`() {
        // Der Wechsel soll um vier Uhr *Ortszeit* liegen, nicht um vier Uhr UTC.
        // Bei Sommerzeit ist das eine Stunde früher in UTC gerechnet.
        val winterSwitch = DailyCycle.endsAtMillis(30L, cet)
        val summerSwitch = DailyCycle.endsAtMillis(30L, cest)

        assertEquals(hour, winterSwitch - summerSwitch)
    }

    @Test
    fun `Zeitzonen westlich von Greenwich funktionieren ebenso`() {
        // Negativer Versatz — sonst stolpert die Ganzzahl-Division über null.
        val newYork = -5 * hour
        val now = utc(days = 40, hours = 2)

        val cycle = DailyCycle.cycleIdAt(now, newYork)
        val endsAt = DailyCycle.endsAtMillis(cycle, newYork)

        assertEquals(cycle, DailyCycle.cycleIdAt(endsAt - 1, newYork))
        assertEquals(cycle + 1, DailyCycle.cycleIdAt(endsAt, newYork))
    }
}

/** Abrechnung und Belohnung — die Regeln rund um den Zyklus. */
class DailyScoringTest {

    private val hour = 60 * 60_000L
    private val cet = 1 * hour
    private val day = DailyCycle.MILLIS_PER_DAY

    private fun atCycle(cycleId: Long): Long =
        DailyCycle.endsAtMillis(cycleId, cet) - 12 * hour

    // ---- Punkte sammeln ----

    @Test
    fun `Punkte am selben Tag addieren sich`() {
        val now = atCycle(50L)
        val start = DailyScore(cycleId = 50L, points = 0, settledCycleId = 49L)

        val after = DailyScoring.add(start, 300, now, cet)
        val later = DailyScoring.add(after.score, 200, now + hour, cet)

        assertEquals(500, later.score.points)
        assertNull("am selben Tag wird nicht abgerechnet", later.settlement)
    }

    @Test
    fun `negative Punkte koennen den Stand nicht senken`() {
        val start = DailyScore(cycleId = 50L, points = 400, settledCycleId = 49L)

        val after = DailyScoring.add(start, -100, atCycle(50L), cet)

        assertEquals(400, after.score.points)
    }

    @Test
    fun `Punkte nach dem Stichtag landen auf dem neuen Tag`() {
        // Sonst schriebe das erste Level nach vier Uhr früh noch auf dem
        // gestrigen Konto gut.
        val start = DailyScore(cycleId = 50L, points = 400, settledCycleId = 49L)

        val after = DailyScoring.add(start, 300, atCycle(51L), cet)

        assertEquals(51L, after.score.cycleId)
        assertEquals(300, after.score.points)
        assertEquals("der alte Tag wird dabei abgerechnet", 400, after.settlement?.points)
    }

    // ---- Abrechnung ----

    @Test
    fun `am selben Tag aendert sich nichts`() {
        val start = DailyScore(cycleId = 50L, points = 400, bestPoints = 900, settledCycleId = 49L)

        val result = DailyScoring.settle(start, atCycle(50L), cet)

        assertEquals(start, result.score)
        assertNull(result.settlement)
    }

    @Test
    fun `der abgeschlossene Tag wird abgerechnet und der Zaehler beginnt neu`() {
        val start = DailyScore(cycleId = 50L, points = 3_000, bestPoints = 900, settledCycleId = 49L)

        val result = DailyScoring.settle(start, atCycle(51L), cet)

        assertEquals(51L, result.score.cycleId)
        assertEquals(0, result.score.points)
        assertEquals("das beste Tagesergebnis zieht nach", 3_000, result.score.bestPoints)
        assertEquals(50L, result.score.settledCycleId)

        val settlement = result.settlement
        assertNotNull(settlement)
        assertEquals(50L, settlement!!.cycleId)
        assertEquals(3_000, settlement.points)
        assertTrue("3.000 schlagen die bisherigen 900", settlement.wasBest)
    }

    @Test
    fun `ein schwaecherer Tag laesst die Bestleistung stehen`() {
        val start = DailyScore(cycleId = 50L, points = 600, bestPoints = 4_000, settledCycleId = 49L)

        val result = DailyScoring.settle(start, atCycle(51L), cet)

        assertEquals(4_000, result.score.bestPoints)
        assertEquals(false, result.settlement?.wasBest)
    }

    @Test
    fun `ein Tag ohne Punkte erzeugt kein Overlay`() {
        // Ein Abschluss über null Punkte wäre keine Belohnung, sondern eine
        // Mahnung — deshalb wird still abgehakt.
        val start = DailyScore(cycleId = 50L, points = 0, bestPoints = 4_000, settledCycleId = 49L)

        val result = DailyScoring.settle(start, atCycle(51L), cet)

        assertNull(result.settlement)
        assertEquals(51L, result.score.cycleId)
    }

    @Test
    fun `zwei Wochen Abwesenheit erzeugen genau eine Abrechnung`() {
        val start = DailyScore(cycleId = 50L, points = 2_000, bestPoints = 0, settledCycleId = 49L)

        val result = DailyScoring.settle(start, atCycle(64L), cet)

        assertEquals(64L, result.score.cycleId)
        assertEquals(0, result.score.points)
        assertEquals("die des zuletzt gespielten Tages", 50L, result.settlement?.cycleId)
        assertEquals(2_000, result.settlement?.points)
    }

    @Test
    fun `ein bereits abgerechneter Tag wird nicht doppelt ausgeschuettet`() {
        // Der Fall: abgerechnet, aber das Overlay noch nicht weggetippt, und
        // dazwischen wird erneut abgeglichen.
        val settled = DailyScore(cycleId = 50L, points = 2_000, settledCycleId = 50L)

        val result = DailyScoring.settle(settled, atCycle(51L), cet)

        assertNull(result.settlement)
    }

    @Test
    fun `ein zweiter Abgleich am selben neuen Tag rechnet nicht erneut ab`() {
        val start = DailyScore(cycleId = 50L, points = 2_000, settledCycleId = 49L)

        val first = DailyScoring.settle(start, atCycle(51L), cet)
        val second = DailyScoring.settle(first.score, atCycle(51L) + hour, cet)

        assertNotNull(first.settlement)
        assertNull(second.settlement)
    }

    // ---- Belohnungsstufen ----

    @Test
    fun `wer nichts geschafft hat bekommt nichts`() {
        assertTrue(DailyScoring.rewardFor(0).isEmpty)
        assertTrue(DailyScoring.rewardFor(499).isEmpty)
    }

    @Test
    fun `die Stufen greifen in der richtigen Reihenfolge`() {
        assertEquals(DailyReward(fairyDust = 1), DailyScoring.rewardFor(500))
        assertEquals(DailyReward(fairyDust = 1), DailyScoring.rewardFor(2_499))
        assertEquals(DailyReward(fairyDust = 1, irrlicht = 1), DailyScoring.rewardFor(2_500))
        assertEquals(DailyReward(fairyDust = 2, irrlicht = 1), DailyScoring.rewardFor(6_000))
        assertEquals(DailyReward(fairyDust = 2, irrlicht = 1), DailyScoring.rewardFor(99_000))
    }

    @Test
    fun `die naechste Stufe zeigt den kuerzesten Weg dorthin`() {
        val (missing, reward) = DailyScoring.nextTier(100)!!

        assertEquals("bis zur untersten Stufe", 400, missing)
        assertEquals(DailyReward(fairyDust = 1), reward)
    }

    @Test
    fun `oberhalb der hoechsten Stufe gibt es kein Ziel mehr`() {
        assertNull(DailyScoring.nextTier(6_000))
        assertNull(DailyScoring.nextTier(50_000))
    }

    @Test
    fun `eine frisch abgerechnete Wertung zielt wieder auf die unterste Stufe`() {
        val start = DailyScore(cycleId = 50L, points = 8_000, settledCycleId = 49L)

        val result = DailyScoring.settle(start, atCycle(51L), cet)

        assertEquals(500, DailyScoring.nextTier(result.score.points)?.first)
    }

    @Test
    fun `die Belohnung richtet sich nach dem abgerechneten Tag`() {
        val start = DailyScore(cycleId = 50L, points = 2_600, settledCycleId = 49L)

        val result = DailyScoring.settle(start, atCycle(51L), cet)

        assertEquals(DailyReward(fairyDust = 1, irrlicht = 1), result.settlement?.reward)
    }

    @Test
    fun `ein Tag am Jahreswechsel verhaelt sich wie jeder andere`() {
        // Keine Sonderbehandlung für Monats- oder Jahresgrenzen: Die Kennung
        // zählt Tage seit 1970, nicht Kalendertage.
        val start = DailyScore(cycleId = 20_453L, points = 1_000, settledCycleId = 20_452L)

        val result = DailyScoring.settle(start, atCycle(20_454L), cet)

        assertEquals(20_454L, result.score.cycleId)
        assertEquals(20_453L, result.settlement?.cycleId)
    }

    @Test
    fun `ein zurueckgestellter Geraetekalender rechnet nicht rueckwaerts ab`() {
        // Wer die Uhr zurückstellt, landet in einem früheren Zyklus. Abgerechnet
        // wird trotzdem, aber `settledCycleId` darf dabei nicht zurückfallen —
        // sonst ließe sich die Belohnung durch Uhrstellen wiederholen.
        val start = DailyScore(cycleId = 50L, points = 2_000, settledCycleId = 50L)

        val result = DailyScoring.settle(start, atCycle(30L), cet)

        assertEquals(30L, result.score.cycleId)
        assertEquals("der Riegel bleibt liegen", 50L, result.score.settledCycleId)
        assertNull("und es gibt nichts zu holen", result.settlement)
    }

    @Test
    fun `ein Tag ohne gespeicherte Vorgeschichte laeuft sauber an`() {
        // Frische Installation: Der Vorgabewert zeigt auf den heutigen Tag.
        val fresh = DailyScore()

        val result = DailyScoring.add(fresh, 700, atCycle(0L), cet)

        assertEquals(700, result.score.points)
        assertNull(result.settlement)
    }

    @Test
    fun `mehrere Tage hintereinander bauen die Bestleistung auf`() {
        var score = DailyScore(cycleId = 50L, settledCycleId = 49L)

        score = DailyScoring.add(score, 1_000, atCycle(50L), cet).score
        score = DailyScoring.add(score, 3_000, atCycle(51L), cet).score
        score = DailyScoring.add(score, 500, atCycle(52L), cet).score

        assertEquals("Tag 51 war der stärkste", 3_000, score.bestPoints)
        assertEquals(500, score.points)
        assertEquals(52L, score.cycleId)
    }
}
