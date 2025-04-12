package com.nasahacker.convertit.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.nasahacker.convertit.ui.component.AudioItem
import com.nasahacker.convertit.ui.component.DialogDeleteItem
import com.nasahacker.convertit.util.AppUtil
import java.io.File

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
    val data =
        remember {
            mutableStateListOf(
                *AppUtil.getAudioFilesFromConvertedFolder(context).toTypedArray(),
            )
        }
    var showDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(data) { item ->
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
                    data.removeAll { file -> file.file == it }
                }
                showDialog = false
            },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewConvertsScreen(modifier: Modifier = Modifier) {
    ConvertsScreen()
}
