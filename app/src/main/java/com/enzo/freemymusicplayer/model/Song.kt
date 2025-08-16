package com.enzo.freemymusicplayer.model

import android.graphics.Bitmap
import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val data: String, // ファイルパス
    val uri: Uri,
    val albumId: Long = 0,
    var albumArt: Bitmap? = null
) {
    fun getFormattedDuration(): String {
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    fun getDisplayTitle(): String {
        return if (title.isNotBlank()) title else data.substringAfterLast("/").substringBeforeLast(".")
    }
    
    fun getDisplayArtist(): String {
        return if (artist.isNotBlank() && artist != "<unknown>") artist else "不明なアーティスト"
    }
    
    fun getDisplayAlbum(): String {
        return if (album.isNotBlank() && album != "<unknown>") album else "不明なアルバム"
    }
}