package com.example.domain

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class InstrumentType(val id: String, val labelPt: String) {
    ACOUSTIC("acoustic", "Violão"),
    ELECTRIC("electric", "Guitarra");

    companion object {
        fun fromId(id: String?): InstrumentType =
            entries.firstOrNull { it.id == id } ?: ACOUSTIC
    }
}

enum class TuningCategory(val titlePt: String) {
    STANDARD("Clássicas & Drop"),
    DOWN_TUNED("Afinações Baixas"),
    OPEN("Afinações Abertas"),
    CHROMATIC("Modo Livre")
}

data class StringNote(
    val stringNumber: Int, // 6 (thickest) down to 1 (thinnest)
    val noteName: String,
    val accidental: String,
    val octave: Int,
    val frequencyHz: Double
) {
    val shortLabel: String get() = "$noteName$accidental"
    val fullLabel: String get() = "$noteName$accidental$octave"
}

data class TuningPreset(
    val id: String,
    val namePt: String,
    val summaryNotes: String,
    val descriptionPt: String,
    val category: TuningCategory,
    val strings: List<StringNote>,
    val isChromatic: Boolean = false
)

data class PitchDetectionResult(
    val frequencyHz: Double,
    val confidence: Float
)

data class DetectedNoteMatch(
    val noteName: String,
    val accidental: String,
    val octave: Int,
    val targetFrequencyHz: Double,
    val centsDeviation: Float,
    val matchedString: StringNote?
)

object TuningCatalog {

    private fun freq(midi: Int, a4Hz: Double = 440.0): Double {
        return a4Hz * 2.0.pow((midi - 69) / 12.0)
    }

    // MIDI reference: E2=40, A2=45, D3=50, G3=55, B3=59, E4=64
    private fun str(num: Int, name: String, acc: String, oct: Int, midi: Int): StringNote {
        return StringNote(
            stringNumber = num,
            noteName = name,
            accidental = acc,
            octave = oct,
            frequencyHz = freq(midi)
        )
    }

    private val standardStrings = listOf(
        str(6, "E", "", 2, 40),
        str(5, "A", "", 2, 45),
        str(4, "D", "", 3, 50),
        str(3, "G", "", 3, 55),
        str(2, "B", "", 3, 59),
        str(1, "E", "", 4, 64)
    )

