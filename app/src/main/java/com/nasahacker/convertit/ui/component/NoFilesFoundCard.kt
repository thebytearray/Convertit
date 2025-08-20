package com.nasahacker.convertit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
fun NoFilesFoundCard(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = "No Converted Files Yet",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Start converting your audio files to see them here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoFilesFoundCard() {
    MaterialTheme {
        NoFilesFoundCard()
    }
}
