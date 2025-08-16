package com.enzo.freemymusicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            binding.textItemName.text = item.getDisplayName()
            binding.textItemInfo.text = item.getDisplayInfo()
            
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