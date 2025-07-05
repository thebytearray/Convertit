package com.nasahacker.convertit.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

@Composable
fun ExpandableFab(
    onEditMetadataClick: () -> Unit,
    onConvertAudioClick: () -> Unit,
    onConvertVideoClick: () -> Unit,
    onCustomSaveLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(300),
        label = "rotation"
    )

    val fabScale by animateFloatAsState(
        targetValue = if (isExpanded) 0.9f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )

    val itemScale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(300),
        label = "itemScale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isExpanded) {
            ExpandableFabItem(
                icon = Icons.Filled.Edit,
                label = stringResource(R.string.label_edit_metadata_action),
                onClick = {
                    onEditMetadataClick()
                    isExpanded = false
                },
                scale = itemScale
            )

            ExpandableFabItem(
                icon = Icons.Filled.Folder,
                label = stringResource(R.string.label_custom_save_location_action),
                onClick = {
                    onCustomSaveLocationClick()
                    isExpanded = false
                },
                scale = itemScale
            )

            ExpandableFabItem(
                icon = Icons.Filled.VideoLibrary,
                label = stringResource(R.string.label_convert_video_action),
                onClick = {
                    onConvertVideoClick()
                    isExpanded = false
                },
                scale = itemScale
            )

            ExpandableFabItem(
                icon = Icons.Filled.Audiotrack,
                label = stringResource(R.string.label_convert_audio_action),
                onClick = {
                    onConvertAudioClick()
                    isExpanded = false
                },
                scale = itemScale
            )
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.scale(fabScale)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Settings,
                contentDescription = stringResource(if (isExpanded) R.string.label_close else R.string.label_actions),
                modifier = if (isExpanded) Modifier else Modifier.rotate(rotation)
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.scale(scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
} 