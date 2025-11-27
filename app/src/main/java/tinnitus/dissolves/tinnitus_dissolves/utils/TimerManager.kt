package tinnitus.dissolves.tinnitus_dissolves.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.ZoneId

object TimerManager {
    private const val PREF_NAME = "timer_pref"
    private const val KEY_TIMER_ENABLE = "timer_enable"
    private const val TIMER_MINUTES = 30

    private val _isTimerEnable = MutableStateFlow(false)
    val isTimerEnable: StateFlow<Boolean> = _isTimerEnable

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /** タイマー有効時間を現在から60分後に設定 */
    fun setTimerTime(context: Context) {
        val enableUntil = LocalDateTime.now().plusMinutes(TIMER_MINUTES.toLong())
        val epoch = enableUntil.atZone(ZoneId.systemDefault()).toEpochSecond()
        getPrefs(context).edit { putLong(KEY_TIMER_ENABLE, epoch) }
        checkTimerMember(context)
    }

    /** タイマー会員かどうかチェック（状態を更新） */
    fun checkTimerMember(context: Context) {
//        _isTimerEnable.value = when {
//            PurchaseManager.isPurchase.value -> true
//            CouponManager.isCouponMember(context) -> true
//            else -> isTimerValid(context)  // ← 純粋に保存された有効時間だけを確認
//        }
        _isTimerEnable.value = true
    }

    /** 純粋に保存されたタイマー有効時間をチェック */
    private fun isTimerValid(context: Context): Boolean {
        val prefs = getPrefs(context)
        val savedEpoch = prefs.getLong(KEY_TIMER_ENABLE, -1L)
        if (savedEpoch == -1L) return false

        val nowEpoch = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        return savedEpoch > nowEpoch
    }
}
