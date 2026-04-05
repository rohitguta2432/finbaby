package com.finbaby.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryManagementUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val categoryToDelete: CategoryEntity? = null
)

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun toggleCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.update(category.copy(isEnabled = !category.isEnabled))
        }
    }

    fun addCustomCategory(name: String, icon: String) {
        viewModelScope.launch {
            val maxOrder = _uiState.value.categories.maxOfOrNull { it.sortOrder } ?: 0
            categoryRepository.insert(
                CategoryEntity(
                    name = name,
                    icon = icon,
                    color = 0xFF607D8B,
                    isDefault = false,
                    isEnabled = true,
                    sortOrder = maxOrder + 1,
                    budgetType = "wants"
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            categoryRepository.update(category)
        }
    }

    fun requestDelete(category: CategoryEntity) {
        _uiState.update { it.copy(showDeleteDialog = true, categoryToDelete = category) }
    }

    fun confirmDelete() {
        val cat = _uiState.value.categoryToDelete ?: return
        viewModelScope.launch {
            categoryRepository.delete(cat)
            _uiState.update { it.copy(showDeleteDialog = false, categoryToDelete = null) }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, categoryToDelete = null) }
    }
}
