package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.ExerciseCategory
import com.example.model.WorkoutSession
import com.example.ui.theme.VoxBackground
import com.example.ui.theme.VoxBlue
import com.example.ui.theme.VoxBorder
import com.example.ui.theme.VoxCyan
import com.example.ui.theme.VoxGreenSuccess
import com.example.ui.theme.VoxOrangeAccent
import com.example.ui.theme.VoxRedMic
import com.example.ui.theme.VoxSurface
import com.example.ui.theme.VoxSurfaceHover
import com.example.ui.theme.VoxSurfaceVariant
import com.example.ui.theme.VoxTextMuted
import com.example.ui.theme.VoxTextPrimary
import com.example.ui.theme.VoxTextSecondary

@Composable
fun FinishWorkoutDialog(
    timerText: String,
    totalVolumeKg: Float,
    setsCount: Int,
    exerciseName: String,
    onConfirmFinish: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VoxSurface,
            border = BorderStroke(1.dp, VoxBorder),
            modifier = Modifier.testTag("finish_workout_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(VoxOrangeAccent.copy(alpha = 0.15f))
                        .border(2.dp, VoxOrangeAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Победа",
                        tint = VoxOrangeAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Завершить тренировку?",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = VoxTextPrimary
                )

                Text(
                    text = "Отличная работа в зале! Результаты будут сохранены в дневник.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoxTextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats summary cards in 2 columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Time stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoxSurfaceVariant)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Время", style = MaterialTheme.typography.labelSmall, color = VoxTextMuted)
                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = VoxCyan
                        )
                    }

                    // Volume stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoxSurfaceVariant)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Тоннаж", style = MaterialTheme.typography.labelSmall, color = VoxTextMuted)
                        Text(
                            text = "${totalVolumeKg.toInt()} кг",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = VoxBlue
                        )
                    }

                    // Sets stat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoxSurfaceVariant)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Подходов", style = MaterialTheme.typography.labelSmall, color = VoxTextMuted)
                        Text(
                            text = "$setsCount",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = VoxGreenSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VoxTextSecondary)
                    ) {
                        Text("Продолжить")
                    }

                    Button(
                        onClick = onConfirmFinish,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_finish_workout_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoxBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VoxSurface,
            border = BorderStroke(1.dp, VoxBorder),
            modifier = Modifier.testTag("voice_help_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = VoxBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Голосовые команды",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = VoxTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = VoxTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VoxFit понимает естественные фразы на русском языке во время отдыха между подходами:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VoxTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                val examples = listOf(
                    "«начинаю жим лёжа»" to "Выбирает или переключает упражнение",
                    "«жим лёжа 80 на 8»" to "Записывает подход: 80 кг × 8 повторений",
                    "«80 на 8»" to "Записывает подход в текущее упражнение",
                    "«жим лёжа 80 с половиной на 8»" to "Дробный вес: 80.5 кг × 8",
                    "«разминка 40 на 12»" to "Отмечает подход как разминочный",
                    "«80 на 8 до отказа»" to "Отмечает подход до отказа",
                    "«повтори предыдущий подход»" to "Дублирует последний подход",
                    "«удали последний подход»" to "Удаляет ошибочно добавленный подход",
                    "«исправь вес на 82.5»" to "Корректирует вес последнего подхода",
                    "«новое упражнение выпады с гантелями»" to "Создает и выбирает упражнение",
                    "«закончил упражнение»" to "Завершает текущее упражнение",
                    "«закончить тренировку»" to "Открывает окно завершения тренировки",
                    "«восемьдесят пять» / «двадцать точка пять»" to "Числа словами и дроби"
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    items(examples) { (phrase, desc) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(VoxSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = phrase,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = VoxCyan
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = VoxTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VoxBlue)
                ) {
                    Text("Понятно", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    onConfirm: (name: String, category: ExerciseCategory, muscles: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExerciseCategory.CHEST) }
    var muscles by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VoxSurface,
            border = BorderStroke(1.dp, VoxBorder),
            modifier = Modifier.testTag("add_exercise_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Новое упражнение",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = VoxTextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название упражнения") },
                    placeholder = { Text("Например: Жим Арнольда") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoxBlue,
                        unfocusedBorderColor = VoxBorder,
                        focusedTextColor = VoxTextPrimary,
                        unfocusedTextColor = VoxTextPrimary,
                        focusedContainerColor = VoxSurfaceVariant,
                        unfocusedContainerColor = VoxSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VoxBlue,
                            unfocusedBorderColor = VoxBorder,
                            focusedTextColor = VoxTextPrimary,
                            unfocusedTextColor = VoxTextPrimary,
                            focusedContainerColor = VoxSurfaceVariant,
                            unfocusedContainerColor = VoxSurfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(VoxSurface)
                    ) {
                        ExerciseCategory.entries.filterNot { it == ExerciseCategory.ALL }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName, color = VoxTextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target muscles
                OutlinedTextField(
                    value = muscles,
                    onValueChange = { muscles = it },
                    label = { Text("Целевые мышцы") },
                    placeholder = { Text("Например: Передняя дельта, верх груди") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoxBlue,
                        unfocusedBorderColor = VoxBorder,
                        focusedTextColor = VoxTextPrimary,
                        unfocusedTextColor = VoxTextPrimary,
                        focusedContainerColor = VoxSurfaceVariant,
                        unfocusedContainerColor = VoxSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VoxTextSecondary)
                    ) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = { onConfirm(name, selectedCategory, muscles) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_new_exercise_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VoxBlue,
                            disabledContainerColor = VoxSurfaceHover
                        )
                    ) {
                        Text("Создать", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsBottomSheet(
    session: WorkoutSession,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VoxSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(VoxTextMuted)
            )
        },
        modifier = Modifier.testTag("workout_details_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = VoxTextPrimary
                    )
                    Text(
                        text = "${session.dateFormatted} • ${session.durationFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VoxCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(VoxBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${session.totalVolumeKg.toInt()} кг",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VoxBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of exercises in this session
            Text(
                text = "Упражнения и подходы (${session.exerciseCount}):",
                style = MaterialTheme.typography.labelLarge,
                color = VoxTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 380.dp)
            ) {
                items(session.exercises) { workoutEx ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(VoxSurfaceVariant)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = workoutEx.exercise.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = VoxTextPrimary
                            )
                            Text(
                                text = "${workoutEx.totalVolumeKg.toInt()} кг суммарно",
                                style = MaterialTheme.typography.bodySmall,
                                color = VoxTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sets list
                        workoutEx.sets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Подход ${set.setNumber}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (set.isWarmup) VoxOrangeAccent else VoxTextSecondary
                                    )
                                    if (set.isWarmup) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(Разминка)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VoxOrangeAccent
                                        )
                                    }
                                }

                                Text(
                                    text = "${set.displayWeight} кг × ${set.reps}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = VoxTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VoxSurfaceHover)
            ) {
                Text("Закрыть", color = VoxTextPrimary)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ExportDialog(
    history: List<WorkoutSession>,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val exportText = buildString {
        appendLine("--- VOXFIT WORKOUT DIARY EXPORT ---")
        history.forEach { session ->
            appendLine("${session.dateFormatted} | ${session.title} | ${session.durationFormatted} | Тоннаж: ${session.totalVolumeKg.toInt()} кг")
            session.exercises.forEach { ex ->
                appendLine("  • ${ex.exercise.name}:")
                ex.sets.forEach { set ->
                    appendLine("     Подход ${set.setNumber}: ${set.displayWeight} кг × ${set.reps}${if (set.isWarmup) " (Разминка)" else ""}")
                }
            }
            appendLine()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VoxSurface,
            border = BorderStroke(1.dp, VoxBorder),
            modifier = Modifier.testTag("export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Экспорт истории",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = VoxTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = VoxTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Текстовая выгрузка тренировок для резервного копирования или анализа:",
                    style = MaterialTheme.typography.bodySmall,
                    color = VoxTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(VoxBackground)
                        .border(1.dp, VoxBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn {
                        item {
                            Text(
                                text = exportText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                color = VoxCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportText))
                            copied = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (copied) VoxGreenSuccess else VoxBlue
                        )
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (copied) "Скопировано!" else "Скопировать", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ClearDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Очистить историю тренировок?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = VoxTextPrimary
            )
        },
        text = {
            Text(
                text = "Все сохранённые записи тренировок и подходов будут удалены с устройства.",
                style = MaterialTheme.typography.bodyMedium,
                color = VoxTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = VoxRedMic),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Очистить", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = VoxTextSecondary)
            }
        },
        containerColor = VoxSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("clear_data_dialog")
    )
}

@Composable
fun EditSetDialog(
    set: com.example.model.WorkoutSet,
    onSave: (weightKg: Float, reps: Int, isWarmup: Boolean, isFailure: Boolean) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var weightText by remember { mutableStateOf(set.displayWeight) }
    var repsText by remember { mutableStateOf("${set.reps}") }
    var isWarmup by remember { mutableStateOf(set.isWarmup) }
    var isFailure by remember { mutableStateOf(set.isFailure) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VoxSurface,
            border = BorderStroke(1.dp, VoxBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_set_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Редактировать подход #${set.setNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = VoxTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = VoxTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weight input with quick stepper buttons for fractional weights (+2.5, -2.5, +0.5)
                Text(
                    text = "Вес (кг)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = VoxTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_weight_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VoxTextPrimary,
                            unfocusedTextColor = VoxTextPrimary,
                            focusedBorderColor = VoxBlue,
                            unfocusedBorderColor = VoxBorder,
                            focusedContainerColor = VoxSurfaceVariant,
                            unfocusedContainerColor = VoxSurfaceVariant
                        ),
                        singleLine = true,
                        placeholder = { Text("82.5", color = VoxTextMuted) }
                    )

                    // Quick +/- stepper
                    OutlinedButton(
                        onClick = {
                            val current = weightText.toFloatOrNull() ?: 0f
                            val next = (current - 2.5f).coerceAtLeast(0f)
                            weightText = if (next % 1f == 0f) "${next.toInt()}" else "$next"
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, VoxBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("-2.5", color = VoxTextPrimary, style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            val current = weightText.toFloatOrNull() ?: 0f
                            val next = current + 2.5f
                            weightText = if (next % 1f == 0f) "${next.toInt()}" else "$next"
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, VoxBorder),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("+2.5", color = VoxCyan, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reps input
                Text(
                    text = "Количество повторений",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = VoxTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_reps_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = VoxTextPrimary,
                            unfocusedTextColor = VoxTextPrimary,
                            focusedBorderColor = VoxBlue,
                            unfocusedBorderColor = VoxBorder,
                            focusedContainerColor = VoxSurfaceVariant,
                            unfocusedContainerColor = VoxSurfaceVariant
                        ),
                        singleLine = true,
                        placeholder = { Text("8", color = VoxTextMuted) }
                    )

                    OutlinedButton(
                        onClick = {
                            val current = repsText.toIntOrNull() ?: 0
                            val next = (current - 1).coerceAtLeast(1)
                            repsText = "$next"
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, VoxBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("-1", color = VoxTextPrimary, style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            val current = repsText.toIntOrNull() ?: 0
                            val next = current + 1
                            repsText = "$next"
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, VoxBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+1", color = VoxCyan, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Warmup & Failure toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { isWarmup = !isWarmup },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isWarmup) VoxOrangeAccent.copy(alpha = 0.2f) else VoxSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, if (isWarmup) VoxOrangeAccent else VoxBorder)
                    ) {
                        Text(
                            text = if (isWarmup) "✓ Разминка" else "Разминка",
                            color = if (isWarmup) VoxOrangeAccent else VoxTextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    OutlinedButton(
                        onClick = { isFailure = !isFailure },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isFailure) VoxRedMic.copy(alpha = 0.2f) else VoxSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, if (isFailure) VoxRedMic else VoxBorder)
                    ) {
                        Text(
                            text = if (isFailure) "✓ До отказа" else "До отказа",
                            color = if (isFailure) VoxRedMic else VoxTextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Delete & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VoxRedMic),
                        border = BorderStroke(1.dp, VoxRedMic.copy(alpha = 0.5f))
                    ) {
                        Text("Удалить", color = VoxRedMic)
                    }

                    Button(
                        onClick = {
                            val weight = weightText.replace(',', '.').toFloatOrNull() ?: set.weightKg
                            val reps = repsText.toIntOrNull() ?: set.reps
                            onSave(weight, reps, isWarmup, isFailure)
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VoxBlue)
                    ) {
                        Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

