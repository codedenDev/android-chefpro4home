package com.chefpro4home.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.chefpro4home.data.model.*
import com.chefpro4home.data.database.dao.*

@Database(
    entities = [
        Recipe::class,
        ServingsAdvanced::class,
        Tag::class,
        Equipment::class,
        Ingredient::class,
        Instruction::class,
        Nutrition::class,
        ShoppingItem::class,
        InventoryItem::class,
        Event::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun instructionDao(): InstructionDao
    abstract fun tagDao(): TagDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipes_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}



