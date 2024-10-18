package com.nasahacker.convertit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.nasahacker.convertit.util.Constants.CHANNEL_ID
import com.nasahacker.convertit.util.Constants.CHANNEL_NAME

class ConvertItApplication : Application() {
    companion object {
        lateinit var application: Application
    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val manager = NotificationManagerCompat.from(this)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
    }
}