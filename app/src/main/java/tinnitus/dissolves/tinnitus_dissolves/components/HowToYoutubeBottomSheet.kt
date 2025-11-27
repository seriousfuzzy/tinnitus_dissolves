package tinnitus.dissolves.tinnitus_dissolves.components

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HowToYoutubeBottomSheetDialog : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HowToYoutubeModalScreen {
                        dismiss() // 閉じるボタン押したらダイアログを閉じる
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToYoutubeModalScreen(onDismissRequest: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.large.copy(
            topStart = CornerSize(24.dp),
            topEnd = CornerSize(24.dp)
        ), // 上部だけ角丸
        containerColor = Color.White, // 背景色
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        HowToYoutubeScreen(onClose = onDismissRequest)
    }
}

@Composable
fun HowToYoutubeScreen(onClose: () -> Unit = {}) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "本アプリの音源はYouTubeおよびその他のアプリの音源とMIXして使用することができます。\n使用するためには「YouTubeアプリ」のダウンロードが必要です。",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val url = "https://play.google.com/store/apps/details?id=com.google.android.youtube"
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                "「YouTubeアプリ」ダウンロード",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("「使い方」", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(24.dp))

        HighlightedText("①本アプリと他のアプリの操作の切り替え", Color.Yellow, 22.sp)
        Text("スマホ画面下部を左右にスワイプする。", fontSize = 18.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(20.dp))

        HighlightedText("②使用しないアプリを閉じる。", Color.Yellow, 22.sp)
        Text(
            "スマホ画面下部から画面中央にスワイプすると現在動作中のアプリがすべて表示されます。必要のないアプリを上にスワイプするとアプリを閉じることができます。",
            fontSize = 18.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        HighlightedText("③YouTubeの音源とMIXできない場合", Color.Yellow, 22.sp)
        IndentText("1)", "アプリの音源を再生する")
        IndentText(
            "2)",
            "YouTubeのアプリは使用せず、ブラウザーでYouTubeを開いて音源を再生してください。"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val url =
                    "https://www.google.com/search?q=youtube+%E3%83%96%E3%83%A9%E3%82%A6%E3%82%B6%E7%89%88"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                "ブラウザを起動",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        IndentText("3)", "検索画面の一番上の「YouTube」をクリック")
        IndentText("4)", "「Google」をクリック")
        IndentText("5)", "「YouTubeを見る」をクリック")
        IndentText("6)", "何かYouTubeの音源を再生してアプリの音と１度MIXできればあとは使用可能です")

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHowToYoutubeScreen() {
    MaterialTheme {
        HowToYoutubeScreen()
    }
}
