package com.nasahacker.convertit.ui.component

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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CommunityIcon(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(30.dp),
        )
    }
}
