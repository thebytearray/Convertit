package com.nasahacker.convertit.di

import com.nasahacker.convertit.data.repository.AppRepositoryImpl
import com.nasahacker.convertit.data.repository.AudioConverterRepositoryImpl
import com.nasahacker.convertit.data.repository.FileAccessRepositoryImpl
import com.nasahacker.convertit.data.repository.FileRepositoryImpl
import com.nasahacker.convertit.data.repository.MetadataRepositoryImpl
import com.nasahacker.convertit.domain.repository.AppRepository
import com.nasahacker.convertit.domain.repository.AudioConverterRepository
import com.nasahacker.convertit.domain.repository.FileAccessRepository
import com.nasahacker.convertit.domain.repository.FileRepository
import com.nasahacker.convertit.domain.repository.MetadataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserRepository(impl: AppRepositoryImpl): AppRepository

    @Binds
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    abstract fun bindFileAccessRepository(impl: FileAccessRepositoryImpl): FileAccessRepository

    @Binds
    abstract fun bindAudioConverterRepository(impl: AudioConverterRepositoryImpl): AudioConverterRepository

    @Binds
    abstract fun bindMetadataRepository(impl: MetadataRepositoryImpl): MetadataRepository
}
