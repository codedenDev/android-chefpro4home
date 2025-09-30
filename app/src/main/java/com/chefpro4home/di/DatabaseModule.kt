package com.chefpro4home.di

import android.content.Context
import com.chefpro4home.data.database.RecipeDatabase
import com.chefpro4home.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRecipeDatabase(@ApplicationContext context: Context): RecipeDatabase {
        return RecipeDatabase.getDatabase(context)
    }

    @Provides
    fun provideRecipeDao(database: RecipeDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideIngredientDao(database: RecipeDatabase): IngredientDao {
        return database.ingredientDao()
    }

    @Provides
    fun provideInstructionDao(database: RecipeDatabase): InstructionDao {
        return database.instructionDao()
    }

    @Provides
    fun provideTagDao(database: RecipeDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideEquipmentDao(database: RecipeDatabase): EquipmentDao {
        return database.equipmentDao()
    }

    @Provides
    fun provideNutritionDao(database: RecipeDatabase): NutritionDao {
        return database.nutritionDao()
    }

    @Provides
    fun provideShoppingListDao(database: RecipeDatabase): ShoppingListDao {
        return database.shoppingListDao()
    }

    @Provides
    fun provideInventoryDao(database: RecipeDatabase): InventoryDao {
        return database.inventoryDao()
    }

    @Provides
    fun provideEventDao(database: RecipeDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
}
