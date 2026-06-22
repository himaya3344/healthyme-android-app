package com.example.healthyme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthyme.utils.SharedPreferencesHelper

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = SharedPreferencesHelper(this)
        
        val intent = if (prefs.getOnboardingCompleted()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnboardingActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}