package com.example.pocketsafe.data

/**
 * Represents different expense and subscription categories
 * Uses the pixel-retro theme colors scheme with gold (#f3c34e) and brown (#5b3f2c)
 */
enum class CategoryType {
    FOOD,
    SHOPPING,
    TRANSPORTATION,
    ENTERTAINMENT,
    UTILITIES,
    HEALTHCARE,
    EDUCATION,
    SAVINGS,
    INVESTMENT,
    BILLS,
    SUBSCRIPTION,
    NECESSITY,
    SPORTS,
    MEDICAL,
    OTHER;

    companion object {
        fun fromString(value: String): CategoryType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                OTHER // Default if not found
            }
        }

        fun getDisplayName(type: CategoryType): String {
            return when (type) {
                FOOD -> "Food & Groceries"
                SHOPPING -> "Shopping"
                TRANSPORTATION -> "Transportation"
                ENTERTAINMENT -> "Entertainment"
                UTILITIES -> "Utilities"
                HEALTHCARE -> "Healthcare"
                EDUCATION -> "Education"
                SAVINGS -> "Savings"
                INVESTMENT -> "Investment"
                BILLS -> "Bills"
                SUBSCRIPTION -> "Subscriptions"
                NECESSITY -> "Necessities"
                SPORTS -> "Sports"
                MEDICAL -> "Medical"
                OTHER -> "Other"
            }
        }

        fun getIconResourceName(type: CategoryType): String {
            return when (type) {
                FOOD -> "ic_category_food"
                SHOPPING -> "ic_category_shopping"
                TRANSPORTATION -> "ic_category_transportation"
                ENTERTAINMENT -> "ic_category_entertainment"
                UTILITIES -> "ic_category_utilities"
                HEALTHCARE -> "ic_category_healthcare"
                EDUCATION -> "ic_category_education"
                SAVINGS -> "ic_category_savings"
                INVESTMENT -> "ic_category_investment"
                BILLS -> "ic_category_bills"
                SUBSCRIPTION -> "ic_category_subscription"
                NECESSITY -> "ic_category_necessity"
                SPORTS -> "ic_category_sports"
                MEDICAL -> "ic_category_medical"
                OTHER -> "ic_category_other"
            }
        }

        fun getColor(type: CategoryType): Int {
            // Return pixel-retro theme gold (#f3c34e) and brown (#5b3f2c) colors
            // Based on category type
            return when (type) {
                FOOD, SHOPPING, ENTERTAINMENT, SPORTS -> 0xFFF3C34E.toInt() // Gold
                TRANSPORTATION, UTILITIES, HEALTHCARE, EDUCATION,
                SAVINGS, INVESTMENT, BILLS, SUBSCRIPTION, 
                NECESSITY, MEDICAL, OTHER -> 0xFF5B3F2C.toInt() // Brown
            }
        }
    }
}
