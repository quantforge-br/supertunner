package com.example

import com.example.domain.PitchMath
import com.example.domain.TuningCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class PitchMathTest {

    @Test
    fun yinDetectsStandardLowE2() {
        val sampleRate = 44100
        val targetFreq = 82.4069
        val samples = FloatArray(3072) { i ->
            (0.7 * sin(2.0 * PI * targetFreq * i / sampleRate) +
                0.2 * sin(2.0 * PI * (targetFreq * 2.0) * i / sampleRate)).toFloat()
        }

        val result = PitchMath.yinPitchDetect(samples, sampleRate)
        assertNotNull(result)
        assertTrue(abs(result!!.frequencyHz - targetFreq) < 0.6)
        assertTrue(result.confidence > 0.8f)
    }

    @Test
    fun yinDetectsHighE4() {
        val sampleRate = 44100
        val targetFreq = 329.6276
        val samples = FloatArray(3072) { i ->
            (0.8 * sin(2.0 * PI * targetFreq * i / sampleRate)).toFloat()
        }

        val result = PitchMath.yinPitchDetect(samples, sampleRate)
        assertNotNull(result)
        assertTrue(abs(result!!.frequencyHz - targetFreq) < 0.8)
    }

    @Test
    fun matchToDropDPresetIdentifiesSixthString() {
        val dropD = TuningCatalog.findById("drop_d")
        val match = PitchMath.matchToPreset(
            frequencyHz = 73.42,
            preset = dropD,
            lockedString = null
        )
        assertEquals("D", match.noteName)
        assertEquals(2, match.octave)
        assertEquals(6, match.matchedString?.stringNumber)
        assertTrue(abs(match.centsDeviation) < 1.0f)
    }
}
