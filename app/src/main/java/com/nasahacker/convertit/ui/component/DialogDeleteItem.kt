package com.nasahacker.convertit.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
            title = {
                Text(
                    text = stringResource(R.string.label_delete_item),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.label_delete_confirmation),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            },
            confirmButton = {
                Button(
                    onClick = onDeleteConfirm,
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Text(text = stringResource(R.string.label_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Text(text = stringResource(R.string.label_cancel))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDialogDeleteItem() {
    DialogDeleteItem(
        showDialog = true,
        onDismissRequest = {},
        onDeleteConfirm = {},
    )
}
