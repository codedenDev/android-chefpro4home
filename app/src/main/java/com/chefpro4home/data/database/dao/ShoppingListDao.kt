package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getPendingItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE recipeId = :recipeId")
    fun getItemsByRecipeId(recipeId: String): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items: List<ShoppingItem>)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM shopping_items WHERE isCompleted = 1")
    suspend fun deleteCompletedItems()

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAllItems()

    @Query("UPDATE shopping_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateItemCompletion(id: String, isCompleted: Boolean)
}


