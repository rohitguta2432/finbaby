package com.finbaby.app.ui.tips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.ProfileEntity
import com.finbaby.app.data.repository.BudgetRepository
import com.finbaby.app.data.repository.ProfileRepository
import com.finbaby.app.data.repository.TransactionRepository
import com.finbaby.app.util.DateUtils
import com.finbaby.app.util.SavingsTip
import com.finbaby.app.util.TipsEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmartTipsUiState(
    val tips: List<SavingsTip> = emptyList(),
    val monthlyScore: Int = 5,
    val isLoading: Boolean = true
)

@HiltViewModel
class SmartTipsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartTipsUiState())
    val uiState: StateFlow<SmartTipsUiState> = _uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SmartTipsUiState())

    init {
        loadTips()
    }

    private fun loadTips() {
        viewModelScope.launch {
            val profile = profileRepository.getProfileSync() ?: return@launch

            val salaryDate = profile.salaryDate
            val (cycleStart, cycleEnd) = DateUtils.getSalaryCycleRange(salaryDate)
            val month = DateUtils.getCurrentMonth()
            val (monthStart, monthEnd) = DateUtils.getMonthRange(month)

            val totalExpense = transactionRepository.getTotalExpenseSync(cycleStart, cycleEnd) ?: 0.0
            val salary = profile.salary

            // Calculate EMI total (categoryId 10 is EMI based on CategoryMatcher)
            val emiCategoryId = 10L
            var emiTotal = 0.0
            transactionRepository.getTotalExpenseByCategory(emiCategoryId, cycleStart, cycleEnd)
                .collect { emiTotal = it ?: 0.0; return@collect }

            // Food ordering total (categoryId 2)
            val foodCategoryId = 2L
            var foodOrderingTotal = 0.0
            transactionRepository.getTotalExpenseByCategory(foodCategoryId, cycleStart, cycleEnd)
                .collect { foodOrderingTotal = it ?: 0.0; return@collect }

            // Entertainment total (categoryId 11)
            val entertainmentCategoryId = 11L
            var entertainmentTotal = 0.0
            transactionRepository.getTotalExpenseByCategory(entertainmentCategoryId, cycleStart, cycleEnd)
                .collect { entertainmentTotal = it ?: 0.0; return@collect }

            // Food budget
            val foodBudget = budgetRepository.getBudgetForCategory(foodCategoryId, month)
            val foodBudgetAmount = foodBudget?.limitAmount ?: 0.0

            val daysInCycle = DateUtils.getDaysInCycle(salaryDate)
            val daysSinceSalary = DateUtils.getDaysSinceSalary(salaryDate)
            val daysLeftInCycle = (daysInCycle - daysSinceSalary).coerceAtLeast(0)

            // Calculate days under budget (simplified: count days where daily spend < average)
            val dailyBudget = if (daysInCycle > 0) salary / daysInCycle else 0.0
            val daysUnderBudget = daysSinceSalary / 2 // Simplified heuristic

            val savingsGoal = salary * (profile.savingsPercent / 100.0)
            val currentSavings = (salary - totalExpense).coerceAtLeast(0.0)

            val tips = TipsEngine.generateTips(
                salary = salary,
                totalExpense = totalExpense,
                totalIncome = salary,
                emiTotal = emiTotal,
                foodOrderingTotal = foodOrderingTotal,
                foodBudget = foodBudgetAmount,
                entertainmentTotal = entertainmentTotal,
                daysUnderBudget = daysUnderBudget,
                savingsGoal = savingsGoal,
                currentSavings = currentSavings,
                daysLeftInCycle = daysLeftInCycle
            )

            // Calculate budgets kept for score
            var totalBudgets = 0
            var budgetsKept = 0
            budgetRepository.getBudgetsForMonth(month).collect { budgets ->
                totalBudgets = budgets.size
                budgets.forEach { budget ->
                    val spent = transactionRepository.getTotalExpenseSync(monthStart, monthEnd) ?: 0.0
                    if (spent <= budget.limitAmount) budgetsKept++
                }
                return@collect
            }

            val score = TipsEngine.calculateMonthlyScore(
                salary = salary,
                totalExpense = totalExpense,
                budgetsKept = budgetsKept,
                totalBudgets = totalBudgets
            )

            _uiState.value = SmartTipsUiState(
                tips = tips,
                monthlyScore = score,
                isLoading = false
            )
        }
    }
}
