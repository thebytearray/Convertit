package com.nasahacker.convertit.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioItem(
    modifier: Modifier = Modifier,
    fileName: String = "Sample Audio File",
    fileSize: String = "100KB",
    format: String = "Mp3",
    isActionVisible: Boolean = false,
    onPlayClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .combinedClickable(onClick = {}, onLongClick = {
                    onLongClick()
                }),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Filled.Audiotrack,
                    contentDescription = stringResource(R.string.label_audio_icon),
                    modifier =
                        Modifier
                            .size(50.dp)
                            .padding(end = 8.dp),
                )
                Column {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(4.dp),
                                    ).padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = fileSize,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier =
                                Modifier
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(4.dp),
                                    ).padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = format,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isActionVisible) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.label_play_icon),
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clickable { onPlayClick() },
                    )
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.label_share_icon),
                        modifier =
                            Modifier
                                .size(30.dp)
                                .clickable { onShareClick() },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAudioItem() {
    MaterialTheme {
        AudioItem()
    }
}
