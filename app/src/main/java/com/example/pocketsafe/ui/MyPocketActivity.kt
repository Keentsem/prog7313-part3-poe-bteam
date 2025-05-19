package com.example.pocketsafe.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.AppDatabase
import com.example.pocketsafe.data.Expense
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyPocketActivity : AppCompatActivity() {
    @Inject
    lateinit var db: AppDatabase

    private lateinit var pieChart: PieChart
    private lateinit var categorySpinner: Spinner
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var applyFilterButton: Button
    private lateinit var totalAmountTextView: TextView
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter

    private var startDate: Date? = null
    private var endDate: Date? = null
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_pocket)

        initializeViews()
        setupPieChart()
        setupCategorySpinner()
        setupDateButtons()
        setupFilterButton()
        setupRecyclerView()

        loadCategories()
        loadExpenses()
    }

    private fun initializeViews() {
        pieChart = findViewById(R.id.pieChart)
        categorySpinner = findViewById(R.id.categorySpinner)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        applyFilterButton = findViewById(R.id.applyFilterButton)
        totalAmountTextView = findViewById(R.id.totalAmountTextView)
        expensesRecyclerView = findViewById(R.id.expensesRecyclerView)
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setHoleColor(android.graphics.Color.TRANSPARENT)
            setEntryLabelColor(android.graphics.Color.WHITE)
            setEntryLabelTextSize(12f)
            legend.textColor = android.graphics.Color.WHITE
            legend.textSize = 12f
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

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter()
        expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyPocketActivity)
            adapter = expenseAdapter
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
                val categories = db.expenseDao().getAllExpenseCategories()
                val categoriesWithAll = listOf("All Categories") + categories

                withContext(Dispatchers.Main) {
                    val adapter = ArrayAdapter(
                        this@MyPocketActivity,
                        android.R.layout.simple_spinner_item,
                        categoriesWithAll
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    categorySpinner.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyPocketActivity, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadExpenses() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val selectedCategory = categorySpinner.selectedItem.toString()
                // Get expenses, handling Flow type properly
                val allExpenses: List<Expense> = if (selectedCategory == "All Categories") {
                    // Collect the Flow to get a List
                    db.expenseDao().getAllExpenses().first()
                } else {
                    try {
                        // Try to parse the selection as a category ID (integer)
                        val categoryId = selectedCategory.toIntOrNull() 
                            ?: return@launch withContext(Dispatchers.Main) {
                                Toast.makeText(this@MyPocketActivity, "Invalid category ID", Toast.LENGTH_SHORT).show()
                            }
                        // Collect the Flow to get a List
                        db.expenseDao().getExpensesByCategory(categoryId).first()
                    } catch (e: Exception) {
                        // If parsing fails, default to all expenses
                        db.expenseDao().getAllExpenses().first()
                    }
                }

                // Create a new filtered list instead of using the filter extension function
                val filteredExpenses = mutableListOf<Expense>()
                for (expense in allExpenses) {
                    val expenseDate = Date(expense.date)
                    if ((startDate == null || expenseDate >= startDate) &&
                        (endDate == null || expenseDate <= endDate)) {
                        filteredExpenses.add(expense)
                    }
                }

                // Calculate category totals for pie chart
                val categoryTotals = mutableMapOf<Int, Double>()
                for (expense in filteredExpenses) {
                    val categoryId = expense.categoryId
                    val currentTotal = categoryTotals.getOrDefault(categoryId, 0.0)
                    categoryTotals[categoryId] = currentTotal + expense.amount
                }

                // Create pie chart entries
                val pieEntries = mutableListOf<PieEntry>()
                for ((categoryId, amount) in categoryTotals) {
                    // Convert categoryId to a string for display in the pie chart
                    pieEntries.add(PieEntry(amount.toFloat(), "Category $categoryId"))
                }

                // Update UI
                withContext(Dispatchers.Main) {
                    // Update pie chart
                    val dataSet = PieDataSet(pieEntries, "Categories").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                    }
                    pieChart.data = PieData(dataSet)
                    pieChart.invalidate()

                    // Update total amount
                    val total = filteredExpenses.sumOf { it.amount }
                    totalAmountTextView.text = "Total Amount: $${String.format("%.2f", total)}"

                    // Update RecyclerView
                    expenseAdapter.updateExpenses(filteredExpenses)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyPocketActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
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