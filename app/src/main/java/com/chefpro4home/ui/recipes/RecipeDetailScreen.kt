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
import com.chefpro4home.data.model.Recipe
import com.chefpro4home.ui.theme.ChefPro4HomeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsState()
    val recipe = recipes.find { it.id == recipeId }
    
    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Recipe not found")
        }
        return
    }
    
    var isFavorite by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text(recipe.name) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { /* Share recipe */ }) {
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
                    model = recipe.imageURL,
                    contentDescription = recipe.name,
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
                        text = recipe.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (recipe.summary.isNotEmpty()) {
                        Text(
                            text = recipe.summary,
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
                            value = recipe.servings
                        )
                        RecipeStatItem(
                            label = "Prep Time",
                            value = "${recipe.prepTime} min"
                        )
                        RecipeStatItem(
                            label = "Cook Time",
                            value = "${recipe.cookTime} min"
                        )
                        RecipeStatItem(
                            label = "Total Time",
                            value = "${recipe.totalTime} min"
                        )
                    }
                }
            }
            
            // Ingredients Section
            item {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Placeholder ingredients - in a real app, these would come from the database
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "• 2 cups black beans",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• 1 cup white rice",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• 1 onion, diced",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• 2 cloves garlic, minced",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• 1 bay leaf",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "• Salt and pepper to taste",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Instructions Section
            item {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Placeholder instructions - in a real app, these would come from the database
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        InstructionStep(
                            stepNumber = 1,
                            instruction = "Rinse the black beans and soak them overnight, or use canned beans for convenience."
                        )
                        InstructionStep(
                            stepNumber = 2,
                            instruction = "In a large pot, heat oil over medium heat. Add diced onion and cook until translucent."
                        )
                        InstructionStep(
                            stepNumber = 3,
                            instruction = "Add minced garlic and cook for 1 minute until fragrant."
                        )
                        InstructionStep(
                            stepNumber = 4,
                            instruction = "Add the black beans, bay leaf, and enough water to cover. Bring to a boil, then reduce heat and simmer for 45 minutes."
                        )
                        InstructionStep(
                            stepNumber = 5,
                            instruction = "Meanwhile, cook the rice according to package instructions."
                        )
                        InstructionStep(
                            stepNumber = 6,
                            instruction = "Season the beans with salt and pepper to taste. Serve the beans over the rice."
                        )
                    }
                }
            }
            
            // Nutrition Section
            item {
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
                        item {
                            NutritionItem(
                                label = "Calories",
                                value = "472",
                                unit = "kcal"
                            )
                        }
                        item {
                            NutritionItem(
                                label = "Protein",
                                value = "7",
                                unit = "g"
                            )
                        }
                        item {
                            NutritionItem(
                                label = "Carbs",
                                value = "24",
                                unit = "g"
                            )
                        }
                        item {
                            NutritionItem(
                                label = "Fat",
                                value = "40",
                                unit = "g"
                            )
                        }
                        item {
                            NutritionItem(
                                label = "Fiber",
                                value = "8",
                                unit = "g"
                            )
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

