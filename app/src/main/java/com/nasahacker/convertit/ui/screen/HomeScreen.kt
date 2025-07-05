package com.nasahacker.convertit.ui.screen

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nasahacker.convertit.R
import com.nasahacker.convertit.service.ConvertItService
import com.nasahacker.convertit.ui.component.AudioItem
import com.nasahacker.convertit.ui.component.DialogConvertAlertDialog
import com.nasahacker.convertit.ui.component.RatingDialog
import com.nasahacker.convertit.ui.viewmodel.AppViewModel
import com.nasahacker.convertit.util.AppUtil
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nasahacker.convertit.dto.ConvertitDarkPreview
import com.nasahacker.convertit.dto.ConvertitLightPreview
import com.nasahacker.convertit.ui.component.DialogEditMetadata
import com.nasahacker.convertit.ui.component.ExpandableFab

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
fun HomeScreen(
    viewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    val uriList by viewModel.uriList.collectAsStateWithLifecycle()
    val metadataUri by viewModel.metadataUri.collectAsStateWithLifecycle()
    val conversionStatus by viewModel.conversionStatus.collectAsStateWithLifecycle()
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
                Toast.makeText(context, "Custom save location feature coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

    val metadataPickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateMetadataUri(result.data)
                showMetadataDialog = true
            }
        }

    LaunchedEffect(conversionStatus) {
        conversionStatus?.let { isSuccess ->
            if (isSuccess) {
                Toast.makeText(
                    context,
                    context.getString(R.string.label_conversion_successful),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearUriList()
                viewModel.resetConversionStatus()
                showReviewDialog = true
            }
        }
    }

    RatingDialog(
        showReviewDialog = showReviewDialog,
        onConfirm = { showReviewDialog = false },
        onDismiss = { showReviewDialog = false })

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(uriList) { uri ->
                val file = AppUtil.getFileFromUri(context, uri)
                AudioItem(
                    fileName = file?.name.orEmpty(),
                    fileSize = file?.let { AppUtil.getFileSizeInReadableFormat(context, it) }
                                                        ?: stringResource(R.string.label_unknown))
            }
        }

        ExpandableFab(
            onEditMetadataClick = {
                AppUtil.openMetadataEditorFilePicker(context, metadataPickFileLauncher)
            },
            onConvertAudioClick = {
                if (ConvertItService.isForegroundServiceStarted) {
                    Toast.makeText(
                        context, context.getString(R.string.label_warning), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    AppUtil.openFilePicker(context, pickFileLauncher)
                }
            },
            onConvertVideoClick = {
                if (ConvertItService.isForegroundServiceStarted) {
                    Toast.makeText(
                        context, context.getString(R.string.label_warning), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    AppUtil.openVideoFilePicker(context, videoPickFileLauncher)
                }
            },
            onCustomSaveLocationClick = {
                Toast.makeText(context, "Custom save location feature coming soon!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(26.dp)
        )


    }

    DialogConvertAlertDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onCancel = {
            viewModel.clearUriList()
            showDialog = false
        },
        uris = uriList
    )

    DialogEditMetadata(
        showDialog = showMetadataDialog,
        audioUri = metadataUri,
        onDismissRequest = { 
            showMetadataDialog = false
            viewModel.setMetadataUri(null) // Clear the URI
        },
        onMetadataSaved = {
            // Optionally refresh or update UI after metadata is saved
        }
    )
}

@ConvertitLightPreview
@ConvertitDarkPreview
@Composable
fun PreviewMainScreen() {
    HomeScreen()
}
