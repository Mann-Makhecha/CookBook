package com.example.cookbook.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.cookbook.MainActivity
import com.example.cookbook.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service that keeps the cooking timer running
 * even when the user navigates away from the timer screen.
 * Shows a persistent notification with the countdown.
 * Plays alarm sound when timer finishes.
 */
class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "cooking_timer_channel"
        const val ALARM_CHANNEL_ID = "cooking_timer_alarm_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_RESET = "ACTION_RESET"

        const val EXTRA_DURATION = "EXTRA_DURATION"
    }

    private val binder = TimerBinder()
    private var timerJob: Job? = null
    private var autoStopJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var totalSeconds = 0
    private var alarmRingtone: Ringtone? = null

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getIntExtra(EXTRA_DURATION, 0)
                startTimer(duration)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> {
                stopTimer()
                stopSelf()
            }
            ACTION_RESET -> {
                resetTimer()
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun startTimer(durationMinutes: Int) {
        autoStopJob?.cancel()
        stopAlarmSound()
        totalSeconds = durationMinutes * 60
        _remainingSeconds.value = totalSeconds
        _isFinished.value = false
        _isRunning.value = true

        startForeground(NOTIFICATION_ID, buildNotification())
        startCountdown()
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        updateNotification()
    }

    fun resumeTimer() {
        if (_remainingSeconds.value > 0 && !_isFinished.value) {
            _isRunning.value = true
            startCountdown()
            updateNotification()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        stopAlarmSound()
        _remainingSeconds.value = totalSeconds
        _isRunning.value = false
        _isFinished.value = false
        updateNotification()
    }

    private fun stopTimer() {
        timerJob?.cancel()
        stopAlarmSound()
        _isRunning.value = false
        _isFinished.value = false
        _remainingSeconds.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000L)
                _remainingSeconds.value = _remainingSeconds.value - 1
                updateNotification()
            }
            if (_remainingSeconds.value == 0) {
                _isRunning.value = false
                _isFinished.value = true
                playAlarmSound()
                showFinishedNotification()
                // Auto-stop the service after 60 seconds if user doesn't dismiss
                autoStopJob = serviceScope.launch {
                    delay(60_000L)
                    withContext(Dispatchers.Main) {
                        stopTimer()
                        stopSelf()
                    }
                }
            }
        }
    }

    /**
     * Play alarm sound directly via Ringtone API.
     * This is more reliable than notification sound since it
     * doesn't depend on channel importance level.
     */
    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            alarmRingtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            alarmRingtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            alarmRingtone?.isLooping = true
            alarmRingtone?.play()
        } catch (_: Exception) {
            // Silently fail if no alarm ringtone available
        }
    }

    private fun stopAlarmSound() {
        try {
            alarmRingtone?.stop()
            alarmRingtone = null
        } catch (_: Exception) {}
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Low-priority channel for countdown updates (silent)
        val timerChannel = NotificationChannel(
            CHANNEL_ID,
            "Cooking Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows cooking timer countdown"
            setShowBadge(false)
        }
        manager.createNotificationChannel(timerChannel)

        // High-priority channel for alarm when timer finishes
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Timer Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm when cooking timer finishes"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
        }
        manager.createNotificationChannel(alarmChannel)
    }

    private fun buildNotification(): Notification {
        val minutes = _remainingSeconds.value / 60
        val seconds = _remainingSeconds.value % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = when {
            _isFinished.value -> "Time's up!"
            _isRunning.value -> "Cooking… $timeText remaining"
            else -> "Paused at $timeText"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle("Cooking Timer")
            .setContentText(statusText)
            .setContentIntent(pendingIntent)
            .setOngoing(!_isFinished.value)
            .setSilent(true)
            .setOnlyAlertOnce(true)

        // Add pause/resume action
        if (_isRunning.value) {
            val pauseIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePending = PendingIntent.getService(
                this, 1, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Pause", pausePending)
        } else if (!_isFinished.value) {
            val resumeIntent = Intent(this, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePending = PendingIntent.getService(
                this, 2, resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Resume", resumePending)
        }

        // Add stop action
        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(0, "Stop", stopPending)

        return builder.build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun showFinishedNotification() {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 3, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use the HIGH priority alarm channel so the notification is visible
        val notification = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle("⏰ Cooking Timer")
            .setContentText("Time's up! Your food is ready!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .addAction(0, "Dismiss", stopPending)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        stopAlarmSound()
        serviceScope.cancel()
        timerJob?.cancel()
        super.onDestroy()
    }
}
