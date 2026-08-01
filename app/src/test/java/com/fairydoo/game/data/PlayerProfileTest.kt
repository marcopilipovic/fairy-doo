package com.fairydoo.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Die Ableitung „hörbar oder nicht" aus der Lautstärke.
 *
 * Klingt trivial, trägt aber die ganze Oberfläche: Der Regler auf null ist
 * zugleich der Stummschalter, und das Zeichen am Bildschirmrand richtet sich
 * danach.
 */
class PlayerProfileTest {

    @Test
    fun `null bedeutet stumm`() {
        val profile = PlayerProfile(musicVolume = 0f, soundVolume = 0f, voiceVolume = 0f)

        assertFalse(profile.musicEnabled)
        assertFalse(profile.soundEnabled)
        assertFalse(profile.voiceEnabled)
    }

    @Test
    fun `jede Lautstaerke ueber null ist hoerbar`() {
        val profile = PlayerProfile(musicVolume = 0.01f, soundVolume = 1f, voiceVolume = 0.5f)

        assertTrue(profile.musicEnabled)
        assertTrue(profile.soundEnabled)
        assertTrue(profile.voiceEnabled)
    }

    @Test
    fun `die Musik ist voreingestellt leiser als die Klaenge`() {
        // Die Musik läuft ununterbrochen, die Klänge sind Rückmeldung auf
        // eigene Züge und sollen sich darüber behaupten.
        assertTrue(
            "Musik ${PlayerProfile.DEFAULT_MUSIC_VOLUME} " +
                "gegen Klänge ${PlayerProfile.DEFAULT_SOUND_VOLUME}",
            PlayerProfile.DEFAULT_MUSIC_VOLUME < PlayerProfile.DEFAULT_SOUND_VOLUME,
        )
    }

    @Test
    fun `alle Voreinstellungen liegen im gueltigen Bereich`() {
        val defaults = listOf(
            PlayerProfile.DEFAULT_MUSIC_VOLUME,
            PlayerProfile.DEFAULT_SOUND_VOLUME,
            PlayerProfile.DEFAULT_VOICE_VOLUME,
        )

        assertTrue(defaults.all { it in 0f..1f })
        // Ein Spiel, das nach der Installation stumm ist, wirkt kaputt.
        assertTrue("Voreingestellt muss etwas zu hören sein", defaults.all { it > 0f })
    }

    @Test
    fun `ohne Angabe gilt die Voreinstellung`() {
        val profile = PlayerProfile()

        assertEquals(PlayerProfile.DEFAULT_MUSIC_VOLUME, profile.musicVolume, 0.0001f)
        assertEquals(PlayerProfile.DEFAULT_SOUND_VOLUME, profile.soundVolume, 0.0001f)
        assertEquals(PlayerProfile.DEFAULT_VOICE_VOLUME, profile.voiceVolume, 0.0001f)
    }
}
