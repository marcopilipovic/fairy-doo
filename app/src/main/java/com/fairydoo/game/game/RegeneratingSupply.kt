package com.fairydoo.game.game

/**
 * Ein Vorrat, der sich mit der Zeit von selbst auffüllt.
 *
 * Zwei Dinge im Spiel funktionieren so: die Wald-Leben und der Feenstaub. Beide
 * haben eine Obergrenze, beide wachsen in festen Abständen nach, und beide
 * müssen das auch tun, während die App geschlossen ist. Deshalb liegt die Logik
 * hier statt zweimal nebeneinander.
 *
 * Der Zustand trägt seine eigene Zeitbasis ([nextAtMillis]) — dadurch lässt sich
 * das Nachwachsen ohne Android-Uhr und ohne laufenden Prozess prüfen, und ein
 * Gerät, das drei Stunden aus war, holt beim nächsten Start alles nach.
 */
data class SupplyState(
    val amount: Int,
    /** Wann das nächste Stück nachwächst; 0, solange der Vorrat voll ist. */
    val nextAtMillis: Long,
)

/**
 * Die Regeln eines solchen Vorrats.
 *
 * @param max wie viel höchstens vorrätig ist
 * @param intervalMillis wie lange ein einzelnes Stück zum Nachwachsen braucht
 */
class RegeneratingSupply(val max: Int, val intervalMillis: Long) {

    /**
     * Gleicht gespeicherten Stand und Uhrzeit ab.
     *
     * Holt nach, was seit dem letzten Zugriff nachgewachsen ist — auch nach
     * Stunden außerhalb der App. Ist der Vorrat voll, wird die Uhr angehalten
     * (`nextAtMillis = 0`); sonst rückt sie um so viele volle Abstände vor, wie
     * vergangen sind. Der Rest der Zeit bleibt stehen und zählt weiter, statt
     * verloren zu gehen.
     */
    fun normalize(storedAmount: Int, nextAtMillis: Long, nowMillis: Long): SupplyState {
        if (storedAmount >= max || nextAtMillis == 0L) {
            return SupplyState(storedAmount.coerceIn(0, max), 0L)
        }
        if (nowMillis < nextAtMillis) {
            return SupplyState(storedAmount, nextAtMillis)
        }

        val elapsedPastFirst = nowMillis - nextAtMillis
        val regained = 1 + elapsedPastFirst / intervalMillis
        val newAmount = (storedAmount + regained).coerceAtMost(max.toLong()).toInt()
        val newNextAt = if (newAmount >= max) 0L else nextAtMillis + regained * intervalMillis
        return SupplyState(newAmount, newNextAt)
    }

    /**
     * Verbraucht ein Stück und startet die Nachwachs-Uhr, falls sie stand.
     *
     * Lief sie schon, bleibt sie unangetastet: Sonst könnte man durch Verbrauchen
     * kurz vor Ablauf die Wartezeit immer wieder verlängern — oder, je nach
     * Umsetzung, verkürzen.
     */
    fun consume(state: SupplyState, nowMillis: Long): SupplyState {
        val newAmount = (state.amount - 1).coerceAtLeast(0)
        val newNextAt =
            if (state.nextAtMillis == 0L) nowMillis + intervalMillis else state.nextAtMillis
        return SupplyState(newAmount, newNextAt)
    }
}

/**
 * Der Feenstaub — die einzige Hilfe im Spiel.
 *
 * Drei Stück, und ein verbrauchtes wächst in einer halben Stunde nach. Damit ist
 * er eine Entscheidung und keine Selbstverständlichkeit: Wer ihn bei jedem
 * schweren Feld einsetzt, steht beim nächsten ohne da.
 */
val FairyDustSupply = RegeneratingSupply(max = 3, intervalMillis = 30 * 60_000L)
