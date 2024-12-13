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
import com.nasahacker.convertit.util.Constant.ACTION_STOP_SERVICE
import com.nasahacker.convertit.util.Constant.AUDIO_FORMAT
import com.nasahacker.convertit.util.Constant.BITRATE
import com.nasahacker.convertit.util.Constant.CHANNEL_ID
import com.nasahacker.convertit.util.Constant.CONVERT_BROADCAST_ACTION
import com.nasahacker.convertit.util.Constant.IS_SUCCESS
import com.nasahacker.convertit.util.Constant.URI_LIST
import com.nasahacker.convertit.util.AppUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioConversionService : Service() {

    private val notificationId = 1

    companion object {
        var isForegroundServiceStarted = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val uriList: ArrayList<Uri>? = intent?.getParcelableArrayListExtra(URI_LIST)
        val bitrate = AudioBitrate.fromBitrate(intent?.getStringExtra(BITRATE))
        val format = AudioFormat.fromExtension(intent?.getStringExtra(AUDIO_FORMAT))
        Log.d(
            "ZERO_DOLLAR", "Service: Starting Conversion with Format Extension: ${
                intent?.getStringExtra(
                    AUDIO_FORMAT
                )
            } and Bitrate Name: ${intent?.getStringExtra(BITRATE)}"
        )

        // Log the received values for format and bitrate
        Log.d(
            "ZERO_DOLLAR",
            "Service: Received Format: ${format.extension} and Bitrate: ${bitrate.bitrate}"
        )
        val broadcastIntent = Intent().apply { action = CONVERT_BROADCAST_ACTION }

        if (intent?.action == ACTION_STOP_SERVICE) {
            showCompletionNotification(false)
            broadcastConversionResult(broadcastIntent, false)
            stopForegroundService()
        }

        if (uriList != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AppUtil.convertAudio(context = this@AudioConversionService,
                        uris = uriList,
                        outputFormat = format,
                        bitrate = bitrate,
                        onSuccess = {
                            Log.d("HACKER", "onStartCommand: CONVERTION SUCCESS")
                            // Notify success and send broadcast
                            showCompletionNotification(success = true)
                            broadcastConversionResult(broadcastIntent, true)
                            stopForegroundService()
                        },
                        onFailure = {
                            Log.d("HACKER", "onStartCommand: CONVERTION failure")
                            // Notify failure and send broadcast
                            showCompletionNotification(success = false)
                            broadcastConversionResult(broadcastIntent, false)
                            stopForegroundService()
                        })
                } catch (e: Exception) {
                    Log.e("AudioConversionService", "Conversion failed", e)
                    Log.d("HACKER", "onStartCommand: CONVERTION FAILURE")
                    showCompletionNotification(success = false)
                    broadcastConversionResult(broadcastIntent, false)
                    stopForegroundService()
                }
            }
        } else {
            showCompletionNotification(success = false)
            broadcastConversionResult(broadcastIntent, false)
            stopForegroundService()
        }
        return START_STICKY
    }

    private fun broadcastConversionResult(intent: Intent, isSuccess: Boolean) {
        intent.putExtra(IS_SUCCESS, isSuccess)
        sendBroadcast(intent)
    }

    private fun startForegroundServiceWithNotification() {
        val notification = createProgressNotification(progress = 0, isIndeterminate = true)
        if (!isForegroundServiceStarted) {
            startForeground(notificationId, notification)
            isForegroundServiceStarted = true
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
    }

    private fun createProgressNotification(progress: Int, isIndeterminate: Boolean): Notification {
        val stopIntent = Intent(this, AudioConversionService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val progressText = if (isIndeterminate) {
            getString(R.string.label_converting_audio)
        } else {
            "Conversion in progress: $progress%"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.converting_audio_files))
            .setContentText(progressText).setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, progress, isIndeterminate).setAutoCancel(false).setOngoing(true)
            .addAction(
                R.drawable.baseline_stop_24, "Stop", stopPendingIntent
            ).build()
    }


    private fun updateNotification(progress: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = createProgressNotification(progress, isIndeterminate = false)
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun showCompletionNotification(success: Boolean) {
        // First, stop the foreground service and remove the ongoing notification
        stopForegroundService()

        val notificationText = if (success) {
            getString(R.string.conversion_success)
        } else {
            getString(R.string.conversion_failed)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.conversion_status)).setContentText(notificationText)
            .setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(true).build()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
