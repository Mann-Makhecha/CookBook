package com.example.cookbook.presentation.recipe

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookbook.data.service.TimerService

/**
 * Timer Screen for cooking countdown.
 * Binds to TimerService so the timer keeps running
 * in the background with a persistent notification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    durationMinutes: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val totalSeconds = durationMinutes * 60

    // Service binding
    var timerService by remember { mutableStateOf<TimerService?>(null) }
    var isBound by remember { mutableStateOf(false) }

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

    // Bind to the service on composition
    DisposableEffect(Unit) {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            if (isBound) {
                context.unbindService(connection)
                isBound = false
            }
        }
    }

    // Observe service state
    val remainingSeconds by timerService?.remainingSeconds?.collectAsState()
        ?: remember { mutableIntStateOf(totalSeconds) }
    val isRunning by timerService?.isRunning?.collectAsState()
        ?: remember { mutableStateOf(false) }
    val isFinished by timerService?.isFinished?.collectAsState()
        ?: remember { mutableStateOf(false) }

    // Progress for the circular indicator (1.0 = full, 0.0 = empty)
    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f,
        label = "timer_progress"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cooking Timer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular timer display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                val finishedColor = MaterialTheme.colorScheme.error

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background track
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        color = if (isFinished) finishedColor else primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeText,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFinished) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (isFinished) {
                        Text(
                            text = "Time's up!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = "$durationMinutes min timer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset button
                FilledTonalIconButton(
                    onClick = {
                        if (isBound) {
                            timerService?.resetTimer()
                        }
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }

                // Start / Pause button
                FloatingActionButton(
                    onClick = {
                        if (isFinished) {
                            // Reset and start via service
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_START
                                putExtra(TimerService.EXTRA_DURATION, durationMinutes)
                            }
                            context.startForegroundService(intent)
                        } else if (isRunning) {
                            timerService?.pauseTimer()
                        } else {
                            // Start or resume
                            if (remainingSeconds == totalSeconds || remainingSeconds == 0) {
                                // Fresh start
                                val intent = Intent(context, TimerService::class.java).apply {
                                    action = TimerService.ACTION_START
                                    putExtra(TimerService.EXTRA_DURATION, durationMinutes)
                                }
                                context.startForegroundService(intent)
                            } else {
                                // Resume
                                timerService?.resumeTimer()
                            }
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = when {
                            isFinished -> Icons.Default.Refresh
                            isRunning -> Icons.Default.Pause
                            else -> Icons.Default.PlayArrow
                        },
                        contentDescription = when {
                            isFinished -> "Restart"
                            isRunning -> "Pause"
                            else -> "Start"
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Stop button
                FilledTonalIconButton(
                    onClick = {
                        val intent = Intent(context, TimerService::class.java).apply {
                            action = TimerService.ACTION_STOP
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
            }
        }
    }
}
