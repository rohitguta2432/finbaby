package com.finbaby.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import com.finbaby.app.data.repository.CategoryRepository
import com.finbaby.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionDetailUiState(
    val transaction: TransactionEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val accountType: String = "cash",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val recurringPeriod: String? = null,
    val type: String = "expense",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L

    private val _uiState = MutableStateFlow(TransactionDetailUiState())
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            categoryRepository.getEnabledCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        viewModelScope.launch {
            val txn = transactionRepository.getById(transactionId)
            if (txn != null) {
                _uiState.update {
                    it.copy(
                        transaction = txn,
                        amount = txn.amount.toString(),
                        selectedCategoryId = txn.categoryId,
                        accountType = txn.accountType,
                        note = txn.note,
                        date = txn.date,
                        isRecurring = txn.isRecurring,
                        recurringPeriod = txn.recurringPeriod,
                        type = txn.type,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amount = value) }
    }

    fun updateCategory(categoryId: Long?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateAccountType(type: String) {
        _uiState.update { it.copy(accountType = type) }
    }

    fun updateNote(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun updateDate(millis: Long) {
        _uiState.update { it.copy(date = millis) }
    }

    fun updateRecurring(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isRecurring = enabled,
                recurringPeriod = if (enabled) (it.recurringPeriod ?: "monthly") else null
            )
        }
    }

    fun updateRecurringPeriod(period: String) {
        _uiState.update { it.copy(recurringPeriod = period) }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun saveChanges() {
        val state = _uiState.value
        val original = state.transaction ?: return
        val amt = state.amount.toDoubleOrNull() ?: return

        val updated = original.copy(
            amount = amt,
            categoryId = state.selectedCategoryId,
            accountType = state.accountType,
            note = state.note,
            date = state.date,
            isRecurring = state.isRecurring,
            recurringPeriod = state.recurringPeriod
        )

        viewModelScope.launch {
            transactionRepository.update(updated)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            transactionRepository.deleteById(transactionId)
            _uiState.update { it.copy(isDeleted = true, showDeleteDialog = false) }
        }
    }
}
