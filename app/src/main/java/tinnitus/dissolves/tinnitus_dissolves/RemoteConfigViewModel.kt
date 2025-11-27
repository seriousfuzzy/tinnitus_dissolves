package tinnitus.dissolves.tinnitus_dissolves

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class YouTubeCategory(
    val name: String,
    val url: String,
    val color: String
)

class RemoteConfigViewModel : ViewModel() {
    var categories by mutableStateOf(listOf<YouTubeCategory>())
        private set

    var otherCategories by mutableStateOf(listOf<YouTubeCategory>())
        private set

    private val remoteConfig = MyApplication.getRemoteConfig()

    init {
        fetchRemoteConfig()
    }

    private fun fetchRemoteConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val youtubeJson = remoteConfig.getString("youtubeCategories")
                val otherJson = remoteConfig.getString("otherCategories_android")

                val gson = Gson()
                val type = object : TypeToken<List<YouTubeCategory>>() {}.type
                val youtubeList: List<YouTubeCategory> =
                    gson.fromJson(youtubeJson, type) ?: emptyList()
                val otherList: List<YouTubeCategory> = gson.fromJson(otherJson, type) ?: emptyList()

                // UI更新はメインスレッドで
                launch(Dispatchers.Main) {
                    categories = youtubeList
                    otherCategories = otherList
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // エラー処理をここに
            }
        }
    }
}
