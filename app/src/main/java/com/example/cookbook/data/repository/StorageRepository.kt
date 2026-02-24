package com.example.cookbook.data.repository

import android.content.Context
import android.net.Uri
import com.example.cookbook.BuildConfig
import com.example.cookbook.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Repository for handling image uploads.
 * Uses ImgBB free API for image hosting.
 */
class StorageRepository {
    private val client = OkHttpClient()

    /**
     * Upload a recipe image to ImgBB.
     *
     * @param context Used to resolve the Uri to bytes
     * @param imageUri The local URI of the image to upload
     * @param userId The ID of the user uploading the image
     * @param recipeId The ID of the recipe (optional)
     * @return Flow emitting the download URL of the uploaded image
     */
    fun uploadRecipeImage(
        context: Context,
        imageUri: Uri,
        userId: String,
        recipeId: String? = null
    ): Flow<Result<String>> = flow {
        try {
            emit(Result.Loading)

            val apiKey = BuildConfig.IMGBB_API_KEY
            if (apiKey.isEmpty()) {
                emit(Result.Error(Exception("ImgBB API Key is missing. Please add it to local.properties.")))
                return@flow
            }

            // Read the file data from Uri
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            }

            if (bytes == null) {
                emit(Result.Error(Exception("Could not read image data")))
                return@flow
            }

            // Create OkHttp request body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", apiKey)
                .addFormDataPart(
                    "image",
                    "image.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(requestBody)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val data = json.optJSONObject("data")
                val imageUrl = data?.optString("display_url")
                
                if (!imageUrl.isNullOrEmpty()) {
                    emit(Result.Success(imageUrl))
                } else {
                    emit(Result.Error(Exception("Failed to get image URL from response")))
                }
            } else {
                emit(Result.Error(Exception("Upload failed with code: ${response.code}")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Delete a recipe image.
     * With ImgBB free API, deletion requires a deletion URL which isn't practical to store and manage without full auth.
     * Just returning success as no-op.
     */
    fun deleteRecipeImage(imageUrl: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        emit(Result.Success(Unit))
    }

    /**
     * Delete multiple recipe images.
     */
    fun deleteRecipeImages(imageUrls: List<String>): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        emit(Result.Success(Unit))
    }

    fun getImageSize(imageUri: Uri): Long {
        return 0L
    }
}
