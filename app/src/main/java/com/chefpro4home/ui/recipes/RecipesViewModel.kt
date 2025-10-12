package com.chefpro4home.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefpro4home.data.model.Recipe
import com.chefpro4home.data.repository.RecipesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val repository: RecipesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedCuisine = MutableStateFlow<String?>(null)
    val selectedCuisine: StateFlow<String?> = _selectedCuisine.asStateFlow()

    init {
        // Clear any cached data and load fresh from API
        clearCacheAndLoadRecipes()
    }
    
    private fun clearCacheAndLoadRecipes() {
        viewModelScope.launch {
            try {
                println("🧹 Clearing local cache...")
                // Clear all local data first
                repository.clearAllLocalData()
                
                // Test API health first
                println("🏥 Testing API health...")
                val isHealthy = repository.checkBackendHealth()
                if (isHealthy) {
                    println("✅ API is healthy, loading fresh data...")
                    loadRecipes()
                } else {
                    println("❌ API is not healthy - showing empty state")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "API is not available. Please check your internet connection."
                    )
                }
            } catch (e: Exception) {
                println("⚠️ Error in clearCacheAndLoadRecipes: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to connect to API. Please check your internet connection."
                )
            }
        }
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                println("🔄 Refreshing recipes from API...")
                // Only load from API - no fallback to local data
                repository.refreshRecipes()
                
                println("📱 Loading recipes from local database after API refresh...")
                // Load from local database (which now has fresh API data)
                repository.getAllRecipes().collect { recipeList ->
                    println("📋 Loaded ${recipeList.size} recipes from local database")
                    _recipes.value = recipeList
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                println("💥 Error in loadRecipes: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load recipes"
                )
            }
        }
    }

    fun refreshRecipes() {
        viewModelScope.launch {
            try {
                repository.refreshRecipes()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to refresh recipes"
                )
            }
        }
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
        filterRecipes()
    }

    fun updateSelectedCuisine(cuisine: String?) {
        _selectedCuisine.value = cuisine
        filterRecipes()
    }

    private fun filterRecipes() {
        viewModelScope.launch {
            val currentRecipes = _recipes.value
            var filteredRecipes = currentRecipes

            // Apply search filter
            if (_searchText.value.isNotEmpty()) {
                val searchTerm = _searchText.value.lowercase()
                filteredRecipes = filteredRecipes.filter { recipe ->
                    recipe.name.lowercase().contains(searchTerm) ||
                    recipe.summary.lowercase().contains(searchTerm)
                }
            }

            // Apply cuisine filter
            _selectedCuisine.value?.let { cuisine ->
                if (cuisine != "All") {
                    // This would need to be implemented with tags
                    // filteredRecipes = filteredRecipes.filter { recipe ->
                    //     recipe.tags.cuisine.contains(cuisine)
                    // }
                }
            }

            _recipes.value = filteredRecipes
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getCuisineCategories(): List<String> {
        // This would query tags to get available cuisines
        return listOf("All", "Cuban", "German", "Tex-Mex", "Italian", "Mexican", "American")
    }
    
    fun addRecipeToShoppingList(recipe: Recipe) {
        viewModelScope.launch {
            try {
                println("🛒 Adding recipe ${recipe.name} (ID: ${recipe.id}) to shopping list...")
                // Use the existing repository method that handles ingredients properly
                repository.addRecipeToShoppingList(recipe)
                println("✅ Successfully added recipe ${recipe.name} to shopping list")
                _uiState.value = _uiState.value.copy(
                    successMessage = "Added ${recipe.name} to shopping list"
                )
            } catch (e: Exception) {
                println("❌ Error adding recipe to shopping list: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add recipe to shopping list: ${e.message}"
                )
            }
        }
    }
    
}

data class RecipesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isRefreshing: Boolean = false
)


