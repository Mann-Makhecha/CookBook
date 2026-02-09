package com.example.cookbook.data.repository

import com.example.cookbook.data.model.User
import com.example.cookbook.util.Constants
import com.example.cookbook.util.Result
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling user-related Firestore operations.
 * Manages user profiles and favorites.
 */
class UserRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Get a user by their ID.
     */
    fun getUserById(userId: String): Flow<Result<User>> = flow {
        try {
            emit(Result.Loading)

            val document = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = User.fromMap(document.data ?: emptyMap())
                emit(Result.Success(user))
            } else {
                emit(Result.Error(Exception("User not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Update user profile information.
     */
    fun updateUserProfile(userId: String, name: String): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("name", name)
                .await()

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Add a recipe to the user's favorites.
     */
    fun addToFavorites(userId: String, recipeId: String): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("favorites", FieldValue.arrayUnion(recipeId))
                .await()

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Remove a recipe from the user's favorites.
     */
    fun removeFromFavorites(userId: String, recipeId: String): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .update("favorites", FieldValue.arrayRemove(recipeId))
                .await()

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Check if a recipe is in the user's favorites.
     */
    fun isRecipeFavorite(userId: String, recipeId: String): Flow<Result<Boolean>> = flow {
        try {
            emit(Result.Loading)

            val document = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = User.fromMap(document.data ?: emptyMap())
                val isFavorite = user.favorites.contains(recipeId)
                emit(Result.Success(isFavorite))
            } else {
                emit(Result.Error(Exception("User not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Get all favorite recipe IDs for a user.
     */
    fun getFavoriteRecipeIds(userId: String): Flow<Result<List<String>>> = flow {
        try {
            emit(Result.Loading)

            val document = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = User.fromMap(document.data ?: emptyMap())
                emit(Result.Success(user.favorites))
            } else {
                emit(Result.Error(Exception("User not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
