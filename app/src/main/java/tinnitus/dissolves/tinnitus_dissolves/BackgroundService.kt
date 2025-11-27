package tinnitus.dissolves.tinnitus_dissolves

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaMetadata
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.media.session.MediaButtonReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tinnitus.dissolves.tinnitus_dissolves.model.SliderModel
import tinnitus.dissolves.tinnitus_dissolves.utils.LoopMediaPlayer
import tinnitus.dissolves.tinnitus_dissolves.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Timer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.schedule

class BackgroundService : Service() {

    companion object {
        var mediaPlayerList: List<LoopMediaPlayer> = emptyList()
        val message = MutableLiveData<String>()
        val isPlaying = MutableLiveData<Boolean>()
        val isService = MutableLiveData<Boolean>()
        val showAdRequest = MutableLiveData<Boolean>()

        private const val TAG = "BackgroundService"
        private const val ACTION_TIMER_UPDATE = "TIMER_UPDATE"
        private const val ACTION_RESET_LEVELS = "RESET_LEVELS"
        private const val ACTION_LOAD_PRESET_AND_PLAY = "LOAD_PRESET_AND_PLAY"
        private const val ACTION_STOP = "STOP"
        private const val NOTIF_ID = 1
        private const val CHANNEL_ID = "service"
    }

    private var timer: Timer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var debounce: Long = 0
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val startSeq = AtomicInteger(0)        // 起動世代を単調増加で管理
    private var prepareJob: Job? = null            // 直近の準備ジョブ
    private val isStopping = AtomicBoolean(false)  // STOP進行中フラグ

