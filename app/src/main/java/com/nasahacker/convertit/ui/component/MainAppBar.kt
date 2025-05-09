package com.nasahacker.convertit.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nasahacker.convertit.R

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    onNavigateToAbout: () -> Unit,
    onNavigateBack: () -> Unit,
    isAboutScreen: Boolean = false,
) {
    TopAppBar(
        title = { Text(text = if (isAboutScreen) "About" else stringResource(R.string.app_name)) },
        navigationIcon = {
            if (isAboutScreen) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (!isAboutScreen) {
                IconButton(onClick = onNavigateToAbout) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.label_about)
                    )
                }
            }
        }
    )
}
