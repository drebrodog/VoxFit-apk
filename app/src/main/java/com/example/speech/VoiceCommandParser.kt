package com.example.speech

import java.util.Locale

sealed interface ParsedVoiceCommand {
    data class LogSet(
        val rawText: String,
        val exerciseName: String?,
        val weightKg: Float,
        val reps: Int,
        val isWarmup: Boolean = false,
        val isFailure: Boolean = false,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "log_set"
    ) : ParsedVoiceCommand

    data class SwitchExercise(
        val rawText: String,
        val exerciseName: String,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "switch_exercise"
    ) : ParsedVoiceCommand

    data class RepeatLastSet(
        val rawText: String,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "repeat_set"
    ) : ParsedVoiceCommand

    data class DeleteLastSet(
        val rawText: String,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "delete_last_set"
    ) : ParsedVoiceCommand

    data class CorrectWeight(
        val rawText: String,
        val newWeightKg: Float,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "correct_weight"
    ) : ParsedVoiceCommand

    data class AddNewExercise(
        val rawText: String,
        val newExerciseName: String,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "add_exercise"
    ) : ParsedVoiceCommand

    data class FinishExercise(
        val rawText: String,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "finish_exercise"
    ) : ParsedVoiceCommand

    data class FinishWorkout(
        val rawText: String,
        val confidence: Float = 0.99f,
        val matchedPatternId: String = "finish_workout"
    ) : ParsedVoiceCommand

    data class Unknown(
        val rawText: String,
        val reason: String = "Команда не понята. Пример: «жим лёжа 80 на 8» или «80 на 8»"
    ) : ParsedVoiceCommand
}

object VoiceCommandParser {

    private val numberWordsMap = mapOf(
        "ноль" to 0f,
        "один" to 1f, "одна" to 1f, "раз" to 1f,
        "два" to 2f, "две" to 2f,
        "три" to 3f,
        "четыре" to 4f,
        "пять" to 5f,
        "шесть" to 6f,
        "семь" to 7f,
        "восемь" to 8f,
        "девять" to 9f,
        "десять" to 10f,
        "одиннадцать" to 11f,
        "двенадцать" to 12f,
        "тринадцать" to 13f,
        "четырнадцать" to 14f,
        "пятнадцать" to 15f,
        "шестнадцать" to 16f,
        "семнадцать" to 17f,
        "восемнадцать" to 18f,
        "девятнадцать" to 19f,
        "двадцать" to 20f,
        "тридцать" to 30f,
        "сорок" to 40f,
        "пятьдесят" to 50f,
        "шестьдесят" to 60f,
        "семьдесят" to 70f,
        "восемьдесят" to 80f,
        "девяносто" to 90f,
        "сто" to 100f,
        "двести" to 200f,
        "триста" to 300f,
        "четыреста" to 400f,
        "пятьсот" to 500f
    )

    private val exerciseKeywords = mapOf(
        "жим лежа" to "Жим лёжа",
        "жим лёжа" to "Жим лёжа",
        "жим" to "Жим лёжа",
        "присед со штангой" to "Приседания со штангой",
        "приседания со штангой" to "Приседания со штангой",
        "приседания" to "Приседания со штангой",
        "присед" to "Приседания со штангой",
        "тяга штанги в наклоне" to "Тяга штанги в наклоне",
        "тяга в наклоне" to "Тяга штанги в наклоне",
        "тяга" to "Тяга штанги в наклоне",
        "жим ногами" to "Жим ногами",
        "подтягивания" to "Подтягивания",
        "армейский жим" to "Армейский жим",
        "подъем на бицепс" to "Подъем на бицепс",
        "подъём на бицепс" to "Подъем на бицепс",
        "бицепс" to "Подъем на бицепс",
        "французский жим" to "Французский жим",
        "разведение гантелей" to "Разведение гантелей",
        "разведение" to "Разведение гантелей",
        "скручивания на блоке" to "Скручивания на блоке",
        "скручивания" to "Скручивания на блоке",
        "пресс" to "Скручивания на блоке",
        "выпады с гантелями" to "Выпады с гантелями",
        "выпады" to "Выпады с гантелями",
        "становая тяга" to "Становая тяга",
        "становая" to "Становая тяга",
        "брусья" to "Отжимания на брусьях",
        "отжимания" to "Отжимания на брусьях"
    )

