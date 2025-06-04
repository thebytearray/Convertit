package com.nasahacker.convertit.util

/**
 * @author Tamim Hossain
 * @email tamimh.dev@gmail.com
 * @license Apache-2.0
 *
 * ConvertIt is a free and easy-to-use audio converter app.
 * It supports popular audio formats like MP3 and M4A.
 * With options for high-quality bitrates ranging from 128k to 320k,
 * ConvertIt offers a seamless conversion experience tailored to your needs.
 */

object AppConfig {
    const val GITHUB_ISSUES_URL = "https://github.com/TheByteArray/ConvertIt/issues"
    const val STORAGE_PERMISSION_CODE = 101

    val BITRATE_ARRAY =
        listOf(
            "64k",
            "96k",
            "128k",
            "192k",
            "256k",
            "320k",
            "512k",
            "768k",
            "1024k",
        )
    val FORMAT_ARRAY =
        listOf(
            ".mp3",
            ".m4a",
            ".aac",
            ".ogg",
            ".opus",
            ".wma",
            ".mka",
            ".spx",
        )

    val FORMAT_BITRATE_MAP = mapOf(
        ".mp3" to listOf("64k", "96k", "128k", "192k", "256k", "320k"),
        ".aac" to listOf("64k", "96k", "128k", "192k", "256k", "320k"),
        ".m4a" to listOf("64k", "96k", "128k", "192k", "256k", "320k", "512k"),
        ".ogg" to listOf("64k", "96k", "128k", "192k", "256k", "320k", "512k"),
        ".opus" to listOf("64k", "96k", "128k", "192k", "256k", "320k"),
        ".wma" to listOf("64k", "96k", "128k", "192k", "256k", "320k"),
        ".mka" to BITRATE_ARRAY,
        ".spx" to listOf("64k", "96k", "128k", "192k"),
    )

    const val URI_LIST = "uri_list"
    const val BITRATE = "bitrate"
    const val AUDIO_FORMAT = "audio_format"
    const val AUDIO_PLAYBACK_SPEED = "audio_playback_speed"
    const val CONVERT_BROADCAST_ACTION = "com.nasahacker.convertit.ACTION_CONVERSION_COMPLETE"
    const val ACTION_STOP_SERVICE = "com.nasahacker.convertit.ACTION_STOP_SERVICE"
    const val IS_SUCCESS = "isSuccess"
    const val CHANNEL_ID = "CONVERT_IT_CHANNEL_ID"
    const val CHANNEL_NAME = "ConvertIt Notifications"
    const val FOLDER_DIR = "ConvertIt"
    const val DISCORD_CHANNEL = "https://discord.com/invite/2WCsnpw4et"
    const val GITHUB_PROFILE = "https://github.com/codewithtamim"
    const val GITHUB_PROFILE_MOD = "https://github.com/moontahid"
    const val TELEGRAM_CHANNEL = "https://t.me/thebytearray"
    const val APP_PREF = "app_prefs"
    const val PREF_DONT_SHOW_AGAIN = "pref_dont_show_again"
}
