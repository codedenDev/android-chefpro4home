package com.chefpro4home.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.chefpro4home.util.RecipeSearchService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FavoritesManager - Manages favorite recipes using SharedPreferences
 * Equivalent to iOS FavoritesManager.swift
 */
@Singleton
class FavoritesManager @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _favoriteRecipes = MutableStateFlow<List<RecipeSearchService.RecipeSearchResult>>(emptyList())
    val favoriteRecipes: StateFlow<List<RecipeSearchService.RecipeSearchResult>> = _favoriteRecipes.asStateFlow()
    
    init {
        loadFavoritesFromPreferences()
    }
    
    private fun loadFavoritesFromPreferences() {
        try {
            val json = prefs.getString(FAVORITES_KEY, null)
            if (json != null) {
                val type = object : TypeToken<List<RecipeSearchService.RecipeSearchResult>>() {}.type
                val favorites = gson.fromJson<List<RecipeSearchService.RecipeSearchResult>>(json, type)
                _favoriteRecipes.value = favorites ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _favoriteRecipes.value = emptyList()
        }
    }
    
    private fun saveFavoritesToPreferences() {
        try {
            val json = gson.toJson(_favoriteRecipes.value)
            prefs.edit().putString(FAVORITES_KEY, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Add a recipe to favorites
     */
    fun addToFavorites(recipe: RecipeSearchService.RecipeSearchResult) {
        if (!_favoriteRecipes.value.any { it.id == recipe.id }) {
            _favoriteRecipes.value = _favoriteRecipes.value + recipe
            saveFavoritesToPreferences()
        }
    }
    
    /**
     * Remove a recipe from favorites
     */
    fun removeFromFavorites(recipe: RecipeSearchService.RecipeSearchResult) {
        _favoriteRecipes.value = _favoriteRecipes.value.filter { it.id != recipe.id }
        saveFavoritesToPreferences()
    }
    
    /**
     * Check if a recipe is in favorites
     */
    fun isFavorite(recipe: RecipeSearchService.RecipeSearchResult): Boolean {
        return _favoriteRecipes.value.any { it.id == recipe.id }
    }
    
    /**
     * Check if a recipe is in favorites by ID
     */
    fun isFavorite(recipeId: String): Boolean {
        return _favoriteRecipes.value.any { it.id == recipeId }
    }
    
    /**
     * Toggle favorite status
     */
    fun toggleFavorite(recipe: RecipeSearchService.RecipeSearchResult) {
        if (isFavorite(recipe)) {
            removeFromFavorites(recipe)
        } else {
            addToFavorites(recipe)
        }
    }
    
    /**
     * Clear all favorites
     */
    fun clearFavorites() {
        _favoriteRecipes.value = emptyList()
        saveFavoritesToPreferences()
    }
    
    /**
     * Get favorite recipes as list
     */
    fun getFavoriteRecipes(): List<RecipeSearchService.RecipeSearchResult> {
        return _favoriteRecipes.value
    }
    
    companion object {
        private const val PREFS_NAME = "FavoritesPrefs"
        private const val FAVORITES_KEY = "FavoriteRecipes"
    }
}