    fun parse(rawTranscript: String, currentExerciseName: String = "Жим лёжа"): ParsedVoiceCommand {
        val clean = rawTranscript.trim().lowercase(Locale("ru")).replace('ё', 'е')
        if (clean.isBlank()) {
            return ParsedVoiceCommand.Unknown(rawTranscript, "Речь не распознана")
        }

        // 1. Команда: «закончить тренировку» / «завершить тренировку»
        if (clean.contains("закончить тренировку") ||
            clean.contains("завершить тренировку") ||
            clean.contains("закончил тренировку") ||
            clean.contains("завершил тренировку") ||
            clean.contains("тренировка окончена") ||
            clean.contains("тренировка закончена")
        ) {
            return ParsedVoiceCommand.FinishWorkout(rawTranscript)
        }

        // 2. Команда: «закончил упражнение» / «закончить упражнение»
        if (clean.contains("закончил упражнение") ||
            clean.contains("закончить упражнение") ||
            clean.contains("завершил упражнение") ||
            clean.contains("завершить упражнение") ||
            clean.contains("следующее упражнение")
        ) {
            return ParsedVoiceCommand.FinishExercise(rawTranscript)
        }

        // 3. Команда: «повтори предыдущий подход» / «повтори подход»
        if (clean.contains("повтори предыдущий подход") ||
            clean.contains("повтори подход") ||
            clean.contains("повторить подход") ||
            clean.contains("повторить предыдущий подход") ||
            clean.contains("повтори") ||
            clean.contains("повторить") ||
            clean.contains("еще один такой же") ||
            clean.contains("ещё один такой же")
        ) {
            return ParsedVoiceCommand.RepeatLastSet(rawTranscript)
        }

        // 4. Команда: «удали последний подход» / «отмени подход»
        if (clean.contains("удали последний подход") ||
            clean.contains("удалить последний подход") ||
            clean.contains("удали подход") ||
            clean.contains("удалить подход") ||
            clean.contains("отмени подход") ||
            clean.contains("отменить подход") ||
            clean.contains("стереть подход") ||
            clean.contains("убрать подход")
        ) {
            return ParsedVoiceCommand.DeleteLastSet(rawTranscript)
        }

        // 5. Команда: «исправь вес на 82.5» / «исправить вес на 85»
        if (clean.startsWith("исправь вес") ||
            clean.startsWith("исправить вес") ||
            clean.startsWith("измени вес") ||
            clean.startsWith("изменить вес") ||
            clean.startsWith("поменяй вес") ||
            clean.startsWith("поменять вес") ||
            clean.startsWith("вес на") ||
            clean.startsWith("исправь на")
        ) {
            val numbers = parseNumbersList(clean)
            if (numbers.isNotEmpty()) {
                return ParsedVoiceCommand.CorrectWeight(rawTranscript, numbers.first())
            }
        }

        // 6. Команда: «новое упражнение выпады с гантелями»
        if (clean.startsWith("новое упражнение") || clean.startsWith("добавь упражнение") || clean.startsWith("создай упражнение")) {
            val name = clean
                .removePrefix("новое упражнение")
                .removePrefix("добавь упражнение")
                .removePrefix("создай упражнение")
                .trim()
            if (name.isNotBlank()) {
                val formattedName = name.replaceFirstChar { it.uppercase() }
                return ParsedVoiceCommand.AddNewExercise(rawTranscript, formattedName)
            }
        }

        // 7. Команда: «начинаю жим лёжа» / «начинаем приседания»
        if (clean.startsWith("начинаю") || clean.startsWith("начинаем") || clean.startsWith("перехожу на") || clean.startsWith("упражнение")) {
            val target = clean
                .removePrefix("начинаю")
                .removePrefix("начинаем")
                .removePrefix("перехожу на")
                .removePrefix("упражнение")
                .trim()
            val matched = findExerciseName(target) ?: target.replaceFirstChar { it.uppercase() }
            if (matched.isNotBlank()) {
                return ParsedVoiceCommand.SwitchExercise(rawTranscript, matched)
            }
        }

        // 8. Команда фиксации подхода: «жим лёжа 80 на 8», «80 на 8», «жим лёжа 80 с половиной на 8», «разминка 40 на 12», «80 на 8 до отказа»
        val isWarmup = clean.contains("разминка") || clean.contains("разминочный") || clean.contains("разогрев")
        val isFailure = clean.contains("отказ") || clean.contains("до отказа") || clean.contains("максимум")

        val matchedExercise = findExerciseName(clean)

        // Parse numbers: weight and reps
        val numbers = parseNumbersList(clean)

        if (numbers.size >= 2) {
            val weight = numbers[0]
            val reps = numbers[1].toInt().coerceIn(1, 100)
            return ParsedVoiceCommand.LogSet(
                rawText = rawTranscript,
                exerciseName = matchedExercise ?: currentExerciseName,
                weightKg = weight,
                reps = reps,
                isWarmup = isWarmup,
                isFailure = isFailure,
                confidence = 0.99f
            )
        } else if (numbers.size == 1 && (clean.contains("на") || clean.contains("по") || clean.contains("повторений"))) {
            // e.g. "80 на 8" parsed with regex fallback
            val pattern = Regex("""(\d+(?:[.,]\d+)?)\s*(?:кг|килограмм)?\s*(?:на|по|х|x|\*)\s*(\d+)""")
            val match = pattern.find(clean)
            if (match != null) {
                val weight = match.groupValues[1].replace(',', '.').toFloatOrNull() ?: numbers[0]
                val reps = match.groupValues[2].toIntOrNull() ?: 8
                return ParsedVoiceCommand.LogSet(
                    rawText = rawTranscript,
                    exerciseName = matchedExercise ?: currentExerciseName,
                    weightKg = weight,
                    reps = reps,
                    isWarmup = isWarmup,
                    isFailure = isFailure,
                    confidence = 0.99f
                )
            }
        }

        // Check if user just named an exercise without numbers: "жим лежа"
        if (matchedExercise != null && numbers.isEmpty()) {
            return ParsedVoiceCommand.SwitchExercise(rawTranscript, matchedExercise)
        }

        return ParsedVoiceCommand.Unknown(
            rawText = rawTranscript,
            reason = "Команда не понята. Пример: «жим лёжа 80 на 8» или «80 на 8»"
        )
    }

