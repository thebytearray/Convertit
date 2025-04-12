package com.nasahacker.convertit.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nasahacker.convertit.R
import com.nasahacker.convertit.dto.AudioBitrate
import com.nasahacker.convertit.dto.AudioFormat
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.util.Constant.ACTION_STOP_SERVICE
import com.nasahacker.convertit.util.Constant.AUDIO_FORMAT
import com.nasahacker.convertit.util.Constant.AUDIO_PLAYBACK_SPEED
import com.nasahacker.convertit.util.Constant.BITRATE
import com.nasahacker.convertit.util.Constant.CHANNEL_ID
import com.nasahacker.convertit.util.Constant.CONVERT_BROADCAST_ACTION
import com.nasahacker.convertit.util.Constant.IS_SUCCESS
import com.nasahacker.convertit.util.Constant.URI_LIST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConvertItService : Service() {
    companion object {
        private const val TAG = "ConvertItService"
        var isForegroundServiceStarted = false
    }

    private val notificationId = 1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        startForegroundServiceWithNotification()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Log.i(TAG, "onStartCommand: Received intent with action: ${intent?.action}")

        if (intent?.action == ACTION_STOP_SERVICE) {
            Log.i(TAG, "Stopping service as per user request.")
            showCompletionNotification(false)
            broadcastConversionResult(Intent().apply { action = CONVERT_BROADCAST_ACTION }, false)
            stopForegroundService()
            return START_NOT_STICKY
        }

        val uriList: ArrayList<Uri>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.getParcelableArrayListExtra(URI_LIST, Uri::class.java)
            } else {
                intent?.getParcelableArrayListExtra(URI_LIST)
            }

        val bitrate = AudioBitrate.fromBitrate(intent?.getStringExtra(BITRATE))
        val format = AudioFormat.fromExtension(intent?.getStringExtra(AUDIO_FORMAT))
        val speed = intent?.getStringExtra(AUDIO_PLAYBACK_SPEED) ?: "1.0"

        Log.d(TAG, "Received Format: ${format.extension}, Bitrate: ${bitrate.bitrate}, UriList: ${uriList?.size ?: 0}")

        if (uriList.isNullOrEmpty()) {
            Log.e(TAG, "No valid URIs provided. Stopping service.")
            showCompletionNotification(false)
            broadcastConversionResult(Intent().apply { action = CONVERT_BROADCAST_ACTION }, false)
            stopForegroundService()
            return START_NOT_STICKY
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Starting audio conversion for ${uriList.size} files.")
                AppUtil.convertAudio(
                    context = this@ConvertItService,
                    speed,
                    uris = uriList,
                    outputFormat = format,
                    bitrate = bitrate,
                    onSuccess = {
                        Log.i(TAG, "Conversion successful.")
                        showCompletionNotification(true)
                        broadcastConversionResult(Intent().apply { action = CONVERT_BROADCAST_ACTION }, true)
                        stopForegroundService()
                    },
                    onFailure = {
                        Log.e(TAG, "Conversion failed.")
                        showCompletionNotification(false)
                        broadcastConversionResult(Intent().apply { action = CONVERT_BROADCAST_ACTION }, false)
                        stopForegroundService()
                    },
                    onProgress = { progress ->
                        updateNotification(progress)
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during conversion", e)
                showCompletionNotification(false)
                broadcastConversionResult(Intent().apply { action = CONVERT_BROADCAST_ACTION }, false)
                stopForegroundService()
            }
        }

        return START_STICKY
    }

    private fun broadcastConversionResult(
        intent: Intent,
        isSuccess: Boolean,
    ) {
        intent.putExtra(IS_SUCCESS, isSuccess)
        sendBroadcast(intent)
        Log.d(TAG, "Broadcast conversion result: $isSuccess")
    }

    private fun startForegroundServiceWithNotification() {
        val notification = createProgressNotification(0, true)
        if (!isForegroundServiceStarted) {
            startForeground(notificationId, notification)
            isForegroundServiceStarted = true
            Log.i(TAG, "Started foreground service with notification")
        }
    }

    private fun stopForegroundService() {
        isForegroundServiceStarted = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
        Log.i(TAG, "Foreground service stopped")
    }

    private fun createProgressNotification(
        progress: Int,
        isIndeterminate: Boolean,
    ): Notification {
        val stopIntent =
            Intent(this, ConvertItService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
        val stopPendingIntent =
            PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val progressText =
            if (isIndeterminate) {
                getString(R.string.label_converting_audio)
            } else {
                "Conversion in progress: $progress%"
            }

        Log.d(TAG, "Creating progress notification: $progressText")

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.converting_audio_files))
            .setContentText(progressText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, progress, isIndeterminate)
            .setAutoCancel(false)
            .setOngoing(true)
            .setDefaults(0)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.baseline_stop_24, "Stop", stopPendingIntent)
            .build()
    }

    private fun updateNotification(progress: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification permission not granted. Skipping update.")
            return
        }

        val notification = createProgressNotification(progress, false)
        NotificationManagerCompat.from(this).notify(notificationId, notification)
        Log.d(TAG, "Updated notification: Progress $progress%")
    }

    private fun showCompletionNotification(success: Boolean) {
        stopForegroundService()

        val notificationText =
            if (success) {
                getString(R.string.conversion_success)
            } else {
                getString(R.string.conversion_failed)
            }

        Log.i(TAG, "Showing completion notification: $notificationText")

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.conversion_status))
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission not granted. Skipping completion notification.")
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
