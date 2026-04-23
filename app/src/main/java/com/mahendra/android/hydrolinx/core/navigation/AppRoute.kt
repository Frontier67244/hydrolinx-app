package com.mahendra.android.hydrolinx.core.navigation

sealed class AppRoute(val route: String) {
    data object Onboarding : AppRoute("onboarding")
    data object MainHome : AppRoute("main_home")
    data object Maps : AppRoute("maps")
    data object Hydration : AppRoute("hydration")
    data object Points : AppRoute("points")
}
