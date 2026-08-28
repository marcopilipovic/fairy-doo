package com.fairydoo.game.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Was der Werbe-Knopf gerade anbieten kann. */
enum class AdOffer {
    /** Der Knopf lässt sich drücken — entweder liegt eine Anzeige bereit oder sie wird geholt. */
    Available,

    /** Einwilligung, SDK-Start oder Anzeige werden gerade geladen. */
    Preparing,

    /** Keine Einwilligung oder dauerhaft keine Anzeige — der Knopf bleibt aus. */
    Unavailable,
}

/**
 * Kapselt Einwilligung, Laden und Zeigen einer Rewarded-Anzeige (Werbung
 * ansehen, dafür ein Feenstaub/Irrlicht/Leben extra).
 *
 * ## Warum hier nichts beim App-Start passiert
 *
 * Bis Level [com.fairydoo.game.game.GameViewModel.ADS_UNLOCK_AFTER_LEVEL] gibt
 * es überhaupt keine Werbe-Knöpfe, und Fairydoku hat keine Banner: Werbung
 * kommt ausschließlich, wenn jemand sie selbst anfordert. Trotzdem startete
 * das SDK früher beim App-Start und lud eine Anzeige vor — bei *jedem*
 * Spieler, auch bei denen, die nie einen Werbe-Knopf anfassen. Damit flossen
 * Gerätedaten an Google, ohne dass es je einen Anlass gab.
 *
 * Jetzt geschieht bis zum ersten Tippen nichts. Wer nie Werbung ansieht, bei
 * dem verlässt nichts das Gerät. Das ist nicht nur sauberer, es macht auch die
 * Datenschutzerklärung einfacher: Sie kann sagen, dass ohne Zutun nichts
 * passiert, statt es zu erklären.
 *
 * Der Preis ist ein paar Sekunden Wartezeit beim allerersten Mal. Ab dem
 * zweiten ist es wie vorher, weil nach jeder gesehenen Anzeige sofort die
 * nächste nachgeladen wird.
 *
 * ## Warum die Einwilligung sein muss
 *
 * Für Nutzer im EWR und in Großbritannien verlangt Google seit Anfang 2024
 * eine zertifizierte Einwilligungslösung; ohne sie schränkt Google die
 * Auslieferung ein. Unabhängig davon verlangt § 25 TDDDG eine Einwilligung
 * dafür, dass eine App auf dem Gerät eine Werbekennung ausliest — und zwar
 * auch dann, wenn die Werbung wie hier ausdrücklich nicht personalisiert ist.
 * „Nicht personalisiert" heißt eben nicht „ohne Zugriff aufs Gerät".
 *
 * Googles User Messaging Platform übernimmt beides. Außerhalb des EWR meldet
 * sie schlicht, dass kein Formular nötig ist, und es geht ohne Dialog weiter.
 *
 * TEMP: Googles offizielle Test-Anzeigen-ID — es gibt noch kein eigenes
 * AdMob-Konto. Vor Veröffentlichung durch die echte Ad-Unit-ID ersetzen,
 * zusammen mit der App-ID im AndroidManifest.
 */
class RewardedAdManager(private val appContext: Context) {

    private var rewardedAd: RewardedAd? = null
    private var sdkStarted = false

    /** Für den Wachhund in [onAdRequested]; alle Rückrufe kommen ohnehin hier an. */
    private val hauptSchleife = Handler(Looper.getMainLooper())
    private var wachhund: Runnable? = null

    private val consent: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(appContext)

    private val _offer = MutableStateFlow(AdOffer.Available)
    /** Steuert Beschriftung und Zustand der Werbe-Knöpfe. */
    val offer: StateFlow<AdOffer> = _offer.asStateFlow()

    private val _privacyOptionsAvailable = MutableStateFlow(false)
    /**
     * Ob der Menüpunkt „Datenschutz-Einstellungen ändern" gezeigt werden soll.
     *
     * Er erscheint erst, wenn es tatsächlich etwas zu ändern gibt — also nachdem
     * eine Einwilligung abgefragt wurde. Ein Menüpunkt, der ein leeres Formular
     * öffnet, verwirrt mehr, als er hilft.
     */
    val privacyOptionsAvailable: StateFlow<Boolean> = _privacyOptionsAvailable.asStateFlow()

