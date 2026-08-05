package com.fairydoo.game.game

/**
 * Der App-weite Lebenspool — getrennt von den drei Leben pro Level.
 *
 * Ein verlorenes Level kostet eins von fünf; alle [REGEN_INTERVAL_MILLIS] wächst
 * eins nach, bis wieder alle fünf voll sind.
 *
 * Die Rechnerei selbst steht in [RegeneratingSupply] — dieselbe Mechanik trägt
 * auch den Feenstaub. Hier bleiben nur die Werte, die diesen Vorrat von jenem
 * unterscheiden, und die Namen, unter denen ihn der Rest des Spiels kennt.
 */
data class GlobalLivesState(
    val lives: Int,
    /** Wann das nächste Leben nachwächst; 0, solange der Vorrat voll ist. */
    val nextLifeAtMillis: Long,
)

object GlobalLives {
    const val MAX = 5

    /**
     * Wie lange ein Wald-Leben zum Nachwachsen braucht.
     *
     * Zwei Stunden, wie Feenstaub und Irrlicht — alle drei Vorräte wachsen
     * jetzt im selben Takt nach.
     */
    const val REGEN_INTERVAL_MILLIS = 2 * 60 * 60_000L

    private val supply = RegeneratingSupply(MAX, REGEN_INTERVAL_MILLIS)

    /**
     * Gleicht gespeicherten Stand und Uhrzeit ab — holt nach, was seit dem
     * letzten Zugriff nachgewachsen ist (auch nach Stunden außerhalb der App).
     */
    fun normalize(storedLives: Int, nextLifeAtMillis: Long, nowMillis: Long): GlobalLivesState =
        supply.normalize(storedLives, nextLifeAtMillis, nowMillis).toLives()

    /** Verbraucht ein Leben — startet die Nachwachs-Uhr, falls sie nicht schon lief. */
    fun consume(state: GlobalLivesState, nowMillis: Long): GlobalLivesState =
        supply.consume(SupplyState(state.lives, state.nextLifeAtMillis), nowMillis).toLives()

    private fun SupplyState.toLives() = GlobalLivesState(amount, nextAtMillis)
}
