package com.example.pocketsafe.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import java.text.NumberFormat
import java.util.*

class CategoryLegendAdapter : RecyclerView.Adapter<CategoryLegendAdapter.LegendViewHolder>() {
    private var categories: List<CategoryLegendItem> = emptyList()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun updateCategories(newCategories: List<CategoryLegendItem>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_legend, parent, false)
        return LegendViewHolder(view)
    }

    override fun onBindViewHolder(holder: LegendViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    inner class LegendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorIndicator: View = itemView.findViewById(R.id.colorIndicator)
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val categoryAmountTextView: TextView = itemView.findViewById(R.id.categoryAmountTextView)

        fun bind(item: CategoryLegendItem) {
            colorIndicator.setBackgroundColor(item.color)
            categoryNameTextView.text = item.name
            categoryAmountTextView.text = currencyFormat.format(item.amount)
        }
    }
}

data class CategoryLegendItem(
    val name: String,
    val amount: Double,
    val color: Int,
    val categoryId: Int
)
