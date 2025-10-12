package com.chefpro4home.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chefpro4home.data.model.InventoryItem
import com.chefpro4home.ui.components.LoadingIndicator
import com.chefpro4home.ui.theme.ChefPro4HomeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchText by viewModel.searchText.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        InventoryHeader(
            selectedCategory = selectedCategory,
            categories = categories,
            onCategoryChange = viewModel::updateSelectedCategory,
            onAddItem = { /* Show add item dialog */ },
            onScanBarcode = { /* Show barcode scanner */ }
        )
        
        // Search bar
        SearchBar(
            searchText = searchText,
            onSearchTextChange = viewModel::updateSearchText
        )
        
        // Content
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            inventoryItems.isEmpty() -> {
                EmptyInventoryContent()
            }
            else -> {
                InventoryItemsList(
                    items = inventoryItems,
                    onEdit = { /* Edit item */ },
                    onDelete = { id -> viewModel.deleteInventoryItem(id) }
                )
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
fun InventoryHeader(
    selectedCategory: String?,
    categories: List<String>,
    onCategoryChange: (String?) -> Unit,
    onAddItem: () -> Unit,
    onScanBarcode: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Title and action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventory",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = onScanBarcode) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan Barcode"
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
        
        // Category filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(if (category == selectedCategory) null else category) },
                    label = { Text(category) }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Search inventory...") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun InventoryItemsList(
    items: List<InventoryItem>,
    onEdit: (InventoryItem) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            InventoryItemRow(
                item = item,
                onEdit = { onEdit(item) },
                onDelete = { onDelete(item.id) }
            )
        }
    }
}

@Composable
fun InventoryItemRow(
    item: InventoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            // Item details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${item.amount} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (item.category.isNotEmpty()) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                item.expirationDate?.let { expirationDate ->
                    Text(
                        text = "Expires: ${formatDate(expirationDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Action buttons
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit"
                    )
                }
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
}

@Composable
fun EmptyInventoryContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your inventory is empty",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Add items manually or scan barcodes to track your pantry",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun formatDate(timestamp: String): String {
    return try {
        val time = timestamp.toLong()
        val date = java.util.Date(time)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        "Unknown"
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryItemRowPreview() {
    ChefPro4HomeTheme {
        InventoryItemRow(
            item = InventoryItem(
                id = "1",
                name = "Black beans",
                amount = "2",
                unit = "cups",
                category = "Pantry",
                expirationDate = null,
                barcode = null,
                createdAt = ""
            ),
            onEdit = {},
            onDelete = {}
        )
    }
}


