package com.example.pocketsafe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.databinding.ItemExpenseCategoryBinding
import java.text.NumberFormat
import java.util.*

class ExpenseCategoryAdapter(private val categories: List<ExpenseCategory>) :
    RecyclerView.Adapter<ExpenseCategoryAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount() = categories.size

    inner class ViewHolder(private val binding: ItemExpenseCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: ExpenseCategory) {
            binding.categoryName.text = category.name
            binding.categoryAmount.text = currencyFormat.format(category.amount)
            binding.categoryProgress.progress = category.percentage
            binding.categoryIcon.setImageResource(category.iconResId)
        }
    }
}