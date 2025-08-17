package com.enzo.freemymusicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.enzo.freemymusicplayer.databinding.ActivityPlayCountBinding
import com.enzo.freemymusicplayer.fragment.AllSongsFragment
import com.enzo.freemymusicplayer.fragment.RankingFragment
import com.google.android.material.tabs.TabLayoutMediator

class PlayCountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayCountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewPager()
        ThemeHelper.applyTheme(this)
        applyThemeColors()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "再生回数"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupViewPager() {
        val adapter = PlayCountPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "全ての曲"
                1 -> "ランキング"
                else -> ""
            }
        }.attach()
        
        // 上部のボタンでタブ切り替え
        setupTabButtons()
        updateButtonStates(0) // 初期状態
    }
    
    private fun setupTabButtons() {
        binding.buttonAllSongs.setOnClickListener {
            binding.viewPager.currentItem = 0
            updateButtonStates(0)
        }
        
        binding.buttonRanking.setOnClickListener {
            binding.viewPager.currentItem = 1
            updateButtonStates(1)
        }
        
        // ViewPagerの変更を監視してボタン状態を更新
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonStates(position)
            }
        })
    }
    
    private fun updateButtonStates(selectedTab: Int) {
        val themeColor = ThemeHelper.getThemeColor(this)
        val textColor = ThemeHelper.getContrastTextColor(this)
        
        // 背景は透明のまま、テキスト色だけで選択状態を表現
        when (selectedTab) {
            0 -> {
                binding.buttonAllSongs.setTextColor(themeColor)
                binding.buttonAllSongs.alpha = 1.0f
                binding.buttonRanking.setTextColor(textColor)
                binding.buttonRanking.alpha = 0.6f
            }
            1 -> {
                binding.buttonAllSongs.setTextColor(textColor)
                binding.buttonAllSongs.alpha = 0.6f
                binding.buttonRanking.setTextColor(themeColor)
                binding.buttonRanking.alpha = 1.0f
            }
        }
    }

    private fun applyThemeColors() {
        val backgroundColor = ThemeHelper.getBackgroundColor(this)
        val themeColor = ThemeHelper.getThemeColor(this)
        
        val tabBackgroundColor = if (themeColor == android.graphics.Color.WHITE) {
            // 白テーマの場合は曲一覧と同じ背景色
            androidx.core.graphics.ColorUtils.blendARGB(backgroundColor, android.graphics.Color.BLACK, 0.05f)
        } else {
            themeColor
        }
        
        val tabTextColor = if (themeColor == android.graphics.Color.WHITE) {
            android.graphics.Color.BLACK
        } else {
            android.graphics.Color.WHITE
        }
        
        binding.root.setBackgroundColor(backgroundColor)
        binding.tabLayout.setBackgroundColor(tabBackgroundColor)
        binding.tabLayout.setTabTextColors(
            tabTextColor,
            tabTextColor
        )
        
        // タブインジケーター色を設定
        val tabIndicatorColor = ColorConfig.getTabIndicatorColor(themeColor, backgroundColor)
        binding.tabLayout.setSelectedTabIndicatorColor(tabIndicatorColor)
        
        // ViewPagerの背景を少し暗く設定（他の領域との区別のため）
        val listBackgroundColor = androidx.core.graphics.ColorUtils.blendARGB(backgroundColor, android.graphics.Color.BLACK, 0.05f)
        binding.viewPager.setBackgroundColor(listBackgroundColor)
    }

    override fun onResume() {
        super.onResume()
        applyThemeToActionBar()
        applyThemeColors()
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

    private class PlayCountPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AllSongsFragment()
                1 -> RankingFragment()
                else -> AllSongsFragment()
            }
        }
    }
}