package com.nasahacker.convertit.ui.component

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.nasahacker.convertit.R
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.util.Constant.BITRATE_ARRAY
import com.nasahacker.convertit.util.Constant.FORMAT_ARRAY

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
fun DialogConvertAlertDialog(
    showDialog: Boolean,
    uris: ArrayList<Uri>,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(".mp3") }
    var selectedBitrate by remember { mutableStateOf("256k") }
    var sliderValue by remember { mutableFloatStateOf(1.0f) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.label_conversion_settings),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            },
            text = {
                DialogConvertContent(
                    selectedFormat = selectedFormat,
                    onFormatSelected = { selectedFormat = it },
                    selectedBitrate = selectedBitrate,
                    onBitrateSelected = { selectedBitrate = it },
                    sliderValue = sliderValue,
                    onSliderValueChanged = { sliderValue = it },
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d(
                            "ZERO_DOLLAR",
                            "Starting Service with Format $selectedFormat And Bitrate $selectedBitrate",
                        )
                        AppUtil.startAudioConvertService(
                            sliderValue.toString(),
                            uris,
                            selectedBitrate,
                            selectedFormat,
                        )
                        onDismiss()
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) {
                    Text(stringResource(R.string.label_convert))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancel,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                ) {
                    Text(stringResource(R.string.label_cancel))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
        )
    }
}

@Composable
fun DialogConvertContent(
    selectedFormat: String,
    onFormatSelected: (String) -> Unit,
    selectedBitrate: String,
    onBitrateSelected: (String) -> Unit,
    sliderValue: Float,
    onSliderValueChanged: (Float) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DropdownField(
            label = stringResource(R.string.label_bitrate_options),
            options = BITRATE_ARRAY,
            selectedOption = selectedBitrate,
            onOptionSelected = onBitrateSelected,
        )

        DropdownField(
            label = stringResource(R.string.label_format_options),
            options = FORMAT_ARRAY,
            selectedOption = selectedFormat,
            onOptionSelected = onFormatSelected,
        )

        Text(
            text = stringResource(R.string.label_slider),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Slider(
            value = sliderValue,
            onValueChange = onSliderValueChanged,
            valueRange = 0.5f..2.0f,
            steps = 30,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Current: ${"%.2f".format(sliderValue)}x",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp),
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                modifier =
                    Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                    )
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
            uris = ArrayList(),
            onCancel = { showDialog = false },
            onDismiss = { },
        )
    }
}
