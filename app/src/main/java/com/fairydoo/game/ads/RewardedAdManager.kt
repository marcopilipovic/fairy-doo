package com.fairydoo.game.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Kapselt das Laden und Zeigen einer Rewarded-Anzeige (Werbung ansehen, dafür
 * ein Feenstaub/Irrlicht/Leben extra).
 *
 * TEMP: Googles offizielle Test-Anzeigen-ID — es gibt noch kein eigenes
 * AdMob-Konto. Vor Veröffentlichung durch die echte Ad-Unit-ID ersetzen,
 * zusammen mit der App-ID im AndroidManifest.
 */
class RewardedAdManager(private val appContext: Context) {

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    private val _isReady = MutableStateFlow(false)
    /** Ob gerade eine Anzeige bereitsteht — die UI blendet den Werbe-Knopf danach ein/aus. */
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    /**
     * Einmal beim App-Start: initialisiert das SDK und lädt die erste Anzeige
     * vor.
     *
     * Wird erst aufgerufen, wenn [AdConsentManager] die Einwilligung geklärt
     * hat — vorher darf keine Anfrage an Google gehen. Ohne Einwilligung wird
     * gar nicht initialisiert: Die App bleibt vollständig spielbar, es entfällt
     * nur das Angebot, für eine Belohnung ein Video anzusehen.
     */
    fun init() {
        // Fairydoku ist für alle Altersstufen freigegeben — "G" filtert
        // Glücksspiel-, Gewalt- und sexuelle Werbeinhalte schon auf
        // Anzeigen-Ebene heraus, unabhängig von der Zielgruppen-Einstufung im
        // Play-Store-Konsolen-Fragebogen.
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build(),
        )
        MobileAds.initialize(appContext) {}
        load()
    }

    private fun load() {
        if (rewardedAd != null || isLoading) return
        isLoading = true

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
                    isLoading = false
                    rewardedAd = ad
                    _isReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    rewardedAd = null
                    _isReady.value = false
                }
            },
        )
    }

    /**
     * Zeigt die vorgeladene Anzeige und ruft [onReward] auf, sobald sie zu
     * Ende gesehen wurde. Lädt danach sofort die nächste nach, damit die
     * nächste Anfrage nicht ins Leere läuft.
     */
    fun show(activity: Activity, onReward: () -> Unit) {
        val ad = rewardedAd ?: return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _isReady.value = false
                load()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _isReady.value = false
                load()
            }
        }
        ad.show(activity) { onReward() }
    }

    private companion object {
        const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
