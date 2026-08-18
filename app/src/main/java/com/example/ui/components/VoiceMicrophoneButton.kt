package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VoiceState
import com.example.model.VoiceStatus
import com.example.ui.theme.VoxBlue
import com.example.ui.theme.VoxBorder
import com.example.ui.theme.VoxCyan
import com.example.ui.theme.VoxGreenSuccess
import com.example.ui.theme.VoxOrangeAccent
import com.example.ui.theme.VoxRedGlow
import com.example.ui.theme.VoxRedMic
import com.example.ui.theme.VoxSurfaceVariant
import com.example.ui.theme.VoxTextMuted
import com.example.ui.theme.VoxTextPrimary
import com.example.ui.theme.VoxTextSecondary
import com.example.viewmodel.VoiceSample

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceMicrophoneButton(
    voiceState: VoiceState,
    onPressDown: () -> Unit,
    onPressUp: () -> Unit,
    onQuickSampleClicked: (VoiceSample) -> Unit,
    onOpenVoiceHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for active listening state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_pulse"
    )

    // Rotation animation for processing state
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. STATUS BADGE: «Готов к записи», «Слушаю…», «Распознаю команду…» ---
        AnimatedContent(
            targetState = voiceState.status,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "status_badge_animation"
        ) { status ->
            val (badgeBg, badgeBorder, badgeTextColor, badgeIcon) = when (status) {
                VoiceStatus.LISTENING -> Quadruple(
                    VoxRedMic.copy(alpha = 0.2f),
                    VoxRedMic,
                    VoxRedGlow,
                    Icons.Default.GraphicEq
                )
                VoiceStatus.PROCESSING -> Quadruple(
                    VoxOrangeAccent.copy(alpha = 0.2f),
                    VoxOrangeAccent,
                    VoxOrangeAccent,
                    Icons.Default.Sync
                )
                VoiceStatus.READY, VoiceStatus.RECOGNIZED -> Quadruple(
                    VoxBlue.copy(alpha = 0.15f),
                    VoxBlue.copy(alpha = 0.4f),
                    VoxCyan,
                    Icons.Default.Mic
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBg)
                    .border(1.dp, badgeBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("voice_status_badge"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (status == VoiceStatus.PROCESSING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = VoxOrangeAccent
                        )
                    } else {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = when (status) {
                            VoiceStatus.LISTENING -> "Слушаю…"
                            VoiceStatus.PROCESSING -> "Распознаю команду…"
                            VoiceStatus.READY, VoiceStatus.RECOGNIZED -> "Готов к записи"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = badgeTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. CENTRAL HERO PUSH-TO-TALK BUTTON CONTAINER ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(185.dp)
                .testTag("push_to_talk_container")
        ) {
            // Pulsating audio radar waves when listening
            if (voiceState.isListening) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    // Inner wave
                    drawCircle(
                        color = VoxRedGlow.copy(alpha = pulseAlpha),
                        radius = (size.width / 2) * pulseScale * 0.95f,
                        center = center,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    // Outer wave
                    drawCircle(
                        color = VoxRedMic.copy(alpha = pulseAlpha * 0.6f),
                        radius = (size.width / 2) * pulseScale * 1.15f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else if (voiceState.isProcessing) {
                // Subtle rotating ring when processing
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .rotate(rotationAngle)
                ) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(VoxOrangeAccent, VoxCyan, Color.Transparent)
                        ),
                        radius = size.width / 2 * 0.98f,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            } else {
                // Subtle static glowing aura in idle "Готов к записи" state
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    VoxBlue.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Big Central Hero Circle Button
            val isListening = voiceState.isListening
            val isProcessing = voiceState.isProcessing

            val buttonColor1 = when {
                isListening -> VoxRedGlow
                isProcessing -> VoxOrangeAccent
                else -> VoxCyan
            }
            val buttonColor2 = when {
                isListening -> VoxRedMic
                isProcessing -> Color(0xFFE65100)
                else -> VoxBlue
            }
            val buttonColor3 = when {
                isListening -> Color(0xFF8E0000)
                isProcessing -> Color(0xFFBF360C)
                else -> Color(0xFF0D47A1)
            }

            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                shadowElevation = if (isListening) 20.dp else 10.dp,
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onPressDown()
                                tryAwaitRelease()
                                onPressUp()
                            }
                        )
                    }
                    .testTag("voice_mic_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(
                            Brush.radialGradient(
                                colors = listOf(buttonColor1, buttonColor2, buttonColor3)
                            )
                        )
                        .border(
                            width = 3.5.dp,
                            brush = Brush.linearGradient(
                                colors = when {
                                    isListening -> listOf(Color.White, VoxRedGlow)
                                    isProcessing -> listOf(Color.White, VoxOrangeAccent)
                                    else -> listOf(VoxCyan, VoxBlue)
                                }
                            ),
                            shape = CircleShape
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isListening -> Icons.Default.GraphicEq
                                isProcessing -> Icons.Default.Sync
                                else -> Icons.Default.Mic
                            },
                            contentDescription = "Микрофон голосового ввода",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(if (isListening) pulseScale.coerceAtMost(1.15f) else 1f)
                                .then(
                                    if (isProcessing) Modifier.rotate(rotationAngle) else Modifier
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when {
                                isListening -> "СЛУШАЮ…"
                                isProcessing -> "ОБРАБОТКА"
                                else -> "ГОЛОС"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Instruction caption under the button as requested:
        // «Удерживайте и скажите: жим лёжа 80 на 8»
        Text(
            text = "Удерживайте и скажите: жим лёжа 80 на 8",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = if (voiceState.isListening) VoxRedGlow else VoxTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick sample chips for testing voice simulation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Быстрые голосовые примеры:",
                style = MaterialTheme.typography.labelSmall,
                color = VoxTextMuted
            )
            IconButton(
                onClick = onOpenVoiceHelp,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "Справка команд",
                    tint = VoxBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val samples = listOf(
                VoiceSample("80 на 8", "Жим лёжа", 80f, 8, false),
                VoiceSample("жим лёжа 80 с половиной на 8", "Жим лёжа", 80.5f, 8, false),
                VoiceSample("разминка 40 на 12", "Жим лёжа", 40f, 12, true),
                VoiceSample("80 на 8 до отказа", "Жим лёжа", 80f, 8, false),
                VoiceSample("повтори предыдущий подход", "Жим лёжа", 80f, 8, false),
                VoiceSample("исправь вес на 82.5", "Жим лёжа", 82.5f, 8, false),
                VoiceSample("удали последний подход", "Жим лёжа", 80f, 8, false)
            )

            samples.forEach { sample ->
                AssistChip(
                    onClick = { onQuickSampleClicked(sample) },
                    label = {
                        Text(
                            text = "«${sample.rawPhrase}»",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = VoxTextPrimary
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = VoxSurfaceVariant,
                        labelColor = VoxTextPrimary
                    ),
                    border = BorderStroke(1.dp, VoxBorder),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
