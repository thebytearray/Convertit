package com.nasahacker.convertit.ui.component

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nasahacker.convertit.AboutActivity

import com.nasahacker.convertit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar() {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(text = context.getString(R.string.app_name))
        },
        actions = {
            IconButton(onClick = {
                context.startActivity(Intent(context, AboutActivity::class.java))
            }) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = stringResource(R.string.label_about)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}


@Preview(showBackground = true, showSystemUi = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewMainAppBar() {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(text = context.getString(R.string.app_name))
        },
        actions = {
            IconButton(onClick = {

            }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_info_24),
                    contentDescription = stringResource(R.string.label_about)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

}