    val presets: List<TuningPreset> = listOf(
        TuningPreset(
            id = "standard",
            namePt = "Padrão",
            summaryNotes = "E  A  D  G  B  E",
            descriptionPt = "Afinação universal para violão e guitarra",
            category = TuningCategory.STANDARD,
            strings = standardStrings
        ),
        TuningPreset(
            id = "drop_d",
            namePt = "Drop D",
            summaryNotes = "D  A  D  G  B  E",
            descriptionPt = "6ª corda um tom abaixo para riffs e peso",
            category = TuningCategory.STANDARD,
            strings = listOf(
                str(6, "D", "", 2, 38),
                str(5, "A", "", 2, 45),
                str(4, "D", "", 3, 50),
                str(3, "G", "", 3, 55),
                str(2, "B", "", 3, 59),
                str(1, "E", "", 4, 64)
            )
        ),
        TuningPreset(
            id = "half_step_down",
            namePt = "Meio Tom Abaixo",
            summaryNotes = "Eb Ab Db Gb Bb Eb",
            descriptionPt = "Todas as cordas 1 semitom abaixo (Eb Standard)",
            category = TuningCategory.DOWN_TUNED,
            strings = listOf(
                str(6, "E", "b", 2, 39),
                str(5, "A", "b", 2, 44),
                str(4, "D", "b", 3, 49),
                str(3, "G", "b", 3, 54),
                str(2, "B", "b", 3, 58),
                str(1, "E", "b", 4, 63)
            )
        ),
        TuningPreset(
            id = "whole_step_down",
            namePt = "Um Tom Abaixo",
            summaryNotes = "D  G  C  F  A  D",
            descriptionPt = "Timbre encorpado e menor tensão nas cordas",
            category = TuningCategory.DOWN_TUNED,
            strings = listOf(
                str(6, "D", "", 2, 38),
                str(5, "G", "", 2, 43),
                str(4, "C", "", 3, 48),
                str(3, "F", "", 3, 53),
                str(2, "A", "", 3, 57),
                str(1, "D", "", 4, 62)
            )
        ),
        TuningPreset(
            id = "drop_c",
            namePt = "Drop C",
            summaryNotes = "C  G  C  F  A  D",
            descriptionPt = "Graves profundos para rock moderno e metal",
            category = TuningCategory.DOWN_TUNED,
            strings = listOf(
                str(6, "C", "", 2, 36),
                str(5, "G", "", 2, 43),
                str(4, "C", "", 3, 48),
                str(3, "F", "", 3, 53),
                str(2, "A", "", 3, 57),
                str(1, "D", "", 4, 62)
            )
        ),
        TuningPreset(
            id = "open_g",
            namePt = "Open G",
            summaryNotes = "D  G  D  G  B  D",
            descriptionPt = "Acorde de Sol aberto ideal para blues e slide",
            category = TuningCategory.OPEN,
            strings = listOf(
                str(6, "D", "", 2, 38),
                str(5, "G", "", 2, 43),
                str(4, "D", "", 3, 50),
                str(3, "G", "", 3, 55),
                str(2, "B", "", 3, 59),
                str(1, "D", "", 4, 62)
            )
        ),
        TuningPreset(
            id = "open_d",
            namePt = "Open D",
            summaryNotes = "D  A  D  F# A  D",
            descriptionPt = "Acorde de Ré maior ressonante para fingerstyle",
            category = TuningCategory.OPEN,
            strings = listOf(
                str(6, "D", "", 2, 38),
                str(5, "A", "", 2, 45),
                str(4, "D", "", 3, 50),
                str(3, "F", "#", 3, 54),
                str(2, "A", "", 3, 57),
                str(1, "D", "", 4, 62)
            )
        ),
        TuningPreset(
            id = "dadgad",
            namePt = "DADGAD",
            summaryNotes = "D  A  D  G  A  D",
            descriptionPt = "Afinação suspensa modal para arranjos acústicos",
            category = TuningCategory.OPEN,
            strings = listOf(
                str(6, "D", "", 2, 38),
                str(5, "A", "", 2, 45),
                str(4, "D", "", 3, 50),
                str(3, "G", "", 3, 55),
                str(2, "A", "", 3, 57),
                str(1, "D", "", 4, 62)
            )
        ),
        TuningPreset(
            id = "chromatic",
            namePt = "Cromático",
            summaryNotes = "Todas as 12 notas",
            descriptionPt = "Detecta qualquer nota musical em todas as oitavas",
            category = TuningCategory.CHROMATIC,
            strings = standardStrings,
            isChromatic = true
        )
    )

    fun findById(id: String?): TuningPreset =
        presets.firstOrNull { it.id == id } ?: presets.first()
}

object PitchMath {

    private val chromaticNames = arrayOf(
        "C" to "", "C" to "#", "D" to "", "E" to "b",
        "E" to "", "F" to "", "F" to "#", "G" to "",
        "A" to "b", "A" to "", "B" to "b", "B" to ""
    )

    fun frequencyToCents(measuredHz: Double, targetHz: Double): Float {
        if (measuredHz <= 0.0 || targetHz <= 0.0) return 0f
        return (1200.0 * (ln(measuredHz / targetHz) / ln(2.0))).toFloat()
    }

    fun nearestChromaticNote(
        frequencyHz: Double,
        a4Hz: Double = 440.0
    ): DetectedNoteMatch {
        val midiFloat = 69.0 + 12.0 * (ln(frequencyHz / a4Hz) / ln(2.0))
        val midiRounded = midiFloat.roundToInt().coerceIn(21, 108)
        val noteIndex = ((midiRounded % 12) + 12) % 12
        val (name, acc) = chromaticNames[noteIndex]
        val octave = (midiRounded / 12) - 1
        val targetHz = a4Hz * 2.0.pow((midiRounded - 69) / 12.0)
        val cents = frequencyToCents(frequencyHz, targetHz).coerceIn(-50f, 50f)
        return DetectedNoteMatch(
            noteName = name,
            accidental = acc,
            octave = octave,
            targetFrequencyHz = targetHz,
            centsDeviation = cents,
            matchedString = null
        )
    }

