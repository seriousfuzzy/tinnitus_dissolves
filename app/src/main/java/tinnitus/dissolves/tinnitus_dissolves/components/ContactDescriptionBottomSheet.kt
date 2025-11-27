package tinnitus.dissolves.tinnitus_dissolves.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ContactDescriptionBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    ContactDescriptionScreen {
                        dismiss()
                    }
                }
            }
        }
        return composeView
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ContactDescriptionScreen(onDismissRequest: () -> Unit) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            shape = MaterialTheme.shapes.large.copy(
                topStart = CornerSize(24.dp),
                topEnd = CornerSize(24.dp)
            ), // 上部だけ角丸
            containerColor = Color.White, // 背景色
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            ContactDescriptionView()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ContactDescriptionView() {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("お問合せ") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black
                    )
                )
            },
            containerColor = Color.White,
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Text(
                            text = """
                            アプリについて、ご意見・質問・要望・アンケートなどを送信してください。
                        """.trimIndent(),
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .width(340.dp)
                                .padding(vertical = 15.dp)
                        )
                        Divider(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 30.dp, vertical = 15.dp)
                        )

                        ActionButton(
                            label = "問合せ",
                            backgroundColor = Color(0xFF007AFF), // iOS風の青
                            onClick = {
                                coroutineScope.launch {
                                    delay(1000)
                                    sendMail(
                                        context,
                                        title = "とろける～耳鳴り！問合せ",
                                        message = null
                                    )
                                }
                            }
                        )

                        ActionButton(
                            label = "アンケート",
                            backgroundColor = Color(0xFF007AFF),
                            onClick = {
                                coroutineScope.launch {
                                    delay(1000)
                                    sendMail(
                                        context,
                                        title = "とろける～耳鳴り！アンケート",
                                        message = """
                                        ① お名前（ハンドルネーム可）

                                        ② 都道府県

                                        ③ 耳鳴り歴

                                        ④ 今までの耳鳴り対策（具体的に教えていただけると参考になります。）

                                        ⑤ 本アプリ（とろける～耳鳴り）の使用歴

                                        ⑥ 本アプリのお気に入りの音源

                                        ⑦ 本アプリについての感想、改善点など。
                                    """.trimIndent()
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        )
    }

    @Composable
    fun ActionButton(
        label: String,
        backgroundColor: Color,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .width(300.dp)
                .height(80.dp) // Material3推奨高さ
                .padding(vertical = 10.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 10.dp) // Textが潰れないように
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @SuppressLint("UseKtx")
    private fun sendMail(context: Context, title: String, message: String?) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf("master@miminari2023.com"))
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, message ?: "")
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "メールアプリが見つかりません", Toast.LENGTH_SHORT).show()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ContactDescriptionViewScreen() {
        MaterialTheme {
            ContactDescriptionView()
        }
    }

    companion object {
        fun show(fragmentManager: FragmentManager) {
            ContactDescriptionBottomSheet().show(fragmentManager, "ContactDescription")
        }
    }
}
