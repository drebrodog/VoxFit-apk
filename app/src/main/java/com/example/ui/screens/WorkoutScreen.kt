package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Exercise
import com.example.model.VoiceState
import com.example.model.WorkoutSet
import com.example.ui.components.VoiceMicrophoneButton
import com.example.ui.theme.VoxBackground
import com.example.ui.theme.VoxBlue
import com.example.ui.theme.VoxBorder
import com.example.ui.theme.VoxCyan
import com.example.ui.theme.VoxGreenSuccess
import com.example.ui.theme.VoxOrangeAccent
import com.example.ui.theme.VoxRedMic
import com.example.ui.theme.VoxSurface
import com.example.ui.theme.VoxSurfaceVariant
import com.example.ui.theme.VoxTextMuted
import com.example.ui.theme.VoxTextPrimary
import com.example.ui.theme.VoxTextSecondary
import com.example.viewmodel.VoiceSample

@Composable
fun WorkoutScreen(
    currentExercise: Exercise,
    allExercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    activeSets: List<WorkoutSet>,
    voiceState: VoiceState,
    onMicPressDown: () -> Unit,
    onMicPressUp: () -> Unit,
    onQuickSampleClicked: (VoiceSample) -> Unit,
    onAddRecognizedSet: () -> Unit,
    onToggleSetCompleted: (String) -> Unit,
    onEditSet: (WorkoutSet) -> Unit,
    onRemoveSet: (String) -> Unit,
    onRemoveLastSet: () -> Unit,
    onAddQuickSet: (weight: Float, reps: Int, isWarmup: Boolean) -> Unit,
    onOpenVoiceHelp: () -> Unit,
    onFinishWorkoutClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var exerciseMenuExpanded by remember { mutableStateOf(false) }
    val lastSet = activeSets.lastOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VoxBackground)
            .testTag("workout_screen"),
        contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
    ) {
        // --- 1. CURRENT EXERCISE SELECTOR BAR ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("current_exercise_card"),
                colors = CardDefaults.cardColors(containerColor = VoxSurface),
                border = BorderStroke(1.dp, VoxBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VoxBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = VoxBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ТЕКУЩЕЕ УПРАЖНЕНИЕ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = VoxTextMuted
                            )
                        }

                        // Category tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(VoxSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentExercise.category.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VoxCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Exercise Name with dropdown trigger
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VoxSurfaceVariant)
                                .clickable { exerciseMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("exercise_selector_button"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentExercise.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp
                                    ),
                                    color = VoxTextPrimary
                                )
                                Text(
                                    text = currentExercise.targetMuscles,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VoxTextSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Выбрать другое упражнение",
                                tint = VoxBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Exercise selector dropdown
                        DropdownMenu(
                            expanded = exerciseMenuExpanded,
                            onDismissRequest = { exerciseMenuExpanded = false },
                            modifier = Modifier
                                .background(VoxSurface)
                                .border(width = 1.dp, color = VoxBorder, shape = RoundedCornerShape(12.dp))
                        ) {
                            allExercises.forEach { exercise ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = exercise.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = if (exercise.id == currentExercise.id) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (exercise.id == currentExercise.id) VoxBlue else VoxTextPrimary
                                            )
                                            Text(
                                                text = "${exercise.category.displayName} • Рекорд: ${exercise.personalRecord}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = VoxTextMuted
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectExercise(exercise)
                                        exerciseMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. LAST SET HIGHLIGHT CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("last_set_highlight_card"),
                colors = CardDefaults.cardColors(containerColor = VoxSurface),
                border = BorderStroke(1.dp, VoxBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ПОСЛЕДНИЙ ПОДХОД",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = VoxTextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VoxBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Подход ${lastSet?.setNumber ?: 3}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = VoxBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large weight & reps supporting fraction: 82.5 кг × 8
                        Text(
                            text = lastSet?.let { "${it.displayWeight} кг × ${it.reps}" } ?: "80 кг × 8",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = VoxTextPrimary
                        )
                    }

                    // Status / PR badge
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = VoxGreenSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+2.5 кг к ПР",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VoxGreenSuccess
                            )
                        }
                        Text(
                            text = "Отдых: ~90 сек",
                            style = MaterialTheme.typography.bodySmall,
                            color = VoxTextMuted
                        )
                    }
                }
            }
        }

        // --- 3. CENTRAL HERO PUSH-TO-TALK MIC BUTTON ---
        item {
            Spacer(modifier = Modifier.height(6.dp))
            VoiceMicrophoneButton(
                voiceState = voiceState,
                onPressDown = onMicPressDown,
                onPressUp = onMicPressUp,
                onQuickSampleClicked = onQuickSampleClicked,
                onOpenVoiceHelp = onOpenVoiceHelp
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // --- 4. BLOCK «РАСПОЗНАНО» ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("recognized_voice_card"),
                colors = CardDefaults.cardColors(containerColor = VoxSurface),
                border = BorderStroke(
                    1.dp,
                    if (voiceState.isListening) VoxRedMic
                    else if (voiceState.isProcessing) VoxOrangeAccent
                    else VoxBlue.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (voiceState.isListening) VoxRedMic else VoxCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "РАСПОЗНАНО",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = VoxTextMuted
                            )
                        }

                        // Confidence badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(VoxGreenSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "99% точность",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VoxGreenSuccess
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "«${voiceState.recognizedText}»",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = VoxCyan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(VoxSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${voiceState.parsedExerciseName}${if (voiceState.isWarmup) " (Разминка)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = VoxTextSecondary
                            )
                            val displayParsedWeight = if (voiceState.parsedWeight % 1f == 0f) "${voiceState.parsedWeight.toInt()}" else "${voiceState.parsedWeight}"
                            Text(
                                text = "$displayParsedWeight кг × ${voiceState.parsedReps} повт.",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = VoxTextPrimary
                            )
                        }

                        Button(
                            onClick = onAddRecognizedSet,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VoxBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_recognized_set_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ Добавить подход",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- 5. SETS LIST HEADER WITH COMPACT DELETE LAST SET BUTTON ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Выполненные подходы (${activeSets.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VoxTextPrimary
                    )
                    Text(
                        text = "Объём: ${activeSets.filter { it.isCompleted }.sumOf { (it.weightKg * it.reps).toDouble() }.toInt()} кг",
                        style = MaterialTheme.typography.labelSmall,
                        color = VoxBlue
                    )
                }

                // Compact "Отменить подход" button
                if (activeSets.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onRemoveLastSet,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = VoxSurfaceVariant,
                            contentColor = VoxRedMic
                        ),
                        border = BorderStroke(1.dp, VoxRedMic.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("delete_last_set_compact_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Удалить последний подход",
                            tint = VoxRedMic,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Отменить подход",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = VoxRedMic
                        )
                    }
                }
            }
        }

        itemsIndexed(activeSets, key = { _, set -> set.id }) { _, set ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onEditSet(set) }
                    .testTag("set_item_${set.setNumber}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (set.isCompleted) VoxSurface else VoxSurfaceVariant
                ),
                border = BorderStroke(
                    1.dp,
                    if (set.isCompleted) VoxBorder else VoxBlue.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Set number badge & details
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (set.isWarmup) VoxOrangeAccent.copy(alpha = 0.2f)
                                    else VoxSurfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${set.setNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (set.isWarmup) VoxOrangeAccent else VoxTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (set.isWarmup) "Разминка" else if (set.isFailure) "До отказа" else "Рабочий подход",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (set.isWarmup) VoxOrangeAccent else if (set.isFailure) VoxRedMic else VoxTextMuted
                                )
                            }
                            Text(
                                text = set.summaryText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = VoxTextPrimary
                            )
                        }
                    }

                    // Right: Edit, Completion toggle & delete
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onEditSet(set) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать подход",
                                tint = VoxCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onToggleSetCompleted(set.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (set.isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = "Отметить выполненным",
                                tint = if (set.isCompleted) VoxGreenSuccess else VoxTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { onRemoveSet(set.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить подход",
                                tint = VoxTextMuted.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick manual add set bar & finish workout shortcut
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val last = activeSets.lastOrNull()
                        onAddQuickSet(last?.weightKg ?: 80f, last?.reps ?: 8, false)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VoxTextPrimary
                    ),
                    border = BorderStroke(1.dp, VoxBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = VoxBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Повторить подход", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        val last = activeSets.lastOrNull()
                        onAddQuickSet((last?.weightKg ?: 80f) + 2.5f, last?.reps ?: 8, false)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VoxTextPrimary
                    ),
                    border = BorderStroke(1.dp, VoxBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = VoxCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+2.5 кг к весу", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Finish Workout Card / Button at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = onFinishWorkoutClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("finish_workout_bottom_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoxSurfaceVariant,
                        contentColor = VoxRedMic
                    ),
                    border = BorderStroke(1.dp, VoxRedMic.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = VoxRedMic,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Завершить тренировку",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = VoxRedMic
                    )
                }
            }
        }
    }
}
