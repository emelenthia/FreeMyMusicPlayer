package com.enzo.freemymusicplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enzo.freemymusicplayer.adapter.RankingAdapter
import com.enzo.freemymusicplayer.adapter.RankingSong
import com.enzo.freemymusicplayer.databinding.FragmentRankingBinding
import com.enzo.freemymusicplayer.service.MusicScanner
import com.enzo.freemymusicplayer.service.PlayCountManager
import kotlinx.coroutines.launch

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!
    private lateinit var playCountManager: PlayCountManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        playCountManager = PlayCountManager(requireContext())
        setupRecyclerView()
        loadRanking()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewRanking.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadRanking() {
        lifecycleScope.launch {
            val scanner = MusicScanner(requireContext())
            val musicFolders = scanner.scanMusicFolders()
            
            // 全楽曲を再生回数と共に取得
            val allSongsWithPlayCount = musicFolders.flatMap { folder ->
                folder.songs.map { song ->
                    RankingSong(
                        rank = 0, // 後で設定
                        song = song,
                        folderName = folder.getDisplayName(),
                        playCount = playCountManager.getPlayCount(song.id)
                    )
                }
            }
            
            // 再生回数でソートして1回以上のもののみ取得、ランキング設定
            val rankedSongs = allSongsWithPlayCount
                .filter { it.playCount > 0 }
                .sortedByDescending { it.playCount }
                .mapIndexed { index, song ->
                    song.copy(rank = index + 1)
                }

            val adapter = RankingAdapter(rankedSongs)
            binding.recyclerViewRanking.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}