package com.nasahacker.convertit.data.repository

import android.net.Uri
import com.nasahacker.convertit.domain.model.AudioFile
import com.nasahacker.convertit.domain.repository.FileAccessRepository
import com.nasahacker.convertit.domain.repository.FileRepository
import javax.inject.Inject

class FileRepositoryImpl
    @Inject
    constructor(
        private val fileAccessRepository: FileAccessRepository,
    ) : FileRepository {
        override fun getAudioFilesFromConvertedFolder(customSaveUri: Uri?): List<AudioFile> =
            fileAccessRepository.getAudioFilesFromConvertedFolder(customSaveUri)
    }
