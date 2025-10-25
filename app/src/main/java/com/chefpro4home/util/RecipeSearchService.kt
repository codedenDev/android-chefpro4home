package com.chefpro4home.util

import android.util.Log
import com.chefpro4home.data.api.RecipesApiService
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeSearchService @Inject constructor(
    private val apiService: RecipesApiService
) {
    companion object {
        private const val TAG = "RecipeSearchService"
    }
    
    private val maxResults = 20
    private val spoonacularApiKey = "7586e03006f74affad896d020a5bc51c"
    private val edamamAppId = "d7ca72be"
    private val edamamAppKey = "a35636cb1874445aa657362a8a575449f05f3b065b95054c1ac9fef68ac4fbf3"
    
    private val httpClient = OkHttpClient.Builder().build()
    private val gson = Gson()

    // Helper function to clean HTML tags (equivalent to iOS cleanHTMLTags)
    private fun cleanHtmlTags(htmlString: String): String {
        // Remove HTML tags using regex
        val cleanString = htmlString.replace(Regex("<[^>]+>"), "")
        
        // Decode HTML entities
        val decodedString = cleanString
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
        
        // Clean up extra whitespace
        return decodedString.trim()
    }

    // Recipe search result data class
    data class RecipeSearchResult(
        val id: String,
        val title: String,
        val description: String,
        val imageURL: String,
        val source: RecipeSource,
        val ingredients: List<String>,
        val missingIngredients: List<String>,
        val prepTime: Int?,
        val servings: Int?,
        val url: String,
        val instructions: List<String>
    )

    enum class RecipeSource {
        LOCAL, SPOONACULAR, EDAMAM
    }

    enum class DietType(val rawValue: String, val displayName: String) {
        NONE("none", "Any Diet"),
        VEGETARIAN("vegetarian", "Vegetarian"),
        VEGAN("vegan", "Vegan"),
        GLUTEN_FREE("gluten-free", "Gluten-Free"),
        DAIRY_FREE("dairy-free", "Dairy-Free"),
        LOW_CARB("low-carb", "Low-Carb"),
        KETO("keto", "Keto"),
        PALEO("paleo", "Paleo")
    }

    // Multi-source recipe search
    suspend fun searchRecipes(
        ingredients: List<String>,
        diet: DietType = DietType.NONE,
        maxTime: Int = 60
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 RecipeSearchService: Starting search with ${ingredients.size} ingredients")
        Log.d(TAG, "🔍 Ingredients: $ingredients")
        
        val allResults = mutableListOf<RecipeSearchResult>()
        
        // 1. Search local recipes (iOS returns empty for What2Cook - external only)
        val localResults = searchLocalRecipes(ingredients, diet, maxTime)
        Log.d(TAG, "🔍 RecipeSearchService: Found ${localResults.size} local recipes")
        allResults.addAll(localResults)
        
        // 2. Search web APIs
        try {
            val webResults = searchSpoonacularByIngredients(ingredients, diet, maxTime)
            Log.d(TAG, "🔍 RecipeSearchService: Found ${webResults.size} Spoonacular recipes")
            allResults.addAll(webResults)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Spoonacular API error: ${e.message}")
        }
        
        Log.d(TAG, "🔍 RecipeSearchService: Returning ${allResults.size} final results")
        allResults.take(maxResults)
    }

    // Search local recipes (equivalent to iOS searchLocalRecipes)
    @Suppress("UNUSED_PARAMETER")
    private suspend fun searchLocalRecipes(
        ingredients: List<String>,
        diet: DietType,
        maxTime: Int
    ): List<RecipeSearchResult> {
        // This would query your local database
        // Implementation depends on your repository pattern
        return emptyList()
    }

    // Search Spoonacular recipes (equivalent to iOS searchSpoonacularRecipes)
    @Suppress("UNUSED_PARAMETER")
    private suspend fun searchSpoonacularRecipes(
        ingredients: List<String>,
        diet: DietType,
        maxTime: Int
    ): List<RecipeSearchResult> {
        // Implementation for Spoonacular API
        // This would make API calls to Spoonacular
        return emptyList()
    }

    // Search Edamam recipes (equivalent to iOS searchEdamamRecipes)
    @Suppress("UNUSED_PARAMETER")
    private suspend fun searchEdamamRecipes(
        ingredients: List<String>,
        diet: DietType,
        maxTime: Int
    ): List<RecipeSearchResult> {
        // Implementation for Edamam API
        // This would make API calls to Edamam
        return emptyList()
    }

    // Popular recipes search
    suspend fun searchPopularRecipes(
        diet: DietType = DietType.NONE,
        maxTime: Int = 60,
        maxServings: Int = 4
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 RecipeSearchService: Searching popular recipes")
        try {
            searchSpoonacularRandom(diet, maxTime, maxServings)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Popular recipes error: ${e.message}")
            emptyList()
        }
    }

    // Text-based search
    @Suppress("UNUSED_PARAMETER")
    suspend fun searchByText(
        query: String,
        diet: DietType = DietType.NONE,
        maxTime: Int = 60,
        maxServings: Int = 4
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 RecipeSearchService: Searching by text: $query")
        try {
            val ingredients = query.split(",", " ").map { it.trim() }.filter { it.isNotEmpty() }
            searchSpoonacularByIngredients(ingredients, diet, maxTime)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Text search error: ${e.message}")
            emptyList()
        }
    }

    // Quick meals search
    @Suppress("UNUSED_PARAMETER")
    suspend fun searchQuickMeals(
        diet: DietType = DietType.NONE,
        maxServings: Int = 4
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 RecipeSearchService: Searching quick meals")
        try {
            searchSpoonacularQuick(diet, maxServings)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Quick meals error: ${e.message}")
            emptyList()
        }
    }

    // Random recipes search
    @Suppress("UNUSED_PARAMETER")
    suspend fun searchRandomRecipes(
        diet: DietType = DietType.NONE,
        maxTime: Int = 60,
        maxServings: Int = 4
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔍 RecipeSearchService: Searching random recipes")
        try {
            searchSpoonacularRandom(diet, maxTime, maxServings)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Random recipes error: ${e.message}")
            emptyList()
        }
    }

    // Quick action methods (equivalent to iOS quick action methods)
    suspend fun getInventoryRecipes(): List<RecipeSearchResult> {
        // Get recipes based on inventory items
        return emptyList()
    }

    suspend fun getPopularRecipes(): List<RecipeSearchResult> {
        return searchPopularRecipes()
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun getQuickMeals(maxTime: Int = 30): List<RecipeSearchResult> {
        return searchQuickMeals()
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun getRandomRecipes(count: Int = 5): List<RecipeSearchResult> {
        return searchRandomRecipes()
    }

    suspend fun getFavoriteRecipes(): List<RecipeSearchResult> {
        // Get favorite recipes
        return emptyList()
    }

    // MARK: - Spoonacular API Implementation
    
    @Suppress("UNUSED_PARAMETER")
    private suspend fun searchSpoonacularByIngredients(
        ingredients: List<String>,
        diet: DietType,
        maxTime: Int
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        try {
            val ingredientsString = ingredients.joinToString(",")
            val dietParam = if (diet != DietType.NONE) "&diet=${diet.rawValue}" else ""
            
            val url = "https://api.spoonacular.com/recipes/findByIngredients?apiKey=$spoonacularApiKey&ingredients=$ingredientsString&number=$maxResults&ranking=2$dietParam"
            
            Log.d(TAG, "🌐 Calling Spoonacular API: $url")
            
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "❌ Spoonacular API returned ${response.code}")
                return@withContext emptyList()
            }
            
            val jsonArray = gson.fromJson(response.body?.string(), JsonArray::class.java)
            val results = mutableListOf<RecipeSearchResult>()
            
            jsonArray.forEach { element ->
                val recipeJson = element.asJsonObject
                val id = recipeJson.get("id")?.asString ?: return@forEach
                val title = recipeJson.get("title")?.asString ?: return@forEach
                val image = recipeJson.get("image")?.asString ?: ""
                
                val usedIngredients = recipeJson.getAsJsonArray("usedIngredients")?.map {
                    it.asJsonObject.get("name")?.asString ?: ""
                } ?: emptyList()
                
                val missedIngredients = recipeJson.getAsJsonArray("missedIngredients")?.map {
                    it.asJsonObject.get("name")?.asString ?: ""
                } ?: emptyList()
                
                results.add(RecipeSearchResult(
                    id = id,
                    title = title,
                    description = "",
                    imageURL = image,
                    source = RecipeSource.SPOONACULAR,
                    ingredients = usedIngredients,
                    missingIngredients = missedIngredients,
                    prepTime = null,
                    servings = null,
                    url = "",
                    instructions = listOf(
                        "Follow the recipe instructions from Spoonacular",
                        "Visit the recipe URL for detailed cooking steps",
                        "Adjust ingredients and cooking time as needed",
                        "Enjoy your delicious meal!"
                    )
                ))
            }
            
            Log.d(TAG, "✅ Found ${results.size} recipes from Spoonacular")
            results
        } catch (e: Exception) {
            Log.e(TAG, "❌ Spoonacular API error: ${e.message}")
            emptyList()
        }
    }
    
    @Suppress("UNUSED_PARAMETER")
    private suspend fun searchSpoonacularRandom(
        diet: DietType,
        maxTime: Int,
        maxServings: Int
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.spoonacular.com/recipes/random?apiKey=$spoonacularApiKey&number=$maxResults"
            
            Log.d(TAG, "🌐 Calling Spoonacular Random API: $url")
            
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "❌ Spoonacular Random API returned ${response.code}")
                return@withContext emptyList()
            }
            
            val jsonResponse = gson.fromJson(response.body?.string(), JsonObject::class.java)
            val recipesArray = jsonResponse.getAsJsonArray("recipes")
            val results = mutableListOf<RecipeSearchResult>()
            
            recipesArray?.forEach { element ->
                val recipe = element.asJsonObject
                val id = recipe.get("id")?.asString ?: return@forEach
                val title = recipe.get("title")?.asString ?: return@forEach
                val image = recipe.get("image")?.asString ?: ""
                val summary = cleanHtmlTags(recipe.get("summary")?.asString ?: "")
                val readyInMinutes = recipe.get("readyInMinutes")?.asInt
                val servings = recipe.get("servings")?.asInt
                
                val ingredients = recipe.getAsJsonArray("extendedIngredients")?.map {
                    it.asJsonObject.get("original")?.asString ?: ""
                } ?: emptyList()
                
                results.add(RecipeSearchResult(
                    id = id,
                    title = title,
                    description = summary,
                    imageURL = image,
                    source = RecipeSource.SPOONACULAR,
                    ingredients = ingredients,
                    missingIngredients = emptyList(),
                    prepTime = readyInMinutes,
                    servings = servings,
                    url = "",
                    instructions = listOf(
                        "Follow the recipe instructions from Spoonacular",
                        "Visit the recipe URL for detailed cooking steps",
                        "Adjust ingredients and cooking time as needed",
                        "Enjoy your delicious meal!"
                    )
                ))
            }
            
            Log.d(TAG, "✅ Found ${results.size} random recipes from Spoonacular")
            results
        } catch (e: Exception) {
            Log.e(TAG, "❌ Spoonacular Random API error: ${e.message}")
            emptyList()
        }
    }
    
    @Suppress("UNUSED_PARAMETER")
    private suspend fun searchSpoonacularQuick(
        diet: DietType,
        maxServings: Int
    ): List<RecipeSearchResult> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.spoonacular.com/recipes/random?apiKey=$spoonacularApiKey&number=$maxResults&tags=quick,easy"
            
            Log.d(TAG, "🌐 Calling Spoonacular Quick API: $url")
            
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "❌ Spoonacular Quick API returned ${response.code}")
                return@withContext emptyList()
            }
            
            val jsonResponse = gson.fromJson(response.body?.string(), JsonObject::class.java)
            val recipesArray = jsonResponse.getAsJsonArray("recipes")
            val results = mutableListOf<RecipeSearchResult>()
            
            recipesArray?.forEach { element ->
                val recipe = element.asJsonObject
                val readyInMinutes = recipe.get("readyInMinutes")?.asInt ?: Int.MAX_VALUE
                
                // Filter for recipes under 30 minutes
                if (readyInMinutes <= 30) {
                    val id = recipe.get("id")?.asString ?: return@forEach
                    val title = recipe.get("title")?.asString ?: return@forEach
                    val image = recipe.get("image")?.asString ?: ""
                    val summary = cleanHtmlTags(recipe.get("summary")?.asString ?: "")
                    val servings = recipe.get("servings")?.asInt
                    
                    val ingredients = recipe.getAsJsonArray("extendedIngredients")?.map {
                        it.asJsonObject.get("original")?.asString ?: ""
                    } ?: emptyList()
                    
                    results.add(RecipeSearchResult(
                        id = id,
                        title = title,
                        description = summary,
                        imageURL = image,
                        source = RecipeSource.SPOONACULAR,
                        ingredients = ingredients,
                        missingIngredients = emptyList(),
                        prepTime = readyInMinutes,
                        servings = servings,
                        url = "",
                        instructions = listOf(
                            "Follow the recipe instructions from Spoonacular",
                            "Visit the recipe URL for detailed cooking steps",
                            "Adjust ingredients and cooking time as needed",
                            "Enjoy your delicious meal!"
                        )
                    ))
                }
            }
            
            Log.d(TAG, "✅ Found ${results.size} quick recipes from Spoonacular")
            results
        } catch (e: Exception) {
            Log.e(TAG, "❌ Spoonacular Quick API error: ${e.message}")
            emptyList()
        }
    }

}



