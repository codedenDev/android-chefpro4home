package com.chefpro4home.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.chefpro4home.data.model.Recipe
import com.chefpro4home.data.model.Tag
import com.chefpro4home.ui.components.LoadingIndicator
import com.chefpro4home.ui.theme.ChefPro4HomeTheme
import androidx.compose.ui.res.painterResource
import com.chefpro4home.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    navController: NavController? = null,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedCuisine by viewModel.selectedCuisine.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with logo, title, and settings
        RecipeHeader(
            onSettingsClick = { /* Navigate to settings */ },
            onRefreshClick = { viewModel.refreshRecipes() }
        )
        
        // Search bar
        SearchBar(
            text = searchText,
            onTextChange = viewModel::updateSearchText
        )
        
        // Cuisine filter chips
        CuisineFilterChips(
            selectedCuisine = selectedCuisine,
            onCuisineSelected = viewModel::updateSelectedCuisine
        )
        
        // Recipe grid
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { 
                            navController?.navigate("recipe_detail/${recipe.id}")
                        },
                        onAddToShoppingList = { selectedRecipe ->
                            // Add recipe ingredients to shopping list
                            viewModel.addRecipeToShoppingList(selectedRecipe)
                        }
                    )
                }
            }
        }
    }
    
    // Show error message if any
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show snackbar or error dialog
            viewModel.clearError()
        }
    }
}

@Composable
fun RecipeHeader(
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chef Pro Logo
        Image(
            painter = painterResource(id = R.drawable.ic_chef_pro_brand),
            contentDescription = "Chef Pro Logo",
            modifier = Modifier
                .size(80.dp, 40.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Recipes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Discover & Cook",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Row {
            // Refresh button
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Settings button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    text: String,
    onTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search recipes...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun CuisineFilterChips(
    selectedCuisine: String?,
    onCuisineSelected: (String?) -> Unit,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val availableCuisines by viewModel.availableCuisines.collectAsState()
    val recipeCounts by viewModel.cuisineRecipeCounts.collectAsState()
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(availableCuisines) { cuisine ->
            val isSelected = (cuisine == "All" && selectedCuisine == null) || (cuisine == selectedCuisine)
            val count = recipeCounts[cuisine] ?: 0
            
            FilterChip(
                onClick = { onCuisineSelected(cuisine) },
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cuisine)
                        // Recipe count badge
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                           else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                selected = isSelected
            )
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onAddToShoppingList: (Recipe) -> Unit = {},
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val tags = remember(recipe.id) {
        mutableStateOf<List<com.chefpro4home.data.model.Tag>>(emptyList())
    }
    
    LaunchedEffect(recipe.id) {
        // Load tags for this recipe
        val loadedTags = viewModel.getTagsForRecipe(recipe.id)
        tags.value = loadedTags
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Recipe image
            AsyncImage(
                model = recipe.imageURL,
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            // Recipe info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = recipe.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${recipe.servings} servings",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "${recipe.cookTime} min cook",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Action buttons row - matches iOS ElegantRecipeListCard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorite button (heart)
                    IconButton(
                        onClick = { /* Toggle favorite - iOS integration TODO */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder, // TODO: Check favorite status
                            contentDescription = "Add to favorites",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Cuisine tags (centered) - matches iOS
                    val cuisineTags = tags.value.filter { it.tagType == "cuisine" }.take(2)
                    if (cuisineTags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            cuisineTags.forEach { tag ->
                                Text(
                                    text = tag.tagValue,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    // Shopping cart button
                    IconButton(
                        onClick = { onAddToShoppingList(recipe) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Add to shopping list",
                            tint = MaterialTheme.colorScheme.primary, // TODO: Check if in shopping list
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeCardPreview() {
    ChefPro4HomeTheme {
        RecipeCard(
            recipe = Recipe(
                id = "1",
                name = "Black Beans & Rice",
                summary = "My Cuban mother's go-to recipe, black beans and rice is a simple, comforting dish full of flavor and tradition.",
                imageURL = "",
                servings = "6",
                prepTime = "30",
                cookTime = "45",
                totalTime = "75",
                createdAt = "",
                updatedAt = ""
            ),
            onClick = {}
        )
    }
}

