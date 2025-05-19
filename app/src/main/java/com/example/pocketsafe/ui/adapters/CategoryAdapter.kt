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