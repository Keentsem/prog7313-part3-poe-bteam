package com.example.pocketsafe.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pocketsafe.ui.expense.ExpenseListScreen
import com.example.pocketsafe.ui.expense.ExpenseEntryScreen

object Route {
    const val HOME = "home"
    const val EXPENSE_LIST = "expense_list"
    const val EXPENSE_ENTRY = "expense_entry"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Route.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... existing composables ...
        
        composable(Route.EXPENSE_LIST) {
            ExpenseListScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Route.EXPENSE_ENTRY)
                }
            )
        }
        
        composable(Route.EXPENSE_ENTRY) {
            ExpenseEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 