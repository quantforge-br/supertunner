package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.domain.PitchDetectionResult
import com.example.domain.PitchMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.math.abs

data class AudioAnalysisFrame(
    val pitch: PitchDetectionResult?,
    val rmsLevel: Float,
    val waveformBars: List<Float>
)

class AudioPitchEngine(
    private val sampleRate: Int = 44100,
    private val windowSize: Int = 3072,
    private val hopSize: Int = 1024
) {

    @SuppressLint("MissingPermission")
    fun observeAudioFrames(): Flow<AudioAnalysisFrame> = flow {
        val minBufferBytes = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(hopSize * 4)

        val bufferBytes = (minBufferBytes * 2).coerceAtLeast(windowSize * 2)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferBytes
        )

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return@flow
        }

        val readBuffer = ShortArray(hopSize)
        val rollingWindow = FloatArray(windowSize)
        var filled = 0

        try {
            recorder.startRecording()
            while (currentCoroutineContext().isActive) {
                val readCount = recorder.read(readBuffer, 0, hopSize)
                if (readCount > 0) {
                    // Shift rolling window left and append normalized samples
                    val shift = readCount.coerceAtMost(windowSize)
                    if (shift < windowSize) {
                        System.arraycopy(
                            rollingWindow,
                            shift,
                            rollingWindow,
                            0,
                            windowSize - shift
                        )
                    }
                    val destStart = windowSize - shift
                    val srcStart = readCount - shift
                    for (i in 0 until shift) {
                        rollingWindow[destStart + i] = readBuffer[srcStart + i] / 32768f
                    }
                    filled = (filled + readCount).coerceAtMost(windowSize)

                    if (filled >= windowSize) {
                        val rms = PitchMath.computeRms(rollingWindow)
                        val bars = buildWaveformBars(rollingWindow, barCount = 28)
                        val pitch = if (rms >= NOISE_GATE_RMS) {
                            PitchMath.yinPitchDetect(
                                samples = rollingWindow,
                                sampleRate = sampleRate,
                                minFreqHz = 52.0,
                                maxFreqHz = 980.0,
                                threshold = 0.16f
                            )?.takeIf { it.confidence >= 0.72f }
                        } else {
                            null
                        }
                        emit(
                            AudioAnalysisFrame(
                                pitch = pitch,
                                rmsLevel = (rms * 7.5f).coerceIn(0f, 1f),
                                waveformBars = bars
                            )
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
            // Permission revoked mid-session
        } finally {
            try {
                if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.stop()
                }
            } catch (_: IllegalStateException) {
                // Ignore if already stopped
            }
            recorder.release()
        }
    }.flowOn(Dispatchers.Default)

    private fun buildWaveformBars(samples: FloatArray, barCount: Int): List<Float> {
        val recentSliceStart = (samples.size - 1024).coerceAtLeast(0)
        val sliceLen = samples.size - recentSliceStart
        val chunkSize = (sliceLen / barCount).coerceAtLeast(1)
        return List(barCount) { index ->
            val start = recentSliceStart + index * chunkSize
            val end = (start + chunkSize).coerceAtMost(samples.size)
            var peak = 0f
            for (i in start until end) {
                val v = abs(samples[i])
                if (v > peak) peak = v
            }
            (peak * 4.2f).coerceIn(0.06f, 1f)
        }
    }

    companion object {
        private const val NOISE_GATE_RMS = 0.008f
    }
}
