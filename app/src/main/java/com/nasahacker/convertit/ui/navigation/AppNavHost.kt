package com.nasahacker.convertit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nasahacker.convertit.domain.model.BottomNavigation
import com.nasahacker.convertit.ui.library.LibraryScreen
import com.nasahacker.convertit.ui.home.HomeScreen
import com.nasahacker.convertit.ui.about.AboutScreen
/**
 * Convertit Android app
 * <a href="https://github.com/thebytearray/Convertit">GitHub Repository</a>
 *
 * Created by Tamim Hossain.
 * Copyright (c) 2025 The Byte Array LTD.
 *
 * This file is part of the Convertit Android app.
 *
 * The Convertit Android app is free software: you can redistribute it and/or
 * modify it under the terms of the Apache License, Version 2.0 as published by
 * the Apache Software Foundation.
 *
 * The Convertit Android app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Apache License for more
 * details.
 *
 * You should have received a copy of the Apache License
 * along with the Convertit Android app. If not, see <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache License 2.0</a>.
 *
 * @author Tamim Hossain
 * @company The Byte Array LTD
 * @year 2025
 * @license Apache-2.0
 */


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    controller: NavHostController,
) {

    NavHost(
        modifier = modifier,
        navController = controller,
        startDestination = BottomNavigation.Home.route,
    ) {
        composable(BottomNavigation.Home.route) {
            HomeScreen()
        }
        composable(BottomNavigation.Library.route) {
            LibraryScreen()
        }
        composable("about") {
            AboutScreen()
        }
    }
}
