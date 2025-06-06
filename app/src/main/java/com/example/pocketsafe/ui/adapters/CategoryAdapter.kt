package com.example.pocketsafe.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.IconType
import com.example.pocketsafe.databinding.ItemCategoryBinding

class CategoryAdapter : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                imageViewCategory.setImageResource(getIconResourceId(category.iconType))
                textViewCategoryName.text = category.name
                textViewDescription.text = category.description
                // Format amount with currency symbol directly to avoid R.string reference issues
                textViewAmount.text = "$${category.monthlyAmount}"
            }
        }

        private fun getIconResourceId(iconType: IconType): Int {
            return when (iconType) {
                IconType.NECESSITY -> R.drawable.ic_necessity
                IconType.ENTERTAINMENT -> R.drawable.ic_entertainment
                IconType.SPORTS -> R.drawable.ic_sports
                IconType.MEDICAL -> R.drawable.ic_medical
                IconType.SHOPPING -> R.drawable.ic_shopping
                IconType.FOOD -> R.drawable.ic_food
                IconType.TRANSPORT -> R.drawable.ic_transport
                IconType.UTILITIES -> R.drawable.ic_utilities
                IconType.HEALTHCARE -> R.drawable.ic_health // Using existing health icon
                IconType.EDUCATION -> R.drawable.ic_education
                IconType.SAVINGS -> R.drawable.ic_money // Using money icon for savings
                IconType.INVESTMENT -> R.drawable.ic_bank // Using bank icon for investments
                IconType.BILLS -> R.drawable.ic_bills
                IconType.SUBSCRIPTION -> R.drawable.ic_subscription
                IconType.GROCERIES -> R.drawable.ic_food // Mapping to existing icon
                IconType.FUEL -> R.drawable.ic_transport // Mapping to existing icon
                IconType.RENT -> R.drawable.ic_necessity // Mapping to existing icon
                IconType.LUXURY -> R.drawable.ic_entertainment // Mapping to existing icon
                IconType.PIXEL_COIN -> R.drawable.ic_money_circle
                IconType.PIXEL_FOOD -> R.drawable.ic_food
                IconType.PIXEL_CAR -> R.drawable.ic_transport
                IconType.PIXEL_HOUSE -> R.drawable.ic_home
                IconType.PIXEL_GAME -> R.drawable.ic_entertainment
                IconType.PIXEL_HEART -> R.drawable.ic_health
                IconType.PIXEL_STAR -> R.drawable.ic_target
                IconType.PIXEL_MONEY_BAG -> R.drawable.ic_money
                IconType.OTHER -> R.drawable.ic_other
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
} 