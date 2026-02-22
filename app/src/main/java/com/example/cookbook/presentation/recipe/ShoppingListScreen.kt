package com.example.cookbook.presentation.recipe

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Shopping List Screen displaying recipe ingredients as a checkable list.
 * Ingredients are received via navigation argument.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    ingredients: List<String>,
    onNavigateBack: () -> Unit
) {
    // Track checked state per ingredient
    var checkedStates by remember {
        mutableStateOf(ingredients.map { false })
    }
    // Track visible items (for clearing checked)
    var visibleIngredients by remember {
        mutableStateOf(ingredients)
    }

    val checkedCount = checkedStates.count { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (checkedCount > 0) {
                        TextButton(onClick = {
                            // Remove checked items
                            val newIngredients = mutableListOf<String>()
                            val newChecked = mutableListOf<Boolean>()
                            visibleIngredients.forEachIndexed { index, ingredient ->
                                if (!checkedStates[index]) {
                                    newIngredients.add(ingredient)
                                    newChecked.add(false)
                                }
                            }
                            visibleIngredients = newIngredients
                            checkedStates = newChecked
                        }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear ($checkedCount)")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (visibleIngredients.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty list",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Done!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You've checked off all the ingredients.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = "${visibleIngredients.size} ingredient${if (visibleIngredients.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                itemsIndexed(visibleIngredients) { index, ingredient ->
                    val isChecked = checkedStates.getOrElse(index) { false }
                    val textColor by animateColorAsState(
                        targetValue = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        label = "text_color"
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        onClick = {
                            checkedStates = checkedStates.toMutableList().also {
                                it[index] = !it[index]
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    checkedStates = checkedStates.toMutableList().also {
                                        it[index] = checked
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ingredient,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                textDecoration = if (isChecked) TextDecoration.LineThrough
                                else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
