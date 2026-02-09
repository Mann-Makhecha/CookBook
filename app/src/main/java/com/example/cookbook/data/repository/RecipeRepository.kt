package com.example.cookbook.data.repository

import com.example.cookbook.data.model.Recipe
import com.example.cookbook.util.Constants
import com.example.cookbook.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling recipe-related Firestore operations.
 * Manages CRUD operations, search, and filtering for recipes.
 */
class RecipeRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Get all recipes, ordered by creation date (newest first).
     */
    fun getAllRecipes(): Flow<Result<List<Recipe>>> = callbackFlow {
        val listener = firestore.collection(Constants.RECIPES_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(Exception(error)))
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Recipe.fromMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(Result.Success(recipes))
            }

        trySend(Result.Loading)

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Get a recipe by its ID.
     */
    fun getRecipeById(recipeId: String): Flow<Result<Recipe>> = flow {
        try {
            emit(Result.Loading)

            val document = firestore.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .get()
                .await()

            if (document.exists()) {
                val recipe = Recipe.fromMap(document.data ?: emptyMap())
                emit(Result.Success(recipe))
            } else {
                emit(Result.Error(Exception("Recipe not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Get recipes by category.
     */
    fun getRecipesByCategory(category: String): Flow<Result<List<Recipe>>> = callbackFlow {
        val listener = firestore.collection(Constants.RECIPES_COLLECTION)
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(Exception(error)))
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Recipe.fromMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(Result.Success(recipes))
            }

        trySend(Result.Loading)

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Get recipes created by a specific user.
     */
    fun getRecipesByUser(userId: String): Flow<Result<List<Recipe>>> = callbackFlow {
        val listener = firestore.collection(Constants.RECIPES_COLLECTION)
            .whereEqualTo("createdBy", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(Exception(error)))
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Recipe.fromMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(Result.Success(recipes))
            }

        trySend(Result.Loading)

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Search recipes by name (case-insensitive contains).
     * Note: Firestore doesn't support full-text search, so this fetches all recipes
     * and filters client-side. For production, consider using Algolia or similar.
     */
    fun searchRecipes(query: String): Flow<Result<List<Recipe>>> = flow {
        try {
            emit(Result.Loading)

            val snapshot = firestore.collection(Constants.RECIPES_COLLECTION)
                .get()
                .await()

            val recipes = snapshot.documents.mapNotNull { doc ->
                try {
                    Recipe.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                    null
                }
            }.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true) ||
                        recipe.description.contains(query, ignoreCase = true)
            }

            emit(Result.Success(recipes))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Get multiple recipes by their IDs (used for favorites).
     */
    fun getRecipesByIds(recipeIds: List<String>): Flow<Result<List<Recipe>>> = flow {
        try {
            emit(Result.Loading)

            if (recipeIds.isEmpty()) {
                emit(Result.Success(emptyList()))
                return@flow
            }

            // Firestore 'in' query supports max 10 items
            val recipes = mutableListOf<Recipe>()
            recipeIds.chunked(10).forEach { chunk ->
                val snapshot = firestore.collection(Constants.RECIPES_COLLECTION)
                    .whereIn("recipeId", chunk)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { doc ->
                    try {
                        Recipe.fromMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                }.let { recipes.addAll(it) }
            }

            emit(Result.Success(recipes))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Add a new recipe to Firestore.
     */
    fun addRecipe(recipe: Recipe): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)

            // Generate a unique ID for the recipe
            val recipeId = firestore.collection(Constants.RECIPES_COLLECTION)
                .document()
                .id

            val recipeWithId = recipe.copy(recipeId = recipeId)

            firestore.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .set(recipeWithId.toMap())
                .await()

            emit(Result.Success(recipeId))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Update an existing recipe.
     */
    fun updateRecipe(recipe: Recipe): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            firestore.collection(Constants.RECIPES_COLLECTION)
                .document(recipe.recipeId)
                .set(recipe.toMap())
                .await()

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Delete a recipe.
     */
    fun deleteRecipe(recipeId: String): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            firestore.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .delete()
                .await()

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
