package com.nasahacker.convertit.dto
/**
 * @author      Tamim Hossain
 * @email       tamimh.dev@gmail.com
 * @license     Apache-2.0
 *
 * ConvertIt is a free and easy-to-use audio converter app.
 * It supports popular audio formats like MP3 and M4A.
 * With options for high-quality bitrates ranging from 128k to 320k,
 * ConvertIt offers a seamless conversion experience tailored to your needs.
 */

enum class AudioBitrate(val bitrate: String) {
    BITRATE_64K("64k"),
    BITRATE_96K("96k"),
    BITRATE_128K("128k"),
    BITRATE_192K("192k"),
    BITRATE_256K("256k"),
    BITRATE_320K("320k"),
    BITRATE_512K("512k"),
    BITRATE_768K("768k"),
    BITRATE_1024K("1024k");


    companion object {
        fun fromBitrate(bitrate: String?): AudioBitrate {
            return entries.find { it.bitrate.equals(bitrate, ignoreCase = true) }
                ?: BITRATE_128K
        }
    }
}