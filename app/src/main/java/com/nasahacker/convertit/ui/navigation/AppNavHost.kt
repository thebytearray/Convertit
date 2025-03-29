package com.nasahacker.convertit.ui.navigation


import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nasahacker.convertit.ui.screen.ConvertsScreen
import com.nasahacker.convertit.ui.screen.HomeScreen
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
fun AppNavHost(activity: Activity, modifier: Modifier = Modifier, controller: NavHostController) {
    NavHost(
        modifier = modifier,
        navController = controller,
        startDestination = BottomNavigation.Home.route
    ) {
        composable(BottomNavigation.Home.route) {
            HomeScreen(activity)
        }
        composable(BottomNavigation.Library.route) {
            ConvertsScreen()
        }
    }
}
