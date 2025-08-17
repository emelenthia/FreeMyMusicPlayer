package com.enzo.freemymusicplayer.service

import android.content.Context
import android.content.SharedPreferences

class PlayCountManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("play_counts", Context.MODE_PRIVATE)
    
    fun incrementPlayCount(songId: Long) {
        val currentCount = getPlayCount(songId)
        prefs.edit().putInt(songId.toString(), currentCount + 1).apply()
    }
    
    fun getPlayCount(songId: Long): Int {
        return prefs.getInt(songId.toString(), 0)
    }
    
    fun getAllPlayCounts(): Map<String, Int> {
        return prefs.all.mapValues { it.value as? Int ?: 0 }
    }
}