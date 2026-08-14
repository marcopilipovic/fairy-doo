package com.fairydoo.game.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import java.util.Locale
import kotlin.random.Random

/**
 * Die Stimme des Waldes: das Lob nach einem gelösten Rätsel.
 *
 * Nutzt die Sprachausgabe des Geräts statt aufgenommener Sprache, damit das
 * Lob den Spielstand nennen kann — „Level 4 geschafft" lässt sich nicht
 * aufnehmen, es gibt zu viele Level.
 *
 * **Sie spricht nur noch hier.** Bis dahin rief die Sprachausgabe auch bei
 * jeder richtig gesetzten Fee ein Wort — „Juhuu!", „Jippie!", je Art ein
 * anderes. Beim Spielen setzt man dutzende Feen, und ein gesprochenes Wort
 * verträgt diese Wiederholung nicht; an ihre Stelle ist ein kurzer berechneter
 * Klick getreten (siehe `FairySounds.place`). Ein einmaliges Lob am Levelende
 * ist etwas anderes — es kommt selten und darf deshalb sprechen.
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
        engine?.setPitch(FAIRY_PITCH)
        engine?.setSpeechRate(FAIRY_RATE)
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

    /**
     * Was eine Fee sagt, wenn ein Level geschafft ist.
     *
     * **Zwanzig statt sechs.** Bei sechs sah man jeden Satz nach zwanzig Leveln
     * dreimal — und ab dem zweiten Mal ist ein Lob keins mehr, sondern eine
     * Quittung. Bei einem Spiel ohne letztes Level fällt das schnell auf.
     *
     * **Und keine Zahlen mehr.** Zwei der alten Sätze nannten die Levelnummer
     * und den Punktestand — beides steht im selben Fenster schon da, einmal in
     * der Überschrift und einmal in Grün darunter. Ein Satz, der wiederholt,
     * was daneben steht, hat seine Aufgabe verfehlt: Er soll etwas sagen, das
     * sonst nirgends steht.
     *
     * Sie werden **vorgelesen**, nicht nur angezeigt. Deshalb keine Zeichen,
     * die eine Sprachausgabe stolpern lassen, keine Klammern, keine Ziffern —
     * und Sätze, die gesprochen ebenso tragen wie gelesen.
     *
     * Der Ton ist der des Spiels: Nachtwald, Mondlicht, Tau, Glühwürmchen.
     * Nicht kindlich — Fairydoku ist für die ganze Bandbreite gedacht.
     */
    private fun praisePhrases(level: Int, score: Int): List<String> = listOf(
        "Wunderbar. Alle Feen leben in Harmonie.",
        "Großartig gemacht, Hüterin des Waldes.",
        "Der Wald leuchtet für dich.",
        "Du hast ein feines Gespür für Feen.",
        "Perfekt platziert. Weiter so.",
        "Kein Flügel zu viel, kein Platz zu wenig.",
        "So ruhig war die Lichtung lange nicht.",
        "Jede steht genau dort, wo sie hingehört.",
        "Die Glühwürmchen haben dir zugesehen.",
        "Das war klug gedacht.",
        "Der Tau glitzert, als hätte er darauf gewartet.",
        "Ordnung im Wald, und du hast sie gemacht.",
        "Keine einzige musste zweimal überlegen.",
        "Der Mond steht günstig für dich.",
        "Die Feen tuscheln. Es klingt nach Anerkennung.",
        "Sauber gelöst, ohne ein Blatt zu verrücken.",
        "Du liest die Lichtung wie ein Buch.",
        "Still geworden. So klingt es, wenn es stimmt.",
        "Da hat jemand aufgepasst.",
        "Der Wald atmet auf.",
    )

    private companion object {
        const val TAG = "FairyVoice"

        /** Deutlich höher als normal — das macht aus der Systemstimme eine Fee. */
        const val FAIRY_PITCH = 1.6f
        const val FAIRY_RATE = 1.05f
    }
}
