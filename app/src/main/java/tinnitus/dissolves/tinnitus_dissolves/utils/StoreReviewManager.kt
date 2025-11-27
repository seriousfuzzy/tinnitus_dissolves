package tinnitus.dissolves.tinnitus_dissolves.utils

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.concurrent.TimeUnit

object StoreReviewManager {

    private const val PREF_NAME = "store_review_prefs"
    private const val KEY_STARTUP_DATE = "start_up_date"
    private const val KEY_REVIEW_COUNT = "review_cnt"

    private val reviewThresholds = listOf(1, 7, 31) // インストール後 1日, 7日, 31日

    fun checkStoreReview(context: Context, isTest: Boolean = false, completion: (Boolean) -> Unit) {
        if (isTest) {
            completion(true) // テストモードなら毎回表示
            return
        }
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val currentTime = System.currentTimeMillis()
        val installTime = prefs.getLong(KEY_STARTUP_DATE, -1L).let {
            if (it == -1L) {
                prefs.edit { putLong(KEY_STARTUP_DATE, currentTime) }
                currentTime
            } else {
                it
            }
        }

        val daysSinceInstall = TimeUnit.MILLISECONDS.toDays(currentTime - installTime)
        val reviewCount = prefs.getInt(KEY_REVIEW_COUNT, 0)

        if (reviewCount < reviewThresholds.size && daysSinceInstall > reviewThresholds[reviewCount]) {
            prefs.edit { putInt(KEY_REVIEW_COUNT, reviewCount + 1) }
            completion(true)
        } else {
            completion(false)
        }
    }

    fun showReviewDialog(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            }
        }
    }
}