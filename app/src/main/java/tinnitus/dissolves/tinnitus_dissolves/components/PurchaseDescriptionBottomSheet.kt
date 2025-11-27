package tinnitus.dissolves.tinnitus_dissolves.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import tinnitus.dissolves.tinnitus_dissolves.MainActivity
import tinnitus.dissolves.tinnitus_dissolves.utils.CouponManager
import tinnitus.dissolves.tinnitus_dissolves.utils.FireStoreHelper
import tinnitus.dissolves.tinnitus_dissolves.utils.PurchaseManager
import tinnitus.dissolves.tinnitus_dissolves.utils.TimerManager

class PurchaseDescriptionBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager) {
            PurchaseDescriptionBottomSheet().show(fragmentManager, "PurchaseDescription")
        }
    }

    private val showingCouponDialog = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PurchaseDescriptionModalScreen(
                    showingCouponDialog = showingCouponDialog,
                    onClose = { dismiss() }
                )
            }
        }
        return composeView
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PurchaseDescriptionModalScreen(
        showingCouponDialog: MutableState<Boolean>,
        onClose: () -> Unit
    ) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            shape = MaterialTheme.shapes.large.copy(
                topStart = CornerSize(24.dp),
                topEnd = CornerSize(24.dp)
            ), // 上部だけ角丸
            containerColor = Color.White, // 背景色
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            PurchaseDescriptionScreen(
                showingCouponDialog = showingCouponDialog,
                onClose = onClose
            )
        }
    }

    @Composable
    fun PurchaseDescriptionScreen(
        showingCouponDialog: MutableState<Boolean>,
        onClose: () -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var couponCode by remember { mutableStateOf("") }

        val isPremium by PurchaseManager.isPurchase.collectAsState()
        val isCouponUser = remember { CouponManager.isCouponMember(context) }
        val isAdUser = remember { TimerManager.isTimerEnable.value }

        // クーポン入力ダイアログ
        if (showingCouponDialog.value) {
            AlertDialog(
                onDismissRequest = { showingCouponDialog.value = false },
                title = { Text("クーポンコードを入力してください") },
                text = {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("コード") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (couponCode.isBlank()) return@TextButton
                        scope.launch {
                            val coupon = FireStoreHelper.getCoupon(couponCode)
                            if (coupon == null || coupon.used) {
                                couponCode = ""
                                Toast.makeText(
                                    context,
                                    "無効なクーポンコードです",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val result = FireStoreHelper.usedCoupon(coupon)
                                if (result) {
                                    CouponManager.setCoupon(context, coupon.expire)
                                    CouponManager.isCouponEnable = true
                                    Toast.makeText(
                                        context,
                                        "クーポン特典が有効になりました（${
                                            CouponManager.getCouponDate(
                                                context
                                            ) ?: ""
                                        }まで）",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showingCouponDialog.value = false
                                    TimerManager.checkTimerMember(context)
                                }
                            }
                        }
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        couponCode = ""
                        showingCouponDialog.value = false
                    }) {
                        Text("キャンセル")
                    }
                }
            )
        }

        // 本体UI
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .background(color = Color.White)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "あなたの現在のステータス",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .align(Alignment.CenterHorizontally)
                )

                when {
                    isPremium -> Text(
                        "有料会員",
                        fontSize = 18.sp, color = Color.Magenta,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    isCouponUser -> Text(
                        "クーポン特典会員",
                        fontSize = 18.sp,
                        color = Color.Green,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    isAdUser -> Text(
                        "広告視聴会員",
                        fontSize = 18.sp,
                        color = Color.Cyan,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    else -> Text(
                        "無料会員",
                        fontSize = 18.sp,
                        color = Color.Blue,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()

                Text(
                    text = "有料会員（月額300円）になると\n以下の機能が有効になります。",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "・広告非表示\n・タイマー機能\n★1日約10円で快適な睡眠に近づけます！",
                    fontSize = 15.sp,
                    color = Color(0xFFCD853F),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 購入ボタン
                Button(
                    onClick = {
                        val activity = context as? MainActivity
                        if (activity != null) {
                            scope.launch {
                                PurchaseManager.fetchOfferings(activity) { product ->
                                    if (product != null) {
                                        PurchaseManager.purchaseProduct(activity, product)
                                    } else {
                                        Toast.makeText(
                                            activity,
                                            "プラン取得に失敗しました",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Activity取得に失敗しました",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("有料会員になる", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // リストアボタン
                Button(
                    onClick = {
                        val activity = context as? MainActivity
                        if (activity != null) {
                            scope.launch {
                                PurchaseManager.restore(activity)
                            }
                        } else {
                            Toast.makeText(context, "リストアに失敗しました", Toast.LENGTH_SHORT)
                                .show()
                        }
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("リストア（購入復元）", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showingCouponDialog.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFcd853f))
                ) {
                    Text("特典クーポン入力", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = """
⚫︎購入の確認・注意事項
・利用規約・プライバシーポリシー
有料会員への加入で「利用規約」と「プライバシーポリシー」に同意いただいたとみなします。

・自動継続課金
契約が切れる24時間以内に自動更新の解除がされない限り、自動的に継続されます。

・解約方法
Google Play アプリ > アカウント > サブスクリプション から解約できます。

・キャンセル
当月分のキャンセルはできず、翌月以降から適用されます。
                """.trimIndent(),
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("閉じる", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewPurchaseDescriptionScreen() {
        val showingCouponDialog = remember { mutableStateOf(false) }

        MaterialTheme {
            PurchaseDescriptionScreen(
                showingCouponDialog = showingCouponDialog,
                onClose = { }
            )
        }
    }
}
