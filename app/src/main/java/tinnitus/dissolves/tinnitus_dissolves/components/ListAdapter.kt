package tinnitus.dissolves.tinnitus_dissolves.components

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.toColorInt
import tinnitus.dissolves.tinnitus_dissolves.BackgroundService
import tinnitus.dissolves.tinnitus_dissolves.R
import tinnitus.dissolves.tinnitus_dissolves.model.SliderModel

data class ViewHolderItem(
    val parent: LinearLayout,
    val titleView: TextView,
    val panButton: Button,
    val panText: TextView,
    val seekBar: SeekBar
)

class ListAdapter(
    context: Context, var listData: List<SliderModel>
) : ArrayAdapter<SliderModel>(context, 0, listData) {

    private val layoutInflater = LayoutInflater.from(context)

    private var colors: List<Color> = listOf(
        Color.valueOf(0.64f, 0.52f, 0.37f),
        Color.valueOf(1.00f, 0.23f, 0.19f),
        Color.valueOf(1.00f, 0.58f, 0.00f),
        Color.valueOf(0.00f, 0.78f, 0.75f),
        Color.valueOf(0.20f, 0.78f, 0.35f),
        Color.valueOf(0.20f, 0.68f, 0.90f),
        Color.valueOf(0.00f, 0.48f, 1.00f),
        Color.valueOf(0.35f, 0.34f, 0.84f),
        Color.valueOf(0.69f, 0.32f, 0.87f),
        Color.valueOf(1.00f, 0.18f, 0.33f),
    )

    fun reset() {
        listData = listData.map {
            SliderModel(
                0.0f, 0.0f, it.data
            )
        }
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val (viewHolder, view) = when (convertView) {
            null -> {
                val view = layoutInflater.inflate(R.layout.list_item, parent, false)
                val parent: LinearLayout = view.findViewById(R.id.list_item_ll)
                val titleView: TextView = view.findViewById(R.id.item_text_title)
                val panButton: Button = view.findViewById(R.id.item_button_pan)
                val panText: TextView = view.findViewById(R.id.item_text_pan)
                val seekBar: SeekBar = view.findViewById(R.id.item_seek_volume)
                val viewHolder = ViewHolderItem(
                    parent, titleView, panButton, panText, seekBar//, panView, dialog
                )
                view.tag = viewHolder
                viewHolder to view
            }

            else -> convertView.tag as ViewHolderItem to convertView
        }

        val model = listData[position]
        if (model.volume > 0) {
            viewHolder.parent.setBackgroundColor("#ffc0cb".toColorInt())
        } else {
            viewHolder.parent.setBackgroundColor(Color.WHITE)
        }
        viewHolder.titleView.text = model.data.displayName
        viewHolder.panText.text = model.pan.toString()
        viewHolder.seekBar.progress = (model.volume * 100).toInt()
        viewHolder.seekBar.thumb.setTintList(ColorStateList.valueOf(colors[position % 10].toArgb()))
        viewHolder.seekBar.progressTintList = ColorStateList.valueOf(colors[position % 10].toArgb())

        viewHolder.panButton.setOnClickListener {
            val panView = layoutInflater.inflate(R.layout.dialog_pan, parent, false)
            val panTextView: TextView = panView.findViewById(R.id.dialog_text_pan_value)
            val panSeekView: SeekBar = panView.findViewById(R.id.dialog_seek_pan)
            val panResetButton: Button = panView.findViewById(R.id.dialog_button_reset)
            panResetButton.setOnClickListener {
                panTextView.text = "0.0"
                panSeekView.progress = 50

                listData[position].pan = 0.0f
                setMediaPlayer(position, listData[position])
            }
            if (model.pan != 0f) {
                panTextView.text = model.pan.toString()
                panSeekView.progress = (model.pan * 50f + 50).toInt()
            } else {
                panTextView.text = "0.0"
                panSeekView.progress = 50
            }
            panSeekView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        var panValue = 0.0f
                        if (progress == 50) {
                            panTextView.text = panValue.toString()
                        } else {
                            panValue = (progress - 50).toFloat() / 50f
                            panTextView.text = panValue.toString()
                        }
                        listData[position].pan = panValue
                        setMediaPlayer(position, listData[position])
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}

            })
            val dialog = AlertDialog.Builder(context)
                .setView(panView)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val progress = panSeekView.progress
                    if (progress == 50) {
                        viewHolder.panText.text = "0.0"
                        listData[position].pan = 0.0f
                        Log.d("@@@test", "$position is 0f")
                        setMediaPlayer(position, listData[position])
                    } else {
                        val panValue = (progress - 50).toFloat() / 50f
                        viewHolder.panText.text = panValue.toString()
                        listData[position].pan = panValue
                        setMediaPlayer(position, listData[position])
                    }
                }
                .create()
            dialog.show()
        }
        viewHolder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                if (progress != 0) {
                    viewHolder.parent.setBackgroundColor(Color.parseColor("#ffc0cb"))
                    viewHolder.titleView.typeface = Typeface.DEFAULT_BOLD
                    listData[position].volume = (progress / 100f)
                } else {
                    viewHolder.parent.setBackgroundColor(Color.WHITE)
                    viewHolder.titleView.typeface = Typeface.DEFAULT_BOLD
                    listData[position].volume = 0f
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 指を離した時点の値を1回だけ反映
                setMediaPlayer(position, listData[position])
            }
        })

        return view
    }

    private fun setMediaPlayer(position: Int, model: SliderModel) {
        // 再生中でなくても、setVolume() 呼び出しは許可
        BackgroundService.mediaPlayerList.getOrNull(position)?.setVolume(model.volume, model.pan)
    }
}