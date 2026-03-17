package com.example.cookbook.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * Modern liquid glass background with floating animated glowing orbs
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Deep slate base
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "LiquidBlobs")

        // Animated position values for floating orbs
        val x1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "x1"
        )
        val y1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
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
                .blur(radius = 120.dp, edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded)
        ) {
            val width = size.width
            val height = size.height
            
            // Orb 1 - Cyan
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00F0FF).copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(width * x1, height * (0.3f + 0.4f * y1)),
                    radius = width * 0.6f
                ),
                center = Offset(width * x1, height * (0.3f + 0.4f * y1)),
                radius = width * 0.6f
            )
            
            // Orb 2 - Purple/Pink
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF007F).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(width * x2, height * y2),
                    radius = width * 0.7f
                ),
                center = Offset(width * x2, height * y2),
                radius = width * 0.7f
            )
            
            // Orb 3 - Blue
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7000FF).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(width * (1f - x1), height * x2),
                    radius = width * 0.5f
                ),
                center = Offset(width * (1f - x1), height * x2),
                radius = width * 0.5f
            )
        }

        // Noise overlay (optional for extra texture, skipped for simple liquid)
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))

        content()
    }
}

/**
 * Reusable Glassmorphic Container
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}
