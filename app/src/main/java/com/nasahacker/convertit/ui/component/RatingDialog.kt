package com.nasahacker.convertit.ui.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.nasahacker.convertit.util.Constant.APP_PREF
import com.nasahacker.convertit.util.Constant.PREF_DONT_SHOW_AGAIN

@Composable
fun RatingDialog(showReviewDialog: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE)
    }

    var dontShowAgain by remember {
        mutableStateOf(sharedPreferences.getBoolean(PREF_DONT_SHOW_AGAIN, false))
    }

    val appPackageName = "com.nasahacker.convertit"

    if (showReviewDialog && !dontShowAgain) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Star Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Rate Us",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "If you enjoy using our app, please take a moment to rate us on Google Play.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Checkbox(
                            checked = dontShowAgain,
                            onCheckedChange = {
                                dontShowAgain = it
                                sharedPreferences.edit().putBoolean(PREF_DONT_SHOW_AGAIN, it).apply()
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            "Don't show again",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            onDismissRequest = {
                sharedPreferences.edit().putBoolean(PREF_DONT_SHOW_AGAIN, dontShowAgain).apply()
                onDismiss()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                        context.startActivity(intent)

                        sharedPreferences.edit().putBoolean(PREF_DONT_SHOW_AGAIN, dontShowAgain).apply()
                        onConfirm()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Review",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        sharedPreferences.edit().putBoolean(PREF_DONT_SHOW_AGAIN, dontShowAgain).apply()
                        onDismiss()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}
