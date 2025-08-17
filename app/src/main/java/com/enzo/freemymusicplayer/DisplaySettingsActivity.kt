package com.enzo.freemymusicplayer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.enzo.freemymusicplayer.databinding.ActivityDisplaySettingsBinding

class DisplaySettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisplaySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedColorIndex = 0
    private val colorViews = mutableListOf<View>()
    
    // 12色のテーマカラー
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
    
    private val colorNames = arrayOf(
        "ピンク", "赤", "ターコイズ", "青",
        "黒", "白", "プラム", "オレンジ",
        "紫", "エメラルド", "深紅", "グレー"
    )

    companion object {
        const val PREF_NAME = "display_settings"
        const val KEY_THEME_COLOR = "theme_color"
        const val KEY_SIZE_SETTING = "size_setting"
        const val KEY_SHOW_ARTIST = "show_artist"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplaySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadSettings()
        setupToolbar()
        setupColorGrid()
        setupSizeSettings()
        setupArtistSettings()
        applyThemeColor()
        applyBackgroundColor()
        applySizeSettings()
    }
    
    private fun applyBackgroundColor() {
        val backgroundColor = ThemeHelper.getBackgroundColor(this)
        binding.root.setBackgroundColor(backgroundColor)
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "表示設定"
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupColorGrid() {
        val gridLayout = binding.colorGrid
        colorViews.clear()
        
        themeColors.forEachIndexed { index, colorHex ->
            val colorButton = View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 80
                    height = 80
                    setMargins(8, 8, 8, 8)
                }
                
                val drawable = GradientDrawable().apply {
                    setColor(Color.parseColor(colorHex))
                    cornerRadius = 8f
                }
                background = drawable
                elevation = 4f
                
                setOnClickListener {
                    selectColor(index)
                }
            }
            colorViews.add(colorButton)
            gridLayout.addView(colorButton)
        }
        
        updateColorSelection()
    }

    private fun setupSizeSettings() {
        binding.radioGroupSize.setOnCheckedChangeListener { _, checkedId ->
            val sizeText = when (checkedId) {
                R.id.radioSmallSize -> "小"
                R.id.radioMediumSize -> "中"
                R.id.radioLargeSize -> "大"
                else -> "不明"
            }
            
            saveSizeSetting(checkedId)
            applySizeSettings()
        }
    }
    
    private fun applySizeSettings() {
        val displaySize = ThemeHelper.getDisplaySize(this)
        
        // ScrollView内のTextViewを見つけて更新
        val scrollView = binding.scrollView
        updateTextSizesInViewGroup(scrollView, displaySize)
    }
    
    private fun updateTextSizesInViewGroup(viewGroup: android.view.ViewGroup?, displaySize: ThemeHelper.DisplaySize) {
        if (viewGroup == null) return
        
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            when (child) {
                is android.widget.TextView -> {
                    // テキストの種類に応じてサイズを設定
                    when {
                        child.textSize > 50f -> child.textSize = displaySize.playerTitleSize // タイトル
                        child.textSize > 30f -> child.textSize = displaySize.songTitleSize // 中見出し
                        else -> child.textSize = displaySize.songArtistSize // 通常テキスト
                    }
                }
                is android.view.ViewGroup -> {
                    updateTextSizesInViewGroup(child, displaySize)
                }
            }
        }
    }
    
    private fun loadSettings() {
        selectedColorIndex = sharedPreferences.getInt(KEY_THEME_COLOR, 0)
        val savedSizeId = sharedPreferences.getInt(KEY_SIZE_SETTING, R.id.radioMediumSize)
        binding.radioGroupSize.check(savedSizeId)
        val showArtist = sharedPreferences.getBoolean(KEY_SHOW_ARTIST, true)
        binding.checkBoxShowArtist.isChecked = showArtist
    }
    
    private fun selectColor(index: Int) {
        selectedColorIndex = index
        saveColorSetting(index)
        updateColorSelection()
        applyThemeColor()
        applyBackgroundColor()
    }
    
    private fun updateColorSelection() {
        colorViews.forEachIndexed { index, view ->
            val drawable = view.background as GradientDrawable
            if (index == selectedColorIndex) {
                // 白色の場合は黒枠、その他の場合は白枠で強調
                val strokeColor = if (themeColors[index] == "#FFFFFF") Color.BLACK else Color.WHITE
                drawable.setStroke(6, strokeColor)
            } else {
                drawable.setStroke(0, Color.TRANSPARENT)
            }
        }
    }
    
    private fun saveColorSetting(colorIndex: Int) {
        sharedPreferences.edit()
            .putInt(KEY_THEME_COLOR, colorIndex)
            .apply()
    }
    
    private fun saveSizeSetting(sizeId: Int) {
        sharedPreferences.edit()
            .putInt(KEY_SIZE_SETTING, sizeId)
            .apply()
    }
    
    private fun applyThemeColor() {
        val colorHex = themeColors[selectedColorIndex]
        val color = Color.parseColor(colorHex)
        
        // ActionBarの色を変更
        supportActionBar?.setBackgroundDrawable(
            GradientDrawable().apply {
                setColor(color)
            }
        )
        
        // ウィンドウのステータスバー色も変更
        window.statusBarColor = color
    }
    
    private fun setupArtistSettings() {
        binding.checkBoxShowArtist.setOnCheckedChangeListener { _, isChecked ->
            saveArtistSetting(isChecked)
        }
    }
    
    private fun saveArtistSetting(showArtist: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_SHOW_ARTIST, showArtist)
            .apply()
    }
}