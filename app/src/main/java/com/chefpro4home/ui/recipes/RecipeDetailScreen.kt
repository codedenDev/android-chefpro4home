package com.chefpro4home.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chefpro4home.data.model.Ingredient
import com.chefpro4home.data.model.Instruction
import com.chefpro4home.data.model.Nutrition
import com.chefpro4home.data.model.Recipe
import com.chefpro4home.data.model.Tag
import com.chefpro4home.ui.theme.ChefPro4HomeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val recipe by viewModel.recipe.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val instructions by viewModel.instructions.collectAsState()
    val nutrition by viewModel.nutrition.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    
    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }
    
    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    val currentRecipe = recipe!!
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(currentRecipe.name) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { viewModel.shareRecipe() }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share recipe"
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipe Image
            item {
                AsyncImage(
                    model = currentRecipe.imageURL,
                    contentDescription = currentRecipe.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Recipe Title and Summary
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentRecipe.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (currentRecipe.summary.isNotEmpty()) {
                        Text(
                            text = currentRecipe.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            
            // Recipe Stats
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RecipeStatItem(
                            label = "Servings",
                            value = currentRecipe.servings
                        )
                        RecipeStatItem(
                            label = "Prep Time",
                            value = "${currentRecipe.prepTime} min"
                        )
                        RecipeStatItem(
                            label = "Cook Time",
                            value = "${currentRecipe.cookTime} min"
                        )
                        RecipeStatItem(
                            label = "Total Time",
                            value = "${currentRecipe.totalTime} min"
                        )
                    }
                }
            }
            
            // Ingredients Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (ingredients.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "No ingredients available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ingredients.forEach { ingredient ->
                                    val ingredientText = buildString {
                                        append("• ")
                                        if (!ingredient.amount.isNullOrEmpty()) {
                                            append("${ingredient.amount} ")
                                        }
                                        if (!ingredient.unit.isNullOrEmpty()) {
                                            append("${ingredient.unit} ")
                                        }
                                        append(ingredient.name ?: "Unknown ingredient")
                                        if (!ingredient.notes.isNullOrEmpty()) {
                                            append(" (${ingredient.notes})")
                                        }
                                    }
                                    Text(
                                        text = ingredientText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Instructions Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (instructions.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "No instructions available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                instructions.filter { it.type == "instruction" }.forEachIndexed { index, instruction ->
                                    InstructionStep(
                                        stepNumber = index + 1,
                                        instruction = instruction.text ?: ""
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Nutrition Section
            nutrition?.let { nutritionData ->
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Nutrition Information",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                nutritionData.calories?.let { calories ->
                                    item {
                                        NutritionItem(
                                            label = "Calories",
                                            value = calories.toInt().toString(),
                                            unit = "kcal"
                                        )
                                    }
                                }
                                nutritionData.protein?.let { protein ->
                                    item {
                                        NutritionItem(
                                            label = "Protein",
                                            value = protein.toInt().toString(),
                                            unit = "g"
                                        )
                                    }
                                }
                                nutritionData.carbohydrates?.let { carbs ->
                                    item {
                                        NutritionItem(
                                            label = "Carbs",
                                            value = carbs.toInt().toString(),
                                            unit = "g"
                                        )
                                    }
                                }
                                nutritionData.fat?.let { fat ->
                                    item {
                                        NutritionItem(
                                            label = "Fat",
                                            value = fat.toInt().toString(),
                                            unit = "g"
                                        )
                                    }
                                }
                                nutritionData.fiber?.let { fiber ->
                                    item {
                                        NutritionItem(
                                            label = "Fiber",
                                            value = fiber.toInt().toString(),
                                            unit = "g"
                                        )
                                    }
                                }
                                nutritionData.sugar?.let { sugar ->
                                    item {
                                        NutritionItem(
                                            label = "Sugar",
                                            value = sugar.toInt().toString(),
                                            unit = "g"
                                        )
                                    }
                                }
                                nutritionData.sodium?.let { sodium ->
                                    item {
                                        NutritionItem(
                                            label = "Sodium",
                                            value = sodium.toInt().toString(),
                                            unit = "mg"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InstructionStep(
    stepNumber: Int,
    instruction: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Step number
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Instruction text
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NutritionItem(
    label: String,
    value: String,
    unit: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

