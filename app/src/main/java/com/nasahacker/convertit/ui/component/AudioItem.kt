package com.nasahacker.convertit.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioItem(
    modifier: Modifier = Modifier,
    fileName: String = "Sample Audio File",
    fileSize: String = "100KB",
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
                .combinedClickable(onClick = {
                }, onLongClick = {
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
                Image(
                    painter = painterResource(R.drawable.audio_ic),
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
                    Text(
                        text = fileSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(visible = isActionVisible) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.play_ic),
                        contentDescription = stringResource(R.string.label_play_icon),
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clickable { onPlayClick() },
                    )
                    Image(
                        painter = painterResource(R.drawable.share_ic),
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
