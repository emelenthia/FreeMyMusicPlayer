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
    
    fun applyThemeToViews(context: Context) {
        val themeColor = getThemeColor(context)
        val lighterColor = getLighterThemeColor(context)
        val textColor = getContrastTextColor(context)
        
        // ここで各Viewに色を適用する処理を実装
    }
}