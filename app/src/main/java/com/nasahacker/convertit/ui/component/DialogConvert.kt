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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.nasahacker.convertit.R

/**
 * Convertit Android app
 * <a href="https://github.com/thebytearray/Convertit">GitHub Repository</a>
 *
 * Created by Tamim Hossain.
 * Copyright (c) 2025 The Byte Array LTD.
 *
 * This file is part of the Convertit Android app.
 *
 * The Convertit Android app is free software: you can redistribute it and/or
 * modify it under the terms of the Apache License, Version 2.0 as published by
 * the Apache Software Foundation.
 *
 * The Convertit Android app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Apache License for more
 * details.
 *
 * You should have received a copy of the Apache License
 * along with the Convertit Android app. If not, see <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>.
 *
 * @author Tamim Hossain
 * @company The Byte Array LTD
 * @year 2025
 * @license Apache-2.0
 */

@Composable
fun DialogConvertAlertDialog(
    showDialog: Boolean,
    uris: ArrayList<Uri>,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onStartConversion: (speed: String, uris: ArrayList<Uri>, bitrate: String, format: String) -> Unit = { _, _, _, _ -> },
    onStartConversionWithCue: (speed: String, audioUri: Uri, cueUri: Uri, bitrate: String, format: String) -> Unit = { _, _, _, _, _ -> },
    onSelectCueFile: () -> Unit = {},
    selectedCueUri: Uri? = null,
) {
    var selectedFormat by remember { mutableStateOf(".mp3") }
    var selectedBitrate by remember { mutableStateOf("256k") }
    var sliderValue by remember { mutableFloatStateOf(1.0f) }
    var enableCueSplitting by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.label_conversion_settings),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = MaterialTheme.colorScheme.primary,
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
                    enableCueSplitting = enableCueSplitting,
                    onCueSplittingChanged = { enableCueSplitting = it },
                    selectedCueUri = selectedCueUri,
                    onSelectCueFile = onSelectCueFile,
                    hasFlacOrWavFiles = uris.any { uri ->
                        val fileName = (uri.lastPathSegment ?: uri.toString()).lowercase()
                        val isFlacOrWav = fileName.contains(".flac") || fileName.contains(".wav")
                        Log.d("CueDebug", "URI: $uri, FileName: $fileName, IsFlacOrWav: $isFlacOrWav")
                        isFlacOrWav
                    }.also { hasFlac ->
                        Log.d("CueDebug", "Total URIs: ${uris.size}, HasFlacOrWav: $hasFlac")
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enableCueSplitting && selectedCueUri != null && uris.isNotEmpty()) {
                            Log.d(
                                "ZERO_DOLLAR",
                                "Starting CUE-based conversion with Format $selectedFormat And Bitrate $selectedBitrate",
                            )
                            onStartConversionWithCue(
                                sliderValue.toString(),
                                uris.first(),
                                selectedCueUri,
                                selectedBitrate,
                                selectedFormat,
                            )
                        } else {
                            Log.d(
                                "ZERO_DOLLAR",
                                "Starting Service with Format $selectedFormat And Bitrate $selectedBitrate",
                            )
                            onStartConversion(
                                sliderValue.toString(),
                                uris,
                                selectedBitrate,
                                selectedFormat,
                            )
                        }
                        onDismiss()
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_convert),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onCancel,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_cancel),
                        style = MaterialTheme.typography.labelLarge,
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
    enableCueSplitting: Boolean = false,
    onCueSplittingChanged: (Boolean) -> Unit = {},
    selectedCueUri: Uri? = null,
    onSelectCueFile: () -> Unit = {},
    hasFlacOrWavFiles: Boolean = false,
) {
    val allFormats = stringArrayResource(R.array.format_array).toList()
    val bitratesMp3 = stringArrayResource(R.array.bitrates_mp3).toList()
    val bitratesAac = stringArrayResource(R.array.bitrates_aac).toList()
    val bitratesM4a = stringArrayResource(R.array.bitrates_m4a).toList()
    val bitratesOgg = stringArrayResource(R.array.bitrates_ogg).toList()
    val bitratesOpus = stringArrayResource(R.array.bitrates_opus).toList()
    val bitratesWma = stringArrayResource(R.array.bitrates_wma).toList()
    val bitratesMka = stringArrayResource(R.array.bitrates_mka).toList()
    val bitratesSpx = stringArrayResource(R.array.bitrates_spx).toList()
    val bitratesArray = stringArrayResource(R.array.bitrates_array).toList()

    val bitrateOptions =
        remember(selectedFormat) {
            when (selectedFormat) {
                ".mp3" -> bitratesMp3
                ".aac" -> bitratesAac
                ".m4a" -> bitratesM4a
                ".ogg" -> bitratesOgg
                ".opus" -> bitratesOpus
                ".wma" -> bitratesWma
                ".mka" -> bitratesMka
                ".spx" -> bitratesSpx
                else -> bitratesArray
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AudioFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_format_options),
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    DropdownField(
                        label = "",
                        options = allFormats,
                        selectedOption = selectedFormat,
                        onOptionSelected = {
                            onFormatSelected(it)
                            val validBitrates =
                                when (it) {
                                    ".mp3" -> bitratesMp3
                                    ".aac" -> bitratesAac
                                    ".m4a" -> bitratesM4a
                                    ".ogg" -> bitratesOgg
                                    ".opus" -> bitratesOpus
                                    ".wma" -> bitratesWma
                                    ".mka" -> bitratesMka
                                    ".spx" -> bitratesSpx
                                    else -> bitratesArray
                                }
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_bitrate_options),
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.label_slider),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Slider(
                    value = sliderValue,
                    onValueChange = onSliderValueChanged,
                    valueRange = 0.5f..2.0f,
                    steps = 30,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                        ),
                )
                Text(
                    text = "Current: ${"%.2f".format(sliderValue)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }

        // Temporarily always show for debugging
        Log.d("CueDebug", "Rendering CUE section - hasFlacOrWavFiles: $hasFlacOrWavFiles")
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.label_cue_splitting),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Switch(
                        checked = enableCueSplitting,
                        onCheckedChange = onCueSplittingChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_enable_track_splitting),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (enableCueSplitting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onSelectCueFile,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = enableCueSplitting,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedCueUri != null) {
                                stringResource(R.string.label_cue_file_selected)
                            } else {
                                stringResource(R.string.label_select_cue_file)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    if (selectedCueUri != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedCueUri.lastPathSegment ?: stringResource(R.string.label_cue_file_selected),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
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
                modifier =
                    Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, true)
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