    /**
     * Ein Druck auf einen Werbe-Knopf.
     *
     * Kümmert sich um alles, was gerade nötig ist: Einwilligung einholen, das
     * SDK starten, eine Anzeige laden, sie zeigen. Liegt schon eine bereit,
     * läuft nur der letzte Schritt.
     */
    fun onAdRequested(
        activity: Activity,
        onReward: () -> Unit,
        onFinished: () -> Unit = {},
    ) {
        // Genau einmal, komme was wolle.
        //
        // An [onFinished] hängt, dass das Spiel weiterläuft — der Aufrufer hat
        // es angehalten, bevor er hier hereinkam. Wird die Meldung verschluckt,
        // bleibt das Spiel für immer stehen, und der Spieler kann sich nur noch
        // durch Beenden und Neustarten befreien. Genau das ist passiert: Der
        // Ausstieg „es lädt schon eine" unten meldete sich nicht zurück.
        //
        // Deshalb kommt jede Rückmeldung durch dieses Nadelöhr. Es lässt die
        // erste durch und schluckt alle weiteren — doppelte Meldungen sind
        // ebenso möglich wie gar keine.
        var gemeldet = false
        val fertig = {
            if (!gemeldet) {
                gemeldet = true
                wachhund?.let(hauptSchleife::removeCallbacks)
                wachhund = null
                onFinished()
            }
        }

        // Der Wachhund für die Vorbereitung: Einwilligungsdialog und Laden sind
        // fremde Rückrufe, und ein Rückruf, der nie kommt, ist nicht
        // auszuschließen — etwa wenn der Einwilligungsdialog durch einen
        // Wechsel in den Hintergrund verschwindet. Bellt er, läuft das Spiel
        // weiter, als wäre nichts gewesen.
        //
        // Er wird abbestellt, sobald eine Anzeige tatsächlich auf dem Schirm
        // ist: Ab da ist Warten richtig, die Anzeige darf ruhig eine Minute
        // dauern.
        wachhund = Runnable {
            Log.w(TAG, "Keine Rückmeldung nach ${WATCHDOG_MILLIS / 1000} s — Spiel läuft weiter")
            _offer.value = AdOffer.Available
            fertig()
        }.also { hauptSchleife.postDelayed(it, WATCHDOG_MILLIS) }

        rewardedAd?.let { show(activity, it, onReward, fertig) ; return }
        if (_offer.value == AdOffer.Preparing) {
            // Es lädt bereits eine Anzeige aus einem früheren Tippen. Hier fehlte
            // die Rückmeldung — der Grund für die Hängepartie.
            fertig()
            return
        }

        _offer.value = AdOffer.Preparing
        ensureConsent(activity) { stand ->
            if (stand != Consent.Erteilt) {
                // Abgelehnt heißt aus; unklar heißt "später nochmal", sonst
                // kostet ein Funkloch alle weiteren Anzeigen dieser Sitzung.
                _offer.value =
                    if (stand == Consent.Abgelehnt) AdOffer.Unavailable else AdOffer.Available
                fertig()
                return@ensureConsent
            }
            startSdk()
            load { geladen ->
                if (geladen) {
                    // Der Spieler wartet seit seinem Tippen — die Anzeige
                    // kommt jetzt von selbst, ohne dass er erneut drücken muss.
                    val ad = rewardedAd
                    if (ad != null) show(activity, ad, onReward, fertig) else fertig()
                } else {
                    // Kein Netz, kein Inventar: beim nächsten Tippen neu
                    // versuchen, statt den Knopf dauerhaft auszuschalten.
                    _offer.value = AdOffer.Available
                    fertig()
                }
            }
        }
    }

