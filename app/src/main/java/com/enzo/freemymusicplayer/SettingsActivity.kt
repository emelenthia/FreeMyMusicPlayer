package com.enzo.freemymusicplayer

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // その他設定
        binding.buttonOtherSettings.setOnClickListener {
            Toast.makeText(this, "その他設定画面を開きます", Toast.LENGTH_SHORT).show()
            // TODO: その他設定画面へ遷移
        }
    }

    override fun onResume() {
        super.onResume()
        ThemeHelper.applyTheme(this)
        applyThemeColors()
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
}