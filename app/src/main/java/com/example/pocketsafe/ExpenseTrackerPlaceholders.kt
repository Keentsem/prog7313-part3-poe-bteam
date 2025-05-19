package com.example.pocketsafe

import android.graphics.Color
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*
import kotlin.random.Random

/**
 * This class provides placeholder data for the expense tracking charts
 */
object ExpenseTrackerPlaceholders {

    /**
     * Get placeholder data for the monthly expenses line chart
     */
    fun getMonthlyExpensesData(): LineData {
        // Sample data for monthly expenses over the past 6 months
        val entries = listOf(
            Entry(0f, 1250.75f),  // January
            Entry(1f, 980.25f),   // February
            Entry(2f, 1345.50f),  // March
            Entry(3f, 1120.80f),  // April
            Entry(4f, 1560.30f),  // May
            Entry(5f, 1245.67f)   // June
        )

        val dataSet = LineDataSet(entries, "Monthly Expenses")
        dataSet.color = Color.parseColor("#4CAF50")
        dataSet.setCircleColor(Color.parseColor("#4CAF50"))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 12f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#4CAF50")
        dataSet.fillAlpha = 50

        return LineData(dataSet)
    }

    /**
     * Get placeholder data for the category expenses bar chart
     */
    fun getCategoryExpensesData(): BarData {
        // Sample data for category expenses
        val entries = listOf(
            BarEntry(0f, 345.67f),  // Food & Dining
            BarEntry(1f, 289.50f),  // Shopping
            BarEntry(2f, 198.75f),  // Transportation
            BarEntry(3f, 156.40f),  // Bills & Utilities
            BarEntry(4f, 132.80f),  // Entertainment
            BarEntry(5f, 122.55f)   // Health & Fitness
        )

        val dataSet = BarDataSet(entries, "Category Expenses")

        // Use custom colors for better visualization
        dataSet.colors = listOf(
            Color.parseColor("#FF5722"),  // Deep Orange for Food
            Color.parseColor("#9C27B0"),  // Purple for Shopping
            Color.parseColor("#2196F3"),  // Blue for Transportation
            Color.parseColor("#FFC107"),  // Amber for Bills
            Color.parseColor("#4CAF50"),  // Green for Entertainment
            Color.parseColor("#E91E63")   // Pink for Health
        )

        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        return barData
    }

    /**
     * Get placeholder data for the category distribution pie chart
     */
    fun getCategoryDistributionData(): PieData {
        // Sample data for category distribution (percentages)
        val entries = listOf(
            PieEntry(27.8f, "Food & Dining"),
            PieEntry(23.2f, "Shopping"),
            PieEntry(16.0f, "Transportation"),
            PieEntry(12.5f, "Bills & Utilities"),
            PieEntry(10.7f, "Entertainment"),
            PieEntry(9.8f, "Health & Fitness")
        )

        val dataSet = PieDataSet(entries, "Expense Categories")

        // Use custom colors for better visualization
        dataSet.colors = listOf(
            Color.parseColor("#FF5722"),  // Deep Orange for Food
            Color.parseColor("#9C27B0"),  // Purple for Shopping
            Color.parseColor("#2196F3"),  // Blue for Transportation
            Color.parseColor("#FFC107"),  // Amber for Bills
            Color.parseColor("#4CAF50"),  // Green for Entertainment
            Color.parseColor("#E91E63")   // Pink for Health
        )

        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f

        return PieData(dataSet)
    }

    /**
     * Get placeholder data for the top spending categories
     */
    // In ExpenseTrackerPlaceholders.kt
    fun getTopSpendingCategories(): List<ExpenseCategory> {
        return listOf(
            ExpenseCategory("Food & Dining", 345.67, 65, R.drawable.ic_food),
            ExpenseCategory("Shopping", 289.50, 55, R.drawable.ic_shopping),
            ExpenseCategory("Transportation", 198.75, 38, R.drawable.ic_transport),
            ExpenseCategory("Bills & Utilities", 156.40, 30, R.drawable.ic_bills),
            ExpenseCategory("Entertainment", 132.80, 25, R.drawable.ic_entertainment),
            ExpenseCategory("Health & Fitness", 122.55, 23, R.drawable.ic_health),
            ExpenseCategory("Home", 98.30, 19, R.drawable.ic_home),
            ExpenseCategory("Education", 87.45, 17, R.drawable.ic_education)
        )
    }

