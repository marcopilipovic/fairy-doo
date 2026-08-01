package com.fairydoo.game.game

/**
 * Der App-weite Lebenspool — getrennt von den drei Leben pro Level.
 *
 * Ein verlorenes Level kostet eins von fünf; alle [REGEN_INTERVAL_MILLIS]
 * wächst eins nach, bis wieder alle fünf voll sind. Reine Funktionen, damit
 * sich das Nachwachsen ohne Android-Uhr und ohne laufenden Prozess testen
 * lässt — der Zustand trägt seine eigene Zeitbasis ([nextLifeAtMillis]).
 */
data class GlobalLivesState(
    val lives: Int,
    /** Wann das nächste Leben nachwächst; 0, solange der Vorrat voll ist. */
    val nextLifeAtMillis: Long,
)

object GlobalLives {
    const val MAX = 5
    const val REGEN_INTERVAL_MILLIS = 5 * 60_000L

    /**
     * Gleicht gespeicherten Stand und Uhrzeit ab — holt nach, was seit dem
     * letzten Zugriff nachgewachsen ist (auch nach Stunden außerhalb der App).
     */
    fun normalize(storedLives: Int, nextLifeAtMillis: Long, nowMillis: Long): GlobalLivesState {
        if (storedLives >= MAX || nextLifeAtMillis == 0L) {
            return GlobalLivesState(storedLives.coerceIn(0, MAX), 0L)
        }
        if (nowMillis < nextLifeAtMillis) {
            return GlobalLivesState(storedLives, nextLifeAtMillis)
        }

        val elapsedPastFirst = nowMillis - nextLifeAtMillis
        val regains = 1 + elapsedPastFirst / REGEN_INTERVAL_MILLIS
        val newLives = (storedLives + regains).coerceAtMost(MAX.toLong()).toInt()
        val newNextAt = if (newLives >= MAX) 0L else nextLifeAtMillis + regains * REGEN_INTERVAL_MILLIS
        return GlobalLivesState(newLives, newNextAt)
    }

    /** Verbraucht ein Leben — startet die Nachwachs-Uhr, falls sie nicht schon lief. */
    fun consume(state: GlobalLivesState, nowMillis: Long): GlobalLivesState {
        val newLives = (state.lives - 1).coerceAtLeast(0)
        val newNextAt = if (state.nextLifeAtMillis == 0L) nowMillis + REGEN_INTERVAL_MILLIS else state.nextLifeAtMillis
        return GlobalLivesState(newLives, newNextAt)
    }
}
