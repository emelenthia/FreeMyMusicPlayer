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
            
            // アイコンサイズを適用（dpをpxに変換）
            val density = context.resources.displayMetrics.density
            val iconSizePx = (displaySize.iconSize * density).toInt()
            val paddingPx = (displaySize.itemPadding * density).toInt()
            
            val showArtist = ThemeHelper.getShowArtist(context)
            
            // 戻るボタンは常に小さくコンパクトに
            if (item.type == BrowserItem.ItemType.BACK_BUTTON) {
                binding.textItemInfo.visibility = android.view.View.GONE
                val backButtonPaddingPx = (8 * density).toInt()
                binding.linearLayoutContent.setPadding(paddingPx, backButtonPaddingPx, paddingPx, backButtonPaddingPx)
                binding.textContainer.gravity = android.view.Gravity.CENTER_VERTICAL
                val backButtonHeight = (40 * density).toInt()
                binding.linearLayoutContent.minimumHeight = backButtonHeight
            } else if (showArtist || item.type != BrowserItem.ItemType.SONG) {
                binding.textItemInfo.text = item.getDisplayInfo()
                binding.textItemInfo.visibility = android.view.View.VISIBLE
                // アーティスト表示時は通常のパディング
                binding.linearLayoutContent.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                // テキストコンテナを上揃えに（2行表示用）
                binding.textContainer.gravity = android.view.Gravity.TOP
                // LinearLayoutの高さを通常に戻す
                binding.linearLayoutContent.minimumHeight = 0
            } else {
                binding.textItemInfo.visibility = android.view.View.GONE
                // アーティスト非表示時はパディングを大幅に縮小
                val smallPaddingPx = (paddingPx * 0.5f).toInt()
                binding.linearLayoutContent.setPadding(paddingPx, smallPaddingPx, paddingPx, smallPaddingPx)
                // テキストコンテナを中央配置（1行表示用）
                binding.textContainer.gravity = android.view.Gravity.CENTER_VERTICAL
                // LinearLayoutの最小高さを設定してコンパクトに
                val compactHeight = (48 * density).toInt()
                binding.linearLayoutContent.minimumHeight = compactHeight
            }
            
            // テーマカラーを適用（CardViewの背景色を設定）
            binding.root.setCardBackgroundColor(lighterColor)
            binding.root.elevation = 0f
            binding.textItemName.setTextColor(textColor)
            binding.textItemInfo.setTextColor(textColor)
            
            // サイズ設定を適用
            binding.textItemName.textSize = displaySize.songTitleSize
            binding.textItemInfo.textSize = displaySize.songArtistSize
            
            val iconLayoutParams = binding.imageIcon.layoutParams
            iconLayoutParams.width = iconSizePx
            iconLayoutParams.height = iconSizePx
            binding.imageIcon.layoutParams = iconLayoutParams
            
            
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