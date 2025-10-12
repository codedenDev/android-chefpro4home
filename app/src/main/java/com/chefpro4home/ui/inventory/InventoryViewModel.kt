package com.chefpro4home.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefpro4home.data.model.InventoryItem
import com.chefpro4home.data.repository.RecipesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: RecipesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    init {
        loadInventoryItems()
        loadCategories()
    }

    fun loadInventoryItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                when {
                    _searchText.value.isNotEmpty() -> {
                        repository.searchInventoryItems(_searchText.value).collect { items ->
                            _inventoryItems.value = items
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                    _selectedCategory.value != null -> {
                        repository.getInventoryItemsByCategory(_selectedCategory.value!!).collect { items ->
                            _inventoryItems.value = items
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                    else -> {
                        repository.getAllInventoryItems().collect { items ->
                            _inventoryItems.value = items
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load inventory items"
                )
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                repository.getAllInventoryCategories().collect { categoryList ->
                    _categories.value = listOf("All") + categoryList
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to load categories"
                )
            }
        }
    }

    fun addInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.addInventoryItem(item)
                loadInventoryItems()
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to add inventory item"
                )
            }
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.updateInventoryItem(item)
                loadInventoryItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to update inventory item"
                )
            }
        }
    }

    fun deleteInventoryItem(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteInventoryItem(id)
                loadInventoryItems()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to delete inventory item"
                )
            }
        }
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
        loadInventoryItems()
    }

    fun updateSelectedCategory(category: String?) {
        _selectedCategory.value = category
        loadInventoryItems()
    }

    fun getInventoryItemByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val item = repository.getInventoryItemByBarcode(barcode)
                if (item != null) {
                    _uiState.value = _uiState.value.copy(
                        scannedItem = item,
                        showBarcodeResult = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Item not found in inventory"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to scan barcode"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearBarcodeResult() {
        _uiState.value = _uiState.value.copy(
            scannedItem = null,
            showBarcodeResult = false
        )
    }

    fun getItemsByCategory(): Map<String, List<InventoryItem>> {
        return _inventoryItems.value.groupBy { it.category }
    }

    fun getExpiringItems(): List<InventoryItem> {
        val currentTime = System.currentTimeMillis()
        return _inventoryItems.value.filter { item ->
            item.expirationDate?.let { expirationDate ->
                try {
                    val expirationTime = expirationDate.toLong()
                    val timeUntilExpiration = expirationTime - currentTime
                    // Items expiring in the next 7 days
                    timeUntilExpiration > 0 && timeUntilExpiration <= 7 * 24 * 60 * 60 * 1000
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
    }

    fun getLowStockItems(): List<InventoryItem> {
        return _inventoryItems.value.filter { item ->
            try {
                val amount = item.amount.toDoubleOrNull()
                amount != null && amount <= 1.0
            } catch (e: Exception) {
                false
            }
        }
    }
}

data class InventoryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
    val scannedItem: InventoryItem? = null,
    val showBarcodeResult: Boolean = false
)


