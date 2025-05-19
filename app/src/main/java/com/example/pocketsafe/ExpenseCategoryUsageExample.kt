package com.example.pocketsafe

// Creating expense category instances
val foodCategory = ExpenseCategory(
    name = "Food & Dining",
    amount = 345.67,
    percentage = 65,
    iconResId = R.drawable.ic_food
)

val transportCategory = ExpenseCategory(
    name = "Transportation",
    amount = 198.75,
    percentage = 38,
    iconResId = R.drawable.ic_transport
)

// Creating a list of categories
val categories = listOf(
    foodCategory,
    ExpenseCategory("Shopping", 289.50, 55, R.drawable.ic_shopping),
    transportCategory,
    ExpenseCategory("Bills & Utilities", 156.40, 30, R.drawable.ic_bills),
    ExpenseCategory("Entertainment", 132.80, 25, R.drawable.ic_entertainment)
)