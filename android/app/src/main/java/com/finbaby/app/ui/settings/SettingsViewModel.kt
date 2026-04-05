package com.finbaby.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.FinBabyDatabase
import com.finbaby.app.data.db.entity.ProfileEntity
import com.finbaby.app.data.repository.ProfileRepository
import com.finbaby.app.data.repository.TransactionRepository
import com.finbaby.app.data.repository.CategoryRepository
import com.finbaby.app.util.BackupManager
import com.finbaby.app.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val profile: ProfileEntity? = null,
    val isAppLockEnabled: Boolean = false,
    val isHideAmountsEnabled: Boolean = false,
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val language: String = "English",
    val isEditingSalary: Boolean = false,
    val salaryInput: String = "",
    val backupMessage: String? = null,
    val exportMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val database: FinBabyDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.getProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        profile = profile,
                        salaryInput = profile?.salary?.toString() ?: ""
                    )
                }
            }
        }
    }

    fun setEditingSalary(editing: Boolean) {
        _uiState.update { it.copy(isEditingSalary = editing) }
    }

    fun updateSalaryInput(value: String) {
        _uiState.update { it.copy(salaryInput = value) }
    }

    fun saveSalary() {
        val profile = _uiState.value.profile ?: return
        val salary = _uiState.value.salaryInput.toDoubleOrNull() ?: return
        viewModelScope.launch {
            profileRepository.update(profile.copy(salary = salary))
            _uiState.update { it.copy(isEditingSalary = false) }
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        _uiState.update { it.copy(isAppLockEnabled = enabled) }
    }

    fun toggleHideAmounts(enabled: Boolean) {
        _uiState.update { it.copy(isHideAmountsEnabled = enabled) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationsEnabled = enabled) }
    }

    fun backup() {
        viewModelScope.launch {
            try {
                val path = BackupManager.backup(database, context)
                _uiState.update { it.copy(backupMessage = "Backup saved to $path") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Backup failed: ${e.message}") }
            }
        }
    }

    fun restore() {
        viewModelScope.launch {
            try {
                BackupManager.restore(database, context)
                _uiState.update { it.copy(backupMessage = "Restore completed successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Restore failed: ${e.message}") }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            try {
                transactionRepository.getAllFlow().first().let { transactions ->
                    categoryRepository.getAllCategories().first().let { categories ->
                        val path = CsvExporter.export(context, transactions, categories)
                        _uiState.update { it.copy(exportMessage = "CSV exported to $path") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(backupMessage = null, exportMessage = null) }
    }
}
