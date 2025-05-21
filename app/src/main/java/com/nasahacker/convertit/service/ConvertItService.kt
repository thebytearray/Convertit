package com.nasahacker.convertit.service

/**
 * @author Tamim Hossain
 * @email tamimh.dev@gmail.com
 * @license Apache-2.0
 *
 * ConvertIt is a free and easy-to-use audio converter app.
 * It supports popular audio formats like MP3 and M4A.
 * With options for high-quality bitrates ranging from 128k to 320k,
 * ConvertIt offers a seamless conversion experience tailored to your needs.
 */

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
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
        Log.i(TAG, "Service created - Process ID: ${android.os.Process.myPid()}")
        startForegroundServiceWithNotification()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Log.i(
            TAG, "onStartCommand: Received intent with action: ${intent?.action}, startId: $startId"
        )

        if (intent?.action == ACTION_STOP_SERVICE) {
            Log.i(TAG, "Stopping service as per user request. startId: $startId")
            showCompletionNotification(false)
            broadcastConversionResult(Intent().apply { action = CONVERT_BROADCAST_ACTION }, false)
            stopForegroundService()
            return START_NOT_STICKY
        }

        val uriList: ArrayList<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra(URI_LIST, Uri::class.java)
        } else {
            intent?.getParcelableArrayListExtra(URI_LIST)
        }

        val bitrate = AudioBitrate.fromBitrate(intent?.getStringExtra(BITRATE))
        val format = AudioFormat.fromExtension(intent?.getStringExtra(AUDIO_FORMAT))
        val speed = intent?.getStringExtra(AUDIO_PLAYBACK_SPEED) ?: "1.0"

        Log.d(
            TAG, """
            Conversion parameters:
            - Format: ${format.extension}
            - Bitrate: ${bitrate.bitrate}
            - Playback Speed: $speed
            - Number of files: ${uriList?.size ?: 0}
            - Files: ${uriList?.joinToString { it.lastPathSegment ?: "unknown" }}
        """.trimIndent()
        )

        if (uriList.isNullOrEmpty()) {
            Log.e(TAG, "No valid URIs provided. Stopping service. startId: $startId")
            showCompletionNotification(false)
            broadcastConversionResult(Intent().apply { action = CONVERT_BROADCAST_ACTION }, false)
            stopForegroundService()
            return START_NOT_STICKY
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Starting audio conversion for ${uriList.size} files. startId: $startId")
                AppUtil.convertAudio(
                    context = this@ConvertItService,
                    speed,
                    uris = uriList,
                    outputFormat = format,
                    bitrate = bitrate,
                    onSuccess = {
                        Log.i(
                            TAG,
                            "Conversion completed successfully for all files. startId: $startId"
                        )
                        showCompletionNotification(true)
                        broadcastConversionResult(Intent().apply {
                            action = CONVERT_BROADCAST_ACTION
                        }, true)
                        stopForegroundService()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Conversion failed with error: ${error}. startId: $startId")
                        showCompletionNotification(false)
                        broadcastConversionResult(Intent().apply {
                            action = CONVERT_BROADCAST_ACTION
                        }, false)
                        stopForegroundService()
                    },
                    onProgress = { progress ->
                        Log.v(TAG, "Conversion progress: $progress%. startId: $startId")
                        updateNotification(progress)
                    },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during conversion. startId: $startId", e)
                showCompletionNotification(false)
                broadcastConversionResult(
                    Intent().apply { action = CONVERT_BROADCAST_ACTION }, false
                )
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
            ServiceCompat.startForeground(
                /* service = */ this,
                /* id = */ notificationId,
                /* notification = */ notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
                } else {
                    0
                },
            )
            isForegroundServiceStarted = true
            Log.i(TAG, "Started foreground service with notification ID: $notificationId")
        } else {
            Log.w(TAG, "Foreground service already started. Skipping start.")
        }
    }

    private fun stopForegroundService() {
        if (!isForegroundServiceStarted) {
            Log.w(TAG, "Attempted to stop foreground service when it wasn't started")
            return
        }

        isForegroundServiceStarted = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
        Log.i(TAG, "Foreground service stopped and removed")
    }

    private fun createProgressNotification(
        progress: Int,
        isIndeterminate: Boolean,
    ): Notification {
        val stopIntent = Intent(this, ConvertItService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val progressText = if (isIndeterminate) {
            getString(R.string.label_converting_audio)
        } else {
            "Conversion in progress: $progress%"
        }

        Log.d(
            TAG, "Creating progress notification: $progressText (Indeterminate: $isIndeterminate)"
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.converting_audio_files))
            .setContentText(progressText).setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, progress, isIndeterminate).setAutoCancel(false).setOngoing(true)
            .setDefaults(0).setOnlyAlertOnce(true)
            .addAction(R.drawable.baseline_stop_24, "Stop", stopPendingIntent).build()
    }

    private fun updateNotification(progress: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(
                TAG, "Notification permission not granted. Skipping update for progress: $progress%"
            )
            return
        }

        val notification = createProgressNotification(progress, false)
        NotificationManagerCompat.from(this).notify(notificationId, notification)
        Log.v(TAG, "Updated notification: Progress $progress%")
    }

    private fun showCompletionNotification(success: Boolean) {
        stopForegroundService()

        val notificationText = if (success) {
            getString(R.string.conversion_success)
        } else {
            getString(R.string.conversion_failed)
        }

        Log.i(TAG, "Showing completion notification: $notificationText")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.conversion_status)).setContentText(notificationText)
            .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).build()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(
                TAG,
                "Notification permission not granted. Skipping completion notification for status: $success"
            )
            return
        }

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