    /** Öffnet das Einwilligungsformular erneut — für den Menüpunkt in den Einstellungen. */
    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w(TAG, "Datenschutz-Formular: ${error.message}")
            refreshPrivacyOptions()
        }
    }

    /** Wie eine Einwilligungsabfrage ausgegangen ist. */
    private enum class Consent {
        /** Es darf angefragt werden. */
        Erteilt,

        /** Der Spieler hat abgelehnt — dabei bleibt es, bis er es selbst ändert. */
        Abgelehnt,

        /** Nicht erreichbar, etwa ohne Netz — beim nächsten Tippen neu versuchen. */
        Unklar,
    }

    /**
     * Holt den Einwilligungsstand und zeigt bei Bedarf das Formular.
     *
     * Außerhalb des EWR meldet das Werkzeug ohne jeden Dialog, dass angefragt
     * werden darf.
     *
     * Der Unterschied zwischen [Consent.Abgelehnt] und [Consent.Unklar] ist
     * bares Geld: Ohne Netz schlägt die Abfrage fehl, und wer das mit einer
     * Ablehnung gleichsetzt, schaltet den Werbe-Knopf für den Rest der Sitzung
     * aus — ein Funkloch beim ersten Versuch kostet dann alle weiteren
     * Anzeigen.
     */
    private fun ensureConsent(activity: Activity, onDone: (Consent) -> Unit) {
        // Ab 13 Jahren: Die Nutzer gelten nicht als „unter dem Einwilligungs-
        // alter" im Sinne der Google-Richtlinie — dieselbe Aussage wie in
        // AGB und Datenschutzerklärung.
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consent.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) Log.w(TAG, "Einwilligungsformular: ${error.message}")
                    refreshPrivacyOptions()
                    onDone(if (consent.canRequestAds()) Consent.Erteilt else Consent.Abgelehnt)
                }
            },
            { error ->
                // Kein Netz, Dienst nicht erreichbar: Das ist keine Ablehnung.
                Log.w(TAG, "Einwilligungsstand nicht abrufbar: ${error.message}")
                onDone(Consent.Unklar)
            },
        )
    }

    private fun refreshPrivacyOptions() {
        _privacyOptionsAvailable.value =
            consent.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /** Startet das Werbe-SDK — einmal, und erst nach erteilter Einwilligung. */
    private fun startSdk() {
        if (sdkStarted) return
        sdkStarted = true

        // Zwei Einstellungen, die zwei verschiedene Dinge regeln — und die man
        // leicht verwechselt.
        //
        // "G" begrenzt, *was* an Werbung kommen darf: Glücksspiel-, Gewalt- und
        // sexuelle Inhalte sind damit schon auf Anzeigen-Ebene ausgeschlossen.
        // Das gilt unabhängig davon, an wen sich die App richtet, und bleibt so
        // — die Inhalte des Spiels sind für jedes Alter unbedenklich.
        //
        // Die zweite sagt Google, *an wen* sich die App richtet: ab 13 Jahren
        // und damit kein an Kinder gerichtetes Angebot im Sinne der
        // Play-Store-Familienrichtlinien. Genau so steht es auch in AGB und
        // Datenschutzerklärung (siehe [com.fairydoo.game.ui.GameCopy.legalBody]).
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .setTagForChildDirectedTreatment(
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE,
                )
                .build(),
        )
        MobileAds.initialize(appContext) {}
    }

    private fun load(onDone: (Boolean) -> Unit = {}) {
        if (rewardedAd != null) {
            onDone(true)
            return
        }

        // Ausdrücklich unpersonalisierte Anfrage: Die App zeigt keine
        // personalisierte Werbung. Das "npa"-Flag ist die von Google
        // dokumentierte Art, das je Anfrage zu erzwingen — die Einstellung im
        // Play-Store-Konsolen-Fragebogen allein reicht dafür nicht, sie
        // betrifft nur die Einstufung der App, nicht die einzelne Anfrage.
        val nonPersonalizedExtras = Bundle().apply { putString("npa", "1") }
        val request = AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, nonPersonalizedExtras)
            .build()

        RewardedAd.load(
            appContext,
            AD_UNIT_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _offer.value = AdOffer.Available
                    onDone(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    Log.w(TAG, "Anzeige nicht ladbar: ${error.message}")
                    onDone(false)
                }
            },
        )
    }

    /**
     * Zeigt die Anzeige und ruft [onReward] auf, sobald sie zu Ende gesehen
     * wurde. Lädt danach sofort die nächste nach, damit das nächste Tippen
     * nicht wieder warten muss.
     *
     * [onFinished] meldet, dass der Spieler wieder vor dem Spiel sitzt — egal,
     * ob er die Anzeige zu Ende gesehen oder weggetippt hat. Daran hängt, dass
     * die Uhr weiterläuft, die für die Dauer der Anzeige stillstand.
     */
    private fun show(
        activity: Activity,
        ad: RewardedAd,
        onReward: () -> Unit,
        onFinished: () -> Unit,
    ) {
        _offer.value = AdOffer.Available

        // Ab hier zählt nur noch, was das SDK meldet: Die Anzeige läuft, und
        // sie darf so lange dauern, wie sie will. Der Wachhund aus
        // [onAdRequested] hätte sonst mitten hineingebellt.
        wachhund?.let(hauptSchleife::removeCallbacks)
        wachhund = null

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                load()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                Log.w(TAG, "Anzeige nicht zeigbar: ${error.message}")
                load()
                onFinished()
            }
        }
        ad.show(activity) { onReward() }
    }

    private companion object {
        const val TAG = "RewardedAds"
        const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

        /**
         * Wie lange auf Einwilligung und Ladevorgang gewartet wird, bevor das
         * Spiel ohne Anzeige weiterläuft.
         *
         * Zwölf Sekunden: lang genug, dass eine langsame Verbindung eine
         * Anzeige noch schafft, kurz genug, dass niemand glaubt, die App sei
         * abgestürzt. Sobald eine Anzeige läuft, gilt die Frist nicht mehr.
         */
        const val WATCHDOG_MILLIS = 12_000L
    }
}
