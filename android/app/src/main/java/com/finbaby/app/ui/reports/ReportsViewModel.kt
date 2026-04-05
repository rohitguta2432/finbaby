package com.finbaby.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.dao.CategoryTotal
import com.finbaby.app.data.db.dao.DailyTotal
import com.finbaby.app.data.repository.CategoryRepository
import com.finbaby.app.data.repository.TransactionRepository
import com.finbaby.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class CategorySpending(
    val categoryId: Long?,
    val categoryName: String,
    val total: Double,
    val previousTotal: Double
)

data class ReportsUiState(
    val currentMonth: String = DateUtils.getCurrentMonth(),
    val monthDisplayName: String = "",
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    val uiState: StateFlow<ReportsUiState> = _currentMonth.flatMapLatest { month ->
        val (start, end) = DateUtils.getMonthRange(month)
        val prevMonth = getPreviousMonth(month)
        val (prevStart, prevEnd) = DateUtils.getMonthRange(prevMonth)

        combine(
            transactionRepository.getTotalIncome(start, end),
            transactionRepository.getTotalExpense(start, end),
            transactionRepository.getExpenseByCategoryGrouped(start, end),
            transactionRepository.getDailyExpenses(start, end),
            transactionRepository.getExpenseByCategoryGrouped(prevStart, prevEnd),
            categoryRepository.getAllCategories()
        ) { values ->
            val income = values[0] as? Double ?: 0.0
            val expense = values[1] as? Double ?: 0.0
            @Suppress("UNCHECKED_CAST")
            val catTotals = values[2] as? List<CategoryTotal> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val daily = values[3] as? List<DailyTotal> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val prevCatTotals = values[4] as? List<CategoryTotal> ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val categories = values[5] as? List<com.finbaby.app.data.db.entity.CategoryEntity> ?: emptyList()

            val prevMap = prevCatTotals.associate { it.categoryId to it.total }
            val catMap = categories.associate { it.id to it.name }

            val categorySpending = catTotals.map { ct ->
                CategorySpending(
                    categoryId = ct.categoryId,
                    categoryName = catMap[ct.categoryId] ?: "Other",
                    total = ct.total,
                    previousTotal = prevMap[ct.categoryId] ?: 0.0
                )
            }

            ReportsUiState(
                currentMonth = month,
                monthDisplayName = formatMonthDisplay(month),
                totalIncome = income,
                totalExpense = expense,
                balance = income - expense,
                categoryTotals = catTotals,
                categorySpending = categorySpending,
                dailyTotals = daily,
                isLoading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun goToPreviousMonth() {
        _currentMonth.value = getPreviousMonth(_currentMonth.value)
    }

    fun goToNextMonth() {
        _currentMonth.value = getNextMonth(_currentMonth.value)
    }

    private fun getPreviousMonth(monthStr: String): String {
        val parts = monthStr.split("-")
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            add(Calendar.MONTH, -1)
        }
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
    }

    private fun getNextMonth(monthStr: String): String {
        val parts = monthStr.split("-")
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            add(Calendar.MONTH, 1)
        }
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
    }

    private fun formatMonthDisplay(monthStr: String): String {
        val parts = monthStr.split("-")
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
        }
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }
}
