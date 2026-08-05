package com.fairydoo.game.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import com.fairydoo.game.game.FairySpecies
import java.util.Locale
import kotlin.random.Random

/**
 * Die Stimme des Waldes: lobt nach einem gelösten Rätsel und lässt jede Fee
 * beim Setzen ihren eigenen kurzen Ausruf hören.
 *
 * Nutzt die Sprachausgabe des Geräts statt aufgenommener Sprache — beim Lob,
 * damit es den Spielstand nennen kann („Level 4 geschafft"), bei den
 * Ausrufen, damit keine Aufnahmen mit unklaren Rechten im Spiel landen.
 *
 * Hohe Tonlage und leicht erhöhtes Tempo lassen die Systemstimme nach Fee
 * klingen; jeder Ausruf hat zusätzlich seine eigene Tonhöhe/Tempo (siehe
 * [FairyExclamations]). Fehlt auf dem Gerät eine deutsche Stimme, bleibt sie
 * still — das Spiel funktioniert auch ohne.
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
        // Tonhöhe/Tempo explizit zurücksetzen — ein zuvor gesetzter Feen-Ausruf
        // (siehe [exclaim]) hätte sie sonst auf den letzten Ausruf stehen
        // lassen.
        engine?.setPitch(FAIRY_PITCH)
        engine?.setSpeechRate(FAIRY_RATE)
        engine?.speak(phrase, TextToSpeech.QUEUE_FLUSH, params, "praise-$level")
    }

    /**
     * Der kurze, artspezifische Ausruf beim Setzen einer Fee — siehe
     * [FairyExclamations]. `QUEUE_ADD` statt `QUEUE_FLUSH`, damit beim
     * schnellen Setzen mehrerer Feen jeder Ausruf zu Ende gesprochen wird,
     * statt vom nächsten abgeschnitten zu werden.
     */
    fun exclaim(species: FairySpecies, volume: Float = 1f) {
        if (!ready) return

        val line = FairyExclamations.of(species)
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0f, 1f))
        }
        engine?.setPitch(line.pitch)
        engine?.setSpeechRate(line.rate)
        engine?.speak(line.text, TextToSpeech.QUEUE_ADD, params, "exclaim-${species.name}")
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
