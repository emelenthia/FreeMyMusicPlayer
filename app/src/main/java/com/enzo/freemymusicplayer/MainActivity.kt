package com.enzo.freemymusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var musicController: MusicPlayerController
    private lateinit var songAdapter: SongAdapter
    private lateinit var browserAdapter: BrowserAdapter
    private var songs = mutableListOf<Song>()
    private var folders = mutableListOf<MusicFolder>()
    private var browserItems = mutableListOf<BrowserItem>()
    private var currentFolder: MusicFolder? = null
    private var isInFolderView = false
    private var scrollPosition = 0
    private var scrollOffset = 0
    
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
        ThemeHelper.applyTheme(this)
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
                
                // 再生回数を表示
                val playCount = musicController.getPlayCount(currentSong.id)
                binding.textPlayCount.text = if (playCount > 0) "♪$playCount" else ""
                
                val showArtist = ThemeHelper.getShowArtist(this)
                if (showArtist) {
                    binding.textCurrentArtist.text = currentSong.getDisplayArtist()
                    binding.textCurrentArtist.visibility = android.view.View.VISIBLE
                    // 通常の位置
                    adjustPlayerTextPosition(false)
                } else {
                    binding.textCurrentArtist.visibility = android.view.View.GONE
                    // 中央配置
                    adjustPlayerTextPosition(true)
                }
            }
            
            binding.buttonPlay.setImageResource(
                if (playerState.playbackState == com.enzo.freemymusicplayer.model.PlaybackState.PLAYING)
                    android.R.drawable.ic_media_pause
                else
                    android.R.drawable.ic_media_play
            )
            
            // シャッフルボタンの状態を更新
            binding.buttonShuffle.alpha = if (playerState.isShuffleEnabled) 1.0f else 0.5f
            
            // リピートボタンの状態を更新
            when (playerState.repeatMode) {
                com.enzo.freemymusicplayer.model.RepeatMode.NONE -> {
                    binding.buttonRepeat.alpha = 0.5f
                    binding.buttonRepeat.setImageResource(android.R.drawable.ic_menu_rotate)
                }
                com.enzo.freemymusicplayer.model.RepeatMode.ALL -> {
                    binding.buttonRepeat.alpha = 1.0f
                    binding.buttonRepeat.setImageResource(android.R.drawable.ic_menu_rotate)
                }
                com.enzo.freemymusicplayer.model.RepeatMode.ONE -> {
                    binding.buttonRepeat.alpha = 1.0f
                    binding.buttonRepeat.setImageResource(android.R.drawable.ic_menu_revert)
                }
            }
            
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
        
        binding.buttonShuffle.setOnClickListener {
            musicController.toggleShuffle()
        }
        
        binding.buttonRepeat.setOnClickListener {
            musicController.toggleRepeatMode()
        }
        
        binding.buttonRestart.setOnClickListener {
            musicController.seekTo(0L)
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
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
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
        
        // 以前のスクロール位置を復元
        val layoutManager = binding.recyclerViewSongs.layoutManager as LinearLayoutManager
        layoutManager.scrollToPositionWithOffset(scrollPosition, scrollOffset)
    }
    
    private fun showSongsInFolder(folder: MusicFolder) {
        // フォルダリストのスクロール位置を保存
        val layoutManager = binding.recyclerViewSongs.layoutManager as LinearLayoutManager
        scrollPosition = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleView = layoutManager.findViewByPosition(scrollPosition)
        scrollOffset = firstVisibleView?.top ?: 0
        
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        
        // メニューアイコンの色を設定
        val themeColor = ThemeHelper.getThemeColor(this)
        val iconColor = if (themeColor == android.graphics.Color.WHITE) {
            android.graphics.Color.BLACK
        } else {
            android.graphics.Color.WHITE
        }
        
        menu?.let {
            for (i in 0 until it.size()) {
                val menuItem = it.getItem(i)
                menuItem.icon?.setTint(iconColor)
            }
        }
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Log.d("MainActivity", "Settings menu clicked")
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun applyThemeColors() {
        val themeColor = ThemeHelper.getThemeColor(this)
        val lighterColor = ThemeHelper.getLighterThemeColor(this)
        val backgroundColor = ThemeHelper.getBackgroundColor(this)
        val textColorForLighter = ThemeHelper.getContrastTextColor(this)
        val displaySize = ThemeHelper.getDisplaySize(this)
        
        // プレイヤーコントロールの文字色（濃いテーマカラーに対するコントラスト）
        val themeColorLuminance = androidx.core.graphics.ColorUtils.calculateLuminance(themeColor)
        val playerTextColor = if (themeColorLuminance > 0.5) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        
        // 全体の背景色を適用
        binding.root.setBackgroundColor(backgroundColor)
        
        // プレイヤーコントロール部分にテーマカラーを適用
        binding.playerControls.background = ColorDrawable(themeColor)
        
        // 曲情報の文字色とサイズを適用（プレイヤーコントロール背景に対するコントラスト）
        binding.textCurrentSong.setTextColor(playerTextColor)
        binding.textCurrentSong.textSize = displaySize.playerTitleSize
        binding.textCurrentArtist.setTextColor(playerTextColor)
        binding.textCurrentArtist.textSize = displaySize.playerArtistSize
        
        // 再生回数の色とサイズを適用
        binding.textPlayCount.setTextColor(playerTextColor)
        binding.textPlayCount.textSize = displaySize.songArtistSize * 0.8f
        
        // topBarに薄いテーマカラーを適用
        binding.topBar.background = ColorDrawable(lighterColor)
        
        // ステータステキストの色とサイズを適用（薄いテーマカラーに対するコントラスト）
        binding.textStatus.setTextColor(textColorForLighter)
        binding.textStatus.textSize = displaySize.statusTextSize
        
        // RecyclerViewの背景色設定はapplySkinBackground()内で行うため、ここでは削除
        
        // アダプターを更新してテーマカラーを反映
        browserAdapter.notifyDataSetChanged()
        
    }

    override fun onResume() {
        super.onResume()
        ThemeHelper.applyTheme(this)
        applyThemeColors()
    }

    private fun adjustPlayerTextPosition(centerSong: Boolean) {
        if (centerSong) {
            // アーティスト非表示時：曲名を再生エリア上部とSeekBarの中間に配置
            val layoutParams = binding.textCurrentSong.layoutParams as android.view.ViewGroup.MarginLayoutParams
            layoutParams.topMargin = (2 * resources.displayMetrics.density).toInt()
            layoutParams.bottomMargin = (24 * resources.displayMetrics.density).toInt()
            binding.textCurrentSong.layoutParams = layoutParams
            binding.textCurrentSong.gravity = android.view.Gravity.CENTER
        } else {
            // アーティスト表示時：通常の配置
            val layoutParams = binding.textCurrentSong.layoutParams as android.view.ViewGroup.MarginLayoutParams
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = (2 * resources.displayMetrics.density).toInt()
            binding.textCurrentSong.layoutParams = layoutParams
            binding.textCurrentSong.gravity = android.view.Gravity.CENTER
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicController.release()
    }
}