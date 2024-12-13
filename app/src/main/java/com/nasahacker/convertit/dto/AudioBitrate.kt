package com.nasahacker.convertit.dto

enum class AudioBitrate(val bitrate: String) {
    BITRATE_128K("128k"),
    BITRATE_192K("192k"),
    BITRATE_256K("256k"),
    BITRATE_320K("320k");

    companion object {
        fun fromBitrate(bitrate: String?): AudioBitrate {
            return entries.find { it.bitrate.equals(bitrate, ignoreCase = true) }
                ?: BITRATE_128K
        }
    }
}