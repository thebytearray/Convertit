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
    context: Activity,
    viewModel: AppViewModel = viewModel(),
) {
    val uriList by viewModel.uriList.collectAsState()
    val conversionStatus by viewModel.conversionStatus.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showReviewDialog by rememberSaveable { mutableStateOf(false) }

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
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.label_conversion_successful),
                        Toast.LENGTH_SHORT,
                    ).show()
                viewModel.clearUriList()
                viewModel.resetConversionStatus()
                showReviewDialog = true
            }
        }
    }

    RatingDialog(
        showReviewDialog = showReviewDialog,
        onConfirm = {
            showReviewDialog = false
        },
        onDismiss = { showReviewDialog = false },
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            items(uriList) { uri ->
                val file = AppUtil.getFileFromUri(context, uri)
                AudioItem(
                    fileName = file?.name.orEmpty(),
                    fileSize =
                        file?.let { AppUtil.getFileSizeInReadableFormat(context, it) }
                            ?: "Unknown",
                )
            }
        }

        FloatingActionButton(
            onClick = {
                if (ConvertItService.isForegroundServiceStarted) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.label_warning),
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    AppUtil.openFilePicker(context, pickFileLauncher)
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(26.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.audio_ic),
                contentDescription =
                    stringResource(
                        R.string.label_select_files,
                    ),
            )
        }
    }

    DialogConvertAlertDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onCancel = {
            viewModel.clearUriList()
            showDialog = false
        },
        uris = uriList,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
}
