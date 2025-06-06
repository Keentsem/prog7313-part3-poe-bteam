package com.example.pocketsafe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.ui.dashboard.DashboardScreen
import com.example.pocketsafe.ui.expense.ExpenseEntryScreen
import com.example.pocketsafe.ui.expense.ExpenseListScreen
import com.example.pocketsafe.ui.expense.ExpenseViewModel
import com.example.pocketsafe.ui.settings.SettingsScreen
import com.example.pocketsafe.ui.subscription.SubscriptionDetailScreen
import com.example.pocketsafe.ui.subscription.SubscriptionListScreen
import com.example.pocketsafe.ui.subscription.SubscriptionEntryScreen
import com.example.pocketsafe.ui.viewmodel.SubscriptionViewModel

// Screens in the app defined as sealed class for type safety
// Uses the gold (#f3c34e) and brown (#5b3f2c) pixel-retro theme
sealed class PocketSafeDestination(val route: String) {
    data object Dashboard : PocketSafeDestination(route = "dashboard")
    data object ExpenseList : PocketSafeDestination(route = "expense_list")
    data object ExpenseEntry : PocketSafeDestination(route = "expense_entry")
    data object SubscriptionList : PocketSafeDestination(route = "subscription_list")
    data object SubscriptionDetail : PocketSafeDestination(route = "subscription_detail")
    data object SubscriptionEntry : PocketSafeDestination(route = "subscription_entry")
    data object Settings : PocketSafeDestination(route = "settings")
}

@Composable
fun PocketSafeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = PocketSafeDestination.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard Screen - serves as the main entry point
        composable(route = PocketSafeDestination.Dashboard.route) {
            DashboardScreen(
                onNavigateToSubscriptions = {
                    navController.navigate(PocketSafeDestination.SubscriptionList.route)
                },
                onNavigateToExpenses = {
                    navController.navigate(PocketSafeDestination.ExpenseList.route)
                },
                onNavigateToSettings = {
                    navController.navigate(PocketSafeDestination.Settings.route)
                }
            )
        }
        
        // Expenses screens
        composable(route = PocketSafeDestination.ExpenseList.route) {
            val expenseViewModel: ExpenseViewModel = viewModel(
                factory = ExpenseViewModel.Factory(MainApplication.instance)
            )
            
            ExpenseListScreen(
                onNavigateToAddExpense = {
                    navController.navigate(PocketSafeDestination.ExpenseEntry.route)
                },
                viewModel = expenseViewModel
            )
        }
        
        composable(route = PocketSafeDestination.ExpenseEntry.route) {
            val expenseViewModel: ExpenseViewModel = viewModel(
                factory = ExpenseViewModel.Factory(MainApplication.instance)
            )
            
            ExpenseEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = expenseViewModel
            )
        }
        
        // Subscriptions
        composable(route = PocketSafeDestination.SubscriptionList.route) {
            val subscriptionViewModel: SubscriptionViewModel = viewModel(
                factory = SubscriptionViewModel.Factory(MainApplication.instance)
            )
            
            SubscriptionListScreen(
                onNavigateToSubscriptionDetail = { subscriptionId ->
                    navController.navigate("${PocketSafeDestination.SubscriptionDetail.route}/$subscriptionId")
                },
                onNavigateToAddSubscription = {
                    navController.navigate(PocketSafeDestination.SubscriptionEntry.route)
                },
                viewModel = subscriptionViewModel
            )
        }
        
        // Subscription Detail
        composable(
            route = "${PocketSafeDestination.SubscriptionDetail.route}/{subscriptionId}",
            arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: 0L
            val subscriptionViewModel: SubscriptionViewModel = viewModel(
                factory = SubscriptionViewModel.Factory(MainApplication.instance)
            )
            
            SubscriptionDetailScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditSubscription = { id ->
                    navController.navigate("${PocketSafeDestination.SubscriptionEntry.route}/$id")
                },
                viewModel = subscriptionViewModel
            )
        }
        
        // Add new subscription
        composable(route = PocketSafeDestination.SubscriptionEntry.route) {
            val subscriptionViewModel: SubscriptionViewModel = viewModel(
                factory = SubscriptionViewModel.Factory(MainApplication.instance)
            )
            
            SubscriptionEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = subscriptionViewModel
            )
        }
        
        // Edit existing subscription
        composable(
            route = "${PocketSafeDestination.SubscriptionEntry.route}/{subscriptionId}",
            arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: 0L
            val subscriptionViewModel: SubscriptionViewModel = viewModel(
                factory = SubscriptionViewModel.Factory(MainApplication.instance)
            )
            
            SubscriptionEntryScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = subscriptionViewModel
            )
        }
        
        // Settings Screen
        composable(route = PocketSafeDestination.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
