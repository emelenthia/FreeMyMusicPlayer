package com.enzo.freemymusicplayer.model

data class MusicFolder(
    val name: String,
    val path: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
) {
    fun getDisplayName(): String {
        return if (name.isNotBlank()) name else path.substringAfterLast("/")
    }
    
    fun getDisplayInfo(): String {
        return "$songCount 曲"
    }
}

data class BrowserItem(
    val type: ItemType,
    val name: String,
    val path: String,
    val songCount: Int = 0,
    val folder: MusicFolder? = null,
    val song: Song? = null
) {
    enum class ItemType {
        BACK_BUTTON,
        FOLDER,
        SONG
    }
    
    fun getDisplayName(): String {
        return when (type) {
            ItemType.BACK_BUTTON -> "← 戻る"
            ItemType.FOLDER -> name
            ItemType.SONG -> song?.getDisplayTitle() ?: name
        }
    }
    
    fun getDisplayInfo(): String {
        return when (type) {
            ItemType.BACK_BUTTON -> ""
            ItemType.FOLDER -> "$songCount 曲"
            ItemType.SONG -> song?.getDisplayArtist() ?: ""
        }
    }
}