package com.nasahacker.convertit.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun DialogDeleteItem(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onDeleteConfirm: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.label_delete_item),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.label_delete_confirmation),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDeleteConfirm,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_delete),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_cancel),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDialogDeleteItem() {
    MaterialTheme {
        DialogDeleteItem(
            showDialog = true,
            onDismissRequest = {},
            onDeleteConfirm = {},
        )
    }
}
