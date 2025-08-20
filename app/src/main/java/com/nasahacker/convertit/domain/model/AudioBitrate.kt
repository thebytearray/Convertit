package com.nasahacker.convertit.domain.model

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

enum class AudioBitrate(
    val bitrate: String,
) {
    // voice optimized ones
    BITRATE_9K("9k"),       
    BITRATE_16K("16k"),     
    BITRATE_24K("24k"),      
    BITRATE_32K("32k"),     
    BITRATE_48K("48k"),      
    
    // other standard
    BITRATE_64K("64k"),
    BITRATE_96K("96k"),
    BITRATE_128K("128k"),
    BITRATE_192K("192k"),
    BITRATE_256K("256k"),
    BITRATE_320K("320k"),
    BITRATE_512K("512k"),
    BITRATE_768K("768k"),
    BITRATE_1024K("1024k"),
    ;

    companion object {
        fun fromBitrate(bitrate: String?): AudioBitrate =
            entries.find { it.bitrate.equals(bitrate, ignoreCase = true) }
                ?: BITRATE_128K
    }
}
