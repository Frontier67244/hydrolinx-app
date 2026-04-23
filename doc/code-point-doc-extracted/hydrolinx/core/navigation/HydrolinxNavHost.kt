package com.mahendra.android.hydrolinx.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mahendra.android.hydrolinx.feature.onboarding.ui.OnboardingScreen

@Composable
fun HydrolinxNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppRoute.Onboarding.route,
    ) {
        composable(AppRoute.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(AppRoute.MainHome.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoute.MainHome.route) {
            MainHomeScreen()
        }
    }
}
