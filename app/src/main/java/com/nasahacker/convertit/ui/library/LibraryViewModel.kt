package com.nasahacker.convertit.ui.library

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.domain.model.AudioFile
import com.nasahacker.convertit.domain.usecase.GetConvertedAudioFilesUseCase
import com.nasahacker.convertit.domain.usecase.GetSelectedCustomLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
        val selectedCustomLocation: StateFlow<String> =
            getSelectedCustomLocationUseCase().stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                "",
            )

        // Reactive audio files that update when custom location changes
        val audioFiles: StateFlow<List<AudioFile>> =
            selectedCustomLocation.map { location ->
                android.util.Log.d("LibraryViewModel", "Loading audio files from location: '$location'")
                val uri = if (location.isNotBlank()) location.toUri() else null
                val files = getConvertedAudioFiles(uri)
                android.util.Log.d("LibraryViewModel", "Found ${files.size} audio files")
                files
            }.stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                emptyList()
            )

        // Kept for backward compatibility but now uses the reactive audioFiles
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
