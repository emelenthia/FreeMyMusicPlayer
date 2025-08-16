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
    val repeatMode: RepeatMode = RepeatMode.NONE
) {
    fun hasNext(): Boolean {
        return when (repeatMode) {
            RepeatMode.ALL -> true
            RepeatMode.ONE -> true
            RepeatMode.NONE -> currentIndex < playlist.size - 1
        }
    }
    
    fun hasPrevious(): Boolean {
        return when (repeatMode) {
            RepeatMode.ALL -> true
            RepeatMode.ONE -> true
            RepeatMode.NONE -> currentIndex > 0
        }
    }
}