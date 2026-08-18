package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    WORKOUT("Тренировка", Icons.Filled.Mic, "nav_workout"),
    HISTORY("История", Icons.Filled.History, "nav_history"),
    EXERCISES("Упражнения", Icons.Filled.FitnessCenter, "nav_exercises"),
    SETTINGS("Настройки", Icons.Filled.Settings, "nav_settings")
}

data class WorkoutSet(
    val id: String,
    val sessionId: String = "",
    val exerciseId: String = "",
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val isWarmup: Boolean = false,
    val isFailure: Boolean = false,
    val isCompleted: Boolean = true,
    val inputSource: String = "VOICE", // VOICE, MANUAL, QUICK
    val rawVoiceText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val displayWeight: String
        get() = if (weightKg % 1f == 0f) "${weightKg.toInt()}" else "$weightKg"

    val summaryText: String
        get() = when {
            isWarmup -> "Разминка $displayWeight кг × $reps"
            isFailure -> "$displayWeight кг × $reps (до отказа)"
            else -> "$displayWeight кг × $reps"
        }
}

data class Exercise(
    val id: String,
    val name: String,
    val normalizedName: String = "",
    val category: ExerciseCategory,
    val targetMuscles: String,
    val equipment: String = "Штанга",
    val isCustom: Boolean = false,
    val personalRecord: String = "—",
    val defaultWeightKg: Float = 60f,
    val defaultReps: Int = 10,
    val description: String = ""
)

enum class ExerciseCategory(val displayName: String) {
    ALL("Все"),
    CHEST("Грудь"),
    BACK("Спина"),
    LEGS("Ноги"),
    SHOULDERS("Плечи"),
    ARMS("Руки"),
    CORE("Пресс")
}

data class WorkoutExercise(
    val exercise: Exercise,
    val sets: List<WorkoutSet>
) {
    val totalVolumeKg: Float
        get() = sets.filter { it.isCompleted }.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()
}

data class WorkoutSession(
    val id: String,
    val title: String,
    val dateFormatted: String,
    val durationFormatted: String,
    val durationSeconds: Int,
    val exercises: List<WorkoutExercise>,
    val totalVolumeKg: Float,
    val status: String = "COMPLETED",
    val isCompleted: Boolean = true
) {
    val exerciseCount: Int
        get() = exercises.size
}

enum class VoiceStatus(val label: String) {
    READY("Готов к записи"),
    LISTENING("Слушаю…"),
    PROCESSING("Распознаю команду…"),
    RECOGNIZED("Команда принята")
}

data class VoiceState(
    val status: VoiceStatus = VoiceStatus.READY,
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val rawSpeechText: String = "",
    val recognizedText: String = "жим лёжа восемьдесят на восемь",
    val parsedExerciseName: String = "Жим лёжа",
    val parsedWeight: Float = 80f,
    val parsedReps: Int = 8,
    val isWarmup: Boolean = false,
    val audioWaveIntensity: Float = 0f,
    val statusMessage: String = "Готов к записи"
)

data class AppSettings(
    val language: String = "Русский",
    val weightUnit: String = "кг",
    val offlineMode: Boolean = true,
    val soundFeedback: Boolean = true,
    val vibrationFeedback: Boolean = true,
    val voiceSensitivity: Float = 0.85f,
    val appVersion: String = "1.0.0",
    val author: String = "Rudenko A. D."
)
