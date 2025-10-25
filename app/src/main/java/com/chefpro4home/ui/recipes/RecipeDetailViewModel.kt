package com.chefpro4home.ui.recipes

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chefpro4home.data.manager.FavoritesManager
import com.chefpro4home.data.model.Ingredient
import com.chefpro4home.data.model.Instruction
import com.chefpro4home.data.model.Nutrition
import com.chefpro4home.data.model.Recipe
import com.chefpro4home.data.model.Tag
import com.chefpro4home.data.repository.RecipesRepository
import com.chefpro4home.util.RecipeSearchService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val repository: RecipesRepository,
    private val favoritesManager: FavoritesManager
) : ViewModel() {

    companion object {
        private const val TAG = "RecipeDetailViewModel"
    }

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()

    private val _instructions = MutableStateFlow<List<Instruction>>(emptyList())
    val instructions: StateFlow<List<Instruction>> = _instructions.asStateFlow()

    private val _nutrition = MutableStateFlow<Nutrition?>(null)
    val nutrition: StateFlow<Nutrition?> = _nutrition.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading recipe: $recipeId")
                
                // Load recipe
                val loadedRecipe = repository.getRecipeById(recipeId)
                _recipe.value = loadedRecipe
                
                if (loadedRecipe == null) {
                    Log.e(TAG, "Recipe not found: $recipeId")
                    return@launch
                }
                
                // Load ingredients
                val loadedIngredients = repository.getIngredientsByRecipeIdSync(recipeId)
                _ingredients.value = loadedIngredients
                Log.d(TAG, "Loaded ${loadedIngredients.size} ingredients")
                
                // Load instructions
                val loadedInstructions = repository.getInstructionsByRecipeIdSync(recipeId)
                _instructions.value = loadedInstructions
                Log.d(TAG, "Loaded ${loadedInstructions.size} instructions")
                
                // Load nutrition
                val loadedNutrition = repository.getNutritionByRecipeId(recipeId)
                _nutrition.value = loadedNutrition
                Log.d(TAG, "Loaded nutrition: ${loadedNutrition != null}")
                
                // Load tags
                val loadedTags = repository.getTagsByRecipeIdSync(recipeId)
                _tags.value = loadedTags
                Log.d(TAG, "Loaded ${loadedTags.size} tags")
                
                // Check if recipe is favorite (not supported for RecipeModel, only RecipeSearchResult)
                _isFavorite.value = false
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recipe: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun toggleFavorite() {
        // TODO: Convert Recipe to RecipeSearchResult and use FavoritesManager
        _isFavorite.value = !_isFavorite.value
    }

    fun shareRecipe() {
        viewModelScope.launch {
            try {
                val recipe = _recipe.value ?: return@launch
                val ingredients = _ingredients.value
                val instructions = _instructions.value
                
                val shareText = buildString {
                    append("🍽️ ${recipe.name}\n\n")
                    
                    if (recipe.summary.isNotEmpty()) {
                        append("📝 ${recipe.summary}\n\n")
                    }
                    
                    append("👥 Serves: ${recipe.servings} ${recipe.servingsUnit}\n")
                    if (recipe.prepTime.isNotEmpty() && recipe.prepTime != "0") {
                        append("⏱️ Prep Time: ${recipe.prepTime} min\n")
                    }
                    if (recipe.cookTime.isNotEmpty() && recipe.cookTime != "0") {
                        append("🔥 Cook Time: ${recipe.cookTime} min\n")
                    }
                    if (recipe.totalTime.isNotEmpty() && recipe.totalTime != "0") {
                        append("⏰ Total Time: ${recipe.totalTime} min\n")
                    }
                    append("\n")
                    
                    // Ingredients
                    if (ingredients.isNotEmpty()) {
                        append("🥘 Ingredients:\n")
                        ingredients.forEach { ingredient ->
                            append("  • ")
                            if (!ingredient.amount.isNullOrEmpty()) {
                                append("${ingredient.amount} ")
                            }
                            if (!ingredient.unit.isNullOrEmpty()) {
                                append("${ingredient.unit} ")
                            }
                            append(ingredient.name ?: "")
                            if (!ingredient.notes.isNullOrEmpty()) {
                                append(" (${ingredient.notes})")
                            }
                            append("\n")
                        }
                        append("\n")
                    }
                    
                    // Instructions
                    if (instructions.isNotEmpty()) {
                        append("📋 Instructions:\n")
                        instructions.filter { it.type == "instruction" }.forEachIndexed { index, instruction ->
                            append("  ${index + 1}. ${instruction.text}\n")
                        }
                    }
                }
                
                Log.d(TAG, "Share text prepared: ${shareText.length} characters")
                // TODO: Trigger share intent in UI
            } catch (e: Exception) {
                Log.e(TAG, "Error preparing share text: ${e.message}")
            }
        }
    }
}

