package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WorkoutSession
import com.example.ui.theme.VoxBackground
import com.example.ui.theme.VoxBlue
import com.example.ui.theme.VoxBorder
import com.example.ui.theme.VoxCyan
import com.example.ui.theme.VoxSurface
import com.example.ui.theme.VoxSurfaceVariant
import com.example.ui.theme.VoxTextMuted
import com.example.ui.theme.VoxTextPrimary
import com.example.ui.theme.VoxTextSecondary

@Composable
fun HistoryScreen(
    historyList: List<WorkoutSession>,
    onSelectSession: (WorkoutSession) -> Unit,
    onStartNewWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVolumeAllTime = historyList.sumOf { it.totalVolumeKg.toDouble() }.toInt()
    val totalWorkoutsCount = historyList.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VoxBackground)
            .testTag("history_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp)
        ) {
            // Header stats overview banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = VoxSurface),
                    border = BorderStroke(1.dp, VoxBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "История тренировок",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = VoxTextPrimary
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VoxBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$totalWorkoutsCount сессий",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = VoxBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total tonnage
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VoxSurfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Общий тоннаж",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VoxTextMuted
                                )
                                Text(
                                    text = "$totalVolumeAllTime кг",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = VoxCyan
                                )
                            }

                            // Average intensity
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VoxSurfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Средний объём",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VoxTextMuted
                                )
                                val avg = if (totalWorkoutsCount > 0) totalVolumeAllTime / totalWorkoutsCount else 0
                                Text(
                                    text = "$avg кг",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = VoxBlue
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (historyList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = VoxTextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "История пуста",
                                style = MaterialTheme.typography.titleMedium,
                                color = VoxTextSecondary
                            )
                            Text(
                                text = "Завершите тренировку, чтобы она появилась здесь",
                                style = MaterialTheme.typography.bodySmall,
                                color = VoxTextMuted
                            )
                        }
                    }
                }
            } else {
                // Required format example: «18 августа — 52 мин — 4 упражнения — 6 840 кг»
                items(historyList, key = { it.id }) { session ->
                    val volumeFormatted = String.format("%,d", session.totalVolumeKg.toInt()).replace(',', ' ')
                    val summaryLine = "${session.dateFormatted} — ${session.durationFormatted} — ${session.exerciseCount} упражнения — $volumeFormatted кг"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onSelectSession(session) }
                            .testTag("history_card_${session.id}"),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = session.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = VoxTextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // The full summary line as requested by prompt
                                Text(
                                    text = summaryLine,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = VoxCyan
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Exercise badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    session.exercises.take(3).forEach { ex ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(VoxSurfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = ex.exercise.name,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = VoxTextSecondary
                                            )
                                        }
                                    }
                                    if (session.exercises.size > 3) {
                                        Text(
                                            text = "+${session.exercises.size - 3}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = VoxTextMuted
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Подробнее",
                                tint = VoxTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Start new workout Floating Action Button with mic icon
        ExtendedFloatingActionButton(
            onClick = onStartNewWorkout,
            icon = {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Голосовой старт",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            },
            text = {
                Text(
                    text = "Новая тренировка",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            containerColor = VoxBlue,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("start_workout_fab")
        )
    }
}
