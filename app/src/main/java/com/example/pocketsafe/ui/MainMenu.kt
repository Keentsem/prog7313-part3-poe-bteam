package com.example.pocketsafe.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
// import android.widget.GridLayout - removed to fix compilation error
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.pocketsafe.ViewExpensesActivity
import com.example.pocketsafe.ui.MyPocketActivity
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.R
import com.example.pocketsafe.data.SavingsGoal
import com.example.pocketsafe.data.dao.SavingsGoalDao
import com.example.pocketsafe.databinding.ActivityMainMenuBinding
import com.example.pocketsafe.ui.activity.BillReminderListActivity
import com.example.pocketsafe.ui.activity.SubscriptionListActivity
import com.example.pocketsafe.ui.dialogs.SavingsInputDialog
import com.example.pocketsafe.ui.expense.ExpenseEntryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

/**
 * Main menu activity with pixel-retro theme styling and wallet stitching borders
 * Modified to work without Hilt to prevent app crashes
 * Uses gold (#f3c34e) and brown (#5b3f2c) color scheme with login screen styling
 * Features a safe dial image and pixel-art UI elements
 */
class MainMenu : com.example.pocketsafe.ui.activity.BaseActivity() {
    // ID to identify this activity
    private val MAIN_MENU_ID = "MAIN_MENU"

    // Manually initialize instead of using Hilt injection
    private lateinit var savingsGoalDao: SavingsGoalDao

    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var cvAddExpense: CardView
    private lateinit var cvEditGoal: CardView
    private lateinit var cvSubscriptions: CardView
    private lateinit var cvBillReminders: CardView
    private lateinit var cvViewCategories: CardView
    private lateinit var cvMyPocket: CardView

    private lateinit var progressAnimation: ImageView
    private lateinit var tvGoalStatus: TextView
    private lateinit var tvMonthsRemaining: TextView
    private lateinit var tvProgressPercentage: TextView
    private lateinit var imgDial: ImageView
    
    // Goal celebration elements
    private lateinit var goalCelebrationContainer: FrameLayout
    private lateinit var goalCelebrationImage: ImageView
    private lateinit var tvWellDone: TextView

    // Removed bottomNavigation reference to fix crashes

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if first time using the app to show dial help
        val prefs = getSharedPreferences("PocketSafePrefs", MODE_PRIVATE)
        val dialHelpShown = prefs.getBoolean("dialHelpShown", false)
        
        // Apply pixel-retro theme styling with login screen brown background
        window.decorView.setBackgroundColor(android.graphics.Color.parseColor("#511900"))
        
