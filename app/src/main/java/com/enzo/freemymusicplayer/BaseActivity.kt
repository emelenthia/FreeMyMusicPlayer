package com.enzo.freemymusicplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activity遷移アニメーションを無効化
        overridePendingTransition(0, 0)
        applySkinBackground()
    }
    
    override fun onResume() {
        super.onResume()
        applySkinBackground()
    }
    
    protected fun applySkinBackground() {
        val skinUri = ThemeHelper.getSkinUri(this)
        val opacity = ThemeHelper.getSkinOpacity(this)
        
        Log.d("BaseActivity", "applySkinBackground - skinUri: $skinUri, opacity: $opacity")
        
        // 既存の背景画像を削除
        val decorView = window.decorView as android.view.ViewGroup
        decorView.findViewWithTag<android.widget.ImageView>("background_overlay")?.let {
            decorView.removeView(it)
        }
        
        if (skinUri != null && skinUri.startsWith("file://")) {
            val file = java.io.File(skinUri.removePrefix("file://"))
            if (file.exists()) {
                Log.d("BaseActivity", "Loading image from file: ${file.absolutePath}")
                
                // DecorViewに直接ImageViewを追加
                val backgroundImageView = android.widget.ImageView(this).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    tag = "background_overlay"
                    alpha = opacity / 100f
                }
                
                Glide.with(this)
                    .load(file)
                    .into(backgroundImageView)
                
                decorView.addView(backgroundImageView) // 最上層に追加
                
                Log.d("BaseActivity", "Background image added to DecorView with opacity: $opacity%")
            }
        }
    }
    
    // 透明度のみを更新する軽量メソッド
    protected fun updateBackgroundOpacity() {
        val opacity = ThemeHelper.getSkinOpacity(this)
        val decorView = window.decorView as android.view.ViewGroup
        decorView.findViewWithTag<android.widget.ImageView>("background_overlay")?.let { imageView ->
            imageView.alpha = opacity / 100f
            Log.d("BaseActivity", "Background opacity updated to: $opacity%")
        }
    }
    
    override fun finish() {
        super.finish()
        // 終了時のアニメーションも無効化
        overridePendingTransition(0, 0)
    }
}