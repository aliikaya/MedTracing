package com.medtracking.app.presentation.navigation

sealed class Screen(val route: String) {
    // Auth screens
    data object Login : Screen("login")
    data object Register : Screen("register")
    
    // Main app screens
    data object Profiles : Screen("profiles")
    
    data object Main : Screen("main/{profileId}") {
        fun createRoute(profileId: Long): String = "main/$profileId"
    }
    
    data object Today : Screen("today/{profileId}") {
        fun createRoute(profileId: Long): String = "today/$profileId"
    }
    
    data object AddMedication : Screen("add_medication/{profileId}") {
        fun createRoute(profileId: Long): String = "add_medication/$profileId"
    }
    
    data object MedicationHistory : Screen("medication_history/{medicationId}") {
        fun createRoute(medicationId: Long): String = "medication_history/$medicationId"
    }

    data object Invite : Screen("invite/{invitationId}/{token}") {
        fun createRoute(invitationId: String, token: String): String = "invite/$invitationId/$token"
    }
}
