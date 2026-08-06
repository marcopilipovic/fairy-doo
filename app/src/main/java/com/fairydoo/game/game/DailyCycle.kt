package com.fairydoo.game.game

/**
 * Die Tageswertung: Punkte, die nach einem Tag verfallen.
 *
 * Anders als die ewige Bestleistung sammelt die Tageswertung nur, was seit dem
 * letzten Stichtag zusammengekommen ist. Am Stichtag wird abgerechnet — es gibt
 * eine Belohnung, und der Zähler beginnt wieder bei null.
 *
 * Der feste Stichtag ist bewusst gewählt, nicht das rollierende Fenster „die
 * letzten 24 Stunden". Bei einem gleitenden Fenster tröpfeln Punkte im
 * Hintergrund heraus, und niemand kann erklären, warum der Stand kleiner
 * geworden ist, ohne dass man etwas getan hätte. Ein Stichtag lässt sich
 * dagegen als Countdown anzeigen und in einem Satz erklären.
 *
 * Wie [RegeneratingSupply] trägt die Logik ihre Zeit von außen herein: Alle
 * Funktionen bekommen `nowMillis` und den Zeitzonenversatz übergeben, statt die
 * Uhr selbst zu lesen. Dadurch ist der Zyklus ohne Android und ohne laufenden
 * Prozess prüfbar — und ein Gerät, das drei Tage aus war, holt beim nächsten
 * Start das Richtige nach.
 */
object DailyCycle {

    private const val MILLIS_PER_HOUR = 60 * 60_000L
    const val MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR

    /**
     * Wann der Tag wechselt, in Ortszeit.
     *
     * Vier Uhr morgens statt Mitternacht: Wer abends um halb zwölf noch spielt,
     * soll nicht mitten in der Sitzung seinen Punktestand verlieren. Um vier
     * schläft praktisch jede Spielerin, der Wechsel passiert damit unbemerkt.
     */
    const val RESET_HOUR_LOCAL = 4

    /**
     * Die laufende Nummer des Tages, zu dem ein Zeitpunkt gehört.
     *
     * Fortlaufend und lückenlos — die Differenz zweier Kennungen ist die Zahl
     * der Tage dazwischen. Genau das braucht die Abrechnung, um zu erkennen, ob
     * ein Tag verpasst wurde oder dreißig.
     *
     * @param zoneOffsetMillis der Versatz der Ortszeit gegenüber UTC zum
     *   Zeitpunkt [nowMillis] — von außen hereingereicht, damit hier keine
     *   Zeitzonendatenbank nötig ist.
     */
    fun cycleIdAt(nowMillis: Long, zoneOffsetMillis: Long): Long =
        Math.floorDiv(nowMillis + zoneOffsetMillis - RESET_HOUR_LOCAL * MILLIS_PER_HOUR, MILLIS_PER_DAY)

    /** Wann der Zyklus [cycleId] endet, als Zeitpunkt in UTC-Millisekunden. */
    fun endsAtMillis(cycleId: Long, zoneOffsetMillis: Long): Long =
        (cycleId + 1) * MILLIS_PER_DAY + RESET_HOUR_LOCAL * MILLIS_PER_HOUR - zoneOffsetMillis

    /**
     * Wie viele Sekunden der laufende Tag noch hat.
     *
     * Für den Countdown auf der Levelkarte. Nie negativ: Schlägt die Uhr
     * zwischen zwei Abfragen um, ist der Rest null, und der nächste Abgleich
     * rechnet den neuen Zyklus aus.
     */
    fun remainingSeconds(nowMillis: Long, zoneOffsetMillis: Long): Int {
        val endsAt = endsAtMillis(cycleIdAt(nowMillis, zoneOffsetMillis), zoneOffsetMillis)
        return ((endsAt - nowMillis) / 1000L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }
}

/**
 * Der gespeicherte Stand der Tageswertung.
 *
 * [settledCycleId] merkt sich, welcher Tag zuletzt abgerechnet wurde. Ohne
 * diesen Wert würde ein zweimal geöffnetes Overlay zweimal Belohnung
 * ausschütten — oder, schlimmer, ein Absturz vor dem Anzeigen die Belohnung
 * verschlucken.
 */
data class DailyScore(
    /** Der Tag, zu dem [points] gehören. */
    val cycleId: Long = 0L,
    /** Was heute schon zusammengekommen ist. */
    val points: Int = 0,
    /** Das beste Tagesergebnis, das je erreicht wurde. */
    val bestPoints: Int = 0,
    /** Bis einschließlich dieses Tages wurde abgerechnet. */
    val settledCycleId: Long = 0L,
)

/**
 * Was die Oberfläche von der Tageswertung sieht.
 *
 * Der Reststand ist bereits gegen die Uhr abgeglichen — die Levelkarte zeigt
 * also auch dann null Punkte an, wenn der Tageswechsel noch nicht gespeichert
 * wurde. [remainingSeconds] tickt sichtbar herunter.
 */
data class DailyScoreState(
    val points: Int = 0,
    val bestPoints: Int = 0,
    val remainingSeconds: Int = 0,
)

/**
 * Das Ergebnis einer Abrechnung — die Vorlage für das Abschluss-Overlay.
 *
 * Wird gespeichert und erst gelöscht, wenn die Spielerin das Overlay
 * weggetippt hat. So überlebt der Moment auch einen Absturz oder ein
 * weggewischtes App-Fenster: Das Ergebnis eines ganzen Tages soll nicht davon
 * abhängen, ob die App lange genug offen war.
 */
data class DailySettlement(
    /** Der abgerechnete Tag. */
    val cycleId: Long,
    /** Was an diesem Tag zusammenkam. */
    val points: Int,
    /** Ob das der beste Tag bisher war — der Anlass für den Glückwunsch. */
    val wasBest: Boolean,
    val reward: DailyReward,
)

/**
 * Was ein abgeschlossener Tag einbringt.
 *
 * Die Belohnung fällt in Feenstaub und Irrlichtern aus, nicht in einer neuen
 * Währung. Beides wächst ohnehin über [RegeneratingSupply] nach und wird im
 * Spiel gebraucht — dadurch ist die Tageswertung ein Teil des Spiels und nicht
 * eine Zahl daneben, die man wegtippt.
 */
data class DailyReward(val fairyDust: Int = 0, val irrlicht: Int = 0) {
    val isEmpty: Boolean get() = fairyDust == 0 && irrlicht == 0
}

/**
 * Die Regeln der Tageswertung.
 *
 * Eigene Klasse statt fester Werte im Code, damit die Zyklus-Länge später zum
 * Einstellwert werden kann: Eine Wochen-Liga ist dann ein anderer Wert, kein
 * Umbau. Aus demselben Grund ist die Abrechnung von der Speicherung getrennt —
 * kommt später eine echte Rangliste dazu, ändert sich nur, woher die
 * Platzierung stammt.
 */
object DailyScoring {

