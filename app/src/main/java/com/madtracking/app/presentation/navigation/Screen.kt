package com.madtracking.app.presentation.navigation

sealed class Screen(val route: String) {
    object Profiles : Screen("profiles")
    object Today : Screen("today/{profileId}") {
        fun createRoute(profileId: Long): String = "today/$profileId"
    }
}

