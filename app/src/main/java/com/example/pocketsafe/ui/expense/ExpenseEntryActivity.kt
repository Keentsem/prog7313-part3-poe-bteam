package com.example.pocketsafe.ui.expense

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.pocketsafe.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for entering new expenses
 * Modified to work without Hilt to prevent app crashes
 * Maintains pixel-retro theme styling with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class ExpenseEntryActivity : AppCompatActivity() {
    private lateinit var dateEditText: EditText
    private lateinit var categoryButton: Button
    private lateinit var descriptionEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var submitButton: ImageButton
    private lateinit var cancelButton: ImageButton
    private lateinit var addPhotosButton: Button
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var tvNoPhoto: TextView

    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)

        initializeViews()
        setupDatePicker()
        setupCategoryButton()
        setupButtons()
    }

    private fun initializeViews() {
        dateEditText = findViewById(R.id.dateEditText)
        categoryButton = findViewById(R.id.categoryButton)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        amountEditText = findViewById(R.id.amountEditText)
        submitButton = findViewById(R.id.submitButton)
        cancelButton = findViewById(R.id.cancelButton)
        addPhotosButton = findViewById(R.id.addPhotosButton)
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview)
        tvNoPhoto = findViewById(R.id.tvNoPhoto)

        // Set current date
        dateEditText.setText(dateFormatter.format(calendar.time))
    }

    private fun setupDatePicker() {
        dateEditText.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dateEditText.setText(dateFormatter.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupCategoryButton() {
        categoryButton.setOnClickListener {
            showCategorySelectionDialog()
        }
    }

    private fun showCategorySelectionDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_category_selection)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Set up category card click listeners
        setupCategoryCard(dialog, R.id.card_sports, "Sports")
        setupCategoryCard(dialog, R.id.card_entertainment, "Entertainment")
        setupCategoryCard(dialog, R.id.card_medix, "Medical")
        setupCategoryCard(dialog, R.id.card_necessity, "Necessities")

        // Set up save button
        dialog.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val description = dialog.findViewById<TextInputEditText>(R.id.et_description).text.toString()
            val amount = dialog.findViewById<TextInputEditText>(R.id.et_amount).text.toString()

            if (selectedCategory != null) {
                descriptionEditText.setText(description)
                amountEditText.setText(amount)
                categoryButton.text = selectedCategory
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupCategoryCard(dialog: Dialog, cardId: Int, category: String) {
        dialog.findViewById<CardView>(cardId).setOnClickListener {
            selectedCategory = category
            // Reset all cards to default state
            resetCategoryCards(dialog)
            // Highlight selected card
            dialog.findViewById<CardView>(cardId).setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.selected_category)
            )
        }
    }

    private fun resetCategoryCards(dialog: Dialog) {
        val cards = listOf(
            dialog.findViewById<CardView>(R.id.card_sports),
            dialog.findViewById<CardView>(R.id.card_entertainment),
            dialog.findViewById<CardView>(R.id.card_medix),
            dialog.findViewById<CardView>(R.id.card_necessity)
        )
        cards.forEach { 
            it.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.category_card_default)
            )
        }
    }

    private fun setupButtons() {
        submitButton.setOnClickListener {
            if (validateInputs()) {
                saveExpense()
                showSuccessDialog()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }

        addPhotosButton.setOnClickListener {
            // TODO: Implement photo capture/selection
            // For now, just show a placeholder
            ivPhotoPreview.visibility = View.VISIBLE
            tvNoPhoto.visibility = View.GONE
        }
    }

    private fun validateInputs(): Boolean {
        if (descriptionEditText.text.toString().trim().isEmpty()) {
            descriptionEditText.error = "Please enter a description"
            return false
        }
        if (amountEditText.text.toString().trim().isEmpty()) {
            amountEditText.error = "Please enter an amount"
            return false
        }
        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveExpense() {
        // TODO: Implement actual expense saving logic
        // This is where you would save to your database
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Your expense has been saved successfully!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
} 