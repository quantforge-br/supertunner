package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPitchEngine
import com.example.audio.ReferenceTonePlayer
import com.example.data.TunerPreferencesRepository
import com.example.domain.InstrumentType
import com.example.domain.PitchMath
import com.example.domain.StringNote
import com.example.domain.TuningCatalog
import com.example.domain.TuningPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class PitchZone {
    IDLE,
    FLAT_FAR,
    FLAT_CLOSE,
    IN_TUNE,
    SHARP_CLOSE,
    SHARP_FAR
}

data class TunerUiState(
    val hasMicPermission: Boolean = false,
    val isListening: Boolean = false,
    val instrument: InstrumentType = InstrumentType.ACOUSTIC,
    val activePreset: TuningPreset = TuningCatalog.presets.first(),
    val isAutoMode: Boolean = true,
    val lockedString: StringNote? = null,
    val detectedNoteName: String? = null,
    val detectedAccidental: String = "",
    val detectedOctave: Int? = null,
    val detectedString: StringNote? = null,
    val detectedFrequencyHz: Double? = null,
    val targetFrequencyHz: Double? = null,
    val centsDeviation: Float = 0f,
    val pitchZone: PitchZone = PitchZone.IDLE,
    val audioLevel: Float = 0f,
    val waveformBars: List<Float> = List(28) { 0.08f },
    val playingStringNumber: Int? = null,
    val isTuningSheetOpen: Boolean = false
) {
    val isSignalActive: Boolean get() = detectedNoteName != null
}

class TunerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = TunerPreferencesRepository(application.applicationContext)
    private val pitchEngine = AudioPitchEngine()
    private val tonePlayer = ReferenceTonePlayer()

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private var captureJob: Job? = null
    private var silenceResetJob: Job? = null
    private var isForeground = false

    // Smoothing buffers
    private val recentFreqs = ArrayDeque<Double>(3)
    private var smoothedCents = 0f
    private var lastMatchTimestamp = 0L

    init {
        viewModelScope.launch {
            prefsRepo.preferencesFlow.collect { saved ->
                val preset = TuningCatalog.findById(saved.tuningId)
                _uiState.update { current ->
                    val updatedLocked = if (!current.isAutoMode && current.lockedString != null) {
                        preset.strings.firstOrNull { it.stringNumber == current.lockedString.stringNumber }
                    } else {
                        null
                    }
                    current.copy(
                        instrument = saved.instrument,
                        activePreset = preset,
                        lockedString = updatedLocked
                    )
                }
            }
        }
    }

    fun onPermissionUpdated(granted: Boolean) {
        _uiState.update { it.copy(hasMicPermission = granted) }
        if (granted && isForeground) {
            startListening()
        } else if (!granted) {
            stopListening()
        }
    }

    fun onForegroundChanged(foreground: Boolean) {
        isForeground = foreground
        if (foreground && _uiState.value.hasMicPermission) {
            startListening()
        } else {
            stopListening()
        }
    }

    fun selectInstrument(instrument: InstrumentType) {
        _uiState.update { it.copy(instrument = instrument) }
        viewModelScope.launch {
            prefsRepo.saveInstrument(instrument)
        }
    }

    fun openTuningSheet() {
        _uiState.update { it.copy(isTuningSheetOpen = true) }
    }

    fun dismissTuningSheet() {
        _uiState.update { it.copy(isTuningSheetOpen = false) }
    }

    fun selectTuning(preset: TuningPreset) {
        recentFreqs.clear()
        smoothedCents = 0f
        _uiState.update {
            it.copy(
                activePreset = preset,
                isTuningSheetOpen = false,
                isAutoMode = true,
                lockedString = null,
                detectedNoteName = null,
                detectedAccidental = "",
                detectedOctave = null,
                detectedString = null,
                detectedFrequencyHz = null,
                targetFrequencyHz = null,
                centsDeviation = 0f,
                pitchZone = PitchZone.IDLE
            )
        }
        viewModelScope.launch {
            prefsRepo.saveTuning(preset)
        }
    }

    fun toggleAutoMode() {
        val current = _uiState.value
        if (current.isAutoMode) {
            // Switch to manual targeting 6th string (or currently detected string)
            val target = current.detectedString ?: current.activePreset.strings.first()
            _uiState.update {
                it.copy(
                    isAutoMode = false,
                    lockedString = target,
                    detectedNoteName = target.noteName,
                    detectedAccidental = target.accidental,
                    detectedOctave = target.octave,
                    detectedString = target,
                    targetFrequencyHz = target.frequencyHz
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isAutoMode = true,
                    lockedString = null
                )
            }
        }
    }

    fun onStringTapped(stringNote: StringNote) {
        val current = _uiState.value
        // Lock onto tapped string (if not chromatic mode) and play reference sound
        val lock = !current.activePreset.isChromatic
        _uiState.update {
            it.copy(
                isAutoMode = !lock,
                lockedString = if (lock) stringNote else null,
                detectedNoteName = stringNote.noteName,
                detectedAccidental = stringNote.accidental,
                detectedOctave = stringNote.octave,
                detectedString = stringNote,
                targetFrequencyHz = stringNote.frequencyHz,
                playingStringNumber = stringNote.stringNumber,
                centsDeviation = 0f,
                pitchZone = PitchZone.IDLE
            )
        }
        recentFreqs.clear()
        smoothedCents = 0f

        tonePlayer.playTone(
            frequencyHz = stringNote.frequencyHz,
            instrument = current.instrument,
            onFinished = {
                _uiState.update { state ->
                    if (state.playingStringNumber == stringNote.stringNumber) {
                        state.copy(playingStringNumber = null)
                    } else {
                        state
                    }
                }
            }
        )
    }

    private fun startListening() {
        if (captureJob?.isActive == true) return
        _uiState.update { it.copy(isListening = true) }

        captureJob = viewModelScope.launch {
            pitchEngine.observeAudioFrames().collect { frame ->
                val state = _uiState.value
                // Pause pitch matching while reference tone is playing so speaker doesn't feed mic
                if (state.playingStringNumber != null) {
                    _uiState.update {
                        it.copy(
                            audioLevel = frame.rmsLevel * 0.4f,
                            waveformBars = frame.waveformBars
                        )
                    }
                    return@collect
                }

                val pitch = frame.pitch
                if (pitch != null) {
                    silenceResetJob?.cancel()
                    lastMatchTimestamp = System.currentTimeMillis()

                    if (recentFreqs.size >= 3) recentFreqs.removeFirst()
                    recentFreqs.addLast(pitch.frequencyHz)
                    val medianFreq = medianOf(recentFreqs)

                    val match = PitchMath.matchToPreset(
                        frequencyHz = medianFreq,
                        preset = state.activePreset,
                        lockedString = if (!state.isAutoMode) state.lockedString else null
                    )

                    // Exponential moving average for smooth needle
                    smoothedCents = if (state.detectedNoteName == match.noteName &&
                        state.detectedOctave == match.octave
                    ) {
                        smoothedCents * 0.62f + match.centsDeviation * 0.38f
                    } else {
                        match.centsDeviation
                    }

                    val zone = classifyZone(smoothedCents, state.pitchZone)
                    _uiState.update {
                        it.copy(
                            detectedNoteName = match.noteName,
                            detectedAccidental = match.accidental,
                            detectedOctave = match.octave,
                            detectedString = match.matchedString,
                            detectedFrequencyHz = medianFreq,
                            targetFrequencyHz = match.targetFrequencyHz,
                            centsDeviation = smoothedCents,
                            pitchZone = zone,
                            audioLevel = frame.rmsLevel,
                            waveformBars = frame.waveformBars
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            audioLevel = frame.rmsLevel,
                            waveformBars = frame.waveformBars
                        )
                    }
                    scheduleIdleDecayIfNeeded()
                }
            }
        }
    }

    private fun scheduleIdleDecayIfNeeded() {
        if (silenceResetJob?.isActive == true) return
        silenceResetJob = viewModelScope.launch {
            delay(1400)
            val elapsed = System.currentTimeMillis() - lastMatchTimestamp
            if (elapsed >= 1350) {
                recentFreqs.clear()
                smoothedCents = 0f
                _uiState.update { current ->
                    val locked = current.lockedString
                    if (!current.isAutoMode && locked != null) {
                        current.copy(
                            detectedNoteName = locked.noteName,
                            detectedAccidental = locked.accidental,
                            detectedOctave = locked.octave,
                            detectedString = locked,
                            detectedFrequencyHz = null,
                            targetFrequencyHz = locked.frequencyHz,
                            centsDeviation = 0f,
                            pitchZone = PitchZone.IDLE
                        )
                    } else {
                        current.copy(
                            detectedNoteName = null,
                            detectedAccidental = "",
                            detectedOctave = null,
                            detectedString = null,
                            detectedFrequencyHz = null,
                            targetFrequencyHz = null,
                            centsDeviation = 0f,
                            pitchZone = PitchZone.IDLE
                        )
                    }
                }
            }
        }
    }

    private fun stopListening() {
        captureJob?.cancel()
        captureJob = null
        silenceResetJob?.cancel()
        _uiState.update { it.copy(isListening = false, audioLevel = 0f) }
    }

    private fun classifyZone(cents: Float, previousZone: PitchZone): PitchZone {
        // Hysteresis around in-tune threshold so needle doesn't flicker at ±3.5 cents
        val inTuneThreshold = if (previousZone == PitchZone.IN_TUNE) 4.2f else 3.2f
        val absCents = abs(cents)
        return when {
            absCents <= inTuneThreshold -> PitchZone.IN_TUNE
            cents < -15f -> PitchZone.FLAT_FAR
            cents < 0f -> PitchZone.FLAT_CLOSE
            cents > 15f -> PitchZone.SHARP_FAR
            else -> PitchZone.SHARP_CLOSE
        }
    }

    private fun medianOf(values: ArrayDeque<Double>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.sorted()
        return sorted[sorted.size / 2]
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        tonePlayer.stop()
    }
}
