package com.example.pocketsafe.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.example.pocketsafe.R
import com.google.android.material.textfield.TextInputLayout

/**
 * Dialog for handling savings input for the savings goal dial
 * Uses the pixel-retro theme with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class SavingsInputDialog(private val context: Context) {

    private lateinit var dialog: Dialog
    private var onAddSavingsListener: ((Double) -> Unit)? = null

    /**
     * Sets the listener for when the user adds savings
     * @param listener Callback that receives the savings amount entered by user
     */
    fun setOnAddSavingsListener(listener: (Double) -> Unit) {
        onAddSavingsListener = listener
    }

    /**
     * Shows the savings input dialog
     * @param currentAmount Current savings amount
     * @param targetAmount Target savings goal amount
     */
    fun show(currentAmount: Double, targetAmount: Double) {
        // Create dialog instance
        dialog = Dialog(context).apply {
            setCancelable(true)
            setContentView(R.layout.dialog_savings_input)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Update dialog message to show current progress
        val messageTextView = dialog.findViewById<TextView>(R.id.tvDialogMessage)
        messageTextView.text = "Your current savings: $${String.format("%.2f", currentAmount)} of $${String.format("%.2f", targetAmount)}. Enter the amount you want to add to your savings goal."

        // Set click listeners for buttons
        val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)
        val addSavingsButton = dialog.findViewById<Button>(R.id.btnAddSavings)
        val savingsAmountEditText = dialog.findViewById<EditText>(R.id.etSavingsAmount)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        addSavingsButton.setOnClickListener {
            val amountText = savingsAmountEditText.text.toString()
            if (amountText.isBlank()) {
                Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountText.toDouble()
                if (amount <= 0) {
                    Toast.makeText(context, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                onAddSavingsListener?.invoke(amount)
                dialog.dismiss()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    /**
     * Dismisses the dialog if it's showing
     */
    fun dismiss() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
