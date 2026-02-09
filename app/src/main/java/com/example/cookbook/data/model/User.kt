package com.example.cookbook.data.model

/**
 * User data model representing a user in the CookBook app.
 * Mapped to Firestore 'users' collection.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val favorites: List<String> = emptyList() // List of recipe IDs
) {
    /**
     * Convert User to a Map for Firestore storage.
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "favorites" to favorites
        )
    }

    companion object {
        /**
         * Create a User from Firestore document data.
         */
        fun fromMap(map: Map<String, Any>): User {
            return User(
                uid = map["uid"] as? String ?: "",
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                favorites = (map["favorites"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        }
    }
}
