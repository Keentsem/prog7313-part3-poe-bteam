package com.example.pocketsafe.data

import com.example.pocketsafe.R

sealed class CategoryIcon(val drawableId: Int, val description: String) {
    object Home : CategoryIcon(R.drawable.home, "Home")
    object Sports : CategoryIcon(R.drawable.sports, "Sports")
    object Medical : CategoryIcon(R.drawable.medix, "Medical")
    object Necessity : CategoryIcon(R.drawable.necessity, "Necessity")
    object Entertainment : CategoryIcon(R.drawable.entertainment, "Entertainment")

    companion object {
        val allIcons = listOf(
            Home,
            Sports,
            Medical,
            Necessity,
            Entertainment
        )
    }
} 