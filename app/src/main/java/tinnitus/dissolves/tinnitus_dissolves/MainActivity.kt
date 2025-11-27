package tinnitus.dissolves.tinnitus_dissolves

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.edit
import com.google.gson.Gson
import tinnitus.dissolves.tinnitus_dissolves.components.ContactDescriptionBottomSheet
import tinnitus.dissolves.tinnitus_dissolves.components.HowToUseBottomSheetDialog
import tinnitus.dissolves.tinnitus_dissolves.components.ListAdapter
import tinnitus.dissolves.tinnitus_dissolves.components.RewardDescriptionDialog
import tinnitus.dissolves.tinnitus_dissolves.components.YouTubeListView
import tinnitus.dissolves.tinnitus_dissolves.model.MusicData
import tinnitus.dissolves.tinnitus_dissolves.model.SliderModel
import tinnitus.dissolves.tinnitus_dissolves.utils.TimerManager
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private val playerInfo: ArrayList<MusicData> = arrayListOf(
            MusicData("1． 単音 MIX（1297）", R.raw.s1),
            MusicData("2． 単音（1295）", R.raw.s2),
            MusicData("3． 超高音MIX（1306）", R.raw.s3),
            MusicData("4． 超高音（1267）", R.raw.s4),
            MusicData("5． 高音MIX 単音ﾌﾟﾗｽ（1314）", R.raw.s5),
            MusicData("6． 高音MIX ﾐｽﾄ（1286）", R.raw.s6),
            MusicData("7． 高音MIX 木枯らし（1287）", R.raw.s7),
            MusicData("8． 高音MIX 白龍（1316）", R.raw.s8),
            MusicData("9． 高音①（1217）", R.raw.s9),
            MusicData("10． 高音②（1221）", R.raw.s10),
            MusicData("11． 中音MIX ｽﾌﾟﾗｯｼｭ（1302）", R.raw.s11),
            MusicData("12． 中音MIX ﾒｼｱ（1269）", R.raw.s12),
            MusicData("13． 中音MIX 青龍（1317）", R.raw.s13),
            MusicData("14． 中音MIX 銀龍（1315）", R.raw.s14),
            MusicData("15． 中音①（1281）", R.raw.s15),
            MusicData("16． 中音②（1225）", R.raw.s16),
            MusicData("17． 中音③（1224）", R.raw.s17),
            MusicData("18． 低音MIX 送風機（1320）", R.raw.s18),
            MusicData("19． 低音MIX 夜の海（1282）", R.raw.s19),
            MusicData("20． 低音MIX 黒龍（1322）", R.raw.s20),
            MusicData("21． 低音（1321）", R.raw.s21),
            MusicData("22． 鈴（1312）", R.raw.s22),
            MusicData("23． 鈴MIX（1313）", R.raw.s23),
            MusicData("24． ベル 長音（1309）", R.raw.s24),
            MusicData("25． 小鳥（1311）", R.raw.s25),
            MusicData("26． 鈴虫（1300）", R.raw.s26),
            MusicData("27． セミ（1233）", R.raw.s27),
            MusicData("28． 焚火（1237）", R.raw.s28),
            MusicData("29． ﾘﾗｯｸｽ 湧き水（1310）", R.raw.s29),
            MusicData("30． ﾘﾗｯｸｽ シリシリ（1241）", R.raw.s30),
        )
    }

    private val audioManager by lazy { getSystemService(AUDIO_SERVICE) as AudioManager }

    private lateinit var volumeSeekBar: SeekBar
    private lateinit var listView: ListView
    private lateinit var listAdapter: ListAdapter
    private lateinit var timerImage: AppCompatImageView
    private lateinit var timerText: TextView
    private lateinit var toggleButton: AppCompatToggleButton
    private lateinit var setting1: Button
    private lateinit var setting2: Button
    private lateinit var setting3: Button
    private lateinit var timePicker: TimePickerDialog
    private lateinit var notificationManager: NotificationManager

    private var hour = 0
    private var minute = 0
    private var debounce: Long = 0

    private val showRewardDialogState = mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SimpleDateFormat", "UnspecifiedRegisterReceiverFlag", "ImplicitSamInstance")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        TimerManager.checkTimerMember(this)

        val name = getString(R.string.app_name)
        val descriptionText = getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel("service", name, importance).apply {
            description = descriptionText
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        timePicker = TimePickerDialog(
            this,
            AlertDialog.THEME_HOLO_LIGHT,
            { _, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                val format = SimpleDateFormat("HH:mm")
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                timerText.text = format.format(calendar.time)

                if (toggleButton.isChecked) {
                    val service = Intent(this@MainActivity, BackgroundService::class.java)
                        .setAction("TIMER_UPDATE")
                        .putExtra("hour", hour)
                        .putExtra("minute", minute)
                    startForegroundService(service)
                }
            }, hour, minute, true
        )
        timePicker.setTitle("タイマーの設定")
        timePicker.setMessage("")

        timerImage = findViewById(R.id.timer)
        timerImage.setOnClickListener {
            TimerManager.checkTimerMember(this)
            if (TimerManager.isTimerEnable.value) {
                timePicker.show()
            } else {
                Toast.makeText(this, "タイマーは有料会員で使用可能です", Toast.LENGTH_SHORT).show()
            }
        }

        timerText = findViewById(R.id.text_timer)

        volumeSeekBar = findViewById(R.id.seek_volume)
        volumeSeekBar.progress = volume
        volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.min = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_SHOW_UI
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        listView = findViewById(R.id.list)

        // Compose footer
        val composeView = ComposeView(this).apply {
            layoutParams = AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT
            )
            setContent { YouTubeListView(fragmentManager = supportFragmentManager) }
        }
        val dialogHost = ComposeView(this).apply {
            layoutParams = AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.WRAP_CONTENT
            )
            setContent {
                if (showRewardDialogState.value) {
                    RewardDescriptionDialog(
                        onWatchAd = {
                            // Ad後の処理（省略）
                            showRewardDialogState.value = false
                            invalidate()
                        },
                        onSurvey = {
                            ContactDescriptionBottomSheet.show(supportFragmentManager)
                            showRewardDialogState.value = false
                            invalidate()
                        },
                        onClose = {
                            showRewardDialogState.value = false
                            invalidate()
                        }
                    )
                }
            }
        }

        listView.addFooterView(composeView)
        findViewById<LinearLayout>(R.id.center_container).addView(dialogHost)

        toggleButton = findViewById(R.id.toggle_button)
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            val time = System.currentTimeMillis()
            if (debounce != 0L && time - debounce < 1000L) {
                toggleButton.isChecked = !isChecked
                return@setOnCheckedChangeListener
            }
            debounce = time

            val service = Intent(this@MainActivity, BackgroundService::class.java)
            if (isChecked) {
                // 必要最小限の配列にして軽量化（インデックスをUIと揃える）
                val size = listAdapter.listData.size
                val resIds = IntArray(size) { i -> listAdapter.listData[i].data.soundId1 }
                val volumes = FloatArray(size) { i -> listAdapter.listData[i].volume }
                val pans = FloatArray(size) { i -> listAdapter.listData[i].pan }
                service.putExtra("resIds", resIds)
                service.putExtra("volumes", volumes)
                service.putExtra("pans", pans)

                if (TimerManager.isTimerEnable.value) {
                    service.putExtra("hour", hour)
                    service.putExtra("minute", minute)
                } else {
                    service.putExtra("hour", 0)
                    service.putExtra("minute", 1)
                }
                // 再生開始 → Serviceが生成/前面化
                startForegroundService(service)
            } else {
                // 明示停止（STOPアクション）→ onDestroyでrelease
                Intent(this@MainActivity, BackgroundService::class.java)
                    .setAction("STOP").also { startService(it) }
                stopService(Intent(this@MainActivity, BackgroundService::class.java)) // ★ 追加
                this.hour = 0
                this.minute = 0
                timerText.text = "00:00"
            }
        }

        setting1 = findViewById(R.id.setting_1)
        setting2 = findViewById(R.id.setting_2)
        setting3 = findViewById(R.id.setting_3)
        setting1.setOnClickListener(this)
        setting2.setOnClickListener(this)
        setting3.setOnClickListener(this)

        findViewById<TextView>(R.id.reset).setOnClickListener {
            // Service稼働中なら一括反映をServiceへ
            if (BackgroundService.isService.value == true) {
                Intent(this, BackgroundService::class.java)
                    .setAction("RESET_LEVELS")
                    .also { startForegroundService(it) }
            }
            // UIはゼロ表示に更新（反映は上のServiceが担当）
            listAdapter.reset()
        }
        findViewById<TextView>(R.id.how_to_use).setOnClickListener {
            HowToUseBottomSheetDialog.show(supportFragmentManager)
        }
        findViewById<TextView>(R.id.contact).setOnClickListener {
            ContactDescriptionBottomSheet.show(supportFragmentManager)
        }

        findViewById<TextView>(R.id.volume_up).setOnClickListener {
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (currentVolume < maxVolume) {
                val newVolume = currentVolume + 1
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    newVolume,
                    AudioManager.FLAG_SHOW_UI
                )
                volumeSeekBar.progress = newVolume
            }
        }
        findViewById<TextView>(R.id.volume_down).setOnClickListener {
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val minVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
            if (currentVolume > minVolume) {
                val newVolume = currentVolume - 1
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    newVolume,
                    AudioManager.FLAG_SHOW_UI
                )
                volumeSeekBar.progress = newVolume
            }
        }

        listAdapter = ListAdapter(this@MainActivity, playerInfo.map { SliderModel(data = it) })
        listView.adapter = listAdapter

        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            HowToUseBottomSheetDialog.show(supportFragmentManager)
            sharedPreferences.edit { putBoolean("is_first_launch", false) }
        }

        BackgroundService.message.observe(this) { msg ->
            runOnUiThread {
                if (msg.isNotEmpty()) {
                    timerText.text = msg
                    if (msg == "00:00") {
                        this.hour = 0
                        this.minute = 0
                        toggleButton.isChecked = false
                    }
                }
            }
        }
        BackgroundService.showAdRequest.observe(this) { request ->
            if (request == true) {
                // 必要なら広告導線
                showRewardDialogState.value = false
                BackgroundService.showAdRequest.postValue(false)
            }
        }
    }

    override fun onResume() {
        super.onResume() /* レビュー誘導など従来どおり */
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return

        if (BackgroundService.isService.value != true) {
            // サービス自体がいないときだけリセット
            timerText.text = "00:00"
            this.hour = 0
            this.minute = 0
            toggleButton.isChecked = false
        }
        // isService が true の間は UI 側からトグル状態を変更しない
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val result = super.onKeyDown(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            volumeSeekBar.progress = v
        }
        return result
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        val result = super.onKeyLongPress(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            val v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            volumeSeekBar.progress = v
        }
        return result
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.setting_1 -> showSettingDialog("setting1")
            R.id.setting_2 -> showSettingDialog("setting2")
            R.id.setting_3 -> showSettingDialog("setting3")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showSettingDialog(settingKey: String) {
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        val gson = Gson()

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("音源データの登録")
            .setMessage("")
            .setPositiveButton("保存") { _, _ ->
                sharedPreferences.edit { putString(settingKey, gson.toJson(listAdapter.listData)) }
            }
            .setNegativeButton("再生") { _, _ ->
                // Service にプリセットキーを渡して「読込→準備→再生」を任せる
                val intent = Intent(this, BackgroundService::class.java)
                    .setAction("LOAD_PRESET_AND_PLAY")
                    .putExtra("presetKey", settingKey)
                    .putExtra("hour", if (TimerManager.isTimerEnable.value) hour else 0)
                    .putExtra("minute", if (TimerManager.isTimerEnable.value) minute else 1)
                startForegroundService(intent)
            }
            .create()

        alertDialog.setOnShowListener {
            alertDialog.window?.decorView?.rootView?.setOnTouchListener { _, _ ->
                alertDialog.dismiss(); true
            }
        }
        alertDialog.show()
    }
}
