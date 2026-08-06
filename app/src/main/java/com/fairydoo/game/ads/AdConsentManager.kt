package com.fairydoo.game.ads

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holt die Werbe-Einwilligung ein, bevor die erste Anzeige geladen wird.
 *
 * Google verlangt für Nutzerinnen im EWR und im Vereinigten Königreich ein
 * zertifiziertes Einwilligungswerkzeug — **auch dann, wenn ausschließlich nicht
 * personalisierte Werbung ausgeliefert wird.** Fairydoku zeigt nur solche
 * Werbung; die Pflicht besteht trotzdem, weil bereits das Ausliefern einer
 * Anzeige Gerätedaten berührt.
 *
 * Benutzt wird Googles eigenes UMP-SDK. Es ist kostenlos, zertifiziert und
 * bringt das Dialogfenster mit; welche Fragen darin stehen, wird nicht hier,
 * sondern in der AdMob-Konsole eingerichtet.
 *
 * Außerhalb des EWR wird kein Dialog gezeigt — dort meldet das SDK von sich aus,
 * dass keine Einwilligung nötig ist, und die Werbung startet sofort.
 */
class AdConsentManager(private val activity: Activity) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    private val _canRequestAds = MutableStateFlow(false)

    /**
     * Ob Anzeigen angefragt werden dürfen.
     *
     * Bleibt `false`, solange die Einwilligung aussteht oder verweigert wurde.
     * [RewardedAdManager] darf erst danach starten — sonst liefe eine Anfrage
     * an Google, bevor gefragt wurde.
     */
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)

    /**
     * Ob die Einstellungen einen Punkt „Datenschutz-Einstellungen" zeigen
     * müssen.
     *
     * Wo eine Einwilligung eingeholt wird, muss sie auch widerrufbar sein.
     * Außerhalb des EWR gibt es nichts zu widerrufen, und der Punkt bliebe eine
     * Sackgasse — deshalb erscheint er nur, wenn das SDK ihn verlangt.
     */
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    /**
     * Fragt den Einwilligungsstand ab und zeigt bei Bedarf das Formular.
     *
     * [onFinished] läuft in jedem Fall — auch bei einem Fehler. Ein Netzausfall
     * oder ein Gerät ohne Play-Dienste darf nicht dazu führen, dass die App
     * hängt: Das Spiel funktioniert ohne Werbung vollständig, es entfällt dann
     * lediglich das Angebot, für eine Belohnung ein Video anzusehen.
     */
    fun gather(onFinished: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    // Der Parameter ist ein Fehler oder null. Beides ändert
                    // nichts am weiteren Ablauf: Gefragt wurde, das Ergebnis
                    // steht in `canRequestAds`.
                    publish()
                    onFinished()
                }
            },
            {
                // Konnte nicht ermittelt werden — dann keine Werbung. Lieber
                // auf die Einnahme verzichten als ohne Einwilligung ausliefern.
                publish()
                onFinished()
            },
        )
    }

    /**
     * Öffnet das Formular erneut, damit die Wahl geändert werden kann.
     *
     * Aufgerufen aus den Einstellungen, wenn [privacyOptionsRequired] gilt.
     */
    fun showPrivacyOptions() {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { publish() }
    }

    private fun publish() {
        _canRequestAds.value = consentInformation.canRequestAds()
        _privacyOptionsRequired.value =
            consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Setzt die Einwilligung zurück — nur für die Entwicklung.
     *
     * Ohne das ließe sich das Formular nach der ersten Antwort nicht wieder
     * hervorholen, ohne die App zu deinstallieren.
     */
    fun resetForTesting() {
        consentInformation.reset()
    }

    companion object {
        /**
         * Lässt das Gerät beim Testen wie eines im EWR aussehen.
         *
         * Von hier aus nicht verdrahtet — wer das Formular prüfen will, baut
         * die Kennung des Testgeräts hier ein und reicht die Einstellungen an
         * [ConsentRequestParameters] weiter. Die Kennung steht beim ersten
         * Start im Logcat.
         */
        fun debugSettings(activity: Activity, hashedDeviceId: String): ConsentDebugSettings =
            ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(hashedDeviceId)
                .build()
    }
}
