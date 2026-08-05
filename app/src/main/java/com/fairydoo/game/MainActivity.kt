package com.fairydoo.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fairydoo.game.ads.RewardedAdManager
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.ui.screens.GameScreen
import com.fairydoo.game.ui.theme.FairyDooTheme

/**
 * Fairydoku schaltet zwischen Levelkarte und Spielbildschirm nur per
 * Zustands-Flag um, nicht über einen Navigations-Graphen — dafür ist bei
 * zwei Bildschirmen (plus Overlays darüber) noch kein Navigations-Framework
 * nötig.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Einfache manuelle Abhängigkeit statt DI-Framework — solange es ein
        // Repository ist, wäre Hilt/Koin nur Zeremonie.
        val preferences = GamePreferencesRepository(applicationContext)

        // Läuft app-weit unabhängig vom Compose-Baum: Eine Anzeige darf über
        // einen Level- oder Bildschirmwechsel hinweg vorgeladen bleiben, sonst
        // wartet man nach jedem Wechsel erneut auf das Laden.
        val ads = RewardedAdManager(applicationContext)
        ads.init()

        setContent {
            FairyDooTheme {
                GameScreen(preferences = preferences, ads = ads)
            }
        }
    }
}
