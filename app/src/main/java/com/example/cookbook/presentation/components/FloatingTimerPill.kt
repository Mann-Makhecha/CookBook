package com.example.cookbook.presentation.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookbook.data.service.TimerService
import kotlin.math.roundToInt

/**
 * Floating pill-shaped timer widget that appears on all screens
 * when the cooking timer is running and the user is not on the timer screen.
 *
 * Features:
 * - Draggable: user can move it anywhere on screen
 * - Closable: X button dismisses the pill (timer keeps running in background)
 * - Tappable: navigates back to the timer screen
 */
@Composable
fun FloatingTimerPill(
    isOnTimerScreen: Boolean,
    onPillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var timerService by remember { mutableStateOf<TimerService?>(null) }
    var isBound by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }

    // Drag offset
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as TimerService.TimerBinder).getService()
                timerService = service
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                timerService = null
                isBound = false
            }
        }
    }

    // Bind to service
    DisposableEffect(Unit) {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            if (isBound) {
                try {
                    context.unbindService(connection)
                } catch (_: Exception) {}
                isBound = false
            }
        }
    }

    // Observe service state
    val remainingSeconds by timerService?.remainingSeconds?.collectAsState()
        ?: remember { mutableIntStateOf(0) }
    val isRunning by timerService?.isRunning?.collectAsState()
        ?: remember { mutableStateOf(false) }
    val isFinished by timerService?.isFinished?.collectAsState()
        ?: remember { mutableStateOf(false) }

    val isActive = isRunning || isFinished

    // Re-show the pill when a new timer starts
    LaunchedEffect(isActive) {
        if (isActive) {
            isDismissed = false
        }
    }

    val shouldShow = isActive && !isOnTimerScreen && !isDismissed

    AnimatedVisibility(
        visible = shouldShow,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Surface(
                onClick = onPillClick,
                shape = RoundedCornerShape(28.dp),
                color = if (isFinished) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = if (isFinished) MaterialTheme.colorScheme.onError
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFinished) "Time's up!" else timeText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isFinished) MaterialTheme.colorScheme.onError
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    // Close / dismiss button
                    IconButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss timer pill",
                            tint = if (isFinished) MaterialTheme.colorScheme.onError.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
