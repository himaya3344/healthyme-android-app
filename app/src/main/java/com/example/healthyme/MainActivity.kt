package com.example.healthyme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.healthyme.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up Bottom Navigation
        binding.bottomNavigation.setupWithNavController(navController)

        // Setup the AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_habits,
                R.id.navigation_mood,
                R.id.navigation_settings
            )
        )

        // Hide bottom navigation for non-main fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val shouldShowBottomNav = when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_habits,
                R.id.navigation_mood,
                R.id.navigation_settings -> true
                else -> false
            }
            binding.bottomNavigation.visibility = if (shouldShowBottomNav) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}