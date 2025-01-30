package com.nasahacker.convertit.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nasahacker.convertit.R

@Composable
fun DialogDeleteItem(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = stringResource(R.string.label_delete_item))
            },
            text = {
                Text(text = stringResource(R.string.label_delete_confirmation))
            },
            confirmButton = {
                Button(
                    onClick = onDeleteConfirm,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDialogDeleteItem() {
    DialogDeleteItem(
        showDialog = true,
        onDismissRequest = {},
        onDeleteConfirm = {}
    )
}
