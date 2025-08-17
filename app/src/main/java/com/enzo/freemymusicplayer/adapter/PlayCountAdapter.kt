package com.enzo.freemymusicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enzo.freemymusicplayer.ThemeHelper
import com.enzo.freemymusicplayer.databinding.ItemFolderGroupBinding
import com.enzo.freemymusicplayer.databinding.ItemPlayCountBinding
import com.enzo.freemymusicplayer.model.Song

data class SongWithPlayCount(
    val song: Song,
    val playCount: Int
)

data class FolderGroup(
    val folderName: String,
    val songs: List<SongWithPlayCount>
)

class PlayCountAdapter(
    private val items: List<SongWithPlayCount>
) : RecyclerView.Adapter<PlayCountAdapter.PlayCountViewHolder>() {

    class PlayCountViewHolder(
        private val binding: ItemPlayCountBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SongWithPlayCount) {
            val context = binding.root.context
            val lighterColor = ThemeHelper.getLighterThemeColor(context)
            val textColor = ThemeHelper.getContrastTextColor(context)
            val displaySize = ThemeHelper.getDisplaySize(context)
            val showArtist = ThemeHelper.getShowArtist(context)

            binding.textSongTitle.text = item.song.getDisplayTitle()
            
            if (showArtist) {
                binding.textSongArtist.text = item.song.getDisplayArtist()
                binding.textSongArtist.visibility = android.view.View.VISIBLE
                binding.textContainer.gravity = android.view.Gravity.TOP
            } else {
                binding.textSongArtist.visibility = android.view.View.GONE
                binding.textContainer.gravity = android.view.Gravity.CENTER_VERTICAL
            }

            binding.textPlayCountValue.text = "${item.playCount}回"

            // テーマカラーを適用
            binding.root.setCardBackgroundColor(lighterColor)
            binding.root.elevation = 0f
            
            // 文字色を薄くして圧迫感を軽減
            val softTextColor = android.graphics.Color.argb(
                (android.graphics.Color.alpha(textColor) * 0.8f).toInt(),
                android.graphics.Color.red(textColor),
                android.graphics.Color.green(textColor),
                android.graphics.Color.blue(textColor)
            )
            binding.textSongTitle.setTextColor(softTextColor)
            binding.textSongArtist.setTextColor(softTextColor)
            binding.textPlayCountValue.setTextColor(textColor)

            // サイズ設定を適用
            binding.textSongTitle.textSize = displaySize.songTitleSize
            binding.textSongArtist.textSize = displaySize.songArtistSize
            binding.textPlayCountValue.textSize = displaySize.songArtistSize

            // パディング調整
            val density = context.resources.displayMetrics.density
            val paddingPx = (displaySize.itemPadding * density).toInt()
            
            if (showArtist) {
                binding.linearLayoutContent.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            } else {
                val smallPaddingPx = (paddingPx * 0.5f).toInt()
                binding.linearLayoutContent.setPadding(paddingPx, smallPaddingPx, paddingPx, smallPaddingPx)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayCountViewHolder {
        val binding = ItemPlayCountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayCountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayCountViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}