package ug.humb.fairydoku.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ug.humb.fairydoku.audio.SoundEvent
import ug.humb.fairydoku.audio.SoundEvents
import ug.humb.fairydoku.data.GamePreferencesRepository
import ug.humb.fairydoku.data.PlayerProfile
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.TimeZone

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

    /** Der Irrlicht-Vorrat, live nachgeführt — wie der Feenstaub. */
    val irrlicht: StateFlow<SupplyState> = combine(
        preferences.profile,
        tickerFlow(1_000L),
    ) { current, _ ->
        IrrlichtSupply.normalize(
            current.irrlicht,
            current.nextIrrlichtAtMillis,
            System.currentTimeMillis(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SupplyState(IrrlichtSupply.max, 0L),
    )

    /** Der Feenkreis-Vorrat, live nachgeführt — wie die beiden anderen. */
    val feenkreis: StateFlow<SupplyState> = combine(
        preferences.profile,
        tickerFlow(1_000L),
    ) { current, _ ->
        FeenkreisSupply.normalize(
            current.feenkreis,
            current.nextFeenkreisAtMillis,
            System.currentTimeMillis(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SupplyState(FeenkreisSupply.max, 0L),
    )

    /**
     * Die Tageswertung, live nachgeführt.
     *
     * Der Punktestand ist bereits gegen die Uhr abgeglichen: Wechselt der Tag,
     * während die App offen ist, springt die Anzeige sofort auf null, ohne auf
     * das Speichern zu warten. Der Sekundentakt lässt außerdem den Countdown
     * bis zum nächsten Tag sichtbar herunterlaufen.
     */
    val dailyScore: StateFlow<DailyScoreState> = combine(
        preferences.profile,
        tickerFlow(1_000L),
    ) { current, _ ->
        val now = System.currentTimeMillis()
        val offset = zoneOffsetAt(now)
        val settled = DailyScoring.settle(current.dailyScore, now, offset).score
        DailyScoreState(
            points = settled.points,
            bestPoints = settled.bestPoints,
            remainingSeconds = DailyCycle.remainingSeconds(now, offset),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyScoreState(),
    )

    /** Ein abgerechneter Tag, dessen Abschluss-Overlay noch aussteht. */
    val pendingSettlement: StateFlow<DailySettlement?> = profile
        .map { it.pendingSettlement }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    /** Das Abschluss-Overlay wurde weggetippt. */
    fun acknowledgeDailySettlement() {
        viewModelScope.launch { preferences.acknowledgeDailySettlement() }
    }

    /**
     * Werbung wird erst angeboten, nachdem die ersten Level geschafft sind —
     * wer gerade erst anfängt, soll nicht mit Werbeangeboten begrüßt werden.
     * Bis dahin tritt an ihre Stelle ein Geschenk.
     *
     * Ab welchem Level, steht in [ADS_UNLOCK_AFTER_LEVEL]. Hier stand bis zum
     * 28. August „nach den ersten zehn" — ein Rest aus der Zeit vor der
     * Zusammenführung, als die Zahl noch 10 war. Die Zahl gehört an genau eine
     * Stelle, und das ist die Konstante.
     */
    val adsUnlocked: StateFlow<Boolean> = profile
        .map { it.highestLevelUnlocked > ADS_UNLOCK_AFTER_LEVEL }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    /**
     * Werbung angesehen — je ein Feenstaub/Irrlicht/Leben extra.
     *
     * Feenstaub und Irrlicht wirken sofort im laufenden Level mit, nicht erst
     * beim nächsten Levelstart: Wer mitten im Rätsel ohne Vorrat dasteht, will
     * mit der Belohnung sofort weiterspielen, nicht erst nach einem Neustart.
     * Das Wald-Leben betrifft dagegen nie das laufende Level, sondern nur den
     * app-weiten Vorrat — dafür reicht das Schreiben in die Vorlieben, der
     * eigene [globalLives]-Zustand liest ihn ohnehin live nach.
     */
    fun grantFairyDust() {
        viewModelScope.launch {
            preferences.grantFairyDust()
            applyState(_state.value.copy(fairyDust = (_state.value.fairyDust + 1).coerceAtMost(FairyDustSupply.max)))
        }
    }

    fun grantIrrlicht() {
        viewModelScope.launch {
            preferences.grantIrrlicht()
            applyState(_state.value.copy(irrlicht = (_state.value.irrlicht + 1).coerceAtMost(IrrlichtSupply.max)))
        }
    }

    fun grantFeenkreis() {
        viewModelScope.launch {
            preferences.grantFeenkreis()
            applyState(
                _state.value.copy(
                    feenkreis = (_state.value.feenkreis + 1).coerceAtMost(FeenkreisSupply.max),
                ),
            )
        }
    }

    fun grantGlobalLife() {
        viewModelScope.launch { preferences.grantGlobalLife() }
    }

    /** Steuert, ob die Levelkarte statt des Spiels gezeigt wird. Start: die Karte. */
    private val _showLevelSelect = MutableStateFlow(true)
    val showLevelSelect: StateFlow<Boolean> = _showLevelSelect.asStateFlow()

    private val _tutorialOpen = MutableStateFlow(false)
    val tutorialOpen: StateFlow<Boolean> = _tutorialOpen.asStateFlow()

    private val _tutorialStep = MutableStateFlow(0)
    val tutorialStep: StateFlow<Int> = _tutorialStep.asStateFlow()

    /** Wie viele Bildschirme die gerade laufende Anleitung hat. */
    private val _tutorialTotal = MutableStateFlow(1)
    val tutorialTotal: StateFlow<Int> = _tutorialTotal.asStateFlow()

    /** Der wievielte davon gerade zu sehen ist — für „Weiter" gegen „Fertig". */
    private val _tutorialPosition = MutableStateFlow(0)
    val tutorialPosition: StateFlow<Int> = _tutorialPosition.asStateFlow()

    /**
     * Ob das die Begrüßung vor dem allerersten Zug ist.
     *
     * Nur dort passt „Den Wald betreten" auf den Knopf. Ein Hinweis, der
     * mitten im Spiel auftaucht, endet mit „Weiter spielen" — man betritt den
     * Wald ja nicht zum zweiten Mal.
     */
    private val _tutorialIstErstlauf = MutableStateFlow(false)
    val tutorialIstErstlauf: StateFlow<Boolean> = _tutorialIstErstlauf.asStateFlow()

    /**
     * Die Anleitung ist eine Warteschlange, keine feste Folge.
     *
     * Beim ersten Start standen fünf Bildschirme zwischen dem Spieler und dem
     * Spiel. Mirco Lehnhoff am 1. September 2026: „Kinder lesen nicht! Die
     * Gefahr ist groß, dass das Spiel wieder geschlossen wird, noch bevor es
     * losgeht." Er hat recht — ein Spiel, das vor dem ersten Zug weggewischt
     * wird, hat verloren, und keine der fünf Seiten war die Ursache; ihre
     * Anzahl war es.
     *
     * Vor dem ersten Zug bleibt deshalb nur, was man wirklich vorher wissen
     * muss: das Ziel und wie man setzt. Alles Übrige taucht in dem Augenblick
     * auf, in dem es zum ersten Mal etwas bedeutet — die Leben, wenn eines
     * verloren geht, die Helferlein, wenn das zweite Level beginnt.
     *
     * Über den ❔-Knopf gibt es weiterhin alles am Stück.
     */
    private var warteschlange: List<Int> = ALLE_SCHRITTE
    private var position: Int = 0
    private var nachDemSchliessen: (suspend () -> Unit)? = null

    init {
        viewModelScope.launch {
            // Ein einmaliger, echter Blick auf den gespeicherten Stand — nicht
            // auf den Platzhalter, den [profile] vor dem ersten Laden liefert.
            // So blitzt die Anleitung bei wiederkehrenden Spieler:innen nicht
            // kurz auf, nur um sofort wieder zuzuklappen.
            if (!preferences.profile.first().hasSeenTutorial) {
                zeige(ERSTLAUF_SCHRITTE, erstlauf = true) { preferences.markTutorialSeen() }
            }
        }

        // Der gestrige Tag wird beim Start abgerechnet — das ist der übliche
        // Fall, weil zwischen zwei Sitzungen eine Nacht liegt.
        viewModelScope.launch { preferences.settleDailyCycle() }

        // Und noch einmal regelmäßig, für den seltenen Fall, dass die App über
        // den Stichtag hinweg offen bleibt. Ein halbminütiger Takt genügt:
        // Der Wechsel liegt um vier Uhr früh, da wartet niemand auf die Sekunde.
        viewModelScope.launch {
            tickerFlow(CYCLE_CHECK_MILLIS).collect {
                val now = System.currentTimeMillis()
                val stored = preferences.profile.first().dailyScore
                if (DailyCycle.cycleIdAt(now, zoneOffsetAt(now)) != stored.cycleId) {
                    preferences.settleDailyCycle()
                }
            }
        }
    }

    /**
     * Öffnet die Anleitung von vorn — über den ❔-Knopf, jederzeit erreichbar.
     * Pausiert ein laufendes Spiel dabei, wie die Levelkarte es auch tut: Wer
     * die Regeln nachliest, soll dafür keine Zeit verlieren.
     */
    fun openTutorial() = zeige(ALLE_SCHRITTE) { preferences.markTutorialSeen() }

    /** Zeigt eine Auswahl von Anleitungsschritten und merkt sich das Ergebnis. */
    private fun zeige(
        schritte: List<Int>,
        erstlauf: Boolean = false,
        danach: (suspend () -> Unit)?,
    ) {
        if (schritte.isEmpty()) return
        pause()
        warteschlange = schritte
        position = 0
        nachDemSchliessen = danach
        _tutorialStep.value = schritte.first()
        _tutorialTotal.value = schritte.size
        _tutorialPosition.value = 0
        _tutorialIstErstlauf.value = erstlauf
        _tutorialOpen.value = true
    }

    /** „Weiter" — beim letzten Schritt schließt es die Anleitung stattdessen. */
    fun tutorialNext() {
        position += 1
        if (position < warteschlange.size) {
            _tutorialStep.value = warteschlange[position]
            _tutorialPosition.value = position
        } else {
            closeTutorial()
        }
    }

    fun skipTutorial() = closeTutorial()

    private fun closeTutorial() {
        _tutorialOpen.value = false
        resume()
        val danach = nachDemSchliessen
        nachDemSchliessen = null
        if (danach != null) viewModelScope.launch { danach() }
    }

    /**
     * Die Erklärung zu den Leben — einmalig, beim ersten verlorenen Versuch.
     *
     * Genau dann bedeutet sie etwas: Es ist gerade etwas passiert, das man
     * verstehen will. Vorher wäre sie eine Regel unter fünf anderen gewesen.
     */
    private fun zeigeLebenHinweis() {
        viewModelScope.launch {
            if (preferences.profile.first().hasSeenLivesHint) return@launch
            zeige(listOf(SCHRITT_LEBEN)) { preferences.markLivesHintSeen() }
        }
    }

    /**
     * Die Erklärung zu den Helferlein — einmalig, zu Beginn des zweiten Levels.
     *
     * Nicht beim ersten: Da ist gerade genug zu begreifen. Wer ein Level
     * geschafft hat, kennt das Spiel und nimmt den Hinweis als Angebot statt
     * als weitere Hürde.
     */
    private fun zeigeHelferleinHinweis(level: Int) {
        if (level != 2) return
        viewModelScope.launch {
            if (preferences.profile.first().hasSeenPowerUpHint) return@launch
            zeige(listOf(SCHRITT_HELFERLEIN)) { preferences.markPowerUpHintSeen() }
        }
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
            val fresh = started.copy(
                fairyDust = fairyDust.value.amount,
                irrlicht = irrlicht.value.amount,
                feenkreis = feenkreis.value.amount,
            )
            _isPreparing.value = false
            _showLevelSelect.value = false
            applyState(fresh)
            startLoop()
            zeigeHelferleinHinweis(fresh.level)
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
                zeigeHelferleinHinweis(next.level)
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

            // Wie der Feenstaub: zusätzlich dauerhaft abgebucht.
            GameInput.UseFeenkreis -> {
                val before = _state.value
                if (before.feenkreis > 0 && before.feenkreisMillis <= 0L) {
                    applyState(engine.onInput(before, input))
                    viewModelScope.launch { preferences.consumeFeenkreis() }
                }
            }

            GameInput.UseIrrlicht -> {
                val before = _state.value
                if (before.irrlicht > 0) {
                    applyState(engine.onInput(before, input))
                    viewModelScope.launch { preferences.consumeIrrlicht() }
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

    fun setPlayerName(name: String) {
        viewModelScope.launch { preferences.setPlayerName(name) }
    }

    fun setSelectedAvatar(species: FairySpecies) {
        viewModelScope.launch { preferences.setSelectedAvatar(species) }
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

    /**
     * Startet die Schleife — aber nur, wenn es etwas zu takten gibt.
     *
     * Sie lief früher durchgehend mit 60 Hz, solange ein Level offen war: Jede
     * der sechzig Runden je Sekunde schrieb einen neuen Zustand in den
     * StateFlow und stieß damit eine Neuzeichnung des ganzen Spielfelds an —
     * für ein Rätsel, das sich von selbst überhaupt nicht bewegt. Ein Tester
     * meldete am 1. September 2026 heißes Gerät, leeren Akku und Eingaben, die
     * hinterherhinken; das war die Ursache.
     *
     * Getaktet werden muss einzig das Nachleuchten eines Hinweises. Läuft
     * keines, endet die Schleife und das Gerät hat Ruhe.
     */
    private fun startLoop() {
        if (loopJob?.isActive == true) return
        if (!brauchtTakt()) return
        loopJob = viewModelScope.launch { runLoop() }
    }

    /** Ob überhaupt etwas läuft, das Zeit vergehen sehen muss. */
    private fun brauchtTakt(): Boolean {
        val jetzt = _state.value
        return jetzt.status == GameStatus.Running &&
            (jetzt.hintPulseMillis > 0L || jetzt.feenkreisMillis > 0L)
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

        // Ein frisch gesetzter Hinweis ist das Einzige, was von selbst abläuft —
        // dafür und nur dafür springt die Schleife an, siehe [startLoop].
        if (next.hintPulseMillis > previous.hintPulseMillis ||
            next.feenkreisMillis > previous.feenkreisMillis
        ) {
            startLoop()
        }

        // Ein Versuch ist verbraucht — der Augenblick, in dem die Leben zum
        // ersten Mal etwas bedeuten. Siehe [zeigeLebenHinweis].
        //
        // Nur solange das Level weiterläuft: War es der letzte Versuch, kommt
        // ohnehin gleich der Verloren-Dialog, und zwei Fenster übereinander
        // erklären nichts, sie stapeln sich nur.
        if (next.lives < previous.lives && next.status == GameStatus.Running) {
            zeigeLebenHinweis()
        }

        if (previous.status != GameStatus.GameOver && next.status == GameStatus.GameOver) {
            viewModelScope.launch {
                preferences.recordFinishedGame(next.score)
                // Das Level ist verloren — kostet eins der App-weiten Leben.
                preferences.consumeGlobalLife()
            }
        }

        if (previous.status != GameStatus.LevelComplete && next.status == GameStatus.LevelComplete) {
            viewModelScope.launch {
                // Das nächste Level bleibt freigeschaltet, auch wenn ein späterer Versuch misslingt.
                preferences.recordLevelCompleted(next.level)
                // Die Punkte dieses Levels zählen sofort für den heutigen Tag —
                // nicht erst am Ende des Laufs, siehe `addDailyPoints`.
                preferences.addDailyPoints(next.gained)
            }
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

        while (viewModelScope.isActive && brauchtTakt()) {
            delay(TICK_MILLIS)

            val now = System.nanoTime() / 1_000_000
            // Deckelung, damit ein langer Hintergrundaufenthalt nicht Hunderte
            // Ticks auf einmal nachholt.
            accumulator += (now - lastMillis).coerceIn(0L, MAX_FRAME_MILLIS)
            lastMillis = now

            while (accumulator >= TICK_MILLIS && brauchtTakt()) {
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

    /** Der Versatz der Ortszeit gegenüber UTC — inklusive Sommerzeit. */
    private fun zoneOffsetAt(millis: Long): Long =
        TimeZone.getDefault().getOffset(millis).toLong()

    companion object {
        /** Die Bildschirme der Anleitung, in der Reihenfolge von TutorialOverlay. */
        private const val SCHRITT_REGELN = 0
        private const val SCHRITT_ANFASSEN = 1
        private const val SCHRITT_GESTEN = 2
        const val SCHRITT_HELFERLEIN = 3
        const val SCHRITT_LEBEN = 4

        /** Alles am Stück — über den ❔-Knopf. */
        val ALLE_SCHRITTE = listOf(
            SCHRITT_REGELN, SCHRITT_ANFASSEN, SCHRITT_GESTEN,
            SCHRITT_HELFERLEIN, SCHRITT_LEBEN,
        )

        /**
         * Was man vor dem allerersten Zug wissen muss — und keine Seite mehr.
         * Das Ziel, und wie man eine Fee setzt. Der Rest kommt, wenn er dran ist.
         */
        val ERSTLAUF_SCHRITTE = listOf(SCHRITT_REGELN, SCHRITT_GESTEN)

        private const val TICK_MILLIS = 16L
        private const val MAX_FRAME_MILLIS = 250L

        /** Wie oft geprüft wird, ob der Tag gewechselt hat — siehe `init`. */
        private const val CYCLE_CHECK_MILLIS = 30_000L

        /** Willkommen, Berührungsregel, Antippen&Halten, Zauberhilfen, Leben. */

        /**
         * Ab wann die Werbe-Knöpfe erscheinen — siehe [adsUnlocked].
         *
         * Drei Level, nicht zehn wie in den anderen Spielen des Hauses: Ein
         * Fairydoku-Level dauert Minuten, kein paar Sekunden. Bis Level zehn
         * wäre der Spieler eine halbe Stunde unterwegs, und so lange gäbe es
         * keine Möglichkeit, einen leeren Vorrat aufzufüllen — die Hilfe käme
         * an, wenn man sie längst nicht mehr braucht.
         *
         * An dieser Zahl hängt auch, wann das letzte Geschenk verteilt wird
         * (siehe `giftIsLast` im GameScreen). Deshalb steht sie hier einmal
         * und wird von dort geholt, statt zweimal dazustehen.
         */
        const val ADS_UNLOCK_AFTER_LEVEL = 3

        /** Für `viewModel(factory = GameViewModel.factory(repository))`. */
        fun factory(preferences: GamePreferencesRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { GameViewModel(preferences = preferences) }
            }
    }
}
