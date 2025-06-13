package com.nasahacker.convertit.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nasahacker.convertit.dto.ConvertitDarkPreview
import com.nasahacker.convertit.dto.ConvertitLightPreview
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button // Added for new button, or IconButton
import androidx.compose.material3.IconButton
import com.nasahacker.convertit.dto.AudioFileMetadata
import com.nasahacker.convertit.ui.component.MetadataEditDialog
import com.nasahacker.convertit.util.ImageUtil
import com.nasahacker.convertit.util.MetadataUtil
import com.nasahacker.convertit.util.toImageBitmap // Import the helper
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.ImageBitmap


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
    val conversionStatus by viewModel.conversionStatus.collectAsStateWithLifecycle()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showReviewDialog by rememberSaveable { mutableStateOf(false) }

    // State variables for metadata editing
    var showMetadataDialog by remember { mutableStateOf(false) }
    var editingAudioUri by remember { mutableStateOf<Uri?>(null) }
    var currentMetadata by remember { mutableStateOf<AudioFileMetadata?>(null) }
    var selectedCoverArtBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedCoverArtBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedCoverArtMimeType by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Launcher for picking a single audio file for metadata editing
    val singleAudioFilePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                editingAudioUri.value = it
                scope.launch {
                    val metadata = MetadataUtil.readMetadata(context, it)
                    if (metadata != null) {
                        currentMetadata.value = metadata
                        selectedCoverArtBytes.value = metadata.coverArt
                        selectedCoverArtMimeType.value = metadata.coverArtMimeType
                        selectedCoverArtBitmap.value = metadata.coverArt.toImageBitmap() // Uses helper from ImageUtil
                        showMetadataDialog.value = true
                    } else {
                        Toast.makeText(context, "Failed to read metadata.", Toast.LENGTH_SHORT).show()
                        // Reset states if metadata read fails
                        editingAudioUri.value = null
                        currentMetadata.value = null
                        selectedCoverArtBitmap.value = null
                        selectedCoverArtBytes.value = null
                        selectedCoverArtMimeType.value = null
                    }
                }
            }
        }

    // Launcher for picking an image for cover art
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { imageUri: Uri? ->
            imageUri?.let {
                scope.launch {
                    val processedArt = ImageUtil.processSelectedCoverImage(context, it)
                    if (processedArt != null) {
                        selectedCoverArtBytes.value = processedArt.first
                        selectedCoverArtMimeType.value = processedArt.second
                        selectedCoverArtBitmap.value = processedArt.first.toImageBitmap() // Uses helper
                    } else {
                        Toast.makeText(context, "Failed to process cover art.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    val pickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateUriList(result.data)
                showDialog = true
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
        onDismiss = { showReviewDialog = false }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column( // Main content column
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally // Center buttons
        ) {
            // Edit Metadata Button
            Button(
                onClick = { AppUtil.openSingleAudioFilePicker(context, singleAudioFilePickerLauncher) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Metadata Icon", modifier = Modifier.padding(end = 8.dp))
                Text("Edit Audio Metadata")
            }

            Spacer(modifier = Modifier.height(8.dp)) // Spacer between buttons

            // Existing LazyColumn for picked files for conversion
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(), // Takes remaining space
                contentPadding = PaddingValues(bottom = 80.dp) // Keep padding for FAB
            ) {
                items(uriList) { uri ->
                    val file = AppUtil.getFileFromUri(context, uri)
                    AudioItem(
                        fileName = file?.name.orEmpty(),
                        fileSize = file?.let { AppUtil.getFileSizeInReadableFormat(context, it.toUri()) } ?: "Unknown"
                        // Make sure getFileSizeInReadableFormat takes Uri or adapt 'it'
                    )
                }
            }
        }


        FloatingActionButton(
            onClick = {
                if (ConvertItService.isForegroundServiceStarted) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.label_warning),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    AppUtil.openFilePicker(context, pickFileLauncher)
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(26.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.audio_ic),
                contentDescription = stringResource(R.string.label_select_files)
            )
        }
    }

    if (showMetadataDialog.value && currentMetadata.value != null && editingAudioUri.value != null) {
        MetadataEditDialog(
            originalMetadata = currentMetadata.value!!,
            currentCoverArtBitmap = selectedCoverArtBitmap.value,
            onDismissRequest = {
                showMetadataDialog.value = false
                editingAudioUri.value = null
                currentMetadata.value = null
                selectedCoverArtBitmap.value = null
                selectedCoverArtBytes.value = null
                selectedCoverArtMimeType.value = null
            },
            onChangeCoverArtClick = { AppUtil.openImagePicker(context, imagePickerLauncher) },
            onSave = { editedTextData ->
                val audioUriToSave = editingAudioUri.value
                if (audioUriToSave == null) {
                    Toast.makeText(context, "Error: No audio file URI available for saving.", Toast.LENGTH_SHORT).show()
                    showMetadataDialog.value = false // Close dialog
                    // Reset states
                    editingAudioUri.value = null
                    currentMetadata.value = null
                    selectedCoverArtBitmap.value = null
                    selectedCoverArtBytes.value = null
                    selectedCoverArtMimeType.value = null
                    // return@MetadataEditDialog // Not directly in lambda scope, handled by if
                } else {
                    val finalMetadataToSave = editedTextData.copy(
                        coverArt = selectedCoverArtBytes.value,
                        coverArtMimeType = selectedCoverArtMimeType.value
                    )

                    scope.launch {
                        val success = MetadataUtil.writeMetadata(context, audioUriToSave, finalMetadataToSave)
                        if (success) {
                            Toast.makeText(context, "Metadata saved successfully.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to save metadata.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Close dialog and reset states after initiating the save operation.
                    showMetadataDialog.value = false
                    editingAudioUri.value = null
                    currentMetadata.value = null
                    selectedCoverArtBitmap.value = null
                    selectedCoverArtBytes.value = null
                    selectedCoverArtMimeType.value = null
                }
            }
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
}

// Small fix for AudioItem call, assuming AppUtil.getFileFromUri returns File, and getFileSizeInReadableFormat needs Uri
// If getFileFromUri is changed to return Uri, this is simpler.
// For now, assuming getFileFromUri still returns File and getFileSizeInReadableFormat takes Uri.
// So, if file is not null, then file.toUri() can be used.
// AppUtil.getFileSizeInReadableFormat(context, it.toUri()) was added above.

@ConvertitLightPreview
@ConvertitDarkPreview
@Composable
fun PreviewMainScreen() {
    HomeScreen()
}
