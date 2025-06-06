package com.example.pocketsafe.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketsafe.R
import com.example.pocketsafe.data.RenewalPeriod
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.databinding.ActivitySubscriptionListBinding
import com.example.pocketsafe.ui.adapters.SubscriptionAdapter
import com.example.pocketsafe.ui.viewmodel.SubscriptionViewModel
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Activity that displays a list of subscriptions
 * Modified to work without Hilt to prevent app crashes
 * Maintains pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class SubscriptionListActivity : BaseActivity() {

    private lateinit var binding: ActivitySubscriptionListBinding
    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var adapter: SubscriptionAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            binding = ActivitySubscriptionListBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Initialize ViewModel using Factory pattern instead of Hilt
            try {
                viewModel = ViewModelProvider(this, SubscriptionViewModel.Factory(application))
                    .get(SubscriptionViewModel::class.java)
                Log.d("SubListActivity", "ViewModel initialized successfully")
            } catch (e: Exception) {
                Log.e("SubListActivity", "Error initializing ViewModel: ${e.message}")
                Toast.makeText(this, getString(R.string.error_initializing_viewmodel, e.message), Toast.LENGTH_LONG).show()
            }
            
            // Apply pixel-retro theme styling with brown (#5b3f2c) background
            window.decorView.setBackgroundColor("#5b3f2c".toColorInt())
            
            // Setup navigation bar
            super.setupNavigationBar()
            
            try {
                setupRecyclerView()
                setupTabLayout()
                setupAddButton()
                // Navigation bar is already set up in BaseActivity
                observeSubscriptions()
                
                // Initial data load with error handling
                safeDataLoad()
            } catch (e: Exception) {
                showErrorMessage("Error initializing UI: ${e.message ?: "Unknown error"}")
            }
        } catch (e: Exception) {
            // Catastrophic failure - show a simple alert and finish
            Toast.makeText(this, R.string.error_init_subscription_view, Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun safeDataLoad() {
        try {
            viewModel.getAllSubscriptions()
        } catch (e: Exception) {
            showErrorMessage("Error loading subscriptions: ${e.message ?: "Unknown error"}")
        }
    }
    
    private fun showErrorMessage(message: String) {
        try {
            binding.tvErrorMessage.text = message
            binding.tvErrorMessage.visibility = View.VISIBLE
            
            // Log error for debugging
            Log.e("SubListActivity", message)
        } catch (e: Exception) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = SubscriptionAdapter(
            onToggleActive = { subscription -> 
                val updatedSubscription = subscription.copy(
                    activeStatus = !subscription.activeStatus,
                    lastUpdated = System.currentTimeMillis()
                )
                viewModel.updateSubscription(updatedSubscription)
            },
            onEdit = { subscription ->
                val intent = Intent(this, SubscriptionDetailActivity::class.java).apply {
                    putExtra(SubscriptionDetailActivity.EXTRA_SUBSCRIPTION_ID, subscription.id)
                }
                startActivity(intent)
            },
            onDelete = { subscription ->
                viewModel.deleteSubscription(subscription)
            }
        )
        
        binding.rvSubscriptions.apply {
            layoutManager = LinearLayoutManager(this@SubscriptionListActivity)
            adapter = this@SubscriptionListActivity.adapter
        }
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.getAllSubscriptions()
                    1 -> viewModel.getActiveSubscriptions()
                    2 -> viewModel.getUpcomingSubscriptions()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupAddButton() {
        binding.fabAddSubscription.setOnClickListener {
            val intent = Intent(this, SubscriptionDetailActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun observeSubscriptions() {
        viewModel.subscriptions.observe(this) { subscriptions ->
            try {
                adapter.submitList(subscriptions)
                updateSummaryCard(subscriptions)
            } catch (e: Exception) {
                // Handle any adapter or UI update exceptions
                binding.tvErrorMessage.text = getString(R.string.error_loading_subscriptions, e.message ?: "")
                binding.tvErrorMessage.visibility = View.VISIBLE
            }
        }
        
        // Observe errors
        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.tvErrorMessage.text = errorMessage
                binding.tvErrorMessage.visibility = View.VISIBLE
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }
        }
    }
    
    private fun updateSummaryCard(subscriptions: List<Subscription>) {
        // Calculate monthly cost
        val monthlyCost = subscriptions
            .filter { it.activeStatus }
            .sumOf { 
                when (it.renewalPeriod) {
                    RenewalPeriod.DAILY -> it.amount * 30
                    RenewalPeriod.WEEKLY -> it.amount * 4.33
                    RenewalPeriod.MONTHLY -> it.amount
                    RenewalPeriod.QUARTERLY -> it.amount / 3
                    RenewalPeriod.YEARLY -> it.amount / 12
                    else -> it.amount
                }
            }
        
        // Format the monthly cost
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.tvMonthlyCost.text = currencyFormat.format(monthlyCost)
        
        // Count active subscriptions
        val activeCount = subscriptions.count { it.activeStatus }
        binding.tvActiveCount.text = activeCount.toString()
        
        // Count upcoming subscriptions (due in the next 7 days)
        val currentTime = System.currentTimeMillis()
        val sevenDaysInMillis = TimeUnit.DAYS.toMillis(7)
        val upcomingCount = subscriptions.count { 
            it.activeStatus && it.renewalDate in currentTime..(currentTime + sevenDaysInMillis)
        }
        binding.tvUpcomingCount.text = upcomingCount.toString()
        
        // Update the bar chart visualization with pixel-retro styling
        try {
            binding.subscriptionBarChart.setSubscriptions(subscriptions)
        } catch (e: Exception) {
            Log.e("SubListActivity", "Error updating bar chart: ${e.message}")
        }
    }
    
    companion object {
        const val EXTRA_TAB_POSITION = "extra_tab_position"
    }
}
