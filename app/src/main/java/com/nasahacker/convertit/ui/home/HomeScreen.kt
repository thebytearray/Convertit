package com.nasahacker.convertit.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nasahacker.convertit.R
import com.nasahacker.convertit.domain.model.ConvertitDarkPreview
import com.nasahacker.convertit.domain.model.ConvertitLightPreview
import com.nasahacker.convertit.service.ConvertItService
import com.nasahacker.convertit.ui.component.AudioItem
import com.nasahacker.convertit.ui.component.DialogConvertAlertDialog
import com.nasahacker.convertit.ui.component.DialogEditMetadata
import com.nasahacker.convertit.ui.component.ExpandableFab
import com.nasahacker.convertit.ui.component.RatingDialog
import com.nasahacker.convertit.util.IntentLauncher

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

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val intentLauncher = remember { IntentLauncher(context as Activity) }
    val uriList by viewModel.uriList.collectAsStateWithLifecycle()
    val metadataUri by viewModel.metadataUri.collectAsStateWithLifecycle()
    val conversionStatus by viewModel.conversionStatus.collectAsStateWithLifecycle()
    val selectedCustomLocation by viewModel.selectedCustomLocation.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showMetadataDialog by rememberSaveable { mutableStateOf(false) }
    var showReviewDialog by rememberSaveable { mutableStateOf(false) }

    val pickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateUriList(result.data)
                showDialog = true
            }
        }

    val videoPickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateUriList(result.data)
                showDialog = true
            }
        }

    val folderPickLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                        )
                        viewModel.onSelectedCustomLocation(uri.toString())
                        
                        
                        val displayName = when {
                            uri.scheme == "content" && uri.path?.contains("/tree/primary:") == true -> {
                                uri.lastPathSegment?.substringAfterLast(':')?.replace("%2F", "/") ?: "Custom folder"
                            }
                            uri.scheme == "content" -> {
                                uri.lastPathSegment?.substringAfterLast(':') ?: "Custom folder"
                            }
                            else -> {
                                uri.lastPathSegment ?: "Custom folder"
                            }
                        }
                        
                        Toast
                            .makeText(context, "Save location updated to: $displayName", Toast.LENGTH_LONG)
                            .show()
                    } catch (e: Exception) {
                        Toast
                            .makeText(context, "Failed to set custom location", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

    val metadataPickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateMetadataUri(result.data)
                showMetadataDialog = true
            }
        }

    val isDontShowAgain by viewModel.isDontShowAgain.collectAsStateWithLifecycle()
    val shouldShowReviewDialog =
        remember(showReviewDialog, isDontShowAgain) {
            showReviewDialog && !isDontShowAgain
        }

    LaunchedEffect(conversionStatus) {
        conversionStatus?.let { isSuccess ->
            if (isSuccess) {
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.label_conversion_successful),
                        Toast.LENGTH_SHORT,
                    ).show()
                viewModel.clearUriList()
                viewModel.resetConversionStatus()
                showReviewDialog = true
            }
        }
    }

    RatingDialog(
        showReviewDialog = shouldShowReviewDialog,
        dontShowAgainInitially = isDontShowAgain,
        onSaveDontShowAgain = { checked -> viewModel.onIsDontShowAgainSelected(checked) },
        onConfirm = { showReviewDialog = false },
        onDismiss = { showReviewDialog = false },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            items(uriList) { uri ->
                val file = viewModel.getFileFromUri(uri)
                AudioItem(
                    fileName = file?.name.orEmpty(),
                    fileSize =
                        file?.let { viewModel.getReadableFileSize(it) }
                            ?: stringResource(R.string.label_unknown),
                    format = file!!.extension,
                )
            }
        }

        ExpandableFab(
            onEditMetadataClick = {
                intentLauncher.openMetadataEditorFilePicker(metadataPickFileLauncher)
            },
            onConvertAudioClick = {
                if (ConvertItService.isForegroundServiceStarted) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.label_warning),
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    intentLauncher.openFilePicker(pickFileLauncher)
                }
            },
            onConvertVideoClick = {
                if (ConvertItService.isForegroundServiceStarted) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.label_warning),
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    intentLauncher.openVideoFilePicker(videoPickFileLauncher)
                }
            },
            onCustomSaveLocationClick = {
                android.util.Log.d("HomeScreen", "Raw current location: '$selectedCustomLocation'")
                
                val displayLocation = when {
                    selectedCustomLocation.isBlank() -> "Default (Music/ConvertIt)"
                    selectedCustomLocation.startsWith("content://") -> {
                   
                        val parsed = Uri.parse(selectedCustomLocation)
                        val displayName = parsed.lastPathSegment?.substringAfterLast(':')?.replace("%2F", "/") ?: "Custom folder"
                        android.util.Log.d("HomeScreen", "Content URI parsed to: '$displayName'")
                        displayName
                    }
                    selectedCustomLocation.startsWith("/") -> {
               
                        val folderName = File(selectedCustomLocation).name.takeIf { it.isNotBlank() } ?: "ConvertIt"
                        android.util.Log.d("HomeScreen", "File path parsed to: '$folderName'")
                        folderName
                    }
                    else -> {
                        android.util.Log.d("HomeScreen", "Using raw location: '$selectedCustomLocation'")
                        selectedCustomLocation
                    }
                }
                android.util.Log.d("HomeScreen", "Final display location: '$displayLocation'")
                Toast.makeText(context, "Current: $displayLocation", Toast.LENGTH_LONG).show()
                intentLauncher.openFolderPicker(folderPickLauncher)
            },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(26.dp),
        )
    }

    DialogConvertAlertDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onCancel = {
            viewModel.clearUriList()
            showDialog = false
        },
        uris = uriList,
        onStartConversion = { speed, uris, bitrate, format ->
            viewModel.startConversion(speed, uris, bitrate, format)
        },
    )

    DialogEditMetadata(
        showDialog = showMetadataDialog,
        audioUri = metadataUri,
        onDismissRequest = {
            showMetadataDialog = false
            viewModel.setMetadataUri(null)
        },
        onMetadataSaved = {},
        onLoadMetadata = { uri -> viewModel.loadMetadata(uri) },
        onSaveMetadata = { uri, metadata -> viewModel.saveMetadata(uri, metadata) },
        onSaveCoverArt = { uri, bitmap -> viewModel.saveCoverArt(uri, bitmap) },
    )
}

@ConvertitLightPreview
@ConvertitDarkPreview
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}
