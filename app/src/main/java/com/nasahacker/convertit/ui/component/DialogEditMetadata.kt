package com.nasahacker.convertit.ui.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nasahacker.convertit.R
import com.nasahacker.convertit.domain.model.Metadata
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogEditMetadata(
    showDialog: Boolean,
    audioUri: Uri?,
    onDismissRequest: () -> Unit,
    onMetadataSaved: () -> Unit = {},
    onLoadMetadata: suspend (Uri) -> Metadata = { Metadata() },
    onSaveMetadata: suspend (Uri, Metadata) -> Boolean = { _, _ -> false },
    onSaveCoverArt: suspend (Uri, Bitmap?) -> Boolean = { _, _ -> false },
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var metadata by remember { mutableStateOf(Metadata()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var titleText by remember { mutableStateOf("") }
    var artistText by remember { mutableStateOf("") }
    var albumText by remember { mutableStateOf("") }
    var albumArtistText by remember { mutableStateOf("") }
    var genreText by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("") }
    var trackText by remember { mutableStateOf("") }
    var commentText by remember { mutableStateOf("") }

    var currentCoverArt by remember { mutableStateOf<Bitmap?>(null) }
    var newCoverArt by remember { mutableStateOf<Bitmap?>(null) }
    var hasCoverArtChanged by remember { mutableStateOf(false) }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                try {
                    val mimeType = context.contentResolver.getType(uri)
                    if (mimeType?.startsWith("image/") == true) {
                        val bitmap =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                            } else {
                                @Suppress("DEPRECATION")
                                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                            }
                        newCoverArt = bitmap
                        hasCoverArtChanged = true
                    } else {
                        Toast.makeText(context, context.getString(R.string.label_please_select_image_only), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.label_failed_to_load_image, e.message ?: ""),
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    LaunchedEffect(showDialog, audioUri) {
        if (showDialog && audioUri != null) {
            isLoading = true
            coroutineScope.launch {
                try {
                    val loadedMetadata = onLoadMetadata(audioUri)
                    metadata = loadedMetadata

                    titleText = loadedMetadata.title
                    artistText = loadedMetadata.artist
                    albumText = loadedMetadata.album
                    albumArtistText = loadedMetadata.albumArtist
                    genreText = loadedMetadata.genre
                    yearText = loadedMetadata.year
                    trackText = loadedMetadata.track
                    commentText = loadedMetadata.comment

                    currentCoverArt =
                        loadedMetadata.pictures.firstOrNull()?.let { picture ->
                            BitmapFactory.decodeByteArray(picture.data, 0, picture.data.size)
                        }
                    newCoverArt = null
                    hasCoverArtChanged = false
                } catch (e: Exception) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.label_failed_to_load_metadata, e.message ?: ""),
                            Toast.LENGTH_SHORT,
                        ).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.label_edit_metadata),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            text = {
                if (isLoading) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                                .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                ),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(R.string.label_cover_art),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )

                                val displayBitmap = newCoverArt ?: currentCoverArt

                                if (displayBitmap != null) {
                                    Image(
                                        bitmap = displayBitmap.asImageBitmap(),
                                        contentDescription = stringResource(R.string.label_cover_art),
                                        modifier =
                                            Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(8.dp),
                                                ),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(8.dp),
                                                ).background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = stringResource(R.string.label_no_cover_art),
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            imagePickerLauncher.launch("image/*")
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(
                                            imageVector = if (displayBitmap != null) Icons.Default.Edit else Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(if (displayBitmap != null) R.string.label_change else R.string.label_add))
                                    }

                                    if (displayBitmap != null) {
                                        OutlinedButton(
                                            onClick = {
                                                currentCoverArt = null
                                                newCoverArt = null
                                                hasCoverArtChanged = true
                                            },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text(stringResource(R.string.label_remove))
                                        }
                                    }
                                }

                                if (hasCoverArtChanged) {
                                    Text(
                                        text = stringResource(R.string.label_cover_art_will_be_updated),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            label = { Text(stringResource(R.string.label_title)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        OutlinedTextField(
                            value = artistText,
                            onValueChange = { artistText = it },
                            label = { Text(stringResource(R.string.label_artist)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        OutlinedTextField(
                            value = albumText,
                            onValueChange = { albumText = it },
                            label = { Text(stringResource(R.string.label_album)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        OutlinedTextField(
                            value = albumArtistText,
                            onValueChange = { albumArtistText = it },
                            label = { Text(stringResource(R.string.label_album_artist)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        OutlinedTextField(
                            value = genreText,
                            onValueChange = { genreText = it },
                            label = { Text(stringResource(R.string.label_genre)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        OutlinedTextField(
                            value = yearText,
                            onValueChange = { yearText = it },
                            label = { Text(stringResource(R.string.label_year)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        OutlinedTextField(
                            value = trackText,
                            onValueChange = { trackText = it },
                            label = { Text(stringResource(R.string.label_track_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text(stringResource(R.string.label_comment)) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (audioUri != null) {
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val updatedMetadata =
                                        Metadata(
                                            title = titleText,
                                            artist = artistText,
                                            album = albumText,
                                            albumArtist = albumArtistText,
                                            genre = genreText,
                                            year = yearText,
                                            track = trackText,
                                            comment = commentText,
                                        )

                                    var success = onSaveMetadata(audioUri, updatedMetadata)

                                    if (success && hasCoverArtChanged) {
                                        success = onSaveCoverArt(audioUri, newCoverArt)
                                    }

                                    if (success) {
                                        Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.label_metadata_saved_successfully),
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        onMetadataSaved()
                                        onDismissRequest()
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                context.getString(R.string.label_failed_to_save_metadata),
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                    }
                                } catch (e: Exception) {
                                    Toast
                                        .makeText(
                                            context,
                                            context.getString(R.string.label_error_saving_metadata, e.message ?: ""),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    },
                    enabled = !isSaving && !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = stringResource(if (isSaving) R.string.label_saving else R.string.label_save),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismissRequest,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.label_cancel),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDialogEditMetadata() {
    MaterialTheme {
        DialogEditMetadata(
            showDialog = true,
            audioUri = null,
            onDismissRequest = {},
            onMetadataSaved = {},
        )
    }
}
