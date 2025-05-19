package com.example.pocketsafe.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.pocketsafe.R
import com.example.pocketsafe.data.SavingsGoal
import com.example.pocketsafe.data.dao.SavingsGoalDao
import com.example.pocketsafe.databinding.ActivityMainMenuBinding
import com.example.pocketsafe.ui.expense.ExpenseEntryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainMenu : AppCompatActivity() {

    @Inject
    lateinit var savingsGoalDao: SavingsGoalDao

    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var cvBankingDetails: CardView
    private lateinit var cvAddExpense: CardView
    private lateinit var cvEditGoal: CardView
    private lateinit var cvSubscriptions: CardView
    private lateinit var cvViewCategories: CardView
    private lateinit var cvMyPocket: CardView

    private lateinit var progressAnimation: ImageView
    private lateinit var tvGoalStatus: TextView
    private lateinit var tvMonthsRemaining: TextView
    private lateinit var tvProgressPercentage: TextView

    private lateinit var bottomNavigation: com.google.android.material.bottomnavigation.BottomNavigationView

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        try {
            cvBankingDetails = binding.btnBankingDetails
            cvAddExpense = binding.btnAddExpense
            cvEditGoal = binding.btnEditGoal
            cvSubscriptions = binding.btnSubscriptions
            cvViewCategories = binding.btnViewCategories
            cvMyPocket = binding.btnViewUsers // This is actually the MyPocket card

            bottomNavigation = binding.navigationBar.bottomNavigation

            progressAnimation = binding.progressAnimation
            tvGoalStatus = binding.tvGoalStatus
            tvMonthsRemaining = binding.tvMonthsRemaining
            tvProgressPercentage = binding.tvProgressPercentage

            loadSavingsGoal()

            cvAddExpense.setOnClickListener {
                startActivity(Intent(this, ExpenseEntryActivity::class.java))
            }

            cvEditGoal.setOnClickListener {
                startActivity(Intent(this, SetupActivity::class.java))
            }

            cvBankingDetails.setOnClickListener {
                Toast.makeText(this, "Banking Details feature coming soon!", Toast.LENGTH_SHORT).show()
            }

            cvSubscriptions.setOnClickListener {
                Toast.makeText(this, "Subscriptions feature coming soon!", Toast.LENGTH_SHORT).show()
            }

            cvViewCategories.setOnClickListener {
                startActivity(Intent(this, CategoryActivity::class.java))
            }

            // MyPocket card click listener
            cvMyPocket.setOnClickListener {
                val intent = Intent(this, MyPocketActivity::class.java)
                startActivity(intent)
            }

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_home -> {
                        Toast.makeText(this, "You're already on the home screen", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.navigation_expenses -> {
                        startActivity(Intent(this, MyPocketActivity::class.java))
                        true
                    }
                    R.id.navigation_categories -> {
                        startActivity(Intent(this, CategoryActivity::class.java))
                        true
                    }
                    R.id.navigation_profile -> {
                        Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }

            checkAndShowSetupPrompt()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing UI: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadSavingsGoal() {
        lifecycleScope.launch {
            try {
                val goal = withContext(Dispatchers.IO) {
                    runCatching { savingsGoalDao.getCurrentGoal() }.getOrNull()
                }

                if (goal != null) {
                    updateProgressUI(goal)
                } else {
                    Toast.makeText(this@MainMenu, "No savings goal set", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@MainMenu, "Error loading savings goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProgressUI(goal: SavingsGoal) {
        val progress = if (goal.target_amount > 0) {
            ((goal.current_amount / goal.target_amount) * 100).toInt()
        } else {
            0
        }

        try {
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
            progressAnimation.setImageResource(progressDrawable)
            progressAnimation.contentDescription = "Savings progress animation"
        } catch (e: Exception) {
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
}
