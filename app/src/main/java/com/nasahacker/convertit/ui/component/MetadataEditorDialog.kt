package com.nasahacker.convertit.ui.component

import android.graphics.ImageDecoder
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.nasahacker.convertit.ui.viewmodel.AppViewModel
import com.nasahacker.convertit.dto.AudioMetadataItem
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Collections
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import android.os.Build

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataEditorDialog(
    viewModel: AppViewModel,
    onDismissRequest: () -> Unit
) {
    val metadataItem by viewModel.editableMetadata.collectAsState()
    val coverBitmap by viewModel.editingCoverArtBitmap.collectAsState() // This is the original bitmap from ViewModel
    val error by viewModel.metadataError.collectAsState()
    val editingUri by viewModel.editingFileUri.collectAsState()
    val context = LocalContext.current

    // Local state for the bitmap preview in the dialog
    var localBitmapPreview by remember { mutableStateOf(coverBitmap) }
    // Local state for the URI of a newly picked image
    var tempCoverUri by remember { mutableStateOf<Uri?>(null) }

    // When the dialog is first shown or metadataItem changes, update the local preview
    // This effect ensures that if the dialog is recomposed with a new item (or cover art),
    // the preview reflects the new `coverBitmap` from the ViewModel.
    LaunchedEffect(coverBitmap, metadataItem) {
        localBitmapPreview = coverBitmap
        tempCoverUri = null // Reset temp URI if metadata item changes (e.g. new file loaded)
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it == "Metadata saved successfully.") {
                 viewModel.clearMetadataError() // Clear error first
                 onDismissRequest() // Then dismiss
            } else {
                viewModel.clearMetadataError()
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            tempCoverUri = uri
            try {
                val source = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    ImageDecoder.createSource(context.contentResolver, uri)
                } else {
                    ImageDecoder.createSource(context.contentResolver, uri)
                }
                localBitmapPreview = ImageDecoder.decodeBitmap(source)
            } catch (e: Exception) {
                // Handle exception, perhaps show a toast or log
                Toast.makeText(context, "Error loading image preview", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (editingUri != null && metadataItem == null) {
        // Still loading or URI set but item not yet loaded
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text("Loading Metadata...") },
            text = { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(16.dp)) { CircularProgressIndicator() } },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearEditingState() // Clear loading attempt
                    onDismissRequest()
                }) { Text("Cancel") }
            }
        )
        return
    }

    // metadataItem should not be null here if we are to show the editor
    // If editingUri is null, dialog shouldn't be shown (controlled by HomeScreen state)
    // So if metadataItem is null here, it means loading failed or was cleared.
    if (metadataItem == null) {
        // This case might be hit if clearEditingState() is called while dialog is fading out.
        // Or if loading failed and error was cleared before this recomposition.
        // To prevent crash, just don't show the dialog content.
        // The LaunchedEffect for error handling or onDismissRequest should manage dialog closure.
        return
    }

    // Mutable states for each text field, initialized from metadataItem
    var title by remember(metadataItem) { mutableStateOf(metadataItem.title ?: "") }
    var artist by remember(metadataItem) { mutableStateOf(metadataItem.artist ?: "") }
    var album by remember(metadataItem) { mutableStateOf(metadataItem.album ?: "") }
    var albumArtist by remember(metadataItem) { mutableStateOf(metadataItem.albumArtist ?: "") }
    var trackNumber by remember(metadataItem) { mutableStateOf(metadataItem.trackNumber ?: "") }
    var totalTracks by remember(metadataItem) { mutableStateOf(metadataItem.totalTracks ?: "") }
    var discNumber by remember(metadataItem) { mutableStateOf(metadataItem.discNumber ?: "") }
    var totalDiscs by remember(metadataItem) { mutableStateOf(metadataItem.totalDiscs ?: "") }
    var year by remember(metadataItem) { mutableStateOf(metadataItem.year ?: "") }
    var genre by remember(metadataItem) { mutableStateOf(metadataItem.genre ?: "") }
    var composer by remember(metadataItem) { mutableStateOf(metadataItem.composer ?: "") }
    var comment by remember(metadataItem) { mutableStateOf(metadataItem.comment ?: "") }
    var lyrics by remember(metadataItem) { mutableStateOf(metadataItem.lyrics ?: "") }


    AlertDialog(
        onDismissRequest = {
            viewModel.clearEditingState()
            onDismissRequest()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text("Edit Metadata") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
                Box(modifier = Modifier.size(150.dp).align(Alignment.CenterHorizontally).padding(bottom = 8.dp)) {
                    if (localBitmapPreview != null) {
                        Image(
                            bitmap = localBitmapPreview!!.asImageBitmap(),
                            contentDescription = "Cover Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Filled.BrokenImage, "No Cover Art", modifier = Modifier.fillMaxSize())
                    }
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Icon(Icons.Filled.Collections, "Change Cover")
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = artist, onValueChange = { artist = it }, label = { Text("Artist") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = album, onValueChange = { album = it }, label = { Text("Album") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = albumArtist, onValueChange = { albumArtist = it }, label = { Text("Album Artist") }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = trackNumber, onValueChange = { trackNumber = it }, label = { Text("Track") }, modifier = Modifier.weight(1f).padding(end = 4.dp))
                    OutlinedTextField(value = totalTracks, onValueChange = { totalTracks = it }, label = { Text("Total Tracks") }, modifier = Modifier.weight(1f).padding(start = 4.dp))
                }
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = discNumber, onValueChange = { discNumber = it }, label = { Text("Disc") }, modifier = Modifier.weight(1f).padding(end = 4.dp))
                    OutlinedTextField(value = totalDiscs, onValueChange = { totalDiscs = it }, label = { Text("Total Discs") }, modifier = Modifier.weight(1f).padding(start = 4.dp))
                }
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = composer, onValueChange = { composer = it }, label = { Text("Composer") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Comment") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                OutlinedTextField(value = lyrics, onValueChange = { lyrics = it }, label = { Text("Lyrics") }, modifier = Modifier.fillMaxWidth(), maxLines = 5)
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedItem = AudioMetadataItem(
                    title = title.ifBlank { null },
                    artist = artist.ifBlank { null },
                    album = album.ifBlank { null },
                    albumArtist = albumArtist.ifBlank { null },
                    trackNumber = trackNumber.ifBlank { null },
                    totalTracks = totalTracks.ifBlank { null },
                    discNumber = discNumber.ifBlank { null },
                    totalDiscs = totalDiscs.ifBlank { null },
                    year = year.ifBlank { null },
                    genre = genre.ifBlank { null },
                    composer = composer.ifBlank { null },
                    comment = comment.ifBlank { null },
                    lyrics = lyrics.ifBlank { null },
                    // If tempCoverUri is null, it means user didn't pick a new image.
                    // So, we pass the original coverArtBytes from the loaded metadataItem.
                    // If tempCoverUri is NOT null, it means a new image was picked.
                    // In this case, coverArtBytes should be null in updatedItem,
                    // because saveMetadata will use tempCoverUri to get the new bytes.
                    // If user wants to REMOVE cover art, tempCoverUri will be null,
                    // AND we need a way to signal removal. This is handled by setting coverArtBytes to null
                    // if the localBitmapPreview was also set to null by some explicit action (not implemented here, but can be).
                    // For now, if tempCoverUri is null, existing art is preserved.
                    // If tempCoverUri is not null, new art is used.
                    // If localBitmapPreview is null AND tempCoverUri is null, it means remove.
                    coverArtBytes = if (tempCoverUri == null && localBitmapPreview == null) null else metadataItem.coverArtBytes
                )
                viewModel.saveMetadata(updatedItem, tempCoverUri)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearEditingState() // Clear states if user cancels
                onDismissRequest()
            }) {
                Text("Cancel")
            }
        }
    )
}
