package com.chefpro4home.ui.what2cook

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefpro4home.data.manager.FavoritesManager
import com.chefpro4home.data.repository.RecipesRepository
import com.chefpro4home.util.RecipeSearchService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class What2CookViewModel @Inject constructor(
    private val repository: RecipesRepository,
    private val favoritesManager: FavoritesManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "What2CookViewModel"
    }

    private val _uiState = MutableStateFlow(What2CookUiState())
    val uiState: StateFlow<What2CookUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<RecipeSearchService.RecipeSearchResult>>(emptyList())
    val searchResults: StateFlow<List<RecipeSearchService.RecipeSearchResult>> = _searchResults.asStateFlow()

    private val _quickActionResults = MutableStateFlow<List<RecipeSearchService.RecipeSearchResult>>(emptyList())
    val quickActionResults: StateFlow<List<RecipeSearchService.RecipeSearchResult>> = _quickActionResults.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _selectedDiet = MutableStateFlow(RecipeSearchService.DietType.NONE)
    val selectedDiet: StateFlow<RecipeSearchService.DietType> = _selectedDiet.asStateFlow()

    private val _maxTime = MutableStateFlow(60)
    val maxTime: StateFlow<Int> = _maxTime.asStateFlow()

    private val _maxServings = MutableStateFlow(4)
    val maxServings: StateFlow<Int> = _maxServings.asStateFlow()

    private val _useInventoryIngredients = MutableStateFlow(true)
    val useInventoryIngredients: StateFlow<Boolean> = _useInventoryIngredients.asStateFlow()

    private val _quickActionTitle = MutableStateFlow("")
    val quickActionTitle: StateFlow<String> = _quickActionTitle.asStateFlow()

    private val _quickActionSubtitle = MutableStateFlow("")
    val quickActionSubtitle: StateFlow<String> = _quickActionSubtitle.asStateFlow()

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun updateSelectedDiet(diet: RecipeSearchService.DietType) {
        _selectedDiet.value = diet
    }

    fun updateMaxTime(time: Int) {
        _maxTime.value = time
    }

    fun updateMaxServings(servings: Int) {
        _maxServings.value = servings
    }

    fun updateUseInventoryIngredients(use: Boolean) {
        _useInventoryIngredients.value = use
    }

    // Main search function - matches iOS performSearch()
    fun searchRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                Log.d(TAG, "Starting recipe search...")
                val results = if (_searchText.value.isNotEmpty()) {
                    // Text-based search
                    Log.d(TAG, "Searching by text: ${_searchText.value}")
                    repository.searchRecipesWithIngredients(
                        ingredients = _searchText.value.split(",", " ").map { it.trim() }.filter { it.isNotEmpty() },
                        diet = _selectedDiet.value,
                        maxTime = _maxTime.value
                    )
                } else {
                    // Popular recipes fallback
                    Log.d(TAG, "Searching popular recipes")
                    repository.getPopularRecipes()
                }
                
                _searchResults.value = results
                _uiState.value = _uiState.value.copy(isLoading = false)
                Log.d(TAG, "Search completed: ${results.size} results")
            } catch (e: Exception) {
                Log.e(TAG, "Search failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to search recipes"
                )
            }
        }
    }

    // Quick Action: Inventory Recipes
    fun getInventoryRecipes(inventoryCount: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Recipes with Your Ingredients"
            _quickActionSubtitle.value = "Based on $inventoryCount items in your inventory"
            
            try {
                val results = repository.getInventoryRecipes()
                _quickActionResults.value = results
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "Inventory recipes failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load inventory recipes"
                )
            }
        }
    }

    // Quick Action: Popular Recipes
    fun getPopularRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Popular Recipes"
            _quickActionSubtitle.value = "Trending and favorite dishes"
            
            try {
                val results = repository.getPopularRecipes()
                _quickActionResults.value = results
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "Popular recipes failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load popular recipes"
                )
            }
        }
    }

    // Quick Action: Quick Meals
    fun getQuickMeals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Quick Meals"
            _quickActionSubtitle.value = "Ready in under 30 minutes"
            
            try {
                val results = repository.getQuickMeals(maxTime = 30)
                _quickActionResults.value = results
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "Quick meals failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load quick meals"
                )
            }
        }
    }

    // Quick Action: Random Recipes
    fun getRandomRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Surprise Recipes"
            _quickActionSubtitle.value = "Discover something new and exciting"
            
            try {
                val results = repository.getRandomRecipes(count = 10)
                _quickActionResults.value = results
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "Random recipes failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load random recipes"
                )
            }
        }
    }

    // Quick Action: Favorite Recipes
    fun getFavoriteRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val favoriteCount = favoritesManager.getFavoriteRecipes().size
            _quickActionTitle.value = "My Favorite Recipes"
            _quickActionSubtitle.value = "$favoriteCount saved recipes"
            
            try {
                val results = favoritesManager.getFavoriteRecipes()
                _quickActionResults.value = results
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "Favorite recipes failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load favorite recipes"
                )
            }
        }
    }

    fun addToShoppingList(recipe: RecipeSearchService.RecipeSearchResult) {
        viewModelScope.launch {
            try {
                // Convert search result to shopping items
                recipe.ingredients.forEach { ingredient ->
                    val shoppingItem = com.chefpro4home.data.model.ShoppingItem(
                        id = "${recipe.id}_${ingredient.hashCode()}",
                        name = ingredient,
                        recipeId = recipe.id,
                        createdAt = System.currentTimeMillis().toString()
                    )
                    repository.addShoppingItem(shoppingItem)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to add to shopping list"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun clearQuickActionResults() {
        _quickActionResults.value = emptyList()
    }

    fun formatTimeDisplay(minutes: Int): String {
        return if (minutes < 60) {
            "$minutes minutes"
        } else {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            if (remainingMinutes == 0) {
                "$hours hours"
            } else {
                "$hours hours $remainingMinutes minutes"
            }
        }
    }
}

data class What2CookUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)


