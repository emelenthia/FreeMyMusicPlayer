package com.enzo.freemymusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.enzo.freemymusicplayer.controller.MusicPlayerController
import com.enzo.freemymusicplayer.databinding.ActivityMainBinding
import com.enzo.freemymusicplayer.adapter.SongAdapter
import com.enzo.freemymusicplayer.adapter.BrowserAdapter
import com.enzo.freemymusicplayer.service.MusicScanner
import com.enzo.freemymusicplayer.model.Song
import com.enzo.freemymusicplayer.model.MusicFolder
import com.enzo.freemymusicplayer.model.BrowserItem
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var musicController: MusicPlayerController
    private lateinit var songAdapter: SongAdapter
    private lateinit var browserAdapter: BrowserAdapter
    private var songs = mutableListOf<Song>()
    private var folders = mutableListOf<MusicFolder>()
    private var browserItems = mutableListOf<BrowserItem>()
    private var currentFolder: MusicFolder? = null
    private var isInFolderView = false
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadMusicFiles()
        } else {
            Toast.makeText(this, "音楽ファイルへのアクセス許可が必要です", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupMusicController()
        setupBackPressedHandler()
        checkAndRequestPermissions()
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter(songs) { song, position ->
            musicController.playPlaylist(songs, position)
        }
        
        browserAdapter = BrowserAdapter(browserItems) { item, position ->
            handleBrowserItemClick(item, position)
        }
        
        binding.recyclerViewSongs.apply {
            adapter = browserAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }
    
    private fun setupMusicController() {
        musicController = MusicPlayerController(this)
        
        musicController.playerState.observe(this) { playerState ->
            playerState.currentSong?.let { currentSong ->
                binding.textCurrentSong.text = currentSong.getDisplayTitle()
                binding.textCurrentArtist.text = currentSong.getDisplayArtist()
            }
            
            binding.buttonPlay.setImageResource(
                if (playerState.playbackState == com.enzo.freemymusicplayer.model.PlaybackState.PLAYING)
                    android.R.drawable.ic_media_pause
                else
                    android.R.drawable.ic_media_play
            )
            
            binding.seekBar.max = (playerState.duration / 1000).toInt()
            binding.seekBar.progress = (playerState.position / 1000).toInt()
        }
        
        setupPlaybackControls()
    }
    
    private fun setupPlaybackControls() {
        binding.buttonPlay.setOnClickListener {
            val currentState = musicController.playerState.value?.playbackState
            if (currentState == com.enzo.freemymusicplayer.model.PlaybackState.PLAYING) {
                musicController.pause()
            } else {
                musicController.play()
            }
        }
        
        binding.buttonPrevious.setOnClickListener {
            musicController.seekToPrevious()
        }
        
        binding.buttonNext.setOnClickListener {
            musicController.seekToNext()
        }
        
        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicController.seekTo(progress * 1000L)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }
    
    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            loadMusicFiles()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
    
    private fun loadMusicFiles() {
        lifecycleScope.launch {
            val scanner = MusicScanner(this@MainActivity)
            val musicFolders = scanner.scanMusicFolders()
            
            folders.clear()
            folders.addAll(musicFolders)
            
            showFolderList()
            
            if (folders.isNotEmpty()) {
                binding.textStatus.text = "${folders.size} フォルダが見つかりました"
            } else {
                binding.textStatus.text = "音楽フォルダが見つかりませんでした"
            }
        }
    }
    
    private fun showFolderList() {
        isInFolderView = false
        currentFolder = null
        browserItems.clear()
        
        folders.forEach { folder ->
            browserItems.add(
                BrowserItem(
                    type = BrowserItem.ItemType.FOLDER,
                    name = folder.getDisplayName(),
                    path = folder.path,
                    songCount = folder.songCount,
                    folder = folder
                )
            )
        }
        
        browserAdapter.notifyDataSetChanged()
    }
    
    private fun showSongsInFolder(folder: MusicFolder) {
        isInFolderView = true
        currentFolder = folder
        browserItems.clear()
        
        // 戻るボタンを追加
        browserItems.add(
            BrowserItem(
                type = BrowserItem.ItemType.BACK_BUTTON,
                name = "戻る",
                path = ""
            )
        )
        
        // フォルダ内の楽曲を追加
        folder.songs.forEach { song ->
            browserItems.add(
                BrowserItem(
                    type = BrowserItem.ItemType.SONG,
                    name = song.getDisplayTitle(),
                    path = song.data,
                    song = song
                )
            )
        }
        
        browserAdapter.notifyDataSetChanged()
        binding.textStatus.text = "${folder.getDisplayName()} - ${folder.songCount} 曲"
    }
    
    private fun handleBrowserItemClick(item: BrowserItem, position: Int) {
        when (item.type) {
            BrowserItem.ItemType.BACK_BUTTON -> {
                showFolderList()
                binding.textStatus.text = "${folders.size} フォルダが見つかりました"
            }
            BrowserItem.ItemType.FOLDER -> {
                item.folder?.let { folder ->
                    showSongsInFolder(folder)
                }
            }
            BrowserItem.ItemType.SONG -> {
                item.song?.let { song ->
                    currentFolder?.let { folder ->
                        songs.clear()
                        songs.addAll(folder.songs)
                        val songIndex = folder.songs.indexOf(song)
                        if (songIndex >= 0) {
                            musicController.playPlaylist(songs, songIndex)
                        }
                    }
                }
            }
        }
    }
    
    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isInFolderView) {
                    showFolderList()
                    binding.textStatus.text = "${folders.size} フォルダが見つかりました"
                } else {
                    finish()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        musicController.release()
    }
}