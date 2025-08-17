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