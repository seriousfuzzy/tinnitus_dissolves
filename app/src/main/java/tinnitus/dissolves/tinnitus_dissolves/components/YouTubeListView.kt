package tinnitus.dissolves.tinnitus_dissolves.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import tinnitus.dissolves.tinnitus_dissolves.RemoteConfigViewModel

@Composable
fun YouTubeListView(
    viewModel: RemoteConfigViewModel = viewModel(),
    fragmentManager: FragmentManager
) {
    val context = LocalContext.current
    val showHowToView = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 8.dp)
    ) {

        Divider(color = Color(0xFFA0A0A0), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "「Youtubeの音源とMIX」",
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        Text(
            text = "※YouTubeのアプリのダウンロードが必要です。",
            fontSize = 18.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        TextButton(onClick = { showHowToView.value = true }) {
            Text(
                text = "※使い方はコチラ",
                fontSize = 18.sp,
                color = Color.Blue,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        }

        Text(
            text = "（下記は外部サイトです。）",
            fontSize = 16.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        // YouTube categories
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 1000.dp)
        ) {
            items(viewModel.categories) { category ->
                Button(
                    onClick = { openUrl(context, category.url) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(category.color.toColorInt())
                    ),
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = category.name, color = Color.Black, fontSize = 14.sp)
                }
            }
        }

        Divider(
            color = Color(0xFFA0A0A0),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Text(
            text = "「他のアプリ音源とMIX」",
            fontSize = 18.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Other app categories
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            viewModel.otherCategories.forEach { category ->
                Button(
                    onClick = { openUrl(context, category.url) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(category.color.toColorInt())
                    ),
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .fillMaxWidth()
                ) {
                    Text(text = category.name, color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }

    if (showHowToView.value) {
        HowToYoutubeBottomSheetDialog().show(fragmentManager, "HowToYoutube")
        showHowToView.value = false
    }
}

fun openUrl(context: Context, urlString: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
    context.startActivity(intent)
}