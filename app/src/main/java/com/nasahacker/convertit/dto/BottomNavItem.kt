package com.nasahacker.convertit.dto


sealed class BottomNavItem(val route: String, val label: String) {
    data object Home : BottomNavItem("home", "Home")
    data object Converts : BottomNavItem("library ", "Library ")
}
