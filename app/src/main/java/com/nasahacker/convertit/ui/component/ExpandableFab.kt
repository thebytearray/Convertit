package com.nasahacker.convertit.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nasahacker.convertit.R

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

@Composable
fun ExpandableFab(
    onEditMetadataClick: () -> Unit,
    onConvertAudioClick: () -> Unit,
    onConvertVideoClick: () -> Unit,
    onCustomSaveLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(300),
        label = "rotation",
    )

    val fabScale by animateFloatAsState(
        targetValue = if (isExpanded) 0.9f else 1f,
        animationSpec = tween(300),
        label = "scale",
    )

    val itemScale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(300),
        label = "itemScale",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isExpanded) {
            ExpandableFabItem(
                icon = Icons.Filled.Edit,
                label = stringResource(R.string.label_edit_metadata_action),
                onClick = {
                    onEditMetadataClick()
                    isExpanded = false
                },
                scale = itemScale,
            )

            ExpandableFabItem(
                icon = Icons.Filled.Folder,
                label = stringResource(R.string.label_custom_save_location_action),
                onClick = {
                    onCustomSaveLocationClick()
                    isExpanded = false
                },
                scale = itemScale,
            )

            ExpandableFabItem(
                icon = Icons.Filled.VideoLibrary,
                label = stringResource(R.string.label_convert_video_action),
                onClick = {
                    onConvertVideoClick()
                    isExpanded = false
                },
                scale = itemScale,
            )

            ExpandableFabItem(
                icon = Icons.Filled.Audiotrack,
                label = stringResource(R.string.label_convert_audio_action),
                onClick = {
                    onConvertAudioClick()
                    isExpanded = false
                },
                scale = itemScale,
            )
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.scale(fabScale),
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Settings,
                contentDescription = stringResource(if (isExpanded) R.string.label_close else R.string.label_actions),
                modifier = if (isExpanded) Modifier else Modifier.rotate(rotation),
            )
        }
    }
}

@Composable
private fun ExpandableFabItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.scale(scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            shape = CircleShape,
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                ),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