    fun matchToPreset(
        frequencyHz: Double,
        preset: TuningPreset,
        lockedString: StringNote?
    ): DetectedNoteMatch {
        if (lockedString != null) {
            val cents = frequencyToCents(frequencyHz, lockedString.frequencyHz).coerceIn(-50f, 50f)
            return DetectedNoteMatch(
                noteName = lockedString.noteName,
                accidental = lockedString.accidental,
                octave = lockedString.octave,
                targetFrequencyHz = lockedString.frequencyHz,
                centsDeviation = cents,
                matchedString = lockedString
            )
        }

        if (preset.isChromatic) {
            return nearestChromaticNote(frequencyHz)
        }

        // Find closest string in cents (logarithmic pitch distance)
        var bestString = preset.strings.first()
        var bestCents = frequencyToCents(frequencyHz, bestString.frequencyHz)
        var minAbsCents = abs(bestCents)

        for (i in 1 until preset.strings.size) {
            val candidate = preset.strings[i]
            val cents = frequencyToCents(frequencyHz, candidate.frequencyHz)
            if (abs(cents) < minAbsCents) {
                minAbsCents = abs(cents)
                bestCents = cents
                bestString = candidate
            }
        }

        return DetectedNoteMatch(
            noteName = bestString.noteName,
            accidental = bestString.accidental,
            octave = bestString.octave,
            targetFrequencyHz = bestString.frequencyHz,
            centsDeviation = bestCents.coerceIn(-50f, 50f),
            matchedString = bestString
        )
    }

    fun computeRms(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0f
        var sum = 0.0
        for (s in samples) {
            sum += s * s
        }
        return sqrt(sum / samples.size).toFloat()
    }

    /**
     * YIN pitch detection algorithm with cumulative mean normalized difference
     * and parabolic interpolation for sub-cent precision down to ~52 Hz.
     */
    fun yinPitchDetect(
        samples: FloatArray,
        sampleRate: Int,
        minFreqHz: Double = 52.0,
        maxFreqHz: Double = 980.0,
        threshold: Float = 0.16f
    ): PitchDetectionResult? {
        val maxLag = (sampleRate / minFreqHz).toInt()
        val minLag = (sampleRate / maxFreqHz).toInt().coerceAtLeast(2)
        val integrationWindow = (samples.size - maxLag - 1).coerceAtMost(2048)

        if (integrationWindow <= maxLag || samples.size < maxLag + 64) {
            return null
        }

        val difference = FloatArray(maxLag + 1)
        for (tau in 1..maxLag) {
            var sum = 0f
            for (i in 0 until integrationWindow) {
                val delta = samples[i] - samples[i + tau]
                sum += delta * delta
            }
            difference[tau] = sum
        }

        val cmndf = FloatArray(maxLag + 1)
        cmndf[0] = 1f
        var runningSum = 0f
        for (tau in 1..maxLag) {
            runningSum += difference[tau]
            cmndf[tau] = if (runningSum > 1e-9f) {
                difference[tau] * tau / runningSum
            } else {
                1f
            }
        }

        // Find first lag below threshold, then descend to local minimum
        var tauEstimate = -1
        var tau = minLag
        while (tau < maxLag) {
            if (cmndf[tau] < threshold) {
                while (tau + 1 <= maxLag && cmndf[tau + 1] < cmndf[tau]) {
                    tau++
                }
                tauEstimate = tau
                break
            }
            tau++
        }

        // Fallback: accept global minimum only if reasonably periodic
        if (tauEstimate == -1) {
            var bestTau = minLag
            var bestVal = cmndf[minLag]
            for (t in (minLag + 1)..maxLag) {
                if (cmndf[t] < bestVal) {
                    bestVal = cmndf[t]
                    bestTau = t
                }
            }
            if (bestVal < 0.28f) {
                tauEstimate = bestTau
            } else {
                return null
            }
        }

        // Parabolic interpolation around tauEstimate
        val betterTau: Double = if (tauEstimate in 1 until maxLag) {
            val s0 = cmndf[tauEstimate - 1]
            val s1 = cmndf[tauEstimate]
            val s2 = cmndf[tauEstimate + 1]
            val denom = 2.0 * (2.0 * s1 - s2 - s0)
            if (abs(denom) > 1e-9) {
                tauEstimate + (s2 - s0) / denom
            } else {
                tauEstimate.toDouble()
            }
        } else {
            tauEstimate.toDouble()
        }

        if (betterTau <= 0.0) return null
        val freq = sampleRate / betterTau
        if (freq < minFreqHz || freq > maxFreqHz) return null

        val confidence = (1f - cmndf[tauEstimate]).coerceIn(0f, 1f)
        return PitchDetectionResult(frequencyHz = freq, confidence = confidence)
    }
}
