package com.example.cookbook.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Recipe data model representing a recipe in the CookBook app.
 * Mapped to Firestore 'recipes' collection.
 */
data class Recipe(
    val recipeId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val cookingTime: String = "", // e.g., "30 minutes"
    val difficulty: String = "", // Easy, Medium, Hard
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val imageUrl: String = "",
    val createdBy: String = "", // User ID
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    /**
     * Convert Recipe to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "recipeId" to recipeId,
            "name" to name,
            "description" to description,
            "category" to category,
            "cookingTime" to cookingTime,
            "difficulty" to difficulty,
            "ingredients" to ingredients,
            "steps" to steps,
            "imageUrl" to imageUrl,
            "createdBy" to createdBy,
            "createdAt" to createdAt
        )
    }

    companion object {
        /**
         * Create a Recipe from Firestore document data.
         */
        fun fromMap(map: Map<String, Any>): Recipe {
            return Recipe(
                recipeId = map["recipeId"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = map["category"] as? String ?: "",
                cookingTime = map["cookingTime"] as? String ?: "",
                difficulty = map["difficulty"] as? String ?: "",
                ingredients = (map["ingredients"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                steps = (map["steps"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                imageUrl = map["imageUrl"] as? String ?: "",
                createdBy = map["createdBy"] as? String ?: "",
                createdAt = map["createdAt"] as? Timestamp
            )
        }
    }
}
