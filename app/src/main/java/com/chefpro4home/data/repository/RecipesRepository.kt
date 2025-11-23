package com.chefpro4home.data.repository

import android.content.Context
import com.chefpro4home.data.api.RecipesApiService
import com.chefpro4home.data.api.*
import com.chefpro4home.data.database.dao.*
import com.chefpro4home.data.model.*
import com.chefpro4home.util.RecipeSearchService
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Updated RecipesRepository for Cloudflare integration
 * 
 * This repository loads data from Cloudflare API instead of local JSON,
 * providing real-time data with WordPress independence.
 */
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
    
    // Additional methods needed by ViewModel
    suspend fun getIngredientsForRecipe(recipeId: String): List<Ingredient> = 
        ingredientDao.getIngredientsByRecipeIdSync(recipeId)
    
    suspend fun getInstructionsForRecipe(recipeId: String): List<Instruction> = 
        instructionDao.getInstructionsByRecipeIdSync(recipeId)
    
    suspend fun getEquipmentForRecipe(recipeId: String): List<Equipment> = 
        equipmentDao.getEquipmentByRecipeId(recipeId).first()
    
    suspend fun getNutritionForRecipe(recipeId: String): Nutrition? = 
        nutritionDao.getNutritionByRecipeId(recipeId)
    
    suspend fun getTagsForRecipe(recipeId: String): List<Tag> = 
        tagDao.getTagsByRecipeIdSync(recipeId)
    
    suspend fun toggleFavorite(recipe: Recipe) {
        // For now, just update the recipe without favorite functionality
        recipeDao.updateRecipe(recipe)
    }
    
    suspend fun isRecipeFavorite(recipeId: String): Boolean {
        // For now, return false since favorites aren't implemented
        return false
    }
    
    // Ingredient operations
    fun getIngredientsByRecipeId(recipeId: String): Flow<List<Ingredient>> = 
        ingredientDao.getIngredientsByRecipeId(recipeId)
    
    suspend fun getIngredientsByRecipeIdSync(recipeId: String): List<Ingredient> = 
        ingredientDao.getIngredientsByRecipeIdSync(recipeId)
    
    // Instruction operations  
    fun getInstructionsByRecipeId(recipeId: String): Flow<List<Instruction>> = 
        instructionDao.getInstructionsByRecipeId(recipeId)
    
    suspend fun getInstructionsByRecipeIdSync(recipeId: String): List<Instruction> = 
        instructionDao.getInstructionsByRecipeIdSync(recipeId)
    
    // Tag operations
    fun getTagsByRecipeId(recipeId: String): Flow<List<Tag>> = 
        tagDao.getTagsByRecipeId(recipeId)
    
    suspend fun getTagsByRecipeIdSync(recipeId: String): List<Tag> = 
        tagDao.getTagsByRecipeIdSync(recipeId)
    
    suspend fun getAllCuisines(): List<String> = 
        tagDao.getTagsByType("cuisine")
    
    suspend fun getRecipeCountByCuisine(cuisine: String): Int = 
        if (cuisine == "All") recipeDao.getAllRecipes().first().size
        else tagDao.getRecipeCountForTag("cuisine", cuisine)
    
    // Nutrition operations
    suspend fun getNutritionByRecipeId(recipeId: String): Nutrition? = 
        nutritionDao.getNutritionByRecipeId(recipeId)
    
    // Equipment operations
    fun getEquipmentByRecipeId(recipeId: String): Flow<List<Equipment>> = 
        equipmentDao.getEquipmentByRecipeId(recipeId)
    
    suspend fun clearAllLocalData() {
        // Clear all local data to start fresh
        recipeDao.deleteAllRecipes()
        ingredientDao.deleteAllIngredients()
        instructionDao.deleteAllInstructions()
        tagDao.deleteAllTags()
        equipmentDao.deleteAllEquipment()
        nutritionDao.deleteAllNutrition()
    }
    
    /**
     * Load recipes from Cloudflare API - handles direct array response
     */
    private suspend fun loadRecipesFromCloudflare(): RecipeData {
        return try {
            println("🌐 Loading recipes from Cloudflare API...")
            println("🌐 API URL: https://recipes-api.recipedos.workers.dev/api/recipes")
            
            val response = apiService.getRecipes()
            println("🌐 API Response Code: ${response.code()}")
            println("🌐 API Response Message: ${response.message()}")
            println("🌐 API Response Headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                println("🌐 Response body is null: ${apiResponse == null}")
                println("🌐 Response message: ${apiResponse?.message}")
                
                val cloudflareRecipes = apiResponse?.recipes
                println("🌐 Recipes array size: ${cloudflareRecipes?.size ?: 0}")
                
                if (cloudflareRecipes != null && cloudflareRecipes.isNotEmpty()) {
                    println("📋 Found ${cloudflareRecipes.size} recipes from Cloudflare API")
                    println("📋 First recipe: ${cloudflareRecipes.firstOrNull()?.title}")
                    
                    val recipes = mutableListOf<Recipe>()
                    val nutritionList = mutableListOf<Nutrition>()
                    val ingredientsList = mutableListOf<Ingredient>()
                    val instructionsList = mutableListOf<Instruction>()
                    val tagsList = mutableListOf<Tag>()
                    val equipmentList = mutableListOf<Equipment>()
                    
                    cloudflareRecipes.forEach { cloudflareRecipe ->
                        println("🔄 Processing recipe: ${cloudflareRecipe.title}")
                        
                        // Convert Cloudflare recipe to local Recipe model
                        val recipe = Recipe(
                            id = cloudflareRecipe.id,
                            type = "food", // Default type
                            name = cloudflareRecipe.title,
                            summary = cloudflareRecipe.description ?: "",
                            imageURL = cloudflareRecipe.image_url,
                            pinImageURL = cloudflareRecipe.image_url, // Use same image for pin
                            pinImageRepinID = "",
                            servings = cloudflareRecipe.servings?.toString() ?: "1",
                            servingsUnit = "servings",
                            servingsAdvancedEnabled = "false",
                            prepTime = cloudflareRecipe.prep_time?.toString() ?: "0",
                            prepTimeZero = "false",
                            cookTime = cloudflareRecipe.cook_time?.toString() ?: "0",
                            cookTimeZero = "false",
                            totalTime = ((cloudflareRecipe.prep_time ?: 0) + (cloudflareRecipe.cook_time ?: 0)).toString(),
                            customTime = "",
                            customTimeZero = "false",
                            customTimeLabel = "",
                            videoEmbed = "",
                            notes = "",
                            difficulty = cloudflareRecipe.difficulty ?: "Medium",
                            createdAt = cloudflareRecipe.created_at ?: System.currentTimeMillis().toString(),
                            updatedAt = cloudflareRecipe.updated_at ?: System.currentTimeMillis().toString()
                        )
                        
                        // Convert ingredients
                        cloudflareRecipe.ingredients?.forEachIndexed { index, ingredient ->
                            val ingredientModel = Ingredient(
                                id = "${recipe.id}_ingredient_$index",
                                recipeId = recipe.id,
                                amount = ingredient.amount?.toString(),
                                unit = ingredient.unit,
                                name = ingredient.name,
                                notes = ingredient.notes,
                                unitID = null,
                                type = null
                            )
                            ingredientsList.add(ingredientModel)
                        }
                        
                        // Convert instructions
                        cloudflareRecipe.instructions?.forEachIndexed { index, instruction ->
                            val instructionModel = Instruction(
                                id = "${recipe.id}_instruction_$index",
                                recipeId = recipe.id,
                                stepNumber = index + 1,
                                name = null,
                                text = instruction.instruction,
                                ingredients = null,
                                type = null,
                                imageURL = null
                            )
                            instructionsList.add(instructionModel)
                        }
                        
                        // Convert tags
                        cloudflareRecipe.tags?.let { tags ->
                            tags.cuisine?.forEach { cuisine ->
                                tagsList.add(Tag(
                                    id = "${recipe.id}_tag_cuisine_${cuisine.hashCode()}",
                                    recipeId = recipe.id,
                                    tagType = "cuisine",
                                    tagValue = cuisine
                                ))
                            }
                            tags.course?.forEach { course ->
                                tagsList.add(Tag(
                                    id = "${recipe.id}_tag_course_${course.hashCode()}",
                                    recipeId = recipe.id,
                                    tagType = "course",
                                    tagValue = course
                                ))
                            }
                            tags.keyword?.forEach { keyword ->
                                tagsList.add(Tag(
                                    id = "${recipe.id}_tag_keyword_${keyword.hashCode()}",
                                    recipeId = recipe.id,
                                    tagType = "keyword",
                                    tagValue = keyword
                                ))
                            }
                        }
                        
                        recipes.add(recipe)
                        println("✅ Loaded recipe: ${recipe.name} with ${ingredientsList.count { it.recipeId == recipe.id }} ingredients")
                    }
                    
                    println("🎉 Successfully loaded ${recipes.size} recipes from Cloudflare API!")
                    RecipeData(recipes, nutritionList, ingredientsList, instructionsList, tagsList, equipmentList)
                } else {
                    println("❌ Cloudflare API returned empty response")
                    RecipeData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
                }
            } else {
                println("❌ Cloudflare API request failed: ${response.code()} - ${response.message()}")
                println("❌ Error body: ${response.errorBody()?.string()}")
                RecipeData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
            }
        } catch (e: Exception) {
            println("💥 Error loading recipes from Cloudflare API: ${e.message}")
            e.printStackTrace()
            RecipeData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
    
    /**
     * Fallback method to load from local assets (same as original)
     */
    private suspend fun loadRecipesFromAssets(): RecipeData {
        return try {
            println("📖 Reading recipes.json from assets (fallback)...")
            val jsonString = context.assets.open("recipes.json").bufferedReader().use { it.readText() }
            println("📄 JSON file size: ${jsonString.length} characters")
            
            val gson = Gson()
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)
            val recipeArray = jsonObject.getAsJsonArray("recipeModel")
            
            println("📋 Found ${recipeArray.size()} recipes in JSON")
            
            val recipes = mutableListOf<Recipe>()
            val nutritionList = mutableListOf<Nutrition>()
            val ingredientsList = mutableListOf<Ingredient>()
            val instructionsList = mutableListOf<Instruction>()
            val tagsList = mutableListOf<Tag>()
            val equipmentList = mutableListOf<Equipment>()
            
            for (i in 0 until recipeArray.size()) {
                val recipeJson = recipeArray.get(i).asJsonObject
                
                val recipeId = recipeJson.get("id")?.asString ?: "recipe_$i"
                val recipe = Recipe(
                    id = recipeId,
                    type = recipeJson.get("type")?.asString,
                    name = recipeJson.get("name")?.asString ?: "Unknown Recipe",
                    summary = recipeJson.get("summary")?.asString ?: "",
                    imageURL = recipeJson.get("image_url")?.asString ?: "",
                    pinImageURL = recipeJson.get("pin_image_url")?.asString ?: "",
                    pinImageRepinID = recipeJson.get("pin_image_repin_id")?.asString ?: "",
                    servings = recipeJson.get("servings")?.asString ?: "4",
                    servingsUnit = recipeJson.get("servings_unit")?.asString ?: "people",
                    servingsAdvancedEnabled = recipeJson.get("servings_advanced_enabled")?.asString ?: "false",
                    prepTime = recipeJson.get("prep_time")?.asString ?: "0",
                    prepTimeZero = recipeJson.get("prep_time_zero")?.asString ?: "false",
                    cookTime = recipeJson.get("cook_time")?.asString ?: "0",
                    cookTimeZero = recipeJson.get("cook_time_zero")?.asString ?: "false",
                    totalTime = recipeJson.get("total_time")?.asString ?: "0",
                    customTime = recipeJson.get("custom_time")?.asString ?: "",
                    customTimeZero = recipeJson.get("custom_time_zero")?.asString ?: "false",
                    customTimeLabel = recipeJson.get("custom_time_label")?.asString ?: "",
                    videoEmbed = recipeJson.get("video_embed")?.asString ?: "",
                    notes = recipeJson.get("notes")?.asString ?: "",
                    difficulty = recipeJson.get("difficulty")?.asString ?: "Medium",
                    createdAt = recipeJson.get("created_at")?.asString ?: System.currentTimeMillis().toString(),
                    updatedAt = recipeJson.get("updated_at")?.asString ?: System.currentTimeMillis().toString()
                )
                
                // Parse ingredients_flat
                val ingredientsArray = recipeJson.getAsJsonArray("ingredients_flat")
                ingredientsArray?.forEach { ingredientElement ->
                    val ingredientJson = ingredientElement.asJsonObject
                    val ingredient = Ingredient(
                        id = "${recipeId}_ingredient_${ingredientJson.get("uid")?.asInt ?: 0}",
                        recipeId = recipeId,
                        amount = ingredientJson.get("amount")?.asString,
                        unit = ingredientJson.get("unit")?.asString,
                        name = ingredientJson.get("name")?.asString,
                        notes = ingredientJson.get("notes")?.asString,
                        unitID = ingredientJson.get("unit_id")?.asInt,
                        type = ingredientJson.get("type")?.asString
                    )
                    ingredientsList.add(ingredient)
                }
                
                // Parse instructions_flat
                val instructionsArray = recipeJson.getAsJsonArray("instructions_flat")
                instructionsArray?.forEachIndexed { index, instructionElement ->
                    val instructionJson = instructionElement.asJsonObject
                    val instruction = Instruction(
                        id = "${recipeId}_instruction_${instructionJson.get("uid")?.asInt ?: index}",
                        recipeId = recipeId,
                        stepNumber = index + 1,
                        name = instructionJson.get("name")?.asString,
                        text = instructionJson.get("instruction")?.asString,
                        ingredients = null,
                        type = instructionJson.get("type")?.asString,
                        imageURL = instructionJson.get("image_url")?.asString
                    )
                    instructionsList.add(instruction)
                }
                
                // Parse tags
                val tagsJson = recipeJson.getAsJsonObject("tags")
                if (tagsJson != null) {
                    // Cuisine tags
                    tagsJson.getAsJsonArray("cuisine")?.forEach { cuisineElement ->
                        val cuisine = cuisineElement.asString
                        tagsList.add(Tag(
                            id = "${recipeId}_tag_cuisine_${cuisine.hashCode()}",
                            recipeId = recipeId,
                            tagType = "cuisine",
                            tagValue = cuisine
                        ))
                    }
                    // Course tags
                    tagsJson.getAsJsonArray("course")?.forEach { courseElement ->
                        val course = courseElement.asString
                        tagsList.add(Tag(
                            id = "${recipeId}_tag_course_${course.hashCode()}",
                            recipeId = recipeId,
                            tagType = "course",
                            tagValue = course
                        ))
                    }
                    // Keyword tags
                    tagsJson.getAsJsonArray("keyword")?.forEach { keywordElement ->
                        val keyword = keywordElement.asString
                        tagsList.add(Tag(
                            id = "${recipeId}_tag_keyword_${keyword.hashCode()}",
                            recipeId = recipeId,
                            tagType = "keyword",
                            tagValue = keyword
                        ))
                    }
                }
                
                // Parse nutrition data
                val nutritionJson = recipeJson.getAsJsonObject("nutrition")
                if (nutritionJson != null) {
                    val nutrition = Nutrition(
                        id = "${recipe.id}_nutrition",
                        recipeId = recipe.id,
                        calories = nutritionJson.get("calories")?.asDouble,
                        protein = nutritionJson.get("protein")?.asDouble,
                        carbohydrates = nutritionJson.get("carbs")?.asDouble,
                        fat = nutritionJson.get("fat")?.asDouble,
                        fiber = nutritionJson.get("fiber")?.asDouble,
                        sugar = nutritionJson.get("sugar")?.asDouble,
                        sodium = nutritionJson.get("sodium")?.asDouble
                    )
                    nutritionList.add(nutrition)
                }
                
                // Parse equipment
                val equipmentArray = recipeJson.getAsJsonArray("equipment")
                equipmentArray?.forEach { equipmentElement ->
                    val equipmentJson = equipmentElement.asJsonObject
                    val equipment = Equipment(
                        id = "${recipeId}_equipment_${equipmentJson.get("uid")?.asInt ?: 0}",
                        recipeId = recipeId,
                        amount = equipmentJson.get("amount")?.asString ?: "",
                        name = equipmentJson.get("name")?.asString ?: "",
                        notes = equipmentJson.get("notes")?.asString ?: "",
                        uid = equipmentJson.get("uid")?.asInt ?: 0
                    )
                    equipmentList.add(equipment)
                }
                
                recipes.add(recipe)
                println("✅ Loaded recipe: ${recipe.name} with ${ingredientsList.count { it.recipeId == recipeId }} ingredients, ${instructionsList.count { it.recipeId == recipeId }} instructions")
            }
            
            println("🎉 Successfully loaded ${recipes.size} recipes, ${ingredientsList.size} ingredients, ${instructionsList.size} instructions from assets!")
            RecipeData(recipes, nutritionList, ingredientsList, instructionsList, tagsList, equipmentList)
        } catch (e: Exception) {
            println("💥 Error loading recipes from assets: ${e.message}")
            e.printStackTrace()
            RecipeData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
    
    data class RecipeData(
        val recipes: List<Recipe>,
        val nutrition: List<Nutrition>,
        val ingredients: List<Ingredient>,
        val instructions: List<Instruction>,
        val tags: List<Tag>,
        val equipment: List<Equipment>
    )
    
    /**
     * Load recipes from local JSON (your real recipes)
     */
    suspend fun refreshRecipes() {
        try {
            // Clear existing data first
            clearAllLocalData()

            println("📖 Loading recipes from local JSON (your real recipes)...")
            val recipeData = loadRecipesFromAssets()
            println("✅ Loaded ${recipeData.recipes.size} recipes from local JSON")

            if (recipeData.recipes.isNotEmpty()) {
                insertRecipeData(recipeData)
                println("✅ Successfully loaded complete recipe data from local JSON")
            } else {
                println("❌ No recipes found in local JSON")
            }
        } catch (e: Exception) {
            println("💥 Exception loading recipes from local JSON: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Helper method to insert recipe data into database
     */
    private suspend fun insertRecipeData(recipeData: RecipeData) {
        try {
            // Insert recipes
            if (recipeData.recipes.isNotEmpty()) {
                recipeDao.insertAllRecipes(recipeData.recipes)
                println("💾 Inserted ${recipeData.recipes.size} recipes from fallback")
            }

            // Insert nutrition data
            if (recipeData.nutrition.isNotEmpty()) {
                nutritionDao.insertAllNutrition(recipeData.nutrition)
                println("💾 Inserted ${recipeData.nutrition.size} nutrition records from fallback")
            }

            // Insert ingredients
            if (recipeData.ingredients.isNotEmpty()) {
                ingredientDao.insertAllIngredients(recipeData.ingredients)
                println("💾 Inserted ${recipeData.ingredients.size} ingredients from fallback")
            }

            // Insert instructions
            if (recipeData.instructions.isNotEmpty()) {
                instructionDao.insertAllInstructions(recipeData.instructions)
                println("💾 Inserted ${recipeData.instructions.size} instructions from fallback")
            }

            // Insert tags
            if (recipeData.tags.isNotEmpty()) {
                tagDao.insertAllTags(recipeData.tags)
                println("💾 Inserted ${recipeData.tags.size} tags from fallback")
            }

            // Insert equipment
            if (recipeData.equipment.isNotEmpty()) {
                equipmentDao.insertAllEquipment(recipeData.equipment)
                println("💾 Inserted ${recipeData.equipment.size} equipment items from fallback")
            }

            println("✅ Successfully loaded complete recipe data from fallback")
        } catch (e: Exception) {
            println("💥 Error inserting fallback data: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Rest of the methods remain the same...
    suspend fun createRecipe(recipe: Recipe): Boolean {
        return try {
            val cloudflareRecipe = recipe.toCloudflareModel()
            val response = apiService.createRecipe(cloudflareRecipe)
            if (response.isSuccessful) {
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
            val cloudflareRecipe = recipe.toCloudflareModel()
            val response = apiService.updateRecipe(recipe.id, cloudflareRecipe)
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
    
    // Shopping List operations (unchanged)
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
    
    // Inventory operations (unchanged)
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
    
    // Event operations (unchanged)
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()
    
    suspend fun addEvent(event: Event) = eventDao.insertEvent(event)
    
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    
    suspend fun deleteEvent(id: String) = eventDao.deleteEventById(id)
    
    // Search operations (unchanged)
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
    
    /**
     * Updated health check for Cloudflare API
     */
    suspend fun checkBackendHealth(): Boolean {
        return try {
            println("🏥 Checking Cloudflare backend health...")
            val response = apiService.healthCheck()
            println("🏥 Health check response: ${response.code()} - ${response.message()}")
            val isHealthy = response.isSuccessful && response.body()?.status == "healthy"
            println("🏥 Cloudflare backend is healthy: $isHealthy")
            isHealthy
        } catch (e: Exception) {
            println("💥 Cloudflare health check failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}

// Extension function to convert Recipe to Cloudflare model
private fun Recipe.toCloudflareModel(): com.chefpro4home.data.api.CloudflareRecipeModel {
    return com.chefpro4home.data.api.CloudflareRecipeModel(
        id = id,
        title = name,
        description = summary,
        prep_time = prepTime.toIntOrNull(),
        cook_time = cookTime.toIntOrNull(),
        servings = servings.toIntOrNull(),
        difficulty = difficulty,
        image_url = imageURL,
        created_at = createdAt,
        updated_at = updatedAt,
        ingredients = emptyList(),
        instructions = emptyList(),
        tags = null
    )
}