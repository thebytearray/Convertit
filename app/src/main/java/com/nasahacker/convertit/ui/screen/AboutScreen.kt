package com.nasahacker.convertit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nasahacker.convertit.R
import com.nasahacker.convertit.ui.component.AboutAppContent
import com.nasahacker.convertit.ui.component.CommunityIcon
import com.nasahacker.convertit.ui.component.ContactItem
import com.nasahacker.convertit.ui.component.DonationCard
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
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionTitle(stringResource(R.string.label_contact_us))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ContactItem(
                    name = stringResource(R.string.label_dev),
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE) },
                )
                ContactItem(
                    name = stringResource(R.string.label_mod),
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE_MOD) },
                )
            }

            SectionTitle(stringResource(R.string.label_community))
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
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

            SectionTitle(stringResource(R.string.label_support_us))
            DonationCard(
                iconRes = R.drawable.btc,
                description = stringResource(R.string.label_btc_icon),
                address = stringResource(R.string.label_btc_add),
            )
            DonationCard(
                iconRes = R.drawable.usdt,
                description = stringResource(R.string.label_usdt_icon),
                address = stringResource(R.string.label_usdt_add),
            )

            SectionTitle(stringResource(R.string.label_about_title))
            AboutAppContent(context)

            SectionTitle(stringResource(R.string.label_report_an_issue))
            Button(
                onClick = { AppUtil.openLink(context, Constant.GITHUB_ISSUES_URL) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = stringResource(R.string.label_open_github_issue))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAboutScreen() {
    AboutScreen()
}
