package com.example.pocketsafe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.pocketsafe.R
import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.data.RenewalPeriod
import com.example.pocketsafe.data.CategoryType
import com.example.pocketsafe.databinding.ItemSubscriptionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SubscriptionAdapter(
    private val onToggleActive: (Subscription) -> Unit,
    private val onEdit: (Subscription) -> Unit,
    private val onDelete: (Subscription) -> Unit
) : ListAdapter<Subscription, SubscriptionAdapter.SubscriptionViewHolder>(SubscriptionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubscriptionViewHolder(private val binding: ItemSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(subscription: Subscription) {
            // Set subscription name and category
            binding.tvSubscriptionName.text = subscription.name
            binding.tvCategory.text = getCategoryNameFromId(subscription.categoryId)

            // Format and set the amount
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.tvAmount.text = currencyFormat.format(subscription.amount)

            // Set active status
            if (subscription.activeStatus) {
                binding.btnToggleActive.text = "DEACTIVATE"
            } else {
                binding.btnToggleActive.text = "ACTIVATE"
            }

            // Calculate and display renewal information
            val currentTime = System.currentTimeMillis()
            val daysUntilRenewal = TimeUnit.MILLISECONDS.toDays(subscription.renewalDate - currentTime)

            when {
                daysUntilRenewal < 0 -> {
                    binding.tvRenewalDate.text = "Renewal overdue"
                    binding.tvRenewalDate.setTextColor("#FF0000".toColorInt())
                    binding.ivDueAlert.visibility = View.VISIBLE
                }
                daysUntilRenewal == 0L -> {
                    binding.tvRenewalDate.text = "Renews today"
                    binding.tvRenewalDate.setTextColor("#F3C34E".toColorInt())
                    binding.ivDueAlert.visibility = View.VISIBLE
                }
                daysUntilRenewal <= 7 -> {
                    binding.tvRenewalDate.text = "Renews in ${daysUntilRenewal.toString()} days"
                    binding.tvRenewalDate.setTextColor("#F3C34E".toColorInt())
                    binding.ivDueAlert.visibility = View.VISIBLE
                }
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val renewalDateFormatted = dateFormat.format(Date(subscription.nextDueDate))
                    binding.tvRenewalDate.text = "Renews on $renewalDateFormatted"
                    binding.tvRenewalDate.setTextColor("#F3C34E".toColorInt())
                    binding.ivDueAlert.visibility = View.GONE
                }
            }

            // Set payment method
            binding.tvPaymentMethod.text = subscription.paymentMethod

            // Set button click listeners
            binding.btnToggleActive.setOnClickListener {
                onToggleActive(subscription)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(subscription)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(subscription)
            }
        }
    }

    /**
     * Helper to convert categoryId to category string name
     */
    private fun getCategoryNameFromId(categoryId: Int): String {
        return when (categoryId) {
            CategoryType.FOOD.ordinal -> "Food"
            CategoryType.SHOPPING.ordinal -> "Shopping"
            CategoryType.TRANSPORTATION.ordinal -> "Transport"
            CategoryType.ENTERTAINMENT.ordinal -> "Entertainment"
            CategoryType.UTILITIES.ordinal -> "Utilities"
            CategoryType.HEALTHCARE.ordinal -> "Health"
            CategoryType.EDUCATION.ordinal -> "Education"
            CategoryType.SAVINGS.ordinal -> "Savings"
            CategoryType.INVESTMENT.ordinal -> "Investment"
            CategoryType.BILLS.ordinal -> "Bills"
            CategoryType.SUBSCRIPTION.ordinal -> "Subscription"
            CategoryType.NECESSITY.ordinal -> "Necessity"
            CategoryType.SPORTS.ordinal -> "Sports"
            CategoryType.MEDICAL.ordinal -> "Medical"
            CategoryType.OTHER.ordinal -> "Other"
            else -> "Other"
        }
    }

    class SubscriptionDiffCallback : DiffUtil.ItemCallback<Subscription>() {
        override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem == newItem
        }
    }
}
