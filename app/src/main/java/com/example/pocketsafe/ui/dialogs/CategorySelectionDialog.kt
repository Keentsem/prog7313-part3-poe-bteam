package com.example.pocketsafe.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.IconType
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class CategorySelectionDialog : BottomSheetDialogFragment() {
    private var selectedCategory: String? = null
    private var listener: OnCategorySelectedListener? = null
    
    // Method to set the listener directly without requiring fragment context implementation
    fun setListener(listener: OnCategorySelectedListener) {
        this.listener = listener
    }

    interface OnCategorySelectedListener {
        fun onCategorySelected(category: Category)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_category_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardSports = view.findViewById<CardView>(R.id.card_sports)
        val cardEntertainment = view.findViewById<CardView>(R.id.card_entertainment)
        val cardMedix = view.findViewById<CardView>(R.id.card_medix)
        val cardNecessity = view.findViewById<CardView>(R.id.card_necessity)
        val etDescription = view.findViewById<TextInputEditText>(R.id.et_description)
        val etAmount = view.findViewById<TextInputEditText>(R.id.et_amount)

        val categoryCards = mapOf(
            "Sports" to cardSports,
            "Entertainment" to cardEntertainment,
            "Medical" to cardMedix,
            "Necessities" to cardNecessity
        )

        categoryCards.forEach { (category, card) ->
            card?.setOnClickListener {
                selectedCategory = category
                categoryCards.values.forEach { it?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white)) }
                card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selected_category))
            }
        }

        view.findViewById<View>(R.id.btn_save).setOnClickListener {
            if (selectedCategory == null) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val description = etDescription?.text?.toString() ?: ""
            val amountStr = etAmount?.text?.toString() ?: "0"
            val amount = amountStr.toDoubleOrNull() ?: 0.0

            if (description.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Map string category names to pixel-themed icons
            val iconType = when (selectedCategory?.uppercase()) {
                "SPORTS" -> IconType.SPORTS
                "ENTERTAINMENT" -> IconType.ENTERTAINMENT
                "MEDICAL" -> IconType.MEDICAL
                "HEALTHCARE" -> IconType.MEDICAL
                "NECESSITY" -> IconType.NECESSITY
                "FOOD" -> IconType.FOOD
                "SHOPPING" -> IconType.SHOPPING
                "TRANSPORTATION" -> IconType.TRANSPORT
                "UTILITIES" -> IconType.UTILITIES
                "EDUCATION" -> IconType.EDUCATION
                "SAVINGS" -> IconType.SAVINGS
                "INVESTMENT" -> IconType.INVESTMENT
                "BILLS" -> IconType.BILLS
                "SUBSCRIPTION" -> IconType.SUBSCRIPTION
                else -> IconType.OTHER
            }

            val category = Category(
                name = selectedCategory!!,
                description = description,
                monthlyAmount = amount,
                iconType = iconType
            )

            listener?.onCategorySelected(category)
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Only try to get listener from context if not set directly
        if (listener == null && context is OnCategorySelectedListener) {
            listener = context
        }
        // No longer throwing exception since we're using direct listener setting
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        fun newInstance(): CategorySelectionDialog {
            return CategorySelectionDialog()
        }
        
        /**
         * Static helper method to show the dialog with proper typing
         * Maintains pixel-retro theme styling while avoiding type inference issues
         */
        fun show(manager: androidx.fragment.app.FragmentManager, tag: String, listener: OnCategorySelectedListener) {
            val dialog = CategorySelectionDialog()
            dialog.setListener(listener)
            dialog.show(manager, tag)
        }
    }
} 