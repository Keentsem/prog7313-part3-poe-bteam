package com.example.pocketsafe.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Expense
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    private var expenses: List<Expense> = emptyList()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount() = expenses.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)
        private val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(expense: Expense) {
            // Using categoryId instead of category since Expense doesn't have a category property
            categoryTextView.text = "Category ID: ${expense.categoryId}"
            amountTextView.text = NumberFormat.getCurrencyInstance(Locale.US).format(expense.amount)
            // Convert the Long timestamp to a readable date string
            val dateFormat = java.text.SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val dateString = dateFormat.format(Date(expense.date))
            dateTextView.text = dateString
            descriptionTextView.text = expense.description ?: "No description"
        }
    }
}