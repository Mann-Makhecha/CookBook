package com.example.cookbook.presentation.recipe

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cookbook.data.model.Recipe
import com.example.cookbook.data.repository.AuthRepository
import com.example.cookbook.data.repository.RecipeRepository
import com.example.cookbook.data.repository.StorageRepository
import com.example.cookbook.data.repository.UserRepository
import com.example.cookbook.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing recipe-related operations.
 * Handles recipe CRUD, favorites, and image uploads.
 */
class RecipeViewModel : ViewModel() {
    private val recipeRepository = RecipeRepository()
    private val storageRepository = StorageRepository()
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    // All recipes
    private val _recipesState = MutableStateFlow<Result<List<Recipe>>>(Result.Loading)
    val recipesState: StateFlow<Result<List<Recipe>>> = _recipesState.asStateFlow()

    // Single recipe
    private val _recipeState = MutableStateFlow<Result<Recipe>?>(null)
    val recipeState: StateFlow<Result<Recipe>?> = _recipeState.asStateFlow()

    // Add/Update recipe state
    private val _saveRecipeState = MutableStateFlow<Result<String>?>(null)
    val saveRecipeState: StateFlow<Result<String>?> = _saveRecipeState.asStateFlow()

    // Delete recipe state
    private val _deleteRecipeState = MutableStateFlow<Result<Unit>?>(null)
    val deleteRecipeState: StateFlow<Result<Unit>?> = _deleteRecipeState.asStateFlow()

    // User's recipes
    private val _userRecipesState = MutableStateFlow<Result<List<Recipe>>>(Result.Loading)
    val userRecipesState: StateFlow<Result<List<Recipe>>> = _userRecipesState.asStateFlow()

    // Favorite recipes
    private val _favoriteRecipesState = MutableStateFlow<Result<List<Recipe>>>(Result.Loading)
    val favoriteRecipesState: StateFlow<Result<List<Recipe>>> = _favoriteRecipesState.asStateFlow()

