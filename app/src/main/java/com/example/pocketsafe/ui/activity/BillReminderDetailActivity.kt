package com.example.pocketsafe.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.pocketsafe.databinding.ActivityBillReminderDetailBinding
import com.example.pocketsafe.data.BillReminder
import com.example.pocketsafe.ui.viewmodel.BillReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

class BillReminderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillReminderDetailBinding
    private lateinit var viewModel: BillReminderViewModel
    private var billReminderId: String? = null
    private var dueDate: Long = System.currentTimeMillis()
    private var repeatInterval: String = "MONTHLY"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillReminderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel using Factory pattern instead of Hilt
        viewModel = ViewModelProvider(this, BillReminderViewModel.Factory(application))
            .get(BillReminderViewModel::class.java)
        
        billReminderId = intent.getStringExtra(EXTRA_BILL_REMINDER_ID)
        
        setupSpinner()
        setupDatePicker()
        setupSaveButton()
        setupNotificationToggle()
        setupRepeatingToggle()
        
        // If we're editing an existing bill reminder, load its data
        if (billReminderId != null) {
            binding.tvTitle.text = "Edit Bill Reminder"
            loadBillReminderData()
        }
    }
    
    private fun setupSpinner() {
        // Repeat interval spinner
        val repeatIntervals = arrayOf("DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY")
        
        val repeatAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            repeatIntervals.map { it.capitalize() }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.spinnerRepeatInterval.adapter = repeatAdapter
        binding.spinnerRepeatInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                repeatInterval = repeatIntervals[position]
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Set default to MONTHLY
        val monthlyPosition = repeatIntervals.indexOf("MONTHLY")
        if (monthlyPosition >= 0) {
            binding.spinnerRepeatInterval.setSelection(monthlyPosition)
        }
    }
    
    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            dueDate = calendar.timeInMillis
            
            // Update button text
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.btnSelectDate.text = dateFormat.format(calendar.time)
        }
        
        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        // Set initial date display
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.btnSelectDate.text = dateFormat.format(calendar.time)
    }
    
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveBillReminder()
        }
    }
    
    private fun setupNotificationToggle() {
        binding.cbEnableNotifications.setOnCheckedChangeListener { _, isChecked ->
            binding.llNotificationDays.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupRepeatingToggle() {
        binding.cbRepeating.setOnCheckedChangeListener { _, isChecked ->
            binding.llRepeatOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    private fun loadBillReminderData() {
        viewModel.getBillReminderById(billReminderId!!).observe(this) { billReminder ->
            billReminder?.let {
                binding.etTitle.setText(it.title)
                binding.etAmount.setText(it.amount.toString())
                binding.cbPaid.isChecked = it.paid
                
                // Set due date
                dueDate = it.dueDate
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.btnSelectDate.text = dateFormat.format(Date(dueDate))
                
                // For a real app, we would also load repeat settings and notification settings
                // from additional fields in the BillReminder class
            }
        }
    }
    
    private fun saveBillReminder() {
        // Validate inputs
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.etTitle.error = "Title required"
            return
        }
        
        val amountStr = binding.etAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount required"
            return
        }
        
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val paid = binding.cbPaid.isChecked
        
        // Create or update bill reminder
        val billReminder = BillReminder(
            id = billReminderId ?: UUID.randomUUID().toString(),
            title = title,
            amount = amount,
            dueDate = dueDate,
            paid = paid
        )
        
        if (billReminderId == null) {
            viewModel.addBillReminder(billReminder)
            
            // If this is a repeating bill, we could schedule the creation
            // of future reminders here
        } else {
            viewModel.updateBillReminder(billReminder)
        }
        
        finish()
    }
    
    companion object {
        const val EXTRA_BILL_REMINDER_ID = "extra_bill_reminder_id"
    }
    
    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return if (this.isEmpty()) this
        else this[0].uppercaseChar() + this.substring(1).lowercase()
    }
}
