package com.chefpro4home.ui.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chefpro4home.data.model.ShoppingItem
import com.chefpro4home.data.model.InventoryItem
import com.chefpro4home.ui.components.LoadingIndicator
import com.chefpro4home.ui.theme.ChefPro4HomeTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    var showMoveToInventoryDialog by remember { mutableStateOf(false) }
    var selectedItemToMove by remember { mutableStateOf<ShoppingItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        ShoppingListHeader(
            viewMode = viewMode,
            onViewModeChange = viewModel::updateViewMode,
            onAddItem = { /* Show add item dialog */ },
            onClearCompleted = { viewModel.deleteCompletedItems() }
        )
        
        // Content based on view mode
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            shoppingItems.isEmpty() -> {
                EmptyShoppingListContent()
            }
            else -> {
                ShoppingItemsList(
                    items = shoppingItems,
                    onToggleComplete = { id, isCompleted ->
                        viewModel.toggleItemCompletion(id, isCompleted)
                    },
                    onDelete = { id ->
                        viewModel.deleteShoppingItem(id)
                    },
                    onMoveToInventory = { item ->
                        selectedItemToMove = item
                        showMoveToInventoryDialog = true
                    }
                )
            }
        }
    }
    
    // Move to Inventory Dialog - matches iOS QuickAddToInventoryView
    if (showMoveToInventoryDialog && selectedItemToMove != null) {
        QuickAddToInventoryDialog(
            shoppingItem = selectedItemToMove!!,
            onDismiss = {
                showMoveToInventoryDialog = false
                selectedItemToMove = null
            },
            onConfirm = { inventoryItem ->
                viewModel.moveToInventory(inventoryItem)
                showMoveToInventoryDialog = false
                selectedItemToMove = null
            }
        )
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
fun ShoppingListHeader(
    viewMode: ShoppingListViewModel.ViewMode,
    onViewModeChange: (ShoppingListViewModel.ViewMode) -> Unit,
    onAddItem: () -> Unit,
    onClearCompleted: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Title and Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping List",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = onClearCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear Completed"
                    )
                }
                IconButton(onClick = onAddItem) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Item"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // View mode selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = viewMode == ShoppingListViewModel.ViewMode.ALL,
                    onClick = { onViewModeChange(ShoppingListViewModel.ViewMode.ALL) },
                    label = { Text("All") }
                )
            }
            item {
                FilterChip(
                    selected = viewMode == ShoppingListViewModel.ViewMode.PENDING,
                    onClick = { onViewModeChange(ShoppingListViewModel.ViewMode.PENDING) },
                    label = { Text("Pending") }
                )
            }
            item {
                FilterChip(
                    selected = viewMode == ShoppingListViewModel.ViewMode.COMPLETED,
                    onClick = { onViewModeChange(ShoppingListViewModel.ViewMode.COMPLETED) },
                    label = { Text("Completed") }
                )
            }
        }
    }
}

@Composable
fun ShoppingItemsList(
    items: List<ShoppingItem>,
    onToggleComplete: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onMoveToInventory: (ShoppingItem) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            ShoppingItemRow(
                item = item,
                onToggleComplete = { isCompleted ->
                    onToggleComplete(item.id, isCompleted)
                },
                onDelete = { onDelete(item.id) },
                onMoveToInventory = { onMoveToInventory(item) }
            )
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggleComplete: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onMoveToInventory: (ShoppingItem) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = onToggleComplete
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Item details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                           else MaterialTheme.colorScheme.onSurface
                )
                
                if (item.amount != null || item.unit != null) {
                    Text(
                        text = "${item.amount ?: ""} ${item.unit ?: ""}".trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (item.recipeId != null) {
                    Text(
                        text = "From recipe",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Action buttons (iOS: Move to Inventory + Delete)
            if (!item.isCompleted) {
                // Move to Inventory button - matches iOS
                Button(
                    onClick = { onMoveToInventory(item) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Move to Inventory",
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Move",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyShoppingListContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your shopping list is empty",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add items from recipes or manually add items to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// QuickAddToInventoryDialog - matches iOS QuickAddToInventoryView
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddToInventoryDialog(
    shoppingItem: ShoppingItem,
    onDismiss: () -> Unit,
    onConfirm: (com.chefpro4home.data.model.InventoryItem) -> Unit
) {
    var itemName by remember { mutableStateOf(shoppingItem.name) }
    var quantity by remember { mutableStateOf(shoppingItem.amount ?: "1") }
    var unit by remember { mutableStateOf(shoppingItem.unit ?: "piece") }
    var category by remember { mutableStateOf(determineCategory(shoppingItem.name)) }
    var notes by remember { mutableStateOf("") }
    
    val units = listOf("piece", "pound", "ounce", "gram", "kilogram", "cup", "tablespoon", "teaspoon", 
                      "liter", "milliliter", "gallon", "quart", "pint", "bunch", "head", "can", 
                      "package", "bag", "box", "jar", "bottle")
    val categories = listOf("Refrigerator", "Freezer", "Pantry", "Spices", "Beverages", "Other")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Inventory") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Quantity
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Unit Dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        units.forEach { unitOption ->
                            DropdownMenuItem(
                                text = { Text(unitOption.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    unit = unitOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption) },
                                onClick = {
                                    category = categoryOption
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Notes (Optional)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val inventoryItem = com.chefpro4home.data.model.InventoryItem(
                        id = java.util.UUID.randomUUID().toString(),
                        name = itemName,
                        amount = quantity,
                        unit = unit,
                        category = category,
                        expirationDate = null,
                        barcode = null,
                        createdAt = System.currentTimeMillis().toString()
                    )
                    onConfirm(inventoryItem)
                },
                enabled = itemName.isNotBlank() && quantity.isNotBlank()
            ) {
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to determine category from item name (matches iOS smart defaults)
private fun determineCategory(itemName: String): String {
    val lowercaseName = itemName.lowercase()
    return when {
        lowercaseName.contains("milk") || lowercaseName.contains("cheese") || 
        lowercaseName.contains("yogurt") || lowercaseName.contains("butter") || 
        lowercaseName.contains("cream") || lowercaseName.contains("egg") -> "Refrigerator"
        
        lowercaseName.contains("bread") || lowercaseName.contains("pasta") || 
        lowercaseName.contains("rice") || lowercaseName.contains("flour") || 
        lowercaseName.contains("sugar") || lowercaseName.contains("bean") -> "Pantry"
        
        lowercaseName.contains("meat") || lowercaseName.contains("fish") || 
        lowercaseName.contains("chicken") || lowercaseName.contains("frozen") -> "Freezer"
        
        lowercaseName.contains("pepper") || lowercaseName.contains("cumin") || 
        lowercaseName.contains("oregano") || lowercaseName.contains("spice") -> "Spices"
        
        lowercaseName.contains("juice") || lowercaseName.contains("soda") || 
        lowercaseName.contains("water") || lowercaseName.contains("beer") || 
        lowercaseName.contains("wine") -> "Beverages"
        
        else -> "Pantry" // Default
    }
}

@Preview(showBackground = true)
@Composable
fun ShoppingItemRowPreview() {
    ChefPro4HomeTheme {
        ShoppingItemRow(
            item = ShoppingItem(
                id = "1",
                name = "Black beans",
                amount = "2",
                unit = "cups",
                isCompleted = false,
                recipeId = "recipe_1",
                createdAt = ""
            ),
            onToggleComplete = {},
            onDelete = {},
            onMoveToInventory = {}
        )
    }
}