    /**
     * Die Belohnungsstufen.
     *
     * Bewusst grob: Drei Stufen sind auf einen Blick verständlich, und die
     * unterste ist so niedrig, dass ein einziges geschafftes Level am Tag schon
     * etwas einbringt. Wer nur kurz hereinschaut, soll nicht leer ausgehen —
     * das ist der ganze Zweck einer Tageswertung.
     */
    private val TIERS = listOf(
        6_000 to DailyReward(fairyDust = 2, irrlicht = 1),
        2_500 to DailyReward(fairyDust = 1, irrlicht = 1),
        500 to DailyReward(fairyDust = 1),
    )

    fun rewardFor(points: Int): DailyReward =
        TIERS.firstOrNull { (threshold, _) -> points >= threshold }?.second ?: DailyReward()

    /**
     * Die nächsthöhere Stufe und wie weit es noch bis dahin ist — oder `null`,
     * wenn die oberste schon erreicht ist.
     *
     * Damit im Overlay „noch 480 Punkte" stehen kann statt nur der jetzigen
     * Belohnung. Ein erreichbares Ziel zieht besser als ein erreichter Stand.
     */
    fun nextTier(points: Int): Pair<Int, DailyReward>? =
        TIERS.lastOrNull { (threshold, _) -> points < threshold }
            ?.let { (threshold, reward) -> (threshold - points) to reward }

    /**
     * Gleicht gespeicherten Stand und Uhrzeit ab.
     *
     * Läuft noch derselbe Tag, bleibt alles wie es ist. Ist der Tag vorüber,
     * entsteht eine Abrechnung, der Zähler beginnt bei null, und das beste
     * Tagesergebnis zieht nach.
     *
     * Mehrere verpasste Tage erzeugen genau **eine** Abrechnung, nämlich die
     * des zuletzt gespielten Tages. Wer zwei Wochen weg war, soll nicht
     * vierzehn Overlays wegtippen müssen — und für die leeren Tage dazwischen
     * gäbe es ohnehin nichts zu zeigen.
     *
     * Ein Tag ohne Punkte wird stillschweigend abgehakt, ohne Abrechnung: Ein
     * Overlay über null Punkte wäre keine Belohnung, sondern eine Mahnung.
     */
    fun settle(stored: DailyScore, nowMillis: Long, zoneOffsetMillis: Long): Settlement {
        val currentCycle = DailyCycle.cycleIdAt(nowMillis, zoneOffsetMillis)
        if (currentCycle == stored.cycleId) {
            return Settlement(stored, null)
        }

        // Der gespeicherte Tag liegt in der Vergangenheit — abrechnen, sofern
        // an ihm überhaupt gespielt wurde und er nicht schon abgerechnet ist.
        val worthSettling = stored.points > 0 && stored.cycleId > stored.settledCycleId
        val settlement = if (worthSettling) {
            DailySettlement(
                cycleId = stored.cycleId,
                points = stored.points,
                wasBest = stored.points > stored.bestPoints,
                reward = rewardFor(stored.points),
            )
        } else {
            null
        }

        return Settlement(
            score = DailyScore(
                cycleId = currentCycle,
                points = 0,
                bestPoints = maxOf(stored.bestPoints, stored.points),
                settledCycleId = maxOf(stored.settledCycleId, stored.cycleId),
            ),
            settlement = settlement,
        )
    }

    /**
     * Zählt Punkte zum laufenden Tag.
     *
     * Rechnet vorher ab, falls der Tag inzwischen gewechselt hat: Sonst
     * landeten die Punkte des ersten Levels nach Mitternacht noch auf dem
     * gestrigen Konto.
     */
    fun add(stored: DailyScore, points: Int, nowMillis: Long, zoneOffsetMillis: Long): Settlement {
        val settled = settle(stored, nowMillis, zoneOffsetMillis)
        return settled.copy(
            score = settled.score.copy(points = settled.score.points + points.coerceAtLeast(0)),
        )
    }

    /** Neuer Stand plus, falls einer entstanden ist, der anzuzeigende Abschluss. */
    data class Settlement(val score: DailyScore, val settlement: DailySettlement?)
}
