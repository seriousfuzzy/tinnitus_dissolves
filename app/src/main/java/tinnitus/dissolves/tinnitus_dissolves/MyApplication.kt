package tinnitus.dissolves.tinnitus_dissolves

import android.app.Application
//import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class MyApplication : Application() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
//        MobileAds.initialize(this)
        remoteConfig = FirebaseRemoteConfig.getInstance()

        Purchases.configure(
            PurchasesConfiguration.Builder(this, "goog_PrqkGgyxrDtZOafcREDpDTnGwWZ")
                .build()
        )
        Purchases.logLevel = LogLevel.DEBUG

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()

        instance = this
    }

    companion object {
        private lateinit var instance: MyApplication
        fun getRemoteConfig(): FirebaseRemoteConfig = instance.remoteConfig
    }
}