package tinnitus.dissolves.tinnitus_dissolves.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun RewardDescriptionDialog(
    onClose: () -> Unit,
    onWatchAd: () -> Unit,
    onSurvey: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Column {
                Button(
                    onClick = onSurvey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("アンケート")
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("閉じる")
                }
            }
        },
        title = {
            Text(
                "タイマーが作動しました。",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("※１日約１０円で有料会員になれます。（「会員」メニュー参照）")
                Spacer(modifier = Modifier.height(12.dp))
                Text("有料会員になると、タイマー操作可能になります。")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "「問合せ」からアンケートの回答をすると有料会員のお試し（30日間）をすることができます。",
                    color = Color(0xFFFFA500) // オレンジ
                )
            }
        },
        containerColor = Color.White,
        modifier = Modifier
            .padding(16.dp),
        properties = DialogProperties(dismissOnClickOutside = false)
    )
}
