package com.example.pocketsafe.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketsafe.ui.activity.BaseActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.R
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.IconType
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for viewing expense charts and history with pixel-retro theme styling
 * Modified to work without Hilt to prevent app crashes
 * Uses gold (#f3c34e) and brown (#5b3f2c) color scheme for UI elements
 */
class MyPocketActivity : BaseActivity() {
    // Manually initialize instead of using Hilt injection
    private lateinit var db: AppDatabase

    // UI Components
    private lateinit var pieChart: PieChart
    private lateinit var categorySpinner: Spinner
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var applyFilterButton: Button
    private lateinit var totalAmountTextView: TextView
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var categoryLegendRecyclerView: RecyclerView
    private lateinit var budgetProgressBar: ProgressBar
    private lateinit var budgetPercentageTextView: TextView
    private lateinit var spentAmountTextView: TextView
    private lateinit var budgetAmountTextView: TextView
    private lateinit var remainingBudgetTextView: TextView

    // Adapters
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryLegendAdapter: CategoryLegendAdapter

    // Data
    private var startDate: Date? = null
    private var endDate: Date? = null
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    
    // Budget constants - in a real app, these would come from user settings or database
    private val monthlyBudget = 1000.0

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_my_pocket)
            
            // Apply pixel-retro theme styling with brown (#5b3f2c) background
            window.decorView.setBackgroundColor(Color.parseColor("#5b3f2c"))
            
            // Setup navigation bar
            super.setupNavigationBar()
            
            // Manually initialize the database without Hilt
            try {
                db = MainApplication.getDatabase(applicationContext)
                Log.d("MyPocketActivity", "Database initialized successfully")
            } catch (e: Exception) {
                Log.e("MyPocketActivity", "Error initializing database: ${e.message}")
                Toast.makeText(this, "Error initializing database: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            try {
                initializeViews()
                setupPieChart()
                setupCategorySpinner()
                setupDateButtons()
                setupFilterButton()
                setupRecyclerViews()
                setupBudgetView()

                // Load data with error handling
                safeLoadInitialData()
            } catch (e: Exception) {
                showError("Error initializing UI: ${e.message ?: "Unknown error"}")
            }
        } catch (e: Exception) {
            // Catastrophic failure - show a simple alert and finish
            Toast.makeText(this, "Failed to initialize My Pocket view", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun safeLoadInitialData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                loadCategories()
                loadExpenses()
            } catch (e: Exception) {
                showError("Error loading data: ${e.message ?: "Unknown error"}")
            }
        }
    }
    
    private fun showError(message: String) {
        // Show error on UI
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Create a temporary TextView to display the error if it's not already on screen
        val errorTextView = TextView(this).apply {
            text = message
            setTextColor(Color.RED)
            textSize = 16f
            setPadding(16, 16, 16, 16)
            background = ContextCompat.getDrawable(context, R.drawable.rounded_corner_background)
        }
        
        // Add to root layout
        val rootView = findViewById<View>(android.R.id.content) as FrameLayout
        rootView.addView(errorTextView)
        
        // Log error for debugging
        android.util.Log.e("MyPocket", message)
    }

    private fun initializeViews() {
        // Chart and category views
        pieChart = findViewById(R.id.pieChart)
        categorySpinner = findViewById(R.id.categorySpinner)
        categoryLegendRecyclerView = findViewById(R.id.categoryLegendRecyclerView)
        
        // Date filter views
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        applyFilterButton = findViewById(R.id.applyFilterButton)
        
        // Expense list views
        totalAmountTextView = findViewById(R.id.totalAmountTextView)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
        
        // Budget tracking views
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        budgetPercentageTextView = findViewById(R.id.budgetPercentageTextView)
        spentAmountTextView = findViewById(R.id.spentAmountTextView)
        budgetAmountTextView = findViewById(R.id.budgetAmountTextView)
        remainingBudgetTextView = findViewById(R.id.remainingBudgetTextView)
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            setCenterTextSize(16f)
            setCenterTextColor(ContextCompat.getColor(this@MyPocketActivity, R.color.teal_primary))
            legend.isEnabled = false // We'll use our custom legend
        }
    }

    private fun setupCategorySpinner() {
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadExpenses()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDateButtons() {
        startDateButton.setOnClickListener { showDatePickerDialog(true) }
        endDateButton.setOnClickListener { showDatePickerDialog(false) }
    }

    private fun setupFilterButton() {
        applyFilterButton.setOnClickListener {
            if (startDate == null || endDate == null) {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadExpenses()
        }
    }

    private fun setupRecyclerViews() {
        // Setup expense recycler view
        expenseAdapter = ExpenseAdapter()
        expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyPocketActivity)
            adapter = expenseAdapter
        }

        // Setup category legend recycler view
        categoryLegendAdapter = CategoryLegendAdapter()
        categoryLegendRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyPocketActivity)
            adapter = categoryLegendAdapter
        }
    }

    private fun setupBudgetView() {
        // Set initial budget information
        budgetAmountTextView.text = "Budget: ${currencyFormat.format(monthlyBudget)}"
        updateBudgetProgress(0.0) // Will be updated when expenses are loaded
    }
    
    private fun updateBudgetProgress(spentAmount: Double, budget: Double = monthlyBudget) {
        // Calculate percentage of budget used
        val percentage = if (budget > 0) (spentAmount / budget * 100).toInt().coerceIn(0, 100) else 0
        val remaining = budget - spentAmount
        
        // Update progress bar
        budgetProgressBar.progress = percentage
        budgetPercentageTextView.text = "$percentage%"
        
        // Update amount texts
        spentAmountTextView.text = "Spent: ${currencyFormat.format(spentAmount)}"
        budgetAmountTextView.text = "Budget: ${currencyFormat.format(budget)}"
        remainingBudgetTextView.text = "Remaining: ${currencyFormat.format(remaining)}"
        
        // Adjust progress bar color based on usage
        val progressDrawable = budgetProgressBar.progressDrawable.mutate()
        val color = when {
            percentage > 90 -> Color.parseColor("#F44336") // Red when over 90% of budget
            percentage > 75 -> Color.parseColor("#FF9800") // Orange when over 75% of budget
            else -> Color.parseColor("#FFFFFF") // White (default)
        }
        
        // Apply tint to progress bar
        try {
            progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        } catch (e: Exception) {
            // Fallback if color filter doesn't work
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time

                if (isStartDate) {
                    startDate = date
                    startDateButton.text = "Start: ${dateFormatter.format(date)}"
                } else {
                    endDate = date
                    endDateButton.text = "End: ${dateFormatter.format(date)}"
                }
            },
            year, month, day
        ).show()
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val categories = db.categoryDao().getAllCategories().first()
                val categoryNames = mutableListOf("All Categories")
                categoryNames.addAll(categories.map { it.name }) // Use name instead of ID for better UX
                
                // Create a mapping of category names to their IDs for later use
                val categoryMap = categories.associateBy({ it.name }, { it.id })

                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(
                        this@MyPocketActivity,
                        android.R.layout.simple_spinner_item,
                        categoryNames
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    categorySpinner.adapter = adapter

                    // If no categories exist yet, populate with default categories
                    if (categories.isEmpty()) {
                        createDefaultCategories()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyPocketActivity, "Failed to load categories: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun createDefaultCategories() {
        val defaultCategories = listOf(
            Category(name = "Food & Dining", iconType = IconType.FOOD, icon = "restaurant", color = Color.parseColor("#4CAF50"), monthlyAmount = 300.0),
            Category(name = "Transportation", iconType = IconType.TRANSPORT, icon = "car", color = Color.parseColor("#FFC107"), monthlyAmount = 150.0),
            Category(name = "Shopping", iconType = IconType.SHOPPING, icon = "cart", color = Color.parseColor("#2196F3"), monthlyAmount = 200.0),
            Category(name = "Bills & Utilities", iconType = IconType.NECESSITY, icon = "bill", color = Color.parseColor("#F44336"), monthlyAmount = 250.0),
            Category(name = "Entertainment", iconType = IconType.ENTERTAINMENT, icon = "movie", color = Color.parseColor("#9C27B0"), monthlyAmount = 100.0)
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (category in defaultCategories) {
                    db.categoryDao().insertCategory(category)
                }
                loadCategories() // Reload after adding default categories
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyPocketActivity, "Failed to create default categories", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadExpenses() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First, load all categories to get current info
                val allCategories = db.categoryDao().getAllCategories().first()
                val categoryMap = allCategories.associateBy({ it.id }, { it })
                
                // Get the selected category name from spinner
                val selectedCategoryName = categorySpinner.selectedItem.toString()
                
                // Get expenses based on category selection
                val allExpenses: List<Expense> = if (selectedCategoryName == "All Categories") {
                    // Get all expenses
                    db.expenseDao().getAllExpenses()
                } else {
                    // Find the category ID for the selected name
                    val category = allCategories.find { it.name == selectedCategoryName }
                    if (category != null) {
                        // Get expenses for this category
                        db.expenseDao().getExpensesByCategory(category.id)
                    } else {
                        // Fallback to all expenses if category not found
                        db.expenseDao().getAllExpenses()
                    }
                }

                // Apply date filtering
                val filteredExpenses = allExpenses.filter { expense ->
                    val expenseDate = Date(expense.date)
                    (startDate == null || expenseDate >= startDate) &&
                    (endDate == null || expenseDate <= endDate)
                }

                // Calculate category totals for pie chart
                val categoryTotals = mutableMapOf<Int, Double>()
                for (expense in filteredExpenses) {
                    val currentTotal = categoryTotals.getOrDefault(expense.categoryId, 0.0)
                    categoryTotals[expense.categoryId] = currentTotal + expense.amount
                }

                // Create pie chart entries and legend items
                val pieEntries = mutableListOf<PieEntry>()
                val legendItems = mutableListOf<CategoryLegendItem>()
                
                // Calculate total monthly budget from all categories
                var totalBudget = 0.0
                
                // Process each category with expenses
                for ((categoryId, amount) in categoryTotals) {
                    // Get category from map or create a placeholder
                    val category = categoryMap[categoryId] ?: Category(
                        id = categoryId,
                        name = "Category $categoryId",
                        iconType = IconType.OTHER,
                        icon = "category",
                        color = ContextCompat.getColor(this@MyPocketActivity, R.color.teal_primary)
                    )
                    
                    // Add the category's monthly budget to total budget
                    totalBudget += category.monthlyAmount
                    
                    // Add to pie chart entries
                    pieEntries.add(PieEntry(amount.toFloat(), category.name))
                    
                    // Add to legend items
                    legendItems.add(CategoryLegendItem(
                        name = category.name,
                        amount = amount,
                        color = category.color ?: ContextCompat.getColor(this@MyPocketActivity, R.color.teal_primary),
                        categoryId = categoryId
                    ))
                }

                // Calculate total spent for budget tracking
                val totalSpent = filteredExpenses.sumOf { it.amount }
                
                // If no budget is set, use the default
                if (totalBudget <= 0) {
                    totalBudget = monthlyBudget
                }

                // Update UI
                withContext(Dispatchers.Main) {
                    // Update budget progress with actual category budgets
                    updateBudgetProgress(totalSpent, totalBudget)
                    
                    // Update pie chart
                    if (pieEntries.isNotEmpty()) {
                        val dataSet = PieDataSet(pieEntries, "").apply {
                            // Assign colors from categories
                            colors = legendItems.map { it.color }
                            valueTextSize = 14f
                            valueTextColor = Color.WHITE
                            valueFormatter = PercentFormatter(pieChart)
                            sliceSpace = 2f
                        }
                        
                        val pieData = PieData(dataSet).apply {
                            setValueTextSize(12f)
                            setValueTextColor(Color.WHITE)
                        }
                        
                        pieChart.apply {
                            data = pieData
                            setCenterText("Total\n${currencyFormat.format(totalSpent)}")
                            invalidate()
                        }
                    } else {
                        pieChart.apply {
                            clear()
                            setCenterText("No Data")
                            invalidate()
                        }
                    }

                    // Update category legend with sorted categories
                    categoryLegendAdapter.updateCategories(legendItems.sortedByDescending { it.amount })

                    // Update total amount text
                    totalAmountTextView.text = "Total: ${currencyFormat.format(totalSpent)}"

                    // Update expense list
                    expenseAdapter.updateExpenses(filteredExpenses)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyPocketActivity, "Failed to load expenses: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace() // Log the full stack trace for debugging
                }
            }
        }
    }

    private fun saveImageToDownloads(file: File) {
        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destinationFile = File(downloadsFolder, file.name)
            file.copyTo(destinationFile, overwrite = true)
            Toast.makeText(this, "Image saved to Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
} 