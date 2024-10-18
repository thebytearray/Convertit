package com.nasahacker.convertit.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.nasahacker.convertit.ConvertItApplication
import com.nasahacker.convertit.R
import com.nasahacker.convertit.model.AudioBitrate
import com.nasahacker.convertit.model.AudioFormat
import com.nasahacker.convertit.util.Constants.AUDIO_FORMAT
import com.nasahacker.convertit.util.Constants.BITRATE
import com.nasahacker.convertit.util.Constants.CHANNEL_ID
import com.nasahacker.convertit.util.Constants.CONVERT_BROADCAST_ACTION
import com.nasahacker.convertit.util.Constants.IS_SUCCESS
import com.nasahacker.convertit.util.Constants.URI_LIST
import com.nasahacker.convertit.util.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConvertionService : Service() {
    private var isSuccess: Boolean = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get the URI list from the intent
        val listOfUri: ArrayList<Uri>? = intent?.getParcelableArrayListExtra(URI_LIST)
        val bitrate: AudioBitrate = AudioBitrate.fromBitrate(intent?.getStringExtra(BITRATE))
        val format: AudioFormat = AudioFormat.fromExtension(intent?.getStringExtra(AUDIO_FORMAT))
        val broadcastIntent = Intent().apply {
            action = CONVERT_BROADCAST_ACTION
        }
        if (listOfUri != null) {
            // Start the service as a foreground service
            startForeground(1, getInitialNotification())
            // Launch the audio conversion process in the background using coroutines
            CoroutineScope(Dispatchers.IO).launch {
                FileUtils.convertAudio(
                    context = this@ConvertionService,
                    uris = listOfUri,
                    outputFormat = format,
                    bitrate = bitrate,
                    onSuccess = { msg ->
                        isSuccess = true
                        broadcastIntent.putExtra(IS_SUCCESS, isSuccess)
                        sendBroadcast(broadcastIntent)
                        stopSelf()
                    },
                    onFailure = { error ->
                        isSuccess = false
                        broadcastIntent.putExtra(IS_SUCCESS, isSuccess)
                        sendBroadcast(broadcastIntent)
                        stopSelf()
                    },

                    )
            }
        } else {
            isSuccess = false
            broadcastIntent.putExtra(IS_SUCCESS, isSuccess)
            sendBroadcast(broadcastIntent)
            stopSelf()
        }

        return START_STICKY
    }

    /**
     * Creates the initial notification for starting the foreground service
     */
    private fun getInitialNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.converting_audio_files))
            .setContentText(getString(R.string.label_converting_audio))
            .setAutoCancel(false)
            .setProgress(100, 50, false)
            .build()
    }

    /**
     * Updates the notification with the conversion progress
     */
    private fun updateNotification(progress: Int) {
        val manager = NotificationManagerCompat.from(this)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.converting_audio_files))
            .setContentText("Conversion in progress: $progress%")
            .setAutoCancel(false)
            .setProgress(100, progress, false)

        // Check for notification permission (required for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Update the notification
        manager.notify(1, notification.build())
    }
}
