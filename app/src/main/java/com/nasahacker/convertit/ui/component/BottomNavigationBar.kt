package com.nasahacker.convertit.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nasahacker.convertit.dto.BottomNavItem
import com.nasahacker.convertit.R
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
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            BottomNavItem.Home,
            BottomNavItem.Converts
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    val iconRes = if (currentRoute == item.route) {
                        when (item) {
                            BottomNavItem.Home -> R.drawable.home_filled
                            BottomNavItem.Converts -> R.drawable.storage_filled
                        }
                    } else {
                        when (item) {
                            BottomNavItem.Home -> R.drawable.home_outlined
                            BottomNavItem.Converts -> R.drawable.storage_outlined
                        }
                    }
                    Icon(painter = painterResource(id = iconRes), contentDescription = item.label)
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
