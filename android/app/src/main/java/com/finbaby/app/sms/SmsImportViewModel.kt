package com.finbaby.app.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.TransactionEntity
import com.finbaby.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmsImportUiState(
    val isLoading: Boolean = false,
    val parsedTransactions: List<SmsTransaction> = emptyList(),
    val selectedIds: Set<String> = emptySet(), // smsIds selected for import
    val importedCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class SmsImportViewModel @Inject constructor(
    private val smsTransactionParser: SmsTransactionParser,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsImportUiState())
    val uiState: StateFlow<SmsImportUiState> = _uiState.asStateFlow()

    // Track already-imported SMS IDs via note field (stores smsId)
    private val importedSmsIds = mutableSetOf<String>()

    /**
     * Scan SMS and show parsed transactions for user review.
     * @param sinceTimestamp Only scan SMS after this time (default: last 30 days)
     */
    fun scanSms(sinceTimestamp: Long = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val transactions = smsTransactionParser.parseNewTransactions(
                    sinceTimestamp = sinceTimestamp,
                    existingSmsIds = importedSmsIds
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    parsedTransactions = transactions,
                    selectedIds = transactions.map { it.smsId }.toSet() // select all by default
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to read SMS: ${e.message}"
                )
            }
        }
    }

    fun toggleSelection(smsId: String) {
        val current = _uiState.value.selectedIds.toMutableSet()
        if (smsId in current) current.remove(smsId) else current.add(smsId)
        _uiState.value = _uiState.value.copy(selectedIds = current)
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedIds = _uiState.value.parsedTransactions.map { it.smsId }.toSet()
        )
    }

    fun deselectAll() {
        _uiState.value = _uiState.value.copy(selectedIds = emptySet())
    }

    /**
     * Import selected transactions into Room DB.
     */
    fun importSelected() {
        viewModelScope.launch {
            val state = _uiState.value
            val toImport = state.parsedTransactions.filter { it.smsId in state.selectedIds }

            var count = 0
            for (smsTxn in toImport) {
                val entity = TransactionEntity(
                    amount = smsTxn.amount,
                    type = smsTxn.type,
                    categoryId = smsTxn.categoryId,
                    accountType = smsTxn.accountType,
                    note = smsTxn.note,
                    date = smsTxn.date,
                    isRecurring = false
                )
                transactionRepository.insert(entity)
                importedSmsIds.add(smsTxn.smsId)
                count++
            }

            _uiState.value = _uiState.value.copy(
                importedCount = count,
                parsedTransactions = state.parsedTransactions.filter { it.smsId !in state.selectedIds }
            )
        }
    }

    /**
     * Update category for a parsed transaction before importing.
     */
    fun updateCategory(smsId: String, newCategoryId: Long?) {
        val updated = _uiState.value.parsedTransactions.map {
            if (it.smsId == smsId) it.copy(categoryId = newCategoryId) else it
        }
        _uiState.value = _uiState.value.copy(parsedTransactions = updated)
    }
}
