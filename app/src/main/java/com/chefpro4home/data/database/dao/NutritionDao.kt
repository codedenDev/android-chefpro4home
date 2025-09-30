package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.Nutrition

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition WHERE recipeId = :recipeId LIMIT 1")
    suspend fun getNutritionByRecipeId(recipeId: String): Nutrition?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: Nutrition)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNutrition(nutrition: List<Nutrition>)

    @Update
    suspend fun updateNutrition(nutrition: Nutrition)

    @Delete
    suspend fun deleteNutrition(nutrition: Nutrition)

    @Query("DELETE FROM nutrition WHERE recipeId = :recipeId")
    suspend fun deleteNutritionByRecipeId(recipeId: String)

    @Query("DELETE FROM nutrition")
    suspend fun deleteAllNutrition()
}
