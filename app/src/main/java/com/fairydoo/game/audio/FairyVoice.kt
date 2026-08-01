package com.fairydoo.game.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import java.util.Locale
import kotlin.random.Random

/**
 * Die Stimme, die nach einem gelösten Rätsel lobt.
 *
 * Nutzt die Sprachausgabe des Geräts statt aufgenommener Sprache: Damit kann
 * das Lob den Spielstand nennen („Level 4 geschafft") — mit festen Aufnahmen
 * ginge das nur mit einem Satzbaukasten für jede Zahl.
 *
 * Hohe Tonlage und leicht erhöhtes Tempo lassen die Systemstimme nach Fee
 * klingen. Fehlt auf dem Gerät eine deutsche Stimme, bleibt sie still — das
 * Spiel funktioniert auch ohne.
 */
class FairyVoice(context: Context) {

    private var engine: TextToSpeech? = null
    private var ready = false

    init {
        val listener = OnInitListener { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.i(TAG, "Sprachausgabe nicht verfügbar — Lob bleibt stumm.")
                return@OnInitListener
            }

            val result = engine?.setLanguage(Locale.GERMAN)
            ready = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED

            if (ready) {
                engine?.setPitch(FAIRY_PITCH)
                engine?.setSpeechRate(FAIRY_RATE)
            } else {
                Log.i(TAG, "Keine deutsche Stimme installiert — Lob bleibt stumm.")
            }
        }
        engine = TextToSpeech(context.applicationContext, listener)
    }

    /** Lobt zum abgeschlossenen Level. */
    fun praise(
        level: Int,
        score: Int,
        volume: Float = 1f,
        random: Random = Random.Default,
    ) {
        if (!ready) return

        val phrase = praisePhrases(level, score).let { it[random.nextInt(it.size)] }
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
        }
        engine?.speak(phrase, TextToSpeech.QUEUE_FLUSH, params, "praise-$level")
    }

    fun stop() {
        engine?.stop()
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }

    private fun praisePhrases(level: Int, score: Int): List<String> = listOf(
        "Wunderbar! Alle Feen leben in Harmonie.",
        "Großartig gemacht, Hüterin des Waldes!",
        "Level $level geschafft. Der Wald leuchtet für dich!",
        "Zauberhaft! Schon $score Punkte.",
        "Du hast ein feines Gespür für Feen.",
        "Perfekt platziert! Weiter so.",
    )

    private companion object {
        const val TAG = "FairyVoice"

        /** Deutlich höher als normal — das macht aus der Systemstimme eine Fee. */
        const val FAIRY_PITCH = 1.6f
        const val FAIRY_RATE = 1.05f
    }
}
