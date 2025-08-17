package com.enzo.freemymusicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enzo.freemymusicplayer.ThemeHelper
import com.enzo.freemymusicplayer.databinding.ItemFolderGroupBinding

class FolderGroupAdapter(
    private val folderGroups: List<FolderGroup>
) : RecyclerView.Adapter<FolderGroupAdapter.FolderGroupViewHolder>() {

    class FolderGroupViewHolder(
        private val binding: ItemFolderGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folderGroup: FolderGroup) {
            val context = binding.root.context
            val backgroundColor = ThemeHelper.getBackgroundColor(context)
            val textColor = ThemeHelper.getContrastTextColor(context)
            val displaySize = ThemeHelper.getDisplaySize(context)

            // フォルダ名を設定
            binding.textFolderName.text = folderGroup.folderName
            
            // フォルダ名の色を少し薄く
            val folderTextColor = android.graphics.Color.argb(
                (android.graphics.Color.alpha(textColor) * 0.9f).toInt(),
                android.graphics.Color.red(textColor),
                android.graphics.Color.green(textColor),
                android.graphics.Color.blue(textColor)
            )
            binding.textFolderName.setTextColor(folderTextColor)
            binding.textFolderName.textSize = displaySize.songTitleSize

            // テーマカラーを適用
            binding.root.setCardBackgroundColor(backgroundColor)

            // 楽曲リストを設定
            val songsAdapter = PlayCountAdapter(folderGroup.songs)
            binding.recyclerViewSongs.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = songsAdapter
                isNestedScrollingEnabled = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderGroupViewHolder {
        val binding = ItemFolderGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderGroupViewHolder, position: Int) {
        holder.bind(folderGroups[position])
    }

    override fun getItemCount(): Int = folderGroups.size
}