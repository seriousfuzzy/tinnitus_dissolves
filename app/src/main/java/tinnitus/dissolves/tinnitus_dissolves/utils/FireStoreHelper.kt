package tinnitus.dissolves.tinnitus_dissolves.utils

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FireStoreHelper {

    data class Coupon(
        @JvmField var id: String? = null,
        var coupon_code: String = "",
        var used: Boolean = false,
        var expire: Long = 0
    )

    // FireStore インスタンス
    @SuppressLint("StaticFieldLeak")
    private val db: FirebaseFirestore = Firebase.firestore

    // クーポン使用処理
    suspend fun usedCoupon(coupon: Coupon): Boolean {
        val id = coupon.id ?: return false
        val updatedCoupon = coupon.copy(used = true)

        return try {
            db.collection("coupon")
                .document(id)
                .set(updatedCoupon)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // クーポン取得処理
    suspend fun getCoupon(code: String): Coupon? {
        if (!validate(code)) return null

        return try {
            val snapshot = db.collection("coupon")
                .whereEqualTo("coupon_code", code)
                .limit(1)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
            doc?.toObject(Coupon::class.java)?.apply {
                id = doc.id
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // クーポンコードの検証
    private fun validate(code: String): Boolean {
        if (code.isEmpty()) return false
        if (code.length != 10) return false
        return code.toLongOrNull() != null
    }
}