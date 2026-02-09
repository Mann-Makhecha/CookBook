package com.example.cookbook.util

object Constants {
    // Firestore Collections
    const val USERS_COLLECTION = "users"
    const val RECIPES_COLLECTION = "recipes"

    // Firebase Storage Paths
    const val RECIPE_IMAGES_PATH = "recipe_images"

    // Recipe Categories
    val RECIPE_CATEGORIES = listOf(
        "Breakfast",
        "Lunch",
        "Dinner",
        "Snacks",
        "Dessert"
    )

    // Recipe Difficulties
    val RECIPE_DIFFICULTIES = listOf(
        "Easy",
        "Medium",
        "Hard"
    )

    // Navigation Routes
    object Routes {
        const val SPLASH = "splash"
        const val LOGIN = "login"
        const val REGISTER = "register"
        const val FORGOT_PASSWORD = "forgot_password"
        const val HOME = "home"
        const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
        const val ADD_RECIPE = "add_recipe"
        const val EDIT_RECIPE = "edit_recipe/{recipeId}"
        const val FAVORITES = "favorites"
        const val SEARCH = "search"
        const val TIMER = "timer/{duration}"
        const val SHOPPING_LIST = "shopping_list"
        const val PROFILE = "profile"

        fun recipeDetail(recipeId: String) = "recipe_detail/$recipeId"
        fun editRecipe(recipeId: String) = "edit_recipe/$recipeId"
        fun timer(durationMinutes: Int) = "timer/$durationMinutes"
    }

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_IMAGE_SIZE_MB = 5

    // Preferences Keys
    const val PREFS_NAME = "cookbook_prefs"
    const val KEY_USER_ID = "user_id"
}
