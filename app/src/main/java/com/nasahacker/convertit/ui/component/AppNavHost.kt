package com.nasahacker.convertit.ui.component


import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nasahacker.convertit.dto.BottomNavItem
import com.nasahacker.convertit.ui.screen.ConvertsScreen
import com.nasahacker.convertit.ui.screen.HomeScreen


@Composable
fun AppNavHost(activity: Activity, modifier: Modifier = Modifier, controller: NavHostController) {
    NavHost(
        modifier = modifier,
        navController = controller,
        startDestination = BottomNavItem.Home.route
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(activity)
        }
        composable(BottomNavItem.Converts.route) {
            ConvertsScreen()
        }
    }
}
