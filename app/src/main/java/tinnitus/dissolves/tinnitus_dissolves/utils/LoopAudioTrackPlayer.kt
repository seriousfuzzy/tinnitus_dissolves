package tinnitus.dissolves.tinnitus_dissolves.utils

import android.content.Context
import android.media.*
import android.util.Log
import tinnitus.dissolves.tinnitus_dissolves.model.MusicData
import java.io.IOException

class LoopAudioTrackPlayer(
    private val context: Context,
    private val data: MusicData
) {
    companion object {
        private const val TAG = "LoopAudioTrackPlayer"
    }

    private var audioTrack: AudioTrack? = null
    private var audioData: ByteArray? = null
    private var volume: Float = 1.0f
    private var pan: Float = 0.0f
    private var isPrepared = false

    fun load() {
        try {
            val afd = context.resources.openRawResourceFd(data.soundId1)
            val inputStream = afd.createInputStream()
            val fullBytes = inputStream.readBytes()
            inputStream.close()
            afd.close()

            Log.d(TAG, "fullBytes size: ${fullBytes.size}")
            if (fullBytes.size < 44) throw IOException("Invalid WAV header")

            // WAVヘッダーから動的に抽出
            val sampleRate = extractIntLE(fullBytes, 24)
            val channels = extractShortLE(fullBytes, 22).toInt()
            val bitsPerSample = extractShortLE(fullBytes, 34).toInt()
            val encoding = when (bitsPerSample) {
                8 -> AudioFormat.ENCODING_PCM_8BIT
                16 -> AudioFormat.ENCODING_PCM_16BIT
                else -> throw IOException("Unsupported bits per sample: $bitsPerSample")
            }
            val channelMask = when (channels) {
                1 -> AudioFormat.CHANNEL_OUT_MONO
                2 -> AudioFormat.CHANNEL_OUT_STEREO
                else -> throw IOException("Unsupported channels: $channels")
            }

            val raw = fullBytes.copyOfRange(44, fullBytes.size)
            val bytesPerFrame = bitsPerSample / 8 * channels
            val alignedSize = raw.size - (raw.size % bytesPerFrame)
            audioData = raw.copyOf(alignedSize)

            Log.d(TAG, "audioData size (aligned): ${audioData?.size}")

            val frameCount = alignedSize / bytesPerFrame

            audioTrack = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelMask)
                    .build(),
                alignedSize,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            val result = audioTrack?.write(audioData!!, 0, alignedSize)
            Log.d(TAG, "AudioTrack.write() = $result")

            audioTrack?.setLoopPoints(0, frameCount, -1)

            isPrepared = true
            Log.d(TAG, "AudioTrack loaded successfully (rate=$sampleRate Hz, ch=$channels, bit=$bitsPerSample)")

        } catch (e: Exception) {
            Log.e(TAG, "Error loading audio", e)
        }
    }

    fun start() {
        if (!isPrepared) load()
        audioTrack?.apply {
            setVolume(maxOf(calcLeft(), calcRight()))
            Log.d(TAG, "volume = $volume, pan = $pan, L = ${calcLeft()}, R = ${calcRight()}")
            play()
            Log.d(TAG, "AudioTrack started")
        }
    }

    fun stop() {
        audioTrack?.apply {
            stop()
            release()
        }
        audioTrack = null
        isPrepared = false
        Log.d(TAG, "AudioTrack stopped and released")
    }

    fun setVolume(volume: Float, pan: Float) {
        this.volume = volume
        this.pan = pan
        audioTrack?.setVolume(maxOf(calcLeft(), calcRight()))
        Log.d(TAG, "volume = $volume, pan = $pan, L = ${calcLeft()}, R = ${calcRight()}")
        if (volume <= 0f) stop()
        else if (audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) start()
    }

    fun getVolume(): Float = volume

    private fun calcLeft(): Float = when {
        pan > 0f -> volume * (1f - pan.coerceIn(0f, 1f))
        else -> volume
    }

    private fun calcRight(): Float = when {
        pan < 0f -> volume * (1f + pan.coerceIn(-1f, 0f))
        else -> volume
    }

    // Little Endian 変換ユーティリティ
    private fun extractIntLE(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset + 3].toInt() and 0xFF shl 24) or
                (bytes[offset + 2].toInt() and 0xFF shl 16) or
                (bytes[offset + 1].toInt() and 0xFF shl 8) or
                (bytes[offset].toInt() and 0xFF)
    }

    private fun extractShortLE(bytes: ByteArray, offset: Int): Short {
        return ((bytes[offset + 1].toInt() and 0xFF shl 8) or
                (bytes[offset].toInt() and 0xFF)).toShort()
    }
}
