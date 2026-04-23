package com.mahendra.android.hydrolinx.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mahendra.android.hydrolinx.feature.hydration.ui.HydrationScreen
import com.mahendra.android.hydrolinx.feature.maps.ui.MapsScreen
import com.mahendra.android.hydrolinx.feature.points.ui.PointsScreen

private data class TabItem(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
)

private val TABS = listOf(
    TabItem(AppRoute.Maps, "Maps", Icons.Default.LocationOn),
    TabItem(AppRoute.Hydration, "Hydrate", Icons.Default.LocalDrink),
    TabItem(AppRoute.Points, "Points", Icons.Default.Stars),
)

@Composable
fun MainHomeScreen() {
    val tabController = rememberNavController()
    val currentEntry by tabController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                TABS.forEach { tab ->
                    val selected = currentRoute?.let { r ->
                        currentEntry?.destination?.hierarchy?.any { it.route == tab.route.route }
                    } == true || currentRoute == tab.route.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navigateToTab(tabController, tab.route.route) },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = tabController,
            startDestination = AppRoute.Maps.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(AppRoute.Maps.route) { MapsScreen() }
            composable(AppRoute.Hydration.route) { HydrationScreen() }
            composable(AppRoute.Points.route) { PointsScreen() }
        }
    }
}

private fun navigateToTab(controller: NavController, route: String) {
    controller.navigate(route) {
        popUpTo(controller.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
