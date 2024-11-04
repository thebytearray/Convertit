package com.nasahacker.convertit.view.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ActivityAboutBinding
import com.nasahacker.convertit.util.AppUtils
import com.nasahacker.convertit.viewmodel.AboutViewModel
import com.nasahacker.library.NasaCircleImageView

class AboutActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAboutBinding.inflate(layoutInflater) }
    private val viewModel: AboutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }

        setupToolbar()
        setupButtonListeners()
        observeViewModel()
        viewModel.loadContributors()
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

    private fun observeViewModel() {
        // Observe bitmaps and add them to the GridLayout
        viewModel.bitmaps.observe(this) { bitmaps ->
            // Clear any existing views in the GridLayout to avoid duplicates
            binding.contributorsGrid.removeAllViews()

            // Populate the GridLayout with contributor images
            bitmaps.forEach { bitmap ->
                val imageView = NasaCircleImageView(this).apply {
                    layoutParams = ViewGroup.MarginLayoutParams(100, 100).apply {
                        setMargins(16, 16, 16, 16)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(bitmap)
                }
                binding.contributorsGrid.addView(imageView)
            }

            // Hide the ProgressBar and reveal the GridLayout with animation
            binding.loadingProgress.visibility = View.GONE
            if (binding.contributorsGrid.visibility == View.GONE) {
                binding.contributorsGrid.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate()
                        .alpha(1f)
                        .setDuration(500)
                        .setListener(null)
                }
            }
        }
    }
}
