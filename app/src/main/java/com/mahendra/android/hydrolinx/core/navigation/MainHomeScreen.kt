package com.mahendra.android.hydrolinx.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.feature.hydration.ui.HydrationScreen
import com.mahendra.android.hydrolinx.feature.maps.ui.MapsScreen
import com.mahendra.android.hydrolinx.feature.points.ui.PointsScreen
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private data class TabItem(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
)

private val TABS = listOf(
    TabItem(AppRoute.Maps, "Home", Icons.Default.LocationOn),
    TabItem(AppRoute.Hydration, "Hydrobit", Icons.Default.LocalDrink),
    TabItem(AppRoute.Points, "Poin", Icons.Default.Stars),
)

private data class DemoInAppNotification(
    val title: String,
    val subtitle: String,
    val timeLabel: String,
)

private data class DemoNotificationContent(
    val title: String,
    val subtitle: String,
)

@Composable
fun MainHomeScreen() {
    val tabController = rememberNavController()
    val currentEntry by tabController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    var showDemoNotification by remember { mutableStateOf(false) }
    var demoNotification by remember { mutableStateOf<DemoInAppNotification?>(null) }

    LaunchedEffect(Unit) {
        delay(DEMO_NOTIFICATION_DELAY_MS)
        val content = DEMO_NOTIFICATION_CONTENTS[
            (System.currentTimeMillis() / DEMO_NOTIFICATION_DELAY_MS % DEMO_NOTIFICATION_CONTENTS.size).toInt()
        ]
        demoNotification = DemoInAppNotification(
            title = content.title,
            subtitle = content.subtitle,
            timeLabel = LocalTime.now().format(NOTIFICATION_CLOCK_FORMAT),
        )
        showDemoNotification = true
        delay(DEMO_NOTIFICATION_VISIBLE_MS)
        showDemoNotification = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        AnimatedVisibility(
            visible = showDemoNotification && demoNotification != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 84.dp)
                .zIndex(10f),
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        ) {
            demoNotification?.let { notification ->
                DemoNotificationCard(
                    notification = notification,
                    onDismiss = { showDemoNotification = false },
                )
            }
        }
    }
}

@Composable
private fun DemoNotificationCard(
    notification: DemoInAppNotification,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDismiss),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, HydrolinxCyan),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = HydrolinxCyan,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = HydrolinxOnSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = notification.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HydrolinxOnSurface,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = notification.timeLabel,
                style = MaterialTheme.typography.labelLarge,
                color = HydrolinxOnSurfaceMuted,
                fontWeight = FontWeight.Bold,
            )
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

private const val DEMO_NOTIFICATION_DELAY_MS = 5_000L
private const val DEMO_NOTIFICATION_VISIBLE_MS = 5_000L
private val NOTIFICATION_CLOCK_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")
private val DEMO_NOTIFICATION_CONTENTS = listOf(
    DemoNotificationContent(
        title = "Hari ini kamu butuh air!",
        subtitle = "Temukan sumber terdekat sekarang.",
    ),
    DemoNotificationContent(
        title = "Sudah waktunya minum.",
        subtitle = "Jangan lewatkan jadwal minummu.",
    ),
)
