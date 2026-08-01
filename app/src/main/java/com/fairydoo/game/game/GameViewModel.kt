package com.fairydoo.game.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fairydoo.game.audio.SoundEvent
import com.fairydoo.game.audio.SoundEvents
import com.fairydoo.game.data.GamePreferencesRepository
import com.fairydoo.game.data.PlayerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hält den Spielzustand und treibt die Spieluhr.
 *
 * Der Loop läuft im [viewModelScope] statt in der Composition, damit ein
 * Recompose ihn nicht neu startet und ein Konfigurationswechsel die laufende
 * Partie nicht abbricht.
 */
class GameViewModel(
    private val engine: GameEngine = FairydokuEngine(),
    private val preferences: GamePreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    /** True, solange ein Rätsel erzeugt wird — die UI zeigt so lange einen Ladezustand. */
    private val _isPreparing = MutableStateFlow(true)
    val isPreparing: StateFlow<Boolean> = _isPreparing.asStateFlow()

    val profile: StateFlow<PlayerProfile> = preferences.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerProfile(),
    )

    /**
     * Klangereignisse zum Spielgeschehen.
     *
     * `extraBufferCapacity`, damit schnelle Tipp-Folgen nicht verschluckt
     * werden: Ohne Puffer würde ein Ereignis verworfen, wenn der Sammler gerade
     * beschäftigt ist.
     */
    private val _soundEvents = MutableSharedFlow<SoundEvent>(extraBufferCapacity = 16)
    val soundEvents: SharedFlow<SoundEvent> = _soundEvents.asSharedFlow()

    private var loopJob: Job? = null

    /** Erstes Level samt Willkommens-Overlay. */
    fun startNewGame() {
        loopJob?.cancel()
        loopJob = null

        viewModelScope.launch {
            _isPreparing.value = true
            // Das Erzeugen eines eindeutigen Rätsels kostet spürbar Rechenzeit
            // und gehört deshalb nicht auf den Main-Thread.
            val fresh = withContext(Dispatchers.Default) { engine.newGame() }
            _isPreparing.value = false
            applyState(fresh)
        }
    }

    /** „Neuer Versuch" nach dem Spielende — ohne das Willkommens-Overlay. */
    fun restart() {
        loopJob?.cancel()
        loopJob = null

        viewModelScope.launch {
            _isPreparing.value = true
            val fresh = withContext(Dispatchers.Default) {
                engine.onInput(engine.newGame(), GameInput.Begin)
            }
            _isPreparing.value = false
            applyState(fresh)
            startLoop()
        }
    }

    fun onInput(input: GameInput) {
        when (input) {
            // Der Levelwechsel erzeugt ein neues Rätsel — siehe oben.
            GameInput.NextLevel -> viewModelScope.launch {
                _isPreparing.value = true
                val next = withContext(Dispatchers.Default) {
                    engine.onInput(_state.value, input)
                }
                _isPreparing.value = false
                applyState(next)
                if (next.status == GameStatus.Running) startLoop()
            }

            GameInput.Begin -> {
                applyState(engine.onInput(_state.value, input))
                if (_state.value.status == GameStatus.Running) startLoop()
            }

            else -> applyState(engine.onInput(_state.value, input))
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setSoundEnabled(enabled) }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setMusicEnabled(enabled) }
    }

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setVoiceEnabled(enabled) }
    }

    fun pause() {
        if (_state.value.status != GameStatus.Running) return
        loopJob?.cancel()
        loopJob = null
        applyState(_state.value.copy(status = GameStatus.Paused))
    }

    fun resume() {
        if (_state.value.status != GameStatus.Paused) return
        applyState(_state.value.copy(status = GameStatus.Running))
        startLoop()
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch { runLoop() }
    }

    /**
     * Übernimmt einen neuen Zustand und schreibt das Ergebnis genau einmal
     * fort — beim Übergang in [GameStatus.GameOver]. Zentral hier statt an den
     * einzelnen Auslösern (Zeit abgelaufen, Leben verbraucht), damit kein Weg
     * das Speichern überspringt oder doppelt auslöst.
     */
    private fun applyState(next: GameState) {
        val previous = _state.value
        _state.value = next

        SoundEvents.diff(previous, next).forEach(_soundEvents::tryEmit)

        if (previous.status != GameStatus.GameOver && next.status == GameStatus.GameOver) {
            viewModelScope.launch { preferences.recordFinishedGame(next.score) }
        }
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
                applyState(engine.tick(_state.value, TICK_MILLIS))
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
