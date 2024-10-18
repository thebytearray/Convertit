package com.nasahacker.convertit.view.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nasahacker.convertit.ConvertItApplication
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ActivityAboutBinding
import com.nasahacker.convertit.util.FileUtils

class AboutActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.githubBtn.setOnClickListener {
            FileUtils.openLink(
                this,
                getString(R.string.github_link)
            )
        }
        binding.discordBtn.setOnClickListener {
            FileUtils.openLink(
                this,
                getString(R.string.discord_group_link)
            )
        }
        binding.telegramBtn.setOnClickListener {
            FileUtils.openLink(
                this,
                getString(R.string.telegram_group_link)
            )
        }


    }
}