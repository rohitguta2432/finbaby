package com.finbaby.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import com.finbaby.app.data.repository.CategoryRepository
import com.finbaby.app.data.repository.TransactionRepository
import com.finbaby.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class TimeFilter { ALL, THIS_WEEK, THIS_MONTH }

data class SearchUiState(
    val query: String = "",
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val selectedCategoryId: Long? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getEnabledCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        performSearch()
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        performSearch()
    }

    fun updateTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(timeFilter = filter) }
        performSearch()
    }

    fun updateCategoryFilter(categoryId: Long?) {
        _uiState.update {
            it.copy(selectedCategoryId = if (it.selectedCategoryId == categoryId) null else categoryId)
        }
        performSearch()
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "") }
        performSearch()
    }

    private fun performSearch() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true) }

        val sourceFlow = if (state.query.isNotBlank()) {
            transactionRepository.search(state.query)
        } else {
            transactionRepository.getAllFlow()
        }

        viewModelScope.launch {
            sourceFlow.collect { allTxns ->
                val filtered = allTxns.filter { txn ->
                    val passesTime = when (state.timeFilter) {
                        TimeFilter.ALL -> true
                        TimeFilter.THIS_WEEK -> {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            txn.date >= cal.timeInMillis
                        }
                        TimeFilter.THIS_MONTH -> {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            txn.date >= cal.timeInMillis
                        }
                    }
                    val passesCategory = state.selectedCategoryId == null || txn.categoryId == state.selectedCategoryId
                    passesTime && passesCategory
                }
                _uiState.update { it.copy(transactions = filtered, isLoading = false) }
            }
        }
    }

    fun getGroupedTransactions(): Map<String, List<TransactionEntity>> {
        val txns = _uiState.value.transactions
        return txns.groupBy { txn ->
            when {
                DateUtils.isToday(txn.date) -> "Today"
                DateUtils.isYesterday(txn.date) -> "Yesterday"
                else -> DateUtils.formatDate(txn.date)
            }
        }
    }

    fun getTotalAmount(): Double {
        return _uiState.value.transactions.sumOf { it.amount }
    }
}
