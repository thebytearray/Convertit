package com.nasahacker.convertit.ui.library

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.domain.model.AudioFile
import com.nasahacker.convertit.domain.usecase.GetConvertedAudioFilesUseCase
import com.nasahacker.convertit.domain.usecase.GetSelectedCustomLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
class LibraryViewModel
    @Inject
    constructor(
        private val getConvertedAudioFiles: GetConvertedAudioFilesUseCase,
        private val getSelectedCustomLocationUseCase: GetSelectedCustomLocationUseCase,
    ) : ViewModel() {
        
        companion object {
            private const val TAG = "LibraryViewModel"
        }
        val selectedCustomLocation: StateFlow<String> =
            getSelectedCustomLocationUseCase().stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                "",
            )

        private val _refreshTrigger = MutableStateFlow(0L)
        
        val audioFiles: StateFlow<List<AudioFile>> =
            combine(selectedCustomLocation, _refreshTrigger) { location, trigger ->
                Log.d(TAG, "Loading audio files from location: '$location' (trigger: $trigger)")
                val uri = if (location.isNotBlank()) {
                    val parsedUri = location.toUri()
                    Log.d(TAG, "Parsed URI: $parsedUri (scheme: ${parsedUri.scheme}, path: ${parsedUri.path})")
                    parsedUri
                } else {
                    Log.d(TAG, "Using default location (null URI)")
                    null
                }
                val files = getConvertedAudioFiles(uri)
                Log.d(TAG, "Found ${files.size} audio files")
                if (files.isEmpty()) {
                    Log.w(TAG, "No files found - check if directory exists and has audio files")
                }
                files
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )
        
        // Function to manually refresh the file list
        fun refreshFiles() {
            Log.d(TAG, "Manual refresh triggered")
            _refreshTrigger.value = System.currentTimeMillis()
        }

        fun getInitialAudioFiles(pageSize: Int): List<AudioFile> {
            return audioFiles.value.take(pageSize)
        }

        fun getAudioFilesPage(
            page: Int,
            pageSize: Int,
        ): List<AudioFile> {
            return audioFiles.value
                .drop(page * pageSize)
                .take(pageSize)
        }
    }
