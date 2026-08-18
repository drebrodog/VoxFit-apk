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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.ui.theme.VoxBackground
import com.example.ui.theme.VoxBlue
import com.example.ui.theme.VoxBorder
import com.example.ui.theme.VoxCyan
import com.example.ui.theme.VoxGreenSuccess
import com.example.ui.theme.VoxRedMic
import com.example.ui.theme.VoxSurface
import com.example.ui.theme.VoxSurfaceVariant
import com.example.ui.theme.VoxTextMuted
import com.example.ui.theme.VoxTextPrimary
import com.example.ui.theme.VoxTextSecondary

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onLanguageChange: (String) -> Unit,
    onWeightUnitChange: (String) -> Unit,
    onToggleOfflineMode: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onOpenVoiceHelp: () -> Unit,
    onExportHistory: () -> Unit,
    onOpenClearDataDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(VoxBackground)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp)
    ) {
        item {
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = VoxTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 1. LOCALIZATION & UNITS ---
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
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ОСНОВНЫЕ ПАРАМЕТРЫ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = VoxTextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language: Русский / English
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = VoxBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Язык интерфейса", style = MaterialTheme.typography.bodyLarge, color = VoxTextPrimary)
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoxSurfaceVariant)
                                .padding(4.dp)
                        ) {
                            val isRu = settings.language == "Русский"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isRu) VoxBlue else Color.Transparent)
                                    .clickable { onLanguageChange("Русский") }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("lang_ru_button")
                            ) {
                                Text(
                                    text = "Русский",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isRu) Color.White else VoxTextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isRu) VoxBlue else Color.Transparent)
                                    .clickable { onLanguageChange("English") }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("lang_en_button")
                            ) {
                                Text(
                                    text = "English",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (!isRu) Color.White else VoxTextSecondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = VoxBorder)

                    // Units: кг / lbs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = VoxCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Единицы измерения", style = MaterialTheme.typography.bodyLarge, color = VoxTextPrimary)
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoxSurfaceVariant)
                                .padding(4.dp)
                        ) {
                            val isKg = settings.weightUnit == "кг"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isKg) VoxCyan else Color.Transparent)
                                    .clickable { onWeightUnitChange("кг") }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .testTag("unit_kg_button")
                            ) {
                                Text(
                                    text = "кг",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isKg) Color.Black else VoxTextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isKg) VoxCyan else Color.Transparent)
                                    .clickable { onWeightUnitChange("lbs") }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .testTag("unit_lbs_button")
                            ) {
                                Text(
                                    text = "lbs",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (!isKg) Color.Black else VoxTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- 2. VOICE & OFFLINE ENGINE ---
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
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ГОЛОС И РАСПОЗНАВАНИЕ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = VoxTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Offline Mode Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = VoxGreenSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Офлайн-режим", style = MaterialTheme.typography.bodyLarge, color = VoxTextPrimary)
                                Text(
                                    "Локальное распознавание без интернета",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = VoxTextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = settings.offlineMode,
                            onCheckedChange = onToggleOfflineMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VoxGreenSuccess,
                                uncheckedThumbColor = VoxTextMuted,
                                uncheckedTrackColor = VoxSurfaceVariant
                            ),
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = VoxBorder)

                    // Voice Command Examples
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenVoiceHelp() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = VoxBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Примеры голосовых команд", style = MaterialTheme.typography.bodyLarge, color = VoxTextPrimary)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VoxTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- 3. DATA & HISTORY MANAGEMENT ---
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
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ДАННЫЕ И РЕЗЕРВНОЕ КОПИРОВАНИЕ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = VoxTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Export History
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExportHistory() }
                            .padding(vertical = 4.dp)
                            .testTag("export_history_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = VoxBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Экспорт истории", style = MaterialTheme.typography.bodyLarge, color = VoxTextPrimary)
                                Text("Сохранить тренировки в текстовом виде", style = MaterialTheme.typography.bodySmall, color = VoxTextSecondary)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VoxTextMuted)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = VoxBorder)

                    // Clear Data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenClearDataDialog() }
                            .padding(vertical = 4.dp)
                            .testTag("clear_data_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = VoxRedMic, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Очистка данных", style = MaterialTheme.typography.bodyLarge, color = VoxRedMic)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = VoxTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 4. BLOCK «О ПРИЛОЖЕНИИ» (About the app as requested) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("about_app_card"),
                colors = CardDefaults.cardColors(containerColor = VoxSurface),
                border = BorderStroke(1.dp, VoxBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(VoxBlue, VoxCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "V",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "VoxFit",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = VoxTextPrimary
                    )

                    Text(
                        text = "Голосовой дневник тренировок",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VoxCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Версия ${settings.appVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VoxTextSecondary
                    )

                    Text(
                        text = "Автор: ${settings.author}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = VoxTextPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
