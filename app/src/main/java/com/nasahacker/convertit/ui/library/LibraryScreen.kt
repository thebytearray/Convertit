package com.nasahacker.convertit.ui.library

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.nasahacker.convertit.domain.model.ConvertitDarkPreview
import com.nasahacker.convertit.domain.model.ConvertitLightPreview
import com.nasahacker.convertit.ui.component.AudioItem
import com.nasahacker.convertit.ui.component.NoFilesFoundCard
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
fun LibraryScreen(viewModel: LibraryViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val intentLauncher = remember { IntentLauncher(context as Activity) }
    val audioFiles by viewModel.audioFiles.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.refreshFiles()
    }

    if (audioFiles.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(audioFiles) { item ->
                AudioItem(
                    fileName = item.name,
                    fileSize = item.size,
                    format = item.format,
                    isActionVisible = true,
                    onPlayClick = {
                        intentLauncher.openMusicFileInPlayer(item.file)
                    },
                    onShareClick = {
                        intentLauncher.shareMusicFile(item.file)
                    },
                    onLongClick = {
                    },
                )
            }
        }
    } else {
        NoFilesFoundCard()
    }
}

@ConvertitLightPreview
@ConvertitDarkPreview
@Composable
fun PreviewLibraryScreen(modifier: Modifier = Modifier) {
    LibraryScreen()
}
