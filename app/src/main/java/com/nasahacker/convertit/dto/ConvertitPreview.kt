package com.nasahacker.convertit.dto

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Copyright (c) 2025
 * Created by: Tamim Hossain (tamim@thebytearray.org)
 * Created on: 4/6/25
 **/


@Preview(
    name = "Light Preview",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showSystemUi = true, showBackground = true
)

annotation class ConvertitLightPreview

@Preview(
    name = "Dark Preview",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showSystemUi = true, showBackground = true
)

annotation class ConvertitDarkPreview



