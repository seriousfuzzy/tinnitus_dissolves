package tinnitus.dissolves.tinnitus_dissolves.components

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tinnitus.dissolves.tinnitus_dissolves.R

class HowToUseBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        fun show(fragmentManager: FragmentManager) {
            HowToUseBottomSheetDialog().show(fragmentManager, "HowToUseBottomSheetDialog")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HowToUseModalScreen {
                        dismiss() // 閉じるボタン押したらダイアログを閉じる
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUseModalScreen(onDismissRequest: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.large.copy(
            topStart = CornerSize(24.dp),
            topEnd = CornerSize(24.dp)
        ), // 上部だけ角丸
        containerColor = Color.White, // 背景色
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        HowToUseScreen(onClose = onDismissRequest)
    }
}

@Composable
fun HowToUseScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        SectionTitle("アプリをご利用頂き、ありがとうございます。")
        Spacer(Modifier.height(20.dp))
        SectionTitle("初めてこのアプリをお使いになる方へ")
        Spacer(Modifier.height(20.dp))

        HighlightedText(
            text = "このアプリを使用するには、\n３つのコツが必要です。",
            highlightColor = Color.Cyan,
            fontSize = 24.sp
        )

        Spacer(Modifier.height(20.dp))

        BoldBody(
            """
            使い方を間違えると、雑音にしかなりません。
            最初は、この「３つのコツ」を実践してからお使いください。
        """.trimIndent()
        )

        Spacer(Modifier.height(20.dp))

        HighlightedText(
            text = "このアプリについて",
            highlightColor = Color.Yellow,
            fontSize = 26.sp
        )
        Spacer(Modifier.height(20.dp))

