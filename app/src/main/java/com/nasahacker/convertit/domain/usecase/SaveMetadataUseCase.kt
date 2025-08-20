package com.nasahacker.convertit.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.nasahacker.convertit.domain.model.Metadata
import com.nasahacker.convertit.domain.repository.MetadataRepository
import javax.inject.Inject

class SaveMetadataUseCase
    @Inject
    constructor(
        private val metadataRepository: MetadataRepository,
    ) {
        suspend operator fun invoke(
            audioUri: Uri,
            metadata: Metadata,
        ): Boolean = metadataRepository.saveMetadata(audioUri, metadata)

        suspend operator fun invoke(
            audioUri: Uri,
            bitmap: Bitmap?,
        ): Boolean = metadataRepository.saveCoverArt(audioUri, bitmap)
    }
