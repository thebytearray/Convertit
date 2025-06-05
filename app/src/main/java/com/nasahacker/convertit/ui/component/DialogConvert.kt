package com.nasahacker.convertit.ui.component

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.nasahacker.convertit.R
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.util.AppConfig.BITRATE_ARRAY
import com.nasahacker.convertit.util.AppConfig.FORMAT_ARRAY
import com.nasahacker.convertit.util.AppConfig.FORMAT_BITRATE_MAP

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
    var selectedFormat by remember { mutableStateOf(".mp3") }
    var selectedBitrate by remember { mutableStateOf("256k") }
    var sliderValue by remember { mutableFloatStateOf(1.0f) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.label_conversion_settings),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_convert),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_cancel),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
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
    val bitrateOptions = remember(selectedFormat) {
        FORMAT_BITRATE_MAP[selectedFormat] ?: BITRATE_ARRAY
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_format_options),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    DropdownField(
                        label = "",
                        options = FORMAT_ARRAY,
                        selectedOption = selectedFormat,
                        onOptionSelected = {
                            onFormatSelected(it)
                            val validBitrates = FORMAT_BITRATE_MAP[it] ?: BITRATE_ARRAY
                            onBitrateSelected(validBitrates.first())
                        },
                    )
                }
            }


            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_bitrate_options),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    DropdownField(
                        label = "",
                        options = bitrateOptions,
                        selectedOption = selectedBitrate.takeIf { it in bitrateOptions } ?: bitrateOptions.first(),
                        onOptionSelected = onBitrateSelected,
                    )
                }
            }
        }


        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            shape = RoundedCornerShape(12.dp),

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.label_slider),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = sliderValue,
                    onValueChange = onSliderValueChanged,
                    valueRange = 0.5f..2.0f,
                    steps = 30,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                    )
                )
                Text(
                    text = "Current: ${"%.2f".format(sliderValue)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
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
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable,true)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
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
