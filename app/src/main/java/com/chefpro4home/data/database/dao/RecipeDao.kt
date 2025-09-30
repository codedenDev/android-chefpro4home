package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): Recipe?

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :searchTerm || '%' OR summary LIKE '%' || :searchTerm || '%'")
    fun searchRecipes(searchTerm: String): Flow<List<Recipe>>

    @Query("SELECT DISTINCT tagValue FROM tags WHERE tagType = :tagType ORDER BY tagValue")
    suspend fun getTagsByType(tagType: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecipes(recipes: List<Recipe>)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: String)

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}
