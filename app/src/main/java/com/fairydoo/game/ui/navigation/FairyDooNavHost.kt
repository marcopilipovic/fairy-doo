package com.fairydoo.game.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.ui.screens.GameScreen
import com.fairydoo.game.ui.screens.HomeScreen
import com.fairydoo.game.ui.screens.SettingsScreen

/** Alle Ziele der App an einer Stelle — keine Strings im UI-Code verstreut. */
object Destinations {
    const val HOME = "home"
    const val GAME = "game"
    const val SETTINGS = "settings"
}

@Composable
fun FairyDooNavHost(
    preferences: GamePreferencesRepository,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {

        composable(Destinations.HOME) {
            HomeScreen(
                preferences = preferences,
                onPlay = { navController.navigate(Destinations.GAME) },
                onSettings = { navController.navigate(Destinations.SETTINGS) },
            )
        }

        composable(Destinations.GAME) {
            GameScreen(
                preferences = preferences,
                onExit = { navController.popBackStack() },
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
