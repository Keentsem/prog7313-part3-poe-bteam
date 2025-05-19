package com.example.pocketsafe

/**
 * Data class representing an expense category with its details.
 *
 * @property name The name of the expense category (e.g., "Food & Dining", "Transportation")
 * @property amount The total amount spent in this category
 * @property percentage The percentage of the total budget or spending this category represents (0-100)
 * @property iconResId The resource ID for the icon representing this category
 */
data class ExpenseCategory(
    val name: String,
    val amount: Double,
    val percentage: Int,
    val iconResId: Int
)
