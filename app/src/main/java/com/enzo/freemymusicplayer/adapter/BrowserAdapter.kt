package com.enzo.freemymusicplayer.adapter

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enzo.freemymusicplayer.ThemeHelper
import com.enzo.freemymusicplayer.databinding.ItemBrowserBinding
import com.enzo.freemymusicplayer.model.BrowserItem

class BrowserAdapter(
    private val items: List<BrowserItem>,
    private val onItemClick: (BrowserItem, Int) -> Unit
) : RecyclerView.Adapter<BrowserAdapter.BrowserViewHolder>() {

    class BrowserViewHolder(
        private val binding: ItemBrowserBinding,
        private val onItemClick: (BrowserItem, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BrowserItem, position: Int) {
            val context = binding.root.context
            val lighterColor = ThemeHelper.getLighterThemeColor(context)
            val textColor = ThemeHelper.getContrastTextColor(context)
            val displaySize = ThemeHelper.getDisplaySize(context)
            
            binding.textItemName.text = item.getDisplayName()
            binding.textItemInfo.text = item.getDisplayInfo()
            
            // テーマカラーを適用（CardViewの背景色を設定）
            binding.root.setCardBackgroundColor(lighterColor)
            binding.root.elevation = 0f
            binding.textItemName.setTextColor(textColor)
            binding.textItemInfo.setTextColor(textColor)
            
            // サイズ設定を適用
            binding.textItemName.textSize = displaySize.songTitleSize
            binding.textItemInfo.textSize = displaySize.songArtistSize
            
            // アイコンサイズを適用（dpをpxに変換）
            val density = context.resources.displayMetrics.density
            val iconSizePx = (displaySize.iconSize * density).toInt()
            val paddingPx = (displaySize.itemPadding * density).toInt()
            
            val iconLayoutParams = binding.imageIcon.layoutParams
            iconLayoutParams.width = iconSizePx
            iconLayoutParams.height = iconSizePx
            binding.imageIcon.layoutParams = iconLayoutParams
            
            // LinearLayoutのパディングを調整
            binding.linearLayoutContent.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
            
            // アイコンを設定
            val iconResource = when (item.type) {
                BrowserItem.ItemType.BACK_BUTTON -> android.R.drawable.ic_menu_revert
                BrowserItem.ItemType.FOLDER -> android.R.drawable.ic_menu_agenda
                BrowserItem.ItemType.SONG -> android.R.drawable.ic_media_play
            }
            binding.imageIcon.setImageResource(iconResource)
            
            binding.root.setOnClickListener {
                onItemClick(item, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowserViewHolder {
        val binding = ItemBrowserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BrowserViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: BrowserViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}