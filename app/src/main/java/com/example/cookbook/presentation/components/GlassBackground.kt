package com.example.cookbook.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium liquid glass background with soft subtle floating orbs
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Core background color
    val backgroundColor = if (isDark) Color(0xFF0B1320) else Color(0xFFF0F4F8)
    
    // Orb Colors
    val orb1Color = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFE0F2FE).copy(alpha = 0.8f) // Deep blue / Light sky
    val orb2Color = if (isDark) Color(0xFF0F766E).copy(alpha = 0.2f) else Color(0xFFF1F5F9).copy(alpha = 0.9f) // Subtle teal / Slate 50
    val orb3Color = if (isDark) Color(0xFF312E81).copy(alpha = 0.3f) else Color(0xFFBAE6FD).copy(alpha = 0.5f) // Indigo / Light blue
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "LiquidBlobs")

        // Animated position values for floating orbs (Very smooth and slow)
        val x1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "x1"
        )
        val y1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(25000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "y1"
        )
        
        val x2 by infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(18000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "x2"
        )
        val y2 by infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(13000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "y2"
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 100.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
        ) {
            val width = size.width
            val height = size.height
            
            // Orb 1 
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb1Color, Color.Transparent),
                    center = Offset(width * x1, height * (0.2f + 0.5f * y1)),
                    radius = width * 0.7f
                ),
                center = Offset(width * x1, height * (0.2f + 0.5f * y1)),
                radius = width * 0.7f
            )
            
            // Orb 2 
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb2Color, Color.Transparent),
                    center = Offset(width * x2, height * y2),
                    radius = width * 0.8f
                ),
                center = Offset(width * x2, height * y2),
                radius = width * 0.8f
            )
            
            // Orb 3 
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb3Color, Color.Transparent),
                    center = Offset(width * (1f - x1), height * (0.1f + 0.8f * x2)),
                    radius = width * 0.6f
                ),
                center = Offset(width * (1f - x1), height * (0.1f + 0.8f * x2)),
                radius = width * 0.6f
            )
        }

        // Extremely subtle noise overlay to prevent banding
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.02f)))

        content()
    }
}

/**
 * Premium Reusable Glassmorphic Container
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Glass Surface Color
    val surfaceColor = if (isDark) {
        listOf(Color(0xFFFFFFFF).copy(alpha = 0.06f), Color(0xFFFFFFFF).copy(alpha = 0.02f))
    } else {
        listOf(Color(0xFFFFFFFF).copy(alpha = 0.6f), Color(0xFFFFFFFF).copy(alpha = 0.3f))
    }
    
    // Glass Border Color
    val borderColor = if (isDark) {
        listOf(Color(0xFFFFFFFF).copy(alpha = 0.15f), Color(0xFFFFFFFF).copy(alpha = 0.05f))
    } else {
        listOf(Color(0xFFFFFFFF).copy(alpha = 0.7f), Color(0xFFFFFFFF).copy(alpha = 0.2f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(colors = surfaceColor))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(colors = borderColor),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}
