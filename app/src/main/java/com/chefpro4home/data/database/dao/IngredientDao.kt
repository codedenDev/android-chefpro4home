package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY id")
    fun getIngredientsByRecipeId(recipeId: String): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredientsByRecipeIdSync(recipeId: String): List<Ingredient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIngredients(ingredients: List<Ingredient>)

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: String)

    @Query("DELETE FROM ingredients")
    suspend fun deleteAllIngredients()
}


