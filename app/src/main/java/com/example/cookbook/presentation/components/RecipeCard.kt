package com.example.cookbook.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cookbook.data.model.Recipe
import com.example.cookbook.presentation.components.RatingBar

/**
 * Recipe Card component for displaying recipe preview in lists.
 * Shows image, name, category, cooking time, and difficulty.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Recipe Image
            if (recipe.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            } else {
                // Placeholder when no image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
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

            // Recipe Info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (recipe.reviewCount > 0) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RatingBar(
                            rating = recipe.averageRating,
                            starSize = 16.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${recipe.reviewCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Description
                if (recipe.description.isNotEmpty()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Category, Time, and Difficulty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Chip
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = recipe.category,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Category",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cooking Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Time",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = recipe.cookingTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Difficulty Badge
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = recipe.difficulty,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = when (recipe.difficulty) {
                                    "Easy" -> MaterialTheme.colorScheme.primaryContainer
                                    "Medium" -> MaterialTheme.colorScheme.secondaryContainer
                                    "Hard" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                labelColor = when (recipe.difficulty) {
                                    "Easy" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    "Medium" -> MaterialTheme.colorScheme.onSecondaryContainer
                                    "Hard" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact Recipe Card for smaller displays or grid layouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Recipe Image
            if (recipe.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Vegetarian placeholder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(96.dp)
                            )
                }
            }

            // Recipe Info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (recipe.reviewCount > 0) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RatingBar(
                            rating = recipe.averageRating,
                            starSize = 12.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${recipe.averageRating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = recipe.cookingTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
