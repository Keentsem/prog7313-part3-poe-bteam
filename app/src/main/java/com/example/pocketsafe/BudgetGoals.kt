package com.example.pocketsafe

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import com.example.pocketsafe.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BudgetGoals : Activity() {
    class Goals : Activity() {
        private lateinit var minGoalEditText: EditText
        private lateinit var maxGoalEditText: EditText
        private lateinit var incomeEditText: EditText
        private lateinit var sharedPref: android.content.SharedPreferences

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            sharedPref = getSharedPreferences("GoalPrefs", Context.MODE_PRIVATE)
            val mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#8B5E3C"))
                setPadding(30, 30, 30, 30)
            }

            val backButton = Button(this).apply {
                text = "Back to Main Menu"
                setPadding(40, 40, 40, 40)

                val drawable = GradientDrawable()
                drawable.shape = GradientDrawable.OVAL
                drawable.setColor(Color.parseColor("#D2B48C"))
                background = drawable

                setOnClickListener {
                    val intent = Intent(this@Goals, MainMenu::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 100
                leftMargin = 40
            }

            mainLayout.addView(backButton, layoutParams)

            val space = View(this).apply {
                setLayoutParams(LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 20))
            }
            mainLayout.addView(space)

            setupGoalInputs(mainLayout)
            setupSaveButton(mainLayout)

            setContentView(mainLayout)

            loadExistingGoals()
        }

        private fun setupGoalInputs(mainLayout: LinearLayout) {
            val title = TextView(this).apply {
                text = "Set Your Budget Goals"
                textSize = 24f
                setPadding(0, 20, 0, 40)
                setBackgroundColor(Color.parseColor("#8B5E3C"))
                setTextColor(Color.parseColor("#D2B48C"))
            }
            mainLayout.addView(title)

            val minGoalLabel = TextView(this).apply {
                text = "Minimum Goal (R):"
                textSize = 20f
                setPadding(0, 20, 0, 10)
                setBackgroundColor(Color.parseColor("#8B5E3C"))
                setTextColor(Color.parseColor("#D2B48C"))
            }
            mainLayout.addView(minGoalLabel)

            minGoalEditText = EditText(this).apply {
                hint = "Enter minimum goal amount"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setBackgroundColor(Color.parseColor("#F5F5F5"))
                setTextColor(Color.parseColor("#8B5E3C"))
            }
            mainLayout.addView(minGoalEditText)

            val maxGoalLabel = TextView(this).apply {
                text = "Maximum Goal (R):"
                textSize = 20f
                setPadding(0, 20, 0, 10)
                setBackgroundColor(Color.parseColor("#8B5E3C"))
                setTextColor(Color.parseColor("#D2B48C"))
            }
            mainLayout.addView(maxGoalLabel)

            maxGoalEditText = EditText(this).apply {
                hint = "Enter maximum goal amount"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setBackgroundColor(Color.parseColor("#F5F5F5"))
                setTextColor(Color.parseColor("#8B5E3C"))
            }
            mainLayout.addView(maxGoalEditText)

            val incomeLabel = TextView(this).apply {
                text = "Monthly Income (R):"
                textSize = 20f
                setPadding(0, 20, 0, 10)
                setBackgroundColor(Color.parseColor("#8B5E3C"))
                setTextColor(Color.parseColor("#D2B48C"))
            }
            mainLayout.addView(incomeLabel)

            incomeEditText = EditText(this).apply {
                hint = "Enter monthly income"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setBackgroundColor(Color.parseColor("#F5F5F5"))
                setTextColor(Color.parseColor("#8B5E3C"))
            }
            mainLayout.addView(incomeEditText)
        }

        private fun setupSaveButton(mainLayout: LinearLayout) {
            val saveButton = Button(this).apply {
                text = "Save Goals"
                setPadding(40, 40, 40, 40)
                setBackgroundColor(Color.parseColor("#D2B48C"))
                setOnClickListener { saveGoals() }
            }
            mainLayout.addView(saveButton)
        }

        private fun loadExistingGoals() {
            minGoalEditText.setText(sharedPref.getString("minGoal", ""))
            maxGoalEditText.setText(sharedPref.getString("maxGoal", ""))
            incomeEditText.setText(sharedPref.getString("income", ""))
        }

        private fun saveGoals() {
            val minGoal = minGoalEditText.text.toString()
            val maxGoal = maxGoalEditText.text.toString()
            val income = incomeEditText.text.toString()

            if (minGoal.isEmpty() || maxGoal.isEmpty() || income.isEmpty()) {
                showToast("Please fill in all fields")
                return
            }

            with(sharedPref.edit()) {
                putString("minGoal", minGoal)
                putString("maxGoal", maxGoal)
                putString("income", income)
                apply()
            }

            showToast("Goals saved successfully")
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
            finish()
        }

        private fun showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
} 