    /**
     * Get placeholder data for daily expenses (for detailed view)
     */
    fun getDailyExpensesData(): List<DailyExpense> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val categories = listOf(
            "Food & Dining", "Shopping", "Transportation",
            "Bills & Utilities", "Entertainment", "Health & Fitness"
        )

        val places = mapOf(
            "Food & Dining" to listOf("Starbucks", "Chipotle", "Whole Foods", "Local Restaurant", "Pizza Hut"),
            "Shopping" to listOf("Amazon", "Target", "Walmart", "Best Buy", "IKEA"),
            "Transportation" to listOf("Uber", "Lyft", "Gas Station", "Public Transit", "Parking"),
            "Bills & Utilities" to listOf("Electric Bill", "Water Bill", "Internet", "Phone Bill", "Streaming Service"),
            "Entertainment" to listOf("Movie Theater", "Concert", "Subscription", "App Store", "Game Store"),
            "Health & Fitness" to listOf("Gym", "Pharmacy", "Doctor Visit", "Health Insurance", "Supplements")
        )

        val expenses = mutableListOf<DailyExpense>()

        // Generate 30-40 random expenses for the current month
        repeat(35) {
            val day = Random.nextInt(1, daysInMonth + 1)
            val category = categories.random()
            val place = places[category]?.random() ?: "Unknown"
            val amount = when (category) {
                "Food & Dining" -> Random.nextDouble(5.0, 75.0)
                "Shopping" -> Random.nextDouble(10.0, 150.0)
                "Transportation" -> Random.nextDouble(5.0, 50.0)
                "Bills & Utilities" -> Random.nextDouble(20.0, 200.0)
                "Entertainment" -> Random.nextDouble(10.0, 100.0)
                "Health & Fitness" -> Random.nextDouble(15.0, 120.0)
                else -> Random.nextDouble(10.0, 50.0)
            }

            calendar.set(currentYear, currentMonth, day)

            expenses.add(
                DailyExpense(
                    id = it.toLong(),
                    date = calendar.time,
                    category = category,
                    place = place,
                    amount = amount,
                    notes = if (Random.nextBoolean()) "Personal expense" else ""
                )
            )
        }

        // Sort by date (newest first)
        return expenses.sortedByDescending { it.date }
    }

    /**
     * Get placeholder data for monthly budget comparison
     */
    fun getBudgetComparisonData(): BudgetComparison {
        return BudgetComparison(
            totalBudget = 1500.0,
            totalSpent = 1245.67,
            categories = listOf(
                BudgetCategory("Food & Dining", 400.0, 345.67),
                BudgetCategory("Shopping", 300.0, 289.50),
                BudgetCategory("Transportation", 200.0, 198.75),
                BudgetCategory("Bills & Utilities", 350.0, 156.40),
                BudgetCategory("Entertainment", 150.0, 132.80),
                BudgetCategory("Health & Fitness", 100.0, 122.55)
            )
        )
    }

    /**
     * Get placeholder data for yearly expense trend
     */
    fun getYearlyTrendData(): LineData {
        // Sample data for yearly trend (past 12 months)
        val entries = listOf(
            Entry(0f, 1150.25f),  // July (last year)
            Entry(1f, 1280.75f),  // August
            Entry(2f, 1320.50f),  // September
            Entry(3f, 1450.80f),  // October
            Entry(4f, 1560.30f),  // November
            Entry(5f, 1850.67f),  // December (holiday spending)
            Entry(6f, 1250.75f),  // January
            Entry(7f, 980.25f),   // February
            Entry(8f, 1345.50f),  // March
            Entry(9f, 1120.80f),  // April
            Entry(10f, 1560.30f), // May
            Entry(11f, 1245.67f)  // June (current)
        )

        val dataSet = LineDataSet(entries, "Monthly Expenses (12 Months)")
        dataSet.color = Color.parseColor("#3F51B5")
        dataSet.setCircleColor(Color.parseColor("#3F51B5"))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 12f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#3F51B5")
        dataSet.fillAlpha = 50

        return LineData(dataSet)
    }
}

/**
 * Data class for daily expense entries
 */
data class DailyExpense(
    val id: Long,
    val date: Date,
    val category: String,
    val place: String,
    val amount: Double,
    val notes: String = ""
)

/**
 * Data class for budget comparison
 */
data class BudgetComparison(
    val totalBudget: Double,
    val totalSpent: Double,
    val categories: List<BudgetCategory>
)

/**
 * Data class for budget category
 */
data class BudgetCategory(
    val name: String,
    val budgeted: Double,
    val spent: Double
)