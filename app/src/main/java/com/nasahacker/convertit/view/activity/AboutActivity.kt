package com.nasahacker.convertit.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ActivityAboutBinding
import com.nasahacker.convertit.util.AppUtils

class AboutActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupToolbar()
        setupButtonListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupButtonListeners() {
        binding.githubBtn.setOnClickListener {
            AppUtils.openLink(this, getString(R.string.github_link))
        }
        binding.discordBtn.setOnClickListener {
            AppUtils.openLink(this, getString(R.string.discord_group_link))
        }
        binding.telegramBtn.setOnClickListener {
            AppUtils.openLink(this, getString(R.string.telegram_group_link))
        }
    }
}
