package com.nasahacker.convertit.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nasahacker.convertit.dto.AudioFileMetadata
import com.nasahacker.convertit.util.toImageBitmap // Import the moved helper

// Helper function to convert ByteArray to ImageBitmap - REMOVED FROM HERE
// private fun ByteArray?.toImageBitmap(): ImageBitmap? {
//    return this?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
// }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataEditDialog(
    originalMetadata: AudioFileMetadata,
    currentCoverArtBitmap: ImageBitmap?, // Display this if available, else try originalMetadata.coverArt
    onDismissRequest: () -> Unit,
    onSave: (editedTextData: AudioFileMetadata) -> Unit,
    onChangeCoverArtClick: () -> Unit
) {
    var title by remember { mutableStateOf(originalMetadata.title ?: "") }
    var artist by remember { mutableStateOf(originalMetadata.artist ?: "") }
    var album by remember { mutableStateOf(originalMetadata.album ?: "") }
    var genre by remember { mutableStateOf(originalMetadata.genre ?: "") }
    var year by remember { mutableStateOf(originalMetadata.year ?: "") }
    var trackNumber by remember { mutableStateOf(originalMetadata.trackNumber ?: "") }
    var albumArtist by remember { mutableStateOf(originalMetadata.albumArtist ?: "") }
    var composer by remember { mutableStateOf(originalMetadata.composer ?: "") }
    var comment by remember { mutableStateOf(originalMetadata.comment ?: "") }

    val displayBitmap = currentCoverArtBitmap ?: originalMetadata.coverArt.toImageBitmap()

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Metadata", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                // Cover Art Section
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                ) {
                    if (displayBitmap != null) {
                        Image(
                            bitmap = displayBitmap,
                            contentDescription = "Cover Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "No Cover Art",
                            modifier = Modifier.fillMaxSize().padding(24.dp), // Add padding for the icon
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(
                    onClick = onChangeCoverArtClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Change Cover Art")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Text Fields
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = artist, onValueChange = { artist = it }, label = { Text("Artist") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = album, onValueChange = { album = it }, label = { Text("Album") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = albumArtist, onValueChange = { albumArtist = it }, label = { Text("Album Artist") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = composer, onValueChange = { composer = it }, label = { Text("Composer") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = trackNumber, onValueChange = { trackNumber = it }, label = { Text("Track Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Comment") }, modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), maxLines = 3)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val editedText = AudioFileMetadata(
                            title = title.ifBlank { null },
                            artist = artist.ifBlank { null },
                            album = album.ifBlank { null },
                            genre = genre.ifBlank { null },
                            year = year.ifBlank { null },
                            trackNumber = trackNumber.ifBlank { null },
                            albumArtist = albumArtist.ifBlank { null },
                            composer = composer.ifBlank { null },
                            comment = comment.ifBlank { null },
                            // coverArt and coverArtMimeType are not handled by this dialog's onSave directly
                            coverArt = null, // Caller will handle this
                            coverArtMimeType = null // Caller will handle this
                        )
                        onSave(editedText)
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
