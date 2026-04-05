package com.finbaby.app.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.dao.CategoryTotal
import com.finbaby.app.data.db.entity.BudgetEntity
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.ProfileEntity
import com.finbaby.app.data.repository.BudgetRepository
import com.finbaby.app.data.repository.CategoryRepository
import com.finbaby.app.data.repository.ProfileRepository
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class CategoryBudgetItem(
    val category: CategoryEntity,
    val budget: BudgetEntity?,
    val spent: Double,
    val limit: Double,
    val remaining: Double,
    val progressPercent: Float
)

data class BudgetUiState(
    val currentMonth: String = DateUtils.getCurrentMonth(),
    val monthDisplayName: String = "",
    val profile: ProfileEntity? = null,
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val needsPercent: Int = 50,
    val wantsPercent: Int = 30,
    val savingsPercent: Int = 20,
    val categoryBudgets: List<CategoryBudgetItem> = emptyList(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = true
)

private data class DataBundle(
    val month: String,
    val profile: ProfileEntity?,
    val budgets: List<BudgetEntity>,
    val categories: List<CategoryEntity>,
    val catTotals: List<CategoryTotal>,
    val totalExpense: Double?
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)

    val uiState: StateFlow<BudgetUiState> = combine(
        _currentMonth.flatMapLatest { month ->
            val (start, end) = DateUtils.getMonthRange(month)

            combine(
                profileRepository.getProfile(),
                budgetRepository.getBudgetsForMonth(month),
                categoryRepository.getEnabledCategories(),
                transactionRepository.getExpenseByCategoryGrouped(start, end),
                transactionRepository.getTotalExpense(start, end)
            ) { profile, budgets, categories, catTotals, totalExpense ->
                DataBundle(month, profile, budgets, categories, catTotals, totalExpense)
            }
        },
        _showAddDialog
    ) { data, showDialog ->
        val month = data.month
        val profile = data.profile
        val budgets = data.budgets
        val categories = data.categories
        val catTotals = data.catTotals
        val totalExpense = data.totalExpense
            val budgetMap = budgets.associate { it.categoryId to it }
            val spentMap = catTotals.associate { it.categoryId to it.total }

            val salary = profile?.salary ?: 0.0
            val needsPct = profile?.needsPercent ?: 50
            val wantsPct = profile?.wantsPercent ?: 30
            val savingsPct = profile?.savingsPercent ?: 20

            val totalBudget = budgets.sumOf { it.limitAmount }.let {
                if (it > 0) it else salary
            }
            val spent = totalExpense ?: 0.0

            val categoryBudgets = categories.map { cat ->
                val budget = budgetMap[cat.id]
                val catSpent = spentMap[cat.id] ?: 0.0
                val limit = budget?.limitAmount ?: 0.0
                val remaining = limit - catSpent
                val progress = if (limit > 0) (catSpent / limit).toFloat().coerceIn(0f, 1.5f) else 0f

                CategoryBudgetItem(
                    category = cat,
                    budget = budget,
                    spent = catSpent,
                    limit = limit,
                    remaining = remaining,
                    progressPercent = progress
                )
            }.filter { it.budget != null || it.spent > 0 }
                .sortedByDescending { it.spent }

            BudgetUiState(
                currentMonth = month,
                monthDisplayName = formatMonthDisplay(month),
                profile = profile,
                totalBudget = totalBudget,
                totalSpent = spent,
                totalRemaining = totalBudget - spent,
                needsPercent = needsPct,
                wantsPercent = wantsPct,
                savingsPercent = savingsPct,
                categoryBudgets = categoryBudgets,
                allCategories = categories,
                showAddDialog = showDialog,
                isLoading = false
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetUiState())

    fun showAddBudgetDialog() {
        _showAddDialog.value = true
    }

    fun dismissAddBudgetDialog() {
        _showAddDialog.value = false
    }

    fun saveCategoryBudget(categoryId: Long, amount: Double) {
        viewModelScope.launch {
            val month = _currentMonth.value
            val existing = budgetRepository.getBudgetForCategory(categoryId, month)
            if (existing != null) {
                budgetRepository.update(existing.copy(limitAmount = amount))
            } else {
                budgetRepository.insert(
                    BudgetEntity(
                        categoryId = categoryId,
                        limitAmount = amount,
                        month = month
                    )
                )
            }
            _showAddDialog.value = false
        }
    }

    fun goToPreviousMonth() {
        _currentMonth.value = shiftMonth(_currentMonth.value, -1)
    }

    fun goToNextMonth() {
        _currentMonth.value = shiftMonth(_currentMonth.value, 1)
    }

    private fun shiftMonth(monthStr: String, offset: Int): String {
        val parts = monthStr.split("-")
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            add(Calendar.MONTH, offset)
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
