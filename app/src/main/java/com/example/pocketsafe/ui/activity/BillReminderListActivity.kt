package com.example.pocketsafe.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketsafe.data.BillReminder
import com.example.pocketsafe.databinding.ActivityBillReminderListBinding
import com.example.pocketsafe.ui.adapters.BillReminderAdapter
import com.example.pocketsafe.ui.viewmodel.BillReminderViewModel
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BillReminderListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillReminderListBinding
    private lateinit var viewModel: BillReminderViewModel
    private lateinit var adapter: BillReminderAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillReminderListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel using Factory pattern instead of Hilt
        viewModel = ViewModelProvider(this, BillReminderViewModel.Factory(application))
            .get(BillReminderViewModel::class.java)
        
        setupRecyclerView()
        setupTabLayout()
        setupAddButton()
        observeBillReminders()
        
        // Initial data load
        viewModel.getAllBillReminders()
    }
    
    private fun setupRecyclerView() {
        adapter = BillReminderAdapter(
            onTogglePaid = { billReminder -> 
                val updatedBillReminder = billReminder.copy(
                    paid = !billReminder.paid
                )
                viewModel.updateBillReminder(updatedBillReminder)
                
                // If this is a repeating bill and we're marking it as paid,
                // we could create the next bill reminder here
            },
            onEdit = { billReminder ->
                val intent = Intent(this, BillReminderDetailActivity::class.java).apply {
                    putExtra(BillReminderDetailActivity.EXTRA_BILL_REMINDER_ID, billReminder.id)
                }
                startActivity(intent)
            },
            onDelete = { billReminder ->
                viewModel.deleteBillReminder(billReminder)
            }
        )
        
        binding.rvBillReminders.apply {
            layoutManager = LinearLayoutManager(this@BillReminderListActivity)
            adapter = this@BillReminderListActivity.adapter
        }
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.getAllBillReminders()
                    1 -> viewModel.getUpcomingBillReminders()
                    2 -> viewModel.getPaidBillReminders()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun setupAddButton() {
        binding.fabAddBill.setOnClickListener {
            val intent = Intent(this, BillReminderDetailActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun observeBillReminders() {
        viewModel.billReminders.observe(this) { billReminders ->
            adapter.submitList(billReminders)
            updateSummaryCard(billReminders)
        }
    }
    
    private fun updateSummaryCard(billReminders: List<BillReminder>) {
        // Get current month
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Filter bills for current month
        val currentMonthBills = billReminders.filter {
            val billDate = Calendar.getInstance().apply { timeInMillis = it.dueDate }
            billDate.get(Calendar.MONTH) == currentMonth && 
            billDate.get(Calendar.YEAR) == currentYear
        }
        
        // Calculate total amount for this month
        val monthlyTotal = currentMonthBills.sumOf { it.amount }
        
        // Format the monthly total
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.tvMonthlyTotal.text = currencyFormat.format(monthlyTotal)
        
        // Count paid and unpaid bills for this month
        val paidCount = currentMonthBills.count { it.paid }
        val unpaidCount = currentMonthBills.size - paidCount
        
        binding.tvPaidCount.text = paidCount.toString()
        binding.tvUnpaidCount.text = unpaidCount.toString()
    }
    
    companion object {
        const val EXTRA_TAB_POSITION = "extra_tab_position"
    }
}
