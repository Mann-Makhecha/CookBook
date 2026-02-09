package com.example.cookbook.data.model

/**
 * Shopping list item data model.
 * Represents an ingredient added to the user's shopping list.
 */
data class ShoppingItem(
    val id: String = "",
    val ingredient: String = "",
    val isChecked: Boolean = false,
    val recipeId: String = "", // Optional: link to recipe
    val recipeName: String = "" // Optional: name of recipe this is from
) {
    /**
     * Convert ShoppingItem to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "ingredient" to ingredient,
            "isChecked" to isChecked,
            "recipeId" to recipeId,
            "recipeName" to recipeName
        )
    }

    companion object {
        /**
         * Create a ShoppingItem from Firestore document data.
         */
        fun fromMap(map: Map<String, Any>): ShoppingItem {
            return ShoppingItem(
                id = map["id"] as? String ?: "",
                ingredient = map["ingredient"] as? String ?: "",
                isChecked = map["isChecked"] as? Boolean ?: false,
                recipeId = map["recipeId"] as? String ?: "",
                recipeName = map["recipeName"] as? String ?: ""
            )
        }
    }
}
