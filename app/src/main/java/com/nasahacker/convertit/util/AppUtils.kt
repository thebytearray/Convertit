package com.nasahacker.convertit.util

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleCoroutineScope
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.nasahacker.convertit.R
import com.nasahacker.convertit.model.AudioBitrate
import com.nasahacker.convertit.model.AudioCodec
import com.nasahacker.convertit.model.AudioFormat
import com.nasahacker.convertit.model.AudioMetadata
import com.nasahacker.convertit.util.Constants.FOLDER_DIR
import com.nasahacker.convertit.util.Constants.FORMAT_ARRAY
import com.nasahacker.convertit.util.Constants.STORAGE_PERMISSION_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log10
import kotlin.math.pow

object AppUtils {

    fun openFilePicker(activity: Activity, pickFileLauncher: ActivityResultLauncher<Intent>) {
        if (isStoragePermissionGranted(activity)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            pickFileLauncher.launch(intent)
        } else {
            requestStoragePermissions(activity)
        }
    }

    fun openLink(context: Context, link: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }

    fun shareMusicFile(context: Context, file: File) {
        if (file.exists()) {
            val fileUri =
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.label_share_music_file)
                )
            )
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.label_file_does_not_exist), Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getFileSizeInReadableFormat(context: Context, file: File): String {
        val sizeInBytes = file.length()
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            context.getString(R.string.label_file_size),
            sizeInBytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups]
        )
    }

    fun openMusicFileInPlayer(context: Context, file: File) {
        if (file.exists()) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            } else {
                Uri.fromFile(file)
            }
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "audio/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.label_no_app_found_to_open_the_file), Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getFilesFromUris(context: Context, uriList: List<Uri>): List<File> {
        return uriList.mapNotNull { uri -> getFileFromUri(context, uri) }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri) ?: return null
            val file = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        return if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }
        } else File(uri.path!!).name
    }

    fun getAudioFilesFromConvertedFolder(): List<File> {
        val convertedDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            FOLDER_DIR
        )
        return convertedDir.takeIf { it.exists() && it.isDirectory }?.listFiles()?.filter { file ->
            FORMAT_ARRAY.any { file.extension.equals(it.trimStart('.'), ignoreCase = true) }
        }?.toList() ?: emptyList()
    }

    fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        } ?: "unknown"
    }

    private fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val fileName = getFileName(context.contentResolver, uri)
            val tempFile = File(context.cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile.absolutePath
        } catch (e: Exception) {
            Log.e("FileUtils", "Error copying file: ${e.message}")
            null
        }
    }


    fun showAudioInfoDialog(context: Context, lifecycleScope: LifecycleCoroutineScope, item: File) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_audio_info)
        dialog.window?.setBackgroundDrawableResource(R.drawable.blank_bg)

        // Initialize views
        val ivCoverArt: ImageView = dialog.findViewById(R.id.ivCoverArt)
        val tvTitle: TextView = dialog.findViewById(R.id.tvTitle)
        val tvArtist: TextView = dialog.findViewById(R.id.tvArtist)
        val tvAlbum: TextView = dialog.findViewById(R.id.tvAlbum)
        val tvGenre: TextView = dialog.findViewById(R.id.tvGenre)
        val tvTrack: TextView = dialog.findViewById(R.id.tvTrack)
        val tvYear: TextView = dialog.findViewById(R.id.tvYear)

        lifecycleScope.launch {
            val data: AudioMetadata? = withContext(Dispatchers.IO) {
                getAudioMetadata(context, item)
            }

            data?.let {
                // Populate views and manage visibility
                ivCoverArt.apply {
                    visibility = if (it.coverArtBitmap != null) {
                        setImageBitmap(it.coverArtBitmap)
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }

                val unknown = context.getString(R.string.label_dialog_unknown)

                tvTitle.apply {
                    text = context.getString(R.string.label_dialog_title, it.title ?: unknown)
                    visibility = if (it.title != null) View.VISIBLE else View.GONE
                }

                tvArtist.apply {
                    text = context.getString(R.string.label_dialog_artist, it.artist ?: unknown)
                    visibility = if (it.artist != null) View.VISIBLE else View.GONE
                }

                tvAlbum.apply {
                    text = context.getString(R.string.label_dialog_album, it.album ?: unknown)
                    visibility = if (it.album != null) View.VISIBLE else View.GONE
                }

                tvGenre.apply {
                    text = context.getString(R.string.label_dialog_genre, it.genre ?: unknown)
                    visibility = if (it.genre != null) View.VISIBLE else View.GONE
                }

                tvTrack.apply {
                    text = context.getString(R.string.label_dialog_track, it.track ?: unknown)
                    visibility = if (it.track != null) View.VISIBLE else View.GONE
                }

                tvYear.apply {
                    text = context.getString(R.string.label_dialog_year, it.year ?: unknown)
                    visibility = if (it.year != null) View.VISIBLE else View.GONE
                }

                // Check if all views are invisible (GONE)
                if (ivCoverArt.visibility == View.GONE &&
                    tvTitle.visibility == View.GONE &&
                    tvArtist.visibility == View.GONE &&
                    tvAlbum.visibility == View.GONE &&
                    tvGenre.visibility == View.GONE &&
                    tvTrack.visibility == View.GONE &&
                    tvYear.visibility == View.GONE
                ) {
                    dialog.dismiss()
                    Toast.makeText(
                        context,
                        context.getString(R.string.label_no_metadata_found), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Show the dialog once the data is populated
                    dialog.show()
                }
            } ?: run {
                Log.e("AppUtils", "Failed to retrieve audio metadata.")
                Toast.makeText(
                    context,
                    context.getString(R.string.label_no_metadata_found),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun getAudioMetadata(context: Context, file: File): AudioMetadata? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)

            // Extract metadata
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val track =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            val coverArt = retriever.embeddedPicture

            // Convert cover art byte array to Bitmap if available
            val coverArtBitmap = coverArt?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }

            // Save cover art as a temporary file and get its URI
            val coverArtUri = coverArt?.let {
                val coverFile =
                    File(context.cacheDir, "cover_art_${System.currentTimeMillis()}.jpg")
                coverFile.writeBytes(it)
                Uri.fromFile(coverFile)
            }

            // Return the populated AudioMetadata object
            AudioMetadata(
                title = title,
                artist = artist,
                album = album,
                genre = genre,
                track = track,
                year = year,
                coverArtUri = coverArtUri,
                coverArtBitmap = coverArtBitmap,
                uri = Uri.fromFile(file) // Convert File to Uri for consistency
            )
        } catch (e: Exception) {
            Log.e("AppUtils", "Error retrieving metadata: ${e.message}")
            null
        } finally {
            retriever.release()
        }
    }

    fun editAudioMetadata(
        context: Context,
        inputUri: Uri,
        metadata: AudioMetadata,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val musicDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            FOLDER_DIR
        ).apply { mkdirs() }
        val inputPath = copyUriToInternalStorage(context, inputUri) ?: run {
            onFailure(context.getString(R.string.label_failed_to_copy_file_from_uri, inputUri))
            return
        }
        val outputFilePath =
            File(musicDir, "${File(inputPath).nameWithoutExtension}-output.mp3").absolutePath
        val command = mutableListOf("-y", "-i", inputPath).apply {
            val coverArtPath = metadata.coverArtUri?.let {
                copyUriToInternalStorage(context, it)
            } ?: metadata.coverArtBitmap?.let {
                // Save the bitmap to a temporary file and use its path
                val coverFile =
                    File(context.cacheDir, "cover_art_${System.currentTimeMillis()}.jpg")
                coverFile.outputStream().use { outputStream ->
                    it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                coverFile.absolutePath
            }

            coverArtPath?.let {
                addAll(
                    listOf(
                        "-i",
                        it,
                        "-map",
                        "0:0",
                        "-map",
                        "1:0",
                        "-disposition:v:0",
                        "attached_pic"
                    )
                )
            }

            metadata.title?.let { addAll(listOf("-metadata", "title=\"$it\"")) }
            metadata.artist?.let { addAll(listOf("-metadata", "artist=\"$it\"")) }
            metadata.album?.let { addAll(listOf("-metadata", "album=\"$it\"")) }
            metadata.genre?.let { addAll(listOf("-metadata", "genre=\"$it\"")) }
            metadata.track?.let { addAll(listOf("-metadata", "track=\"$it\"")) }
            metadata.year?.let { addAll(listOf("-metadata", "date=\"$it\"")) }
            add(outputFilePath)
        }

        FFmpeg.executeAsync(command.toTypedArray()) { _, returnCode ->
            if (returnCode == Config.RETURN_CODE_SUCCESS) onSuccess(outputFilePath)
            else onFailure(
                context.getString(
                    R.string.label_metadata_update_failed_with_return_code,
                    returnCode
                )
            )
        }
    }


    fun convertAudio(
        context: Context,
        uris: List<Uri>,
        outputFormat: AudioFormat,
        bitrate: AudioBitrate = AudioBitrate.BITRATE_192K,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit,
    ) {
        val musicDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            FOLDER_DIR
        ).apply { mkdirs() }
        val outputPaths = mutableListOf<String>()
        uris.forEach { uri ->
            val inputPath = copyUriToInternalStorage(context, uri) ?: run {
                onFailure(context.getString(R.string.label_failed_to_copy_file_from_uri, uri))
                return
            }
            val outputFilePath = File(
                musicDir,
                "${File(inputPath).nameWithoutExtension}${outputFormat.extension}"
            ).absolutePath
            val command = arrayOf(
                "-y",
                "-i",
                inputPath,
                "-map",
                "0",
                "-map_metadata",
                "0",
                "-c:a",
                AudioCodec.fromFormat(outputFormat).codec,
                "-b:a",
                bitrate.bitrate,
                "-c:v",
                "copy",
                outputFilePath
            )
            FFmpeg.executeAsync(command) { _, returnCode ->
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    outputPaths.add(outputFilePath)
                    if (outputPaths.size == uris.size) onSuccess(outputPaths)
                } else {
                    onFailure(
                        context.getString(
                            R.string.label_conversion_failed_for_file_with_return_code,
                            inputPath,
                            returnCode
                        )
                    )
                }
            }
        }
    }

    fun handleNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1011
            )
        }
    }

    private fun isStoragePermissionGranted(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermissions(activity: Activity) {
        if (!isStoragePermissionGranted(activity)) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
            ActivityCompat.requestPermissions(activity, permissions, STORAGE_PERMISSION_CODE)
        } else {
            Toast.makeText(
                activity,
                activity.getString(R.string.label_storage_permissions_are_already_granted),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }
}
