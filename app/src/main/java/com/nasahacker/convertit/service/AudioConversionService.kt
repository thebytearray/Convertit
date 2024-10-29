package com.nasahacker.convertit.service

import android.Manifest
import android.app.Notification
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
import com.nasahacker.convertit.model.AudioBitrate
import com.nasahacker.convertit.model.AudioFormat
import com.nasahacker.convertit.util.Constants.AUDIO_FORMAT
import com.nasahacker.convertit.util.Constants.BITRATE
import com.nasahacker.convertit.util.Constants.CHANNEL_ID
import com.nasahacker.convertit.util.Constants.CONVERT_BROADCAST_ACTION
import com.nasahacker.convertit.util.Constants.IS_SUCCESS
import com.nasahacker.convertit.util.Constants.URI_LIST
import com.nasahacker.convertit.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.log

class AudioConversionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uriList: ArrayList<Uri>? = intent?.getParcelableArrayListExtra(URI_LIST)
        val bitrate = AudioBitrate.fromBitrate(intent?.getStringExtra(BITRATE))
        val format = AudioFormat.fromExtension(intent?.getStringExtra(AUDIO_FORMAT))
        val broadcastIntent = Intent().apply { action = CONVERT_BROADCAST_ACTION }
        Log.d("HACKER", "onStartCommand: $format")
        Log.d("HACKER", "onStartCommand: $bitrate")

        if (uriList != null) {
            startForeground(1, createNotification())
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AppUtils.convertAudio(
                        context = this@AudioConversionService,
                        uris = uriList,
                        outputFormat = format,
                        bitrate = bitrate,
                        onSuccess = {
                            broadcastConversionResult(broadcastIntent, true)
                            stopSelf()
                        },
                        onFailure = {
                            broadcastConversionResult(broadcastIntent, false)
                            stopSelf()
                        }
                    )
                } catch (e: Exception) {
                    broadcastConversionResult(broadcastIntent, false)
                    e.printStackTrace()
                }
            }
        } else {
            broadcastConversionResult(broadcastIntent, false)
            stopSelf()
        }
        return START_STICKY
    }

    private fun broadcastConversionResult(intent: Intent, isSuccess: Boolean) {
        intent.putExtra(IS_SUCCESS, isSuccess)
        sendBroadcast(intent)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.converting_audio_files))
            .setContentText(getString(R.string.label_converting_audio))
            .setAutoCancel(false)
            .setProgress(100, 50, false)
            .build()
    }

    private fun updateNotification(progress: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        NotificationManagerCompat.from(this).notify(
            1, NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.converting_audio_files))
                .setContentText("Conversion in progress: $progress%")
                .setAutoCancel(false)
                .setProgress(100, progress, false)
                .build()
        )
    }
}
