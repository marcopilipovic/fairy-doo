package com.fairydoo.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.ui.screens.GameScreen
import com.fairydoo.game.ui.theme.FairyDooTheme

/**
 * Fairydoku hat genau einen Bildschirm; alles Weitere sind Overlays darüber.
 * Deshalb gibt es hier keinen Navigations-Graphen.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Einfache manuelle Abhängigkeit statt DI-Framework — solange es ein
        // Repository ist, wäre Hilt/Koin nur Zeremonie.
        val preferences = GamePreferencesRepository(applicationContext)

        setContent {
            FairyDooTheme {
                GameScreen(preferences = preferences)
            }
        }
    }
}
