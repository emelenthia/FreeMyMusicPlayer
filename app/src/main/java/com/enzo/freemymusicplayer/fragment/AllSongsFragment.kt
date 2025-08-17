package com.enzo.freemymusicplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enzo.freemymusicplayer.adapter.FolderGroup
import com.enzo.freemymusicplayer.adapter.FolderGroupAdapter
import com.enzo.freemymusicplayer.adapter.SongWithPlayCount
import com.enzo.freemymusicplayer.databinding.FragmentAllSongsBinding
import com.enzo.freemymusicplayer.service.MusicScanner
import com.enzo.freemymusicplayer.service.PlayCountManager
import kotlinx.coroutines.launch

class AllSongsFragment : Fragment() {

    private var _binding: FragmentAllSongsBinding? = null
    private val binding get() = _binding!!
    private lateinit var playCountManager: PlayCountManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        playCountManager = PlayCountManager(requireContext())
        setupRecyclerView()
        loadAllSongs()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAllSongs.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadAllSongs() {
        lifecycleScope.launch {
            val scanner = MusicScanner(requireContext())
            val musicFolders = scanner.scanMusicFolders()
            
            // フォルダ毎にグループ化
            val folderGroups = musicFolders.map { folder ->
                val songsWithPlayCount = folder.songs.map { song ->
                    SongWithPlayCount(
                        song = song,
                        playCount = playCountManager.getPlayCount(song.id)
                    )
                }
                FolderGroup(
                    folderName = folder.getDisplayName(),
                    songs = songsWithPlayCount
                )
            }

            val adapter = FolderGroupAdapter(folderGroups)
            binding.recyclerViewAllSongs.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}