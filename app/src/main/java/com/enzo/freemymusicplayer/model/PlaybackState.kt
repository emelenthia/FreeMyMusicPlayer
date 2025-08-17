package com.enzo.freemymusicplayer.model

enum class PlaybackState {
    IDLE,
    PLAYING,
    PAUSED,
    STOPPED,
    BUFFERING,
    ERROR
}

enum class RepeatMode {
    NONE,
    ONE,
    ALL
}

data class PlayerState(
    val currentSong: Song? = null,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val position: Long = 0L,
    val duration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val shufflePlaylist: ShufflePlaylist? = null
) {
    fun hasNext(): Boolean {
        return when (repeatMode) {
            RepeatMode.ALL -> true
            RepeatMode.ONE -> true
            RepeatMode.NONE -> {
                if (isShuffleEnabled) {
                    shufflePlaylist?.hasNext() ?: false
                } else {
                    currentIndex < playlist.size - 1
                }
            }
        }
    }
    
    fun hasPrevious(): Boolean {
        return when (repeatMode) {
            RepeatMode.ALL -> true
            RepeatMode.ONE -> true
            RepeatMode.NONE -> {
                if (isShuffleEnabled) {
                    shufflePlaylist?.hasPrevious() ?: false
                } else {
                    currentIndex > 0
                }
            }
        }
    }
    
    fun getNextIndex(): Int {
        return when {
            repeatMode == RepeatMode.ONE -> currentIndex
            isShuffleEnabled -> shufflePlaylist?.getNextIndex() ?: -1
            else -> getSequentialNextIndex()
        }
    }
    
    fun getPreviousIndex(): Int {
        return when {
            repeatMode == RepeatMode.ONE -> currentIndex
            isShuffleEnabled -> shufflePlaylist?.getPreviousIndex() ?: currentIndex
            else -> getSequentialPreviousIndex()
        }
    }
    
    private fun getSequentialNextIndex(): Int {
        return when (repeatMode) {
            RepeatMode.ALL -> (currentIndex + 1) % playlist.size
            RepeatMode.NONE -> minOf(currentIndex + 1, playlist.size - 1)
            RepeatMode.ONE -> currentIndex
        }
    }
    
    private fun getSequentialPreviousIndex(): Int {
        return when (repeatMode) {
            RepeatMode.ALL -> if (currentIndex <= 0) playlist.size - 1 else currentIndex - 1
            RepeatMode.NONE -> maxOf(currentIndex - 1, 0)
            RepeatMode.ONE -> currentIndex
        }
    }
}