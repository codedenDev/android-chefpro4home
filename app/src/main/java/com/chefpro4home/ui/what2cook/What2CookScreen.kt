package com.chefpro4home.ui.what2cook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chefpro4home.util.RecipeSearchService
import com.chefpro4home.ui.components.LoadingIndicator
import com.chefpro4home.ui.theme.ChefPro4HomeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun What2CookScreen(
    viewModel: What2CookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val quickActionResults by viewModel.quickActionResults.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedDiet by viewModel.selectedDiet.collectAsState()
    val maxTime by viewModel.maxTime.collectAsState()
    val maxServings by viewModel.maxServings.collectAsState()
    val useInventoryIngredients by viewModel.useInventoryIngredients.collectAsState()
    val quickActionTitle by viewModel.quickActionTitle.collectAsState()
    val quickActionSubtitle by viewModel.quickActionSubtitle.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "What 2 Cook",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Section
        SearchSection(
            searchText = searchText,
            onSearchTextChange = viewModel::updateSearchText,
            selectedDiet = selectedDiet,
            onDietChange = viewModel::updateSelectedDiet,
            maxTime = maxTime,
            onMaxTimeChange = viewModel::updateMaxTime,
            maxServings = maxServings,
            onMaxServingsChange = viewModel::updateMaxServings,
            useInventoryIngredients = useInventoryIngredients,
            onUseInventoryChange = viewModel::updateUseInventoryIngredients,
            onSearch = viewModel::searchRecipes
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Actions
        QuickActionsSection(
            onInventoryRecipes = viewModel::getInventoryRecipes,
            onPopularRecipes = viewModel::getPopularRecipes,
            onQuickMeals = viewModel::getQuickMeals,
            onRandomRecipes = viewModel::getRandomRecipes,
            onFavoriteRecipes = viewModel::getFavoriteRecipes
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            when {
                searchResults.isNotEmpty() -> {
                    SearchResultsSection(
                        results = searchResults,
                        onAddToShoppingList = { /* Add to shopping list */ }
                    )
                }
                quickActionResults.isNotEmpty() -> {
                    QuickActionResultsSection(
                        title = quickActionTitle,
                        subtitle = quickActionSubtitle,
                        results = quickActionResults,
                        onAddToShoppingList = { /* Add to shopping list */ }
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
fun SearchSection(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedDiet: RecipeSearchService.DietType,
    onDietChange: (RecipeSearchService.DietType) -> Unit,
    maxTime: Int,
    onMaxTimeChange: (Int) -> Unit,
    maxServings: Int,
    onMaxServingsChange: (Int) -> Unit,
    useInventoryIngredients: Boolean,
    onUseInventoryChange: (Boolean) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Search Recipes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search input
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter ingredients (comma separated)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Diet filter
            Text(
                text = "Diet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(RecipeSearchService.DietType.values()) { diet ->
                    FilterChip(
                        selected = selectedDiet == diet,
                        onClick = { onDietChange(diet) },
                        label = { Text(diet.name.replace("_", " ")) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Time and servings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Max Time (min)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = maxTime.toFloat(),
                        onValueChange = { onMaxTimeChange(it.toInt()) },
                        valueRange = 15f..180f,
                        steps = 10
                    )
                    Text(
                        text = "${maxTime} minutes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Max Servings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = maxServings.toFloat(),
                        onValueChange = { onMaxServingsChange(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text(
                        text = "$maxServings servings",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search button
            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search Recipes")
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onInventoryRecipes: () -> Unit,
    onPopularRecipes: () -> Unit,
    onQuickMeals: () -> Unit,
    onRandomRecipes: () -> Unit,
    onFavoriteRecipes: () -> Unit
) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                QuickActionCard(
                    title = "Inventory Recipes",
                    subtitle = "Use what you have",
                    onClick = onInventoryRecipes
                )
            }
            item {
                QuickActionCard(
                    title = "Popular Recipes",
                    subtitle = "Family favorites",
                    onClick = onPopularRecipes
                )
            }
            item {
                QuickActionCard(
                    title = "Quick Meals",
                    subtitle = "30 min or less",
                    onClick = onQuickMeals
                )
            }
            item {
                QuickActionCard(
                    title = "Random Recipes",
                    subtitle = "Surprise me!",
                    onClick = onRandomRecipes
                )
            }
            item {
                QuickActionCard(
                    title = "Favorites",
                    subtitle = "Your saved recipes",
                    onClick = onFavoriteRecipes
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SearchResultsSection(
    results: List<RecipeSearchService.RecipeSearchResult>,
    onAddToShoppingList: (RecipeSearchService.RecipeSearchResult) -> Unit
) {
    Column {
        Text(
            text = "Search Results",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { result ->
                RecipeSearchResultCard(
                    result = result,
                    onAddToShoppingList = onAddToShoppingList
                )
            }
        }
    }
}

@Composable
fun QuickActionResultsSection(
    title: String,
    subtitle: String,
    results: List<RecipeSearchService.RecipeSearchResult>,
    onAddToShoppingList: (RecipeSearchService.RecipeSearchResult) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { result ->
                RecipeSearchResultCard(
                    result = result,
                    onAddToShoppingList = onAddToShoppingList
                )
            }
        }
    }
}

@Composable
fun RecipeSearchResultCard(
    result: RecipeSearchService.RecipeSearchResult,
    onAddToShoppingList: (RecipeSearchService.RecipeSearchResult) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Recipe image
            AsyncImage(
                model = result.imageURL,
                contentDescription = result.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Recipe info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    result.prepTime?.let { time ->
                        Text(
                            text = "${time} min",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    result.servings?.let { servings ->
                        Text(
                            text = "$servings servings",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Add to shopping list button
            IconButton(
                onClick = { onAddToShoppingList(result) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add to Shopping List"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuickActionCardPreview() {
    ChefPro4HomeTheme {
        QuickActionCard(
            title = "Quick Meals",
            subtitle = "30 min or less",
            onClick = {}
        )
    }
}
