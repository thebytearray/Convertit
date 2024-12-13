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
import com.nasahacker.convertit.ui.composable.AppNavHost
import com.nasahacker.convertit.ui.composable.BottomNavigationBar
import com.nasahacker.convertit.ui.composable.MainAppBar
import com.nasahacker.convertit.ui.screen.HomeScreen
import com.nasahacker.convertit.ui.theme.AppTheme

import com.nasahacker.convertit.util.AppUtil

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
                    }
                ) { innerPadding ->
                    AppNavHost(
                        modifier = Modifier.padding(innerPadding),
                        controller = navController
                    )
                }
            }
        }


        //Handle Notification
        AppUtil.handleNotificationPermission(this)

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMainScreen() {
    HomeScreen(
        viewModel = TODO()
    )
}