    private fun findExerciseName(text: String): String? {
        val clean = text.replace('ё', 'е').lowercase(Locale("ru"))
        for ((key, canonical) in exerciseKeywords) {
            val normKey = key.replace('ё', 'е')
            if (clean.contains(normKey)) {
                return canonical
            }
        }
        return null
    }

    /**
     * Parses all numbers (both Arabic digits like 82.5 and Russian words like «восемьдесят пять»,
     * «восемьдесят с половиной», «двадцать точка пять»).
     */
    fun parseNumbersList(text: String): List<Float> {
        val clean = text.lowercase(Locale("ru")).replace('ё', 'е')
        val tokens = clean.split(Regex("""[\s,;—\-]+""")).filter { it.isNotBlank() }
        val result = mutableListOf<Float>()

        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]

            // 1. Direct digit token: "82.5", "82,5", "80"
            val directNum = token.replace(',', '.').toFloatOrNull()
            if (directNum != null) {
                var value = directNum
                // Check if followed by "с половиной", "точка пять", "запятая пять", "и пять"
                if (i + 2 < tokens.size && tokens[i + 1] == "с" && tokens[i + 2].startsWith("половин")) {
                    value += 0.5f
                    i += 2
                } else if (i + 2 < tokens.size && (tokens[i + 1] == "точка" || tokens[i + 1] == "запятая" || tokens[i + 1] == "и") && (tokens[i + 2] == "пять" || tokens[i + 2] == "5")) {
                    value += 0.5f
                    i += 2
                }
                result.add(value)
                i++
                continue
            }

            // 2. Russian word numbers: "восемьдесят", "пять", "сто двадцать пять", "восемьдесят с половиной"
            if (numberWordsMap.containsKey(token)) {
                var currentNumber = numberWordsMap[token] ?: 0f

                // Accumulate compound hundreds, tens, units: e.g. "сто" + "двадцать" + "пять"
                while (i + 1 < tokens.size && numberWordsMap.containsKey(tokens[i + 1])) {
                    val next = numberWordsMap[tokens[i + 1]] ?: 0f
                    currentNumber += next
                    i++
                }

                // Check fraction: "с половиной", "точка пять", "запятая пять"
                if (i + 2 < tokens.size && tokens[i + 1] == "с" && tokens[i + 2].startsWith("половин")) {
                    currentNumber += 0.5f
                    i += 2
                } else if (i + 2 < tokens.size && (tokens[i + 1] == "точка" || tokens[i + 1] == "запятая" || tokens[i + 1] == "и") && (tokens[i + 2] == "пять" || tokens[i + 2] == "5")) {
                    currentNumber += 0.5f
                    i += 2
                }

                result.add(currentNumber)
                i++
                continue
            }

            i++
        }

        return result
    }
}
