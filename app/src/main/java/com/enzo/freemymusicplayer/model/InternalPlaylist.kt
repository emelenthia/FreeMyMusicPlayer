package com.enzo.freemymusicplayer.model

data class PlaylistItem(
    val song: Song,
    val isLast: Boolean = false
)

class InternalPlaylist(songs: List<Song>) {
    private var playlist: MutableList<PlaylistItem>
    private var currentPosition = 0
    private var isShuffled = false
    
    init {
        // フォルダ内楽曲を辞書順でソート
        val sortedSongs = songs.sortedBy { it.getDisplayTitle() }
        
        // プレイリストアイテムを作成（最後の曲にフラグを付ける）
        playlist = sortedSongs.mapIndexed { index, song ->
            PlaylistItem(
                song = song,
                isLast = index == sortedSongs.size - 1
            )
        }.toMutableList()
    }
    
    fun getCurrentItem(): PlaylistItem? {
        return if (currentPosition >= 0 && currentPosition < playlist.size) {
            playlist[currentPosition]
        } else null
    }
    
    fun getCurrentPosition(): Int = currentPosition
    
    fun setCurrentPosition(position: Int) {
        if (position >= 0 && position < playlist.size) {
            currentPosition = position
        }
    }
    
    fun hasNext(): Boolean {
        return currentPosition < playlist.size - 1
    }
    
    fun hasPrevious(): Boolean {
        return currentPosition > 0
    }
    
    fun moveToNext(): Boolean {
        return if (hasNext()) {
            currentPosition++
            true
        } else {
            false
        }
    }
    
    fun moveToPrevious(): Boolean {
        return if (hasPrevious()) {
            currentPosition--
            true
        } else {
            false
        }
    }
    
    fun getSize(): Int = playlist.size
    
    fun getAllSongs(): List<Song> = playlist.map { it.song }
    
    fun getOriginalSongs(): List<Song> {
        // 元の辞書順のソートされた曲リストを返す（ExoPlayerのプレイリスト順序）
        return playlist.map { it.song }.sortedBy { it.getDisplayTitle() }
    }
    
    fun findPositionBySong(targetSong: Song): Int {
        return playlist.indexOfFirst { it.song.id == targetSong.id }
    }
    
    fun shuffle() {
        if (playlist.isEmpty()) return
        
        // 現在再生中の曲を保存
        val currentSong = getCurrentItem()?.song
        
        // 現在の曲以外をシャッフル
        val otherSongs = playlist.filter { it.song.id != currentSong?.id }.map { it.song }
        val shuffledOthers = otherSongs.shuffled()
        
        // 新しいプレイリストを作成（現在の曲を最初に配置）
        val newPlaylist = mutableListOf<PlaylistItem>()
        
        // 現在の曲を最初に追加
        currentSong?.let { song ->
            newPlaylist.add(PlaylistItem(song = song, isLast = false))
        }
        
        // シャッフルされた他の曲を追加
        shuffledOthers.forEach { song ->
            newPlaylist.add(PlaylistItem(song = song, isLast = false))
        }
        
        // 最後の曲に最終フラグを設定
        if (newPlaylist.isNotEmpty()) {
            newPlaylist[newPlaylist.size - 1] = newPlaylist.last().copy(isLast = true)
        }
        
        // プレイリストを更新
        playlist.clear()
        playlist.addAll(newPlaylist)
        
        // 現在位置を最初に設定（現在の曲が最初になったため）
        currentPosition = 0
        isShuffled = true
    }
    
    fun isShuffled(): Boolean = isShuffled
    
    fun resetToSequentialFromCurrent() {
        if (!isShuffled) return // 既に辞書順の場合は何もしない
        
        // 現在再生中の曲を取得
        val currentSong = getCurrentItem()?.song ?: return
        
        // 元の辞書順リストを作成（全曲）
        val originalSongs = playlist.map { it.song }.sortedBy { it.getDisplayTitle() }
        
        // 現在の曲が辞書順リストのどの位置にあるかを探す
        val currentIndexInOriginal = originalSongs.indexOfFirst { it.id == currentSong.id }
        if (currentIndexInOriginal < 0) return
        
        // 全曲で新しいプレイリストを作成
        val newPlaylist = originalSongs.mapIndexed { index, song ->
            PlaylistItem(
                song = song,
                isLast = index == originalSongs.size - 1
            )
        }
        
        // プレイリストを更新
        playlist.clear()
        playlist.addAll(newPlaylist)
        
        // 現在位置を現在の曲の位置に設定
        currentPosition = currentIndexInOriginal
        isShuffled = false
    }
}