    // Favorite status for current recipe
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Selected category filter
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadAllRecipes()
    }

    /**
     * Load all recipes from Firestore.
     */
    fun loadAllRecipes() {
        viewModelScope.launch {
            recipeRepository.getAllRecipes()
                .collect { result ->
                    _recipesState.value = result
                }
        }
    }

    /**
     * Load recipes by category.
     */
    fun loadRecipesByCategory(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            recipeRepository.getRecipesByCategory(category)
                .collect { result ->
                    _recipesState.value = result
                }
        }
    }

    /**
     * Clear category filter and load all recipes.
     */
    fun clearCategoryFilter() {
        _selectedCategory.value = null
        loadAllRecipes()
    }

    /**
     * Load a single recipe by ID.
     */
    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            recipeRepository.getRecipeById(recipeId)
                .collect { result ->
                    _recipeState.value = result

                    // Check if recipe is favorited
                    if (result is Result.Success) {
                        checkIfFavorite(recipeId)
                    }
                }
        }
    }

    /**
     * Load recipes created by the current user.
     */
    fun loadUserRecipes() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            recipeRepository.getRecipesByUser(userId)
                .collect { result ->
                    _userRecipesState.value = result
                }
        }
    }

    /**
     * Load user's favorite recipes.
     */
    fun loadFavoriteRecipes() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.getFavoriteRecipeIds(userId)
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            if (result.data.isEmpty()) {
                                _favoriteRecipesState.value = Result.Success(emptyList())
                            } else {
                                recipeRepository.getRecipesByIds(result.data)
                                    .collect { recipesResult ->
                                        _favoriteRecipesState.value = recipesResult
                                    }
                            }
                        }
                        is Result.Error -> _favoriteRecipesState.value = result
                        is Result.Loading -> _favoriteRecipesState.value = result
                        is Result.Idle -> {}
                    }
                }
        }
    }

    /**
     * Search recipes by query.
     */
    fun searchRecipes(query: String) {
        viewModelScope.launch {
            recipeRepository.searchRecipes(query)
                .collect { result ->
                    _recipesState.value = result
                }
        }
    }

    /**
     * Add a new recipe with optional image upload.
     */
    fun addRecipe(recipe: Recipe, imageUri: Uri?) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _saveRecipeState.value = Result.Loading

            try {
                // Upload image if provided
                val imageUrl = if (imageUri != null) {
                    var uploadedUrl = ""
                    storageRepository.uploadRecipeImage(imageUri, userId)
                        .collect { result ->
                            when (result) {
                                is Result.Success -> uploadedUrl = result.data
                                is Result.Error -> {
                                    _saveRecipeState.value = result
                                    return@collect
                                }
                                is Result.Loading -> {}
                                is Result.Idle -> {}
                            }
                        }
                    uploadedUrl
                } else {
                    ""
                }

                // Create recipe with image URL
                val recipeWithImage = recipe.copy(
                    imageUrl = imageUrl,
                    createdBy = userId
                )

                // Save recipe to Firestore
                recipeRepository.addRecipe(recipeWithImage)
                    .collect { result ->
                        _saveRecipeState.value = result
                    }
            } catch (e: Exception) {
                _saveRecipeState.value = Result.Error(e)
            }
        }
    }

    /**
     * Update an existing recipe.
     */
    fun updateRecipe(recipe: Recipe, newImageUri: Uri?) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _saveRecipeState.value = Result.Loading

            try {
                // Upload new image if provided
                val imageUrl = if (newImageUri != null) {
                    // Delete old image if exists
                    if (recipe.imageUrl.isNotEmpty()) {
                        storageRepository.deleteRecipeImage(recipe.imageUrl)
                            .collect { /* Ignore result */ }
                    }

                    // Upload new image
                    var uploadedUrl = recipe.imageUrl
                    storageRepository.uploadRecipeImage(newImageUri, userId, recipe.recipeId)
                        .collect { result ->
                            when (result) {
                                is Result.Success -> uploadedUrl = result.data
                                is Result.Error -> {
                                    _saveRecipeState.value = result
                                    return@collect
                                }
                                is Result.Loading -> {}
                                is Result.Idle -> {}
                            }
                        }
                    uploadedUrl
                } else {
                    recipe.imageUrl
                }

                // Update recipe with new image URL
                val updatedRecipe = recipe.copy(imageUrl = imageUrl)

                recipeRepository.updateRecipe(updatedRecipe)
                    .collect { result ->
                        when (result) {
                            is Result.Success -> _saveRecipeState.value = Result.Success(recipe.recipeId)
                            is Result.Error -> _saveRecipeState.value = result
                            is Result.Loading -> {}
                            is Result.Idle -> {}
                        }
                    }
            } catch (e: Exception) {
                _saveRecipeState.value = Result.Error(e)
            }
        }
    }

    /**
     * Delete a recipe and its image.
     */
    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _deleteRecipeState.value = Result.Loading

            try {
                // Delete image if exists
                if (recipe.imageUrl.isNotEmpty()) {
                    storageRepository.deleteRecipeImage(recipe.imageUrl)
                        .collect { /* Ignore result */ }
                }

                // Delete recipe from Firestore
                recipeRepository.deleteRecipe(recipe.recipeId)
                    .collect { result ->
                        _deleteRecipeState.value = result
                    }
            } catch (e: Exception) {
                _deleteRecipeState.value = Result.Error(e)
            }
        }
    }

    /**
     * Toggle favorite status for a recipe.
     */
    fun toggleFavorite(recipeId: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            if (_isFavorite.value) {
                userRepository.removeFromFavorites(userId, recipeId)
                    .collect { result ->
                        if (result is Result.Success) {
                            _isFavorite.value = false
                        }
                    }
            } else {
                userRepository.addToFavorites(userId, recipeId)
                    .collect { result ->
                        if (result is Result.Success) {
                            _isFavorite.value = true
                        }
                    }
            }
        }
    }

    /**
     * Check if a recipe is in the user's favorites.
     */
    private fun checkIfFavorite(recipeId: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            userRepository.isRecipeFavorite(userId, recipeId)
                .collect { result ->
                    if (result is Result.Success) {
                        _isFavorite.value = result.data
                    }
                }
        }
    }

    /**
     * Clear save recipe state.
     */
    fun clearSaveRecipeState() {
        _saveRecipeState.value = null
    }

    /**
     * Clear delete recipe state.
     */
    fun clearDeleteRecipeState() {
        _deleteRecipeState.value = null
    }

    /**
     * Clear recipe state.
     */
    fun clearRecipeState() {
        _recipeState.value = null
    }
}
