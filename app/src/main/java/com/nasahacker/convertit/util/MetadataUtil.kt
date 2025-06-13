package com.nasahacker.convertit.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.nasahacker.convertit.dto.AudioFileMetadata
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

import com.github.Kyant0.taglib.AudioFile as TagLibAudioFile
import com.github.Kyant0.taglib.TagOption
import com.github.Kyant0.taglib.UnsupportedFormatException
import com.github.Kyant0.taglib.Picture // For creating Picture objects
import com.github.Kyant0.taglib.tag.Field // For setting fields, if needed directly (usually properties are fine)
import com.github.Kyant0.taglib.tag.Tag // For type hint if needed

object MetadataUtil {

    private const val TAG = "MetadataUtilTagLib"

    private fun copyUriToTempFile(context: Context, uri: Uri, prefix: String = "audiometa_input", suffix: String = ".tmp"): File? {
        return try {
            val tempFile = File.createTempFile(prefix, suffix, context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("Failed to open input stream for URI: $uri")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error copying URI to temp file: $uri", e)
            null
        }
    }

    fun readMetadata(context: Context, uri: Uri): AudioFileMetadata? {
        val tempFile = copyUriToTempFile(context, uri, "taglib_read_") ?: return null
        try {
            val audioFile = TagLibAudioFile.read(tempFile, TagOption(readPictures = true)) // Ensure pictures are read
            val tag = audioFile.tag() ?: return AudioFileMetadata() // No tag info

            val coverArtPicture = tag.pictures.firstOrNull()

            return AudioFileMetadata(
                title = tag.title?.ifEmpty { null },
                artist = tag.artist?.ifEmpty { null },
                album = tag.album?.ifEmpty { null },
                genre = tag.genre?.ifEmpty { null },
                year = tag.year?.toString()?.ifEmpty { null }, // Year might be Int
                trackNumber = tag.trackNumber?.toString()?.ifEmpty { null }, // Track might be Int
                albumArtist = tag.albumArtist?.ifEmpty { null },
                composer = tag.composer?.ifEmpty { null },
                comment = tag.comment?.ifEmpty { null },
                coverArt = coverArtPicture?.data,
                coverArtMimeType = coverArtPicture?.mimeType
            )
        } catch (e: UnsupportedFormatException) {
            Log.e(TAG, "Unsupported format for metadata reading with taglib: $uri", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error reading metadata with taglib from URI: $uri", e)
            return null
        } finally {
            if (!tempFile.delete()) {
                Log.w(TAG, "Failed to delete temporary file: ${tempFile.absolutePath}")
            }
        }
    }

    fun writeMetadata(context: Context, uri: Uri, metadata: AudioFileMetadata): Boolean {
        val tempAudioFile = copyUriToTempFile(context, uri, "taglib_write_") ?: return false
        try {
            val audioFile = TagLibAudioFile.read(tempAudioFile, TagOption(readPictures = true))
            val tag = audioFile.tag() ?: run {
                Log.e(TAG, "Failed to read or create tag for URI: $uri")
                // Attempt to create a tag if possible, or handle as error
                // For now, assume if tag() is null, we can't proceed.
                // Some file types might not support tags initially.
                // If the library offers a way to create a new tag for a file that doesn't have one,
                // that could be integrated here. e.g. audioFile.createTag() or similar.
                // However, taglib's AudioFile.read usually returns a file with a tag object,
                // even if it's empty or a default one for supported file types.
                // If tag() is truly null, it might be an unsupported file type for tagging by the lib.
                return false
            }

            tag.title = metadata.title?.ifEmpty { null }
            tag.artist = metadata.artist?.ifEmpty { null }
            tag.album = metadata.album?.ifEmpty { null }
            tag.genre = metadata.genre?.ifEmpty { null }
            tag.year = metadata.year?.toIntOrNull() // Assuming taglib expects Int for year
            tag.trackNumber = metadata.trackNumber?.toIntOrNull() // Assuming taglib expects Int
            tag.albumArtist = metadata.albumArtist?.ifEmpty { null }
            tag.composer = metadata.composer?.ifEmpty { null }
            tag.comment = metadata.comment?.ifEmpty { null }

            if (metadata.coverArt == null) {
                tag.pictures = emptyList() // Remove all pictures
            } else {
                val newPicture = Picture(
                    data = metadata.coverArt,
                    mimeType = metadata.coverArtMimeType,
                    type = Picture.Type.FrontCover, // Defaulting to FrontCover
                    description = ""
                )
                tag.pictures = listOf(newPicture) // Replace existing pictures with the new one
            }

            audioFile.save() // Save changes to tempAudioFile

            // Write changes from tempAudioFile back to the original URI
            context.contentResolver.openOutputStream(uri, "w")?.use { outputStream ->
                tempAudioFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("Failed to open output stream for URI: $uri")

            return true
        } catch (e: UnsupportedFormatException) {
            Log.e(TAG, "Unsupported format for metadata writing with taglib: $uri", e)
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error writing metadata with taglib to URI: $uri", e)
            return false
        } finally {
            if (!tempAudioFile.delete()) {
                Log.w(TAG, "Failed to delete temporary file: ${tempAudioFile.absolutePath}")
            }
        }
    }
}
