package com.nasahacker.convertit.ui.component

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.nasahacker.convertit.util.AppUtil

@Composable
fun DialogConvertAlertDialog(
    showDialog: Boolean, uris: ArrayList<Uri>, onDismiss: () -> Unit, onCancel: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(".mp3") }
    var selectedBitrate by remember { mutableStateOf("256k") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = "Conversion Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            },
            text = {
                DialogConvertContent(selectedFormat = selectedFormat,
                    onFormatSelected = { selectedFormat = it },
                    selectedBitrate = selectedBitrate,
                    onBitrateSelected = { selectedBitrate = it })
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d(
                            "ZERO_DOLLAR",
                            "DialogConvertAlertDialog: Starting Service with Format $selectedFormat And  Bitrate $selectedBitrate"
                        )
                        AppUtil.startAudioConvertService(
                            uris, selectedBitrate, selectedFormat
                        )
                        onDismiss()
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Convert")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onCancel() }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
        )
    }
}

@Composable
fun DialogConvertContent(
    selectedFormat: String,
    onFormatSelected: (String) -> Unit,
    selectedBitrate: String,
    onBitrateSelected: (String) -> Unit
) {
    var isBitrateMenuExpanded by remember { mutableStateOf(false) }
    var isFormatMenuExpanded by remember { mutableStateOf(false) }

    val bitrateOptions = listOf("128k", "192k", "256k", "320k")
    val formatOptions = listOf(".mp3", ".m4a", ".wav", ".aac", ".ogg")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DropdownField(label = "Bitrate Options",
            options = bitrateOptions,
            selectedOption = selectedBitrate,
            onOptionSelected = onBitrateSelected,
            isExpanded = isBitrateMenuExpanded,
            onExpandedChange = { isBitrateMenuExpanded = it })

        DropdownField(label = "Format Options",
            options = formatOptions,
            selectedOption = selectedFormat,
            onOptionSelected = onFormatSelected,
            isExpanded = isFormatMenuExpanded,
            onExpandedChange = { isFormatMenuExpanded = it })
    }
}

@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandedChange(true) }
            .padding(vertical = 8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = selectedOption,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = if (selectedOption == "Select Bitrate" || selectedOption == "Select Format") MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            DropdownMenu(expanded = isExpanded, onDismissRequest = { onExpandedChange(false) }) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        onOptionSelected(option)
                        onExpandedChange(false)
                    }, text = { Text(text = option) })
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDialogConvertAlertDialog() {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        DialogConvertAlertDialog(
            showDialog = showDialog,
            uris = ArrayList<Uri>(),
            onCancel = { showDialog = false },
            onDismiss = {  },
        )
    }
}
