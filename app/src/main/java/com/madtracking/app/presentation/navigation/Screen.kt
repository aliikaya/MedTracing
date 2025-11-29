package com.madtracking.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Profiles : Screen("profiles")
    
    data object Today : Screen("today/{profileId}") {
        fun createRoute(profileId: Long): String = "today/$profileId"
    }
    
    data object AddMedication : Screen("add_medication/{profileId}") {
        fun createRoute(profileId: Long): String = "add_medication/$profileId"
    }
    
    data object MedicationHistory : Screen("medication_history/{medicationId}") {
        fun createRoute(medicationId: Long): String = "medication_history/$medicationId"
    }
}