    override fun onBind(intent: Intent?): IBinder? = null

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() = startPlayback()
        override fun onPause() = pausePlayback()
        override fun onStop() = stopSelfSafe()
    }

    private fun log(msg: String) {
        Log.d(
            TAG,
            "[${Thread.currentThread().name}] [seq=${startSeq.get()}] [stopping=${isStopping.get()}] $msg"
        )
    }

    private fun logAbort(where: String, reason: String) {
        Log.d(TAG, "ABORT @$where: $reason")
    }

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        isService.postValue(true)
        ensureNotificationChannel()
        setupMediaSession()
        isPlaying.postValue(false)
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        log("onStartCommand: action=${intent.action} extras=${intent.extras?.keySet()} isPlaying=${isPlaying.value} isService=${isService.value}")
        isService.postValue(true)
        ensureNotificationChannel()
        setupMediaSession()

        when {
            intent.action == ACTION_TIMER_UPDATE -> {
                handleTimerUpdate(intent)
            }

            intent.action == ACTION_RESET_LEVELS -> {
                handleResetLevels()
            }

            intent.action == ACTION_STOP -> {
                stopSelfSafe()
            }

            intent.action == Intent.ACTION_MEDIA_BUTTON && intent.hasExtra(Intent.EXTRA_KEY_EVENT) -> {
                handleMediaButtonIntent(intent)
            }

            else -> {
                if (intent.action == ACTION_LOAD_PRESET_AND_PLAY) {
                    handleLoadPresetAndPlay(intent)
                } else {
                    handleDefaultStart(intent)
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundPreparing() {
        log("startForegroundPreparing()")
        val n = createNotification(true) // 再生前の通知
        try {
            startForeground(NOTIF_ID, n)
        } catch (_: Throwable) {
        }
    }

    private fun setupMediaSession() {
        if (::mediaSession.isInitialized) return

        val session = MediaSessionCompat(this, "service").apply {
            setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, getString(R.string.app_name))
                    .build()
            )
            setCallback(callback)
            isActive = true

            val initState = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_STOP
                )
                .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1f)
                .build()
            setPlaybackState(initState)
        }
        mediaSession = session
    }

    private fun updatePlaybackState(playing: Boolean) {
        if (!::mediaSession.isInitialized) return
        val state = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(
                if (playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                0L, 1f
            )
            .build()
        mediaSession.setPlaybackState(state)
    }

    private fun ensureNotificationChannel() {
        val mgr = getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
            val ch = NotificationChannel(
                CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW
            )
            mgr.createNotificationChannel(ch)
        }
    }

    private fun createNotification(isPlaying: Boolean): Notification {
        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowActionsInCompactView(0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setStyle(style)
            .setOngoing(isPlaying)

        val action = if (isPlaying) {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PAUSE
                )
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this,
                    PlaybackStateCompat.ACTION_PLAY
                )
            ).build()
        }
        builder.addAction(action)
        return builder.build()
    }

    private fun notifyOrForeground(isPlayingNow: Boolean) {
        val nm = getSystemService(NotificationManager::class.java)
        val n = createNotification(isPlayingNow)
        if (isPlayingNow) {
            startForeground(NOTIF_ID, n)
        } else {
            try {
                stopForeground(false)
            } catch (_: Throwable) {
            }
            nm.notify(NOTIF_ID, n)
        }
    }

    private fun handleResetLevels() {
        log("handleResetLevels()")
        mediaPlayerList.forEach { player -> runCatching { player.setVolume(0f, 0f) } }
    }

    private fun handleTimerUpdate(intent: Intent) {
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)
        log("handleTimerUpdate(): hour=$hour minute=$minute")
        startTimer(hour, minute)
    }

    private fun handleMediaButtonIntent(intent: Intent) {
        val time = System.currentTimeMillis()
        if (debounce != 0L && time - debounce < 300L) return
        debounce = time

        val keyEvent: KeyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) ?: return
        if (keyEvent.action != KeyEvent.ACTION_DOWN) return

        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ->
                if (isPlaying.value != true) startPlayback()

            KeyEvent.KEYCODE_MEDIA_PAUSE ->
                if (isPlaying.value == true) pausePlayback()

            KeyEvent.KEYCODE_MEDIA_STOP ->
                stopSelfSafe()
        }
    }

    /** 設定プリセットを読んで再生 */
    private fun handleLoadPresetAndPlay(intent: Intent) {
        startForegroundPreparing()

        val seq = startSeq.incrementAndGet()
        isStopping.set(false)
        prepareJob?.cancel()

        val key = intent.getStringExtra("presetKey") ?: run {
            logAbort(
                "handleLoadPresetAndPlay",
                "no presetKey"
            ); return
        }
        val prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val json = prefs.getString(key, null) ?: run {
            logAbort(
                "handleLoadPresetAndPlay",
                "no json for key=$key"
            ); return
        }

        log("handleLoadPresetAndPlay(): seq=$seq key=$key")

        prepareJob = scope.launch(Dispatchers.IO) {
            log("IO: preset prepare start seq=$seq")

            val listData: List<SliderModel> = try {
                Gson().fromJson(json, object : TypeToken<List<SliderModel>>() {}.type)
            } catch (_: Exception) { emptyList() }

            val builtPlayers: List<LoopMediaPlayer> = listData.map { model ->
                LoopMediaPlayer.create(this@BackgroundService, model.data.soundId1).apply {
                    setVolume(model.volume, model.pan)
                }
            }
            log("IO: preset prepare done players=${builtPlayers.size}")

            var adopted = false
            try {
                if (isStopping.get() || seq != startSeq.get() || !isActive) {
                    builtPlayers.forEach { runCatching { it.stop(); it.release() } }
                    logAbort("IO/preset", "discard & release ${builtPlayers.size}")
                    return@launch
                }

                log("IO: apply preset players (old=${mediaPlayerList.size} -> new=${builtPlayers.size})")
                releaseAllPlayers()
                mediaPlayerList = builtPlayers
                adopted = true

                val hour = intent.getIntExtra("hour", 0)
                val minute = intent.getIntExtra("minute", 0)
                log("IO: switching to Main (preset) seq=$seq hour=$hour minute=$minute")

                withContext(Dispatchers.Main) {
                    if (isStopping.get() || seq != startSeq.get()) { logAbort("Main/preset", "guard"); return@withContext }
                    startPlayback()
                    startTimer(hour, minute)
                }
            } finally {
                if (!adopted) {
                    builtPlayers.forEach { runCatching { it.stop(); it.release() } }
                }
            }
        }
    }

    private fun handleDefaultStart(intent: Intent) {
        startForegroundPreparing()

        // 新しい起動世代
        val seq = startSeq.incrementAndGet()
        isStopping.set(false)       // 新規起動なのでSTOP中フラグは下ろす
        prepareJob?.cancel()        // 旧ジョブは必ず止める

        val resIds = intent.getIntArrayExtra("resIds")
        val volumes = intent.getFloatArrayExtra("volumes")
        val pans = intent.getFloatArrayExtra("pans")
        val data = intent.getStringExtra("data")

        log("handleDefaultStart(): seq=$seq payload arrays(res=${resIds?.size}, vol=${volumes?.size}, pan=${pans?.size}) json=${!data.isNullOrEmpty()}")

        prepareJob = scope.launch(Dispatchers.IO) {
            // …プレイヤー生成（配列優先 / JSONフォールバック）
            val builtPlayers: List<LoopMediaPlayer> = if (resIds != null && volumes != null && pans != null) {
                resIds.mapIndexed { i, resId ->
                    val v = volumes.getOrNull(i) ?: 0f
                    val p = pans.getOrNull(i) ?: 0f
                    LoopMediaPlayer.create(this@BackgroundService, resId).apply { setVolume(v, p) }
                }
            } else {
                val list = if (!data.isNullOrEmpty())
                    Gson().fromJson<List<SliderModel>>(data, object : TypeToken<List<SliderModel>>() {}.type)
                else emptyList()
                list.map { m ->
                    LoopMediaPlayer.create(this@BackgroundService, m.data.soundId1).apply {
                        setVolume(m.volume, m.pan)
                    }
                }
            }

            log("IO: prepare done players=${builtPlayers.size}")

            var adopted = false
            try {
                // ★ STOP中/世代ズレ/キャンセル → 採用せず全解放して終了
                if (isStopping.get() || seq != startSeq.get() || !isActive) {
                    builtPlayers.forEach { runCatching { it.stop(); it.release() } }
                    logAbort("IO/prepare", "discard & release ${builtPlayers.size}")
                    return@launch
                }

                log("IO: apply players (old=${mediaPlayerList.size} -> new=${builtPlayers.size})")
                releaseAllPlayers()
                mediaPlayerList = builtPlayers
                adopted = true

                val hour = intent.getIntExtra("hour", 0)
                val minute = intent.getIntExtra("minute", 0)
                log("IO: switching to Main seq=$seq hour=$hour minute=$minute")

                withContext(Dispatchers.Main) {
                    if (isStopping.get() || seq != startSeq.get()) { logAbort("Main/apply", "guard"); return@withContext }
                    startPlayback()
                    startTimer(hour, minute)
                }
            } finally {
                // ★ 例外/キャンセルで採用に至らなかったらリーク防止
                if (!adopted) {
                    builtPlayers.forEach { runCatching { it.stop(); it.release() } }
                }
            }
        }
    }

    private fun startPlayback() {
        if (isStopping.get()) {
            logAbort("startPlayback", "isStopping"); return
        }
        if (mediaPlayerList.isEmpty()) {
            logAbort("startPlayback", "no players"); return
        }

        log("startPlayback(): players=${mediaPlayerList.size}")
        mediaPlayerList.sortedBy { it.getVolume() }.forEach { runCatching { it.start() } }
        isPlaying.postValue(true)
        updatePlaybackState(true)
        notifyOrForeground(true)
        log("Playback started")
    }

    private fun pausePlayback() {
        log("pausePlayback(): players=${mediaPlayerList.size}")
        mediaPlayerList.forEach { runCatching { it.stop() } }
        isPlaying.postValue(false)
        updatePlaybackState(false)
        notifyOrForeground(false)
        log("Playback paused")
    }

    private fun stopSelfSafe() {
        log("stopSelfSafe(): enter")
        isStopping.set(true)

        timer?.cancel(); timer = null
        log("stopSelfSafe(): timer cancelled")

        prepareJob?.cancel(); prepareJob = null
        scope.coroutineContext.cancelChildren()
        log("stopSelfSafe(): jobs cancelled")

        runCatching { pausePlayback() }
        releaseAllPlayers()
        log("stopSelfSafe(): players released")

        isPlaying.postValue(false)
        isService.postValue(false)

        try {
            stopForeground(true)
        } catch (_: Throwable) {
        }
        log("stopSelfSafe(): stopForeground(true) called")

        stopSelf()
        log("stopSelfSafe(): stopSelf() called")
    }

    private fun releaseAllPlayers() {
        val n = mediaPlayerList.size
        log("releaseAllPlayers(): count=$n")
        mediaPlayerList.forEach { runCatching { it.stop(); it.release() } }
        mediaPlayerList = emptyList()
    }

    override fun onDestroy() {
        super.onDestroy()
        isStopping.set(true)
        prepareJob?.cancel()
        prepareJob = null
        // releaseAllPlayers() は既に呼んでいれば二重でもOK（runCatchingでも良い）
        releaseAllPlayers()
    }

    private fun ensureTimeText(hour: Int, minute: Int): String {
        val format = SimpleDateFormat("HH:mm")
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return format.format(cal.time)
    }

    private fun postTime(hour: Int, minute: Int) {
        message.postValue(ensureTimeText(hour, minute))
    }

    private fun postZero() {
        message.postValue("00:00")
    }

    private fun startTimer(hour: Int, minute: Int) {
        var count = hour * 60 + minute
        timer?.cancel(); timer = null

        if (count <= 0) {
            log("timer: NO TIMER (hour=$hour, minute=$minute)")
            message.postValue("")
            return
        }

        var tempHour = hour
        var tempMinute = minute
        log("timer: start hour=$hour minute=$minute count=$count")
        postTime(tempHour, tempMinute)

        timer = Timer()
        timer?.schedule(60 * 1000, 60 * 1000) {
            if (isPlaying.value != true) {
                log("timer: tick skipped (not playing)"); return@schedule
            }

            count--
            if (tempMinute == 0 && tempHour > 0) {
                tempHour--; tempMinute = 59
            } else if (tempMinute > 0) {
                tempMinute--
            }

            log("timer: tick -> $tempHour:$tempMinute (count=$count)")
            postTime(tempHour, tempMinute)

            if (count <= 0) {
                log("timer: FINISH -> stopSelfSafe()")
                postZero()
                stopSelfSafe()
                TimerManager.checkTimerMember(applicationContext)
                if (!TimerManager.isTimerEnable.value) {
                    showAdRequest.postValue(true)
                }
            }
        }
    }

}
