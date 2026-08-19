package com.example

import com.example.speech.ParsedVoiceCommand
import com.example.speech.VoiceCommandParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun testSwitchExercise() {
        val result = VoiceCommandParser.parse("начинаю жим лёжа", "Приседания")
        assertTrue(result is ParsedVoiceCommand.SwitchExercise)
        val cmd = result as ParsedVoiceCommand.SwitchExercise
        assertEquals("Жим лёжа", cmd.exerciseName)
    }

    @Test
    fun testLogSetDirect() {
        val result = VoiceCommandParser.parse("жим лёжа 80 на 8", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.LogSet)
        val cmd = result as ParsedVoiceCommand.LogSet
        assertEquals(80f, cmd.weightKg, 0.01f)
        assertEquals(8, cmd.reps)
        assertEquals(false, cmd.isWarmup)
    }

    @Test
    fun testLogSetShort() {
        val result = VoiceCommandParser.parse("80 на 8", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.LogSet)
        val cmd = result as ParsedVoiceCommand.LogSet
        assertEquals(80f, cmd.weightKg, 0.01f)
        assertEquals(8, cmd.reps)
    }

    @Test
    fun testLogSetFractionalWeightWords() {
        val result = VoiceCommandParser.parse("жим лёжа 80 с половиной на 8", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.LogSet)
        val cmd = result as ParsedVoiceCommand.LogSet
        assertEquals(80.5f, cmd.weightKg, 0.01f)
        assertEquals(8, cmd.reps)
    }

    @Test
    fun testWarmupSet() {
        val result = VoiceCommandParser.parse("разминка 40 на 12", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.LogSet)
        val cmd = result as ParsedVoiceCommand.LogSet
        assertEquals(40f, cmd.weightKg, 0.01f)
        assertEquals(12, cmd.reps)
        assertEquals(true, cmd.isWarmup)
    }

    @Test
    fun testFailureSet() {
        val result = VoiceCommandParser.parse("80 на 8 до отказа", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.LogSet)
        val cmd = result as ParsedVoiceCommand.LogSet
        assertEquals(80f, cmd.weightKg, 0.01f)
        assertEquals(8, cmd.reps)
        assertEquals(true, cmd.isFailure)
    }

    @Test
    fun testRepeatPreviousSet() {
        val result = VoiceCommandParser.parse("повтори предыдущий подход", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.RepeatLastSet)
    }

    @Test
    fun testDeleteLastSet() {
        val result = VoiceCommandParser.parse("удали последний подход", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.DeleteLastSet)
    }

    @Test
    fun testCorrectWeight() {
        val result = VoiceCommandParser.parse("исправь вес на 82.5", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.CorrectWeight)
        val cmd = result as ParsedVoiceCommand.CorrectWeight
        assertEquals(82.5f, cmd.newWeightKg, 0.01f)
    }

    @Test
    fun testAddNewExercise() {
        val result = VoiceCommandParser.parse("новое упражнение выпады с гантелями", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.AddNewExercise)
        val cmd = result as ParsedVoiceCommand.AddNewExercise
        assertEquals("Выпады с гантелями", cmd.newExerciseName)
    }

    @Test
    fun testFinishExercise() {
        val result = VoiceCommandParser.parse("закончил упражнение", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.FinishExercise)
    }

    @Test
    fun testFinishWorkout() {
        val result = VoiceCommandParser.parse("закончить тренировку", "Жим лёжа")
        assertTrue(result is ParsedVoiceCommand.FinishWorkout)
    }

    @Test
    fun testNumberParsingWords() {
        val numbers1 = VoiceCommandParser.parseNumbersList("восемьдесят пять")
        assertEquals(listOf(85f), numbers1)

        val numbers2 = VoiceCommandParser.parseNumbersList("восемьдесят с половиной")
        assertEquals(listOf(80.5f), numbers2)

        val numbers3 = VoiceCommandParser.parseNumbersList("двадцать точка пять")
        assertEquals(listOf(20.5f), numbers3)
    }
}
