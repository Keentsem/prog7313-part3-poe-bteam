package com.example.pocketsafe.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.IconType
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.PocketSafeApplication
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {

    private lateinit var etCategoryName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etMonthlyAmount: EditText
    private lateinit var btnSaveCategory: Button
    private lateinit var btnBackToMenu: Button
    
    @Inject
    lateinit var categoryDao: CategoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Initialize views
        initializeViews()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        etCategoryName = findViewById(R.id.etCategoryName)
        etDescription = findViewById(R.id.etDescription)
        etMonthlyAmount = findViewById(R.id.etMonthlyAmount)
        btnSaveCategory = findViewById(R.id.btnSaveCategory)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)
    }

    private fun setupClickListeners() {
        btnSaveCategory.setOnClickListener {
            saveCategory()
        }

        btnBackToMenu.setOnClickListener {
            finish()
        }
    }

    private fun saveCategory() {
        val categoryName = etCategoryName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val monthlyAmount = etMonthlyAmount.text.toString().toDoubleOrNull() ?: 0.0

        when {
            categoryName.isBlank() -> {
                showError("Please enter a category name")
            }
            description.isBlank() -> {
                showError("Please enter a description")
            }
            monthlyAmount <= 0 -> {
                showError("Please enter a valid monthly amount")
            }
            else -> {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val category = Category(
                            name = categoryName,
                            description = description,
                            monthlyAmount = monthlyAmount,
                            iconType = IconType.NECESSITY
                        )
                        categoryDao.insertCategory(category)
                    }
                    
                    withContext(Dispatchers.Main) {
                        showSuccess("Category saved successfully!")
                        clearFields()
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearFields() {
        etCategoryName.text.clear()
        etDescription.text.clear()
        etMonthlyAmount.text.clear()
        etCategoryName.requestFocus()
    }
} 