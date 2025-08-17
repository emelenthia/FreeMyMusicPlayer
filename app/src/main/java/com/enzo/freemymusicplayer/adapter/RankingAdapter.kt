package com.enzo.freemymusicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enzo.freemymusicplayer.ThemeHelper
import com.enzo.freemymusicplayer.databinding.ItemRankingBinding
import com.enzo.freemymusicplayer.model.Song

data class RankingSong(
    val rank: Int,
    val song: Song,
    val folderName: String,
    val playCount: Int
)

class RankingAdapter(
    private val items: List<RankingSong>
) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    class RankingViewHolder(
        private val binding: ItemRankingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RankingSong) {
            val context = binding.root.context
            val lighterColor = ThemeHelper.getLighterThemeColor(context)
            val textColor = ThemeHelper.getContrastTextColor(context)
            val displaySize = ThemeHelper.getDisplaySize(context)
            val showArtist = ThemeHelper.getShowArtist(context)

            // ランキング順位を表示
            binding.textRank.text = item.rank.toString()
            
            binding.textSongTitle.text = item.song.getDisplayTitle()
            binding.textFolderName.text = item.folderName
            
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
            
            // ランキング上位は強調
            val rankColor = when (item.rank) {
                1 -> android.graphics.Color.parseColor("#FFD700") // 金色
                2 -> android.graphics.Color.parseColor("#C0C0C0") // 銀色
                3 -> android.graphics.Color.parseColor("#CD7F32") // 銅色
                else -> textColor
            }
            
            binding.textRank.setTextColor(rankColor)
            binding.textSongTitle.setTextColor(softTextColor)
            binding.textSongArtist.setTextColor(softTextColor)
            binding.textFolderName.setTextColor(softTextColor)
            binding.textPlayCountValue.setTextColor(textColor)

            // サイズ設定を適用
            binding.textRank.textSize = displaySize.songTitleSize
            binding.textSongTitle.textSize = displaySize.songTitleSize
            binding.textSongArtist.textSize = displaySize.songArtistSize
            binding.textFolderName.textSize = displaySize.songArtistSize * 0.9f
            binding.textPlayCountValue.textSize = displaySize.songArtistSize

            // パディング調整
            val density = context.resources.displayMetrics.density
            val paddingPx = (displaySize.itemPadding * density).toInt()
            
            if (showArtist) {
                binding.linearLayoutContent.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            } else {
                val smallPaddingPx = (paddingPx * 0.7f).toInt()
                binding.linearLayoutContent.setPadding(paddingPx, smallPaddingPx, paddingPx, smallPaddingPx)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val binding = ItemRankingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RankingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}