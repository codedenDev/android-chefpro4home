package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.Equipment
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment WHERE recipeId = :recipeId ORDER BY name")
    fun getEquipmentByRecipeId(recipeId: String): Flow<List<Equipment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: Equipment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEquipment(equipment: List<Equipment>)

    @Update
    suspend fun updateEquipment(equipment: Equipment)

    @Delete
    suspend fun deleteEquipment(equipment: Equipment)

    @Query("DELETE FROM equipment WHERE recipeId = :recipeId")
    suspend fun deleteEquipmentByRecipeId(recipeId: String)

    @Query("DELETE FROM equipment")
    suspend fun deleteAllEquipment()
}
