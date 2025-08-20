package com.nasahacker.convertit.domain.usecase

import com.nasahacker.convertit.domain.repository.AppRepository
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

class SaveDontShowAgainUseCase
    @Inject
    constructor(
        private val repository: AppRepository,
    ) {
        suspend operator fun invoke(value: Boolean) = repository.saveIsDontShowAgain(value)
    }
