package tinnitus.dissolves.tinnitus_dissolves.utils

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.core.content.edit

object CouponManager {
    private const val PREF_NAME = "coupon_pref"
    private const val KEY_COUPON_ENABLE = "coupon_enable"

    var isCouponEnable: Boolean = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /** クーポン期限を「今＋n日後」に設定 */
    fun setCoupon(context: Context, days: Long) {
        val enableDate = LocalDateTime.now().plusDays(days.toLong())
        val timestamp = enableDate.atZone(ZoneId.systemDefault()).toEpochSecond()
        getPrefs(context).edit { putLong(KEY_COUPON_ENABLE, timestamp) }
    }

    /** クーポンの期限日を「yyyy年MM月dd日 HH:mm」形式で返す */
    fun getCouponDate(context: Context): String? {
        val prefs = getPrefs(context)
        val epoch = prefs.getLong(KEY_COUPON_ENABLE, -1)
        if (epoch == -1L) return null

        val date = LocalDateTime.ofEpochSecond(
            epoch,
            0,
            ZoneId.systemDefault().rules.getOffset(LocalDateTime.now())
        )
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
        return date.format(formatter)
    }

    /** 現在クーポン有効会員かどうか */
    fun isCouponMember(context: Context): Boolean {
        val prefs = getPrefs(context)
        val epoch = prefs.getLong(KEY_COUPON_ENABLE, -1)
        if (epoch == -1L) return false

        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        return epoch > now
    }
}
