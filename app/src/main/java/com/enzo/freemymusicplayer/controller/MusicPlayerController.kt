package com.enzo.freemymusicplayer.controller

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.enzo.freemymusicplayer.model.PlaybackState
import com.enzo.freemymusicplayer.model.PlayerState
import com.enzo.freemymusicplayer.model.RepeatMode
import com.enzo.freemymusicplayer.model.Song
import com.enzo.freemymusicplayer.model.InternalPlaylist
import com.enzo.freemymusicplayer.service.MusicPlayerService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPlayerController(private val context: Context) {
    
    private var mediaController: MediaController? = null
    private val _playerState = MutableLiveData(PlayerState())
    val playerState: LiveData<PlayerState> = _playerState
    
    private var internalPlaylist: InternalPlaylist? = null
    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    
    init {
        initializeController()
    }
    
    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlayerService::class.java)
        )
        
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            setupPlayerListener()
            startPositionUpdates()
        }, MoreExecutors.directExecutor())
    }
    
    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerState()
                
                // 曲が終了した場合の処理
                if (playbackState == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    handleTrackEnded()
                } else {
                    updateCurrentIndex()
                    updatePlayerState()
                }
            }
        })
    }
    
    private fun startPositionUpdates() {
        positionUpdateJob = scope.launch {
            while (true) {
                updatePosition()
                delay(1000)
            }
        }
    }
    
    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        val currentState = _playerState.value
        val isShuffleEnabled = currentState?.isShuffleEnabled ?: false
        
        // 内部再生リストを作成（辞書順ソート済み）
        internalPlaylist = InternalPlaylist(songs)
        
        // 選択された曲の新しい位置を探す
        val selectedSong = if (startIndex >= 0 && startIndex < songs.size) {
            songs[startIndex]
        } else {
            songs.firstOrNull()
        }
        
        selectedSong?.let { song ->
            val newPosition = internalPlaylist?.findPositionBySong(song) ?: 0
            internalPlaylist?.setCurrentPosition(newPosition)
        }
        
        // シャッフルフラグがONの場合は自動的にシャッフル
        if (isShuffleEnabled) {
            internalPlaylist?.shuffle()
        }
        
        // ExoPlayerに設定（常に元の辞書順）
        val originalSongs = internalPlaylist?.getOriginalSongs() ?: emptyList()
        val mediaItems = originalSongs.map { song ->
            MediaItem.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .build()
        }
        
        mediaController?.let { controller ->
            // ExoPlayerでは選択された曲の元の位置から開始
            val originalIndex = if (isShuffleEnabled) {
                findOriginalIndex(selectedSong)
            } else {
                internalPlaylist?.getCurrentPosition() ?: 0
            }
            
            controller.setMediaItems(mediaItems, originalIndex, 0)
            controller.repeatMode = Player.REPEAT_MODE_OFF
            controller.prepare()
            controller.play()
        }
        
        updatePlayerState()
    }
    
    fun play() {
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun stop() {
        mediaController?.stop()
    }
    
    fun seekToNext() {
        val playlist = internalPlaylist ?: return
        
        // 現在位置を更新
        updateCurrentIndex()
        
        // 常に次の曲、最後なら停止
        val nextSong = if (playlist.hasNext()) {
            playlist.moveToNext()
            playlist.getCurrentItem()?.song
        } else {
            mediaController?.stop()
            return
        }

        val originalIndex = findOriginalIndex(nextSong)
        if (originalIndex >= 0) {
            mediaController?.seekToDefaultPosition(originalIndex)
        }
    }
    
    
    private fun findOriginalIndex(song: Song?): Int {
        song ?: return -1
        val originalSongs = internalPlaylist?.getOriginalSongs() ?: return -1
        return originalSongs.indexOfFirst { it.id == song.id }
    }
    
    
    fun seekToPrevious() {
        val playlist = internalPlaylist ?: return
        
        // 現在位置を更新
        updateCurrentIndex()
        
        // 前の曲があれば前の曲、なければ現在の曲を再生
        val prevSong = if (playlist.hasPrevious()) {
            playlist.moveToPrevious()
            playlist.getCurrentItem()?.song
        } else {
            // 最初の曲の場合は現在の曲を再生
            playlist.getCurrentItem()?.song
        }
        
        val originalIndex = findOriginalIndex(prevSong)
        if (originalIndex >= 0) {
            mediaController?.seekToDefaultPosition(originalIndex)
        }
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun toggleRepeatMode() {
        val currentState = _playerState.value ?: return
        val currentMode = currentState.repeatMode
        val newMode = when (currentMode) {
            RepeatMode.NONE -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.NONE
        }
        
        // 状態を更新
        val newState = currentState.copy(repeatMode = newMode)
        _playerState.postValue(newState)
    }
    
    fun toggleShuffle() {
        val currentState = _playerState.value ?: return
        val newShuffleState = !currentState.isShuffleEnabled
        
        // 内部再生リストがある場合のみ実際の処理を実行
        val playlist = internalPlaylist
        if (playlist != null) {
            if (newShuffleState) {
                // シャッフルON: 内部再生リストをシャッフル（ExoPlayerは変更しない）
                playlist.shuffle()
            } else {
                // シャッフルOFF: 現在の曲以降の辞書順リストに切り替え
                playlist.resetToSequentialFromCurrent()
            }
        }
        
        // 状態を更新（内部リストがなくてもシャッフルフラグは切り替える）
        val newState = currentState.copy(isShuffleEnabled = newShuffleState)
        _playerState.postValue(newState)
    }
    
    
    
    private fun updateCurrentIndex() {
        val mediaIndex = mediaController?.currentMediaItemIndex ?: -1
        if (mediaIndex >= 0) {
            // ExoPlayerの現在のインデックスから、内部リストでの対応する曲を探す
            val playlist = internalPlaylist ?: return
            val originalSongs = playlist.getOriginalSongs()
            val currentSong = if (mediaIndex < originalSongs.size) originalSongs[mediaIndex] else null
            
            // 内部リストでの位置を更新
            currentSong?.let { song ->
                val internalIndex = playlist.findPositionBySong(song)
                if (internalIndex >= 0) {
                    playlist.setCurrentPosition(internalIndex)
                }
            }
        }
    }
    
    private fun handleTrackEnded() {
        val currentState = _playerState.value ?: return
        val playlist = internalPlaylist ?: return
        
        val nextSong = when (currentState.repeatMode) {
            RepeatMode.ONE -> {
                // 単曲リピート: 現在の曲を返す
                playlist.getCurrentItem()?.song
            }
            RepeatMode.ALL -> {
                // 現在位置を更新
                updateCurrentIndex()
                // 全曲リピート: 次の曲、最後なら最初の曲
                if (playlist.hasNext()) {
                    playlist.moveToNext()
                    playlist.getCurrentItem()?.song
                } else {
                    playlist.setCurrentPosition(0)
                    playlist.getCurrentItem()?.song
                }
            }
            RepeatMode.NONE -> {
                // 現在位置を更新
                updateCurrentIndex()
                // リピートなし: 次の曲、最後なら停止
                if (playlist.hasNext()) {
                    playlist.moveToNext()
                    playlist.getCurrentItem()?.song
                } else {
                    mediaController?.stop()
                    return
                }
            }
        }

        val originalIndex = findOriginalIndex(nextSong)
        if (originalIndex >= 0) {
            mediaController?.seekToDefaultPosition(originalIndex)
        }
    }
    
    private fun updatePosition() {
        mediaController?.let { controller ->
            val currentState = _playerState.value ?: return
            val newState = currentState.copy(
                position = controller.currentPosition,
                duration = controller.duration.coerceAtLeast(0)
            )
            _playerState.postValue(newState)
        }
    }
    
    private fun updatePlayerState() {
        val controller = mediaController ?: return
        val currentState = _playerState.value ?: PlayerState()
        
        val playbackState = when (controller.playbackState) {
            Player.STATE_IDLE -> PlaybackState.IDLE
            Player.STATE_BUFFERING -> PlaybackState.BUFFERING
            Player.STATE_READY -> if (controller.playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSED
            Player.STATE_ENDED -> PlaybackState.STOPPED
            else -> PlaybackState.IDLE
        }
        
        val currentSong = internalPlaylist?.getCurrentItem()?.song
        val playlist = internalPlaylist?.getAllSongs() ?: emptyList()
        val currentIndex = internalPlaylist?.getCurrentPosition() ?: -1
        val isShuffled = currentState.isShuffleEnabled
        
        val newState = currentState.copy(
            currentSong = currentSong,
            playlist = playlist,
            currentIndex = currentIndex,
            playbackState = playbackState,
            position = controller.currentPosition,
            duration = controller.duration.coerceAtLeast(0),
            isShuffleEnabled = isShuffled,
            repeatMode = currentState.repeatMode, // リピート状態を保持
            shufflePlaylist = null
        )
        
        _playerState.postValue(newState)
    }
    
    fun release() {
        positionUpdateJob?.cancel()
        mediaController?.release()
    }
}