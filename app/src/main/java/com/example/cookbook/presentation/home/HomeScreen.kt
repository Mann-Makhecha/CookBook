package com.example.cookbook.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cookbook.presentation.components.ErrorView
import com.example.cookbook.presentation.components.RecipeCard
import com.example.cookbook.presentation.recipe.RecipeViewModel
import com.example.cookbook.util.Constants
import com.example.cookbook.util.Result

/**
 * Home Screen displaying all recipes with category filtering.
 * Main landing screen after authentication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    val recipesState by viewModel.recipesState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CookBook",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedCategory ?: "All Recipes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorites")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRecipeClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Recipe") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    if (category == null) {
                        viewModel.clearCategoryFilter()
                    } else {
                        viewModel.loadRecipesByCategory(category)
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Recipe List
            when (recipesState) {
                is Result.Loading, Result.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Result.Success -> {
                    val recipes = (recipesState as Result.Success).data

                    if (recipes.isEmpty()) {
                        EmptyRecipesView(
                            onAddRecipeClick = onAddRecipeClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(recipes, key = { it.recipeId }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.recipeId) }
                                )
                            }

                            // Bottom spacing for FAB
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }

                is Result.Error -> {
                    ErrorView(
                        message = (recipesState as Result.Error).exception.message
                            ?: "Failed to load recipes",
                        onRetry = {
                            selectedCategory?.let { viewModel.loadRecipesByCategory(it) }
                                ?: viewModel.loadAllRecipes()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Horizontal scrollable row of category filter chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterRow(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }

        // Category chips
        items(Constants.RECIPE_CATEGORIES) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                leadingIcon = if (selectedCategory == category) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

/**
 * Empty state view when no recipes are available.
 */
@Composable
fun EmptyRecipesView(
    onAddRecipeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Vegetarian placeholder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(96.dp)
                            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Recipes Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start building your recipe collection by adding your first recipe!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddRecipeClick,
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Your First Recipe")
            }
        }
    }
}

