package com.nasahacker.convertit.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nasahacker.convertit.R
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.util.Constant
/**
 * @author      Tamim Hossain
 * @email       tamimh.dev@gmail.com
 * @license     Apache-2.0
 *
 * ConvertIt is a free and easy-to-use audio converter app.
 * It supports popular audio formats like MP3 and M4A.
 * With options for high-quality bitrates ranging from 128k to 320k,
 * ConvertIt offers a seamless conversion experience tailored to your needs.
 */

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Contact Section
            SectionTitle(stringResource(R.string.label_contact_us))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactItem(
                    name = stringResource(R.string.label_dev),
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE) }
                )
                ContactItem(
                    name = stringResource(R.string.label_mod),
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE_MOD) }
                )
            }

            // Community Section
            SectionTitle(stringResource(R.string.label_community))
            Row(
                modifier = Modifier
                    .wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommunityIcon(
                    iconRes = R.drawable.telegram_ic,
                    contentDescription = stringResource(R.string.label_telegram_icon),
                    onClick = { AppUtil.openLink(context, Constant.TELEGRAM_CHANNEL) }
                )
                CommunityIcon(
                    iconRes = R.drawable.discord_ic,
                    contentDescription = stringResource(R.string.label_dc_icon),
                    onClick = { AppUtil.openLink(context, Constant.DISCORD_CHANNEL) }
                )
                CommunityIcon(
                    iconRes = R.drawable.github_ic,
                    contentDescription = stringResource(R.string.label_github_icon),
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE) }
                )
            }

            // Donation Section
            SectionTitle(stringResource(R.string.label_support_us))
            DonationCard(
                iconRes = R.drawable.btc,
                description = stringResource(R.string.label_btc_icon),
                address = stringResource(R.string.label_btc_add)
            )
            DonationCard(
                iconRes = R.drawable.usdt,
                description = stringResource(R.string.label_usdt_icon),
                address = stringResource(R.string.label_usdt_add)
            )

            // About App Section
            SectionTitle(stringResource(R.string.label_about_title))
            AboutAppContent(context)

            // GitHub Issues Section
            SectionTitle(stringResource(R.string.label_report_an_issue))
            Button(
                onClick = { AppUtil.openLink(context, Constant.GITHUB_ISSUES_URL) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.label_open_github_issue))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 12.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AboutAppContent(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = context.getString(R.string.label_about_app),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Start
        )
    }
    Text(
        text = "Apache 2.0",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DonationCard(iconRes: Int, description: String, address: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {/*We don't even need it right now */},
                onLongClick = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(description, address)
                    clipboard.setPrimaryClip(clip)
                }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = description,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun ContactItem(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable(onClick = { onClick() })
        )
    }
}

@Composable
fun CommunityIcon(iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAboutScreen() {
    AboutScreen()
}
