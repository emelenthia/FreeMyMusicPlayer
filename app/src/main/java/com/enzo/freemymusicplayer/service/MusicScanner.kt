package com.enzo.freemymusicplayer.service

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.enzo.freemymusicplayer.model.Song
import com.enzo.freemymusicplayer.model.MusicFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MusicScanner(private val context: Context) {

    suspend fun scanMusicFiles(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND " +
                "(${MediaStore.Audio.Media.DATA} LIKE '%.mp3' OR " +
                "${MediaStore.Audio.Media.DATA} LIKE '%.wav')"
        
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )
        
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: ""
                val artist = it.getString(artistColumn) ?: ""
                val album = it.getString(albumColumn) ?: ""
                val duration = it.getLong(durationColumn)
                val data = it.getString(dataColumn) ?: ""
                val albumId = it.getLong(albumIdColumn)
                
                val songUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                
                val song = Song(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    data = data,
                    uri = songUri,
                    albumId = albumId
                )
                
                songs.add(song)
            }
        }
        
        songs
    }
    
    suspend fun getAlbumArt(albumId: Long): android.graphics.Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            val uri = Uri.parse("content://media/external/audio/albumart/$albumId")
            val inputStream = context.contentResolver.openInputStream(uri)
            android.graphics.BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun scanMusicFolders(): List<MusicFolder> = withContext(Dispatchers.IO) {
        val allSongs = scanMusicFiles()
        val folderMap = mutableMapOf<String, MutableList<Song>>()
        
        // 楽曲をフォルダ別にグループ化
        allSongs.forEach { song ->
            val folderPath = File(song.data).parent ?: "不明なフォルダ"
            if (!folderMap.containsKey(folderPath)) {
                folderMap[folderPath] = mutableListOf()
            }
            folderMap[folderPath]?.add(song)
        }
        
        // MusicFolderオブジェクトを作成
        folderMap.map { (path, songs) ->
            val folderName = File(path).name.takeIf { it.isNotEmpty() } 
                ?: path.substringAfterLast("/").takeIf { it.isNotEmpty() }
                ?: "不明なフォルダ"
            
            MusicFolder(
                name = folderName,
                path = path,
                songCount = songs.size,
                songs = songs.sortedBy { it.title }
            )
        }.sortedBy { it.name }
    }
    
    suspend fun scanSongsInFolder(folderPath: String): List<Song> = withContext(Dispatchers.IO) {
        val allSongs = scanMusicFiles()
        allSongs.filter { song ->
            File(song.data).parent == folderPath
        }.sortedBy { it.title }
    }
}