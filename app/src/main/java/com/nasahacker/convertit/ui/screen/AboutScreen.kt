package com.nasahacker.convertit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nasahacker.convertit.BuildConfig
import com.nasahacker.convertit.R
import com.nasahacker.convertit.ui.component.AboutAppContent
import com.nasahacker.convertit.ui.component.CommunityIcon
import com.nasahacker.convertit.ui.component.ContactItem
import com.nasahacker.convertit.ui.component.SectionTitle
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.util.Constant

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
fun AboutScreen() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // App Title
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 24.dp)
            )

            // About App Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_about_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    AboutAppContent(context)
                }
            }

            // Contact Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_contact_us),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    ContactItem(
                        name = stringResource(R.string.label_dev),
                        onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE) },
                    )
                    ContactItem(
                        name = stringResource(R.string.label_mod),
                        onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE_MOD) },
                    )
                }
            }

            // Community Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.label_community),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CommunityIcon(
                            iconRes = R.drawable.telegram_ic,
                            contentDescription = stringResource(R.string.label_telegram_icon),
                            onClick = { AppUtil.openLink(context, Constant.TELEGRAM_CHANNEL) },
                        )
                        CommunityIcon(
                            iconRes = R.drawable.discord_ic,
                            contentDescription = stringResource(R.string.label_dc_icon),
                            onClick = { AppUtil.openLink(context, Constant.DISCORD_CHANNEL) },
                        )
                        CommunityIcon(
                            iconRes = R.drawable.github_ic,
                            contentDescription = stringResource(R.string.label_github_icon),
                            onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE) },
                        )
                    }
                }
            }

            // Report Issue Button
            Button(
                onClick = { AppUtil.openLink(context, Constant.GITHUB_ISSUES_URL) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.label_open_github_issue),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Version and License
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.label_apache_2_0_license),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAboutScreen() {
    AboutScreen()
}
