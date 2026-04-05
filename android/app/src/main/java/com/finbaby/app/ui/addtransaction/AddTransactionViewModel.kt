package com.finbaby.app.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import com.finbaby.app.data.repository.CategoryRepository
import com.finbaby.app.data.repository.TransactionRepository
import com.finbaby.app.util.CategoryMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val amountText: String = "",
    val isExpense: Boolean = true,
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val accountType: String = "upi",
    val isRecurring: Boolean = false,
    val categories: List<CategoryEntity> = emptyList()
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getEnabledCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun setIsExpense(isExpense: Boolean) {
        _uiState.update { it.copy(isExpense = isExpense) }
    }

    fun onNumpadClick(key: String) {
        val current = _uiState.value.amountText
        // Prevent multiple dots
        if (key == "." && current.contains(".")) return
        // Limit decimal places to 2
        if (current.contains(".")) {
            val decimals = current.substringAfter(".")
            if (decimals.length >= 2) return
        }
        // Prevent leading zeros (except "0.")
        if (current == "0" && key != ".") {
            _uiState.update { it.copy(amountText = key) }
            return
        }
        _uiState.update { it.copy(amountText = current + key) }
    }

    fun onBackspace() {
        val current = _uiState.value.amountText
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(amountText = current.dropLast(1)) }
        }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(note = note) }
        // Auto-suggest category from note
        val suggestedId = CategoryMatcher.suggestCategoryId(note)
        if (suggestedId != null && _uiState.value.selectedCategoryId == null) {
            _uiState.update { it.copy(selectedCategoryId = suggestedId) }
        }
    }

    fun onAccountTypeChanged(type: String) {
        _uiState.update { it.copy(accountType = type) }
    }

    fun onRecurringChanged(isRecurring: Boolean) {
        _uiState.update { it.copy(isRecurring = isRecurring) }
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull() ?: return
        if (amount <= 0) return
        val categoryId = state.selectedCategoryId ?: return

        viewModelScope.launch {
            val transaction = TransactionEntity(
                amount = amount,
                type = if (state.isExpense) "expense" else "income",
                categoryId = categoryId,
                accountType = state.accountType,
                note = state.note,
                date = System.currentTimeMillis(),
                isRecurring = state.isRecurring,
                recurringPeriod = if (state.isRecurring) "monthly" else null
            )
            transactionRepository.insert(transaction)
            onSuccess()
        }
    }
}
