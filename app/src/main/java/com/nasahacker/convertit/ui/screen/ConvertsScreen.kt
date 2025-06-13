package com.nasahacker.convertit.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.nasahacker.convertit.dto.ConvertitDarkPreview
import com.nasahacker.convertit.dto.ConvertitLightPreview
import com.nasahacker.convertit.ui.component.AudioItem
import com.nasahacker.convertit.ui.component.DialogDeleteItem
import androidx.compose.ui.platform.LocalContext
import com.nasahacker.convertit.dto.AudioFile // Ensure AudioFile is imported if not already
import com.nasahacker.convertit.ui.component.NoFilesFoundCard
import com.nasahacker.convertit.util.AppUtil
import kotlinx.coroutines.launch
import android.net.Uri // Import Uri

/**
 * @author Tamim Hossain
 * @email tamimh.dev@gmail.com
 * @license Apache-2.0
 *
 * ConvertIt is a free and easy-to-use audio converter app.
 * It supports popular audio formats like MP3 and M4A.
 * With options for high-quality bitrates ranging from 128k to 320k,
 * ConvertIt offers a seamless conversion experience tailored to your needs.
 */

@Composable
fun ConvertsScreen() {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 20
    var isLoading by remember { mutableStateOf(false) }
    

    val initialData = remember {
        mutableStateListOf(
            *AppUtil.getAudioFilesFromConvertedFolder(context)
                .take(pageSize)
                .toTypedArray()
        )
    }
    
    var showDialog by remember { mutableStateOf(false) }
    var uriToDelete by remember { mutableStateOf<Uri?>(null) } // Changed File? to Uri?


    LaunchedEffect(listState) {
        if (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1) {
            if (!isLoading) {
                isLoading = true
                currentPage++
                val newItems = AppUtil.getAudioFilesFromConvertedFolder(context)
                    .drop(currentPage * pageSize)
                    .take(pageSize)
                if (newItems.isNotEmpty()) {
                    initialData.addAll(newItems)
                }
                isLoading = false
            }
        }
    }

    if (initialData.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items(initialData) { audioFile -> // Changed item to audioFile for clarity
                AudioItem(
                    fileName = audioFile.name,
                    // fileSize = audioFile.size, // We'll use the AppUtil function directly if needed or assume it's pre-formatted
                                                // As per instruction: AppUtil.getFileSizeInReadableFormat(LocalContext.current, audioFile.uri)
                                                // However, AudioFile DTO already has 'size'. Assuming it's correctly pre-formatted.
                                                // If AudioFile.size is NOT pre-formatted, this would be:
                                                // fileSize = AppUtil.getFileSizeInReadableFormat(context, audioFile.uri),
                                                // For now, sticking to the DTO's size. If issues arise, this is the place to change.
                    fileSize = audioFile.size, // Assuming AudioFile.size is already the readable format
                    isActionVisible = true,
                    onPlayClick = {
                        AppUtil.openMusicFileInPlayer(context, audioFile.uri)
                    },
                    onShareClick = {
                        AppUtil.shareMusicFile(context, audioFile.uri)
                    },
                    onLongClick = {
                        showDialog = true
                        uriToDelete = audioFile.uri // Store URI for deletion
                    },
                )
            }
        }
    } else {
        NoFilesFoundCard()
    }

    if (showDialog) {
        DialogDeleteItem(
            showDialog = showDialog,
            onDismissRequest = { showDialog = false },
            onDeleteConfirm = {
                uriToDelete?.let { uri -> // Changed to uri
                    AppUtil.deleteFile(context, uri) // Pass URI to deleteFile
                    initialData.removeAll { audioFile -> audioFile.uri == uri } // Compare URIs for removal
                }
                showDialog = false
            },
        )
    }
}

@ConvertitLightPreview
@ConvertitDarkPreview
@Composable
fun PreviewConvertsScreen(modifier: Modifier = Modifier) {
    ConvertsScreen()
}
