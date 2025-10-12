package com.chefpro4home.data.repository

import android.content.Context
import com.chefpro4home.data.api.RecipesApiService
import com.chefpro4home.data.database.dao.*
import com.chefpro4home.data.model.*
import com.chefpro4home.util.RecipeSearchService
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipesRepository @Inject constructor(
    private val context: Context,
    private val apiService: RecipesApiService,
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val instructionDao: InstructionDao,
    private val tagDao: TagDao,
    private val equipmentDao: EquipmentDao,
    private val nutritionDao: NutritionDao,
    private val shoppingListDao: ShoppingListDao,
    private val inventoryDao: InventoryDao,
    private val eventDao: EventDao,
    private val recipeSearchService: RecipeSearchService
) {
    
    // Recipe operations
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()
    
    suspend fun getRecipeById(id: String): Recipe? = recipeDao.getRecipeById(id)
    
    fun searchRecipes(searchTerm: String): Flow<List<Recipe>> = recipeDao.searchRecipes(searchTerm)
    
    suspend fun clearAllLocalData() {
        // Clear all local data to start fresh
        recipeDao.deleteAllRecipes()
        ingredientDao.deleteAllIngredients()
        instructionDao.deleteAllInstructions()
        tagDao.deleteAllTags()
    }
    
    private suspend fun loadRecipesFromAssets(): Pair<List<Recipe>, List<Nutrition>> {
        return try {
            println("📖 Reading recipes.json from assets...")
            val jsonString = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
            println("📄 JSON file size: ${jsonString.length} characters")
            
            val gson = Gson()
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
            val recipeArray = jsonObject.getAsJsonArray("recipeModel")
            
            println("📋 Found ${recipeArray.size()} recipes in JSON")
            
            val recipes = mutableListOf<Recipe>()
            val nutritionList = mutableListOf<Nutrition>()
            
            for (i in 0 until recipeArray.size()) {
                val recipeJson = recipeArray.get(i).asJsonObject
                
                val recipe = Recipe(
                    id = recipeJson.get("id")?.asString ?: "recipe_$i",
                    name = recipeJson.get("name")?.asString ?: "Unknown Recipe",
                    summary = recipeJson.get("summary")?.asString ?: "",
                    imageURL = recipeJson.get("image_url")?.asString ?: "",
                    servings = recipeJson.get("servings")?.asString ?: "4",
                    prepTime = recipeJson.get("prep_time")?.asString ?: "0",
                    cookTime = recipeJson.get("cook_time")?.asString ?: "0",
                    totalTime = recipeJson.get("total_time")?.asString ?: "0",
                    difficulty = "Medium", // Default since not in JSON
                    createdAt = System.currentTimeMillis().toString(),
                    updatedAt = System.currentTimeMillis().toString()
                )
                
                // Parse nutrition data
                val nutritionJson = recipeJson.getAsJsonObject("nutrition")
                if (nutritionJson != null) {
                    val nutrition = Nutrition(
                        id = "${recipe.id}_nutrition",
                        recipeId = recipe.id,
                        calories = nutritionJson.get("calories")?.asDouble ?: 0.0,
                        protein = nutritionJson.get("protein")?.asDouble ?: 0.0,
                        carbohydrates = nutritionJson.get("carbs")?.asDouble ?: 0.0,
                        fat = nutritionJson.get("fat")?.asDouble ?: 0.0,
                        fiber = nutritionJson.get("fiber")?.asDouble ?: 0.0
                    )
                    nutritionList.add(nutrition)
                }
                
                recipes.add(recipe)
                println("✅ Loaded recipe: ${recipe.name}")
            }
            
            println("🎉 Successfully loaded ${recipes.size} real recipes from assets!")
            Pair(recipes, nutritionList)
        } catch (e: Exception) {
            println("💥 Error loading recipes from assets: ${e.message}")
            e.printStackTrace()
            Pair(emptyList(), emptyList())
        }
    }
    
    suspend fun refreshRecipes() {
        try {
            // Clear existing data first
            clearAllLocalData()
            
            println("🔍 Loading recipes from local JSON file...")
            // Load from local assets instead of API
            val (recipes, nutritionList) = loadRecipesFromAssets()
            println("✅ Loaded ${recipes.size} recipes from local JSON")
            
            if (recipes.isNotEmpty()) {
                recipeDao.insertAllRecipes(recipes)
                
                // Insert nutrition data
                if (nutritionList.isNotEmpty()) {
                    nutritionDao.insertAllNutrition(nutritionList)
                    println("💾 Inserted ${nutritionList.size} nutrition records")
                }
                
                println("✅ Successfully loaded ${recipes.size} real recipes from iOS app data")
            } else {
                println("❌ No recipes found in local JSON file")
            }
        } catch (e: Exception) {
            println("💥 Exception loading recipes from assets: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun createRecipe(recipe: Recipe): Boolean {
        return try {
            // Convert to API model and send to server
            val apiRecipe = recipe.toApiModel()
            val response = apiService.createRecipe(apiRecipe)
            if (response.isSuccessful) {
                // Insert locally
                recipeDao.insertRecipe(recipe)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateRecipe(recipe: Recipe): Boolean {
        return try {
            val apiRecipe = recipe.toApiModel()
            val response = apiService.updateRecipe(recipe.id, apiRecipe)
            if (response.isSuccessful) {
                recipeDao.updateRecipe(recipe)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteRecipe(recipeId: String): Boolean {
        return try {
            val response = apiService.deleteRecipe(recipeId)
            if (response.isSuccessful) {
                recipeDao.deleteRecipeById(recipeId)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    // Shopping List operations
    fun getAllShoppingItems(): Flow<List<ShoppingItem>> = shoppingListDao.getAllItems()
    
    fun getPendingShoppingItems(): Flow<List<ShoppingItem>> = shoppingListDao.getPendingItems()
    
    fun getCompletedShoppingItems(): Flow<List<ShoppingItem>> = shoppingListDao.getCompletedItems()
    
    suspend fun addShoppingItem(item: ShoppingItem) = shoppingListDao.insertItem(item)
    
    suspend fun addToShoppingList(ingredientName: String) {
        val shoppingItem = ShoppingItem(
            id = java.util.UUID.randomUUID().toString(),
            name = ingredientName,
            amount = "1",
            unit = "item",
            isCompleted = false,
            recipeId = null,
            createdAt = System.currentTimeMillis().toString()
        )
        shoppingListDao.insertItem(shoppingItem)
    }
    
    suspend fun updateShoppingItem(item: ShoppingItem) = shoppingListDao.updateItem(item)
    
    suspend fun deleteShoppingItem(id: String) = shoppingListDao.deleteItemById(id)
    
    suspend fun toggleShoppingItemCompletion(id: String, isCompleted: Boolean) {
        shoppingListDao.updateItemCompletion(id, isCompleted)
    }
    
    suspend fun deleteCompletedShoppingItems() = shoppingListDao.deleteCompletedItems()
    
    suspend fun addRecipeToShoppingList(recipe: Recipe) {
        println("📋 Fetching ingredients for recipe ID: ${recipe.id}")
        val ingredients = ingredientDao.getIngredientsByRecipeIdSync(recipe.id)
        println("📋 Found ${ingredients.size} ingredients for recipe ${recipe.name}")
        
        if (ingredients.isEmpty()) {
            println("⚠️ No ingredients found in database for recipe ${recipe.id}")
            println("⚠️ This might mean ingredients weren't saved when the recipe was loaded")
        }
        
        ingredients.forEachIndexed { index, ingredient ->
            println("🥕 Adding ingredient ${index + 1}/${ingredients.size}: ${ingredient.name} (${ingredient.amount} ${ingredient.unit})")
            val shoppingItem = ShoppingItem(
                id = "${recipe.id}_${ingredient.id}",
                name = ingredient.name ?: "",
                amount = ingredient.amount,
                unit = ingredient.unit,
                recipeId = recipe.id,
                createdAt = System.currentTimeMillis().toString()
            )
            shoppingListDao.insertItem(shoppingItem)
            println("✅ Successfully added ${ingredient.name} to shopping list")
        }
        
        println("🎉 Finished adding ${ingredients.size} ingredients to shopping list")
    }
    
    // Inventory operations
    fun getAllInventoryItems(): Flow<List<InventoryItem>> = inventoryDao.getAllItems()
    
    fun getInventoryItemsByCategory(category: String): Flow<List<InventoryItem>> = 
        inventoryDao.getItemsByCategory(category)
    
    fun getAllInventoryCategories(): Flow<List<String>> = inventoryDao.getAllCategories()
    
    fun searchInventoryItems(searchTerm: String): Flow<List<InventoryItem>> = 
        inventoryDao.searchItems(searchTerm)
    
    suspend fun addInventoryItem(item: InventoryItem) = inventoryDao.insertItem(item)
    
    suspend fun updateInventoryItem(item: InventoryItem) = inventoryDao.updateItem(item)
    
    suspend fun deleteInventoryItem(id: String) = inventoryDao.deleteItemById(id)
    
    suspend fun getInventoryItemByBarcode(barcode: String): InventoryItem? = 
        inventoryDao.getItemByBarcode(barcode)
    
    // Event operations
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    
    suspend fun addEvent(event: Event) = eventDao.insertEvent(event)
    
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    
    suspend fun deleteEvent(id: String) = eventDao.deleteEventById(id)
    
    // Search operations
    suspend fun searchRecipesWithIngredients(
        ingredients: List<String>,
        diet: RecipeSearchService.DietType = RecipeSearchService.DietType.NONE,
        maxTime: Int = 60
    ) = recipeSearchService.searchRecipes(ingredients, diet, maxTime)
    
    suspend fun getInventoryRecipes() = recipeSearchService.getInventoryRecipes()
    
    suspend fun getPopularRecipes() = recipeSearchService.getPopularRecipes()
    
    suspend fun getQuickMeals(maxTime: Int = 30) = recipeSearchService.getQuickMeals(maxTime)
    
    suspend fun getRandomRecipes(count: Int = 5) = recipeSearchService.getRandomRecipes(count)
    
    suspend fun getFavoriteRecipes() = recipeSearchService.getFavoriteRecipes()
    
    // Health check
    suspend fun checkBackendHealth(): Boolean {
        return try {
            println("🏥 Checking backend health...")
            val response = apiService.healthCheck()
            println("🏥 Health check response: ${response.code()} - ${response.message()}")
            val isHealthy = response.isSuccessful && response.body()?.status == "healthy"
            println("🏥 Backend is healthy: $isHealthy")
            isHealthy
        } catch (e: Exception) {
            println("💥 Health check failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}

// Extension functions to convert between API and local models
private fun Recipe.toApiModel(): com.chefpro4home.data.api.RecipeApiModel {
    return com.chefpro4home.data.api.RecipeApiModel(
        id = id,
        title = name,
        description = summary,
        prep_time = prepTime.toIntOrNull() ?: 0,
        cook_time = cookTime.toIntOrNull() ?: 0,
        servings = servings.toIntOrNull() ?: 4,
        difficulty = difficulty,
        image_url = imageURL,
        created_at = createdAt,
        updated_at = updatedAt,
        ingredients = emptyList(), // Would be populated from ingredients
        instructions = emptyList(), // Would be populated from instructions
        tags = com.chefpro4home.data.api.TagsApiModel()
    )
}

private fun com.chefpro4home.data.api.RecipeApiModel.toEntity(): Recipe {
    return Recipe(
        id = id,
        name = title,
        summary = description,
        imageURL = image_url,
        servings = servings.toString(),
        prepTime = prep_time.toString(),
        cookTime = cook_time.toString(),
        totalTime = (prep_time + cook_time).toString(),
        difficulty = difficulty ?: "Medium",
        createdAt = created_at ?: System.currentTimeMillis().toString(),
        updatedAt = updated_at ?: System.currentTimeMillis().toString()
    )
}


