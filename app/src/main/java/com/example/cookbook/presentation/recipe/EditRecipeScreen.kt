package com.example.cookbook.presentation.recipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.cookbook.data.model.Recipe
import com.example.cookbook.util.Constants
import com.example.cookbook.util.Result

/**
 * Edit Recipe Screen for modifying existing recipes.
 * Pre-populates form with existing recipe data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    onRecipeUpdated: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Constants.RECIPE_CATEGORIES[0]) }
    var cookingTimeAmount by remember { mutableStateOf("") }
    var selectedTimeUnit by remember { mutableStateOf("min") }
    val timeUnits = listOf("min", "hours")
    var showTimeUnitMenu by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(Constants.RECIPE_DIFFICULTIES[0]) }
    var ingredients by remember { mutableStateOf(mutableListOf<String>()) }
    var steps by remember { mutableStateOf(mutableListOf<String>()) }
    var currentImageUrl by remember { mutableStateOf("") }
    var selectedNewImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDifficultyMenu by remember { mutableStateOf(false) }

    val recipeState by viewModel.recipeState.collectAsState()
    val saveRecipeState by viewModel.saveRecipeState.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedNewImageUri = uri
    }

    // Load recipe data
    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    // Populate form when recipe loads
    LaunchedEffect(recipeState) {
        if (recipeState is Result.Success && !isLoaded) {
            val recipe = (recipeState as Result.Success).data
            recipeName = recipe.name
            description = recipe.description
            selectedCategory = recipe.category
            cookingTimeAmount = recipe.cookingTime.filter { it.isDigit() }
            selectedTimeUnit = if (recipe.cookingTime.contains("hour", ignoreCase = true)) "hours" else "min"
            selectedDifficulty = recipe.difficulty
            ingredients = recipe.ingredients.toMutableList()
            steps = recipe.steps.toMutableList()
            currentImageUrl = recipe.imageUrl
            isLoaded = true
        }
    }

    // Handle successful recipe update
    LaunchedEffect(saveRecipeState) {
        if (saveRecipeState is Result.Success) {
            viewModel.clearSaveRecipeState()
            onRecipeUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (recipeName.isNotBlank() &&
                                cookingTimeAmount.isNotBlank() &&
                                cookingTimeAmount.toIntOrNull() != null &&
                                ingredients.any { it.isNotBlank() } &&
                                steps.any { it.isNotBlank() } &&
                                recipeState is Result.Success) {

                                val originalRecipe = (recipeState as Result.Success).data
                                val updatedRecipe = originalRecipe.copy(
                                    name = recipeName,
                                    description = description,
                                    category = selectedCategory,
                                    cookingTime = "$cookingTimeAmount $selectedTimeUnit",
                                    difficulty = selectedDifficulty,
                                    ingredients = ingredients.filter { it.isNotBlank() },
                                    steps = steps.filter { it.isNotBlank() }
                                )

                                viewModel.updateRecipe(updatedRecipe, selectedNewImageUri)
                            }
                        },
                        enabled = saveRecipeState !is Result.Loading &&
                                  recipeName.isNotBlank() &&
                                  cookingTimeAmount.isNotBlank() &&
                                  cookingTimeAmount.toIntOrNull() != null &&
                                  ingredients.any { it.isNotBlank() } &&
                                  steps.any { it.isNotBlank() }
                    ) {
                        if (saveRecipeState is Result.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            recipeState is Result.Loading && !isLoaded -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            recipeState is Result.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load recipe",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadRecipe(recipeId) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error Message
                    if (saveRecipeState is Result.Error) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = (saveRecipeState as Result.Error).exception.message
                                    ?: "Failed to update recipe",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // Image Picker
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                selectedNewImageUri != null -> {
                                    AsyncImage(
                                        model = selectedNewImageUri,
                                        contentDescription = "New image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                currentImageUrl.isNotEmpty() -> {
                                    AsyncImage(
                                        model = currentImageUrl,
                                        contentDescription = "Current image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                else -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Add photo",
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Change Recipe Photo",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recipe Name
                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        label = { Text("Recipe Name *") },
                        placeholder = { Text("e.g., Chocolate Chip Cookies") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("Brief description of your recipe...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showCategoryMenu,
                        onExpandedChange = { showCategoryMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            Constants.RECIPE_CATEGORIES.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cooking Time Amount
                OutlinedTextField(
                    value = cookingTimeAmount,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) cookingTimeAmount = it },
                    label = { Text("Time") },
                    placeholder = { Text("e.g., 30") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.3f)
                )

                // Cooking Time Unit Dropdown
                ExposedDropdownMenuBox(
                    expanded = showTimeUnitMenu,
                    onExpandedChange = { showTimeUnitMenu = it },
                    modifier = Modifier.weight(0.35f)
                ) {
                    OutlinedTextField(
                        value = selectedTimeUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimeUnitMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showTimeUnitMenu,
                        onDismissRequest = { showTimeUnitMenu = false }
                    ) {
                        timeUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    selectedTimeUnit = unit
                                    showTimeUnitMenu = false
                                }
                            )
                        }
                    }
                }

                // Difficulty Dropdown
                ExposedDropdownMenuBox(
                    expanded = showDifficultyMenu,
                    onExpandedChange = { showDifficultyMenu = it },
                    modifier = Modifier.weight(0.35f)
                        ) {
                            OutlinedTextField(
                                value = selectedDifficulty,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Difficulty") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDifficultyMenu)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showDifficultyMenu,
                                onDismissRequest = { showDifficultyMenu = false }
                            ) {
                                Constants.RECIPE_DIFFICULTIES.forEach { difficulty ->
                                    DropdownMenuItem(
                                        text = { Text(difficulty) },
                                        onClick = {
                                            selectedDifficulty = difficulty
                                            showDifficultyMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Divider()

                    // Ingredients Section
                    Text(
                        text = "Ingredients *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ingredients.forEachIndexed { index, ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = ingredient,
                                onValueChange = { newValue ->
                                    ingredients = ingredients.toMutableList().apply {
                                        this[index] = newValue
                                    }
                                },
                                label = { Text("Ingredient ${index + 1}") },
                                placeholder = { Text("e.g., 2 cups flour") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            if (ingredients.size > 1) {
                                IconButton(
                                    onClick = {
                                        ingredients = ingredients.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Remove")
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            ingredients = ingredients.toMutableList().apply { add("") }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Ingredient")
                    }

                    Divider()

                    // Steps Section
                    Text(
                        text = "Instructions *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            OutlinedTextField(
                                value = step,
                                onValueChange = { newValue ->
                                    steps = steps.toMutableList().apply {
                                        this[index] = newValue
                                    }
                                },
                                label = { Text("Step ${index + 1}") },
                                placeholder = { Text("Describe this step...") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.weight(1f)
                            )

                            if (steps.size > 1) {
                                IconButton(
                                    onClick = {
                                        steps = steps.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Remove")
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            steps = steps.toMutableList().apply { add("") }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Step")
                    }

                    // Bottom spacing
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
