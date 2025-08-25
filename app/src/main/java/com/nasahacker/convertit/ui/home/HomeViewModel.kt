package com.nasahacker.convertit.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.App
import com.nasahacker.convertit.domain.model.AudioBitrate
import com.nasahacker.convertit.domain.model.AudioFormat
import com.nasahacker.convertit.domain.model.Metadata
import com.nasahacker.convertit.domain.repository.FileAccessRepository
import com.nasahacker.convertit.domain.usecase.GetDontShowAgainUseCase
import com.nasahacker.convertit.domain.usecase.GetSelectedCustomLocationUseCase
import com.nasahacker.convertit.domain.usecase.LoadMetadataUseCase
import com.nasahacker.convertit.domain.usecase.SaveDontShowAgainUseCase
import com.nasahacker.convertit.domain.usecase.SaveMetadataUseCase
import com.nasahacker.convertit.domain.usecase.SaveSelectedCustomLocationUseCase
import com.nasahacker.convertit.domain.usecase.StartAudioConversionUseCase
import com.nasahacker.convertit.domain.repository.AudioConverterRepository
import com.nasahacker.convertit.service.ConvertItService
import com.nasahacker.convertit.util.AppConfig
import com.nasahacker.convertit.util.AppUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

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

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val fileAccessRepository: FileAccessRepository,
        private val getDontShowAgain: GetDontShowAgainUseCase,
        private val getSelectedCustomLocation: GetSelectedCustomLocationUseCase,
        private val saveDontShowAgain: SaveDontShowAgainUseCase,
        private val saveSelectedCustomLocation: SaveSelectedCustomLocationUseCase,
        private val startAudioConversion: StartAudioConversionUseCase,
        private val loadMetadata: LoadMetadataUseCase,
        private val saveMetadata: SaveMetadataUseCase,
        private val audioConverterRepository: AudioConverterRepository,
    ) : ViewModel() {
        private val _uriList = MutableStateFlow<ArrayList<Uri>>(ArrayList())
        val uriList: StateFlow<ArrayList<Uri>> = _uriList

        private val _metadataUri = MutableStateFlow<Uri?>(null)
        val metadataUri: StateFlow<Uri?> = _metadataUri

        private val _conversionStatus = MutableStateFlow<Boolean?>(null)
        val conversionStatus: StateFlow<Boolean?> = _conversionStatus

        private val _isConversionInProgress = MutableStateFlow(false)
        val isConversionInProgress: StateFlow<Boolean> = _isConversionInProgress

        private val _conversionProgress = MutableStateFlow(0)
        val conversionProgress: StateFlow<Int> = _conversionProgress

        val isDontShowAgain: StateFlow<Boolean> =
            getDontShowAgain().stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                false,
            )

        val selectedCustomLocation: StateFlow<String> =
            getSelectedCustomLocation().stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                "",
            )

        fun onIsDontShowAgainSelected(value: Boolean) {
            viewModelScope.launch { saveDontShowAgain(value) }
        }

        fun onSelectedCustomLocation(value: String) {
            Log.d("HACKER", "onSelectedCustomLocation called with: '$value'")
            Log.d("HACKER", "Current selectedCustomLocation.value: '${selectedCustomLocation.value}'")
            viewModelScope.launch { 
                Log.d("HACKER", "About to save custom location: '$value'")
                saveSelectedCustomLocation(value)
                Log.d("HACKER", "Save operation completed")
            }
        }

        private val conversionStatusReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    val isSuccess = intent?.getBooleanExtra(AppConfig.IS_SUCCESS, false) == true
                    viewModelScope.launch {
                        _conversionStatus.value = isSuccess
                        _isConversionInProgress.value = false
                        if (isSuccess) {
                            _conversionProgress.value = 100
                        } else {
                            _conversionProgress.value = 0
                        }
                        clearUriList()
                    }
                }
            }

        fun resetConversionStatus() {
            viewModelScope.launch {
                _conversionStatus.value = null
            }
        }

        init {
            startListeningForBroadcasts()
        }

        fun updateMetadataUri(intent: Intent?) {
            viewModelScope.launch {
                intent?.let {
                    _metadataUri.emit(it.data)
                }
            }
        }

        fun setMetadataUri(uri: Uri?) {
            viewModelScope.launch {
                _metadataUri.emit(uri)
            }
        }

        fun updateUriList(intent: Intent?) {
            viewModelScope.launch {
                intent?.let {
                    val uris = AppUtil.getUriListFromIntent(it)
                    if (uris.isNotEmpty()) {
                        val updatedList = ArrayList(_uriList.value).apply { addAll(uris) }
                        _uriList.value = updatedList
                    }
                }
            }
        }

        private fun startListeningForBroadcasts() {
            val intentFilter = IntentFilter(AppConfig.CONVERT_BROADCAST_ACTION)
            ContextCompat.registerReceiver(
                App.application,
                conversionStatusReceiver,
                intentFilter,
                AppUtil.receiverFlags(),
            )
        }

        fun clearUriList() {
            viewModelScope.launch {
                _uriList.value = ArrayList()
            }
        }

        fun getFileFromUri(uri: Uri): File? = fileAccessRepository.getFileFromUri(uri)

        fun getReadableFileSize(file: File): String = fileAccessRepository.getReadableFileSize(file)

        fun startConversion(
            speed: String,
            uris: ArrayList<Uri>,
            bitrate: String,
            format: String,
        ) {
            viewModelScope.launch {
                _isConversionInProgress.value = true
                _conversionProgress.value = 0

                val audioFormat = AudioFormat.fromExtension(format)
                val audioBitrate = AudioBitrate.fromBitrate(bitrate)

                startAudioConversion(
                    customSaveUri = null,
                    playbackSpeed = speed,
                    uris = uris.toList(),
                    outputFormat = audioFormat,
                    bitrate = audioBitrate,
                    onSuccess = { convertedFiles ->
                        // This will be handled by the broadcast receiver
                        // The service will broadcast the result
                    },
                    onFailure = { error ->
                        // This will be handled by the broadcast receiver
                        // The service will broadcast the result
                    },
                    onProgress = { progress ->
                        _conversionProgress.value = progress
                    },
                )
            }
        }

        fun startConversionWithCue(
            speed: String,
            audioUri: Uri,
            cueUri: Uri,
            bitrate: String,
            format: String,
        ) {
            viewModelScope.launch {
                _isConversionInProgress.value = true
                _conversionProgress.value = 0

                startCueConversionService(arrayListOf(audioUri), bitrate, speed, format, cueUri)
            }
        }

        private fun startCueConversionService(
            uris: ArrayList<Uri>,
            bitrate: String,
            playbackSpeed: String,
            outputFormat: String,
            cueUri: Uri,
        ) {
            Log.d(
                "HomeViewModel",
                "Starting CUE conversion service with the following details:\n" + 
                "URI List Size: ${uris.size}\n" +
                "Bitrate: $bitrate\n" +
                "Format: $outputFormat\n" +
                "CUE URI: ${cueUri.lastPathSegment}",
            )

            val intent =
                Intent(App.application, ConvertItService::class.java).apply {
                    putParcelableArrayListExtra(AppConfig.URI_LIST, uris)
                    putExtra(AppConfig.BITRATE, bitrate)
                    putExtra(AppConfig.AUDIO_PLAYBACK_SPEED, playbackSpeed)
                    putExtra(AppConfig.AUDIO_FORMAT, outputFormat)
                    putExtra(AppConfig.CUE_URI, cueUri)
                }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                Log.d("HomeViewModel", "Starting foreground service for CUE conversion...")
                App.application.startForegroundService(intent)
            } else {
                Log.d("HomeViewModel", "Starting regular service for CUE conversion...")
                App.application.startService(intent)
            }
        }

        suspend fun loadMetadata(audioUri: Uri): Metadata = loadMetadata.invoke(audioUri)

        suspend fun saveMetadata(
            audioUri: Uri,
            metadata: Metadata,
        ): Boolean = saveMetadata.invoke(audioUri, metadata)

        suspend fun saveCoverArt(
            audioUri: Uri,
            bitmap: Bitmap?,
        ): Boolean = saveMetadata.invoke(audioUri, bitmap)

        override fun onCleared() {
            super.onCleared()
            App.application.unregisterReceiver(conversionStatusReceiver)
        }
    }
