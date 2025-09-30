package com.chefpro4home.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefpro4home.data.model.ShoppingItem
import com.chefpro4home.data.repository.RecipesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val repository: RecipesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    private val _shoppingItems = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shoppingItems: StateFlow<List<ShoppingItem>> = _shoppingItems.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.ALL)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    enum class ViewMode {
        ALL, PENDING, COMPLETED, BY_RECIPE, COMMON_INGREDIENTS
    }

    init {
        loadShoppingItems()
    }

    fun loadShoppingItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                when (_viewMode.value) {
                    ViewMode.ALL -> repository.getAllShoppingItems().collect { items ->
                        _shoppingItems.value = items
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    ViewMode.PENDING -> repository.getPendingShoppingItems().collect { items ->
                        _shoppingItems.value = items
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    ViewMode.COMPLETED -> repository.getCompletedShoppingItems().collect { items ->
                        _shoppingItems.value = items
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                    else -> {
                        // Handle other view modes
                        repository.getAllShoppingItems().collect { items ->
                            _shoppingItems.value = items
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load shopping items"
                )
            }
        }
    }

    fun updateViewMode(mode: ViewMode) {
        _viewMode.value = mode
        loadShoppingItems()
    }

    fun addShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                repository.addShoppingItem(item)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to add shopping item"
                )
            }
        }
    }

    fun updateShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                repository.updateShoppingItem(item)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to update shopping item"
                )
            }
        }
    }

    fun toggleItemCompletion(id: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleShoppingItemCompletion(id, isCompleted)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to update item"
                )
            }
        }
    }

    fun deleteShoppingItem(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteShoppingItem(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete shopping item"
                )
            }
        }
    }

    fun deleteCompletedItems() {
        viewModelScope.launch {
            try {
                repository.deleteCompletedShoppingItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete completed items"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getItemsByRecipe(): Map<String, List<ShoppingItem>> {
        return _shoppingItems.value
            .filter { it.recipeId != null }
            .groupBy { it.recipeId!! }
    }

    fun getCommonIngredients(): List<ShoppingItem> {
        // This would implement logic to group common ingredients
        return _shoppingItems.value.filter { it.recipeId == null }
    }
}

data class ShoppingListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)
