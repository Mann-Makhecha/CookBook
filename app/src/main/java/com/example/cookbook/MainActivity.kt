package com.example.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cookbook.presentation.components.FloatingTimerPill
import com.example.cookbook.presentation.navigation.NavGraph
import com.example.cookbook.ui.theme.CookBookTheme
import com.example.cookbook.util.Constants

/**
 * Main Activity for the CookBook app.
 * Sets up the navigation graph, Material 3 theme,
 * and the floating timer pill overlay.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CookBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    com.example.cookbook.presentation.components.GlassBackground {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        Box(modifier = Modifier.fillMaxSize()) {
                            NavGraph(navController = navController)

                            // Floating timer pill — shown on all screens except the timer screen
                            FloatingTimerPill(
                                isOnTimerScreen = currentRoute == Constants.Routes.TIMER,
                                onPillClick = {
                                    if (currentRoute != Constants.Routes.TIMER) {
                                        navController.navigate(Constants.Routes.timer(0)) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}