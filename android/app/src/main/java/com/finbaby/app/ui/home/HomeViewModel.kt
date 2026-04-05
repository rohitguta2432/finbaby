package com.finbaby.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import com.finbaby.app.data.repository.CategoryRepository
import com.finbaby.app.data.repository.ProfileRepository
import com.finbaby.app.data.repository.TransactionRepository
import com.finbaby.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionWithCategory(
    val transaction: TransactionEntity,
    val category: CategoryEntity?
)

data class HomeUiState(
    val totalSalary: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val remaining: Double = 0.0,
    val daysSinceSalary: Int = 0,
    val daysInCycle: Int = 30,
    val needsSpent: Double = 0.0,
    val needsLimit: Double = 0.0,
    val wantsSpent: Double = 0.0,
    val wantsLimit: Double = 0.0,
    val savingsSpent: Double = 0.0,
    val savingsLimit: Double = 0.0,
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            profileRepository.getProfile().collect { profile ->
                if (profile == null) return@collect

                val salary = profile.salary
                val salaryDate = profile.salaryDate
                val (cycleStart, cycleEnd) = DateUtils.getSalaryCycleRange(salaryDate)
                val daysSince = DateUtils.getDaysSinceSalary(salaryDate)
                val daysInCycle = DateUtils.getDaysInCycle(salaryDate)

                val needsLimit = salary * profile.needsPercent / 100.0
                val wantsLimit = salary * profile.wantsPercent / 100.0
                val savingsLimit = salary * profile.savingsPercent / 100.0

                // Collect transactions and categories together
                combine(
                    transactionRepository.getByDateRange(cycleStart, cycleEnd),
                    transactionRepository.getTotalExpense(cycleStart, cycleEnd),
                    transactionRepository.getTotalIncome(cycleStart, cycleEnd),
                    categoryRepository.getEnabledCategories()
                ) { transactions, totalExpense, totalIncome, categories ->

                    val categoryMap = categories.associateBy { it.id }
                    val expense = totalExpense ?: 0.0
                    val income = totalIncome ?: 0.0

                    // Calculate budget bucket totals
                    var needsSpent = 0.0
                    var wantsSpent = 0.0
                    var savingsSpent = 0.0

                    transactions.filter { it.type == "expense" }.forEach { tx ->
                        val cat = tx.categoryId?.let { categoryMap[it] }
                        when (cat?.budgetType) {
                            "needs" -> needsSpent += tx.amount
                            "wants" -> wantsSpent += tx.amount
                            "savings" -> savingsSpent += tx.amount
                            else -> needsSpent += tx.amount // default to needs
                        }
                    }

                    // Recent transactions (last 10)
                    val recent = transactions.take(10).map { tx ->
                        TransactionWithCategory(
                            transaction = tx,
                            category = tx.categoryId?.let { categoryMap[it] }
                        )
                    }

                    HomeUiState(
                        totalSalary = salary,
                        totalExpense = expense,
                        totalIncome = income,
                        remaining = salary - expense,
                        daysSinceSalary = daysSince,
                        daysInCycle = daysInCycle,
                        needsSpent = needsSpent,
                        needsLimit = needsLimit,
                        wantsSpent = wantsSpent,
                        wantsLimit = wantsLimit,
                        savingsSpent = savingsSpent,
                        savingsLimit = savingsLimit,
                        recentTransactions = recent,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }
    }
}
