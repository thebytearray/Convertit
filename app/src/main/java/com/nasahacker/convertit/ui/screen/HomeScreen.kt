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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nasahacker.convertit.R
import com.nasahacker.convertit.service.ConvertItService
import com.nasahacker.convertit.ui.component.AudioItem
import com.nasahacker.convertit.ui.component.DialogConvertAlertDialog
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    context: Activity,
    viewModel: HomeViewModel = viewModel()
) {

    val uriList by viewModel.uriList.collectAsState()
    val conversionStatus by viewModel.conversionStatus.collectAsState()
    var showDialog by rememberSaveable { mutableStateOf(false) }


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
                Toast.makeText(context, "Conversion successful!", Toast.LENGTH_SHORT).show()
                viewModel.clearUriList()
                viewModel.resetConversionStatus()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(uriList) { uri ->
                val file = AppUtil.getFileFromUri(context, uri)
                AudioItem(
                    fileName = file?.name.orEmpty(),
                    fileSize = file?.let { AppUtil.getFileSizeInReadableFormat(context, it) }
                        ?: "Unknown"
                )
            }
        }

        FloatingActionButton(
            onClick = {
                if (ConvertItService.isForegroundServiceStarted) {
                    Toast.makeText(
                        context,
                        "Please wait for the previous audios to be converted.",
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
                painter = painterResource(id = R.drawable.audio_ic), contentDescription = "Add Item"
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
        uris = uriList
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
}
