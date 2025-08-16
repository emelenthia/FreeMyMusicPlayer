package com.enzo.freemymusicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enzo.freemymusicplayer.databinding.ItemSongBinding
import com.enzo.freemymusicplayer.model.Song

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song, Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(
        private val binding: ItemSongBinding,
        private val onSongClick: (Song, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, position: Int) {
            binding.textSongTitle.text = song.getDisplayTitle()
            binding.textSongArtist.text = song.getDisplayArtist()
            binding.textSongDuration.text = song.getFormattedDuration()
            binding.textSongAlbum.text = song.getDisplayAlbum()
            
            binding.root.setOnClickListener {
                onSongClick(song, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding, onSongClick)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], position)
    }

    override fun getItemCount(): Int = songs.size
}