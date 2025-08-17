package com.enzo.freemymusicplayer.model

import kotlin.random.Random

class ShufflePlaylist(private val originalPlaylist: List<Song>) {
    private var shuffleOrder = mutableListOf<Int>()
    private var currentShufflePosition = 0
    
    init {
        createShuffleOrder()
    }
    
    private fun createShuffleOrder() {
        // シャッフル順序を作成（0からplaylist.size-1までをシャッフル）
        shuffleOrder.clear()
        shuffleOrder.addAll((0 until originalPlaylist.size).shuffled(Random.Default))
        currentShufflePosition = 0
    }
    
    fun regenerateShuffleOrder() {
        createShuffleOrder()
    }
    
    fun getNextIndex(): Int {
        // 次の曲のインデックスを返す
        if (currentShufflePosition < shuffleOrder.size - 1) {
            currentShufflePosition++
            return shuffleOrder[currentShufflePosition]
        } else {
            // 全曲再生完了
            return -1
        }
    }
    
    fun getPreviousIndex(): Int {
        if (currentShufflePosition > 0) {
            currentShufflePosition--
            return shuffleOrder[currentShufflePosition]
        } else {
            return shuffleOrder[0]
        }
    }
    
    fun getCurrentIndex(): Int {
        if (currentShufflePosition >= 0 && currentShufflePosition < shuffleOrder.size) {
            return shuffleOrder[currentShufflePosition]
        }
        return 0
    }
    
    fun hasNext(): Boolean {
        return currentShufflePosition < shuffleOrder.size - 1
    }
    
    fun hasPrevious(): Boolean {
        return currentShufflePosition > 0
    }
    
    fun reset() {
        currentShufflePosition = 0
    }
    
    fun setCurrentPosition(originalIndex: Int) {
        val position = shuffleOrder.indexOf(originalIndex)
        if (position >= 0) {
            currentShufflePosition = position
        }
    }
    
    fun getCurrentShuffleOrder(): List<Int> = shuffleOrder.toList()
    
    fun getOriginalPlaylist(): List<Song> = originalPlaylist
}