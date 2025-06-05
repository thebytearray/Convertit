package com.nasahacker.convertit.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nasahacker.convertit.R

/**
 * Copyright (c) 2025
 * Created by: Tamim Hossain (tamim@thebytearray.org)
 * Created on: 5/6/25
 **/


@Composable
fun NoFilesFoundCard(modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp),
            imageVector = Icons.Filled.Audiotrack,
            contentDescription = null
        )


        Text(
            text = stringResource(R.string.label_no_files_found),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

    }

}


@Preview
@Composable
private fun PreviewNoFilesFoundCard() {
    NoFilesFoundCard()
}