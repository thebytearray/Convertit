package com.nasahacker.convertit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.nasahacker.convertit.util.AppConfig.CHANNEL_ID
import com.nasahacker.convertit.util.AppConfig.CHANNEL_NAME
import dagger.hilt.android.HiltAndroidApp

/**
 * Convertit Android app
 * <a href="https://github.com/thebytearray/Convertit">GitHub Repository</a>
 *
 * Created by Tamim Hossain.
 * Copyright (c) 2025 The Byte Array LTD.
 *
 * This file is part of the Convertit Android app.
 *
 * The Convertit Android app is free software: you can redistribute it and/or
 * modify it under the terms of the Apache License, Version 2.0 as published by
 * the Apache Software Foundation.
 *
 * The Convertit Android app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Apache License for more
 * details.
 *
 * You should have received a copy of the Apache License
 * along with the Convertit Android app. If not, see <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>.
 *
 * @author Tamim Hossain
 * @company The Byte Array LTD
 * @year 2025
 * @license Apache-2.0
 */

@HiltAndroidApp
class App : Application() {
    companion object {
        lateinit var application: App
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        initChannel()
        clearCacheOnStartup()
    }

    private fun initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Notifications for audio conversion progress and completion"
                    setShowBadge(true)
                    enableLights(false)
                    enableVibration(false)
                    setSound(null, null)
                }
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
    }

    private fun clearCacheOnStartup() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
