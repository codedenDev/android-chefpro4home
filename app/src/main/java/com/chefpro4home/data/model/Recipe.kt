package com.chefpro4home.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "recipes")
@Parcelize
data class Recipe(
    @PrimaryKey val id: String,
    val type: String? = null,
    val imageURL: String,
    val pinImageURL: String = "",
    val pinImageRepinID: String = "",
    val name: String,
    val summary: String,
    val servings: String,
    val servingsUnit: String = "people",
    val servingsAdvancedEnabled: String = "false",
    val prepTime: String,
    val prepTimeZero: String = "false",
    val cookTime: String,
    val cookTimeZero: String = "false",
    val totalTime: String,
    val customTime: String = "",
    val customTimeZero: String = "false",
    val customTimeLabel: String = "",
    val videoEmbed: String = "",
    val notes: String = "",
    val difficulty: String = "Medium",
    val createdAt: String,
    val updatedAt: String
) : Parcelable

@Entity(tableName = "servings_advanced")
@Parcelize
data class ServingsAdvanced(
    @PrimaryKey val id: String,
    val recipeId: String,
    val shape: String,
    val unit: String,
    val diameter: Int,
    val width: Int,
    val length: Int,
    val height: Int
) : Parcelable

@Entity(tableName = "tags")
@Parcelize
data class Tag(
    @PrimaryKey val id: String,
    val recipeId: String,
    val tagType: String, // course, cuisine, keyword
    val tagValue: String
) : Parcelable

@Entity(tableName = "equipment")
@Parcelize
data class Equipment(
    @PrimaryKey val id: String,
    val recipeId: String,
    val amount: String = "",
    val name: String,
    val notes: String = "",
    val uid: Int = 0
) : Parcelable

@Entity(tableName = "ingredients")
@Parcelize
data class Ingredient(
    @PrimaryKey val id: String,
    val recipeId: String,
    val amount: String? = null,
    val unit: String? = null,
    val name: String? = null,
    val notes: String? = null,
    val unitID: Int? = null,
    val type: String? = null
) : Parcelable

@Entity(tableName = "instructions")
@Parcelize
data class Instruction(
    @PrimaryKey val id: String,
    val recipeId: String,
    val stepNumber: Int,
    val name: String? = null,
    val text: String? = null,
    val ingredients: String? = null, // JSON array as string
    val type: String? = null,
    val imageURL: String? = null
) : Parcelable

@Entity(tableName = "nutrition")
@Parcelize
data class Nutrition(
    @PrimaryKey val id: String,
    val recipeId: String,
    val calories: Double? = null,
    val protein: Double? = null,
    val carbohydrates: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val cholesterol: Double? = null
) : Parcelable

// Shopping List Models
@Entity(tableName = "shopping_items")
@Parcelize
data class ShoppingItem(
    @PrimaryKey val id: String,
    val name: String,
    val amount: String? = null,
    val unit: String? = null,
    val isCompleted: Boolean = false,
    val recipeId: String? = null,
    val createdAt: String
) : Parcelable

// Inventory Models
@Entity(tableName = "inventory_items")
@Parcelize
data class InventoryItem(
    @PrimaryKey val id: String,
    val name: String,
    val amount: String,
    val unit: String,
    val category: String,
    val expirationDate: String? = null,
    val barcode: String? = null,
    val createdAt: String
) : Parcelable

// Event Models
@Entity(tableName = "events")
@Parcelize
data class Event(
    @PrimaryKey val id: String,
    val name: String,
    val date: String,
    val location: String,
    val theme: String,
    val dressCode: String,
    val description: String? = null,
    val isExpandedVersion: Boolean = false,
    val createdAt: String
) : Parcelable


