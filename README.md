# Chef Pro 4 Home - Android App

This is the Android version of the Chef Pro 4 Home recipe app, converted from the original iOS SwiftUI implementation.

## Features

- **Recipes**: Browse and search family recipes from Cuban, German, and Tex-Mex traditions
- **Shopping List**: Generate shopping lists from recipes and manage items
- **What 2 Cook**: AI-powered recipe suggestions based on available ingredients
- **Inventory**: Track pantry items with barcode scanning support

## Architecture

- **MVVM Pattern**: Model-View-ViewModel architecture
- **Jetpack Compose**: Modern declarative UI framework
- **Room Database**: Local data persistence
- **Retrofit**: Network API communication
- **Hilt**: Dependency injection
- **Navigation Component**: Type-safe navigation

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Database**: Room
- **Network**: Retrofit + OkHttp
- **DI**: Hilt
- **Image Loading**: Coil
- **Navigation**: Navigation Compose

## Project Structure

```
app/
├── src/main/java/com/chefpro4home/
│   ├── data/
│   │   ├── api/           # Retrofit API interfaces
│   │   ├── database/      # Room database
│   │   ├── repository/    # Data repositories
│   │   └── model/         # Data models
│   ├── ui/
│   │   ├── recipes/       # Recipe screens
│   │   ├── shopping/      # Shopping list screens
│   │   ├── what2cook/     # What to cook screens
│   │   ├── inventory/     # Inventory screens
│   │   ├── components/    # Reusable UI components
│   │   └── theme/         # App theming
│   ├── util/              # Utilities and extensions
│   ├── di/                # Dependency injection modules
│   └── MainActivity.kt    # Main activity
```

## Setup

1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Build and run

## Backend Integration

The app connects to the existing Cloudflare Workers API:
- Base URL: `https://recipes-api.recipedos.workers.dev/`
- Endpoints: `/api/recipes`, `/api/health`, `/api/images`

## Key Components

### Data Layer
- **RecipeDatabase**: Room database with all entities
- **RecipesRepository**: Centralized data access
- **RecipesApiService**: Retrofit API client

### UI Layer
- **MainScreen**: Bottom navigation with 4 tabs
- **RecipesScreen**: Recipe grid with search and filters
- **ShoppingListScreen**: Shopping list management
- **What2CookScreen**: Recipe search and suggestions
- **InventoryScreen**: Inventory management with barcode scanning

### ViewModels
- **RecipesViewModel**: Recipe data and search logic
- **ShoppingListViewModel**: Shopping list operations
- **What2CookViewModel**: Recipe search functionality
- **InventoryViewModel**: Inventory management

## Features Implementation

### Recipe Search Service
Equivalent to iOS `RecipeSearchService.swift`:
- Multi-source recipe search (local, Spoonacular, Edamam)
- HTML tag cleaning
- Quick action methods
- Diet type filtering

### Database Models
Room entities matching iOS SwiftData models:
- Recipe, Ingredient, Instruction, Tag
- ShoppingItem, InventoryItem, Event
- Proper relationships and constraints

### API Integration
Retrofit service matching iOS APIClient:
- Same endpoints and response models
- Error handling and timeouts
- Image upload support

## Development Notes

- Uses the same backend API as iOS app
- Maintains feature parity with iOS version
- Follows Android design guidelines
- Implements proper error handling
- Supports offline functionality with Room database

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## License

Same as the original iOS project.