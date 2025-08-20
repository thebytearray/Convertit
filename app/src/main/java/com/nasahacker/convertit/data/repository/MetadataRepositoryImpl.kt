package com.nasahacker.convertit.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import com.nasahacker.convertit.domain.model.Metadata
import com.nasahacker.convertit.domain.repository.MetadataRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class MetadataRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : MetadataRepository {
        override suspend fun loadMetadata(audioUri: Uri): Metadata {
            return withContext(Dispatchers.IO) {
                try {
                    val parcelFileDescriptor =
                        context.contentResolver.openFileDescriptor(audioUri, "rw")
                            ?: return@withContext Metadata()

                    parcelFileDescriptor.use { fd ->
                        val taglibMetadata =
                            TagLib.getMetadata(fd.dup().detachFd(), readPictures = true)
                                ?: return@withContext Metadata()

                        Metadata.fromPropertyMap(
                            taglibMetadata.propertyMap,
                            taglibMetadata.pictures.toList(),
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Metadata()
                }
            }
        }

        override suspend fun saveMetadata(
            audioUri: Uri,
            metadata: Metadata,
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val parcelFileDescriptor =
                        context.contentResolver.openFileDescriptor(audioUri, "rw")
                            ?: return@withContext false

                    parcelFileDescriptor.use { fd ->
                        val propertyMap = HashMap(metadata.toPropertyMap())
                        TagLib.savePropertyMap(fd.dup().detachFd(), propertyMap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }

        override suspend fun saveCoverArt(
            audioUri: Uri,
            bitmap: Bitmap?,
        ): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val parcelFileDescriptor =
                        context.contentResolver.openFileDescriptor(audioUri, "rw")
                            ?: return@withContext false

                    parcelFileDescriptor.use { fd ->
                        if (bitmap != null) {
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
                            val imageData = byteArrayOutputStream.toByteArray()

                            val picture =
                                Picture(
                                    data = imageData,
                                    description = "Front Cover",
                                    pictureType = "Front Cover",
                                    mimeType = "image/jpeg",
                                )

                            TagLib.savePictures(fd.dup().detachFd(), arrayOf(picture))
                        } else {
                            TagLib.savePictures(fd.dup().detachFd(), arrayOf())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
    }
