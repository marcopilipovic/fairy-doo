package com.fairydoo.game.audio

import com.fairydoo.game.R

/**
 * Die aufgenommene Feenstimme aus `res/raw`.
 *
 * Nur der Aufschrei kommt noch als echte Aufnahme ins Spiel. Die Ausrufe der
 * richtig gesetzten Feen liefen früher ebenfalls über Aufnahmen, sind aber auf
 * die Sprachausgabe umgestellt (siehe [FairyVoice.exclaim]) — keine Aufnahme
 * ohne geklärte Rechte im Spiel. Alle übrigen Klänge (Jubel, Ticks,
 * Fähigkeiten, Musik) werden weiterhin berechnet, siehe [FairySounds].
 */
object FairyClips {
    val startled: Int = R.raw.fairy_startled
}
