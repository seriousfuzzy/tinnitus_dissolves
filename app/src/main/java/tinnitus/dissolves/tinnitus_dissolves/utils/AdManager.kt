//package tinnitus.dissolves.tinnitus_dissolves.utils
//
//import android.app.Activity
//import android.content.Context
//import android.widget.Toast
//import androidx.core.content.edit
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.rewarded.RewardedAd
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
//
//object AdManager {
//    private const val PREFS_NAME = "ad_prefs"
//    private const val KEY_LAST_AD_VIEW = "last_ad_viewer"
//    const val AD_TIME = 60 // 分
//    val adUnitId = "ca-app-pub-3690083200842594/6060465091"
//
//    private var rewardedAd: RewardedAd? = null
//
//    fun shouldShowReward(context: Context): Boolean {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        val lastView = prefs.getLong(KEY_LAST_AD_VIEW, 0L)
//        val now = System.currentTimeMillis()
//        val elapsedMinutes = (now - lastView) / 1000 / 60
//        return elapsedMinutes >= AD_TIME
//    }
//
//    private fun updateLastViewTime(context: Context) {
//        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        prefs.edit { putLong(KEY_LAST_AD_VIEW, System.currentTimeMillis()) }
//    }
//
//    fun loadRewardAd(context: Context, onLoaded: (() -> Unit)? = null) {
//        val adRequest = AdRequest.Builder().build()
//        RewardedAd.load(
//            context,
//            adUnitId,
//            adRequest,
//            object : RewardedAdLoadCallback() {
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    rewardedAd = null
//                }
//
//                override fun onAdLoaded(ad: RewardedAd) {
//                    rewardedAd = ad
//                    onLoaded?.invoke()
//                }
//            }
//        )
//    }
//
//    fun showRewardAd(activity: Activity, onResult: (Boolean) -> Unit) {
//        if (rewardedAd != null) {
//            rewardedAd?.show(activity) {
//                // 報酬が付与された場合
//                updateLastViewTime(activity)
//                TimerManager.setTimerTime(activity)
//                onResult(true)
//            }
//        } else {
//            Toast.makeText(activity, "広告の読み込みに失敗しました", Toast.LENGTH_SHORT).show()
//            onResult(false)
//        }
//    }
//}
