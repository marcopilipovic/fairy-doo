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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
     * Der App-weite Lebenspool, live nachgeführt.
     *
     * Anders als [profile] tickt dieser Wert auch ohne neuen Schreibzugriff:
     * Ein Sekundentakt gleicht [GlobalLives] gegen die aktuelle Uhrzeit ab,
     * damit der Countdown bis zum nächsten Leben sichtbar herunterzählt.
     */
    val globalLives: StateFlow<GlobalLivesState> = combine(
        preferences.profile,
        tickerFlow(1_000L),
    ) { current, _ ->
        GlobalLives.normalize(current.globalLives, current.nextGlobalLifeAtMillis, System.currentTimeMillis())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GlobalLivesState(GlobalLives.MAX, 0L),
    )

    /**
     * Der Feenstaub-Vorrat, live nachgeführt — wie die Wald-Leben.
     *
     * Der Countdown im Knopf muss sichtbar herunterzählen, auch während man auf
     * das Brett schaut, ohne dass irgendetwas geschrieben wird.
     */
    val fairyDust: StateFlow<SupplyState> = combine(
        preferences.profile,
        tickerFlow(1_000L),
    ) { current, _ ->
        FairyDustSupply.normalize(
            current.fairyDust,
            current.nextFairyDustAtMillis,
            System.currentTimeMillis(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SupplyState(FairyDustSupply.max, 0L),
    )

    /** Steuert, ob die Levelkarte statt des Spiels gezeigt wird. Start: die Karte. */
    private val _showLevelSelect = MutableStateFlow(true)
    val showLevelSelect: StateFlow<Boolean> = _showLevelSelect.asStateFlow()

    private val _tutorialOpen = MutableStateFlow(false)
    val tutorialOpen: StateFlow<Boolean> = _tutorialOpen.asStateFlow()

    private val _tutorialStep = MutableStateFlow(0)
    val tutorialStep: StateFlow<Int> = _tutorialStep.asStateFlow()

    init {
        viewModelScope.launch {
            // Ein einmaliger, echter Blick auf den gespeicherten Stand — nicht
            // auf den Platzhalter, den [profile] vor dem ersten Laden liefert.
            // So blitzt die Anleitung bei wiederkehrenden Spieler:innen nicht
            // kurz auf, nur um sofort wieder zuzuklappen.
            if (!preferences.profile.first().hasSeenTutorial) {
                _tutorialOpen.value = true
            }
        }
    }

    /**
     * Öffnet die Anleitung von vorn — über den ❔-Knopf, jederzeit erreichbar.
     * Pausiert ein laufendes Spiel dabei, wie die Levelkarte es auch tut: Wer
     * die Regeln nachliest, soll dafür keine Zeit verlieren.
     */
    fun openTutorial() {
        pause()
        _tutorialStep.value = 0
        _tutorialOpen.value = true
    }

    /** „Weiter" — beim letzten Schritt schließt es die Anleitung stattdessen. */
    fun tutorialNext() {
        val step = _tutorialStep.value
        if (step < TUTORIAL_STEP_COUNT - 1) {
            _tutorialStep.value = step + 1
        } else {
            closeTutorial()
        }
    }

    fun skipTutorial() = closeTutorial()

    private fun closeTutorial() {
        _tutorialOpen.value = false
        resume()
        viewModelScope.launch { preferences.markTutorialSeen() }
    }

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

    /**
     * Startet ein bestimmtes Level frisch — von der Levelkarte, als neuer
     * Versuch nach einem verlorenen Level oder als direkte Wiederholung eines
     * bereits geschafften Levels. Ohne das Willkommens-Overlay: Wer aus der
     * Levelkarte heraus startet, kennt die Regeln schon.
     */
    fun startLevel(level: Int) {
        loopJob?.cancel()
        loopJob = null

        viewModelScope.launch {
            _isPreparing.value = true
            // Das Erzeugen eines eindeutigen Rätsels kostet spürbar Rechenzeit
            // und gehört deshalb nicht auf den Main-Thread.
            val started = withContext(Dispatchers.Default) {
                engine.onInput(engine.newGame(level), GameInput.Begin)
            }
            // Der Vorrat gehört dem Spieler, nicht dem Level: Was gespeichert
            // ist — abzüglich dessen, was inzwischen nachgewachsen ist —, geht
            // ins neue Level mit.
            val fresh = started.copy(fairyDust = fairyDust.value.amount)
            _isPreparing.value = false
            _showLevelSelect.value = false
            applyState(fresh)
            startLoop()
        }
    }

    /** Öffnet die Levelkarte — pausiert ein laufendes Spiel, falls nötig. */
    fun openLevelSelect() {
        pause()
        _showLevelSelect.value = true
    }

    /** Schließt die Levelkarte wieder — nimmt ein pausiertes Spiel wieder auf. */
    fun closeLevelSelect() {
        _showLevelSelect.value = false
        resume()
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

            // Der Feenstaub wird zusätzlich dauerhaft abgebucht — nur so
            // überlebt der Verbrauch den Levelwechsel und den App-Neustart.
            GameInput.UseFairyDust -> {
                val before = _state.value
                if (before.fairyDust > 0) {
                    applyState(engine.onInput(before, input))
                    viewModelScope.launch { preferences.consumeFairyDust() }
                }
            }

            else -> applyState(engine.onInput(_state.value, input))
        }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch { preferences.setMusicVolume(volume) }
    }

    fun setSoundVolume(volume: Float) {
        viewModelScope.launch { preferences.setSoundVolume(volume) }
    }

    fun setVoiceVolume(volume: Float) {
        viewModelScope.launch { preferences.setVoiceVolume(volume) }
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
            viewModelScope.launch {
                preferences.recordFinishedGame(next.score)
                // Das Level ist verloren — kostet eins der App-weiten Leben.
                preferences.consumeGlobalLife()
            }
        }

        if (previous.status != GameStatus.LevelComplete && next.status == GameStatus.LevelComplete) {
            // Das nächste Level bleibt freigeschaltet, auch wenn ein späterer Versuch misslingt.
            viewModelScope.launch { preferences.recordLevelCompleted(next.level) }
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

    /** Emittiert im festen Abstand — Taktgeber für Werte, die nicht auf Ereignisse warten können. */
    private fun tickerFlow(intervalMillis: Long): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(intervalMillis)
        }
    }

    companion object {
        private const val TICK_MILLIS = 16L
        private const val MAX_FRAME_MILLIS = 250L

        /** Willkommen, Berührungsregel, Antippen&Halten, Zauberhilfen, Leben. */
        const val TUTORIAL_STEP_COUNT = 5

        /** Für `viewModel(factory = GameViewModel.factory(repository))`. */
        fun factory(preferences: GamePreferencesRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { GameViewModel(preferences = preferences) }
            }
    }
}
