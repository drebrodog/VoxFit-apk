package com.example.speech

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import java.util.Locale

sealed interface VoiceRecognitionEvent {
    data class Ready(val message: String = "Слушаю…") : VoiceRecognitionEvent
    data class WaveIntensity(val intensity: Float) : VoiceRecognitionEvent
    data class PartialResult(val text: String) : VoiceRecognitionEvent
    data class FinalResult(val text: String) : VoiceRecognitionEvent
    data class Error(val message: String, val errorCode: Int) : VoiceRecognitionEvent
}

class VoiceRecognitionManager(
    private val context: Context,
    private val onEvent: (VoiceRecognitionEvent) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening: Boolean = false

    fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOrCreateRecognizer(): SpeechRecognizer? {
        if (speechRecognizer != null) return speechRecognizer

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return null
        }

        val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onEvent(VoiceRecognitionEvent.Ready("Слушаю…"))
            }

            override fun onBeginningOfSpeech() {
                onEvent(VoiceRecognitionEvent.Ready("Слушаю речь…"))
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB usually varies from -2 to 10 dB
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.15f, 1.0f)
                onEvent(VoiceRecognitionEvent.WaveIntensity(normalized))
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                onEvent(VoiceRecognitionEvent.WaveIntensity(0f))
            }

            override fun onError(error: Int) {
                isListening = false
                val message = when (error) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Нет доступа к микрофону"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Команда не распознана"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Команда не распознана (таймаут)"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Распознаватель занят"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Нет сети или таймаут"
                    SpeechRecognizer.ERROR_AUDIO -> "Ошибка записи звука"
                    SpeechRecognizer.ERROR_SERVER -> "Ошибка сервера распознавания"
                    SpeechRecognizer.ERROR_CLIENT -> "Распознавание завершено"
                    else -> "Ошибка распознавания ($error)"
                }
                onEvent(VoiceRecognitionEvent.Error(message, error))
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val finalTranscript = matches?.firstOrNull()
                if (!finalTranscript.isNullOrBlank()) {
                    onEvent(VoiceRecognitionEvent.FinalResult(finalTranscript))
                } else {
                    onEvent(VoiceRecognitionEvent.Error("Команда не распознана", SpeechRecognizer.ERROR_NO_MATCH))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull()
                if (!partialText.isNullOrBlank()) {
                    onEvent(VoiceRecognitionEvent.PartialResult(partialText))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer = recognizer
        return recognizer
    }

    fun startListening(): Boolean {
        if (!isPermissionGranted()) {
            onEvent(VoiceRecognitionEvent.Error("Нет доступа к микрофону. Разрешите запись аудио.", SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
            return false
        }

        try {
            val recognizer = getOrCreateRecognizer()
            if (recognizer == null) {
                onEvent(VoiceRecognitionEvent.Error("Распознавание речи недоступно на данном устройстве", -1))
                return false
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            recognizer.startListening(intent)
            isListening = true
            return true
        } catch (e: Exception) {
            onEvent(VoiceRecognitionEvent.Error("Ошибка запуска: ${e.localizedMessage}", -1))
            return false
        }
    }

    fun stopListening() {
        try {
            if (isListening) {
                speechRecognizer?.stopListening()
            }
        } catch (_: Exception) {
        }
    }

    fun cancel() {
        try {
            isListening = false
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }
    }

    fun destroy() {
        try {
            isListening = false
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {
        }
    }
}
