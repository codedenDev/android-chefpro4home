package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.Instruction
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructionDao {
    @Query("SELECT * FROM instructions WHERE recipeId = :recipeId ORDER BY stepNumber")
    fun getInstructionsByRecipeId(recipeId: String): Flow<List<Instruction>>

    @Query("SELECT * FROM instructions WHERE recipeId = :recipeId ORDER BY stepNumber")
    suspend fun getInstructionsByRecipeIdSync(recipeId: String): List<Instruction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstruction(instruction: Instruction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInstructions(instructions: List<Instruction>)

    @Update
    suspend fun updateInstruction(instruction: Instruction)

    @Delete
    suspend fun deleteInstruction(instruction: Instruction)

    @Query("DELETE FROM instructions WHERE recipeId = :recipeId")
    suspend fun deleteInstructionsByRecipeId(recipeId: String)

    @Query("DELETE FROM instructions")
    suspend fun deleteAllInstructions()
}
