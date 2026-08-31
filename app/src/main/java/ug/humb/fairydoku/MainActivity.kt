package ug.humb.fairydoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ug.humb.fairydoku.ads.RewardedAdManager
import ug.humb.fairydoku.data.GamePreferencesRepository
import ug.humb.fairydoku.ui.screens.GameScreen
import ug.humb.fairydoku.ui.theme.FairyDooTheme

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

        // Läuft app-weit unabhängig vom Compose-Baum: Eine einmal geladene
        // Anzeige darf über einen Level- oder Bildschirmwechsel hinweg
        // bereitstehen, sonst wartet man nach jedem Wechsel erneut.
        //
        // Hier wird nur das Objekt angelegt, nichts gestartet: Werbe-SDK und
        // Einwilligung kommen erst, wenn zum ersten Mal ein Werbe-Knopf
        // gedrückt wird. Wer nie Werbung ansieht, bei dem verlässt nichts das
        // Gerät.
        val ads = RewardedAdManager(applicationContext)

        setContent {
            FairyDooTheme {
                GameScreen(preferences = preferences, ads = ads)
            }
        }
    }
}
