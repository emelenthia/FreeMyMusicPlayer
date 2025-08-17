package com.enzo.freemymusicplayer

import android.graphics.Color
import androidx.core.graphics.ColorUtils

/**
 * アプリ全体の色設定を管理するクラス
 * UI要素の色を統一的に管理し、テーマ変更時の色の調整を容易にする
 */
object ColorConfig {
    
    /**
     * テーマカラーの配列
     * 各色はユーザーが選択可能なプライマリカラー
     */
    val THEME_COLORS = arrayOf(
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
    
    /**
     * テーマカラーの名前（日本語）
     */
    val COLOR_NAMES = arrayOf(
        "ピンク", "赤", "ターコイズ", "青",
        "黒", "白", "プラム", "オレンジ",
        "紫", "エメラルド", "深紅", "グレー"
    )
    
    // ===== 背景色の調整パラメータ =====
    
    /** 薄いテーマカラー作成時の白とのブレンド比率 */
    const val LIGHTER_COLOR_BLEND_RATIO = 0.7f
    
    /** 背景色作成時の白とのブレンド比率 */
    const val BACKGROUND_COLOR_BLEND_RATIO = 0.9f
    
    /** リスト背景色作成時の黒とのブレンド比率（背景色をベースに少し暗く） */
    const val LIST_BACKGROUND_BLEND_RATIO = 0.05f
    
    // ===== アイコン背景色の調整パラメータ =====
    
    /** 黒テーマ時のアイコン背景を薄くする際の白とのブレンド比率 */
    const val ICON_BLACK_THEME_BLEND_RATIO = 0.3f
    
    /** その他のテーマ時のアイコン背景を濃くする際の黒とのブレンド比率 */
    const val ICON_OTHER_THEME_BLEND_RATIO = 0.2f
    
    // ===== タブインジケーターの調整パラメータ =====
    
    /** タブインジケーター色作成時の調整比率（白テーマ以外） */
    const val TAB_INDICATOR_BLEND_RATIO = 0.3f
    
    // ===== 色計算メソッド =====
    
    /**
     * テーマカラーから薄いバージョンを作成
     * @param baseColor ベースとなるテーマカラー
     * @return 薄くしたカラー
     */
    fun createLighterColor(baseColor: Int): Int {
        return ColorUtils.blendARGB(baseColor, Color.WHITE, LIGHTER_COLOR_BLEND_RATIO)
    }
    
    /**
     * テーマカラーから背景色を作成
     * @param baseColor ベースとなるテーマカラー
     * @return 背景色
     */
    fun createBackgroundColor(baseColor: Int): Int {
        return ColorUtils.blendARGB(baseColor, Color.WHITE, BACKGROUND_COLOR_BLEND_RATIO)
    }
    
    /**
     * 背景色からリスト背景色を作成（少し暗く）
     * @param backgroundColor ベースとなる背景色
     * @return リスト背景色
     */
    fun createListBackgroundColor(backgroundColor: Int): Int {
        return ColorUtils.blendARGB(backgroundColor, Color.BLACK, LIST_BACKGROUND_BLEND_RATIO)
    }
    
    /**
     * テーマカラーからアイコン背景色を作成
     * @param themeColor テーマカラー
     * @return アイコン背景色
     */
    fun createIconBackgroundColor(themeColor: Int): Int {
        return if (themeColor == Color.BLACK) {
            // 黒の場合は少し薄く
            ColorUtils.blendARGB(themeColor, Color.WHITE, ICON_BLACK_THEME_BLEND_RATIO)
        } else {
            // その他の色は少し濃く
            ColorUtils.blendARGB(themeColor, Color.BLACK, ICON_OTHER_THEME_BLEND_RATIO)
        }
    }
    
    /**
     * 白テーマかどうかを判定
     * @param themeColor テーマカラー
     * @return 白テーマの場合true
     */
    fun isWhiteTheme(themeColor: Int): Boolean {
        return themeColor == Color.WHITE
    }
    
    /**
     * 白テーマ用のActionBar/タブ背景色を取得
     * @param backgroundColor ベースとなる背景色
     * @return 白テーマ用の背景色（リスト背景色と同じ）
     */
    fun getWhiteThemeBarColor(backgroundColor: Int): Int {
        return createListBackgroundColor(backgroundColor)
    }
    
    /**
     * ActionBar/タブのテキスト色を取得
     * @param themeColor テーマカラー
     * @return テキスト色
     */
    fun getBarTextColor(themeColor: Int): Int {
        return if (isWhiteTheme(themeColor)) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }
    
    /**
     * タブインジケーター色を取得
     * @param themeColor テーマカラー
     * @param backgroundColor 背景色
     * @return タブインジケーター色
     */
    fun getTabIndicatorColor(themeColor: Int, backgroundColor: Int): Int {
        return if (isWhiteTheme(themeColor)) {
            // 白テーマの場合はテーマカラーをそのまま使用（実際には黒になる）
            Color.BLACK
        } else {
            // その他のテーマではテーマカラーを少し調整
            ColorUtils.blendARGB(themeColor, Color.WHITE, TAB_INDICATOR_BLEND_RATIO)
        }
    }
    
    /**
     * 明度に基づいてコントラストテキスト色を計算
     * @param backgroundColor 背景色
     * @return コントラスト色（黒または白）
     */
    fun getContrastTextColor(backgroundColor: Int): Int {
        val luminance = ColorUtils.calculateLuminance(backgroundColor)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }
}