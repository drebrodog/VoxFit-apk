package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.repository.VoxFitRepository
import com.example.model.AppSettings
import com.example.model.Exercise
import com.example.model.ExerciseCategory
import com.example.model.NavItem
import com.example.model.VoiceState
import com.example.model.VoiceStatus
import com.example.model.WorkoutExercise
import com.example.model.WorkoutSession
import com.example.model.WorkoutSet
import com.example.speech.ParsedVoiceCommand
import com.example.speech.VoiceCommandParser
import com.example.speech.VoiceRecognitionEvent
import com.example.speech.VoiceRecognitionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class VoxFitUiState(
    val currentTab: NavItem = NavItem.WORKOUT,
    val isWorkoutActive: Boolean = true,
    val activeSessionId: String = "session_active_1",
    val workoutTimerSeconds: Int = 42 * 60 + 18,
    val isTimerRunning: Boolean = true,
    val selectedExercise: Exercise = DefaultFallbackData.benchPress,
    val activeSets: List<WorkoutSet> = emptyList(),
    val voiceState: VoiceState = VoiceState(),
    val exercisesCatalog: List<Exercise> = emptyList(),
    val selectedCategory: ExerciseCategory = ExerciseCategory.ALL,
    val searchQuery: String = "",
    val workoutHistory: List<WorkoutSession> = emptyList(),
    val selectedHistorySession: WorkoutSession? = null,
    val setToEdit: WorkoutSet? = null,
    val settings: AppSettings = AppSettings(),
    val showFinishDialog: Boolean = false,
    val showVoiceHelpDialog: Boolean = false,
    val showAddExerciseDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val needRecordAudioPermission: Boolean = false,
    val snackbarMessage: String? = null
) {
    val formattedTimer: String
        get() {
            val hours = workoutTimerSeconds / 3600
            val minutes = (workoutTimerSeconds % 3600) / 60
            val seconds = workoutTimerSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

    val lastSet: WorkoutSet?
        get() = activeSets.lastOrNull()

    val totalWorkoutVolumeKg: Float
        get() = activeSets.filter { it.isCompleted }.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()

    val filteredExercises: List<Exercise>
        get() = exercisesCatalog.filter { exercise ->
            val matchesCategory = selectedCategory == ExerciseCategory.ALL || exercise.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() || 
                exercise.name.contains(searchQuery, ignoreCase = true) ||
                exercise.targetMuscles.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
}

object DefaultFallbackData {
    val benchPress = Exercise(
        id = "ex_1",
        name = "Жим лёжа",
        normalizedName = "жим лежа",
        category = ExerciseCategory.CHEST,
        targetMuscles = "Грудные, трицепс, передняя дельта",
        equipment = "Штанга",
        personalRecord = "110 кг × 5",
        defaultWeightKg = 80f,
        defaultReps = 8,
        description = "Базовое упражнение со штангой на горизонтальной скамье."
    )
}

class VoxFitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VoxFitRepository = VoxFitRepository(
        AppDatabase.getDatabase(application)
    )

    private val _uiState = MutableStateFlow(VoxFitUiState())
    val uiState: StateFlow<VoxFitUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var simulatedWaveJob: Job? = null
    private var activeSessionSetsJob: Job? = null

    val sampleVoicePhrases = listOf(
        VoiceSample("начинаю жим лёжа", "Жим лёжа", 80f, 8, false),
        VoiceSample("жим лёжа 80 на 8", "Жим лёжа", 80f, 8, false),
        VoiceSample("80 на 8", "Жим лёжа", 80f, 8, false),
        VoiceSample("жим лёжа 80 с половиной на 8", "Жим лёжа", 80.5f, 8, false),
        VoiceSample("разминка 40 на 12", "Жим лёжа", 40f, 12, true),
        VoiceSample("80 на 8 до отказа", "Жим лёжа", 80f, 8, false),
        VoiceSample("повтори предыдущий подход", "Жим лёжа", 80f, 8, false),
        VoiceSample("исправь вес на 82.5", "Жим лёжа", 82.5f, 8, false),
        VoiceSample("новое упражнение выпады с гантелями", "Выпады с гантелями", 16f, 10, false),
        VoiceSample("удали последний подход", "Жим лёжа", 80f, 8, false),
        VoiceSample("закончил упражнение", "Жим лёжа", 80f, 8, false),
        VoiceSample("закончить тренировку", "Жим лёжа", 80f, 8, false)
    )
    private var sampleIndex = 0
    private var isRealRecognizerActive = false

    private val voiceManager = VoiceRecognitionManager(
        context = application.applicationContext,
        onEvent = { event -> handleVoiceEvent(event) }
    )

    init {
        startTimer()
        initDatabase()
    }

    private fun initDatabase() {
        viewModelScope.launch {
            repository.ensureDefaultExercises()

            // Observe Exercises from Room
            launch {
                repository.allExercises.collect { entities ->
                    val exercises = entities.map { it.toDomain() }
                    _uiState.update { state ->
                        val currentSelected = exercises.find { it.id == state.selectedExercise.id } 
                            ?: exercises.firstOrNull() 
                            ?: DefaultFallbackData.benchPress
                        state.copy(
                            exercisesCatalog = exercises,
                            selectedExercise = currentSelected
                        )
                    }
                }
            }

            // Initialize or find active session
            ensureActiveSession()

            // Observe Workout History sessions from Room
            launch {
                combine(
                    repository.allSessions,
                    repository.allSets,
                    repository.allExercises
                ) { sessions, allSets, exerciseEntities ->
                    val exercisesMap = exerciseEntities.associate { it.id to it.toDomain() }
                    sessions.filter { it.status == "COMPLETED" }.map { sessionEntity ->
                        val sessionSets = allSets.filter { it.sessionId == sessionEntity.id }
                        val groupedByExercise = sessionSets.groupBy { it.exerciseId }.map { (exId, sets) ->
                            val ex = exercisesMap[exId] ?: Exercise(
                                id = exId,
                                name = "Упражнение",
                                category = ExerciseCategory.ALL,
                                targetMuscles = "Все мышцы"
                            )
                            WorkoutExercise(
                                exercise = ex,
                                sets = sets.map { it.toDomain() }
                            )
                        }

                        val durationSeconds = if (sessionEntity.finishedAt != null && sessionEntity.finishedAt > sessionEntity.startedAt) {
                            ((sessionEntity.finishedAt - sessionEntity.startedAt) / 1000).toInt()
                        } else {
                            52 * 60
                        }

                        val durationMins = durationSeconds / 60
                        val durationFormatted = if (durationMins >= 60) {
                            "${durationMins / 60} ч ${durationMins % 60} мин"
                        } else {
                            "$durationMins мин"
                        }

                        val dateFormatted = SimpleDateFormat("d MMMM", Locale("ru")).format(Date(sessionEntity.startedAt))
                        val totalVolume = sessionSets.filter { it.isCompleted }.sumOf { (it.weight * it.reps).toDouble() }.toFloat()

                        WorkoutSession(
                            id = sessionEntity.id,
                            title = groupedByExercise.firstOrNull()?.exercise?.name?.let { "$it и др." } ?: "Силовая тренировка",
                            dateFormatted = dateFormatted,
                            durationFormatted = durationFormatted,
                            durationSeconds = durationSeconds,
                            exercises = groupedByExercise,
                            totalVolumeKg = totalVolume,
                            status = sessionEntity.status
                        )
                    }
                }.collect { historyList ->
                    _uiState.update { it.copy(workoutHistory = historyList) }
                }
            }
        }
    }

    private suspend fun ensureActiveSession() {
        val currentSessionId = _uiState.value.activeSessionId
        val existingSets = repository.getSetsForSessionSync(currentSessionId)
        if (existingSets.isEmpty()) {
            val session = repository.createNewSession(currentSessionId)
            repository.saveSet(
                SetLogEntity(
                    id = "set_1",
                    sessionId = session.id,
                    exerciseId = "ex_1",
                    setNumber = 1,
                    weight = 40f,
                    reps = 12,
                    inputSource = "VOICE",
                    rawVoiceText = "разминка сорок на двенадцать",
                    isWarmup = true,
                    isCompleted = true
                )
            )
            repository.saveSet(
                SetLogEntity(
                    id = "set_2",
                    sessionId = session.id,
                    exerciseId = "ex_1",
                    setNumber = 2,
                    weight = 70f,
                    reps = 10,
                    inputSource = "VOICE",
                    rawVoiceText = "семьдесят на десять",
                    isWarmup = false,
                    isCompleted = true
                )
            )
            repository.saveSet(
                SetLogEntity(
                    id = "set_3",
                    sessionId = session.id,
                    exerciseId = "ex_1",
                    setNumber = 3,
                    weight = 80f,
                    reps = 8,
                    inputSource = "VOICE",
                    rawVoiceText = "восемьдесят на восемь",
                    isWarmup = false,
                    isCompleted = true
                )
            )
        }

        observeSetsForSession(currentSessionId)
    }

    private fun observeSetsForSession(sessionId: String) {
        activeSessionSetsJob?.cancel()
        activeSessionSetsJob = viewModelScope.launch {
            repository.getSetsForSession(sessionId).collect { setEntities ->
                val sets = setEntities.map { it.toDomain() }
                _uiState.update { it.copy(activeSets = sets) }
            }
        }
    }

    fun selectTab(tab: NavItem) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_uiState.value.isTimerRunning) {
                    _uiState.update { it.copy(workoutTimerSeconds = it.workoutTimerSeconds + 1) }
                }
            }
        }
    }

    fun toggleTimer() {
        _uiState.update { it.copy(isTimerRunning = !it.isTimerRunning) }
    }

    // --- PUSH TO TALK SPEECH RECOGNIZER ---

    fun onMicPressDown() {
        if (!voiceManager.isPermissionGranted()) {
            _uiState.update {
                it.copy(
                    needRecordAudioPermission = true,
                    voiceState = it.voiceState.copy(
                        status = VoiceStatus.READY,
                        isListening = false,
                        statusMessage = "Нет доступа к микрофону"
                    ),
                    snackbarMessage = "Требуется доступ к микрофону для распознавания команд"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                voiceState = it.voiceState.copy(
                    status = VoiceStatus.LISTENING,
                    isListening = true,
                    isProcessing = false,
                    rawSpeechText = "",
                    statusMessage = "Слушаю…"
                )
            )
        }

        isRealRecognizerActive = voiceManager.startListening()
        if (!isRealRecognizerActive) {
            startSimulatedWave()
        }
    }

    fun onMicPressUp() {
        simulatedWaveJob?.cancel()
        _uiState.update {
            it.copy(
                voiceState = it.voiceState.copy(
                    status = VoiceStatus.PROCESSING,
                    isListening = false,
                    isProcessing = true,
                    audioWaveIntensity = 0f,
                    statusMessage = "Распознаю команду…"
                )
            )
        }

        if (isRealRecognizerActive) {
            voiceManager.stopListening()
        } else {
            viewModelScope.launch {
                delay(600)
                val sample = sampleVoicePhrases[sampleIndex % sampleVoicePhrases.size]
                sampleIndex++
                executeVoiceTranscript(sample.rawPhrase)
            }
        }
    }

    private fun handleVoiceEvent(event: VoiceRecognitionEvent) {
        when (event) {
            is VoiceRecognitionEvent.Ready -> {
                _uiState.update {
                    it.copy(
                        voiceState = it.voiceState.copy(
                            status = VoiceStatus.LISTENING,
                            isListening = true,
                            statusMessage = event.message
                        )
                    )
                }
            }
            is VoiceRecognitionEvent.WaveIntensity -> {
                _uiState.update {
                    it.copy(
                        voiceState = it.voiceState.copy(
                            audioWaveIntensity = event.intensity
                        )
                    )
                }
            }
            is VoiceRecognitionEvent.PartialResult -> {
                _uiState.update {
                    it.copy(
                        voiceState = it.voiceState.copy(
                            recognizedText = event.text,
                            rawSpeechText = event.text,
                            statusMessage = "Слушаю: ${event.text}"
                        )
                    )
                }
            }
            is VoiceRecognitionEvent.FinalResult -> {
                executeVoiceTranscript(event.text)
            }
            is VoiceRecognitionEvent.Error -> {
                _uiState.update {
                    it.copy(
                        voiceState = it.voiceState.copy(
                            status = VoiceStatus.READY,
                            isListening = false,
                            isProcessing = false,
                            audioWaveIntensity = 0f,
                            statusMessage = event.message
                        ),
                        snackbarMessage = event.message
                    )
                }
            }
        }
    }

    fun executeVoiceTranscript(transcript: String) {
        viewModelScope.launch {
            val command = VoiceCommandParser.parse(
                rawTranscript = transcript,
                currentExerciseName = _uiState.value.selectedExercise.name
            )

            when (command) {
                is ParsedVoiceCommand.LogSet -> {
                    val state = _uiState.value
                    val matchedExercise = state.exercisesCatalog.find {
                        it.name.equals(command.exerciseName, ignoreCase = true)
                    } ?: state.selectedExercise

                    val nextNumber = state.activeSets.size + 1
                    val newSet = SetLogEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = state.activeSessionId,
                        exerciseId = matchedExercise.id,
                        setNumber = nextNumber,
                        weight = command.weightKg,
                        reps = command.reps,
                        inputSource = "VOICE",
                        rawVoiceText = command.rawText,
                        isWarmup = command.isWarmup,
                        isFailure = command.isFailure,
                        isCompleted = true
                    )
                    repository.saveSet(newSet, command.rawText, command.confidence)

                    val weightFormatted = if (command.weightKg % 1f == 0f) "${command.weightKg.toInt()}" else "${command.weightKg}"
                    val warmupPrefix = if (command.isWarmup) "Разминка " else ""
                    val failureSuffix = if (command.isFailure) " (до отказа)" else ""
                    val successMessage = "Записано: $warmupPrefix$weightFormatted кг × ${command.reps}$failureSuffix"

                    _uiState.update {
                        it.copy(
                            selectedExercise = matchedExercise,
                            voiceState = it.voiceState.copy(
                                status = VoiceStatus.READY,
                                isListening = false,
                                isProcessing = false,
                                recognizedText = transcript,
                                parsedExerciseName = matchedExercise.name,
                                parsedWeight = command.weightKg,
                                parsedReps = command.reps,
                                isWarmup = command.isWarmup,
                                statusMessage = "Готов к записи"
                            ),
                            snackbarMessage = successMessage
                        )
                    }
                }

                is ParsedVoiceCommand.SwitchExercise -> {
                    val state = _uiState.value
                    val found = state.exercisesCatalog.find {
                        it.name.contains(command.exerciseName, ignoreCase = true) ||
                        command.exerciseName.contains(it.name, ignoreCase = true)
                    } ?: repository.insertCustomExercise(command.exerciseName, "Все", "Штанга").toDomain()

                    _uiState.update {
                        it.copy(
                            selectedExercise = found,
                            voiceState = it.voiceState.copy(
                                status = VoiceStatus.READY,
                                isListening = false,
                                isProcessing = false,
                                recognizedText = transcript,
                                parsedExerciseName = found.name,
                                parsedWeight = found.defaultWeightKg,
                                parsedReps = found.defaultReps,
                                statusMessage = "Готов к записи"
                            ),
                            snackbarMessage = "Выбрано упражнение: «${found.name}»"
                        )
                    }
                }

                is ParsedVoiceCommand.RepeatLastSet -> {
                    val state = _uiState.value
                    val last = state.activeSets.lastOrNull()
                    if (last != null) {
                        val nextNumber = state.activeSets.size + 1
                        val newSet = SetLogEntity(
                            id = UUID.randomUUID().toString(),
                            sessionId = state.activeSessionId,
                            exerciseId = last.exerciseId.ifBlank { state.selectedExercise.id },
                            setNumber = nextNumber,
                            weight = last.weightKg,
                            reps = last.reps,
                            inputSource = "VOICE",
                            rawVoiceText = command.rawText,
                            isWarmup = last.isWarmup,
                            isFailure = last.isFailure,
                            isCompleted = true
                        )
                        repository.saveSet(newSet, command.rawText, command.confidence)
                        _uiState.update {
                            it.copy(
                                voiceState = it.voiceState.copy(
                                    status = VoiceStatus.READY,
                                    isListening = false,
                                    isProcessing = false,
                                    recognizedText = transcript,
                                    statusMessage = "Готов к записи"
                                ),
                                snackbarMessage = "Записано: ${last.displayWeight} кг × ${last.reps} (повтор подхода #${last.setNumber})"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                voiceState = it.voiceState.copy(
                                    status = VoiceStatus.READY,
                                    isListening = false,
                                    isProcessing = false,
                                    recognizedText = transcript,
                                    statusMessage = "Нет предыдущего подхода для повтора"
                                ),
                                snackbarMessage = "Предыдущих подходов пока нет"
                            )
                        }
                    }
                }

                is ParsedVoiceCommand.DeleteLastSet -> {
                    val last = _uiState.value.activeSets.lastOrNull()
                    if (last != null) {
                        repository.deleteLastSet(_uiState.value.activeSessionId)
                        _uiState.update {
                            it.copy(
                                voiceState = it.voiceState.copy(
                                    status = VoiceStatus.READY,
                                    isListening = false,
                                    isProcessing = false,
                                    recognizedText = transcript,
                                    statusMessage = "Готов к записи"
                                ),
                                snackbarMessage = "Последний подход #${last.setNumber} (${last.summaryText}) удалён"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                voiceState = it.voiceState.copy(
                                    status = VoiceStatus.READY,
                                    isListening = false,
                                    isProcessing = false,
                                    recognizedText = transcript,
                                    statusMessage = "Список подходов пуст"
                                ),
                                snackbarMessage = "Список подходов пуст"
                            )
                        }
                    }
                }

                is ParsedVoiceCommand.CorrectWeight -> {
                    val last = _uiState.value.activeSets.lastOrNull()
                    if (last != null) {
                        val entity = SetLogEntity(
                            id = last.id,
                            sessionId = last.sessionId.ifBlank { _uiState.value.activeSessionId },
                            exerciseId = last.exerciseId.ifBlank { _uiState.value.selectedExercise.id },
                            setNumber = last.setNumber,
                            weight = command.newWeightKg,
                            reps = last.reps,
                            isWarmup = last.isWarmup,
                            isFailure = last.isFailure,
                            isCompleted = last.isCompleted,
                            inputSource = "VOICE",
                            rawVoiceText = command.rawText,
                            loggedAt = last.timestamp
                        )
                        repository.updateSet(entity)
                        val formattedWeight = if (command.newWeightKg % 1f == 0f) "${command.newWeightKg.toInt()}" else "${command.newWeightKg}"
                        _uiState.update {
                            it.copy(
                                voiceState = it.voiceState.copy(
                                    status = VoiceStatus.READY,
                                    isListening = false,
                                    isProcessing = false,
                                    recognizedText = transcript,
                                    parsedWeight = command.newWeightKg,
                                    statusMessage = "Готов к записи"
                                ),
                                snackbarMessage = "Вес последнего подхода изменён на $formattedWeight кг"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                voiceState = it.voiceState.copy(
                                    status = VoiceStatus.READY,
                                    isListening = false,
                                    isProcessing = false,
                                    recognizedText = transcript,
                                    statusMessage = "Нет подходов для изменения веса"
                                ),
                                snackbarMessage = "Нет подходов для изменения веса"
                            )
                        }
                    }
                }

                is ParsedVoiceCommand.AddNewExercise -> {
                    val entity = repository.insertCustomExercise(command.newExerciseName, "Все", "Тренажёр")
                    _uiState.update {
                        it.copy(
                            selectedExercise = entity.toDomain(),
                            voiceState = it.voiceState.copy(
                                status = VoiceStatus.READY,
                                isListening = false,
                                isProcessing = false,
                                recognizedText = transcript,
                                parsedExerciseName = entity.name,
                                statusMessage = "Готов к записи"
                            ),
                            snackbarMessage = "Добавлено упражнение: «${entity.name}»"
                        )
                    }
                }

                is ParsedVoiceCommand.FinishExercise -> {
                    _uiState.update {
                        it.copy(
                            voiceState = it.voiceState.copy(
                                status = VoiceStatus.READY,
                                isListening = false,
                                isProcessing = false,
                                recognizedText = transcript,
                                statusMessage = "Упражнение завершено"
                            ),
                            snackbarMessage = "Упражнение завершено! Выберите следующее упражнение"
                        )
                    }
                }

                is ParsedVoiceCommand.FinishWorkout -> {
                    _uiState.update {
                        it.copy(
                            showFinishDialog = true,
                            voiceState = it.voiceState.copy(
                                status = VoiceStatus.READY,
                                isListening = false,
                                isProcessing = false,
                                recognizedText = transcript,
                                statusMessage = "Завершение тренировки"
                            )
                        )
                    }
                }

                is ParsedVoiceCommand.Unknown -> {
                    // Do NOT save set on unrecognized command; show example
                    _uiState.update {
                        it.copy(
                            voiceState = it.voiceState.copy(
                                status = VoiceStatus.READY,
                                isListening = false,
                                isProcessing = false,
                                recognizedText = transcript,
                                statusMessage = command.reason
                            ),
                            snackbarMessage = "Команда не понята. Пример: «жим лёжа 80 на 8» или «80 на 8»"
                        )
                    }
                }
            }
        }
    }

    private fun startSimulatedWave() {
        simulatedWaveJob?.cancel()
        simulatedWaveJob = viewModelScope.launch {
            while (isActive) {
                val wave = (0.25f..0.95f).random()
                _uiState.update {
                    it.copy(voiceState = it.voiceState.copy(audioWaveIntensity = wave))
                }
                delay(100)
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(needRecordAudioPermission = false) }
        if (granted) {
            _uiState.update {
                it.copy(snackbarMessage = "Доступ к микрофону предоставлен")
            }
        } else {
            _uiState.update {
                it.copy(
                    voiceState = it.voiceState.copy(statusMessage = "Нет доступа к микрофону"),
                    snackbarMessage = "Нет доступа к микрофону"
                )
            }
        }
    }

    fun applyVoiceSample(sample: VoiceSample) {
        executeVoiceTranscript(sample.rawPhrase)
    }

    fun addRecognizedSetToWorkout() {
        viewModelScope.launch {
            val vs = _uiState.value.voiceState
            val state = _uiState.value
            val matchedExercise = state.exercisesCatalog.find { 
                it.name.equals(vs.parsedExerciseName, ignoreCase = true) 
            } ?: state.selectedExercise

            val newSetEntity = SetLogEntity(
                id = UUID.randomUUID().toString(),
                sessionId = state.activeSessionId,
                exerciseId = matchedExercise.id,
                setNumber = state.activeSets.size + 1,
                weight = vs.parsedWeight,
                reps = vs.parsedReps,
                inputSource = "VOICE",
                rawVoiceText = vs.recognizedText,
                isWarmup = vs.isWarmup,
                isCompleted = true
            )

            repository.saveSet(newSetEntity, vs.recognizedText, 0.99f)

            val displayWeight = if (newSetEntity.weight % 1f == 0f) "${newSetEntity.weight.toInt()}" else "${newSetEntity.weight}"
            _uiState.update {
                it.copy(
                    selectedExercise = matchedExercise,
                    snackbarMessage = "Записано: $displayWeight кг × ${newSetEntity.reps}"
                )
            }
        }
    }

    fun addQuickSet(weight: Float, reps: Int, isWarmup: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            val nextNumber = state.activeSets.size + 1
            val newSet = SetLogEntity(
                id = UUID.randomUUID().toString(),
                sessionId = state.activeSessionId,
                exerciseId = state.selectedExercise.id,
                setNumber = nextNumber,
                weight = weight,
                reps = reps,
                inputSource = "QUICK",
                isWarmup = isWarmup,
                isCompleted = true
            )
            repository.saveSet(newSet)
            val displayWeight = if (weight % 1f == 0f) "${weight.toInt()}" else "$weight"
            _uiState.update {
                it.copy(snackbarMessage = "Записано: $displayWeight кг × $reps")
            }
        }
    }

    fun toggleSetCompleted(setId: String) {
        viewModelScope.launch {
            val target = _uiState.value.activeSets.find { it.id == setId } ?: return@launch
            val entity = SetLogEntity(
                id = target.id,
                sessionId = target.sessionId.ifBlank { _uiState.value.activeSessionId },
                exerciseId = target.exerciseId.ifBlank { _uiState.value.selectedExercise.id },
                setNumber = target.setNumber,
                weight = target.weightKg,
                reps = target.reps,
                isWarmup = target.isWarmup,
                isFailure = target.isFailure,
                isCompleted = !target.isCompleted,
                inputSource = target.inputSource,
                rawVoiceText = target.rawVoiceText,
                loggedAt = target.timestamp
            )
            repository.updateSet(entity)
        }
    }

    fun openEditSetDialog(set: WorkoutSet) {
        _uiState.update { it.copy(setToEdit = set) }
    }

    fun closeEditSetDialog() {
        _uiState.update { it.copy(setToEdit = null) }
    }

    fun updateSetDetails(setId: String, weightKg: Float, reps: Int, isWarmup: Boolean, isFailure: Boolean) {
        viewModelScope.launch {
            val target = _uiState.value.activeSets.find { it.id == setId } ?: return@launch
            val entity = SetLogEntity(
                id = target.id,
                sessionId = target.sessionId.ifBlank { _uiState.value.activeSessionId },
                exerciseId = target.exerciseId.ifBlank { _uiState.value.selectedExercise.id },
                setNumber = target.setNumber,
                weight = weightKg,
                reps = reps,
                isWarmup = isWarmup,
                isFailure = isFailure,
                isCompleted = target.isCompleted,
                inputSource = target.inputSource,
                rawVoiceText = target.rawVoiceText,
                loggedAt = target.timestamp
            )
            repository.updateSet(entity)
            val displayWeight = if (weightKg % 1f == 0f) "${weightKg.toInt()}" else "$weightKg"
            _uiState.update {
                it.copy(
                    setToEdit = null,
                    snackbarMessage = "Подход #${target.setNumber} обновлён: $displayWeight кг × $reps"
                )
            }
        }
    }

    fun removeSet(setId: String) {
        viewModelScope.launch {
            repository.deleteSetById(setId, _uiState.value.activeSessionId)
            _uiState.update {
                it.copy(
                    setToEdit = null,
                    snackbarMessage = "Подход удалён"
                )
            }
        }
    }

    fun removeLastSet() {
        viewModelScope.launch {
            val sets = _uiState.value.activeSets
            if (sets.isNotEmpty()) {
                val last = sets.last()
                repository.deleteLastSet(_uiState.value.activeSessionId)
                _uiState.update {
                    it.copy(snackbarMessage = "Удалён последний подход #${last.setNumber} (${last.summaryText})")
                }
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        _uiState.update {
            it.copy(
                selectedExercise = exercise,
                voiceState = it.voiceState.copy(
                    parsedExerciseName = exercise.name,
                    parsedWeight = exercise.defaultWeightKg,
                    parsedReps = exercise.defaultReps
                )
            )
        }
    }

    fun setCategoryFilter(category: ExerciseCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addNewExercise(name: String, category: ExerciseCategory, targetMuscles: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val entity = repository.insertCustomExercise(
                name = name,
                muscleGroup = category.displayName,
                equipment = "Тренажёр"
            )
            _uiState.update {
                it.copy(
                    showAddExerciseDialog = false,
                    selectedExercise = entity.toDomain(),
                    snackbarMessage = "Упражнение «$name» добавлено в базу данных"
                )
            }
        }
    }

    // --- WORKOUT FINISHING & HISTORY ---

    fun showFinishWorkoutDialog() {
        _uiState.update { it.copy(showFinishDialog = true) }
    }

    fun hideFinishWorkoutDialog() {
        _uiState.update { it.copy(showFinishDialog = false) }
    }

    fun finishAndSaveWorkout() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.finishSession(state.activeSessionId)

            val nextSessionId = "session_${UUID.randomUUID()}"
            repository.createNewSession(nextSessionId)

            _uiState.update {
                it.copy(
                    activeSessionId = nextSessionId,
                    showFinishDialog = false,
                    workoutTimerSeconds = 0,
                    currentTab = NavItem.HISTORY,
                    snackbarMessage = "Тренировка сохранена в базу данных Room!"
                )
            }
            observeSetsForSession(nextSessionId)
        }
    }

    fun startNewWorkout() {
        viewModelScope.launch {
            val newSessionId = "session_${UUID.randomUUID()}"
            repository.createNewSession(newSessionId)
            _uiState.update {
                it.copy(
                    activeSessionId = newSessionId,
                    currentTab = NavItem.WORKOUT,
                    workoutTimerSeconds = 0,
                    isTimerRunning = true,
                    snackbarMessage = "Новая тренировка запущена"
                )
            }
            observeSetsForSession(newSessionId)
        }
    }

    fun viewHistoryDetail(session: WorkoutSession?) {
        _uiState.update { it.copy(selectedHistorySession = session) }
    }

    // --- SETTINGS ---

    fun setLanguage(lang: String) {
        _uiState.update { it.copy(settings = it.settings.copy(language = lang)) }
    }

    fun setWeightUnit(unit: String) {
        _uiState.update { it.copy(settings = it.settings.copy(weightUnit = unit)) }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(offlineMode = enabled)) }
    }

    fun toggleSoundFeedback(enabled: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(soundFeedback = enabled)) }
    }

    fun toggleVibration(enabled: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(vibrationFeedback = enabled)) }
    }

    fun openVoiceHelpDialog() {
        _uiState.update { it.copy(showVoiceHelpDialog = true) }
    }

    fun closeVoiceHelpDialog() {
        _uiState.update { it.copy(showVoiceHelpDialog = false) }
    }

    fun openAddExerciseDialog() {
        _uiState.update { it.copy(showAddExerciseDialog = true) }
    }

    fun closeAddExerciseDialog() {
        _uiState.update { it.copy(showAddExerciseDialog = false) }
    }

    fun openClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun closeClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            val newSessionId = "session_${UUID.randomUUID()}"
            repository.createNewSession(newSessionId)
            _uiState.update {
                it.copy(
                    activeSessionId = newSessionId,
                    showClearDataDialog = false,
                    snackbarMessage = "База данных очищена"
                )
            }
            observeSetsForSession(newSessionId)
        }
    }

    fun exportHistory() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun closeExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }

    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + Math.random().toFloat() * (endInclusive - start)
    }

    private fun ExerciseEntity.toDomain(): Exercise {
        val cat = when (muscleGroup.lowercase()) {
            "грудь" -> ExerciseCategory.CHEST
            "спина" -> ExerciseCategory.BACK
            "ноги" -> ExerciseCategory.LEGS
            "плечи" -> ExerciseCategory.SHOULDERS
            "руки" -> ExerciseCategory.ARMS
            "пресс" -> ExerciseCategory.CORE
            else -> ExerciseCategory.ALL
        }
        return Exercise(
            id = id,
            name = name,
            normalizedName = normalizedName,
            category = cat,
            targetMuscles = muscleGroup,
            equipment = equipment,
            isCustom = isCustom,
            personalRecord = personalRecord,
            defaultWeightKg = defaultWeight,
            defaultReps = defaultReps
        )
    }

    private fun SetLogEntity.toDomain(): WorkoutSet {
        return WorkoutSet(
            id = id,
            sessionId = sessionId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            weightKg = weight,
            reps = reps,
            isWarmup = isWarmup,
            isFailure = isFailure,
            isCompleted = isCompleted,
            inputSource = inputSource,
            rawVoiceText = rawVoiceText,
            timestamp = loggedAt
        )
    }
}

data class VoiceSample(
    val rawPhrase: String,
    val exerciseName: String,
    val weight: Float,
    val reps: Int,
    val isWarmup: Boolean
)
