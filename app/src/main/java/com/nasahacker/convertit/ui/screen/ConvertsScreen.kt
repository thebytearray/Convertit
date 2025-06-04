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
import com.nasahacker.convertit.util.AppUtil
import java.io.File
import kotlinx.coroutines.launch

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

    var currentPage by remember { mutableStateOf(0) }
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
    var fileToDelete by remember { mutableStateOf<File?>(null) }


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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        items(initialData) { item ->
            AudioItem(
                fileName = item.name,
                fileSize = item.size,
                isActionVisible = true,
                onPlayClick = {
                    AppUtil.openMusicFileInPlayer(context, item.file)
                },
                onShareClick = {
                    AppUtil.shareMusicFile(context, item.file)
                },
                onLongClick = {
                    showDialog = true
                    fileToDelete = item.file
                },
            )
        }
    }

    if (showDialog) {
        DialogDeleteItem(
            showDialog = showDialog,
            onDismissRequest = { showDialog = false },
            onDeleteConfirm = {
                fileToDelete?.let {
                    AppUtil.deleteFile(context, it)
                    initialData.removeAll { file -> file.file == it }
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
