package com.example.pocketsafe

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketsafe.databinding.ActivityExpenseTrackerBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.util.*

class ExpenseTrackerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseTrackerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabLayout()
        setupCharts()
        setupRecyclerView()
        setupSummaryCard()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showMonthlyChart()
                    1 -> showCategoryBarChart()
                    2 -> showCategoryPieChart()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupCharts() {
        setupLineChart()
        setupBarChart()
        setupPieChart()
    }

    private fun showMonthlyChart() {
        binding.lineChart.visibility = View.VISIBLE
        binding.barChart.visibility = View.GONE
        binding.pieChart.visibility = View.GONE

        // Animate the chart
        binding.lineChart.animateX(1500, Easing.EaseInOutQuart)
    }

    private fun showCategoryBarChart() {
        binding.lineChart.visibility = View.GONE
        binding.barChart.visibility = View.VISIBLE
        binding.pieChart.visibility = View.GONE

        // Animate the chart
        binding.barChart.animateY(1500, Easing.EaseInOutQuart)
    }

    private fun showCategoryPieChart() {
        binding.lineChart.visibility = View.GONE
        binding.barChart.visibility = View.GONE
        binding.pieChart.visibility = View.VISIBLE

        // Animate the chart
        binding.pieChart.animateY(1500, Easing.EaseInOutQuart)
    }

    private fun setupLineChart() {
        // Get placeholder data
        binding.lineChart.data = ExpenseTrackerPlaceholders.getMonthlyExpensesData()

        // Customize the chart
        binding.lineChart.description.isEnabled = false
        binding.lineChart.legend.isEnabled = true
        binding.lineChart.setTouchEnabled(true)
        binding.lineChart.isDragEnabled = true
        binding.lineChart.setScaleEnabled(true)
        binding.lineChart.setPinchZoom(true)

        val xAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"))

        binding.lineChart.axisRight.isEnabled = false

        // Animate the chart on start
        binding.lineChart.animateX(1500, Easing.EaseInOutQuart)
    }

    private fun setupBarChart() {
        // Get placeholder data
        binding.barChart.data = ExpenseTrackerPlaceholders.getCategoryExpensesData()

        // Customize the chart
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = true
        binding.barChart.setTouchEnabled(true)
        binding.barChart.isDragEnabled = true
        binding.barChart.setScaleEnabled(true)

        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf(
            "Food", "Shopping", "Transport", "Bills", "Entertainment", "Health"
        ))

        binding.barChart.axisRight.isEnabled = false
        binding.barChart.axisLeft.axisMinimum = 0f

        // Animate the chart
        binding.barChart.animateY(1500, Easing.EaseInOutQuart)
    }

    private fun setupPieChart() {
        // Get placeholder data
        val pieData = ExpenseTrackerPlaceholders.getCategoryDistributionData()
        pieData.setValueFormatter(PercentFormatter(binding.pieChart))
        binding.pieChart.data = pieData

        // Customize the chart
        binding.pieChart.description.isEnabled = false
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.setHoleColor(Color.WHITE)
        binding.pieChart.transparentCircleRadius = 61f
        binding.pieChart.setDrawEntryLabels(false)
        binding.pieChart.legend.isEnabled = true
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.setCenterText("Expenses\nby Category")
        binding.pieChart.setCenterTextSize(16f)

        // Animate the chart
        binding.pieChart.animateY(1500, Easing.EaseInOutQuart)
    }

    private fun setupRecyclerView() {
        // Get placeholder data for top spending categories
        val categories = ExpenseTrackerPlaceholders.getTopSpendingCategories()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = ExpenseCategoryAdapter(categories)

        // Add animation to RecyclerView
        val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)
        binding.recyclerView.layoutAnimation = animation
    }

    private fun setupSummaryCard() {
        // Format the total expenses with currency symbol
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        binding.totalExpensesText.text = currencyFormat.format(1245.67)

        // Set comparison text
        binding.comparisonText.text = "12% higher than last month"
        binding.comparisonText.setTextColor(Color.parseColor("#E91E63"))
    }
}