package com.example.cookbook.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cookbook.presentation.auth.AuthViewModel
import com.example.cookbook.presentation.auth.ForgotPasswordScreen
import com.example.cookbook.presentation.auth.LoginScreen
import com.example.cookbook.presentation.auth.RegisterScreen
import com.example.cookbook.presentation.auth.SplashScreen
import com.example.cookbook.presentation.favorites.FavoritesScreen
import com.example.cookbook.presentation.profile.ProfileScreen
import com.example.cookbook.presentation.recipe.AddRecipeScreen
import com.example.cookbook.presentation.recipe.EditRecipeScreen
import com.example.cookbook.presentation.recipe.RecipeDetailScreen
import com.example.cookbook.presentation.search.SearchScreen
import com.example.cookbook.util.Constants

/**
 * Main navigation graph for the CookBook app.
 * Defines all navigation routes and screen transitions.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Constants.Routes.SPLASH
) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Constants.Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(Constants.Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Constants.Routes.HOME) {
                        popUpTo(Constants.Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(Constants.Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Constants.Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Constants.Routes.FORGOT_PASSWORD)
                },
                onLoginSuccess = {
                    navController.navigate(Constants.Routes.HOME) {
                        popUpTo(Constants.Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Register Screen
        composable(Constants.Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Constants.Routes.HOME) {
                        popUpTo(Constants.Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // Forgot Password Screen
        composable(Constants.Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Home Screen
        composable(Constants.Routes.HOME) {
            com.example.cookbook.presentation.home.HomeScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Constants.Routes.recipeDetail(recipeId))
                },
                onAddRecipeClick = {
                    navController.navigate(Constants.Routes.ADD_RECIPE)
                },
                onSearchClick = {
                    navController.navigate(Constants.Routes.SEARCH)
                },
                onFavoritesClick = {
                    navController.navigate(Constants.Routes.FAVORITES)
                },
                onProfileClick = {
                    navController.navigate(Constants.Routes.PROFILE)
                }
            )
        }

        // Recipe Detail Screen
        composable(
            route = Constants.Routes.RECIPE_DETAIL,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Constants.Routes.editRecipe(id))
                },
                onTimerClick = { duration ->
                    navController.navigate(Constants.Routes.timer(duration))
                },
                onShoppingListClick = { ingredients ->
                    // TODO: Pass ingredients to shopping list
                    navController.navigate(Constants.Routes.SHOPPING_LIST)
                }
            )
        }

        // Add Recipe Screen
        composable(Constants.Routes.ADD_RECIPE) {
            AddRecipeScreen(
                onNavigateBack = { navController.popBackStack() },
                onRecipeAdded = { recipeId ->
                    navController.navigate(Constants.Routes.recipeDetail(recipeId)) {
                        popUpTo(Constants.Routes.HOME)
                    }
                }
            )
        }

        // Edit Recipe Screen
        composable(
            route = Constants.Routes.EDIT_RECIPE,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            EditRecipeScreen(
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() },
                onRecipeUpdated = {
                    navController.popBackStack()
                }
            )
        }

        // Favorites Screen
        composable(Constants.Routes.FAVORITES) {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() },
                onRecipeClick = { recipeId ->
                    navController.navigate(Constants.Routes.recipeDetail(recipeId))
                }
            )
        }

        // Search Screen
        composable(Constants.Routes.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onRecipeClick = { recipeId ->
                    navController.navigate(Constants.Routes.recipeDetail(recipeId))
                }
            )
        }

        // Timer Screen - Placeholder
        composable(
            route = Constants.Routes.TIMER,
            arguments = listOf(
                navArgument("duration") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            // TODO: Implement Timer Screen
        }

        // Shopping List Screen - Placeholder
        composable(Constants.Routes.SHOPPING_LIST) {
            // TODO: Implement Shopping List Screen
        }

        // Profile Screen
        composable(Constants.Routes.PROFILE) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRecipeClick = { recipeId ->
                    navController.navigate(Constants.Routes.recipeDetail(recipeId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
