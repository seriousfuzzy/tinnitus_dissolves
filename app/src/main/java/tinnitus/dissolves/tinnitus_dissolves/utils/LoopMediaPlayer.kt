package tinnitus.dissolves.tinnitus_dissolves.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

class LoopMediaPlayer(private val context: Context, private val resId: Int) {

    companion object {
        fun create(context: Context, resId: Int): LoopMediaPlayer = LoopMediaPlayer(context, resId)
        
        private const val TAG = "LoopMediaPlayer"
        private const val FADE_DURATION_MS = 100L // フェードイン・フェードアウトの時間（ミリ秒）- 少し長めにして滑らかに
        private const val FADE_STEPS = 20 // フェードのステップ数（増やしてより滑らかに）
        private const val FADE_INTERVAL_MS = FADE_DURATION_MS / FADE_STEPS // 各ステップの間隔
        private const val CROSSFADE_START_MS = 150L // 次のプレイヤーを開始するタイミング（終了前）- 少し早めに開始
    }

    private var player1: MediaPlayer? = null
    private var player2: MediaPlayer? = null
    private var currentPlayer: MediaPlayer? = null
    private var nextPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var duration: Int = 0
    
    private var volume: Float = 0f
    private var pan: Float = 0f
    private var leftVolume: Float = 0f
    private var rightVolume: Float = 0f
    
    private val handler = Handler(Looper.getMainLooper())
    private var fadeRunnable: Runnable? = null
    private var loopRunnable: Runnable? = null

    init {
        try {
            // 2つのMediaPlayerを初期化
            player1 = createMediaPlayer()
            player2 = createMediaPlayer()
            
            // 最初のプレイヤーを現在のプレイヤーに設定
            currentPlayer = player1
            nextPlayer = player2
            
            // 音源の長さを取得
            duration = player1?.duration ?: 0
            if (duration <= 0) {
                // 取得できない場合はデフォルト値を使用（後で実際の値を取得）
                duration = 3000
            }
            
            // 完了リスナーを設定してループを実現
            setupCompletionListener(player1, player2)
            setupCompletionListener(player2, player1)
            
        } catch (e: Throwable) {
            Log.e(TAG, "Error initializing LoopMediaPlayer", e)
            // フォールバック: 単一のMediaPlayerでisLoopingを使用
            try {
                player1 = MediaPlayer.create(context, resId)?.apply {
                    isLooping = true
                    setVolume(leftVolume, rightVolume)
                }
                currentPlayer = player1
            } catch (e2: Throwable) {
                Log.e(TAG, "Fallback initialization failed", e2)
            }
        }
    }
    
