package com.chefpro4home.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefpro4home.data.model.Recipe
import com.chefpro4home.data.model.Tag
import com.chefpro4home.data.repository.RecipesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val repository: RecipesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    private val _allRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _selectedCuisine = MutableStateFlow<String?>(null)
    val selectedCuisine: StateFlow<String?> = _selectedCuisine.asStateFlow()
    
    private val _availableCuisines = MutableStateFlow<List<String>>(emptyList())
    val availableCuisines: StateFlow<List<String>> = _availableCuisines.asStateFlow()
    
    private val _cuisineRecipeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cuisineRecipeCounts: StateFlow<Map<String, Int>> = _cuisineRecipeCounts.asStateFlow()

    init {
        // Start with empty state to prevent crashes
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
        println("🚀 RecipesViewModel initialized")
        
        // Load recipes with error handling
        initializeRecipes()
    }
    
    private fun initializeRecipes() {
        viewModelScope.launch {
            try {
                // Load recipes quietly without showing loading screen
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                
                // Try to refresh from API first, fallback to local data
                try {
                    repository.refreshRecipes()
                } catch (apiError: Exception) {
                    // Silently fall back to local data
                }
                
                // Load recipes from database
                val recipes = repository.getAllRecipes().first()
                _allRecipes.value = recipes
                _recipes.value = recipes
                
                // Load cuisines
                val cuisines = repository.getAllCuisines()
                _availableCuisines.value = listOf("All") + cuisines
                
                // Load cuisine counts
                val cuisineCounts = mutableMapOf<String, Int>()
                cuisineCounts["All"] = recipes.size
                cuisines.forEach { cuisine ->
                    cuisineCounts[cuisine] = repository.getRecipeCountByCuisine(cuisine)
                }
                _cuisineRecipeCounts.value = cuisineCounts
                
            } catch (e: Exception) {
                // Try to load any existing data from database
                try {
                    val recipes = repository.getAllRecipes().first()
                    _allRecipes.value = recipes
                    _recipes.value = recipes
                } catch (dbError: Exception) {
                    // If all else fails, show empty state
                    _allRecipes.value = emptyList()
                    _recipes.value = emptyList()
                }
            }
        }
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
                    _allRecipes.value = recipeList
                    
                    // Load available cuisines
                    loadAvailableCuisines()
                    
                    // Apply current filters
                    filterRecipes()
                    
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
    
    private fun loadAvailableCuisines() {
        viewModelScope.launch {
            try {
                val cuisines = repository.getAllCuisines()
                _availableCuisines.value = listOf("All") + cuisines.sorted()
                
                // Load recipe counts for each cuisine
                val counts = mutableMapOf<String, Int>()
                counts["All"] = _allRecipes.value.size
                cuisines.forEach { cuisine ->
                    counts[cuisine] = repository.getRecipeCountByCuisine(cuisine)
                }
                _cuisineRecipeCounts.value = counts
                
                println("📊 Loaded ${cuisines.size} cuisines with counts: $counts")
            } catch (e: Exception) {
                println("⚠️ Error loading cuisines: ${e.message}")
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
        _selectedCuisine.value = if (cuisine == "All") null else cuisine
        filterRecipes()
    }

    private fun filterRecipes() {
        viewModelScope.launch {
            var filteredRecipes = _allRecipes.value
            
            // Apply cuisine filter FIRST (more restrictive) - matches iOS
            _selectedCuisine.value?.let { cuisine ->
                val recipeIdsForCuisine = repository.getTagsByRecipeIdSync("")
                    .filter { it.tagType == "cuisine" && it.tagValue == cuisine }
                    .map { it.recipeId }
                    .toSet()
                
                filteredRecipes = filteredRecipes.filter { recipe ->
                    recipeIdsForCuisine.contains(recipe.id)
                }
            }

            // Apply search filter SECOND (on already filtered results) - matches iOS
            if (_searchText.value.isNotEmpty()) {
                val searchTerm = _searchText.value.lowercase()
                filteredRecipes = filteredRecipes.filter { recipe ->
                    // Search in recipe name
                    recipe.name.lowercase().contains(searchTerm) ||
                    // Search in summary
                    recipe.summary.lowercase().contains(searchTerm) ||
                    // Search in ingredients (need to load from DB)
                    searchInIngredients(recipe.id, searchTerm) ||
                    // Search in tags
                    searchInTags(recipe.id, searchTerm)
                }
            }

            _recipes.value = filteredRecipes
            println("🔍 Filtered ${_allRecipes.value.size} recipes down to ${filteredRecipes.size}")
        }
    }
    
    private suspend fun searchInIngredients(recipeId: String, searchTerm: String): Boolean {
        return try {
            val ingredients = repository.getIngredientsByRecipeIdSync(recipeId)
            ingredients.any { ingredient ->
                ingredient.name?.lowercase()?.contains(searchTerm) == true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun searchInTags(recipeId: String, searchTerm: String): Boolean {
        return try {
            val tags = repository.getTagsByRecipeIdSync(recipeId)
            tags.any { tag ->
                tag.tagValue.lowercase().contains(searchTerm)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getCuisineCategories(): List<String> {
        return _availableCuisines.value
    }
    
    fun getRecipeCountForCuisine(cuisine: String): Int {
        return _cuisineRecipeCounts.value[cuisine] ?: 0
    }
    
    suspend fun getTagsForRecipe(recipeId: String): List<Tag> {
        return try {
            repository.getTagsByRecipeIdSync(recipeId)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun isRecipeInShoppingList(recipeId: String): Boolean {
        // Check if recipe ingredients are in shopping list
        return try {
            val shoppingItems = repository.getAllShoppingItems().first()
            shoppingItems.any { it.recipeId == recipeId }
        } catch (e: Exception) {
            false
        }
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


