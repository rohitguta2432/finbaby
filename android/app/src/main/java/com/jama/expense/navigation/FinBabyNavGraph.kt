package com.jama.expense.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jama.expense.ui.budget.BudgetScreen
import com.jama.expense.ui.detail.TransactionDetailScreen
import com.jama.expense.ui.home.HomeScreen
import com.jama.expense.ui.onboarding.OnboardingScreen
import com.jama.expense.ui.reports.ReportsScreen
import com.jama.expense.ui.salary.SalarySetupScreen
import com.jama.expense.ui.search.SearchScreen
import com.jama.expense.ui.settings.CategoryManagementScreen
import com.jama.expense.ui.settings.SettingsScreen
import com.jama.expense.ui.tips.SmartTipsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val SALARY_SETUP = "salary_setup"
    const val HOME = "home"
    const val REPORTS = "reports"
    const val BUDGET = "budget"
    const val SETTINGS = "settings"
    const val SMART_TIPS = "smart_tips"
    const val SEARCH = "search"
    const val TRANSACTION_DETAIL = "transaction_detail/{transactionId}"
    const val CATEGORY_MANAGEMENT = "category_management"

    fun transactionDetail(id: Long) = "transaction_detail/$id"
}

@Composable
fun FinBabyNavGraph() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Routes.HOME

    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onNavigateToSalarySetup = {
                    navController.navigate(Routes.SALARY_SETUP) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SALARY_SETUP) {
            SalarySetupScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SALARY_SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
        composable(Routes.REPORTS) {
            ReportsScreen(navController = navController)
        }
        composable(Routes.BUDGET) {
            BudgetScreen(navController = navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                onCategoryManagement = {
                    navController.navigate(Routes.CATEGORY_MANAGEMENT)
                }
            )
        }
        composable(Routes.SMART_TIPS) {
            SmartTipsScreen(navController = navController)
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                currentRoute = currentRoute,
                onNavigate = onNavigate,
                onTransactionClick = { id ->
                    navController.navigate(Routes.transactionDetail(id))
                }
            )
        }
        composable(
            route = Routes.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) {
            TransactionDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CATEGORY_MANAGEMENT) {
            CategoryManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
