package com.enzo.freemymusicplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.enzo.freemymusicplayer.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSettings()
        ThemeHelper.applyTheme(this)
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "設定"
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupSettings() {
        // 表示設定
        binding.buttonDisplaySettings.setOnClickListener {
            startActivity(Intent(this, DisplaySettingsActivity::class.java))
        }

        // 再生設定
        binding.buttonPlaybackSettings.setOnClickListener {
            Toast.makeText(this, "再生設定画面を開きます", Toast.LENGTH_SHORT).show()
            // TODO: 再生設定画面へ遷移
        }

        // 音質設定
        binding.buttonAudioSettings.setOnClickListener {
            Toast.makeText(this, "音質設定画面を開きます", Toast.LENGTH_SHORT).show()
            // TODO: 音質設定画面へ遷移
        }

        // 再生回数
        binding.buttonPlayCountSettings.setOnClickListener {
            startActivity(Intent(this, PlayCountActivity::class.java))
        }

        // その他設定
        binding.buttonOtherSettings.setOnClickListener {
            Toast.makeText(this, "その他設定画面を開きます", Toast.LENGTH_SHORT).show()
            // TODO: その他設定画面へ遷移
        }
    }

    override fun onResume() {
        super.onResume()
        applyThemeToActionBar()
        applyThemeColors()
        applySkinBackground()
    }
    
    private fun applyThemeToActionBar() {
        val themeColor = ThemeHelper.getThemeColor(this)
        val actionBarColor = if (themeColor == android.graphics.Color.WHITE) {
            // 白テーマの場合は曲一覧と同じ背景色
            val backgroundColor = ThemeHelper.getBackgroundColor(this)
            androidx.core.graphics.ColorUtils.blendARGB(backgroundColor, android.graphics.Color.BLACK, 0.05f)
        } else {
            themeColor
        }
        
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.GradientDrawable().apply {
                setColor(actionBarColor)
            }
        )
        window.statusBarColor = actionBarColor
        
        // タイトル文字色を設定
        val titleTextColor = if (themeColor == android.graphics.Color.WHITE) {
            android.graphics.Color.BLACK
        } else {
            android.graphics.Color.WHITE
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
    }
    
    private fun applyThemeColors() {
        val backgroundColor = ThemeHelper.getBackgroundColor(this)
        val displaySize = ThemeHelper.getDisplaySize(this)
        val textColor = ThemeHelper.getContrastTextColor(this)
        
        binding.root.setBackgroundColor(backgroundColor)
        
        // 各設定項目のTextViewのサイズと色を適用
        applySizeToLinearLayout(binding.buttonDisplaySettings, displaySize, textColor)
        applySizeToLinearLayout(binding.buttonPlaybackSettings, displaySize, textColor)
        applySizeToLinearLayout(binding.buttonAudioSettings, displaySize, textColor)
        applySizeToLinearLayout(binding.buttonPlayCountSettings, displaySize, textColor)
        applySizeToLinearLayout(binding.buttonOtherSettings, displaySize, textColor)
    }
    
    private fun applySizeToLinearLayout(layout: android.widget.LinearLayout, displaySize: ThemeHelper.DisplaySize, textColor: Int) {
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (child is android.widget.TextView) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, displaySize.songTitleSize)
                child.setTextColor(textColor)
            }
        }
    }
    
    private fun applySkinBackground() {
        val skinUri = ThemeHelper.getSkinUri(this)
        val opacity = ThemeHelper.getSkinOpacity(this)
        
        Log.d("SettingsActivity", "applySkinBackground - skinUri: $skinUri, opacity: $opacity")
        
        if (skinUri != null && skinUri.startsWith("file://")) {
            val file = java.io.File(skinUri.removePrefix("file://"))
            if (file.exists()) {
                Log.d("SettingsActivity", "Loading image from file: ${file.absolutePath}")
                
                Glide.with(this)
                    .load(file)
                    .into(binding.debugImageBackground)
                
                val alpha = opacity / 100f
                binding.debugImageBackground.alpha = alpha
                binding.debugImageBackground.visibility = android.view.View.VISIBLE
                
                Log.d("SettingsActivity", "Background image set with opacity: $opacity%")
            } else {
                binding.debugImageBackground.visibility = android.view.View.GONE
            }
        } else {
            binding.debugImageBackground.visibility = android.view.View.GONE
        }
    }
}