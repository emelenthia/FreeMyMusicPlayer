package com.enzo.freemymusicplayer.controller

import android.content.ComponentName
import android.content.Context
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
    
    private var currentPlaylist: List<Song> = emptyList()
    private var currentIndex: Int = -1
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
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentIndex()
                updatePlayerState()
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
        currentPlaylist = songs
        currentIndex = startIndex
        
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .build()
        }
        
        mediaController?.let { controller ->
            controller.setMediaItems(mediaItems, startIndex, 0)
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
        mediaController?.seekToNext()
    }
    
    fun seekToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun toggleRepeatMode() {
        val currentMode = _playerState.value?.repeatMode ?: RepeatMode.NONE
        val newMode = when (currentMode) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
        
        val repeatMode = when (newMode) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        
        mediaController?.repeatMode = repeatMode
        updatePlayerState()
    }
    
    fun toggleShuffle() {
        val currentShuffle = _playerState.value?.isShuffleEnabled ?: false
        mediaController?.shuffleModeEnabled = !currentShuffle
        updatePlayerState()
    }
    
    private fun updateCurrentIndex() {
        currentIndex = mediaController?.currentMediaItemIndex ?: -1
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
        
        val repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> RepeatMode.NONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.NONE
        }
        
        val currentSong = if (currentIndex >= 0 && currentIndex < currentPlaylist.size) {
            currentPlaylist[currentIndex]
        } else null
        
        val newState = currentState.copy(
            currentSong = currentSong,
            playlist = currentPlaylist,
            currentIndex = currentIndex,
            playbackState = playbackState,
            position = controller.currentPosition,
            duration = controller.duration.coerceAtLeast(0),
            isShuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = repeatMode
        )
        
        _playerState.postValue(newState)
    }
    
    fun release() {
        positionUpdateJob?.cancel()
        mediaController?.release()
    }
}