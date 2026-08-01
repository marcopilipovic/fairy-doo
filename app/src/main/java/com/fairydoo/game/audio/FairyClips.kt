package com.fairydoo.game.audio

import com.fairydoo.game.R

/**
 * Die aufgenommenen Feenstimmen aus `res/raw`.
 *
 * Kichern und Aufschrei kommen als echte Aufnahmen ins Spiel — synthetische
 * Töne treffen zwar das Muster einer Stimme, aber nie ihre Klangfarbe. Alle
 * übrigen Klänge (Jubel, Ticks, Fähigkeiten, Musik) werden weiterhin berechnet,
 * siehe [FairySounds].
 */
object FairyClips {

    /**
     * Wie viele Kicher-Aufnahmen es gibt.
     *
     * Bewusst eine schlichte Zahl ohne Bezug auf die R-Klasse: So kann die
     * Ereignis-Zuordnung im JVM-Unit-Test damit rechnen, ohne dass ein
     * Android-Ressourcensystem vorhanden sein muss.
     */
    const val GIGGLE_COUNT = 6

    /** Die Aufnahmen in fester Reihenfolge; der Index ist die Variante. */
    val giggles: List<Int> = listOf(
        R.raw.fairy_giggle_1,
        R.raw.fairy_giggle_2,
        R.raw.fairy_giggle_3,
        R.raw.fairy_giggle_4,
        R.raw.fairy_giggle_5,
        R.raw.fairy_giggle_6,
    )

    val startled: Int = R.raw.fairy_startled
}
