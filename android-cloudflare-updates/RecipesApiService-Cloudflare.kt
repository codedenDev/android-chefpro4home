package com.chefpro4home.data.api

import com.chefpro4home.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Updated RecipesApiService for Cloudflare integration
 * 
 * This service connects to the Cloudflare Workers API instead of WordPress,
 * providing complete WordPress independence with better performance and reliability.
 */
interface RecipesApiService {
    
    // Cloudflare API Base URL
    companion object {
        const val CLOUDFLARE_BASE_URL = "https://recipes-api.recipedos.workers.dev"
    }
    
    // Recipe CRUD operations
    @GET("api/recipes")
    suspend fun getRecipes(): Response<CloudflareApiResponse>
    
    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): Response<CloudflareSingleRecipeResponse>
    
    @POST("api/recipes")
    suspend fun createRecipe(@Body recipe: CloudflareRecipeModel): Response<CloudflareRecipeModel>
    
    @PUT("api/recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: String, @Body recipe: CloudflareRecipeModel): Response<CloudflareRecipeModel>
    
    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String): Response<Unit>
    
    // Health monitoring
    @GET("api/health")
    suspend fun healthCheck(): Response<CloudflareHealthResponse>
    
    // Image operations (served from R2)
    @GET("api/images/{filename}")
    suspend fun getImage(@Path("filename") filename: String): Response<okhttp3.ResponseBody>
    
    @Multipart
    @POST("api/images/upload")
    suspend fun uploadImage(@Part file: okhttp3.MultipartBody.Part): Response<CloudflareImageUploadResponse>
    
    // Migration endpoint
    @POST("api/migrate")
    suspend fun migrateData(@Body migrationData: CloudflareMigrationData): Response<CloudflareMigrationResponse>
}

// MARK: - Cloudflare API Response Models

/**
 * Cloudflare API response structure matching the iOS app format
 */
data class CloudflareApiResponse(
    val success: Boolean,
    val data: CloudflareRecipesData
)

data class CloudflareRecipesData(
    val recipes: List<CloudflareRecipeModel>,
    val total: Int
)

data class CloudflareSingleRecipeResponse(
    val success: Boolean,
    val data: CloudflareSingleRecipeData
)

data class CloudflareSingleRecipeData(
    val recipe: CloudflareRecipeModel
)

/**
 * Cloudflare Recipe Model - matches iOS RecipeModel structure
 */
data class CloudflareRecipeModel(
    val id: String,
    val type: String? = "food",
    val image_url: String,
    val pin_image_url: String,
    val pin_image_repin_id: String? = "",
    val name: String,
    val summary: String? = "",
    val servings: String? = "1",
    val servings_unit: String? = "servings",
    val servings_advanced_enabled: String? = "",
    val servings_advanced: CloudflareServingsAdvanced? = null,
    val prep_time: String? = "0",
    val prep_time_zero: String? = "",
    val cook_time: String? = "0",
    val cook_time_zero: String? = "",
    val total_time: String? = "0",
    val custom_time: String? = "0",
    val custom_time_zero: String? = "",
    val custom_time_label: String? = "",
    val tags: CloudflareTags? = null,
    val equipment: List<CloudflareEquipment>? = emptyList(),
    val ingredients_flat: List<CloudflareIngredient>? = emptyList(),
    val instructions_flat: List<CloudflareInstruction>? = emptyList(),
    val video_embed: String? = "",
    val notes: String? = "",
    val nutrition: CloudflareNutrition? = null,
    val difficulty: String? = "medium",
    val created_at: String? = "",
    val updated_at: String? = ""
)

data class CloudflareServingsAdvanced(
    val shape: String? = "round",
    val unit: String? = "inch",
    val diameter: Int? = 0,
    val width: Int? = 0,
    val length: Int? = 0,
    val height: Int? = 0
)

data class CloudflareTags(
    val course: List<String>? = emptyList(),
    val cuisine: List<String>? = emptyList(),
    val keyword: List<String>? = emptyList()
)

data class CloudflareEquipment(
    val amount: String? = "",
    val name: String,
    val notes: String? = "",
    val uid: Int? = 0
)

data class CloudflareIngredient(
    val uid: Int? = 0,
    val amount: String? = "",
    val unit: String? = "",
    val name: String,
    val notes: String? = "",
    val unit_id: Int? = null,
    val type: String? = "ingredient"
)

data class CloudflareInstruction(
    val step_number: Int? = 1,
    val instruction: String,
    val image_url: String? = null
)

data class CloudflareNutrition(
    val calories: Double? = null,
    val protein: Double? = null,
    val carbohydrates: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val cholesterol: Double? = null
)

data class CloudflareHealthResponse(
    val status: String,
    val timestamp: String,
    val uptime: Long,
    val services: CloudflareServicesStatus,
    val version: String,
    val environment: String
)

data class CloudflareServicesStatus(
    val database: String,
    val storage: String,
    val worker: String
)

data class CloudflareImageUploadResponse(
    val message: String,
    val filename: String,
    val image_url: String
)

data class CloudflareMigrationData(
    val recipes: List<CloudflareRecipeModel>
)

data class CloudflareMigrationResponse(
    val success: Boolean,
    val message: String,
    val results: CloudflareMigrationResults
)

data class CloudflareMigrationResults(
    val recipesProcessed: Int,
    val recipesCreated: Int,
    val recipesUpdated: Int,
    val errors: List<CloudflareMigrationError>
)

data class CloudflareMigrationError(
    val recipeId: String,
    val recipeName: String,
    val error: String
)

