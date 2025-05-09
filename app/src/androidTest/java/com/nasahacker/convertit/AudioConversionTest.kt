package com.nasahacker.convertit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nasahacker.convertit.dto.AudioBitrate
import com.nasahacker.convertit.dto.AudioFormat
import com.nasahacker.convertit.util.AppUtil
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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

@RunWith(AndroidJUnit4::class)
class AudioConversionTest {
    private lateinit var context: Context
    private lateinit var testFile: File
    private lateinit var outputDir: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        outputDir = File(context.getExternalFilesDir(null), "test_output")
        outputDir.mkdirs()

        testFile = File(outputDir, "sample.flac")
        context.assets.open("test_files/sample.flac").use { input ->
            testFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    @After
    fun cleanup() {
        // Clean up test files
        testFile.delete()
        outputDir.listFiles()?.forEach { it.delete() }
        outputDir.delete()
    }

    @Test
    fun testConvertToAllFormats() {
        val formats = AudioFormat.values()
        val bitrates = AudioBitrate.values()

        formats.forEach { format ->
            bitrates.forEach { bitrate ->
                testConversion(format, bitrate)
            }
        }
    }

    private fun testConversion(format: AudioFormat, bitrate: AudioBitrate) {
        val latch = CountDownLatch(1)
        var conversionSuccess = false
        var errorMessage: String? = null

        AppUtil.convertAudio(
            context = context,
            playbackSpeed = "1.0",
            uris = listOf(android.net.Uri.fromFile(testFile)),
            outputFormat = format,
            bitrate = bitrate,
            onSuccess = { outputPaths ->
                // Verify output file exists and has correct extension
                val outputFile = File(outputPaths.first())
                assertTrue(
                    "Output file should exist for format ${format.name} and bitrate ${bitrate.name}",
                    outputFile.exists()
                )
                assertTrue(
                    "Output file should have correct extension for format ${format.name}",
                    outputFile.extension.equals(format.extension.trimStart('.'), ignoreCase = true)
                )
                conversionSuccess = true
                latch.countDown()
            },
            onFailure = { error ->
                errorMessage = error
                latch.countDown()
            },
            onProgress = { /* Progress updates not needed for test */ }
        )

        assertTrue(
            "Conversion timed out for format ${format.name} and bitrate ${bitrate.name}",
            latch.await(30, TimeUnit.SECONDS)
        )

        assertTrue(
            "Conversion failed for format ${format.name} and bitrate ${bitrate.name}: $errorMessage",
            conversionSuccess
        )
    }
} 