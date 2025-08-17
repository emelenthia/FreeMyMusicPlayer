package com.enzo.freemymusicplayer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.enzo.freemymusicplayer.databinding.ActivityDisplaySettingsBinding

class DisplaySettingsActivity : BaseActivity() {

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
        const val KEY_SKIN_URI = "skin_uri"
        const val KEY_SKIN_OPACITY = "skin_opacity"
    }
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            Log.d("DisplaySettings", "Selected URI: $selectedUri")
            
            // 古い背景画像ファイルを削除
            val oldSkinUri = sharedPreferences.getString(KEY_SKIN_URI, null)
            if (oldSkinUri != null && oldSkinUri.startsWith("file://")) {
                try {
                    val oldFile = java.io.File(oldSkinUri.removePrefix("file://"))
                    if (oldFile.exists()) {
                        oldFile.delete()
                        Log.d("DisplaySettings", "Deleted old skin file: ${oldFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    Log.w("DisplaySettings", "Failed to delete old skin file", e)
                }
            }
            
            // 画像をアプリの内部ストレージにコピー
            try {
                val inputStream = contentResolver.openInputStream(selectedUri)
                if (inputStream != null) {
                    val fileName = "skin_background_${System.currentTimeMillis()}.jpg"
                    val outputFile = java.io.File(filesDir, fileName)
                    val outputStream = java.io.FileOutputStream(outputFile)
                    
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    // 内部ストレージのファイルパスを保存
                    saveSkinUri("file://${outputFile.absolutePath}")
                    updateSkinDisplay()
                    Toast.makeText(this, "背景画像を設定しました", Toast.LENGTH_SHORT).show()
                    
                    Log.d("DisplaySettings", "Image copied to internal storage: ${outputFile.absolutePath}")
                } else {
                    Log.e("DisplaySettings", "Failed to open input stream")
                    Toast.makeText(this, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DisplaySettings", "Failed to copy image", e)
                Toast.makeText(this, "画像の保存に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
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
        setupSkinSettings()
        applyThemeColor()
        applyBackgroundColor()
        applySizeSettings()
        updateSkinDisplay()
        updateOpacityDisplay()
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
        val skinOpacity = sharedPreferences.getInt(KEY_SKIN_OPACITY, 50)
        binding.seekBarSkinOpacity.progress = skinOpacity
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
        
        val actionBarColor = if (colorHex == "#FFFFFF") {
            // 白テーマの場合は曲一覧と同じ背景色
            val backgroundColor = ThemeHelper.getBackgroundColor(this)
            androidx.core.graphics.ColorUtils.blendARGB(backgroundColor, Color.BLACK, 0.05f)
        } else {
            color
        }
        
        // ActionBarの色を変更
        supportActionBar?.setBackgroundDrawable(
            GradientDrawable().apply {
                setColor(actionBarColor)
            }
        )
        
        // ウィンドウのステータスバー色も変更
        window.statusBarColor = actionBarColor
        
        // タイトル文字色を設定
        val titleTextColor = if (colorHex == "#FFFFFF") {
            Color.BLACK
        } else {
            Color.WHITE
        }
        
        val actionBar = supportActionBar
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
        
        // ActionBarのナビゲーションアイコン（戻るボタン）の色を設定
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(androidx.appcompat.R.id.action_bar)
        toolbar?.let {
            val iconColor = if (colorHex == "#FFFFFF") Color.BLACK else Color.WHITE
            it.navigationIcon?.setTint(iconColor)
            it.overflowIcon?.setTint(iconColor)
        }
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
    
    private fun setupSkinSettings() {
        binding.buttonSelectSkin.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        binding.buttonClearSkin.setOnClickListener {
            clearSkin()
        }
        
        binding.seekBarSkinOpacity.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.textSkinOpacity.text = "${progress}%"
                    saveSkinOpacity(progress)
                    updateBackgroundOpacity()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }
    
    private fun saveSkinUri(uriString: String) {
        sharedPreferences.edit()
            .putString(KEY_SKIN_URI, uriString)
            .apply()
    }
    
    private fun clearSkin() {
        // 古い背景画像ファイルを削除
        val oldSkinUri = sharedPreferences.getString(KEY_SKIN_URI, null)
        if (oldSkinUri != null && oldSkinUri.startsWith("file://")) {
            try {
                val file = java.io.File(oldSkinUri.removePrefix("file://"))
                if (file.exists()) {
                    file.delete()
                    Log.d("DisplaySettings", "Deleted old skin file: ${file.absolutePath}")
                }
            } catch (e: Exception) {
                Log.w("DisplaySettings", "Failed to delete old skin file", e)
            }
        }
        
        sharedPreferences.edit()
            .remove(KEY_SKIN_URI)
            .apply()
        updateSkinDisplay()
        applySkinBackground()
        Toast.makeText(this, "背景画像をクリアしました", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateSkinDisplay() {
        val skinUri = sharedPreferences.getString(KEY_SKIN_URI, null)
        if (skinUri != null) {
            if (skinUri.startsWith("file://")) {
                // 内部ストレージのファイル
                val file = java.io.File(skinUri.removePrefix("file://"))
                binding.textCurrentSkin.text = if (file.exists()) file.name else "ファイルが見つかりません"
            } else {
                // 従来のURI
                try {
                    val uri = Uri.parse(skinUri)
                    val fileName = getFileName(uri)
                    binding.textCurrentSkin.text = fileName ?: "画像が設定されています"
                } catch (e: Exception) {
                    binding.textCurrentSkin.text = "エラー"
                }
            }
        } else {
            binding.textCurrentSkin.text = "なし"
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        it.getString(nameIndex)
                    } else {
                        uri.lastPathSegment
                    }
                } else {
                    uri.lastPathSegment
                }
            }
        } catch (e: Exception) {
            uri.lastPathSegment
        }
    }
    
    private fun saveSkinOpacity(opacity: Int) {
        sharedPreferences.edit()
            .putInt(KEY_SKIN_OPACITY, opacity)
            .apply()
    }
    
    private fun updateOpacityDisplay() {
        val opacity = sharedPreferences.getInt(KEY_SKIN_OPACITY, 50)
        binding.textSkinOpacity.text = "${opacity}%"
        binding.seekBarSkinOpacity.progress = opacity
    }
    
}