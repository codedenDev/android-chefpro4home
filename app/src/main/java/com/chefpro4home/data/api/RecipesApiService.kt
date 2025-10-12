package com.chefpro4home.data.api

import com.chefpro4home.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface RecipesApiService {
    @GET("api/recipes")
    suspend fun getRecipes(): Response<ApiResponse>

    @GET("api/recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): Response<Recipe>

    @POST("api/recipes")
    suspend fun createRecipe(@Body recipe: RecipeApiModel): Response<RecipeApiModel>

    @PUT("api/recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: String, @Body recipe: RecipeApiModel): Response<RecipeApiModel>

    @DELETE("api/recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String): Response<Unit>

    @GET("api/health")
    suspend fun healthCheck(): Response<HealthResponse>

    @GET("api/images/{filename}")
    suspend fun getImage(@Path("filename") filename: String): Response<okhttp3.ResponseBody>

    @Multipart
    @POST("api/images/upload")
    suspend fun uploadImage(@Part file: okhttp3.MultipartBody.Part): Response<ImageUploadResponse>
}

// API Response Models
data class ApiResponse(
    val message: String,
    val recipes: List<RecipeApiModel>
)

data class RecipeApiModel(
    val id: String,
    val title: String,
    val description: String,
    val prep_time: Int,
    val cook_time: Int,
    val servings: Int,
    val difficulty: String?,
    val image_url: String,
    val created_at: String?,
    val updated_at: String?,
    val ingredients: List<IngredientApiModel>,
    val instructions: List<InstructionApiModel>,
    val tags: TagsApiModel
)

data class IngredientApiModel(
    val name: String,
    val amount: Double,
    val unit: String,
    val notes: String? = null
)

data class InstructionApiModel(
    val step_number: Int,
    val instruction: String,
    val image_url: String? = null
)

data class TagsApiModel(
    val cuisine: List<String> = emptyList(),
    val course: List<String> = emptyList(),
    val keyword: List<String> = emptyList()
)

data class HealthResponse(
    val status: String,
    val timestamp: String,
    val uptime: Long,
    val services: ServicesStatus,
    val version: String,
    val environment: String
)

data class ServicesStatus(
    val database: String,
    val storage: String,
    val worker: String
)

data class ImageUploadResponse(
    val message: String,
    val filename: String,
    val image_url: String
)


