package com.nasahacker.convertit.ui.screen

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ModeEditOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nasahacker.convertit.R
import com.nasahacker.convertit.service.ConvertItService
import com.nasahacker.convertit.ui.component.AudioItem
import com.nasahacker.convertit.ui.component.DialogConvertAlertDialog
import com.nasahacker.convertit.ui.component.MetadataEditorDialog
import com.nasahacker.convertit.ui.component.RatingDialog
import com.nasahacker.convertit.ui.viewmodel.AppViewModel
import com.nasahacker.convertit.util.AppUtil
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nasahacker.convertit.dto.ConvertitDarkPreview
import com.nasahacker.convertit.dto.ConvertitLightPreview

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
    var isFabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showMetadataEditorDialog by rememberSaveable { mutableStateOf(false) }

    val pickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.updateUriList(result.data)
                showDialog = true
            }
        }

    val pickSingleAudioFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.loadMetadataForEditing(uri)
                    // Toast.makeText(context, "Loading metadata for: $uri", Toast.LENGTH_SHORT).show() // Toast is now handled by dialog or error state
                    // showMetadataEditorDialog = true // This will be handled by LaunchedEffect below
                }
            }
        }

    val editingUri by viewModel.editingFileUri.collectAsState()
    val pendingIntent by viewModel.pendingIntentRequest.collectAsState()

    val intentSenderLauncher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Permission action completed. You might need to press save again.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Permission action was not completed.", Toast.LENGTH_SHORT).show()
        }
        viewModel.clearPendingIntentRequest() // Always clear the request
    }

    LaunchedEffect(pendingIntent) {
        pendingIntent?.let { intent ->
            val intentSenderRequest = IntentSenderRequest.Builder(intent.intentSender).build()
            intentSenderLauncher.launch(intentSenderRequest)
            // viewModel.clearPendingIntentRequest() // Moved to launcher's result block
        }
    }

    LaunchedEffect(editingUri) {
        if (editingUri != null) {
            showMetadataEditorDialog = true
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(uriList) { uri ->
                val file = AppUtil.getFileFromUri(context, uri)
                AudioItem(
                    fileName = file?.name.orEmpty(),
                    fileSize = file?.let { AppUtil.getFileSizeInReadableFormat(context, it) } ?: "Unknown"
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 26.dp), // Main FAB padding
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = isFabMenuExpanded,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    FloatingActionButton(
                        onClick = {
                            AppUtil.openSingleAudioFilePicker(context, pickSingleAudioFileLauncher)
                            isFabMenuExpanded = false // Close menu after action
                        },
                        modifier = Modifier.padding(bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ModeEditOutline,
                            contentDescription = "Edit Metadata"
                        )
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
                            isFabMenuExpanded = false // Close menu after action
                        },
                        modifier = Modifier.padding(bottom = 16.dp), // Space between mini FABs and main FAB
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.audio_ic),
                            contentDescription = stringResource(R.string.label_select_files)
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    isFabMenuExpanded = !isFabMenuExpanded
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isFabMenuExpanded) 45f else 0f,
                    label = "FAB Rotation"
                )
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (isFabMenuExpanded) "Close Menu" else "Open Menu",
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }
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

    if (showMetadataEditorDialog && editingUri != null) {
        MetadataEditorDialog(
            viewModel = viewModel,
            onDismissRequest = {
                showMetadataEditorDialog = false
                viewModel.clearEditingState() // Important to clear when dialog is dismissed
            }
        )
    }
}

@ConvertitLightPreview
@ConvertitDarkPreview
@Composable
fun PreviewMainScreen() {
    HomeScreen()
}
