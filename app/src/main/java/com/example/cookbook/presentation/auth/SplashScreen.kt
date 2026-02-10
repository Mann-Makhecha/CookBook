package com.example.cookbook.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cookbook.data.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * Splash Screen that displays while checking authentication state.
 * Navigates to either Login or Home based on whether the user is signed in.
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authRepository = AuthRepository()

    LaunchedEffect(Unit) {
        // Delay for splash screen effect
        delay(1500)

        // Check if user is signed in
        if (authRepository.isUserSignedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Icon/Logo would go here
                Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Vegetarian placeholder",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(96.dp)
                            )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CookBook",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your Digital Recipe Collection",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
