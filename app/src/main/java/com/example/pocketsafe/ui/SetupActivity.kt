package com.example.pocketsafe.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.databinding.ActivitySetupBinding
import com.example.pocketsafe.ui.auth.AuthViewModel
import com.example.pocketsafe.ui.auth.AuthState
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.pocketsafe.data.SavingsGoal
import java.util.Calendar

/**
 * Setup activity for initial user configuration with pixel-retro theme
 * Modified to work without Hilt to prevent app crashes
 * Uses gold (#f3c34e) and brown (#5b3f2c) color scheme
 */
class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding
    private lateinit var viewModel: AuthViewModel
    
    // Database access
    private lateinit var savingsGoalDao: com.example.pocketsafe.data.dao.SavingsGoalDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Apply pixel-retro theme styling with brown (#5b3f2c) background
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#5b3f2c"))
        
        // Manually initialize AuthViewModel without Hilt
        try {
            val viewModelFactory = AuthViewModel.Factory(application)
            viewModel = ViewModelProvider(this, viewModelFactory)[AuthViewModel::class.java]
            
            // Initialize the database access for savings goals
            val database = MainApplication.getDatabase(applicationContext)
            savingsGoalDao = database.savingsGoalDao()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing ViewModel: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupViews()
        observeState()
    }

    private fun setupViews() {
        binding.btnSave.setOnClickListener {
            val monthlyGoal = binding.etMonthlyGoal.text.toString()
            val monthlyIncome = binding.etMonthlyIncome.text.toString()

            if (monthlyGoal.isEmpty() || monthlyIncome.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val goal = monthlyGoal.toDouble()
                val income = monthlyIncome.toDouble()

                if (goal <= 0 || income <= 0) {
                    Toast.makeText(this, "Values must be greater than 0", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Save the goals and income to the database
                lifecycleScope.launch {
                    try {
                        // Create a 6-month target date by default
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.MONTH, 6)
                        val targetDate = calendar.timeInMillis
                        
                        // Create the savings goal with the user's inputs
                        val savingsGoal = SavingsGoal(
                            name = "Monthly Budget Goal",
                            targetAmount = goal,
                            currentAmount = 0.0,
                            targetDate = targetDate,
                            description = "Monthly income: $${income}",
                            iconType = "PIXEL_MONEY_BAG" // Using pixel-retro theme icon
                        )
                        
                        // Save to the local Room database
                        withContext(Dispatchers.IO) {
                            savingsGoalDao.insert(savingsGoal)
                        }
                        
                        // Save to user preferences that this is not a first-time user
                        val sharedPrefs = getSharedPreferences("PocketSafePrefs", MODE_PRIVATE)
                        sharedPrefs.edit().putBoolean("firstTimeUser", false).apply()
                        
                        // Show success message
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SetupActivity, "Savings goal saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Launch main menu
                        val intent = Intent(this@SetupActivity, MainMenu::class.java)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SetupActivity, "Error saving goal: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> {
                            binding.btnSave.isEnabled = false
                        }
                        is AuthState.Error -> {
                            binding.btnSave.isEnabled = true
                            Toast.makeText(this@SetupActivity, state.errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            binding.btnSave.isEnabled = true
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Shows a pixel-themed dialog explaining savings goals
     */
    private fun showBudgetGoalPrompt() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("SETUP YOUR POCKETSAFE")

        val message = """
            🎯 What is your monthly budget goal?
            💸 What are your regular expenses?
            📂 What categories do they fall under?
            🏦 Banking Details?
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("SETUP") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("LATER") { dialog, _ ->
            dialog.dismiss()
        }
        
        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()
        
        // Apply pixel-retro styling to dialog elements
        val titleView = dialog.findViewById<TextView>(android.R.id.title)
        if (titleView != null) {
            titleView.setTextColor(android.graphics.Color.parseColor("#f3c34e"))
            try {
                titleView.typeface = android.graphics.Typeface.createFromAsset(assets, "fonts/pixel_game.otf")
            } catch (e: Exception) {
                Log.e("SetupActivity", "Error setting title typeface: ${e.message}")
            }
        }
        
        val messageView = dialog.findViewById<TextView>(android.R.id.message)
        if (messageView != null) {
            messageView.setTextColor(android.graphics.Color.parseColor("#f3c34e"))
            try {
                messageView.typeface = android.graphics.Typeface.createFromAsset(assets, "fonts/pixel_game.otf")
            } catch (e: Exception) {
                Log.e("SetupActivity", "Error setting message typeface: ${e.message}")
            }
        }
        
        // Style buttons with pixel theme
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(android.graphics.Color.parseColor("#f3c34e"))
        try {
            positiveButton.typeface = android.graphics.Typeface.createFromAsset(assets, "fonts/pixel_game.otf")
        } catch (e: Exception) {
            Log.e("SetupActivity", "Error setting positive button typeface: ${e.message}")
        }
        
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(android.graphics.Color.parseColor("#f3c34e"))
        try {
            negativeButton.typeface = android.graphics.Typeface.createFromAsset(assets, "fonts/pixel_game.otf")
        } catch (e: Exception) {
            Log.e("SetupActivity", "Error setting negative button typeface: ${e.message}")
        }
        
        // Set dialog background to match pixel theme
        try {
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#5b3f2c")))
        } catch (e: Exception) {
            Log.e("SetupActivity", "Error setting dialog background: ${e.message}")
        }
    }
} 