package com.nasahacker.convertit.data.repository

import com.nasahacker.convertit.data.local.UserPreferencesDataSource
import com.nasahacker.convertit.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
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

class AppRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource
) : AppRepository {
    override val isDontShowAgain: Flow<Boolean>
        get() = dataSource.isDontShowAgain
    override val selectedCustomLocation: Flow<String>
        get() = dataSource.selectedCustomSaveLocation

    override suspend fun saveIsDontShowAgain(value: Boolean) {
        dataSource.saveIsDontShowAgain(value)
    }

    override suspend fun saveSelectedCustomLocation(value: String) {
        dataSource.saveSelectedCustomSaveLocation(value)
    }
}