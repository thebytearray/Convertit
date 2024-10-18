package com.nasahacker.convertit.view.activity


import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ActivityMainBinding
import com.nasahacker.convertit.util.FileUtils.handleNotificationPermission
import com.nasahacker.convertit.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.navController?.let { navController ->
            binding.bottomNav.setupWithNavController(navController)
        }
        handleNotificationPermission(this)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.about -> startActivity(Intent(application, AboutActivity::class.java))
            }
            true
        }
    }
}