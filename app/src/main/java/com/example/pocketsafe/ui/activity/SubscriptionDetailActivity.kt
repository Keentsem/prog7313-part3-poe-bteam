package com.example.pocketsafe.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.pocketsafe.R
import com.example.pocketsafe.data.CategoryType
import com.example.pocketsafe.data.RenewalPeriod
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.databinding.ActivitySubscriptionDetailBinding
import com.example.pocketsafe.ui.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for creating and editing subscriptions
 * Modified to work without Hilt to prevent app crashes
 * Maintains pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class SubscriptionDetailActivity : BaseActivity() {

    private lateinit var binding: ActivitySubscriptionDetailBinding
    private lateinit var viewModel: SubscriptionViewModel
    private var subscriptionId: Long? = null
    private var renewalDate: Long = System.currentTimeMillis()
    private var selectedRenewalPeriod: RenewalPeriod = RenewalPeriod.MONTHLY
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Apply pixel-retro theme styling with brown (#5b3f2c) color
        window.decorView.setBackgroundColor("#5b3f2c".toColorInt())
        
        // Setup navigation bar
        super.setupNavigationBar()
        
        // Initialize ViewModel using Factory pattern instead of Hilt
        try {
            viewModel = ViewModelProvider(this, SubscriptionViewModel.Factory(application))
                .get(SubscriptionViewModel::class.java)
            Log.d("SubDetailActivity", "ViewModel initialized successfully")
        } catch (e: Exception) {
            Log.e("SubDetailActivity", "Error initializing ViewModel: ${e.message}")
            android.widget.Toast.makeText(this, "Error initializing ViewModel: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
        
        // Extract subscription ID as Long
        val idString = intent.getStringExtra(EXTRA_SUBSCRIPTION_ID)
        subscriptionId = idString?.toLongOrNull()
        
        setupSpinners()
        setupDatePicker()
        setupSaveButton()
        setupNotificationToggle()
        
        // If we're editing an existing subscription, load its data
        if (subscriptionId != null) {
            binding.tvTitle.setText(R.string.edit_subscription)
            loadSubscriptionData()
        }
    }
    
    private fun setupSpinners() {
        // Category spinner
        val categories = arrayOf(
            "Entertainment", "Utilities", "Health", "Food", "Transport", 
            "Shopping", "Education", "Services", "Other"
        )
        
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.spinnerCategory.adapter = categoryAdapter
        
        // Renewal period spinner
        val renewalPeriods = RenewalPeriod.entries.map { it.name.capitalize() }.toTypedArray()
        
        val renewalAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            renewalPeriods
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.spinnerRenewalPeriod.adapter = renewalAdapter
        binding.spinnerRenewalPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRenewalPeriod = RenewalPeriod.values()[position]
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            renewalDate = calendar.timeInMillis
            
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
            saveSubscription()
        }
    }
    
    private fun setupNotificationToggle() {
        binding.cbEnableNotifications.setOnCheckedChangeListener { _, isChecked ->
            binding.llNotificationDays.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    private fun loadSubscriptionData() {
        lifecycleScope.launch {
            val subscription = viewModel.getSubscriptionById(subscriptionId!!.toLong())
            subscription?.let {
                binding.etName.setText(it.name)
                binding.etDescription.setText(it.description)
                binding.etAmount.setText(it.amount.toString())
                binding.etPaymentMethod.setText(it.paymentMethod)
                binding.cbActive.isChecked = it.activeStatus
                binding.cbEnableNotifications.isChecked = it.notificationEnabled
                binding.etNotificationDays.setText(it.notificationDays.toString())
                
                // Set spinner selections
                // Find position in adapter by looping through items
                val categoryAdapter = binding.spinnerCategory.adapter
                var categoryPosition = -1
                // First get the category name from categoryId
                val categoryName = getCategoryNameFromId(it.categoryId)
                for (i in 0 until categoryAdapter.count) {
                    if (categoryAdapter.getItem(i).toString() == categoryName) {
                        categoryPosition = i
                        break
                    }
                }
                if (categoryPosition >= 0) {
                    binding.spinnerCategory.setSelection(categoryPosition)
                }
                
                // Find renewal period position
                try {
                    val periods = RenewalPeriod.entries
                    val index = periods.indexOfFirst { period ->
                        period.name == it.frequency.uppercase()
                    }
                    if (index >= 0) {
                        binding.spinnerRenewalPeriod.setSelection(index)
                    }
                } catch (e: Exception) {
                    // Default to MONTHLY if there's an issue
                    val monthlyPosition = RenewalPeriod.entries.indexOf(RenewalPeriod.MONTHLY)
                    if (monthlyPosition >= 0) {
                        binding.spinnerRenewalPeriod.setSelection(monthlyPosition)
                    }
                }
                
                // Set renewal date
                renewalDate = it.renewalDate
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.btnSelectDate.text = dateFormat.format(Date(renewalDate))
                
                // Update UI based on notification enabled
                binding.llNotificationDays.visibility = 
                    if (it.notificationEnabled) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun saveSubscription() {
        // Validate inputs
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = "Name required"
            return
        }
        
        val amountStr = binding.etAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            binding.etAmount.error = "Amount required"
            return
        }
        
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val description = binding.etDescription.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val paymentMethod = binding.etPaymentMethod.text.toString().trim()
        val active = binding.cbActive.isChecked
        val isNotificationEnabled = binding.cbEnableNotifications.isChecked
        
        val notificationDaysStr = binding.etNotificationDays.text.toString().trim()
        val daysBeforeNotification = if (notificationDaysStr.isEmpty()) 3 else notificationDaysStr.toIntOrNull() ?: 3
        
        // Create or update subscription
        val subscription = Subscription(
            id = subscriptionId ?: 0L,
            name = name,
            description = description,
            amount = amount,
            frequency = selectedRenewalPeriod.toString(),
            nextDueDate = renewalDate,
            activeStatus = active,
            paymentMethod = paymentMethod,
            categoryId = getCategoryIdFromString(category),
            lastUpdated = System.currentTimeMillis()
        )
        
        lifecycleScope.launch {
            viewModel.saveSubscription(subscription)
            finish()
        }
    }
    
    /**
     * Helper to convert category string to categoryId
     * Uses pixel-themed CategoryType enum values
     */
    private fun getCategoryIdFromString(category: String): Int {
        return when (category.uppercase()) {
            "FOOD" -> CategoryType.FOOD.ordinal
            "SHOPPING" -> CategoryType.SHOPPING.ordinal
            "TRANSPORTATION" -> CategoryType.TRANSPORTATION.ordinal
            "ENTERTAINMENT" -> CategoryType.ENTERTAINMENT.ordinal
            "UTILITIES" -> CategoryType.UTILITIES.ordinal
            "HEALTHCARE" -> CategoryType.HEALTHCARE.ordinal
            "EDUCATION" -> CategoryType.EDUCATION.ordinal
            "SAVINGS" -> CategoryType.SAVINGS.ordinal
            "INVESTMENT" -> CategoryType.INVESTMENT.ordinal
            "BILLS" -> CategoryType.BILLS.ordinal
            "SUBSCRIPTION" -> CategoryType.SUBSCRIPTION.ordinal
            "NECESSITY" -> CategoryType.NECESSITY.ordinal
            "SPORTS" -> CategoryType.SPORTS.ordinal
            "MEDICAL" -> CategoryType.MEDICAL.ordinal
            else -> CategoryType.OTHER.ordinal
        }
    }
    
    /**
     * Helper to convert categoryId to category string name
     * Reverses the mapping in getCategoryIdFromString
     */
    private fun getCategoryNameFromId(categoryId: Int): String {
        return when (categoryId) {
            CategoryType.FOOD.ordinal -> "Food"
            CategoryType.SHOPPING.ordinal -> "Shopping"
            CategoryType.TRANSPORTATION.ordinal -> "Transport"
            CategoryType.ENTERTAINMENT.ordinal -> "Entertainment"
            CategoryType.UTILITIES.ordinal -> "Utilities"
            CategoryType.HEALTHCARE.ordinal -> "Health"
            CategoryType.EDUCATION.ordinal -> "Education"
            CategoryType.SAVINGS.ordinal -> "Savings"
            CategoryType.INVESTMENT.ordinal -> "Investment"
            CategoryType.BILLS.ordinal -> "Bills"
            CategoryType.SUBSCRIPTION.ordinal -> "Subscription"
            CategoryType.NECESSITY.ordinal -> "Necessity"
            CategoryType.SPORTS.ordinal -> "Sports"
            CategoryType.MEDICAL.ordinal -> "Medical"
            CategoryType.OTHER.ordinal -> "Other"
            else -> "Other"
        }
    }
    
    companion object {
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"
    }
    
    // Extension function to capitalize first letter of a string
    private fun String.capitalize(): String {
        return if (this.isEmpty()) this
        else this[0].uppercaseChar() + this.substring(1).lowercase()
    }
}
