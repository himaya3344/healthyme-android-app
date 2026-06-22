package com.example.healthyme

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.healthyme.adapters.OnboardingPagerAdapter
import com.example.healthyme.utils.SharedPreferencesHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var skipButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var prefs: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        prefs = SharedPreferencesHelper(this)
        setupViews()
        setupViewPager()
        setupButtons()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.nextButton)
    }

    private fun setupViewPager() {
        val pagerAdapter = OnboardingPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == pagerAdapter.itemCount - 1) {
                    nextButton.text = "Finish"
                    skipButton.visibility = View.GONE
                } else {
                    nextButton.text = "Next"
                    skipButton.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupButtons() {
        skipButton.setOnClickListener {
            completeOnboarding()
        }

        nextButton.setOnClickListener {
            if (viewPager.currentItem == 2) {
                completeOnboarding()
            } else {
                viewPager.currentItem++
            }
        }
    }

    private fun completeOnboarding() {
        prefs.setOnboardingCompleted(true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}