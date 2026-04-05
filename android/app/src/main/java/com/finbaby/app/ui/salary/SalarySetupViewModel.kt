package com.finbaby.app.ui.salary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.ProfileEntity
import com.finbaby.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SalarySetupUiState(
    val salaryText: String = "",
    val salary: Double = 0.0,
    val salaryDate: Int = 1,
    val needsPercent: Int = 50,
    val wantsPercent: Int = 30,
    val savingsPercent: Int = 20
)

@HiltViewModel
class SalarySetupViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalarySetupUiState())
    val uiState: StateFlow<SalarySetupUiState> = _uiState.asStateFlow()

    fun onSalaryChanged(text: String) {
        val filtered = text.filter { it.isDigit() || it == '.' }
        val amount = filtered.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(salaryText = filtered, salary = amount) }
    }

    fun onSalaryDateChanged(day: Int) {
        _uiState.update { it.copy(salaryDate = day) }
    }

    fun onNeedsPercentChanged(percent: Int) {
        _uiState.update { it.copy(needsPercent = percent) }
    }

    fun onWantsPercentChanged(percent: Int) {
        _uiState.update { it.copy(wantsPercent = percent) }
    }

    fun onSavingsPercentChanged(percent: Int) {
        _uiState.update { it.copy(savingsPercent = percent) }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.salary <= 0) return
        if (state.needsPercent + state.wantsPercent + state.savingsPercent != 100) return

        viewModelScope.launch {
            val profile = ProfileEntity(
                salary = state.salary,
                salaryDate = state.salaryDate,
                needsPercent = state.needsPercent,
                wantsPercent = state.wantsPercent,
                savingsPercent = state.savingsPercent,
                isOnboarded = true
            )
            profileRepository.insert(profile)
            onSuccess()
        }
    }
}
