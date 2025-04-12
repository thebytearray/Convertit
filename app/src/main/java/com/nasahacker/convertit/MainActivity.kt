package com.nasahacker.convertit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.nasahacker.convertit.ui.component.BottomNavigationBar
import com.nasahacker.convertit.ui.component.MainAppBar
import com.nasahacker.convertit.ui.navigation.AppNavHost
import com.nasahacker.convertit.ui.theme.AppTheme
import com.nasahacker.convertit.util.AppUtil

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = {
                        MainAppBar()
                    },
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    },
                ) { innerPadding ->
                    AppNavHost(
                        this,
                        modifier = Modifier.padding(innerPadding),
                        controller = navController,
                    )
                }
            }
        }

        AppUtil.handleNotificationPermission(this)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
}
