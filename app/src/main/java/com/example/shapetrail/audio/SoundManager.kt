package com.example.shapetrail.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

class SoundManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var isEnabled: Boolean = true

    enum class SoundEffect {
        COUNTDOWN_TICK,
        COUNTDOWN_GO,
        CORRECT_TAP,
        ROUND_COMPLETE,
        STREAK_MILESTONE,
        WRONG_TAP,
        TIMER_TICK,
        GAME_OVER,
        BUTTON_CLICK
    }

    fun play(effect: SoundEffect, stepIndex: Int = 0) {
        if (!isEnabled) return
        scope.launch {
            try {
                val sampleRate = 22050
                val samples = when (effect) {
                    SoundEffect.COUNTDOWN_TICK -> generateTone(sampleRate, 440.0, 0.08f, 0.35f)
                    SoundEffect.COUNTDOWN_GO -> generateTone(sampleRate, 880.0, 0.22f, 0.45f)
                    SoundEffect.CORRECT_TAP -> {
                        val baseFreq = 440.0
                        // Ascending pitch scale based on step index
                        val scale = doubleArrayOf(1.0, 1.125, 1.25, 1.333, 1.5, 1.667, 1.875, 2.0, 2.25, 2.5, 2.667, 3.0)
                        val multiplier = scale.getOrElse(stepIndex) { 2.0 }
                        generateBell(sampleRate, baseFreq * multiplier, 0.16f, 0.4f)
                    }
                    SoundEffect.ROUND_COMPLETE -> generateArpeggio(sampleRate, doubleArrayOf(523.25, 659.25, 783.99, 1046.50), 0.35f)
                    SoundEffect.STREAK_MILESTONE -> generateFanfare(sampleRate)
                    SoundEffect.WRONG_TAP -> generateBuzzer(sampleRate, 140.0, 155.0, 0.28f)
                    SoundEffect.TIMER_TICK -> generateTone(sampleRate, 800.0, 0.035f, 0.25f)
                    SoundEffect.GAME_OVER -> generateDescending(sampleRate, doubleArrayOf(392.0, 329.63, 261.63), 0.45f)
                    SoundEffect.BUTTON_CLICK -> generateTone(sampleRate, 620.0, 0.03f, 0.2f)
                }
                playPcm(samples, sampleRate)
            } catch (_: Exception) {
                // Audio synthesis fallback: quiet failure without crashing
            }
        }
    }

    private fun generateTone(sampleRate: Int, freq: Double, durationSec: Float, peakAmp: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = (1.0 - (i.toDouble() / totalSamples))
            val wave = sin(2.0 * Math.PI * freq * t)
            buffer[i] = (wave * envelope * peakAmp * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun generateBell(sampleRate: Int, freq: Double, durationSec: Float, peakAmp: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 18.0)
            val fundamental = sin(2.0 * Math.PI * freq * t)
            val harmonic = 0.3 * sin(2.0 * Math.PI * (freq * 2.0) * t)
            val wave = fundamental + harmonic
            buffer[i] = (wave * decay * peakAmp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateArpeggio(sampleRate: Int, freqs: DoubleArray, totalDurationSec: Float): ShortArray {
        val noteDuration = totalDurationSec / freqs.size
        val noteSamples = (sampleRate * noteDuration).toInt()
        val totalSamples = noteSamples * freqs.size
        val buffer = ShortArray(totalSamples)

        for (n in freqs.indices) {
            val freq = freqs[n]
            val offset = n * noteSamples
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 9.0)
                val wave = sin(2.0 * Math.PI * freq * t) + 0.25 * sin(4.0 * Math.PI * freq * t)
                val idx = offset + i
                if (idx < totalSamples) {
                    buffer[idx] = (wave * decay * 0.45 * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    private fun generateFanfare(sampleRate: Int): ShortArray {
        return generateArpeggio(sampleRate, doubleArrayOf(440.0, 554.37, 659.25, 880.0, 1108.73), 0.45f)
    }

    private fun generateDescending(sampleRate: Int, freqs: DoubleArray, totalDurationSec: Float): ShortArray {
        return generateArpeggio(sampleRate, freqs, totalDurationSec)
    }

    private fun generateBuzzer(sampleRate: Int, freq1: Double, freq2: Double, durationSec: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = (1.0 - (i.toDouble() / totalSamples))
            val wave = 0.5 * sin(2.0 * Math.PI * freq1 * t) + 0.5 * sin(2.0 * Math.PI * freq2 * t)
            buffer[i] = (wave * decay * 0.5 * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun playPcm(samples: ShortArray, sampleRate: Int) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.play()
        // Release once playback completes
        scope.launch {
            val durationMs = (samples.size * 1000L) / sampleRate + 50L
            kotlinx.coroutines.delay(durationMs)
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }
}
