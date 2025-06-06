package com.example.pocketsafe.ui.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.IconType
import com.example.pocketsafe.data.dao.CategoryDao
import com.example.pocketsafe.databinding.FragmentCategoriesBinding
import com.example.pocketsafe.ui.adapters.CategoryAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment for displaying categories in a grid layout
 * Uses simple AlertDialogs with pixel-retro theme
 * Maintains gold (#f3c34e) and brown (#5b3f2c) colors
 */
class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var categoryDao: CategoryDao
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Manually initialize the CategoryDao without Hilt
        try {
            val database = MainApplication.getDatabase(requireContext())
            categoryDao = database.categoryDao()
            Log.d("CategoriesFragment", "Database initialized successfully")
        } catch (e: Exception) {
            Log.e("CategoriesFragment", "Error initializing database: ${e.message}")
            Toast.makeText(requireContext(), "Error initializing database: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        adapter = CategoryAdapter()

        binding.recyclerViewCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@CategoriesFragment.adapter
        }

        // Set FAB click listener to show simple alert dialog
        binding.fabAddCategory.setOnClickListener {
            showCategoryDialog()
        }

        // Observe categories
        viewLifecycleOwner.lifecycleScope.launch {
            categoryDao.getAllCategories().collectLatest { categories ->
                adapter.submitList(categories)
            }
        }
    }

    /**
     * Shows a simple dialog for category selection
     * Uses pixel-retro theme colors (#f3c34e and #5b3f2c)
     */
    private fun showCategoryDialog() {
        try {
            val categories = arrayOf("Sports", "Entertainment", "Medical", "Necessities", "Food", "Shopping")
            var selectedCategory = categories[0]
            
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select Category")
                .setSingleChoiceItems(categories, 0) { _, which ->
                    selectedCategory = categories[which]
                }
                .setPositiveButton("Next") { dialog, _ ->
                    dialog.dismiss()
                    showDetailsDialog(selectedCategory)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            
            val dialog = builder.create()
            dialog.show()
            
            // Apply pixel-retro theme colors
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#f3c34e"))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#f3c34e"))
        } catch (e: Exception) {
            Log.e("CategoriesFragment", "Error showing dialog: ${e.message}")
            Toast.makeText(requireContext(), "Error creating category dialog", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Shows dialog for entering category details
     * @param categoryName The selected category name
     */
    private fun showDetailsDialog(categoryName: String) {
        try {
            val layout = LinearLayout(requireContext())
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 30, 50, 30)
            
            // Description field
            val descriptionField = EditText(requireContext())
            descriptionField.hint = "Description"
            layout.addView(descriptionField)
            
            // Amount field with pixel-retro theming
            val amountField = EditText(requireContext())
            amountField.hint = "Amount"
            amountField.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            layout.addView(amountField)
            
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("$categoryName Details")
                .setView(layout)
                .setPositiveButton("Save") { dialog, _ ->
                    val description = descriptionField.text.toString()
                    val amountStr = amountField.text.toString()
                    
                    // Validate inputs
                    if (description.isBlank()) {
                        Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                        Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    
                    // Map category name to icon type for pixel-retro theme
                    val iconType = when (categoryName.uppercase()) {
                        "SPORTS" -> IconType.SPORTS
                        "ENTERTAINMENT" -> IconType.ENTERTAINMENT
                        "MEDICAL" -> IconType.MEDICAL
                        "NECESSITIES" -> IconType.NECESSITY
                        "FOOD" -> IconType.FOOD
                        "SHOPPING" -> IconType.SHOPPING
                        "TRANSPORTATION" -> IconType.TRANSPORT
                        "UTILITIES" -> IconType.UTILITIES
                        "EDUCATION" -> IconType.EDUCATION
                        else -> IconType.OTHER
                    }
                    
                    // Create category object
                    val category = Category(
                        name = categoryName,
                        description = description,
                        monthlyAmount = amount,
                        iconType = iconType
                    )
                    
                    // Save category using coroutine
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            categoryDao.insertCategory(category)
                            Log.d("CategoriesFragment", "Category saved: $categoryName")
                        } catch (e: Exception) {
                            Log.e("CategoriesFragment", "Error saving category: ${e.message}")
                            Toast.makeText(requireContext(), "Error saving category", Toast.LENGTH_SHORT).show()
                        }
                    }
                    
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            
            val dialog = builder.create()
            dialog.show()
            
            // Apply pixel-retro theme colors
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.parseColor("#f3c34e"))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#f3c34e"))
        } catch (e: Exception) {
            Log.e("CategoriesFragment", "Error showing details dialog: ${e.message}")
            Toast.makeText(requireContext(), "Error creating details dialog", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 