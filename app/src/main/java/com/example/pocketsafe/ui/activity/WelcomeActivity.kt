package com.example.pocketsafe.ui.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketsafe.ui.auth.LoginActivity
import com.example.pocketsafe.R

class WelcomeActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PocketSafePrefs"
    private val KEY_FIRST_LAUNCH = "first_launch"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // Show splash screen for a brief moment, then proceed
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            // Always show welcome dialog on launch
            showWelcomeDialog()
        }, 2000) // 2 second delay
    }

    private fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    private fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    private fun showWelcomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_welcome_message, null)
        val btnGetStarted = dialogView.findViewById<Button>(R.id.btnGetStarted)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        btnGetStarted.setOnClickListener {
            setFirstLaunchComplete()
            dialog.dismiss()
            navigateToMainActivity()
        }
        
        dialog.show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
