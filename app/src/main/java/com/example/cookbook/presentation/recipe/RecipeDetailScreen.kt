package com.example.cookbook.presentation.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cookbook.data.repository.AuthRepository
import com.example.cookbook.presentation.components.AddReviewDialog
import com.example.cookbook.presentation.components.ErrorView
import com.example.cookbook.presentation.components.RatingBar
import com.example.cookbook.presentation.components.ReviewItem
import com.example.cookbook.util.Result

/**
 * Recipe Detail Screen showing full recipe information.
 * Includes image, ingredients, steps, timer button, and favorite toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onTimerClick: (Int) -> Unit,
    onShoppingListClick: (List<String>) -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    val recipeState by viewModel.recipeState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val authRepository = remember { AuthRepository() }
    val currentUserId = authRepository.getCurrentUserId()

    var showDeleteDialog by remember { mutableStateOf(false) }
    val deleteState by viewModel.deleteRecipeState.collectAsState()

    var showAddReviewDialog by remember { mutableStateOf(false) }
    val reviewsState by viewModel.reviewsState.collectAsState()
    val saveReviewState by viewModel.saveReviewState.collectAsState()

    // Load recipe on first composition
    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    // Handle delete success
    LaunchedEffect(deleteState) {
        if (deleteState is Result.Success) {
            viewModel.clearDeleteRecipeState()
            onNavigateBack()
        }
    }

    // Handle add review success (No longer needed to close dialog here)
    LaunchedEffect(saveReviewState) {
        if (saveReviewState is Result.Success) {
            viewModel.clearSaveReviewState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show edit/delete only if user owns the recipe
                    if (recipeState is Result.Success) {
                        val recipe = (recipeState as Result.Success).data
                        if (recipe.createdBy == currentUserId) {
                            IconButton(onClick = { onEditClick(recipeId) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (recipeState) {
            is Result.Loading, Result.Idle, null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Result.Success -> {
                val recipe = (recipeState as Result.Success).data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Recipe Image
                    if (recipe.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = recipe.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Vegetarian placeholder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(96.dp)
                            )
                        }
                    }

                    // Recipe Info Section
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Title and Favorite Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recipe.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (recipe.reviewCount > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        RatingBar(
                                            rating = recipe.averageRating,
                                            starSize = 18.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${recipe.averageRating} (${recipe.reviewCount} reviews)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.toggleFavorite(recipeId) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        if (recipe.description.isNotEmpty()) {
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Category, Time, Difficulty Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { },
                                label = { Text(recipe.category) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            AssistChip(
                                onClick = { },
                                label = { Text(recipe.cookingTime) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            AssistChip(
                                onClick = { },
                                label = { Text(recipe.difficulty) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Extract minutes from cooking time string
                                    val minutes = recipe.cookingTime.filter { it.isDigit() }.toIntOrNull() ?: 30
                                    onTimerClick(minutes)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Timer")
                            }

                            OutlinedButton(
                                onClick = { onShoppingListClick(recipe.ingredients) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Shopping List")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Ingredients Section
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        recipe.ingredients.forEachIndexed { index, ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(8.dp)
                                        .padding(top = 6.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Steps Section
                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Instructions Section
                        // ... existing steps implementation ...
                        recipe.steps.forEachIndexed { index, step ->
                            // ... existing step card ...
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Reviews Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reviews",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            TextButton(onClick = { showAddReviewDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Review")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (reviewsState) {
                            is Result.Loading, Result.Idle -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                            is Result.Success -> {
                                val reviews = (reviewsState as Result.Success).data
                                if (reviews.isEmpty()) {
                                    Text(
                                        text = "No reviews yet. Be the first to rate this recipe!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                } else {
                                    reviews.forEach { review ->
                                        ReviewItem(review = review)
                                    }
                                }
                            }
                            is Result.Error -> {
                                Text(
                                    text = "Failed to load reviews",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }

                        // Bottom spacing
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            is Result.Error -> {
                ErrorView(
                    message = (recipeState as Result.Error).exception.message
                        ?: "Failed to load recipe",
                    onRetry = { viewModel.loadRecipe(recipeId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe?") },
            text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (recipeState is Result.Success) {
                            viewModel.deleteRecipe((recipeState as Result.Success).data)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Review Dialog
    if (showAddReviewDialog) {
        AddReviewDialog(
            onDismiss = { 
                showAddReviewDialog = false
                viewModel.clearSaveReviewState()
            },
            onSubmit = { rating, comment ->
                showAddReviewDialog = false
                viewModel.submitReview(recipeId, rating, comment)
            }
        )
    }

    // Show error if review submission fails
    if (saveReviewState is Result.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSaveReviewState() },
            title = { Text("Error") },
            text = { Text((saveReviewState as Result.Error).exception.message ?: "Failed to submit review") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSaveReviewState() }) {
                    Text("OK")
                }
            }
        )
    }
}
