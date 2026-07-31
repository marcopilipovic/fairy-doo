package com.fairydoo.game.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.data.PlayerProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Hält den Spielzustand und treibt die Spieluhr.
 *
 * Der Loop läuft im [viewModelScope] statt in der Composition, damit ein
 * Recompose ihn nicht neu startet und ein Konfigurationswechsel die laufende
 * Partie nicht abbricht.
 */
class GameViewModel(
    private val engine: GameEngine = PlaceholderEngine(),
    private val preferences: GamePreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    val profile: StateFlow<PlayerProfile> = preferences.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerProfile(),
    )

    private var loopJob: Job? = null

    fun startNewGame(level: Int = 1) {
        loopJob?.cancel()
        _state.value = engine.newGame(level)
        loopJob = viewModelScope.launch { runLoop() }
    }

    fun onInput(input: GameInput) {
        _state.value = engine.onInput(_state.value, input)
    }

    fun pause() {
        if (_state.value.status != GameStatus.Running) return
        loopJob?.cancel()
        loopJob = null
        _state.value = _state.value.copy(status = GameStatus.Paused)
    }

    fun resume() {
        if (_state.value.status != GameStatus.Paused) return
        _state.value = _state.value.copy(status = GameStatus.Running)
        loopJob = viewModelScope.launch { runLoop() }
    }

    /** Bricht die laufende Partie ab, ohne sie als gespielt zu werten. */
    fun abandon() {
        loopJob?.cancel()
        loopJob = null
        _state.value = GameState()
    }

    /**
     * Die Spieluhr: feste Schrittweite, gespeist aus echter verstrichener Zeit.
     *
     * Fester Zeitschritt statt roher Frame-Deltas, damit die Simulation auch bei
     * Rucklern deterministisch bleibt — Voraussetzung für reproduzierbare
     * Punktestände und für Tests, die die Engine direkt takten.
     */
    private suspend fun runLoop() {
        var accumulator = 0L
        var lastMillis = System.nanoTime() / 1_000_000

        while (viewModelScope.isActive && _state.value.status == GameStatus.Running) {
            delay(TICK_MILLIS)

            val now = System.nanoTime() / 1_000_000
            // Deckelung, damit ein langer Hintergrundaufenthalt nicht Hunderte
            // Ticks auf einmal nachholt.
            accumulator += (now - lastMillis).coerceIn(0L, MAX_FRAME_MILLIS)
            lastMillis = now

            while (accumulator >= TICK_MILLIS && _state.value.status == GameStatus.Running) {
                accumulator -= TICK_MILLIS
                _state.value = engine.tick(_state.value, TICK_MILLIS)
            }

            if (_state.value.status == GameStatus.Finished) {
                preferences.recordFinishedGame(_state.value.score)
            }
        }
        loopJob = null
    }

    companion object {
        private const val TICK_MILLIS = 16L
        private const val MAX_FRAME_MILLIS = 250L

        /** Für `viewModel(factory = GameViewModel.factory(repository))`. */
        fun factory(preferences: GamePreferencesRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { GameViewModel(preferences = preferences) }
            }
    }
}
