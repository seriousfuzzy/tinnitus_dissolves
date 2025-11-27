package tinnitus.dissolves.tinnitus_dissolves.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import tinnitus.dissolves.tinnitus_dissolves.R
import tinnitus.dissolves.tinnitus_dissolves.model.MusicData

@OptIn(UnstableApi::class)
class ExoLoopPlayer
    (
    context: Context,
    data: MusicData
) {
    companion object {
        fun create(context: Context, data: MusicData): ExoLoopPlayer {
            return ExoLoopPlayer(context, data)
        }

        private const val TAG = "ExoLoopPlayer"
    }

    private val player1 = ExoPlayer.Builder(context).build()
    private val player2 = ExoPlayer.Builder(context).build()

    private var isPlaying = false
    private var current = 0
    private val handler = Handler(Looper.getMainLooper())

    private var volume = 0f
    private var pan = 0f
    private var leftVolume = 0f
    private var rightVolume = 0f

    private var duration = mutableListOf(2250L, 2250L)

    init {
        try {
            val sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.app_name), Context.MODE_PRIVATE
            )
            val interval = sharedPreferences.getLong("debug", 2250L)

            val uri1 = "android.resource://${context.packageName}/${data.soundId1}".toUri()
//            val uri2 = "android.resource://${context.packageName}/${data.soundId2}".toUri()

            player1.setMediaItem(MediaItem.fromUri(uri1))
//            player2.setMediaItem(MediaItem.fromUri(uri2))

            player1.prepare()
            player2.prepare()

            player1.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        duration[0] = player1.duration.takeIf { it > 0 }?.minus(interval) ?: 3000L
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("@@@test", "Player1 Error: ${error.message}", error)
                }
            })

            player2.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        duration[1] = player2.duration.takeIf { it > 0 }?.minus(interval) ?: 3000L
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("@@@test", "Player2 Error: ${error.message}", error)
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing players", e)
        }
    }

    fun start() {
        if (!isPlaying) {
            isPlaying = true

            // 音量とバランスを再設定する（追加）
            setVolume(this.volume, this.pan)

            player1.seekTo(0)
            player2.seekTo(0)

            player1.play()
            player2.play()

            Log.d(TAG, "Playback started (volume=$volume)")

            playCurrent()
        }
    }

    fun stop() {
        isPlaying = false
        handler.removeCallbacks(runnable)

        player1.pause()
        player2.pause()
        player1.seekTo(0)
        player2.seekTo(0)

        Log.d(TAG, "Playback stopped and reset")
    }

    fun setVolume(volume: Float, pan: Float) {
        this.leftVolume = volume
        this.rightVolume = volume
        this.pan = pan

        if (pan < 0f) rightVolume *= 1 - kotlin.math.abs(pan)
        else if (pan > 0f) leftVolume *= 1 - kotlin.math.abs(pan)

        this.volume = volume

        player1.volume = leftVolume
        player2.volume = rightVolume

        Log.d(TAG, "Volume set: L=$leftVolume, R=$rightVolume")
    }

    fun getVolume(): Float = volume

    private fun playCurrent() {
        if (!isPlaying) return

        if (current == 0) {
            player1.seekTo(0)
            player1.play()
        } else {
            player2.seekTo(0)
            player2.play()
        }

        handler.postDelayed(runnable, duration[current])
    }

    private val runnable = Runnable {
        if (!isPlaying) return@Runnable
        current = if (current == 0) 1 else 0
        playCurrent()
    }

    fun release() {
//        player1.release()
//        player2.release()
    }
}
