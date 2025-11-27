package tinnitus.dissolves.tinnitus_dissolves.utils

import android.widget.Toast
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tinnitus.dissolves.tinnitus_dissolves.MainActivity

object PurchaseManager {

    private val _isPurchase = MutableStateFlow(false)
    val isPurchase: StateFlow<Boolean> = _isPurchase

    fun fetchOfferings(activity: MainActivity, callback: (StoreProduct?) -> Unit) {
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                android.util.Log.d("RevenueCat", "Offerings all: ${offerings.all}")
                android.util.Log.d("RevenueCat", "Current offering: ${offerings.current}")
                val monthlyPackage = offerings.current?.getPackage("\$rc_monthly")
                if (monthlyPackage == null) {
                    android.util.Log.w("RevenueCat", "Package \$rc_monthly not found")
                }
                callback(monthlyPackage?.product)
            }

            override fun onError(error: PurchasesError) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "エラー: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun purchaseProduct(activity: MainActivity, product: StoreProduct) {
        Purchases.sharedInstance.purchaseProduct(activity, product, object : PurchaseCallback {
            override fun onCompleted(
                storeTransaction: StoreTransaction,
                customerInfo: CustomerInfo
            ) {
                updatePurchaseStatus(customerInfo)
                TimerManager.checkTimerMember(activity)
                activity.runOnUiThread {
                    Toast.makeText(activity, "購入処理完了しました", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: PurchasesError, userCancelled: Boolean) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "エラー: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun restore(activity: MainActivity) {

        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                updatePurchaseStatus(customerInfo)
                val msg = when {
                    customerInfo.entitlements.active.isNotEmpty() -> "リストア処理を完了しました。"
                    else -> "購入情報はありません"
                }
                activity.runOnUiThread {
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(error: PurchasesError) {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "リストアに失敗しました: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    /** アプリ起動時などに呼ぶ購読ステータス更新 */
    fun refreshCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                TODO("Not yet implemented")
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                updatePurchaseStatus(customerInfo)
            }

        })
    }

    /** 共通：CustomerInfo から購読ステータスを抽出して更新 */
    private fun updatePurchaseStatus(customerInfo: CustomerInfo) {
        val isActive = customerInfo.entitlements.active.containsKey("monthly")
        _isPurchase.value = isActive
    }
}