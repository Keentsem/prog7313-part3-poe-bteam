package com.example.pocketsafe.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketsafe.data.BillReminder
import com.example.pocketsafe.databinding.ItemBillReminderBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BillReminderAdapter(
    private val onTogglePaid: (BillReminder) -> Unit,
    private val onEdit: (BillReminder) -> Unit,
    private val onDelete: (BillReminder) -> Unit
) : ListAdapter<BillReminder, BillReminderAdapter.BillReminderViewHolder>(BillReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillReminderViewHolder {
        val binding = ItemBillReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BillReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BillReminderViewHolder(private val binding: ItemBillReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(billReminder: BillReminder) {
            // Set bill title
            binding.tvBillTitle.text = billReminder.title

            // Format and set the amount
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.tvAmount.text = currencyFormat.format(billReminder.amount)

            // Calculate and display due date information
            val currentTime = System.currentTimeMillis()
            val daysUntilDue = TimeUnit.MILLISECONDS.toDays(billReminder.dueDate - currentTime)
            
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val dueDateFormatted = dateFormat.format(Date(billReminder.dueDate))

            if (billReminder.paid) {
                // Bill is paid
                binding.ivPaidStamp.visibility = View.VISIBLE
                binding.tvPaymentStatus.text = "Paid"
                binding.tvPaymentStatus.setTextColor(0xFF009900.toInt())
                binding.tvDueDate.text = "Was due on $dueDateFormatted"
                binding.tvDaysStatus.text = ""
                binding.btnTogglePaid.text = "MARK UNPAID"
            } else {
                // Bill is not paid
                binding.ivPaidStamp.visibility = View.GONE
                binding.tvPaymentStatus.text = "Not paid yet"
                
                when {
                    daysUntilDue < 0 -> {
                        // Overdue
                        val daysOverdue = -daysUntilDue
                        binding.tvDueDate.text = "Due on $dueDateFormatted"
                        binding.tvDaysStatus.text = "$daysOverdue days overdue"
                        binding.tvPaymentStatus.setTextColor(0xFFFF0000.toInt())
                    }
                    daysUntilDue == 0L -> {
                        // Due today
                        binding.tvDueDate.text = "Due today"
                        binding.tvDaysStatus.text = "Pay now"
                        binding.tvPaymentStatus.setTextColor(0xFFF3C34E.toInt())
                    }
                    else -> {
                        // Future due date
                        binding.tvDueDate.text = "Due on $dueDateFormatted"
                        binding.tvDaysStatus.text = "$daysUntilDue days left"
                        binding.tvPaymentStatus.setTextColor(0xFFF3C34E.toInt())
                    }
                }
                
                binding.btnTogglePaid.text = "MARK PAID"
            }

            // Set button click listeners
            binding.btnTogglePaid.setOnClickListener {
                onTogglePaid(billReminder)
            }

            binding.btnEdit.setOnClickListener {
                onEdit(billReminder)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(billReminder)
            }
        }
    }

    class BillReminderDiffCallback : DiffUtil.ItemCallback<BillReminder>() {
        override fun areItemsTheSame(oldItem: BillReminder, newItem: BillReminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BillReminder, newItem: BillReminder): Boolean {
            return oldItem == newItem
        }
    }
}
