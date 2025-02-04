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

enum class AudioFormat(val extension: String) {
    MP3(".mp3"),
    M4A(".m4a"),
    WAV(".wav"),
    FLAC(".flac"),
    AAC(".aac"),
    OGG(".ogg");

    companion object {
        fun fromExtension(extension: String?): AudioFormat {
            return entries.find { it.extension.equals(extension, ignoreCase = true) }
                ?: MP3
        }
    }
}