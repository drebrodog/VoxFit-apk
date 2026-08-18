package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.SetLogEntity
import com.example.data.local.entity.VoiceCommandEntity
import com.example.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class VoxFitRepository(private val database: AppDatabase) {

    private val exerciseDao = database.exerciseDao()
    private val sessionDao = database.workoutSessionDao()
    private val setLogDao = database.setLogDao()
    private val voiceCommandDao = database.voiceCommandDao()

    val allExercises: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()
    val allSessions: Flow<List<WorkoutSessionEntity>> = sessionDao.getAllSessions()
    val activeSession: Flow<WorkoutSessionEntity?> = sessionDao.getActiveSession()
    val allSets: Flow<List<SetLogEntity>> = setLogDao.getAllSets()

    fun getSetsForSession(sessionId: String): Flow<List<SetLogEntity>> =
        setLogDao.getSetsForSession(sessionId)

    suspend fun getSetsForSessionSync(sessionId: String): List<SetLogEntity> =
        setLogDao.getSetsForSessionSync(sessionId)

    suspend fun ensureDefaultExercises() {
        if (exerciseDao.getCount() == 0) {
            val defaults = listOf(
                ExerciseEntity(
                    id = "ex_1",
                    name = "Жим лёжа",
                    normalizedName = "жим лежа",
                    muscleGroup = "Грудь",
                    equipment = "Штанга",
                    isCustom = false,
                    personalRecord = "110 кг × 5",
                    defaultWeight = 80f,
                    defaultReps = 8
                ),
                ExerciseEntity(
                    id = "ex_2",
                    name = "Тяга штанги в наклоне",
                    normalizedName = "тяга штанги в наклоне",
                    muscleGroup = "Спина",
                    equipment = "Штанга",
                    isCustom = false,
                    personalRecord = "90 кг × 8",
                    defaultWeight = 70f,
                    defaultReps = 10
                ),
                ExerciseEntity(
                    id = "ex_3",
                    name = "Приседания со штангой",
                    normalizedName = "приседания со штангой",
                    muscleGroup = "Ноги",
                    equipment = "Штанга",
                    isCustom = false,
                    personalRecord = "140 кг × 5",
                    defaultWeight = 100f,
                    defaultReps = 8
                ),
                ExerciseEntity(
                    id = "ex_4",
                    name = "Жим ногами",
                    normalizedName = "жим ногами",
                    muscleGroup = "Ноги",
                    equipment = "Тренажёр",
                    isCustom = false,
                    personalRecord = "220 кг × 10",
                    defaultWeight = 160f,
                    defaultReps = 12
                ),
                ExerciseEntity(
                    id = "ex_5",
                    name = "Подтягивания",
                    normalizedName = "подтягивания",
                    muscleGroup = "Спина",
                    equipment = "Турник",
                    isCustom = false,
                    personalRecord = "+20 кг × 6",
                    defaultWeight = 0f,
                    defaultReps = 10
                ),
                ExerciseEntity(
                    id = "ex_6",
                    name = "Армейский жим",
                    normalizedName = "армейский жим",
                    muscleGroup = "Плечи",
                    equipment = "Штанга",
                    isCustom = false,
                    personalRecord = "65 кг × 6",
                    defaultWeight = 50f,
                    defaultReps = 8
                ),
                ExerciseEntity(
                    id = "ex_7",
                    name = "Подъем на бицепс",
                    normalizedName = "подъем на бицепс",
                    muscleGroup = "Руки",
                    equipment = "Штанга",
                    isCustom = false,
                    personalRecord = "45 кг × 8",
                    defaultWeight = 35f,
                    defaultReps = 10
                ),
                ExerciseEntity(
                    id = "ex_8",
                    name = "Французский жим",
                    normalizedName = "французский жим",
                    muscleGroup = "Руки",
                    equipment = "EZ-гриф",
                    isCustom = false,
                    personalRecord = "40 кг × 10",
                    defaultWeight = 30f,
                    defaultReps = 10
                ),
                ExerciseEntity(
                    id = "ex_9",
                    name = "Разведение гантелей",
                    normalizedName = "разведение гантелей",
                    muscleGroup = "Грудь",
                    equipment = "Гантели",
                    isCustom = false,
                    personalRecord = "22.5 кг × 10",
                    defaultWeight = 17.5f,
                    defaultReps = 12
                ),
                ExerciseEntity(
                    id = "ex_10",
                    name = "Скручивания на блоке",
                    normalizedName = "скручивания на блоке",
                    muscleGroup = "Пресс",
                    equipment = "Блок",
                    isCustom = false,
                    personalRecord = "60 кг × 15",
                    defaultWeight = 45f,
                    defaultReps = 15
                )
            )
            exerciseDao.insertExercises(defaults)
        }
    }

    suspend fun createNewSession(id: String = UUID.randomUUID().toString()): WorkoutSessionEntity {
        val session = WorkoutSessionEntity(
            id = id,
            startedAt = System.currentTimeMillis(),
            status = "IN_PROGRESS"
        )
        sessionDao.insertSession(session)
        return session
    }

    suspend fun saveSet(
        set: SetLogEntity,
        rawVoiceText: String? = null,
        confidence: Float = 0.99f
    ) {
        setLogDao.insertSet(set)
        if (!rawVoiceText.isNullOrBlank()) {
            val command = VoiceCommandEntity(
                id = UUID.randomUUID().toString(),
                setId = set.id,
                transcript = rawVoiceText,
                confidence = confidence,
                createdAt = System.currentTimeMillis()
            )
            voiceCommandDao.insertVoiceCommand(command)
        }
    }

    suspend fun updateSet(set: SetLogEntity) {
        setLogDao.updateSet(set)
    }

    suspend fun deleteSetById(setId: String, sessionId: String) {
        setLogDao.deleteSetById(setId)
        // Renumber remaining sets for session
        val remaining = setLogDao.getSetsForSessionSync(sessionId)
        val renumbered = remaining.mapIndexed { index, item ->
            item.copy(setNumber = index + 1)
        }
        setLogDao.insertSets(renumbered)
    }

    suspend fun deleteLastSet(sessionId: String) {
        val sets = setLogDao.getSetsForSessionSync(sessionId)
        if (sets.isNotEmpty()) {
            val lastSet = sets.last()
            setLogDao.deleteSetById(lastSet.id)
        }
    }

    suspend fun finishSession(sessionId: String, finishedAt: Long = System.currentTimeMillis()) {
        val session = sessionDao.getSessionById(sessionId)
        if (session != null) {
            sessionDao.updateSession(
                session.copy(
                    finishedAt = finishedAt,
                    status = "COMPLETED"
                )
            )
        }
    }

    suspend fun insertCustomExercise(
        name: String,
        muscleGroup: String,
        equipment: String = "Тренажёр"
    ): ExerciseEntity {
        val cleanName = name.trim()
        val normalized = cleanName.lowercase()
        val entity = ExerciseEntity(
            id = "custom_${UUID.randomUUID()}",
            name = cleanName,
            normalizedName = normalized,
            muscleGroup = muscleGroup,
            equipment = equipment,
            isCustom = true,
            personalRecord = "—",
            defaultWeight = 50f,
            defaultReps = 10
        )
        exerciseDao.insertExercise(entity)
        return entity
    }

    suspend fun clearAll() {
        sessionDao.clearAll()
        setLogDao.clearAll()
        voiceCommandDao.clearAll()
    }
}