        StyledText(
            fullText = """
                このアプリは、耳鳴りが酷い時にでも熟睡できることを目指して作成いたしました。
                理想は、部屋のエアコンの音のように生活に溶け込む音で耳鳴りをガードすることです。
            """.trimIndent(),
            boldPart = "耳鳴りが酷い時にでも熟睡できること"
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        IndentText(
            "※",
            "このアプリは医療機器ではございません。ノイズ再生アプリです。ご自分の耳鳴りの具合を確認しながらご自身の責任でご利用ください。大音量などの過度な使い方はお薦めいたしません。いかなる場合でも当方では一切の責任を負うことはできません。ご心配な方は医療機関と相談の上、ご利用ください。"
        )

        Divider(modifier = Modifier.padding(vertical = 20.dp))

        Spacer(Modifier.height(20.dp))

        Cotsu()

        Spacer(modifier = Modifier.height(24.dp))

        Kihon()

        Divider(modifier = Modifier.padding(vertical = 20.dp))

        Spacer(Modifier.height(30.dp))

        Osusume()

        Spacer(Modifier.height(30.dp))

        Other()

        Spacer(Modifier.height(30.dp))

        Developer()

        Spacer(Modifier.height(30.dp))

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            interactionSource = remember { MutableInteractionSource() }
        ) {
            Text("閉じる", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun Cotsu() {
    Column {
        HighlightedText(
            text = "コツ① : 耳になじませる",
            highlightColor = Color(0xFF7cfc00),
            fontSize = 20.sp
        )

        StyledText(
            fullText = """
                最初の１０分間、耳にアプリの音をなじませることが必要です。
                理耳になじむと効果を発揮しだします。
            """.trimIndent(),
            boldPart = "最初の１０分間"
        )

        Spacer(Modifier.height(20.dp))

        StyledText(
            fullText = """
            人間の脳は、聞きなれない音には注意力が増すため、使用当初はアプリの音がとても気になり雑音に聞こえます。ただ、耳になじめばエアコンの音と同じように生活の中に溶け込みます。
            まず、自分の耳鳴りに合いそうな音を探して１０分間聞いてください。
            聞いている間はネットニュースなどを見ながらリラックスしてアプリの音に集中しないように聞き流してください。
            （このアプリは他のアプリの音源と並行して使用することができます。）
        """.trimIndent(),
            boldPart = "１０分間"
        )

        Spacer(Modifier.height(20.dp))

        HighlightedText(
            text = "コツ② : 音に集中しない。",
            highlightColor = Color(0xFF7cfc00),
            fontSize = 20.sp
        )

        Spacer(Modifier.height(20.dp))

        Body(
            """
            耳鳴りの音やアプリの音をできるだけ気にしないようにし、エアコンの音のようにアプリの音が生活に溶け込むことができれば効果が高いです。
            
            特に就寝時はYouTube音源などを組み合わせてYouTube音源に神経を集中させると効果的です。
           """.trimIndent()
        )

        Spacer(Modifier.height(20.dp))

        HighlightedText(
            text = "コツ③ : 使用機器",
            highlightColor = Color(0xFF7cfc00),
            fontSize = 20.sp
        )

        Spacer(Modifier.height(20.dp))

        Body(
            """
            スマートフォン、ｉＰａｄでお聞きすることをお薦めします。

            特に高音の場合は、耳までの距離が遠すぎると音が減衰してしまうため、離れた位置にある外部のスピーカーなどではアプリのパフォーマンスを最大限発揮できません。
            スマートフォンの置く位置はとても重要ですので、色々試してみてください。
            """.trimIndent()
        )

        Spacer(Modifier.height(20.dp))

        Other()

        Spacer(Modifier.height(20.dp))

        HighlightedText(
            "コツは以上です。簡単にお試しできます。\nこのコツの実践後は、お好きにお使いください。",
            Color(0xFFee82ee),
            24.sp
        )
    }
}

@Composable
private fun Kihon() {
    Column {
        HighlightedText(
            "アプリの基本的な使い方",
            Color(0xFFffdab9),
            24.sp
        )

        Spacer(Modifier.height(20.dp))

        IndentText(
            "①",
            "右上のON/OFFボタンをON（緑色）にしてください。"
        )

        IndentText(
            "②",
            """
                音源を選択します。
                中央にある音源の中から気になる音源の横スライダーを右側にスライドさせる。
                音源は３０種類あります。上下にスライドさせると表示されます。
            """.trimIndent()
        )

        IndentText(
            "③",
            """
                右上のON/OFFボタンの下の主音量調整スライド（縦スライド）で音量を調整できます。
            """.trimIndent()
        )

        IndentText(
            "・",
            """
                複数の音源を組み合わせて同時に再生できます。
            """.trimIndent()
        )

        IndentText(
            "・",
            """
                自分の耳鳴りに合った音源を慎重に探してみてください。
                自分に合った音源が見つかると、アプリの音をどんどん小さくできます。
            """.trimIndent()
        )

        IndentText(
            "・",
            """
                耳鳴りの音は、日によって音域が変わります。
            """.trimIndent()
        )

        IndentText(
            "・",
            """
                耳鳴りの音は、単音でない場合もあります。
            """.trimIndent()
        )

        IndentText(
            "・",
            """
                その場合、違う音域の音と組み合わせると効果があります。
                （例：　高音 + 中音　）
            """.trimIndent()
        )

        IndentAndStyleText(
            "・",
            """
                耳鳴りの音やアプリの音に集中しないようにしてください。
                エアコンの音のように生活に溶け込むことができれば効果が高いです。
            """.trimIndent(),
            """
                エアコンの音のように生活に溶け込むことができれば効果が高いです。
            """.trimIndent()
        )
    }
}

@Composable
private fun Osusume() {
    Column {
        HighlightedText(
            "お薦めの使用方法",
            Color(0xFF00bfff),
            24.sp
        )

        Spacer(Modifier.height(20.dp))

        HighlightedText(
            "・耳鳴りが酷くて寝むれない時",
            Color(0xFF7cfc00),
            20.sp
        )

        IndentAndStyleText(
            "",
            """
                スマートフォンを２台使用することをお薦めします。
                左右から音を流すと臨場感もありとても効果的です。
                古いスマートフォンをアプリ専用にすると利用しやすいです。
            """.trimIndent(),
            """
                スマートフォンを２台
            """.trimIndent()
        )

        Spacer(Modifier.height(20.dp))

        IndentText(
            "",
            """
                鈴やベルなどの響く音をMIXさせることをお薦めします。
                あと、スマートフォンを２台使うことも効果的です。
            """.trimIndent()
        )

        Spacer(Modifier.height(20.dp))

        HighlightedText(
            "・就寝時",
            Color(0xFF7cfc00),
            20.sp
        )

        IndentAndStyleText(
            "",
            """
                「耳鳴りに合うアプリの音源」＋「YouTube音源」の組み合わせをお薦めします。
                音への集中をYouTube音源にすれば、耳鳴りを気にせず眠ることができます。
            """.trimIndent(),
            """
                「耳鳴りに合うアプリの音源」＋「YouTube音源」
            """.trimIndent()
        )

        Spacer(Modifier.height(20.dp))

        HighlightedText(
            "・外出時",
            Color(0xFF7cfc00),
            20.sp
        )

        IndentAndStyleText(
            "",
            """
                メガネタイプの骨伝導イヤホンのご利用をお薦めします。

                耳にイヤホンを入れないため、外の音が聞こえ会話にも問題なく、何より他の人に気付かれないので気兼ねの必要がありません。
                仕事中や学校の授業中はもちろん静かな図書館、静かなレストランでの会食時などにも最適です。
            """.trimIndent(),
            """
                メガネタイプの骨伝導イヤホン
            """.trimIndent()
        )
    }
}

@Composable
private fun Other() {
    Column {
        HighlightedText(
            "その他機能",
            Color(0xFFffa07a),
            24.sp
        )

        Spacer(Modifier.height(20.dp))

        IndentText(
            "①",
            """
                タイマー
                （有料会員のみ）
            """.trimIndent()
        )

        IndentText(
            "②",
            """
                音源の保存
            """.trimIndent()
        )

        IndentText(
            "③",
            """
                音源のリセット
            """.trimIndent()
        )

        IndentText(
            "④",
            """
                会員情報の確認
            """.trimIndent()
        )

        IndentText(
            "⑤",
            """
                使い方
            """.trimIndent()
        )
        IndentText(
            "⑥",
            """
                問合せ
            """.trimIndent()
        )

        IndentText(
            "⑦",
            """
                オフライン時のアプリON/OFF操作
                （有料会員のみ）
            """.trimIndent()
        )

        IndentText(
            "⑧",
            """
                音量の左右調整（LR）
                （イヤホン使用時）
            """.trimIndent()
        )
    }
}

@Composable
private fun Developer() {
    val context = LocalContext.current
    Column {
        Button(
            onClick = { showPasswordDialog(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF666666))
        ) {
            Text("調整", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(30.dp))

        HighlightedText(
            "開発者から",
            Color(0xFFee82ee),
            24.sp
        )

        Spacer(Modifier.height(20.dp))

        IndentText(
            "",
            """
                私は、耳鳴り対策は、自分の脳との戦いだと思っております。
                アプリの音や耳鳴りの音に神経を集中してしまうと、頭の中で共鳴してきたり、耳鳴りの音が違う音に変わったりもします。
                その場合は、落ち着いて、スマートフォンの位置や音量を変えたり、耳鳴りの音に合わせた音源を追加して対応してみてください。
                私自身も、耳鳴りでかなり苦労してきました。
                更に良いアプリを作っていきたいと思いますので、アプリ内の問合せフォームからご意見をお待ちしております。
                少しでも皆様のお役に立てれば本望です。

            """.trimIndent()
        )
    }
}

@Preview(showBackground = true, name = "HowToUseScreen Preview", heightDp = 2000)
@Composable
fun PreviewHowToUseScreen1() {
    Cotsu()
}

@Preview(showBackground = true, name = "HowToUseScreen Preview", heightDp = 2000)
@Composable
fun PreviewHowToUseScreen2() {
    Kihon()
}

@Preview(showBackground = true, name = "HowToUseScreen Preview", heightDp = 2000)
@Composable
fun PreviewHowToUseScreen3() {
    Osusume()
}

@Preview(showBackground = true, name = "HowToUseScreen Preview", heightDp = 2000)
@Composable
fun PreviewHowToUseScreen4() {
    Developer()
}

fun showPasswordDialog(context: Context) {
    val editText = EditText(context)
    editText.hint = "パスワードを入力"
    val dialog = AlertDialog.Builder(context)
        .setTitle("開発者専用")
        .setView(editText)
        .setPositiveButton("OK") { _, _ ->
            val input = editText.text.toString()
            if (input == "1234") {
                showDebugSettingDialog(context)
            } else {
                Toast.makeText(context, "パスワードが違います", Toast.LENGTH_SHORT).show()
            }
        }
        .setNegativeButton("キャンセル") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        .create()

    dialog.show()
}

fun showDebugSettingDialog(context: Context) {
    val sharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    val editText = AppCompatEditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        setText(sharedPreferences.getLong("debug", 2250L).toString())
    }

    AlertDialog.Builder(context)
        .setTitle("音源のインターバル設定(ms)")
        .setView(editText)
        .setPositiveButton("OK") { dialog, _ ->
            val debug: Long = try {
                editText.text.toString().toLong()
            } catch (e: Exception) {
                dialog.dismiss()
                return@setPositiveButton
            }
            sharedPreferences.edit { putLong("debug", debug) }
            dialog.dismiss()

            Toast.makeText(context, "設定変更のためアプリを終了します", Toast.LENGTH_SHORT).show()

            // アクティビティ終了（delay付き）※cast安全性確認あり
            if (context is android.app.Activity) {
                Handler(context.mainLooper).postDelayed({
                    context.finish()
                }, 1000)
            }
        }
        .setNegativeButton("キャンセル") { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}