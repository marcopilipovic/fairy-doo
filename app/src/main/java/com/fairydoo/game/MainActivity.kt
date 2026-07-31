package com.fairydoo.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.ui.navigation.FairyDooNavHost
import com.fairydoo.game.ui.theme.FairyDooTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Einfache manuelle Abhängigkeit statt DI-Framework — solange es ein
        // Repository ist, wäre Hilt/Koin nur Zeremonie. Wenn es mehr werden,
        // ist hier der Ort, an dem ein Container einzieht.
        val preferences = GamePreferencesRepository(applicationContext)

        setContent {
            FairyDooTheme {
                FairyDooNavHost(preferences = preferences)
            }
        }
    }
}
