package com.enzo.freemymusicplayer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils

object ThemeHelper {
    
    private const val PREF_NAME = "display_settings"
    private const val KEY_THEME_COLOR = "theme_color"
    private const val KEY_SIZE_SETTING = "size_setting"
    private const val KEY_SHOW_ARTIST = "show_artist"
    
    private val themeColors = arrayOf(
        "#FF69B4", // ピンク（現在のlight_pink）
        "#FF6B6B", // 赤
        "#4ECDC4", // ターコイズ
        "#45B7D1", // 青
        "#000000", // 黒
        "#FFFFFF", // 白
        "#DDA0DD", // プラム
        "#F39C12", // オレンジ
        "#8E44AD", // 紫
        "#2ECC71", // エメラルド
        "#E74C3C", // 深紅
        "#95A5A6"  // グレー
    )
    
    fun applyTheme(activity: AppCompatActivity) {
        val sharedPreferences = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val colorIndex = sharedPreferences.getInt(KEY_THEME_COLOR, 0)
        val colorHex = themeColors[colorIndex]
        val color = Color.parseColor(colorHex)
        
        // ActionBarの色を変更
        activity.supportActionBar?.setBackgroundDrawable(
            GradientDrawable().apply {
                setColor(color)
            }
        )
        
        // ステータスバーの色も変更
        activity.window.statusBarColor = color
        
        // タイトル文字色を設定（白テーマの場合は黒文字）
        val titleTextColor = if (colorHex == "#FFFFFF") {
            Color.BLACK
        } else {
            Color.WHITE
        }
        
        // ActionBarのタイトル色を変更
        val actionBar = activity.supportActionBar
        actionBar?.let {
            val titleSpan = android.text.SpannableString(it.title ?: "")
            titleSpan.setSpan(
                android.text.style.ForegroundColorSpan(titleTextColor),
                0,
                titleSpan.length,
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            it.title = titleSpan
        }
        
        // ActionBarのアイコン色を設定（戻るボタンなど）
        activity.window.decorView.systemUiVisibility = if (colorHex == "#FFFFFF") {
            // 白テーマの場合はダークアイコン
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            // その他の場合はライトアイコン
            0
        }
        
        // ActionBarのナビゲーションアイコンとメニューアイコンの色を設定
        val toolbar = activity.findViewById<androidx.appcompat.widget.Toolbar>(androidx.appcompat.R.id.action_bar)
        toolbar?.let {
            val iconColor = if (colorHex == "#FFFFFF") android.graphics.Color.BLACK else android.graphics.Color.WHITE
            it.navigationIcon?.setTint(iconColor)
            it.overflowIcon?.setTint(iconColor)
        }
    }
    
    fun getThemeColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val colorIndex = sharedPreferences.getInt(KEY_THEME_COLOR, 0)
        val colorHex = themeColors[colorIndex]
        return Color.parseColor(colorHex)
    }
    
    fun getLighterThemeColor(context: Context): Int {
        val baseColor = getThemeColor(context)
        return ColorUtils.blendARGB(baseColor, Color.WHITE, 0.7f)
    }
    
    fun getBackgroundColor(context: Context): Int {
        val baseColor = getThemeColor(context)
        return ColorUtils.blendARGB(baseColor, Color.WHITE, 0.9f)
    }
    
    fun getContrastTextColor(context: Context): Int {
        val lighterColor = getLighterThemeColor(context)
        val luminance = ColorUtils.calculateLuminance(lighterColor)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }
    
    fun getDisplaySize(context: Context): DisplaySize {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val sizeId = sharedPreferences.getInt(KEY_SIZE_SETTING, com.enzo.freemymusicplayer.R.id.radioMediumSize)
        
        return when (sizeId) {
            com.enzo.freemymusicplayer.R.id.radioSmallSize -> DisplaySize.SMALL
            com.enzo.freemymusicplayer.R.id.radioLargeSize -> DisplaySize.LARGE
            else -> DisplaySize.MEDIUM
        }
    }
    
    fun getShowArtist(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_SHOW_ARTIST, true)
    }
    
    enum class DisplaySize(
        val statusTextSize: Float,
        val songTitleSize: Float,
        val songArtistSize: Float,
        val playerTitleSize: Float,
        val playerArtistSize: Float,
        val itemPadding: Int,
        val iconSize: Int
    ) {
        SMALL(14f, 14f, 12f, 16f, 12f, 12, 40),
        MEDIUM(16f, 16f, 14f, 18f, 14f, 16, 48),
        LARGE(18f, 18f, 16f, 20f, 16f, 20, 56)
    }
}