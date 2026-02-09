package com.example.cookbook.data.repository

import android.net.Uri
import com.example.cookbook.util.Constants
import com.example.cookbook.util.Result
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository for handling Firebase Storage operations.
 * Manages image uploads and deletions for recipe images.
 */
class StorageRepository {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Upload a recipe image to Firebase Storage.
     *
     * @param imageUri The local URI of the image to upload
     * @param userId The ID of the user uploading the image
     * @param recipeId The ID of the recipe (optional, generates one if not provided)
     * @return Flow emitting the download URL of the uploaded image
     */
    fun uploadRecipeImage(
        imageUri: Uri,
        userId: String,
        recipeId: String? = null
    ): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)

            // Generate a unique filename
            val fileName = recipeId ?: UUID.randomUUID().toString()
            val imageRef = storageRef
                .child("${Constants.RECIPE_IMAGES_PATH}/$userId/$fileName.jpg")

            // Upload the file
            val uploadTask = imageRef.putFile(imageUri)
            uploadTask.await()

            // Get the download URL
            val downloadUrl = imageRef.downloadUrl.await()

            emit(Result.Success(downloadUrl.toString()))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Delete a recipe image from Firebase Storage using its URL.
     *
     * @param imageUrl The download URL of the image to delete
     */
    fun deleteRecipeImage(imageUrl: String): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            if (imageUrl.isEmpty()) {
                emit(Result.Success(Unit))
                return@flow
            }

            // Get reference from URL
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            // Even if deletion fails, we consider it a success
            // to avoid blocking the user
            emit(Result.Success(Unit))
        }
    }

    /**
     * Delete multiple recipe images.
     *
     * @param imageUrls List of image URLs to delete
     */
    fun deleteRecipeImages(imageUrls: List<String>): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            imageUrls.forEach { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    try {
                        val imageRef = storage.getReferenceFromUrl(imageUrl)
                        imageRef.delete().await()
                    } catch (e: Exception) {
                        // Continue deleting other images even if one fails
                    }
                }
            }

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Get the file size of an image before uploading.
     * Useful for validating file size limits.
     */
    fun getImageSize(imageUri: Uri): Long {
        // This would need to be implemented with ContentResolver
        // For now, return 0 and handle validation on upload
        return 0L
    }
}