        // Initialize the database access
        try {
            val database = MainApplication.getDatabase(applicationContext)
            savingsGoalDao = database.savingsGoalDao()
            
            // Show dial help popup for first-time users
            if (!dialHelpShown) {
                showDialHelpPopup()
                prefs.edit().putBoolean("dialHelpShown", true).apply()
            }
        } catch (e: Exception) {
            Log.e("MainMenu", "Error initializing database: ${e.message}", e)
            Toast.makeText(this, "Error initializing database: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        setupViews()
        // CRITICAL: Set up navigation BEFORE accessing cards to avoid overlay issues
        super.setupNavigationBar()
        
        // CRITICAL: Ensure the main menu is displayed properly
        Log.d("MainMenu", "Setting up main menu cards and ensuring visibility")
    }

    private fun setupViews() {
        try {
            // CRITICAL: Make all cards visible first
            binding.menuCardContainer.visibility = View.VISIBLE
            // Using direct card references instead of GridLayout
            
            // Ensure all CardViews are properly bound and VISIBLE
            cvAddExpense = binding.btnAddExpense
            cvAddExpense.visibility = View.VISIBLE
            
            cvEditGoal = binding.btnEditGoal
            cvEditGoal.visibility = View.VISIBLE
            
            cvSubscriptions = binding.btnSubscriptions
            cvSubscriptions.visibility = View.VISIBLE
            
            cvBillReminders = binding.btnBillReminders
            cvBillReminders.visibility = View.VISIBLE
            
            cvViewCategories = binding.btnViewCategories
            cvViewCategories.visibility = View.VISIBLE
            
            cvMyPocket = binding.btnViewUsers
            cvMyPocket.visibility = View.VISIBLE

            // Initialize the other UI elements
            progressAnimation = binding.progressAnimation
            tvGoalStatus = binding.tvGoalStatus
            tvMonthsRemaining = binding.tvMonthsRemaining
            tvProgressPercentage = binding.tvProgressPercentage
            
            // Initialize goal celebration elements
            goalCelebrationContainer = binding.goalCelebrationContainer
            goalCelebrationContainer.visibility = View.GONE // Ensure it's hidden by default
            goalCelebrationImage = binding.goalCelebrationImage
            tvWellDone = binding.tvWellDone
            
            // Initialize the dial image
            imgDial = binding.imgDial
            
            // Add rotating animation to the dial for visual appeal
            animateDial()
            
            // Setup dial click to add savings amount
            imgDial.setOnClickListener {
                showSavingsInputDialog()
            }

            loadSavingsGoal()

            // Set up all card click handlers
            cvAddExpense.setOnClickListener {
                startActivity(Intent(this, ExpenseEntryActivity::class.java))
            }

            cvEditGoal.setOnClickListener {
                startActivity(Intent(this, SetupActivity::class.java))
            }

            cvSubscriptions.setOnClickListener {
                startActivity(Intent(this, SubscriptionListActivity::class.java))
            }
            
            cvBillReminders.setOnClickListener {
                startActivity(Intent(this, BillReminderListActivity::class.java))
            }

            cvViewCategories.setOnClickListener {
                startActivity(Intent(this, CategoryActivity::class.java))
            }

            // MyPocket card click handler
            cvMyPocket.setOnClickListener {
                startActivity(Intent(this, MyPocketActivity::class.java))
            }

            // Removed bottom navigation listener to fix crashes

            checkAndShowSetupPrompt()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadSavingsGoal() {
        lifecycleScope.launch {
            try {
                // First try to load from Room database
                var goal = withContext(Dispatchers.IO) {
                    runCatching { savingsGoalDao.getCurrentGoal() }.getOrNull()
                }
                
                // If no goal is found, try to sync from Firebase
                if (goal == null) {
                    try {
                        val firebaseService = com.example.pocketsafe.firebase.FirebaseService.getInstance()
                        val goals = withContext(Dispatchers.IO) {
                            firebaseService.fetchUserGoals()
                        }
                        
                        if (goals.isNotEmpty()) {
                            // Save first goal to local database
                            withContext(Dispatchers.IO) {
                                savingsGoalDao.insert(goals.first())
                            }
                            goal = goals.first()
                            Toast.makeText(this@MainMenu, "Loaded goal from Firebase", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("MainMenu", "Error syncing with Firebase: ${e.message}", e)
                    }
                }

                if (goal != null) {
                    updateProgressUI(goal)
                } else {
                    // Show empty state UI
                    progressAnimation.setImageResource(R.drawable.progress_frame_0)
                    tvProgressPercentage.text = "0% COMPLETE"
                    tvGoalStatus.text = "GOAL: ${currencyFormat.format(0)} | SAVED: ${currencyFormat.format(0)}"
                    tvMonthsRemaining.text = "NO GOAL SET"
                    
                    Toast.makeText(this@MainMenu, "No savings goal set", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("MainMenu", "Error loading savings goal: ${e.message}", e)
                Toast.makeText(this@MainMenu, "Error loading savings goal: ${e.message}", Toast.LENGTH_SHORT).show()
                
                // Show error state UI
                progressAnimation.setImageResource(android.R.drawable.ic_menu_compass)
                tvProgressPercentage.text = "ERROR LOADING"
                tvGoalStatus.text = "PLEASE TRY AGAIN"
                tvMonthsRemaining.text = "ERROR: ${e.message?.take(20) ?: "Unknown"}"
            }
        }
    }

    /**
     * Animates the safe dial with a subtle rotation effect
     */
    private fun animateDial() {
        try {
            // Apply rotation animation to the dial
            val rotateAnim = android.view.animation.RotateAnimation(
                0f, 360f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 30000 // 30 seconds for a full rotation
                repeatCount = Animation.INFINITE
                interpolator = android.view.animation.LinearInterpolator()
            }
            
            imgDial.startAnimation(rotateAnim)
        } catch (e: Exception) {
            Log.e("MainMenu", "Error animating dial: ${e.message}", e)
        }
    }
    
    private fun updateProgressUI(goal: SavingsGoal) {
        // Ensure we're using proper pixel-retro gold and brown colors
        progressAnimation.setColorFilter(null) // Clear any existing filters
        
        // Calculate progress percentage safely
        val progress = if (goal.target_amount > 0) {
            ((goal.current_amount / goal.target_amount) * 100).toInt().coerceIn(0, 100) // Clamp between 0-100
        } else {
            0
        }
        
        // Check if goal is met (100%) to show celebration
        if (progress >= 100 && goal.current_amount >= goal.target_amount) {
            showGoalCelebration()
        }

        try {
            // Map progress to animation frames
            val progressDrawable = when {
                progress < 12 -> R.drawable.progress_frame_0
                progress < 25 -> R.drawable.progress_frame_1
                progress < 37 -> R.drawable.progress_frame_2
                progress < 50 -> R.drawable.progress_frame_3
                progress < 62 -> R.drawable.progress_frame_4
                progress < 75 -> R.drawable.progress_frame_5
                progress < 87 -> R.drawable.progress_frame_6
                else -> R.drawable.progress_frame_7
            }
            
            // Log for debugging
            Log.d("MainMenu", "Setting progress animation to frame for $progress% progress")
            
            // Set the image resource
            progressAnimation.setImageResource(progressDrawable)
            progressAnimation.contentDescription = "Savings progress animation: $progress%"
            
            // Ensure visibility
            progressAnimation.visibility = android.view.View.VISIBLE
        } catch (e: Exception) {
            Log.e("MainMenu", "Error setting progress animation: ${e.message}", e)
            progressAnimation.setImageResource(android.R.drawable.ic_menu_compass)
        }

        tvProgressPercentage.text = "$progress% COMPLETE"
        tvGoalStatus.text = "GOAL: ${currencyFormat.format(goal.target_amount)} | SAVED: ${currencyFormat.format(goal.current_amount)}"

        val monthsRemaining = if (goal.target_date > System.currentTimeMillis()) {
            val diff = goal.target_date - System.currentTimeMillis()
            (diff / (1000L * 60 * 60 * 24 * 30)).toInt() + 1
        } else {
            0
        }
        tvMonthsRemaining.text = "$monthsRemaining MONTHS REMAINING"
    }

    private fun checkAndShowSetupPrompt() {
        lifecycleScope.launch {
            try {
                val goal = withContext(Dispatchers.IO) {
                    runCatching { savingsGoalDao.getCurrentGoal() }.getOrNull()
                }

                if (goal == null) {
                    showBudgetGoalPrompt()
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainMenu, "Error checking setup status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Shows the goal celebration animation when the user meets their savings goal
     */
    private fun showGoalCelebration() {
        try {
            // Make the celebration container visible
            goalCelebrationContainer.visibility = View.VISIBLE
            
            // Set the animation drawable
            goalCelebrationImage.setBackgroundResource(R.drawable.goal_celebration_animation)
            val animation = goalCelebrationImage.background as AnimationDrawable
            
            // Start the animation
            animation.start()
            
            // Show the Well Done message after a short delay
            Handler(Looper.getMainLooper()).postDelayed({
                tvWellDone.visibility = View.VISIBLE
            }, 500)
            
            // Hide the celebration after 4 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                // Fade out animation
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = 1000
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    
                    override fun onAnimationEnd(animation: Animation?) {
                        goalCelebrationContainer.visibility = View.GONE
                        tvWellDone.visibility = View.GONE
                    }
                    
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                
                goalCelebrationContainer.startAnimation(fadeOut)
            }, 4000)
            
        } catch (e: Exception) {
            Log.e("MainMenu", "Error showing goal celebration: ${e.message}", e)
        }
    }
    
    private fun showBudgetGoalPrompt() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Setup Your PocketSafe")

        val message = """
            🎯 What is your monthly budget goal?
            💸 What are your regular expenses?
            📂 What categories do they fall under?
            🏦 Banking Details?
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("Setup") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }
        builder.setNegativeButton("Later") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
    
    /**
 * Shows a stylish dialog for the user to input savings amount
 * Uses the same styling as the Welcome dialog
 */
private fun showSavingsInputDialog() {
    try {
        // Load the current savings goal to display current progress
        lifecycleScope.launch {
            try {
                val goal = withContext(Dispatchers.IO) {
                    savingsGoalDao.getCurrentGoal()
                }
                
                if (goal != null) {
                    // Create and configure our custom styled dialog
                    val dialog = SavingsInputDialog(this@MainMenu)
                    dialog.setOnAddSavingsListener { amount ->
                        updateSavingsAmount(amount)
                    }
                    dialog.show(goal.currentAmount, goal.targetAmount)
                } else {
                    Toast.makeText(this@MainMenu, "No savings goal found. Please set up a goal first.", Toast.LENGTH_SHORT).show()
                    showBudgetGoalPrompt()
                }
            } catch (e: Exception) {
                Log.e("MainMenu", "Error loading savings goal: ${e.message}", e)
                Toast.makeText(this@MainMenu, "Error loading savings goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    } catch (e: Exception) {
        Log.e("MainMenu", "Error showing savings input dialog: ${e.message}", e)
        Toast.makeText(this, "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

    /**
     * Shows a pixel-themed help popup explaining how to use the savings dial
     */
    private fun showDialHelpPopup() {
        try {
            val builder = AlertDialog.Builder(this)
            
            // Set title and style it to match pixel theme
            builder.setTitle("SAVINGS DIAL GUIDE")
            
            val message = """
                🔄 The SAVINGS DIAL shows your progress toward your monthly goal!
                
                💰 Current savings amount is shown in GOLD.
                
                🎯 Target amount is shown in BROWN.
                
                ✨ Tap the dial to ADD SAVINGS and watch it spin!
                
                🎉 Meet your goal and see a PIXEL CELEBRATION!
            """.trimIndent()
            
            builder.setMessage(message)
            
            // Add pixel-styled buttons
            builder.setPositiveButton("GOT IT!") { dialog, _ ->
                dialog.dismiss()
            }
            
            // Create and show the dialog with pixel styling
            val dialog = builder.create()
            dialog.show()
            
            // Style the dialog after it's shown
            val titleView = dialog.findViewById<TextView>(android.R.id.title)
            if (titleView != null) {
                titleView.setTextColor(android.graphics.Color.parseColor("#f3c34e"))
                try {
                    titleView.typeface = android.graphics.Typeface.createFromAsset(assets, "fonts/pixel_game.otf")
                } catch (e: Exception) {
                    Log.e("MainMenu", "Error setting typeface: ${e.message}")
                }
            }
            
            val messageView = dialog.findViewById<TextView>(android.R.id.message)
            if (messageView != null) {
                messageView.setTextColor(android.graphics.Color.parseColor("#f3c34e"))
                try {
                    messageView.typeface = android.graphics.Typeface.createFromAsset(assets, "fonts/pixel_game.otf")
                } catch (e: Exception) {
                    Log.e("MainMenu", "Error setting typeface: ${e.message}")
                }
            }
            
            // Style the positive button to match pixel theme
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(android.graphics.Color.parseColor("#f3c34e"))
            try {
                positiveButton.typeface = android.graphics.Typeface.createFromAsset(assets, "fonts/pixel_game.otf")
            } catch (e: Exception) {
                Log.e("MainMenu", "Error setting typeface: ${e.message}")
            }
            
            // Set dialog background
            try {
                dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corner_background)
            } catch (e: Exception) {
                Log.e("MainMenu", "Error setting background: ${e.message}")
                // Try with a color if the resource doesn't exist
                dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#5b3f2c")))
            }
            
        } catch (e: Exception) {
            Log.e("MainMenu", "Error showing dial help popup: ${e.message}", e)
        }
    }
    
    /**
     * Updates the savings amount in the database
     */
    private fun updateSavingsAmount(amount: Double) {
        lifecycleScope.launch {
            try {
                // Get current goal
                val goal = withContext(Dispatchers.IO) {
                    savingsGoalDao.getCurrentGoal()
                }
                
                if (goal != null) {
                    // Update current amount
                    val newAmount = goal.current_amount + amount
                    // Create a copy of the goal with updated amount instead of modifying directly
                    val updatedGoal = goal.copy(currentAmount = newAmount)
                    
                    // Save to database
                    withContext(Dispatchers.IO) {
                        // Use updateSavings method with the goal ID and new amount
                        savingsGoalDao.updateSavings(goal.id, newAmount)
                    }
                    
                    // Update UI
                    updateProgressUI(updatedGoal)
                    
                    // Show success message
                    Toast.makeText(this@MainMenu, "Added ${currencyFormat.format(amount)} to savings!", Toast.LENGTH_SHORT).show()
                    
                    // Check if goal is met to show celebration
                    if (newAmount >= goal.target_amount) {
                        showGoalCelebration()
                    }
                } else {
                    Toast.makeText(this@MainMenu, "No savings goal found. Please set up a goal first.", Toast.LENGTH_SHORT).show()
                    showBudgetGoalPrompt()
                }
            } catch (e: Exception) {
                Log.e("MainMenu", "Error updating savings: ${e.message}", e)
                Toast.makeText(this@MainMenu, "Error updating savings: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Additional methods and functionality can be added here
}
