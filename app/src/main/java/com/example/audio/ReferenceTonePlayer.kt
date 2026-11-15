package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.domain.InstrumentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Synthesizes plucked guitar/acoustic reference tones using a damped
 * Karplus-Strong waveguide combined with warm fundamental reinforcement.
 */
class ReferenceTonePlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playJob: Job? = null
    private var activeTrack: AudioTrack? = null

    fun playTone(
        frequencyHz: Double,
        instrument: InstrumentType,
        onFinished: () -> Unit
    ) {
        stop()
        playJob = scope.launch {
            val sampleRate = 44100
            val durationSec = 1.9
            val totalSamples = (sampleRate * durationSec).toInt()
            val pcm = synthesizePluckedString(
                frequencyHz = frequencyHz,
                sampleRate = sampleRate,
                totalSamples = totalSamples,
                instrument = instrument
            )

            val byteSize = pcm.size * 2
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            if (track.state != AudioTrack.STATE_INITIALIZED) {
                track.release()
                onFinished()
                return@launch
            }

            synchronized(this@ReferenceTonePlayer) {
                activeTrack = track
            }

            try {
                track.write(pcm, 0, pcm.size)
                track.play()
                delay((durationSec * 1000).toLong())
            } finally {
                if (isActive) {
                    onFinished()
                }
                releaseTrack(track)
            }
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
        synchronized(this) {
            activeTrack?.let { releaseTrack(it) }
            activeTrack = null
        }
    }

    private fun releaseTrack(track: AudioTrack) {
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
        } catch (_: IllegalStateException) {
            // Ignore
        }
        try {
            track.release()
        } catch (_: Exception) {
            // Ignore
        }
    }

    private fun synthesizePluckedString(
        frequencyHz: Double,
        sampleRate: Int,
        totalSamples: Int,
        instrument: InstrumentType
    ): ShortArray {
        val period = (sampleRate / frequencyHz).roundToInt().coerceAtLeast(16)
        val delayLine = DoubleArray(period)
        val rng = Random(frequencyHz.toInt())

        // Initial excitation: shaped harmonic burst + soft pluck noise
        for (i in 0 until period) {
            val phase = 2.0 * PI * i / period
            val fundamental = sin(phase) * 0.65 + sin(2.0 * phase) * 0.22
            val pluckNoise = (rng.nextDouble() * 2.0 - 1.0) * 0.45
            delayLine[i] = fundamental + pluckNoise
        }

        val damping = if (instrument == InstrumentType.ACOUSTIC) 0.9965 else 0.9978
        val brightness = if (instrument == InstrumentType.ACOUSTIC) 0.46 else 0.56
        val output = ShortArray(totalSamples)
        var ptr = 0
        var prev = delayLine[period - 1]

        val fadeOutStart = (totalSamples * 0.78).toInt()
        val fadeOutLen = (totalSamples - fadeOutStart).coerceAtLeast(1)

        for (n in 0 until totalSamples) {
            val current = delayLine[ptr]
            val filtered = damping * (brightness * current + (1.0 - brightness) * prev)
            delayLine[ptr] = filtered
            prev = current

            ptr++
            if (ptr >= period) ptr = 0

            // Subtle fundamental body reinforcement for clean tuning clarity
            val t = n.toDouble() / sampleRate
            val bodySine = sin(2.0 * PI * frequencyHz * t) * exp(-1.6 * t) * 0.25

            // Smooth 10ms attack and natural tail fade
            val attack = if (n < 440) n / 440.0 else 1.0
            val release = if (n >= fadeOutStart) {
                1.0 - (n - fadeOutStart).toDouble() / fadeOutLen
            } else {
                1.0
            }

            val sample = ((filtered * 0.78 + bodySine) * attack * release).coerceIn(-0.95, 0.95)
            output[n] = (sample * Short.MAX_VALUE).toInt().toShort()
        }

        return output
    }
}
