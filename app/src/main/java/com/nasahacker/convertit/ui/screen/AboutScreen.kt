package com.nasahacker.convertit.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nasahacker.convertit.R
import com.nasahacker.convertit.dto.Constant
import com.nasahacker.convertit.util.AppUtil

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {


            // Contact Section
            Text(
                text = "Contact Us",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp),
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContactItem(
                    name = "Tamim Hossain (Developer)",
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE) }
                )
                ContactItem(
                    name = "Moontahid (Moderator)",
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE_MOD) }
                )
            }

            // Community Section
            Text(
                text = "Community",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommunityIcon(
                    iconRes = R.drawable.telegram_ic,
                    contentDescription = "Telegram Icon",
                    onClick = { AppUtil.openLink(context, Constant.TELEGRAM_CHANNEL) }
                )
                CommunityIcon(
                    iconRes = R.drawable.discord_ic,
                    contentDescription = "Discord Icon",
                    onClick = { AppUtil.openLink(context, Constant.DISCORD_CHANNEL) }
                )
                CommunityIcon(
                    iconRes = R.drawable.github_ic,
                    contentDescription = "GitHub - Tamim Hossain",
                    onClick = { AppUtil.openLink(context, Constant.GITHUB_PROFILE) }
                )
            }

            // About App Section
            Text(
                text = "About App",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp),
                textAlign = TextAlign.Center
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = context.getString(R.string.label_about_app),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Start
                )
            }
            Text(
                text = "Apache 2.0",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ContactItem(name: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable(onClick = {onClick()})
        )

    }
}

@Composable
fun CommunityIcon(iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewAboutScreen() {
    AboutScreen()
}
