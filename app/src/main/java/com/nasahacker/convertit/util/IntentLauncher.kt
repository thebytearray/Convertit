package com.nasahacker.convertit.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.nasahacker.convertit.R
import java.io.File

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

class IntentLauncher(
    private val activity: Activity,
) {
    fun openFilePicker(pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted()) {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*"))
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions()
        }
    }

    fun openVideoFilePicker(pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted()) {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "video/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions()
        }
    }

    fun openFolderPicker(pickFolderLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            pickFolderLauncher.launch(intent)
        } else {
            requestStoragePermissions()
        }
    }

    fun openMetadataEditorFilePicker(pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted()) {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*"))
                }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions()
        }
    }

    fun openCueFilePicker(pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted()) {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                        "text/plain",
                        "text/*",
                        "application/x-cue",
                        "audio/x-cue",
                        "application/octet-stream"
                    ))
                }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions()
        }
    }

    fun openMusicFileInPlayer(file: File) {
        if (file.exists()) {
            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(activity, "${activity.packageName}.provider", file)
                } else {
                    Uri.fromFile(file)
                }
            val intent =
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "audio/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            activity.startActivity(intent)
        } else {
            Toast
                .makeText(
                    activity,
                    activity.getString(R.string.label_no_app_found_to_open_the_file),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun shareMusicFile(file: File) {
        if (file.exists()) {
            val fileUri =
                FileProvider.getUriForFile(activity, "${activity.packageName}.provider", file)
            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            activity.startActivity(
                Intent.createChooser(
                    shareIntent,
                    activity.getString(R.string.label_share_music_file),
                ),
            )
        } else {
            Toast
                .makeText(
                    activity,
                    activity.getString(R.string.label_file_does_not_exist),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun openLink(link: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    }

    private fun isStoragePermissionGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestStoragePermissions() {
        if (!isStoragePermissionGranted()) {
            val permissions =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                }
            ActivityCompat.requestPermissions(activity, permissions, 101)
        } else {
            Toast
                .makeText(
                    activity,
                    activity.getString(R.string.label_storage_permissions_are_already_granted),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }
}
