package com.medtracking.app.domain.model

/**
 * Role-Based Access Control (RBAC) for shared profiles.
 * Defines what actions a member can perform on a shared profile.
 */
enum class MemberRole {
    /**
     * Profile owner - Full access to everything.
     * Can edit medications, mark intakes, delete profile, manage members.
     */
    OWNER,

    /**
     * Caregiver with editing rights.
     * Can add/edit/delete medications, mark intakes, but cannot delete profile or change owner.
     */
    CAREGIVER_EDITOR,

    /**
     * Patient or family member who can only mark intakes.
     * Cannot add/edit medications, only mark TAKEN/MISSED.
     */
    PATIENT_MARK_ONLY,

    /**
     * View-only access.
     * Can only view medications and intake history, no modifications.
     */
    VIEWER;

    /**
     * Returns true if this role can add/edit/delete medications.
     */
    fun canEditMedications(): Boolean = when (this) {
        OWNER, CAREGIVER_EDITOR -> true
        PATIENT_MARK_ONLY, VIEWER -> false
    }

    /**
     * Returns true if this role can mark intakes as TAKEN/MISSED.
     */
    fun canMarkIntakes(): Boolean = when (this) {
        OWNER, CAREGIVER_EDITOR, PATIENT_MARK_ONLY -> true
        VIEWER -> false
    }

    /**
     * Returns true if this role can manage members (invite, remove).
     */
    fun canManageMembers(): Boolean = when (this) {
        OWNER -> true
        else -> false
    }

    /**
     * Returns true if this role can delete the profile.
     */
    fun canDeleteProfile(): Boolean = when (this) {
        OWNER -> true
        else -> false
    }

    /**
     * User-friendly Turkish display name for UI.
     */
    fun displayName(): String = when (this) {
        OWNER -> "Sahip"
        CAREGIVER_EDITOR -> "Düzenleyebilir"
        PATIENT_MARK_ONLY -> "Hasta"
        VIEWER -> "İzleyici"
    }

    companion object {
        /**
         * Parse role from string (e.g., from Firestore).
         * Returns VIEWER as fallback for unknown values.
         */
        fun fromString(value: String?): MemberRole {
            return entries.find { it.name == value } ?: VIEWER
        }
    }
}

