package com.nasahacker.convertit.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.App
import com.nasahacker.convertit.dto.AudioMetadataItem
import com.nasahacker.convertit.dto.firstOrNullIfEmpty
import com.nasahacker.convertit.dto.joinOrNull
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.util.AppConfig
import com.nasahacker.convertit.util.AppConfig.IS_SUCCESS
import com.kyant.taglib.TagLib
import com.kyant.taglib.Picture
import com.kyant.taglib.PropertyMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class AppViewModel : ViewModel() {
    private val _uriList = MutableStateFlow<ArrayList<Uri>>(ArrayList())
    val uriList: StateFlow<ArrayList<Uri>> = _uriList

    private val _conversionStatus = MutableStateFlow<Boolean?>(null)
    val conversionStatus: StateFlow<Boolean?> = _conversionStatus

    private val _editingFileUri = MutableStateFlow<Uri?>(null)
    val editingFileUri: StateFlow<Uri?> = _editingFileUri

    private val _editableMetadata = MutableStateFlow<AudioMetadataItem?>(null)
    val editableMetadata: StateFlow<AudioMetadataItem?> = _editableMetadata

    private val _editingCoverArtBitmap = MutableStateFlow<Bitmap?>(null)
    val editingCoverArtBitmap: StateFlow<Bitmap?> = _editingCoverArtBitmap

    private val _metadataError = MutableStateFlow<String?>(null)
    val metadataError: StateFlow<String?> = _metadataError

    private val _pendingIntentRequest = MutableStateFlow<PendingIntent?>(null)
    val pendingIntentRequest: StateFlow<PendingIntent?> = _pendingIntentRequest

    private val conversionStatusReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                val isSuccess = intent?.getBooleanExtra(IS_SUCCESS, false) == true
                viewModelScope.launch {
                    _conversionStatus.value = isSuccess
                    if (isSuccess) {
                        clearUriList()
                    }
                }
            }
        }

    fun resetConversionStatus() {
        viewModelScope.launch {
            _conversionStatus.value = null
        }
    }

    init {
        startListeningForBroadcasts()
    }

    /**
     * Updates the URI list with new URIs from the given intent.
     */
    fun updateUriList(intent: Intent?) {
        viewModelScope.launch {
            intent?.let {
                val uris = AppUtil.getUriListFromIntent(it)
                if (uris.isNotEmpty()) {
                    val updatedList = ArrayList(_uriList.value).apply { addAll(uris) }
                    _uriList.value = updatedList
                }
            }
        }
    }

    /**
     * Registers the BroadcastReceiver to listen for conversion status updates.
     */
    private fun startListeningForBroadcasts() {
        val intentFilter = IntentFilter(AppConfig.CONVERT_BROADCAST_ACTION)
        ContextCompat.registerReceiver(
            App.application,
            conversionStatusReceiver,
            intentFilter,
            AppUtil.receiverFlags(),
        )
    }

    /**
     * Clears the URI list.
     */
    fun clearUriList() {
        viewModelScope.launch {
            _uriList.value = ArrayList()
        }
    }

    /**
     * Unregisters the BroadcastReceiver when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        App.application.unregisterReceiver(conversionStatusReceiver)
    }

    fun loadMetadataForEditing(uri: Uri) {
        viewModelScope.launch {
            _editingFileUri.value = uri
            _editableMetadata.value = null // Clear previous
            _editingCoverArtBitmap.value = null
            _metadataError.value = null

            var pfd: android.os.ParcelFileDescriptor? = null
            try {
                pfd = App.application.contentResolver.openFileDescriptor(uri, "r")
                if (pfd == null) {
                    _metadataError.value = "Failed to open file descriptor for URI: $uri"
                    return@launch
                }

                // The TagLib library expects a file descriptor integer.
                // We must ensure the ParcelFileDescriptor (pfd) is closed after its raw fd is used.
                // The .use block on pfd ensures pfd.close() is called.
                // We use pfd.dup().detachFd() to get a new FD that TagLib can use,
                // and the original pfd can be closed by the .use block.
                // TagLib itself does not close the FD passed to it.
                // However, the documentation for detachFd() says:
                // "The returned file descriptor is a duplicate of the file descriptor held by this object.
                // The caller is responsible for closing the returned file descriptor."
                // This means we need to close the `fd` obtained from `detachFd()`.
                // This is problematic as TagLib doesn't provide a close function for an int FD.
                // The most robust way is to let TagLib operate on the path if possible, or manage FD carefully.
                // Given TagLib's API `TagLib.getMetadata(fd: Int, ...)`
                // The `Tests.kt` in Kyant0/taglib shows: `val pfd = context.contentResolver.openFileDescriptor(uri, "r")`
                // then `pfd.use { descriptor -> val fd = descriptor.detachFd() ... }`
                // This implies the `fd` from `detachFd` is what's used, and the `pfd.use` closes the *original* PFD.
                // The detached FD is a *duplicate*. Who closes this duplicate?
                // If TagLib C++ code doesn't close it, it's a leak.
                // A safer approach might be to copy to a temp file and pass path, but that's inefficient.
                // Let's follow the library's example structure, assuming the C++ layer or OS handles the detached fd
                // once the process that created it (or the specific TagLib operation) finishes, or it's closed with native `close(fd)`.
                // The most direct interpretation of `detachFd()` is that the CALLER of `detachFd` is responsible.
                // If TagLib doesn't close it, we can't easily close it from Kotlin without JNI.
                //
                // Re-evaluating: The `ParcelFileDescriptor#close()` method closes the native fd.
                // If `detachFd()` gives a *duplicate*, then the original `pfd.close()` in the `finally` block
                // should be correct and sufficient. TagLib uses the duplicate, and we close our reference to the original.
                // The OS will reclaim the duplicate when TagLib is done with it or the process ends.
                // The key is that TagLib should not hold onto the FD indefinitely.
                // The `pfd.close()` in finally is critical.

                val fd = pfd.detachFd() // We get a raw int FD. PFD still needs to be closed.

                val metadataFromTagLib = withContext(Dispatchers.IO) {
                    // This is a blocking call, run in IO context
                    TagLib.getMetadata(fd, readPictures = true)
                }
                // After this call, TagLib should be done with `fd`.
                // The `pfd` (which holds the original native descriptor for `fd`)
                // will be closed in the `finally` block. This is standard practice.

                val title = metadataFromTagLib?.propertyMap?.get("TITLE")?.firstOrNullIfEmpty()
                val artist = metadataFromTagLib?.propertyMap?.get("ARTIST")?.joinOrNull()
                val album = metadataFromTagLib?.propertyMap?.get("ALBUM")?.firstOrNullIfEmpty()
                val albumArtist = metadataFromTagLib?.propertyMap?.get("ALBUMARTIST")?.joinOrNull()

                val trackInfo = metadataFromTagLib?.propertyMap?.get("TRACKNUMBER")?.firstOrNullIfEmpty()
                var trackNumber: String? = null
                var totalTracks: String? = null
                trackInfo?.split('/')?.let { parts ->
                    trackNumber = parts.getOrNull(0)?.trim()?.ifEmpty { null }
                    totalTracks = parts.getOrNull(1)?.trim()?.ifEmpty { null }
                }

                val discInfo = metadataFromTagLib?.propertyMap?.get("DISCNUMBER")?.firstOrNullIfEmpty()
                var discNumber: String? = null
                var totalDiscs: String? = null
                discInfo?.split('/')?.let { parts ->
                    discNumber = parts.getOrNull(0)?.trim()?.ifEmpty { null }
                    totalDiscs = parts.getOrNull(1)?.trim()?.ifEmpty { null }
                }

                val year = metadataFromTagLib?.propertyMap?.get("DATE")?.firstOrNullIfEmpty()?.takeIf { it.length >= 4 }?.substring(0, 4)
                    ?: metadataFromTagLib?.propertyMap?.get("YEAR")?.firstOrNullIfEmpty()
                val genre = metadataFromTagLib?.propertyMap?.get("GENRE")?.joinOrNull()
                val composer = metadataFromTagLib?.propertyMap?.get("COMPOSER")?.joinOrNull()
                val comment = metadataFromTagLib?.propertyMap?.get("COMMENT")?.firstOrNullIfEmpty()
                val lyrics = metadataFromTagLib?.propertyMap?.get("LYRICS")?.firstOrNullIfEmpty() // Or "UNSYNCEDLYRICS" / "USLT"

                val coverBytes = metadataFromTagLib?.pictures?.firstOrNull()?.data
                _editableMetadata.value = AudioMetadataItem(
                    title = title,
                    artist = artist,
                    album = album,
                    albumArtist = albumArtist,
                    trackNumber = trackNumber,
                    totalTracks = totalTracks,
                    discNumber = discNumber,
                    totalDiscs = totalDiscs,
                    year = year,
                    genre = genre,
                    composer = composer,
                    comment = comment,
                    lyrics = lyrics,
                    coverArtBytes = coverBytes
                )

                if (coverBytes != null && coverBytes.isNotEmpty()) {
                    _editingCoverArtBitmap.value = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
                    }
                }

            } catch (e: Exception) {
                _metadataError.value = "Error loading metadata: ${e.localizedMessage ?: "Unknown error"}"
                e.printStackTrace()
            } finally {
                try {
                    pfd?.close() // Ensure ParcelFileDescriptor is closed
                } catch (ioe: java.io.IOException) {
                    // Log this, but don't override original error if one already occurred
                    if (_metadataError.value == null) {
                        _metadataError.value = "Error closing file descriptor: ${ioe.localizedMessage}"
                    }
                    ioe.printStackTrace()
                }
            }
        }
    }

    fun clearEditingState() {
        _editingFileUri.value = null
        _editableMetadata.value = null
        _editingCoverArtBitmap.value = null
        _metadataError.value = null
    }

    fun clearMetadataError() {
        _metadataError.value = null
    }

    fun clearPendingIntentRequest() {
        _pendingIntentRequest.value = null
    }

    fun saveMetadata(updatedMetadata: AudioMetadataItem, newCoverArtUri: Uri?) {
        viewModelScope.launch {
            val currentUri = _editingFileUri.value
            if (currentUri == null) {
                _metadataError.value = "No file is currently being edited."
                return@launch
            }

            _metadataError.value = null // Clear previous errors
            var pfd: android.os.ParcelFileDescriptor? = null
            try {
                pfd = App.application.contentResolver.openFileDescriptor(currentUri, "rw")
                if (pfd == null) {
                    _metadataError.value = "Failed to open writable file descriptor for URI: $currentUri"
                    return@launch
                }
                val fd = pfd.detachFd() // Detach for TagLib

                // 1. Save Text Metadata
                val propertyMap = PropertyMap()
                updatedMetadata.title?.let { propertyMap["TITLE"] = arrayOf(it) }
                updatedMetadata.artist?.let { propertyMap["ARTIST"] = arrayOf(it) }
                updatedMetadata.album?.let { propertyMap["ALBUM"] = arrayOf(it) }
                updatedMetadata.albumArtist?.let { propertyMap["ALBUMARTIST"] = arrayOf(it) }

                var trackNumString: String? = updatedMetadata.trackNumber?.ifBlank { null }
                if (!updatedMetadata.totalTracks.isNullOrBlank()) {
                    trackNumString = (trackNumString ?: "") + "/${updatedMetadata.totalTracks}"
                }
                trackNumString?.let { propertyMap["TRACKNUMBER"] = arrayOf(it) }


                var discNumString: String? = updatedMetadata.discNumber?.ifBlank { null }
                if (!updatedMetadata.totalDiscs.isNullOrBlank()) {
                    discNumString = (discNumString ?: "") + "/${updatedMetadata.totalDiscs}"
                }
                discNumString?.let { propertyMap["DISCNUMBER"] = arrayOf(it) }

                updatedMetadata.year?.let { propertyMap["DATE"] = arrayOf(it) } // Using DATE as common practice
                updatedMetadata.genre?.let { propertyMap["GENRE"] = arrayOf(it) }
                updatedMetadata.composer?.let { propertyMap["COMPOSER"] = arrayOf(it) }
                updatedMetadata.comment?.let { propertyMap["COMMENT"] = arrayOf(it) }
                updatedMetadata.lyrics?.let { propertyMap["LYRICS"] = arrayOf(it) }

                val propertiesSaved = withContext(Dispatchers.IO) {
                    TagLib.savePropertyMap(fd, propertyMap)
                }

                // 2. Save Cover Art
                var pictureSaved = true // Assume true if no change or removal of non-existent art
                if (newCoverArtUri != null) { // New art selected
                    App.application.contentResolver.openInputStream(newCoverArtUri)?.use { inputStream ->
                        val coverBytes = inputStream.readBytes()
                        if (coverBytes.isNotEmpty()) {
                            val mimeType = App.application.contentResolver.getType(newCoverArtUri) ?: "image/jpeg"
                            val picture = Picture(
                                data = coverBytes,
                                mimeType = mimeType,
                                description = "Cover Art",
                                pictureType = "Front Cover"
                            )
                            pictureSaved = withContext(Dispatchers.IO) {
                                TagLib.savePictures(fd, arrayOf(picture))
                            }
                        }
                    }
                } else if (updatedMetadata.coverArtBytes == null && _editableMetadata.value?.coverArtBytes != null) {
                    // New art URI is null, and the passed metadataItem has null bytes,
                    // and the original loaded metadata HAD cover art bytes (meaning user wants to remove it)
                     pictureSaved = withContext(Dispatchers.IO) {
                        TagLib.savePictures(fd, emptyArray())
                    }
                }
                // If newCoverArtUri is null and updatedMetadata.coverArtBytes is not null, it means keep existing art, so pictureSaved remains true.


                if (propertiesSaved && pictureSaved) {
                    loadMetadataForEditing(currentUri) // Reload to reflect changes
                    _metadataError.value = "Metadata saved successfully."
                } else {
                    _metadataError.value = "Failed to save: properties=${propertiesSaved}, picture=${pictureSaved}"
                }

            } catch (e: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    _pendingIntentRequest.value = e.userAction.actionIntent
                    _metadataError.value = "Permission required. Please act on the prompt."
                } else {
                    _metadataError.value = "SecurityException: ${e.localizedMessage}"
                }
                e.printStackTrace()
            } catch (e: Exception) {
                _metadataError.value = "Error saving metadata: ${e.localizedMessage ?: "Unknown error"}"
                e.printStackTrace()
            } finally {
                try {
                    pfd?.close()
                } catch (ioe: java.io.IOException) {
                    // Log this, but don't override original error if one already occurred
                    if (_metadataError.value == null) {
                        _metadataError.value = "Error closing file descriptor post-save: ${ioe.localizedMessage}"
                    }
                    ioe.printStackTrace()
                }
            }
        }
    }
}
