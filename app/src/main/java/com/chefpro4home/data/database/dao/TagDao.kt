package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags WHERE recipeId = :recipeId")
    fun getTagsByRecipeId(recipeId: String): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE recipeId = :recipeId")
    suspend fun getTagsByRecipeIdSync(recipeId: String): List<Tag>

    @Query("SELECT * FROM tags WHERE recipeId = :recipeId AND tagType = :tagType")
    suspend fun getTagsByRecipeIdAndType(recipeId: String, tagType: String): List<Tag>

    @Query("SELECT DISTINCT tagValue FROM tags WHERE tagType = :tagType ORDER BY tagValue")
    suspend fun getTagValuesByType(tagType: String): List<String>
    
    @Query("SELECT DISTINCT tagValue FROM tags WHERE tagType = :tagType ORDER BY tagValue")
    suspend fun getTagsByType(tagType: String): List<String>
    
    @Query("SELECT COUNT(DISTINCT recipeId) FROM tags WHERE tagType = :tagType AND tagValue = :tagValue")
    suspend fun getRecipeCountForTag(tagType: String, tagValue: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(tags: List<Tag>)

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE recipeId = :recipeId")
    suspend fun deleteTagsByRecipeId(recipeId: String)

    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()
}


