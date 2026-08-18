package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["normalizedName"])]
)
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val normalizedName: String,
    val muscleGroup: String,
    val equipment: String,
    val isCustom: Boolean = false,
    val personalRecord: String = "—",
    val defaultWeight: Float = 60f,
    val defaultReps: Int = 10
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey
    val id: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val status: String = "IN_PROGRESS" // IN_PROGRESS, COMPLETED, CANCELLED
)

@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["exerciseId"])
    ]
)
data class SetLogEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val exerciseId: String,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val inputSource: String = "VOICE", // VOICE, MANUAL, QUICK
    val rawVoiceText: String? = null,
    val isWarmup: Boolean = false,
    val isFailure: Boolean = false,
    val isCompleted: Boolean = true,
    val loggedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "voice_commands",
    foreignKeys = [
        ForeignKey(
            entity = SetLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["setId"])]
)
data class VoiceCommandEntity(
    @PrimaryKey
    val id: String,
    val setId: String? = null,
    val transcript: String,
    val matchedPatternId: String? = null,
    val confidence: Float = 0.99f,
    val createdAt: Long = System.currentTimeMillis()
)
