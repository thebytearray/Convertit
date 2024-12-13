package com.nasahacker.convertit.dto


sealed class BottomNavItem(val route: String, val label: String) {
    object Home : BottomNavItem("home", "Home")
    object Converts : BottomNavItem("converts", "Converts")
}
