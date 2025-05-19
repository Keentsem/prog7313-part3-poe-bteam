package com.example.pocketsafe

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.UserDatabase
import java.text.SimpleDateFormat
import java.util.*

class ViewExpensesActivity : ComponentActivity() {
    private lateinit var db: UserDatabase
    private lateinit var expensesLayout: LinearLayout
    private lateinit var categorySpinner: Spinner
    private lateinit var selectedCategoryLabel: TextView
    private lateinit var startDateEditText: TextView
    private lateinit var endDateEditText: TextView
    private lateinit var filterButton: Button
    private lateinit var totalAmountTextView: TextView
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = UserDatabase.getDatabase(applicationContext)
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
                val intent = Intent(this@ViewExpensesActivity, MainMenu::class.java)
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
        setupCategorySpinner(mainLayout)
        setupDateRangeFilter(mainLayout)
        setupFilterButton(mainLayout)
        setupTotalAmountView(mainLayout)
        setupExpensesScrollView(mainLayout)

        setContentView(mainLayout)

        loadCategories()

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categorySpinner.selectedItem as? Category
                if (selectedCategory != null) {
                    selectedCategoryLabel.text = "Showing Expenses for: ${selectedCategory.name}"
                    loadExpenses(selectedCategory.id)
                }
                selectedCategoryLabel.setTextColor(Color.parseColor("#D2B48C"))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCategorySpinner(mainLayout: LinearLayout) {
        val spinnerLabel = TextView(this).apply {
            text = ""
            textSize = 20f
            setPadding(0, 0, 0, 10)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
        }
        mainLayout.addView(spinnerLabel)

        categorySpinner = Spinner(this)
        mainLayout.addView(categorySpinner)

        selectedCategoryLabel = TextView(this).apply {
            textSize = 18f
            setPadding(0, 30, 0, 20)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
        }
        mainLayout.addView(selectedCategoryLabel)
    }

    private fun setupDateRangeFilter(mainLayout: LinearLayout) {
        val dateLabel = TextView(this).apply {
            text = "Select Date Range:"
            textSize = 20f
            setPadding(0, 20, 0, 10)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(dateLabel)

        startDateEditText = TextView(this).apply {
            text = "Start Date"
            setPadding(0, 10, 0, 20)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setOnClickListener { showDatePickerDialog(true) }
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(startDateEditText)

        endDateEditText = TextView(this).apply {
            text = "End Date"
            setPadding(0, 10, 0, 20)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setOnClickListener { showDatePickerDialog(false) }
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(endDateEditText)
    }

    private fun setupFilterButton(mainLayout: LinearLayout) {
        filterButton = Button(this).apply {
            text = "Apply Date Filter"
            setPadding(0, 20, 0, 20)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(filterButton)

        filterButton.setOnClickListener {
            val selectedCategory = categorySpinner.selectedItem as? Category

            if (startDate == null) {
                showToast("Please select a start date.")
                return@setOnClickListener
            }
            if (endDate == null) {
                showToast("Please select an end date.")
                return@setOnClickListener
            }

            if (selectedCategory != null) {
                loadExpenses(selectedCategory.id)
            }
        }
    }

    private fun setupTotalAmountView(mainLayout: LinearLayout) {
        totalAmountTextView = TextView(this).apply {
            text = "Total Amount: 0"
            textSize = 18f
            setPadding(0, 20, 0, 20)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(totalAmountTextView)
    }

    private fun setupExpensesScrollView(mainLayout: LinearLayout) {
        expensesLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
        }

        val scrollView = ScrollView(this).apply {
            addView(expensesLayout)
        }

        mainLayout.addView(scrollView)
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(formatted)

                if (isStartDate) {
                    startDate = parsedDate
                    startDateEditText.text = "Start Date: $formatted"
                } else {
                    endDate = parsedDate
                    endDateEditText.text = "End Date: $formatted"
                }
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun loadCategories() {
        MainScope().launch(Dispatchers.IO) {
            try {
                db.categoryDao().getAllCategories().collect { categoryList ->
                    categories = categoryList
                    runOnUiThread {
                        val adapter = object : ArrayAdapter<Category>(
                            this@ViewExpensesActivity,
                            android.R.layout.simple_spinner_item,
                            categories
                        ) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                (view as TextView).setTextColor(Color.parseColor("#D2B48C"))
                                return view
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                (view as TextView).setTextColor(Color.parseColor("#D2B48C"))
                                return view
                            }
                        }
                        categorySpinner.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ViewExpensesActivity, "Error loading categories: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadExpenses(categoryId: Int) {
        MainScope().launch(Dispatchers.IO) {
            try {
                db.expenseDao().getExpensesByCategory(categoryId).collect { expenses ->
                    runOnUiThread {
                        expensesLayout.removeAllViews()
                        var totalAmount = 0.0
                        
                        expenses.forEach { expense ->
                            val expenseView = createExpenseView(expense)
                            expensesLayout.addView(expenseView)
                            totalAmount += expense.amount
                        }
                        
                        totalAmountTextView.text = "Total Amount: $${String.format("%.2f", totalAmount)}"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ViewExpensesActivity, "Error loading expenses: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createExpenseView(expense: Expense): View {
        val expenseView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.parseColor("#D2B48C"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 10, 0, 10)
            }
        }

        val amountTextView = TextView(this).apply {
            text = "Amount: $${String.format("%.2f", expense.amount)}"
            textSize = 18f
            setTextColor(Color.parseColor("#8B5E3C"))
        }

        val descriptionTextView = TextView(this).apply {
            text = "Description: ${expense.description}"
            textSize = 16f
            setTextColor(Color.parseColor("#8B5E3C"))
        }

        val dateTextView = TextView(this).apply {
            text = "Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expense.date)}"
            textSize = 16f
            setTextColor(Color.parseColor("#8B5E3C"))
        }

        expenseView.addView(amountTextView)
        expenseView.addView(descriptionTextView)
        expenseView.addView(dateTextView)

        return expenseView
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 