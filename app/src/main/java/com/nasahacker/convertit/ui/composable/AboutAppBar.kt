package com.nasahacker.convertit.ui.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppBar(onBackPressed: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = "About")
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onBackPressed()
                },
                modifier = Modifier
                    .focusable()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}
