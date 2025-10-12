package com.chefpro4home.ui.what2cook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefpro4home.data.model.Recipe
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
    private val repository: RecipesRepository
) : ViewModel() {

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

    fun searchRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val ingredients = _searchText.value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                
                repository.searchRecipesWithIngredients(
                    ingredients = ingredients,
                    diet = _selectedDiet.value,
                    maxTime = _maxTime.value
                ).collect { results ->
                    _searchResults.value = results
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to search recipes"
                )
            }
        }
    }

    fun getInventoryRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Recipes with Your Ingredients"
            _quickActionSubtitle.value = "Based on your inventory"
            
            try {
                repository.getInventoryRecipes().collect { results ->
                    _quickActionResults.value = results
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load inventory recipes"
                )
            }
        }
    }

    fun getPopularRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Popular Recipes"
            _quickActionSubtitle.value = "Trending family favorites"
            
            try {
                repository.getPopularRecipes().collect { results ->
                    _quickActionResults.value = results
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load popular recipes"
                )
            }
        }
    }

    fun getQuickMeals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Quick Meals"
            _quickActionSubtitle.value = "Ready in 30 minutes or less"
            
            try {
                repository.getQuickMeals(maxTime = 30).collect { results ->
                    _quickActionResults.value = results
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load quick meals"
                )
            }
        }
    }

    fun getRandomRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Surprise Me!"
            _quickActionSubtitle.value = "Random recipe suggestions"
            
            try {
                repository.getRandomRecipes(count = 5).collect { results ->
                    _quickActionResults.value = results
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load random recipes"
                )
            }
        }
    }

    fun getFavoriteRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _quickActionTitle.value = "Your Favorites"
            _quickActionSubtitle.value = "Your saved recipes"
            
            try {
                repository.getFavoriteRecipes().collect { results ->
                    _quickActionResults.value = results
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
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