    private fun createMediaPlayer(): MediaPlayer? {
        return try {
            MediaPlayer().apply {
                // Android 14以降では最適なAudioAttributesを設定
                // FLAG_LOW_LATENCYは使用しない（ノイズの原因になる可能性があるため）
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    // Android 14以降では追加のフラグを設定しない（標準設定を使用）
                    .build()
                setAudioAttributes(audioAttributes)

                // リソースを設定
                val afd = context.resources.openRawResourceFd(resId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()

                prepare()
                
                // isLoopingは使用しない（クロスフェード方式でループを実現）
                // これにより、Android 14でのループ時のノイズを回避
                setVolume(0f, 0f) // 初期音量は0
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error creating MediaPlayer", e)
            null
        }
    }
    
    private fun setupCompletionListener(player: MediaPlayer?, otherPlayer: MediaPlayer?) {
        // OnCompletionListenerは使用しない（タイマーベースで制御）
    }
    
    private fun crossFade(fadeOut: MediaPlayer?, fadeIn: MediaPlayer?) {
        if (fadeOut == null || fadeIn == null) return
        
        try {
            // フェードイン用のプレイヤーを開始（音量0で）
            fadeIn.setVolume(0f, 0f)
            fadeIn.start()
            
            // フェードアウト用のプレイヤーを停止（フェードアウト後に）
            var step = 0
            
            fadeRunnable?.let { handler.removeCallbacks(it) }
            fadeRunnable = object : Runnable {
                override fun run() {
                    if (!isPlaying || step >= FADE_STEPS) {
                        // フェード完了
                        try {
                            fadeOut.pause()
                            fadeOut.seekTo(0)
                        } catch (_: Throwable) {}
                        fadeRunnable = null
                        return
                    }
                    
                    val progress = step.toFloat() / FADE_STEPS
                    val fadeOutVolume = leftVolume * (1f - progress)
                    val fadeInVolume = leftVolume * progress
                    
                    try {
                        fadeOut.setVolume(fadeOutVolume, rightVolume * (1f - progress))
                        fadeIn.setVolume(fadeInVolume, rightVolume * progress)
                    } catch (_: Throwable) {}
                    
                    step++
                    handler.postDelayed(this, FADE_INTERVAL_MS)
                }
            }
            handler.post(fadeRunnable!!)
            
        } catch (e: Throwable) {
            Log.e(TAG, "Error in crossFade", e)
        }
    }

    fun start() {
        if (isPlaying) return
        if (leftVolume <= 0f && rightVolume <= 0f) return
        
        val current = currentPlayer ?: return
        isPlaying = true
        
        try {
            // 現在のプレイヤーを開始位置にシーク
            current.seekTo(0)
            
            // 音源の長さを取得（再生開始前に取得を試みる）
            if (duration <= 0 || duration == 3000) {
                val actualDuration = current.duration
                if (actualDuration > 0) {
                    duration = actualDuration
                    Log.d(TAG, "Duration set to: $duration ms")
                } else {
                    // まだ取得できない場合は、再生開始後に再試行
                    handler.postDelayed({
                        if (isPlaying && current.isPlaying) {
                            val d = current.duration
                            if (d > 0) {
                                duration = d
                                Log.d(TAG, "Duration set to (delayed): $duration ms")
                                // ループを再スケジュール
                                scheduleNextLoop()
                            }
                        }
                    }, 100)
                }
            }
            
            // 音量を設定して開始
            current.setVolume(leftVolume, rightVolume)
            current.start()
            
            // ループ用のタイマーを設定（クロスフェード方式）
            if (duration > 0) {
                scheduleNextLoop()
            } else {
                // durationが取得できない場合は、少し待ってから再試行
                handler.postDelayed({
                    if (isPlaying) {
                        val d = current.duration
                        if (d > 0) {
                            duration = d
                            scheduleNextLoop()
                        }
                    }
                }, 200)
            }
            
        } catch (e: Throwable) {
            Log.e(TAG, "Error starting playback", e)
            isPlaying = false
        }
    }
    
    private fun scheduleNextLoop() {
        if (!isPlaying) return
        
        loopRunnable?.let { handler.removeCallbacks(it) }
        
        val current = currentPlayer ?: return
        val next = nextPlayer ?: return
        
        if (duration <= 0) {
            // durationが取得できていない場合は、少し待ってから再試行
            handler.postDelayed({
                if (isPlaying) scheduleNextLoop()
            }, 100)
            return
        }
        
        // 定期的にチェックする方法（より確実）
        val checkInterval = 50L // 50msごとにチェック
        
        loopRunnable = object : Runnable {
            override fun run() {
                if (!isPlaying) return
                
                try {
                    val current = currentPlayer ?: return
                    val next = nextPlayer ?: return
                    
                    if (!current.isPlaying) {
                        // 再生が停止している場合は、再スケジュール
                        handler.postDelayed(this, checkInterval)
                        return
                    }
                    
                    val pos = current.currentPosition
                    val threshold = duration - CROSSFADE_START_MS
                    
                    if (pos >= threshold || pos < 0) {
                        // 切り替えタイミング
                        next.seekTo(0)
                        crossFade(current, next)
                        
                        // プレイヤーを入れ替え
                        val temp = currentPlayer
                        currentPlayer = nextPlayer
                        nextPlayer = temp
                        
                        // 次のループをスケジュール
                        scheduleNextLoop()
                    } else {
                        // まだ早いので、続けてチェック
                        handler.postDelayed(this, checkInterval)
                    }
                    
                } catch (e: Throwable) {
                    Log.e(TAG, "Error in loop check", e)
                    // エラーが発生した場合は、少し待ってから再試行
                    handler.postDelayed({
                        if (isPlaying) scheduleNextLoop()
                    }, 100)
                }
            }
        }
        
        // 最初のチェックを開始
        handler.postDelayed(loopRunnable!!, checkInterval)
    }

    fun stop() {
        isPlaying = false
        
        // すべてのRunnableをキャンセル
        fadeRunnable?.let { handler.removeCallbacks(it) }
        fadeRunnable = null
        loopRunnable?.let { handler.removeCallbacks(it) }
        loopRunnable = null
        
        try {
            player1?.let {
                if (it.isPlaying) it.pause()
                it.seekTo(0)
            }
            player2?.let {
                if (it.isPlaying) it.pause()
                it.seekTo(0)
            }
        } catch (_: Throwable) {}
    }

    fun release() {
        isPlaying = false
        
        // すべてのRunnableをキャンセル
        fadeRunnable?.let { handler.removeCallbacks(it) }
        fadeRunnable = null
        loopRunnable?.let { handler.removeCallbacks(it) }
        loopRunnable = null
        
        try {
            player1?.release()
            player2?.release()
        } catch (_: Throwable) {}
        
        player1 = null
        player2 = null
        currentPlayer = null
        nextPlayer = null
    }

    fun getVolume(): Float = volume

    fun seekToStart() {
        try {
            currentPlayer?.seekTo(0)
            nextPlayer?.seekTo(0)
        } catch (_: Throwable) {}
    }

    fun setVolume(volume: Float, pan: Float) {
        this.pan = pan
        this.volume = volume

        var l = volume
        var r = volume
        if (pan < 0f) r *= 1 - kotlin.math.abs(pan) else if (pan > 0f) l *= 1 - kotlin.math.abs(pan)
        leftVolume = l
        rightVolume = r

        try {
            // 現在再生中のプレイヤーの音量を更新
            currentPlayer?.setVolume(leftVolume, rightVolume)
            
            // 再生中でない場合は、両方のプレイヤーの音量を設定（次回再生時に使用）
            if (!isPlaying) {
                player1?.setVolume(leftVolume, rightVolume)
                player2?.setVolume(leftVolume, rightVolume)
            }
        } catch (_: Throwable) {}
    }
}
